package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spigey.asteroide.AsteroideAddon;
import spigey.asteroide.modules.RTCSettingsModule;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void modifyPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        final RTCSettingsModule rtc = Modules.get().get(RTCSettingsModule.class);
        if(!tooLazyForThisShit(entry.getProfile().name()) || !rtc.disableIcon.get()) return;
        cir.setReturnValue(Component.empty().append("\uE429 ").append(cir.getReturnValue()));
    }

    @Unique
    private boolean tooLazyForThisShit(String username){
        for(String user : AsteroideAddon.users) { if(username.contains(user.replaceAll("§[a-z0-9]", ""))) return true; }
        return username.contains(mc.player.getName().getString());
    }
}
