package spigey.asteroide.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import spigey.asteroide.AsteroideAddon;
import spigey.asteroide.util;

public class WhereIsCommand extends Command {
    public WhereIsCommand() {
        super("whereis", "Makes you look at a specified player");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();
            if((profile == null) || mc.level == null) {error("Player not found."); return SINGLE_SUCCESS;}
            for(Entity entity : mc.level.entitiesForRendering()){
                if(!(entity instanceof Player)) continue;
                if(util.withoutStyle(entity.getName()).equals(profile.name())){
                    ChatUtils.sendMsg(Component.nullToEmpty(String.format("§7Player found at §cX: %.0f§7, §aY: %.0f§7, §9Z: %.0f", entity.getX(), entity.getY(), entity.getZ())));
                    assert mc.player != null;
                    mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(entity.getX(), entity.getY() + 1.62, entity.getZ()));
                    AsteroideAddon.lastPos = new double[]{entity.getX(), entity.getY(), entity.getZ()};
                    return SINGLE_SUCCESS;
                }
            }
            ChatUtils.sendMsg(Component.nullToEmpty("§cPlayer not found, is it too far away?"));
            return SINGLE_SUCCESS;
        }));
    }
}
