package spigey.asteroide.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import spigey.asteroide.AsteroideAddon;

import java.util.Arrays;
import java.util.List;

public class InvCleanerModule extends Module {
    public InvCleanerModule() {
        super(AsteroideAddon.CATEGORY, "Inv-Cleaner", "Automatically drops useless items in your inventory");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to drop.")
        .build()
    );
    private final Setting<List<String>> names = sgGeneral.add(new StringListSetting.Builder()
        .name("names")
        .description("Also drop items with these names")
        .defaultValue()
        .build()
    );

    @EventHandler
    public void onTick(TickEvent.Post event){
        if(mc.screen == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;
        NonNullList<Slot> slots = ((AbstractContainerScreen<?>) mc.screen).getMenu().slots;
        if (slots == null || slots.isEmpty()) return;
        int[] exclude = new int[]{5, 6, 7, 8};
        for(Slot slot : slots){
            if(!(slot.container instanceof Inventory) && !(slot.index == 0 && ((mc.screen instanceof InventoryScreen) || (mc.screen instanceof CraftingScreen)))) continue;
            if(Arrays.stream(exclude).anyMatch(slott -> slott == slot.index)) continue;
            ItemStack uwu = slot.getItem();
            if(!(items.get().contains(uwu.getItem()) || names.get().stream().anyMatch(name -> name.equalsIgnoreCase(uwu.getHoverName().getString())))) continue;
            InvUtils.drop().slotId(slot.index);
        }
    }
}
