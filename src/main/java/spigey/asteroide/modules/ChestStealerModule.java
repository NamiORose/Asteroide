package spigey.asteroide.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.NonNullList;
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

public class ChestStealerModule extends Module {
    public ChestStealerModule() {
        super(AsteroideAddon.CATEGORY, "Chest-Stealer", "Takes all items from Inventories");
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
        .description("Only take items with these names")
        .defaultValue()
        .build()
    );
    private final Setting<List<String>> contain = sgGeneral.add(new StringListSetting.Builder()
        .name("must-contain-name")
        .description("Only take items whose names contain these phrases")
        .defaultValue()
        .build()
    );
    public final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Which items to steal")
        .build()
    );
    private final Setting<StealMode> stealMode = sgGeneral.add(new EnumSetting.Builder<StealMode>()
        .name("steal-mode")
        .description("Whether to use whitelist or blacklist")
        .defaultValue(StealMode.All)
        .build()
    );
    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("New works in all GUIs whereas classic is better for automation")
        .defaultValue(Mode.Classic)
        .build()
    );
    private final Setting<Boolean> close = sgGeneral.add(new BoolSetting.Builder()
        .name("close")
        .description("Closes the screen when done (new only)")
        .defaultValue(true)
        .build()
    );

    private enum Mode {
        New,
        Classic
    }

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
        if(mode.get() == Mode.Classic) { // I am not rewriting this, go fuck yourself
            if ((mc.screen instanceof ContainerScreen) || (mc.screen instanceof ShulkerBoxScreen) || (mc.screen instanceof HopperScreen)) {
                if (mc.screen instanceof ShulkerBoxScreen) {
                    slots = ((ShulkerBoxScreen) mc.screen).getMenu().slots;
                } else if (mc.screen instanceof HopperScreen) {
                    slots = ((HopperScreen) mc.screen).getMenu().slots;
                } else {
                    try {
                        slots = ((ContainerScreen) mc.screen).getMenu().slots;
                    } catch (Exception ignored) {
                    }
                }

                if (tick > 0) {
                    tick--;
                    return;
                }



                while ((i + 1) < (slots.size() - 36)) {
                    i++;
                    ItemStack uwu = slots.get(i).getItem();
                    if (shouldSteal(uwu)) {
                        ServerboundContainerClickPacket packet = getPacket(uwu);
                        assert mc.player != null;
                        mc.player.connection.send(packet);
                        tick = delay.get();
                        if (delay.get() != 0) return;
                    }
                }

                if (!((i + 1) < (slots.size() - 36))) {
                    i = -1;
                }
            }
        } else {
            if (!(mc.screen instanceof AbstractContainerScreen<?> screen) || mc.screen instanceof InventoryScreen) return;
            NonNullList<Slot> slots = screen.getMenu().slots;
            if (slots == null || slots.isEmpty()) return;

            if (tick > 0) { tick--; return; }

            for(Slot slot : slots){
                if(slot.container instanceof Inventory) continue;
                if (shouldSteal(slot.getItem())) {
                    ServerboundContainerClickPacket packet = getPacket(slot.getItem(), slot);
                    assert mc.player != null;
                    mc.player.connection.send(packet);
                    tick = delay.get();
                    if(delay.get() > 0) return;
                }
            }
            if(screen.getMenu().slots.stream().noneMatch(slot -> slot.hasItem() && !(slot.container instanceof Inventory)) && close.get()) {
                screen.onClose();
                mc.player.connection.send(new ServerboundContainerClosePacket(screen.getMenu().containerId));
            }
        }
    }

    private boolean shouldSteal(ItemStack item){
        if(item.isEmpty()) return false;
        if(stealMode.get() == StealMode.All) return true;
        for(int i = 0; i < name.get().size(); i++) if(item.getHoverName().getString().equalsIgnoreCase(name.get().get(i))) return stealMode.get() == StealMode.Whitelist;
        for(int i = 0; i < contain.get().size(); i++) if(item.getHoverName().getString().toLowerCase().contains(contain.get().get(i).toLowerCase())) return stealMode.get() == StealMode.Whitelist;
        for(int i = 0; i < items.get().size(); i++) if(item.getItem().getDefaultInstance().getHoverName().equals(items.get().get(i).getDefaultInstance().getHoverName())) return stealMode.get() == StealMode.Whitelist;
        return (name.get().isEmpty() && contain.get().isEmpty() && items.get().isEmpty()) || stealMode.get() != StealMode.Whitelist;
    }

    private ServerboundContainerClickPacket getPacket(ItemStack uwu) { // intellij wtf
        ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(0, 0, (short) 0, (byte) 0, ContainerInput.PICKUP, Int2ObjectMaps.singleton(0, HashedStack.EMPTY), HashedStack.EMPTY);

        final HashedStack hashedStack = HashedStack.create(uwu, Objects.requireNonNull(mc.player).connection.decoratedHashOpsGenenerator());
        // todo: validate .singleton(..., HashedStack.EMPTY) ????
        if(mc.screen instanceof ContainerScreen) packet = new ServerboundContainerClickPacket(((ContainerScreen) mc.screen).getMenu().containerId, 1, (short) i, (byte) 0, ContainerInput.QUICK_MOVE, Int2ObjectMaps.singleton(i, HashedStack.EMPTY), hashedStack);
        if(mc.screen instanceof ShulkerBoxScreen) packet = new ServerboundContainerClickPacket(((ShulkerBoxScreen) mc.screen).getMenu().containerId, 1, (short) i, (byte) 0, ContainerInput.QUICK_MOVE, Int2ObjectMaps.singleton(i, HashedStack.EMPTY), hashedStack);
        return packet;
    }

    private ServerboundContainerClickPacket getPacket(ItemStack uwu, Slot slot) {
        final HashedStack hashedStack = HashedStack.create(uwu, Objects.requireNonNull(mc.player).connection.decoratedHashOpsGenenerator());
        return new ServerboundContainerClickPacket(((AbstractContainerScreen<?>) mc.screen).getMenu().containerId, 1, (short) slot.index, (byte) 0, ContainerInput.QUICK_MOVE, Int2ObjectMaps.singleton(slot.index, HashedStack.EMPTY), hashedStack);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Sent event){
        if(!(event.packet instanceof ServerboundContainerClosePacket)) return;
        tick = 0;
        i = -1;
    }
}

