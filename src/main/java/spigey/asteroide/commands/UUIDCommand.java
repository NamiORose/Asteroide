package spigey.asteroide.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.UUID;

public class UUIDCommand extends Command {
    public UUIDCommand() {
        super("uuid", "Shows you a players UUID.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            display(Objects.requireNonNull(mc.player).getUUID());
            return SINGLE_SUCCESS;
        });

        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile player = PlayerListEntryArgumentType.get(context).getProfile();

            if (player != null) display(player.id());
            else error("Player not found!");
            return SINGLE_SUCCESS;
        }));
    }

    private void display(UUID uuid){
        log("§f-------------------------------");
        mc.player.sendSystemMessage(Component.literal("§8[§cUUID§8] §7UUID: ").append(getButton(uuid.toString())));
        mc.player.sendSystemMessage(Component.literal("§8[§cUUID§8] §7Compact: ").append(getButton(uuid.toString().replaceAll("-", ""))));
        mc.player.sendSystemMessage(Component.literal("§8[§cUUID§8] §7Numeric: "));
        final IntArrayTag nbtUuid = new IntArrayTag(UUIDUtil.uuidToIntArray(uuid));
        mc.player.sendSystemMessage(Component.literal("§8[§cUUID§8] §7").append(getButton(nbtUuid.toString())));
        log("§f-------------------------------");
    }

    private String random(){ return "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)) + "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)) + "§" + "0123456789abcdefklmnor".charAt((int)(Math.random()*22)); }
    private void log(String message, String... args){ mc.player.sendSystemMessage(Component.nullToEmpty(String.format("§8[§cUUID§8] §7%s%s", String.format(message, (Object[]) args), random()))); }

    private MutableComponent getButton(String uuid){
        return Component.literal(String.format("§8%s", uuid)).withStyle(style -> style
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7Click to copy")))
            .withClickEvent(new ClickEvent.CopyToClipboard(uuid))
        );
    }
}
