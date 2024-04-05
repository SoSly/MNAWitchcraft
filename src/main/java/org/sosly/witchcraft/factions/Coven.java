package org.sosly.witchcraft.factions;

import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import com.mna.gui.GuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.api.CastingResourceIDs;

import java.util.List;

public class Coven extends BaseFaction {
    public Coven() {
        super(CastingResourceIDs.ESSENCE);
    }

    @Override
    public List<IFaction> getEnemyFactions() {
        return List.of();
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
        return null;
    }

    @Override
    public ResourceLocation getFactionIcon() {
//        return new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/coven_icon.png");
        return GuiTextures.Widgets.FACTION_ICON_COUNCIL;
    }

    @Nullable
    @Override
    public int[] getManaweaveRGB() {
        return new int[0];
    }

    @Override
    public ChatFormatting getTornJournalPageFactionColor() {
        return ChatFormatting.LIGHT_PURPLE;
    }
}
