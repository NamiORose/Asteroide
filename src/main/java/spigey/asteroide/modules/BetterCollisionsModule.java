package spigey.asteroide.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import java.util.List;
import spigey.asteroide.AsteroideAddon;


public class BetterCollisionsModule extends Module {
    public BetterCollisionsModule() {
        super(AsteroideAddon.CATEGORY, "Better-Collisions", "Meteor Client's Collisions, but better");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks should be added collision box.")
        .build()
    );
    private final Setting<Boolean> magma = sgGeneral.add(new BoolSetting.Builder()
        .name("magma")
        .description("Prevents you from walking over magma blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> unloadedChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("unloaded-chunks")
        .description("Stops you from going into unloaded chunks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreBorder = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-border")
        .description("Removes world border collision.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (!event.state.getFluidState().isEmpty()) return;
        if (blocks.get().contains(event.state.getBlock())) {
            event.shape = Shapes.block();
        } else if (magma.get() && !mc.player.isShiftKeyDown()
            && event.state.isAir()
            && mc.level.getBlockState(event.pos.below()).getBlock() == Blocks.MAGMA_BLOCK) {
            event.shape = Shapes.block();
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!unloadedChunks.get()) return;
        if (event.packet instanceof ServerboundMoveVehiclePacket packet) {
            if (!mc.level.getChunkSource().hasChunk((int) packet.position().x() >> 4, (int) packet.position().z() >> 4)) {
                mc.player.getVehicle().absMoveTo(mc.player.getVehicle().xo, mc.player.getVehicle().yo, mc.player.getVehicle().zo);
                event.cancel();
            }
        } else if (event.packet instanceof ServerboundMovePlayerPacket packet) {
            if (!mc.level.getChunkSource().hasChunk((int) packet.getX(mc.player.getX()) >> 4, (int) packet.getZ(mc.player.getZ()) >> 4)) {
                event.cancel();
            }
        }
    }
}
