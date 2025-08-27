package org.sosly.witchcraft.items.armor;

import com.mojang.logging.LogUtils;
import com.mna.api.capabilities.IPlayerMagic;
import org.slf4j.Logger;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.ITieredItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.armor.IBrokenArmorReplaceable;
import com.mna.items.armor.ISetItem;
import com.mna.items.base.IManaRepairable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.config.ServerConfig;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.factions.FactionRegistry;
import org.sosly.witchcraft.renderers.items.MoonthreadArmorRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MoonthreadArmorItem extends ArmorItem implements GeoItem, ISetItem, ITieredItem<MoonthreadArmorItem>, IFactionSpecific, IBrokenArmorReplaceable<MoonthreadArmorItem>, IManaRepairable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int tier = -1;
    private static final ResourceLocation MOONTHREAD_SET_BONUS = new ResourceLocation(Witchcraft.MOD_ID, "moonthread_armor_set_bonus");
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private static final Random random = new Random();
    

    public MoonthreadArmorItem(ArmorMaterial material, Type slot, Properties builder) {
        super(material, slot, builder.rarity(Rarity.EPIC));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    @Override
    public void applySetBonus(LivingEntity entity, EquipmentSlot... setSlots) {
    }
    
    private static boolean isNaturalBlock(Block block) {
        return block == Blocks.GRASS_BLOCK || 
               block == Blocks.DIRT || 
               block == Blocks.COARSE_DIRT || 
               block == Blocks.PODZOL || 
               block == Blocks.MYCELIUM || 
               block == Blocks.MOSS_BLOCK;
    }

    @Override
    public void setCachedTier(int i) {
        tier = i;
    }

    @Override
    public int getCachedTier() {
        return tier;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return super.damageItem(stack, amount, entity, onBroken);
    }


    @Override
    public IFaction getFaction() {
        return FactionRegistry.COVEN;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                   EquipmentSlot equipmentSlot, HumanoidModel<?> defaultModel) {
                if (renderer == null) {
                    renderer = new MoonthreadArmorRenderer();
                }

                renderer.prepForRender(livingEntity, itemStack, equipmentSlot, defaultModel);
                return renderer;
            }
        });
    }

    @Override
    public int itemsForSetBonus() {
        return 4;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        
        if (this.getType() != Type.HELMET) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }
        
        if (!isWearingFullSet(player)) {
            return;
        }
        
        if (!player.isShiftKeyDown()) {
            return;
        }
        
        BlockPos pos = player.blockPosition().below();
        Block block = level.getBlockState(pos).getBlock();
        if (!isNaturalBlock(block)) {
            return;
        }
        
        IPlayerMagic magic = player.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
        if (magic == null) {
            LOGGER.warn("Could not access player magic capability for {} during moonthread armor tick", player.getName().getString());
            return;
        }
        
        if (magic.getCastingResource() == null) {
            LOGGER.warn("Player {} has null casting resource during moonthread armor tick", player.getName().getString());
            return;
        }
        
        float currentAmount = magic.getCastingResource().getAmount();
        float maxAmount = magic.getCastingResource().getMaxAmount();
        if (currentAmount >= maxAmount) {
            return;
        }
        
        if (level.getGameTime() % 20 == 0) {
            float percentPerSecond = 1.0f / ServerConfig.moonthreadArmorEarthenRegenTime;
            float regenAmount = maxAmount * percentPerSecond;
            
            if (regenAmount <= 0) {
                LOGGER.warn("Invalid regen amount {} calculated for player {}", regenAmount, player.getName().getString());
                return;
            }
            
            magic.getCastingResource().restore(regenAmount);
        }
        
        if (level.getGameTime() % 40 == 0) {
            damageRandomArmorPiece(player, 1);
        }
        
        if (level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel) {
            spawnEarthenRegenParticles(serverLevel, player, pos);
        }
    }
    
    private void spawnEarthenRegenParticles(ServerLevel level, Player player, BlockPos groundPos) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        
        for (int i = 0; i < 3; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;
            double offsetY = 0.5 + player.getRandom().nextDouble() * 1.5;
            
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                x + offsetX, y + offsetY, z + offsetZ, 
                1, 0, 0, 0, 0);
        }
        
        for (int i = 0; i < 8; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 1.2;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 1.2;
            
            level.sendParticles(ParticleTypes.MYCELIUM, 
                x + offsetX, groundPos.getY() + 1.1, z + offsetZ, 
                1, 0, 0.02, 0, 0);
        }
    }
    
    public static boolean isWearingFullSet(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof MoonthreadArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public ResourceLocation getSetIdentifier() {
        return MOONTHREAD_SET_BONUS;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("item.mnaw.moonthread_armor.set_bonus").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.mnaw.moonthread_armor.set_bonus.earthen_regeneration").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.mnaw.moonthread_armor.set_bonus.potion_doubling").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.mnaw.moonthread_armor.set_bonus.harmful_nullification").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.mnaw.moonthread_armor.set_bonus.spawn_prevention").withStyle(ChatFormatting.GRAY));
    }
    
    public static void damageRandomArmorPiece(Player player, int damage) {
        List<ItemStack> moonthreadPieces = new java.util.ArrayList<>();
        
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof MoonthreadArmorItem && !stack.isEmpty()) {
                moonthreadPieces.add(stack);
            }
        }
        
        if (!moonthreadPieces.isEmpty()) {
            ItemStack armorToDamage = moonthreadPieces.get(random.nextInt(moonthreadPieces.size()));
            armorToDamage.hurtAndBreak(damage, player, (p) -> {
            });
        }
    }
}
