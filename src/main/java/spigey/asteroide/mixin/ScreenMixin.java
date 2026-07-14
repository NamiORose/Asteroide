package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spigey.asteroide.modules.ClickEventsModule;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "defaultHandleGameClickEvent(Lnet/minecraft/network/chat/ClickEvent;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private static void onMouseClicked(final ClickEvent event, final Minecraft minecraft, final @Nullable Screen activeScreen, CallbackInfo ci) {
        // callstack (probably):
        // net.minecraft.client.gui.screens.ChatScreen#mouseClicked
        // net.minecraft.client.gui.screens.ChatScreen#handleComponentClicked
        // net.minecraft.client.gui.screens.Screen#defaultHandleGameClickEvent (static function)
        // *I have a serious suspicion that this method is called not only when clicking on a chat message.*
        if (activeScreen instanceof ChatScreen) return;
        if (event instanceof ClickEvent.RunCommand(String cmd)) {
            ClickEventsModule ce = Modules.get().get(ClickEventsModule.class);
            assert ce != null;
            for(String command : ce.commands.get())
                if(cmd.contains(command.toLowerCase())) {
                    ci.cancel();
                    ce.info(Component.nullToEmpty(String.format("Command execution was blocked! §7%s", cmd.replaceAll(command.toLowerCase(), "§c" + command.toUpperCase() + "§7"))));
                    return;
                }
        }
    }
}
