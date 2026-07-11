package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spigey.asteroide.modules.VersionSpoofModule;

@Mixin(ClientIntentionPacket.class)
public class BedrockSpoofMixin {

    @Mutable
    @Shadow
    @Final
    private int protocolVersion;

    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At("RETURN"))
    private void spoofProtocolVersion(int i, String string, int j, ClientIntent connectionIntent, CallbackInfo ci) {
        try{
            if(!(Modules.get().get(VersionSpoofModule.class).isActive())) return;
            this.protocolVersion = VersionSpoofModule.readable(Modules.get().get(VersionSpoofModule.class).spoofedVersion.get());
        }catch(Exception L){ /**/ }
    }
}
