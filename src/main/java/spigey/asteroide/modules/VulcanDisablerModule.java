package spigey.asteroide.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import spigey.asteroide.AsteroideAddon;

import java.util.List;

public class VulcanDisablerModule extends Module {
    public VulcanDisablerModule() {
        super(AsteroideAddon.CATEGORY, "Vulcan-Disabler", "Movement Bypass for Vulcan AC 2.9.7.5 and under. Requires wearing an elytra.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("Jump")
        .description("Jump")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> muteTips = sgGeneral.add(new BoolSetting.Builder()
        .name("Mute Tips")
        .description("Mute tips.")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate(){
        if(((List<ItemStack>) mc.player.getArmorSlots()).get(2).getItem() != Items.ELYTRA){
            if(!muteTips.get()) info("You need to wear an elytra!");
            toggle();
            return;
        }
        if(jump.get()) mc.player.jumpFromGround();
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING)); // Yes, it's that easy
        if(!muteTips.get()) info("Flight & Speed should now work. You can take off the elytra");
        toggle();
    }
}
