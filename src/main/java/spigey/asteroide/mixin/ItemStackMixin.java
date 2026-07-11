package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spigey.asteroide.modules.BetterAntiCrashModule;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void modifyTooltip(net.minecraft.world.item.Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        BetterAntiCrashModule bac = Modules.get().get(BetterAntiCrashModule.class);
        if(!bac.isActive() || !bac.items.get()) { cir.setReturnValue(cir.getReturnValue()); return; }
        String tooltip = cir.getReturnValue().stream().skip(1).map(Component::getString).collect(Collectors.joining());
        if(tooltip.contains("§c[Translation Blocked],§c[Translation Blocked]") || tooltip.contains("§c[Translation Blocked]§c[Translation Blocked]§c[Translation Blocked]") && bac.translationCrash.get()) cir.setReturnValue(Arrays.stream(new Component[]{cir.getReturnValue().get(0), Component.nullToEmpty("§c[Translation Blocked]")}).toList());
        else cir.setReturnValue(tooltip.length() > bac.ThresholdLength.get() ? Arrays.stream(new Component[]{cir.getReturnValue().get(0), Component.nullToEmpty(String.format("§c[Tooltip with length %s blocked]", bac.getMessage(tooltip)))}).toList() : cir.getReturnValue());
    }

    @Inject(method = "getHoverName()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    private void name(CallbackInfoReturnable<Component> cir) {
        BetterAntiCrashModule bac = Modules.get().get(BetterAntiCrashModule.class);
        if(!bac.isActive() || !bac.items.get()) { cir.setReturnValue(cir.getReturnValue()); return; }
        String name = cir.getReturnValue().getString();
        if(name.contains("§c[Translation Blocked],§c[Translation Blocked]") || name.contains("§c[Translation Blocked]§c[Translation Blocked]§c[Translation Blocked]") && bac.translationCrash.get()) cir.setReturnValue(Component.nullToEmpty("§c[Translation Blocked]"));
        else cir.setReturnValue(name.length() > bac.ThresholdLength.get() ? Component.nullToEmpty(String.format("§c[Name with length %s blocked]", bac.getMessage(name))) : cir.getReturnValue());
    }
}
