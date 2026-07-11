package spigey.asteroide.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import spigey.asteroide.util;

public class MathCommand extends Command {
    public MathCommand() {
        super("math", "Solves math equations for you", "meth");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            ChatUtils.sendMsg(Component.nullToEmpty("§cYou have to specify an equation!"));
            return SINGLE_SUCCESS;
        });
        builder.then(argument("equation", StringArgumentType.greedyString()).executes(context -> {
            String farquaad = String.valueOf(StringArgumentType.getString(context, "equation"));
            try{farquaad = String.valueOf(util.meth(farquaad));} catch(Exception L){ChatUtils.sendMsg(Component.nullToEmpty("§c" + L)); return SINGLE_SUCCESS;}
            if(farquaad.endsWith(".0")) farquaad = farquaad.replace(".0", "");
            ChatUtils.sendMsg(Component.nullToEmpty("The solution is §e" + farquaad));
            ChatUtils.sendMsg(util.getSendButton(util.getCopyButton(farquaad), farquaad));
            return SINGLE_SUCCESS;
        }));
    }
}
