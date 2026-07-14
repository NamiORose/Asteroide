package spigey.asteroide;

import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class util {
    public static String perm(int lvl){
        return "You do not have the required permission level of " + lvl + ", the command will most likely not work!";
    }

    public static void msg(String message) {
        ChatUtils.sendPlayerMsg(message);
    }

    private static final Permission PERMISSION_LEVEL_0 = new Permission.HasCommandLevel(PermissionLevel.ALL);
    private static final Permission PERMISSION_LEVEL_1 = new Permission.HasCommandLevel(PermissionLevel.MODERATORS);
    private static final Permission PERMISSION_LEVEL_2 = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);
    private static final Permission PERMISSION_LEVEL_3 = new Permission.HasCommandLevel(PermissionLevel.ADMINS);
    private static final Permission PERMISSION_LEVEL_4 = new Permission.HasCommandLevel(PermissionLevel.OWNERS);

    // todo: remove this
    public static int getPermissionLevel(){
        if (mc.player == null)
            return 0;

        final PermissionSet permissions = mc.player.permissions();
        if (permissions.hasPermission(PERMISSION_LEVEL_4))
            return 4;

        if (permissions.hasPermission(PERMISSION_LEVEL_3))
            return 3;

        if (permissions.hasPermission(PERMISSION_LEVEL_2))
            return 2;

        if (permissions.hasPermission(PERMISSION_LEVEL_1))
            return 1;

        return 0;
    }

    public static double meth(final String str) { // Skidded off stack overflow
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    private static final Random random = new Random();
    public static int randomNum(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static boolean BoolContains(boolean[] arr, boolean val){
        for(boolean item : arr) if(item == val) return true;
        return false;
    }

    public static String PlayerDir(double yaw) {
        int direction = Math.floorMod((int) Math.round(yaw / 90.0), 4);
        return switch (direction) {
            case 0 -> "south";
            case 1 -> "west";
            case 2 -> "north";
            case 3 -> "east";
            default -> "wtf";
        };
    }

    public static MutableComponent getCopyButton(String copy){
        MutableComponent Button = Component.literal("[COPY]");
        Button.setStyle(Button.getStyle()
            .applyFormat(ChatFormatting.GREEN)
            .withClickEvent(new ClickEvent.CopyToClipboard(copy))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to Copy")))
        );
        return Button;
    }

    public static MutableComponent getSendButton(MutableComponent message, String send){
        MutableComponent Button = Component.literal("[SEND]");
        Button.setStyle(Button.getStyle()
            .applyFormat(ChatFormatting.GREEN)
            .withClickEvent(new ClickEvent.RunCommand(Commands.get("say").toString(send)))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to Send")))
        );
        return message.append(" ").append(Button);
    }

    public static String withoutStyle(Component text){
        StringBuilder sb = new StringBuilder();
        for(Component str : text.toFlatList()) sb.append(str.getString());
        return sb.toString();
    }

    private static SecretKey getKey(String key) throws Exception {
        byte[] salt = "fixed-salt".getBytes(StandardCharsets.UTF_8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static String encrypt(String plainText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(key), new GCMParameterSpec(128, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 69, 42}));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String cipherText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getKey(key), new GCMParameterSpec(128, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 69, 42}));
        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
    }
}
