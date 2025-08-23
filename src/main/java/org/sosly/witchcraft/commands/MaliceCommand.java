package org.sosly.witchcraft.commands;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.factions.FactionRegistry;

public class MaliceCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("malice")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(ctx -> {
                            EntityArgument.getPlayers(ctx, "player").forEach(player -> {
                                IPlayerProgression progress = player.getCapability(ManaAndArtificeMod.getProgressionCapability()).orElse(new PlayerProgression());
                                if (!FactionRegistry.isWitch(progress)) {
                                    return;
                                }

                                ICovenCapability coven = player.getCapability(CovenProvider.COVEN).orElse(new CovenCapability());
                                boolean malice = !coven.hasMalice();
                                coven.setMalice(malice);
                                if (!malice) {
                                    return;
                                }
                                
                                progress.setAlliedFaction(FactionRegistry.DARK_COVEN, player);
                            });
                            return 0;
                        })
                );
    }
}