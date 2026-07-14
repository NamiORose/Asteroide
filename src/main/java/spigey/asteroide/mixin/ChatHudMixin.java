package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spigey.asteroide.modules.ClickEventsModule;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @ModifyVariable( method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), index = 1, argsOnly = true )
    private Component commandInspect(Component value) { try{
        ClickEventsModule ce = Modules.get().get(ClickEventsModule.class);
        if(!ce.isActive()) return value;

        MutableComponent copied = value.copy();
        if (value.getStyle().getClickEvent() != null) {
            HoverEvent old = value.getStyle().getHoverEvent();
            MutableComponent tooltip = Component.empty();

            final ClickEvent clickEvent = value.getStyle().getClickEvent();
            final String clickEventTarget = switch (clickEvent.action()) {
                case SUGGEST_COMMAND -> ((ClickEvent.SuggestCommand)clickEvent).command();
                case RUN_COMMAND -> ((ClickEvent.RunCommand)clickEvent).command();
                case OPEN_URL -> ((ClickEvent.OpenUrl)clickEvent).uri().toString();
                default -> null;
            };

            if (clickEventTarget != null && old != null && old.action() == HoverEvent.Action.SHOW_TEXT) {
                final Component showTextComponent = ((HoverEvent.ShowText) old).value();
                if (showTextComponent instanceof MutableComponent t && !t.getString().contains(clickEventTarget) && ce.showCommand.get())
                    tooltip.append(t.copy()).append("\n\n");
            }
            // todo: validate (logic?)
            // wtf is this ...
//            if(old != null && !old.getValue(HoverEvent.Action.SHOW_TEXT).getString().contains(value.getStyle().getClickEvent().getValue()))
//                if (old.getValue(HoverEvent.Action.SHOW_TEXT) instanceof MutableComponent t && ce.showCommand.get()) tooltip.append(t.copy()).append("\n\n");

            if (ce.showCommand.get()) tooltip.append("§7" + clickEventTarget);
            copied.setStyle(value.getStyle().withHoverEvent(new HoverEvent.ShowText(tooltip)));
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
}
