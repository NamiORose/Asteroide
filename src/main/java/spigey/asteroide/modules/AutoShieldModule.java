package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.simulator.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.entity.simulator.SimulationStep;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import spigey.asteroide.AsteroideAddon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AutoShieldModule extends Module {
    public AutoShieldModule() { super(AsteroideAddon.CATEGORY, "Auto-Shield", "Automatically blocks when an arrow is about to hit you"); }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private Setting<Integer> checkRange = sgGeneral.add(new IntSetting.Builder()
        .name("Check Range")
        .description("Range to check for projectiles")
        .defaultValue(10)
        .sliderRange(0, 20)
        .build()
    );
    private Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Shield Items")
        .description("Items to consider as shield")
        .defaultValue(List.of(Items.SHIELD))
        .build()
    );
    private Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("Entities")
        .description("Entities to consider as projectiles")
        .defaultValue(Set.of(
            EntityType.TRIDENT,
            EntityType.ARROW,
            EntityType.SPECTRAL_ARROW,
            EntityType.EGG,
            EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL,
            EntityType.WIND_CHARGE,
            EntityType.TNT
        ))
        .build()
    );
    private Setting<Boolean> directionCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("Direction Check")
        .description("Whether to check whether the projectile is traveling towards you")
        .defaultValue(true)
        .build()
    );
    private Setting<Boolean> pointToProjectile = sgGeneral.add(new BoolSetting.Builder()
        .name("Point to Entity")
        .description("Look at the entity while it's shooting to make sure the shield actually blocks it")
        .defaultValue(false)
        .build()
    );

    private static final ProjectileEntitySimulator.MotionData META_PROJECTILE = new ProjectileEntitySimulator.MotionData(0, 0, 0.05, 1, 0.6F, null);

    private boolean blocking = false;
    private int tick = -1;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(this.tick > 0) { this.tick--; return; }
        if(this.tick == -1) return;
        if(!this.blocking) return;
        mc.options.keyUse.setDown(false);
        this.blocking = false;
        this.tick = -1;
    }

    @EventHandler
    private void onRender(Render3DEvent event){
        for(Entity entity : mc.level.entitiesForRendering()){
            if(!entities.get().contains(entity.getType())) continue;
            if(mc.player.distanceTo(entity) > checkRange.get()) continue;
            if(!isHoldingShield()) continue;
            if(entity.getDeltaMovement().lengthSqr() <= 0) continue;
            if(directionCheck.get() && !isLookingAtUs(entity)) continue;
            mc.options.keyUse.setDown(true);
            this.blocking = true;
            if(pointToProjectile.get()) mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, entity.position());
            this.tick = 5;
            return;
        }
    }

    private boolean isLookingAtUs(Entity entity) {
        ProjectileEntitySimulator sim = new ProjectileEntitySimulator();
        if(entity instanceof Projectile) {
            if (!sim.set(entity))
                return false;
        } else
            sim.set(entity, 0, true, 0.125F, META_PROJECTILE); // todo: validate

        for(int i = 0; i < 100; i++) {
            final SimulationStep step = sim.tick();
            if(step == null) continue;
            if (step.hitResults.length > 0) {
                for (int j = 0; j < step.hitResults.length; j++)
                    if (step.hitResults[j].getType() == HitResult.Type.ENTITY &&
                        ((EntityHitResult)step.hitResults[j]).getEntity() == mc.player
                    )
                        return true;
                return false;
            }
            if(step.shouldStop) return false; // todo: validate
        }
        return false;
    }

    private boolean isHoldingShield() { return items.get().contains(Objects.requireNonNull(mc.player).getOffhandItem().getItem()) || items.get().contains(mc.player.getMainHandItem().getItem()); }
}
