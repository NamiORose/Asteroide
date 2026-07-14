package spigey.asteroide.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class CopyCommand extends Command {
    public CopyCommand() {
        super("copyitem", "Copy someone's items.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create())
            .then(literal("MAIN_HAND").executes(context -> {
                execute(PlayerListEntryArgumentType.get(context), InteractionHand.MAIN_HAND);
                return SINGLE_SUCCESS;
            }))
            .then(literal("OFF_HAND").executes(context -> {
                execute(PlayerListEntryArgumentType.get(context), InteractionHand.OFF_HAND);
                return SINGLE_SUCCESS;
            }))
            .executes(context -> {
                execute(PlayerListEntryArgumentType.get(context), InteractionHand.MAIN_HAND);
                return SINGLE_SUCCESS;
            })
        );
    }

    private void execute(PlayerInfo player, InteractionHand hand){
        for(Entity entity : mc.level.entitiesForRendering()){
            if(entity instanceof Player plr && Objects.equals(entity.getName().getString(), player.getProfile().name())){ // There definitely is a better way.
                InteractionHand ph = getHand();
                mc.player.setItemInHand(ph, plr.getItemInHand(hand).copy());
                mc.getConnection().send(new ServerboundSetCreativeModeSlotPacket(ph == InteractionHand.OFF_HAND ? 45 : 36 + mc.player.getInventory().getSelectedSlot(), plr.getItemInHand(hand).copy()));
                final ItemStack itemStack = plr.getItemInHand(hand);
                final Component itemStackName = itemStack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
                info(String.format("Copied §f%s§7 from §f%s§7.", itemStackName.getString(), plr.getName().getString()));
                return;
            }
        }
    }

    private InteractionHand getHand(){
        if(mc.player.getMainHandItem().isEmpty()) return InteractionHand.MAIN_HAND;
        else if(mc.player.getOffhandItem().isEmpty()) return InteractionHand.OFF_HAND;
        else return InteractionHand.MAIN_HAND;
    }
}
