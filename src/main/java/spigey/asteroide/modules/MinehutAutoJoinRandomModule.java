package spigey.asteroide.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import spigey.asteroide.AsteroideAddon;

import java.util.Objects;
import java.util.Set;

public class MinehutAutoJoinRandomModule extends Module {
    public MinehutAutoJoinRandomModule() {
        super(AsteroideAddon.CATEGORY, "Minehut-Auto-Join", "Automatically joins random minehut servers when in the lobby");
    }
    private final SettingGroup sgGeneral = settings.createGroup("General", true);
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay when trying to join")
        .defaultValue(5)
        .min(0)
        .sliderMax(30)
        .build()
    );
    private final Setting<Category> category = sgGeneral.add(new meteordevelopment.meteorclient.settings.EnumSetting.Builder<Category>()
        .name("Category")
        .description("The category to join")
        .defaultValue(Category.Random)
        .build()
    );
    private final SettingGroup sgPackets = settings.createGroup("Packets", true);
    private final Setting<Boolean> cancelPackets = sgPackets.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("Cancel Packets")
        .description("Cancels chunk packets while in the minehut lobby to reduce lag")
        .defaultValue(true)
        .build()
    );
    private final Setting<Set<PacketType<? extends @NotNull Packet<?>>>> s2cPackets = sgPackets.add(new PacketListSetting.Builder()
        .name("S2C Packets")
        .description("Packets to cancel")
        .filter(aClass -> PacketUtils.getClientboundPackets().contains(aClass))
        .visible(cancelPackets::get)
            .defaultValue(Set.of(
                GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT,
                GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_RADIUS,
                GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS,
                GamePacketTypes.CLIENTBOUND_ADD_ENTITY
            ))
        .build()
    );
    private final Setting<Set<PacketType<? extends @NotNull Packet<?>>>> c2sPackets = sgPackets.add(new PacketListSetting.Builder()
        .name("C2S Packets")
        .description("Packets to cancel")
        .filter(aClass -> PacketUtils.getServerboundPackets().contains(aClass))
        .visible(cancelPackets::get)
        .defaultValue(Set.of(
            GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS
        ))
        .build()
    );
    private int tick = 0;

    @Override
    public void onActivate() {
        tick = delay.get();
    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        if(tick > 0) {tick--; return;}
        if(mc.isLocalServer()) return;
        if(!(Objects.requireNonNull(mc.getCurrentServer()).ip).toLowerCase().contains("minehut.")) return;
        if(!Objects.equals(mc.player.getInventory().getSelectedItem().getHoverName().getString(), "Find a Server (Right-Click)")) return;
        if(!(mc.screen instanceof ContainerScreen)) Utils.rightClick();
        if(!(mc.screen instanceof ContainerScreen)) return;
        NonNullList<Slot> slots = ((ContainerScreen) mc.screen).getMenu().slots;
        final HashedStack hashedStack = HashedStack.create(slots.getFirst().getItem(), mc.player.connection.decoratedHashOpsGenenerator());
        ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(1, 69, (short) category.get().get(), (byte) 1, ContainerInput.PICKUP, Int2ObjectMaps.singleton(0, HashedStack.EMPTY), hashedStack);
        mc.getConnection().send(packet);
        tick = delay.get();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event){
        if(!isActive() || !cancelPackets.get() || mc.isLocalServer()) return;
        if(!(Objects.requireNonNull(mc.getCurrentServer()).ip).toLowerCase().contains("minehut.")) return;
        if(!Objects.equals(mc.player.getInventory().getSelectedItem().getHoverName().getString(), "Find a Server (Right-Click)")) return;
        if(s2cPackets.get().contains(event.packet.type())) event.cancel();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event){
        if(!isActive() || !cancelPackets.get() || mc.isLocalServer()) return;
        if(!(Objects.requireNonNull(mc.getCurrentServer()).ip).toLowerCase().contains("minehut.")) return;
        if(!Objects.equals(mc.player.getInventory().getSelectedItem().getHoverName().getString(), "Find a Server (Right-Click)")) return;
        if(c2sPackets.get().contains(event.packet.type())) event.cancel();
    }

    private enum Category {
        Ranked(20),
        Puzzle(21),
        Box(22),
        PVP(23),
        SMP(24),
        Gen(29),
        Farming(30),
        Prison(31),
        RPG(32),
        Minigame(33),
        Skyblock(38),
        Parkour(39),
        Meme(40),
        Lifesteal(41),
        Roleplay(42),
        Factions(47),
        Modded(48),
        Random(49),
        Creative(50),
        PVP_1_8(51);

        private final int slot;
        Category(int slot){ this.slot = slot; }
        public int get(){ return slot; }
    }
}


// SLOT     49 | 26
// REVISION 55 | 58
// SYNC ID  1  | 1?
