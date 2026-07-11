package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spigey.asteroide.AsteroideAddon;

public class TrackerModule extends Module {
    private final double[] last = new double[3];
    private long lastTickTime = 0;
    private double partialTicks;

    public TrackerModule() {
        super(AsteroideAddon.CATEGORY, "Tracker", "Tracks a player by always looking at it. Use with .track <player>");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> interpolation = sgGeneral.add(new DoubleSetting.Builder()
        .name("interpolation")
        .description("Smoothing factor to prevent motion sickness")
        .defaultValue(0.2)
        .min(0)
        .max(100)
        .sliderMax(3)
        .build()
    );

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a yellow box on the tracked player when enabled.")
        .defaultValue(true)
        .build()
    );

    @Override
    public void onActivate() {
        lastTickTime = System.currentTimeMillis();
        if(AsteroideAddon.trackedPlayer != null && mc.getConnection() != null &&
            mc.getConnection().getOnlinePlayers().stream().noneMatch(player -> player.getProfile().getName().equals(AsteroideAddon.trackedPlayer))) {
            ChatUtils.sendMsg(Component.nullToEmpty("§7Tracker enabled, use " + Config.get().prefix.get() + "track <player> to start tracking someone. §cThe tracked Player must be loaded!"));
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if(mc.player == null) return;
        long currentTickTime = System.currentTimeMillis();
        partialTicks = (currentTickTime - lastTickTime) / 50.0;
        lastTickTime = currentTickTime;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (AsteroideAddon.trackedPlayer == null || mc.player == null) return;
        Player entity = null;
        for (AbstractClientPlayer player : mc.level.players()) {
            if (player.getGameProfile().getName().equals(AsteroideAddon.trackedPlayer)) {
                entity = player;
                break;
            }
        }
        if (entity == null) {
            mc.gui.setOverlayMessage(Component.nullToEmpty("§cPlayer not found, pointing to last known location"), false);
            mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(AsteroideAddon.lastPos[0], AsteroideAddon.lastPos[1], AsteroideAddon.lastPos[2]));
            return;
        }

        AsteroideAddon.lastPos = new double[]{entity.getX(), entity.getY(), entity.getZ()};
        mc.gui.setOverlayMessage(Component.nullToEmpty(String.format("§7Tracking %s at §cX: %.0f§7, §aY: %.0f§7, §9Z: %.0f", AsteroideAddon.trackedPlayer, entity.getX(), entity.getY(), entity.getZ())), false);
        double[] pos = {entity.getX(), entity.getY(), entity.getZ()};

        for (int i = 0; i < 3; i++) {
            last[i] = Mth.lerp(partialTicks * interpolation.get(), last[i], i == 1 ? entity.getEyeY() : pos[i]);
        }

        if(render.get()) draw(event, entity);
        mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(last[0], last[1], last[2]));
    }


    private void draw(Render3DEvent event, Entity entity) {
        double x = Mth.lerp(event.tickDelta, entity.xOld, entity.getX()) - entity.getX();
        double y = Mth.lerp(event.tickDelta, entity.yOld, entity.getY()) - entity.getY();
        double z = Mth.lerp(event.tickDelta, entity.zOld, entity.getZ()) - entity.getZ();

        AABB box = entity.getBoundingBox();
        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, new Color(255, 255, 0, 128), new Color(255, 255, 0, 255), ShapeMode.Both, 0);
    }
}
