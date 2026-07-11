package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import spigey.asteroide.AsteroideAddon;

public class SafeElytra extends Module {
    public SafeElytra() { super(AsteroideAddon.CATEGORY, "Safe-Elytra", "Attempts to prevent dying from kinetic energy."); }

    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgValues = settings.createGroup("Values");
    private final Setting<Boolean> verticalVelocity = sgGeneral.add(new BoolSetting.Builder()
        .name("Vertical Velocity")
        .description("Prevents high vertical velocity.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> walls = sgGeneral.add(new BoolSetting.Builder()
        .name("Walls")
        .description("Prevents high velocity when looking at walls.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> elytraDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable Elytra")
        .description("Disables elytra when flying at a wall too quickly.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> maxVelocity = sgValues.add(new DoubleSetting.Builder()
        .name("Max Fall Velocity")
        .description("The maximum velocity you can fall at.")
        .defaultValue(0.5)
        .sliderMax(3)
        .visible(verticalVelocity::get)
        .build()
    );
    private final Setting<Double> maxWallVelocity = sgValues.add(new DoubleSetting.Builder()
        .name("Max Wall Velocity")
        .description("The maximum velocity you can move at when looking at a wall.")
        .defaultValue(0.3)
        .sliderMax(3)
        .visible(walls::get)
        .build()
    );
    private final Setting<Integer> wallRange = sgValues.add(new IntSetting.Builder()
        .name("Wall Range")
        .description("Wall raycast range")
        .defaultValue(20)
        .min(0)
        .sliderMax(50)
        .visible(walls::get)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(!mc.player.hasPose(Pose.FALL_FLYING) || !isActive()) return;
        if(verticalVelocity.get() && mc.player.getDeltaMovement().y < -maxVelocity.get()) mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -maxVelocity.get(), mc.player.getDeltaMovement().z);
        if(!walls.get()) return;
        if(mc.player.pick(wallRange.get(), 0f, false).getType() != HitResult.Type.BLOCK) return;
        Vec3 vel = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(
            Mth.clamp(vel.x, -maxWallVelocity.get(), maxWallVelocity.get()),
            Mth.clamp(vel.y, -maxWallVelocity.get(), maxWallVelocity.get()),
            Mth.clamp(vel.z, -maxWallVelocity.get(), maxWallVelocity.get())
        );
        if(!elytraDisable.get()) return;
        mc.player.fallDistance = 0;
        mc.player.setOnGround(true);
        mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, true));
    }
}


