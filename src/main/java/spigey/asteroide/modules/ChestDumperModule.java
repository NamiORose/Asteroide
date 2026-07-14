package spigey.asteroide.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import spigey.asteroide.AsteroideAddon;

import java.util.List;
import java.util.Objects;

public class ChestDumperModule extends Module {
    public ChestDumperModule() {
        super(AsteroideAddon.CATEGORY, "Chest-Dumper", "Dumps all items into inventories");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay before taking another item")
        .defaultValue(2)
        .min(0)
        .sliderMax(10)
        .max(100)
        .build()
    );
    private final Setting<List<String>> name = sgGeneral.add(new StringListSetting.Builder()
        .name("must-have-name")
        .description("Only dump items with these names")
        .defaultValue()
        .build()
    );
    private final Setting<List<String>> contain = sgGeneral.add(new StringListSetting.Builder()
        .name("must-contain-name")
        .description("Only dump items whose names contain these phrases")
        .defaultValue()
        .build()
    );
    public final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Which items to dump")
        .build()
    );
    private final Setting<StealMode> stealMode = sgGeneral.add(new EnumSetting.Builder<StealMode>()
        .name("dump-mode")
        .description("Whether to use whitelist or blacklist")
        .defaultValue(StealMode.All)
        .build()
    );
    private final Setting<Boolean> close = sgGeneral.add(new BoolSetting.Builder()
        .name("close")
        .description("Closes the screen when done")
        .defaultValue(true)
        .build()
    );
    private enum StealMode {
        Whitelist,
        Blacklist,
        All
    }

    private int tick;
    private int i = -1;
    NonNullList<Slot> slots;
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen) || mc.screen instanceof InventoryScreen) return;
        NonNullList<Slot> slots = screen.getMenu().slots;
        if (slots == null || slots.isEmpty()) return;
        if (tick > 0) { tick--; return; }

        for(Slot slot : slots){
            if(!(slot.container instanceof Inventory)) continue;
            if (shouldDump(slot.getItem())) {
                ServerboundContainerClickPacket packet = getPacket(slot.getItem(), slot);
                assert mc.player != null;
                mc.player.connection.send(packet);
                tick = delay.get();
                if(delay.get() > 0) return;
            }
        }
        if(screen.getMenu().slots.stream().noneMatch(slot -> slot.hasItem() && slot.container instanceof Inventory) && close.get()) {
            screen.onClose();
            mc.player.connection.send(new ServerboundContainerClosePacket(screen.getMenu().containerId));
        }
    }

    private boolean shouldDump(ItemStack item){
        if(item.isEmpty()) return false;
        if(stealMode.get() == StealMode.All) return true;
        for(int i = 0; i < name.get().size(); i++) if(item.getHoverName().getString().equalsIgnoreCase(name.get().get(i))) return stealMode.get() == StealMode.Whitelist;
        for(int i = 0; i < contain.get().size(); i++) if(item.getHoverName().getString().toLowerCase().contains(contain.get().get(i).toLowerCase())) return stealMode.get() == StealMode.Whitelist;
        for(int i = 0; i < items.get().size(); i++) if(item.getItem().getDefaultInstance().getHoverName().equals(items.get().get(i).getDefaultInstance().getHoverName())) return stealMode.get() == StealMode.Whitelist;
        return (name.get().isEmpty() && contain.get().isEmpty() && items.get().isEmpty()) || stealMode.get() != StealMode.Whitelist;
    }
    private ServerboundContainerClickPacket getPacket(ItemStack uwu, Slot slot) {
        final HashedStack hashedStack = HashedStack.create(uwu, Objects.requireNonNull(mc.player).connection.decoratedHashOpsGenenerator());
        return new ServerboundContainerClickPacket(((AbstractContainerScreen<?>) mc.screen).getMenu().containerId, 1, (short) slot.index, (byte) 0, ContainerInput.QUICK_MOVE, Int2ObjectMaps.singleton(slot.index, hashedStack), hashedStack);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Sent event){
        if(!(event.packet instanceof ServerboundContainerClosePacket)) return;
        tick = 0;
        i = -1;
    }
}

