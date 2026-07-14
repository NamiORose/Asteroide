package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

import java.util.Objects;

public class CloseCommand extends Command {
    public CloseCommand() {
        super("close", "Closes the currently open GUI.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            try{
                int syncId = Objects.requireNonNull(mc.player).containerMenu.containerId;
                mc.player.clientSideCloseContainer();
                mc.player.connection.send(new ServerboundContainerClosePacket(syncId));
            }catch(Exception L){/**/}
            return SINGLE_SUCCESS;
        });
    }
}
