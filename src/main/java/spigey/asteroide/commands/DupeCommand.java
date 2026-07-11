package spigey.asteroide.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class DupeCommand extends Command {
    public DupeCommand() {
        super("dupe", "Dupes the item in your hand & drops the results.");
    }

    private int tick = -1;
    private List<ItemStack> inventory;
    private boolean setDelay = true;

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            execute();
            this.setDelay = false;
            return SINGLE_SUCCESS;
        }).then(argument("delay", IntegerArgumentType.integer(0)).executes(context -> {
            execute(IntegerArgumentType.getInteger(context, "delay"));
            this.setDelay = false; // uhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
            return SINGLE_SUCCESS;
        }).then(argument("command", StringArgumentType.greedyString()).executes(context -> {
            execute(StringArgumentType.getString(context, "command"), IntegerArgumentType.getInteger(context, "delay"));
            this.setDelay = false;
            return SINGLE_SUCCESS;
        })));
    }


    private void execute(String command, int delay){
        MeteorClient.EVENT_BUS.subscribe(this);
        List<ItemStack> oldInventory = new ArrayList<>();
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) { oldInventory.add(mc.player.getInventory().getItem(i).copyWithCount(1)); }
        this.inventory = oldInventory;
        mc.getConnection().sendCommand(command.startsWith("/") ? command.substring(1) : command);
        this.tick = delay;
    }

    private void execute(String command){ execute(command, 30); }
    private void execute(int delay){ execute("dupe", delay); }
    private void execute(){ execute("dupe", 30); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(mc.player == null || this.tick == -1) { MeteorClient.EVENT_BUS.unsubscribe(this); return; }
        if(!this.setDelay){
            Inventory newInventory = mc.player.getInventory();
            for(int i = 0; i < newInventory.getContainerSize(); i++){
                ItemStack item = this.inventory.get(i);
                if(!ItemStack.matches(item, newInventory.getItem(i).copyWithCount(1))){ InvUtils.drop().slot(i); }
            }
            this.tick--;
            return;
        }
        if(this.tick > 0){ this.tick--; return; }
        Inventory newInventory = mc.player.getInventory();
        for(int i = 0; i < newInventory.getContainerSize(); i++){
            ItemStack item = this.inventory.get(i);
            if(!ItemStack.matches(item, newInventory.getItem(i))){ InvUtils.drop().slot(i); }
        }
        this.tick = -1;
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }
}
