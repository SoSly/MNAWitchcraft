package org.sosly.witchcraft.commands;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerProgression;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;

import java.util.Map;

public class ProgressCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("progress")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(ctx -> {
                            EntityArgument.getPlayers(ctx, "player").forEach(player -> {
                                showCovenProgress(ctx.getSource(), (ServerPlayer) player);
                            });
                            return 0;
                        })
                );
    }
    
    private static void showCovenProgress(CommandSourceStack source, ServerPlayer player) {
        ICovenCapability coven = player.getCapability(CovenProvider.COVEN).orElse(null);
        if (coven == null) {
            source.sendFailure(Component.literal("Failed to get coven capability for " + player.getName().getString()));
            return;
        }
        
        IPlayerProgression progression = player.getCapability(ManaAndArtificeMod.getProgressionCapability()).orElse(null);
        if (progression == null) {
            source.sendFailure(Component.literal("Failed to get progression capability for " + player.getName().getString()));
            return;
        }
        
        source.sendSuccess(() -> Component.literal("=== Coven Progress for " + player.getName().getString() + " ===")
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Current Tier: " + progression.getTier())
                .withStyle(ChatFormatting.YELLOW), false);
        
        for (int tier = 3; tier <= 5; tier++) {
            final int currentTier = tier;
            source.sendSuccess(() -> Component.literal("\nTier " + currentTier + " Requirements:")
                    .withStyle(ChatFormatting.AQUA), false);
            
            Map<ResourceLocation, Boolean> progress = coven.getTierEffectsProgress(tier);
            if (progress == null || progress.isEmpty()) {
                source.sendSuccess(() -> Component.literal("  Not generated")
                        .withStyle(ChatFormatting.GRAY), false);
                return;
            }

            int completed = 0;
            for (Map.Entry<ResourceLocation, Boolean> entry : progress.entrySet()) {
                ResourceLocation effectId = entry.getKey();
                boolean isCompleted = entry.getValue();
                if (isCompleted) completed++;
                
                ChatFormatting color = isCompleted ? ChatFormatting.GREEN : ChatFormatting.RED;
                String symbol = isCompleted ? "✓" : "✗";
                
                source.sendSuccess(() -> Component.literal("  - " + effectId + " " + symbol)
                        .withStyle(color), false);
            }
            
            final int finalCompleted = completed;
            final int total = progress.size();
            ChatFormatting progressColor = completed == total ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
            source.sendSuccess(() -> Component.literal("  Progress: " + finalCompleted + "/" + total)
                    .withStyle(progressColor), false);
        }
    }
}