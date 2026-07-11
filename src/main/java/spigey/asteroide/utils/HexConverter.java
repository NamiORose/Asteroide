package spigey.asteroide.utils;

import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexConverter {
    public static MutableComponent toText(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        MutableComponent out = Component.empty();
        Matcher matcher = Pattern.compile("§(#[0-9a-fA-F]{6}|[0-9a-fk-orA-FK-OR])").matcher(text);

        int last = 0;
        Integer customColor = null;
        List<ChatFormatting> formats = new ArrayList<>();

        while (matcher.find()) {
            if (matcher.start() > last) {
                MutableComponent seg = Component.literal(text.substring(last, matcher.start()));
                if (customColor != null) seg = seg.withColor(customColor);
                if (!formats.isEmpty()) seg = seg.withStyle(formats.toArray(new ChatFormatting[0]));
                out.append(seg);
            }

            String code = matcher.group(1);

            if (code.startsWith("#")) {
                customColor = fromHex(code);
                formats.removeIf(ChatFormatting::isColor);
            } else {
                ChatFormatting f = ChatFormatting.getByCode(Character.toLowerCase(code.charAt(0)));
                if (f == ChatFormatting.RESET) { formats.clear(); customColor = null;
                } else if (f != null) {
                    if (f.isColor()) { formats.removeIf(ChatFormatting::isColor); formats.add(f); customColor = null;}
                    else formats.add(f);
                }
            }

            last = matcher.end();
        }

        if (last < text.length()) {
            MutableComponent seg = Component.literal(text.substring(last));
            if (customColor != null) seg = seg.withColor(customColor);
            if (!formats.isEmpty()) seg = seg.withStyle(formats.toArray(new ChatFormatting[0]));
            out.append(seg);
        }

        return out;
    }

    private static int fromHex(String hex){
        int r = Integer.valueOf( hex.substring( 1, 3 ), 16 );
        int g = Integer.valueOf( hex.substring( 3, 5 ), 16 );
        int b = Integer.valueOf( hex.substring( 5, 7 ), 16 );
        return Color.fromRGBA(r, g, b, 255);
    }
}
