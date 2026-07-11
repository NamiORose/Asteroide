package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import spigey.asteroide.AsteroideAddon;
import spigey.asteroide.util;


public class PlatformFlyModule extends Module {
    public PlatformFlyModule() {
        super(AsteroideAddon.CATEGORY, "Air-Walk", "Lets you walk on air");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> allowJumping = sgGeneral.add(new BoolSetting.Builder()
        .name("Ease Jumping (Buggy)")
        .description("Makes getting up easier")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> allowSneaking = sgGeneral.add(new BoolSetting.Builder()
        .name("Allow Sneaking")
        .description("Allows sneaking while in the air")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event){
        assert mc.player != null;
        boolean sneaking = mc.options.keyShift.isDown();
        boolean jumping = mc.options.keyJump.isDown();
        if(sneaking && !allowSneaking.get()){return;}
        if(jumping && allowJumping.get() && (util.randomNum(1,5) == 3)){mc.player.jumpFromGround();} // I was too lazy to add a proper delay
        int PlayerX = mc.player.blockPosition().getX();
        int PlayerY = mc.player.blockPosition().getY();
        int PlayerZ = mc.player.blockPosition().getZ();
        BlockPos pos = event.pos;
        BlockPos lock = new BlockPos(PlayerX, PlayerY - 1, PlayerZ);
        if(lock.equals(pos)) event.shape = Shapes.block();
    }
}


