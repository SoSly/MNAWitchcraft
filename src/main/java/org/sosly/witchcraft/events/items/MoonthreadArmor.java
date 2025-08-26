package org.sosly.witchcraft.events.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.config.ServerConfig;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.IMoonthreadArmorData;
import org.sosly.witchcraft.capabilities.armor.MoonthreadArmorProvider;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.items.alchemy.PotionPouchItem;
import org.sosly.witchcraft.items.armor.MoonthreadArmorItem;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID)
public class MoonthreadArmor {
    private static final Random random = new Random();
    private static final long TEMPORARY_IMMUNITY_DURATION = 200L;
    
    @SubscribeEvent
    public static void onPotionDrinkEvent(LivingEntityUseItemEvent.Finish event) {
        ItemStack consumedItem = event.getItem();
        boolean isPotion = consumedItem.getItem() == Items.POTION;
        boolean isPotionPouch = consumedItem.getItem() == ItemRegistry.POTION_POUCH.get();
        
        if (!isPotion && !isPotionPouch) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!MoonthreadArmorItem.isWearingFullSet(player)) {
            return;
        }
        
        ItemStack potionStack = consumedItem;
        if (isPotionPouch && consumedItem.getItem() instanceof PotionPouchItem potionPouchItem) {
            potionStack = potionPouchItem.getPotionStack(consumedItem);
        }
        
        List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(potionStack);
        
        for (MobEffectInstance potionEffect : potionEffects) {
            doubleEffectDurationIfApplicable(player, potionEffect);
        }
    }

    private static void doubleEffectDurationIfApplicable(Player player, MobEffectInstance potionEffect) {
        MobEffectCategory category = potionEffect.getEffect().getCategory();
        if (category != MobEffectCategory.BENEFICIAL && category != MobEffectCategory.NEUTRAL) {
            return;
        }
        
        MobEffectInstance activeEffect = player.getEffect(potionEffect.getEffect());
        if (activeEffect == null) {
            return;
        }
        
        player.removeEffect(potionEffect.getEffect());
        
        int doubledDuration = activeEffect.getDuration() * 2;
        MobEffectInstance extendedEffect = new MobEffectInstance(
            activeEffect.getEffect(),
            doubledDuration,
            activeEffect.getAmplifier(),
            activeEffect.isAmbient(),
            activeEffect.isVisible(),
            activeEffect.showIcon()
        );
        
        player.addEffect(extendedEffect);
        
        MoonthreadArmorItem.damageRandomArmorPiece(player, 1);
    }
    
    private static boolean shouldBlockHarmfulEffect(Player player, MobEffectInstance effectInstance) {
        if (!MoonthreadArmorItem.isWearingFullSet(player)) {
            return false;
        }
        
        if (effectInstance == null || effectInstance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return false;
        }
        
        IMoonthreadArmorData armorData = player.getCapability(MoonthreadArmorProvider.MOONTHREAD_ARMOR_DATA).orElse(null);
        if (armorData == null) {
            return false;
        }
        
        if (armorData.hasTemporaryImmunity()) {
            return true;
        }
        
        if (armorData.isNullificationCooldownActive()) {
            return false;
        }
        
        if (random.nextInt(ServerConfig.moonthreadArmorHarmfulNullificationChance) != 0) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownDuration = ServerConfig.moonthreadArmorHarmfulNullificationCooldown * 1000L;
        armorData.setHarmfulEffectNullificationCooldown(currentTime + cooldownDuration);
        armorData.setTemporaryImmunityTimestamp(currentTime + TEMPORARY_IMMUNITY_DURATION);
        
        MoonthreadArmorItem.damageRandomArmorPiece(player, 2);
        
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnNullificationEffects(serverLevel, player);
        }
        
        return true;
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (shouldBlockHarmfulEffect(player, event.getEffectInstance())) {
            event.setResult(MobEffectEvent.Result.DENY);
        }
    }
    
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();
        if (shouldBlockHarmfulEffect(player, effectInstance)) {
            player.removeEffect(effectInstance.getEffect());
            Witchcraft.LOGGER.debug("Moonthread Armor removed harmful effect {} from player {}", 
                effectInstance.getEffect().getDescriptionId(), player.getName().getString());
        }
    }
    
    @SubscribeEvent
    public static void onMobSpawnCheck(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL) {
            return;
        }
        
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        
        Level level = (Level) event.getLevel();
        
        if (!level.dimensionType().hasSkyLight() || level.isDay()) {
            return;
        }
        
        if (!level.canSeeSky(event.getEntity().blockPosition())) {
            return;
        }
        
        double spawnX = event.getX();
        double spawnY = event.getY();
        double spawnZ = event.getZ();
        
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, 
            event.getEntity().getBoundingBox().inflate(ServerConfig.moonthreadArmorSpawnPreventionRadius));
        
        for (Player player : nearbyPlayers) {
            if (!MoonthreadArmorItem.isWearingFullSet(player)) {
                continue;
            }
            
            double distance = player.distanceToSqr(spawnX, spawnY, spawnZ);
            double radiusSqr = ServerConfig.moonthreadArmorSpawnPreventionRadius * ServerConfig.moonthreadArmorSpawnPreventionRadius;
            if (distance > radiusSqr) {
                continue;
            }
            
            event.setSpawnCancelled(true);
            Witchcraft.LOGGER.info("Moonthread Armor prevented {} from spawning at ({}, {}, {}) near player {}", 
                event.getEntity().getType().getDescription(),
                spawnX, spawnY, spawnZ,
                player.getName().getString());
            return;
        }
    }
    
    private static void spawnNullificationEffects(ServerLevel level, Player player) {
        double x = player.getX();
        double y = player.getY() + player.getBbHeight() / 2.0;
        double z = player.getZ();
        
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * 2 * Math.PI;
            double radius = 1.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = player.getRandom().nextDouble() * 0.5 - 0.25;
            
            level.sendParticles(ParticleTypes.WITCH,
                x + offsetX, y + offsetY, z + offsetZ,
                1, 0, 0, 0, 0);
        }
        
        for (int i = 0; i < 10; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = player.getRandom().nextDouble() * 1.5;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
            
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                x + offsetX, y + offsetY, z + offsetZ,
                1, 0, 0, 0, 0);
        }
        
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.5f);
    }
}
