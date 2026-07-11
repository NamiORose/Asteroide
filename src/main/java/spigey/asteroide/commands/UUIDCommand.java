package spigey.asteroide.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import java.util.UUID;

public class UUIDCommand extends Command {
    public UUIDCommand() {
        super("uuid", "Shows you a players UUID.");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            display(mc.player.getUUID());
            return SINGLE_SUCCESS;
        });

        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile player = PlayerListEntryArgumentType.get(context).getProfile();

            if (player != null) display(player.getId());
            else error("Player not found!");
            return SINGLE_SUCCESS;
        }));
    }

    private void display(UUID uuid){
        log("§f-------------------------------");
        mc.player.displayClientMessage(Component.literal("§8[§cUUID§8] §7UUID: ").append(getButton(uuid.toString())), false);
        mc.player.displayClientMessage(Component.literal("§8[§cUUID§8] §7Compact: ").append(getButton(uuid.toString().replaceAll("-", ""))), false);
        mc.player.displayClientMessage(Component.literal("§8[§cUUID§8] §7Numeric: "), false);
        mc.player.displayClientMessage(Component.literal("§8[§cUUID§8] §7").append(getButton(NbtUtils.createUUID(uuid).toString())), false);
        log("§f-------------------------------");
    }

    private String random(){ return "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)) + "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)) + "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)); }
    private void log(String message, String... args){ mc.player.displayClientMessage(Component.nullToEmpty(String.format("§8[§cUUID§8] §7%s%s", String.format(message, (Object[]) args), random())), false); }

    private MutableComponent getButton(String uuid){
        return Component.literal(String.format("§8%s", uuid)).withStyle(style -> style
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,  Component.literal("§7Click to copy")))
            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))
        );
    }
}
