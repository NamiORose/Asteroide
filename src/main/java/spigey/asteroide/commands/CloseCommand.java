package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class CloseCommand extends Command {
    public CloseCommand() {
        super("close", "Closes the currently open GUI.");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            try{
                int syncId = mc.player.containerMenu.containerId;
                mc.player.clientSideCloseContainer();
                mc.player.connection.send(new ServerboundContainerClosePacket(syncId));
            }catch(Exception L){/**/}
            return SINGLE_SUCCESS;
        });
    }
}
