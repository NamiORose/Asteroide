package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.HighJump;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.StairBlock;
import spigey.asteroide.AsteroideAddon;

public class FastStaircaseModule extends Module {
    public FastStaircaseModule() {super(AsteroideAddon.CATEGORY, "Fast-Staircase", "Makes you walk up stairs quickly");}
    // For some time I actually thought this doesn't work, until I played on a server with AC, and I was so fast that I flagged the anticheat
    @EventHandler
    private void onTick(TickEvent.Post event){
        if(!isActive()){return;}
        if(!BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(mc.player.blockPosition()).getBlock()).toString().endsWith("_stairs")) return;
        boolean asd = Modules.get().get(HighJump.class).isActive();
        if(asd) Modules.get().get(HighJump.class).toggle();
        if(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(mc.player.blockPosition()).getBlock()).toString().endsWith("_stairs")) if(mc.player.getDirection() == mc.level.getBlockState(mc.player.blockPosition()).getValue(StairBlock.FACING) && mc.player.input.forwardImpulse > 0) mc.player.jumpFromGround();
        if(asd) Modules.get().get(HighJump.class).toggle();
    }
}
