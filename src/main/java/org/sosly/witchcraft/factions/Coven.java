package org.sosly.witchcraft.factions;

import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import com.mna.gui.GuiTextures;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.api.CastingResourceIDs;
import org.sosly.witchcraft.items.ItemRegistry;

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
        return new ItemStack((ItemLike) ItemRegistry.GRIMOIRE_COVEN.get());
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
        return ChatFormatting.LIGHT_PURPLE;
    }
}
