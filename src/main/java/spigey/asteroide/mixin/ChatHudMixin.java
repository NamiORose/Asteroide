package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spigey.asteroide.modules.ClickEventsModule;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @ModifyVariable( method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), index = 1, argsOnly = true )
    private Component commandInspect(Component value) { try{
        ClickEventsModule ce = Modules.get().get(ClickEventsModule.class);
        if(!ce.isActive()) return value;

        MutableComponent copied = value.copy();
        if (value.getStyle().getClickEvent() != null) {
            HoverEvent old = value.getStyle().getHoverEvent();
            MutableComponent tooltip = Component.empty();
            if(old != null && !old.getValue(HoverEvent.Action.SHOW_TEXT).getString().contains(value.getStyle().getClickEvent().getValue())) if (old.getValue(HoverEvent.Action.SHOW_TEXT) instanceof MutableComponent t && ce.showCommand.get()) tooltip.append(t.copy()).append("\n\n");
            if (ce.showCommand.get()) tooltip.append("§7" + value.getStyle().getClickEvent().getValue());
            copied.setStyle(value.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
            if(ce.customColorEnabled.get()) copied.setStyle(copied.getStyle().withColor(ce.customColor.get().toTextColor()));
            if(ce.customStyleEnabled.get()) copied.setStyle(copied.getStyle().applyFormat(switch(ce.customStyle.get()){
                case Bold -> ChatFormatting.BOLD;
                case Italic -> ChatFormatting.ITALIC;
                case Underline -> ChatFormatting.UNDERLINE;
                case Strike -> ChatFormatting.STRIKETHROUGH;
                case Obfuscated -> ChatFormatting.OBFUSCATED;
                default -> ChatFormatting.RED; // idfk
            }));
        }
        for (int i = 0; i < value.getSiblings().size(); i++) copied.getSiblings().set(i, commandInspect(value.getSiblings().get(i)));
        return copied;
    }catch(Exception L){ return value; } }

    @Inject(method = "handleChatQueueClicked(DD)Z", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) { try{
        ClickEventsModule ce = Modules.get().get(ClickEventsModule.class);
        if(!ce.isActive() || !ce.blockCommands.get()) return;
        Style style = ((ChatComponent) (Object) this).getClickedComponentStyleAt(mouseX, mouseY);
        if (style == null) return;
        ClickEvent click = style.getClickEvent();
        if (click == null) return;
        if (click.getAction() != ClickEvent.Action.RUN_COMMAND) return;


        for(String command : ce.commands.get()) if(click.getValue().toLowerCase().contains(command.toLowerCase())){
            cir.setReturnValue(true);
            ce.info(Component.nullToEmpty(String.format("Command execution was blocked! §7%s", click.getValue().toLowerCase().replaceAll(command.toLowerCase(), "§c" + command.toUpperCase() + "§7"))));
            return;
        }
    }catch(Exception L){/* home botnet server */}}
}
