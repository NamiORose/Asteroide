package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class MeCommand extends Command {
    public MeCommand() {
        super("me", "Basic information for devs");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            info("§f------ Info ------");
            info("§fUsername: §7" + mc.getUser().getName());
            info("§fUUID: §7" + mc.getUser().getProfileId());
            info("§fAccountType: §7" + "<???>"); // TODO: BREAKING CHANGE
            info("§fServer: §7" + mc.getSingleplayerServer());
            info("§fScreen: §7" + mc.screen);
            return SINGLE_SUCCESS;
        });
    }
}
