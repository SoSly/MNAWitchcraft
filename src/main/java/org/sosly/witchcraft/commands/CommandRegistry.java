package org.sosly.witchcraft.commands;

import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("mnaw")
                .then(MaliceCommand.register())
                .then(ProgressCommand.register())
        );
    }
}
