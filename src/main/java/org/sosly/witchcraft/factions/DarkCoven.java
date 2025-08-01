package org.sosly.witchcraft.factions;

import com.mna.api.faction.BaseFaction;
import com.mna.gui.GuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.api.CastingResourceIDs;

public class DarkCoven extends BaseFaction {
    public DarkCoven() {
        super(CastingResourceIDs.MALICE);
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return null;
    }

    @Override
    public Item getTokenItem() {
        return null;
    }

    @Override
    public SoundEvent getRaidSound() {
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getHornSound() {
        return null;
    }

    @Override
    public Component getOcculusTaskPrompt(int i) {
        return Component.translatable("mnaw:factions/oculus_task_prompt");
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return GuiTextures.Widgets.FACTION_ICON_COUNCIL;
    }

    @Nullable
    @Override
    public int[] getManaweaveRGB() {
        return null;
    }

    @Override
    public ChatFormatting getTornJournalPageFactionColor() {
        return ChatFormatting.DARK_PURPLE;
    }
}
