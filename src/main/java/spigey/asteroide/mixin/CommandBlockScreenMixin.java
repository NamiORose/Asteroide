package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spigey.asteroide.modules.BetterAntiCrashModule;

@Mixin(CommandBlockEditScreen.class)
public class CommandBlockScreenMixin {
    @Inject(method = "updateGui", at = @At("TAIL"), cancellable = true)
    private void init(CallbackInfo ci) {
        BetterAntiCrashModule bac = Modules.get().get(BetterAntiCrashModule.class);
        EditBox command = ((CommandAccessor) this).getCommand();
        if(!bac.isActive() || !bac.commandBlockCrash.get()) return;
        command.setValue(command.getValue().substring(0, Math.min(bac.commandBlockThreshold.get(), command.getValue().length())));
        ci.cancel();
    }
}

