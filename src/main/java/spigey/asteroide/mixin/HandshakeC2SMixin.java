package spigey.asteroide.mixin;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.*;
import spigey.asteroide.modules.BetterBungeeSpoofModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spigey.asteroide.util;
import static spigey.asteroide.AsteroideAddon.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static spigey.asteroide.AsteroideAddon.spoofedIP;

@Mixin(ClientIntentionPacket.class)
public abstract class HandshakeC2SMixin {
    @Mutable
    @Shadow
    @Final
    private String hostName;

    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At("RETURN"))
    private void onHandshakeC2SPacket(int i, String string, int j, ClientIntent connectionIntent, CallbackInfo ci) {
        try {
            BetterBungeeSpoofModule bungeeSpoofModule = Modules.get().get(BetterBungeeSpoofModule.class);
            assert bungeeSpoofModule != null;
            if (!bungeeSpoofModule.isActive()) return;

            String spoofedUUID = mc.getUser().getProfileId().toString();
            spoofedIP = bungeeSpoofModule.spoofedAddress.get();
            if (bungeeSpoofModule.randomize.get()) spoofedIP = util.randomNum(0, bungeeSpoofModule.range.get()) + "." + util.randomNum(0, bungeeSpoofModule.range.get()) + "." + util.randomNum(0, bungeeSpoofModule.range.get()) + "." + util.randomNum(0, bungeeSpoofModule.range.get());

            if(spoofedUUID == null) {
                String response = Http.get(String.format("https://api.mojang.com/users/profiles/minecraft/%s", mc.getUser().getName())).sendString();
                if (response != null) {
                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                    if (jsonObject != null && jsonObject.has("id")) spoofedUUID = jsonObject.get("id").getAsString();
                }
            }
            this.hostName += "\u0000" + spoofedIP + "\u0000" + spoofedUUID;
        }catch(Exception L){ /**/ }
    }
}
