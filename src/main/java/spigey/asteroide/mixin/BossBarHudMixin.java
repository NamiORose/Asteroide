package spigey.asteroide.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import spigey.asteroide.modules.BetterAntiCrashModule;

@Mixin(BossHealthOverlay.class)
public class BossBarHudMixin {
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;"))
    public Component modifyBossBarName(Component original, @Local(print = true) LerpingBossEvent event) {
        BetterAntiCrashModule bac = Modules.get().get(BetterAntiCrashModule.class);
        return bac.isActive() && bac.bossBarLimit.get() && bac.ThresholdLength.get() < original.getString().length() ? Component.nullToEmpty(String.format("§c[Bossbar with length %s blocked]", bac.getMessage(original.getString()))) : original;
    }
}
