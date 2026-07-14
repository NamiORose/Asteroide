package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import org.jetbrains.annotations.NotNull;
import spigey.asteroide.AsteroideAddon;

import java.util.Set;

public class DevModule extends Module {
    public DevModule() {
        super(AsteroideAddon.CATEGORY, "Dev", "Dev tools you shouldn't be able to see.");
    }

    private final SettingGroup sgPackets = settings.createGroup("Packets"); // Artics*
    private final SettingGroup sgGeneral = settings.createGroup("General");

    private final Setting<Boolean> slots = sgGeneral.add(new BoolSetting.Builder()
        .name("Test Slot Packets")
        .description("Tests Slot Packets")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> testEvents = sgGeneral.add(new BoolSetting.Builder()
        .name("Test Events")
        .description("Tests SendMessageEvent")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> testMsgs = sgGeneral.add(new BoolSetting.Builder()
        .name("Test AutoChatGame")
        .description("Tests AutoChatGame")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> testBrand = sgGeneral.add(new BoolSetting.Builder()
        .name("Brand Test")
        .description("Tests Client Brand")
        .defaultValue(false)
        .build()
    );

    private final Setting<Set<PacketType<? extends @NotNull Packet<?>>>> packets = sgPackets.add(new PacketListSetting.Builder()
        .name("Packets")
        .description("Artics to log")
        .filter(aClass -> PacketUtils.getServerboundPackets().contains(aClass))
        .build()
    );
    private final Setting<Boolean> packetDetails = sgPackets.add(new BoolSetting.Builder()
        .name("Details")
        .description("Include the full packet body")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> packetDelay = sgPackets.add(new BoolSetting.Builder()
        .name("Delays")
        .description("Log delays in ticks")
        .defaultValue(false)
        .build()
    );

    private int lastPacket = 0;

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if(packets.get().contains(event.packet.getClass())){
            if(packetDetails.get()) info(event.packet.toString());
            else info(Component.translationArg(event.packet.type().id())); // todo: validate
            if(packetDelay.get()) info(String.format("Last packet %d ticks ago", lastPacket));
            this.lastPacket = 0;
        }
        if(event.packet instanceof ServerboundCustomPayloadPacket && testBrand.get()){
            info("§cClient Brand: §a" + ((ServerboundCustomPayloadPacket) event.packet).payload().type().id());
            System.out.print(((ServerboundCustomPayloadPacket) event.packet).payload().type().id());
        }
        if (!(event.packet instanceof ServerboundContainerClickPacket) || !slots.get()) return;
        ChatUtils.sendMsg(Component.nullToEmpty("§cSLOT " + ((ServerboundContainerClickPacket) event.packet).slotNum()));
        ChatUtils.sendMsg(Component.nullToEmpty("§aREVISION " + ((ServerboundContainerClickPacket) event.packet).stateId()));
        ChatUtils.sendMsg(Component.nullToEmpty("§9SYNC ID " + ((ServerboundContainerClickPacket) event.packet).containerId()));
        ChatUtils.sendMsg(Component.nullToEmpty("§7ACTION " + ((ServerboundContainerClickPacket) event.packet).containerInput().name()));
        event.cancel();
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) throws Exception {
        if(!testEvents.get()) return;
        event.message = event.message.replaceAll("a", "а")
            .replaceAll("c", "с")
            .replaceAll("e", "е")
            .replaceAll("h", "һ")
            .replaceAll("i", "і")
            .replaceAll("j", "ј")
            .replaceAll("n", "ո")
            .replaceAll("o", "о")
            .replaceAll("p", "р")
            .replaceAll("u", "ս")
            .replaceAll("v", "ν")
            .replaceAll("x", "х")
            .replaceAll("y", "у") + " h";
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event){
        if(!testEvents.get()) return;
        AsteroideAddon.LOG.info(event.toString());
        info(event.toString());
    }

    boolean Received = false;
    int ticks = 0;
    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event){
        if(!testMsgs.get() || !isActive()) return;
        if(event.getMessage().getString().contains("[Dev]")) return;
        info(event.getMessage().toString());
        this.Received = !this.Received;
        if(this.Received) this.ticks = 0;
        else info(String.format("Auto Chatgame took %d ticks (%.2f seconds)", this.ticks, this.ticks / 20f));
    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        this.lastPacket++;
        if(!testMsgs.get() || !isActive() || !this.Received) return;
        this.ticks++;
    }
}
