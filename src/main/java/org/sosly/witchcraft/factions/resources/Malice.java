package org.sosly.witchcraft.factions.resources;

import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.mna.api.capabilities.resource.SimpleCastingResource;
import com.mna.api.config.GeneralConfigValues;
import com.mna.items.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.CastingResourceIDs;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;

public class Malice extends SimpleCastingResource {
    public Malice() {
        super(GeneralConfigValues.TotalManaRegenTicks);
    }

    @Override
    public int getRegenerationRate(LivingEntity caster) {
        return (int)(this.ticks_for_regeneration * this.getRegenerationModifier(caster));
    }

    @Override
    public float getRegenerationModifier(LivingEntity caster) {
        ICovenCapability coven = caster.getCapability(CovenProvider.COVEN).orElse(new CovenCapability());
        if (coven.hasMalice()) {
            return super.getRegenerationModifier(caster) / 2;
        }
        if (super.getRegenerationModifier(caster) == 1.0F) {
            return 0;
        }
        
        return super.getRegenerationModifier(caster) + 1;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return CastingResourceIDs.MALICE;
    }

    @Override
    public void setMaxAmountByLevel(int level) {
        this.setMaxAmount((float) (50 + 10 * level));
    }

    public static class ResourceGui implements ICastingResourceGuiProvider {
        public ResourceLocation getTexture() {
            // TODO: this texture is not being loaded, and the default MnA texture is being used instead.
            return new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/resource_bars.png");
        }

        public int getXPBarColor() {
            return 0xFF802020;
        }

        public int getBarColor() {
            return 0xFF601010;
        }

        public int getBarManaCostEstimateColor() {
            return 0x80000000;
        }

        public int getResourceNumericTextColor() {
            return 0x80FFFFFF;
        }

        public int getBadgeSize() {
            return 64;
        }

        public int getFrameU() {
            return 0;
        }

        public int getFrameWidth() {
            return 153;
        }

        public int getFrameHeight() {
            return 24;
        }

        public int getFrameV() {
            return 0;
        }

        public ItemStack getBadgeItem() {
            return new ItemStack(ItemInit.FEY_WINTER_HUD_BADGE.get());
        }

        public int getBadgeItemOffsetY() {
            return 9;
        }

        public int getFillWidth() {
            return 128;
        }

        public int getLevelDisplayY() {
            return this.getFrameHeight() - 1;
        }
    }
}
