package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;

public class MeCommand extends Command {
    public MeCommand() {
        super("me", "Basic information for devs");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            info("§f------ Info ------");
            info("§fUsername: §7" + mc.getUser().getName());
            info("§fUUID: §7" + mc.getUser().getProfileId());
            info("§fAccountType: §7" + mc.getUser().getType());
            info("§fServer: §7" + mc.getSingleplayerServer());
            info("§fScreen: §7" + mc.screen);
            return SINGLE_SUCCESS;
        });
    }
}
