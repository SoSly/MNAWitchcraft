package org.sosly.witchcraft;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue BOSSES_BLOCK_SYMPATHY = BUILDER
            .comment("Should boss arenas prevent sympathetic magic from working?")
            .define("bossesBlockSympathy", true);

    private static final ForgeConfigSpec.BooleanValue BOSSES_IMMUNE_TO_SYMPATHY = BUILDER
            .comment("Should bosses be immune to sympathetic magic?")
            .define("bossesImmuneToSympathy", true);

    private static final ForgeConfigSpec.IntValue EFFECT_FOR_TIER = BUILDER
            .comment("How many spell effects should a player have to cast on a Witch mob to progress to the next tier?")
            .defineInRange("effectForTier", 3, 1, 10);

    private static final ForgeConfigSpec.IntValue DEDICATION_CHARGES = BUILDER
            .comment("How many charges should Dedication I give to a staff with 1 mana cost? (divide by mana cost to get charges for other spells)")
            .defineInRange("dedicationCharges", 5000, 10, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue DEDICATION_TIER_MULTIPLIER = BUILDER
            .comment("How much should the charge modifier increase per tier? (multiplier = 1 + (tier - 1) * this value)")
            .defineInRange("dedicationChargeModifier", 0.25, 0.01, 100);

    private static final ForgeConfigSpec.IntValue WITCH_GOSSIP_COOLDOWN = BUILDER
            .comment("How long (in seconds) should a witch wait between gossip attempts?")
            .defineInRange("witchGossipCooldown", 60, 10, 3600);

    private static final ForgeConfigSpec.IntValue WITCH_GOSSIP_DISTANCE = BUILDER
            .comment("How far (in blocks) can players hear witch gossip?")
            .defineInRange("witchGossipDistance", 32, 16, 128);

    private static final ForgeConfigSpec.IntValue WITCH_GOSSIP_SPELL_HINT_CHANCE = BUILDER
            .comment("Chance of spell hints (1 in N). Higher values make spell hints rarer.")
            .defineInRange("witchGossipSpellHintChance", 5, 1, 100);

    private static final ForgeConfigSpec.IntValue MOONTHREAD_ARMOR_EARTHEN_REGEN_TIME = BUILDER
            .comment("How many seconds does it take for Moonthread armor to regenerate from 0% to 100% essence while crouching on natural blocks?")
            .defineInRange("moonthreadArmorEarthenRegenTime", 20, 1, 300);

    private static final ForgeConfigSpec.IntValue MOONTHREAD_ARMOR_HARMFUL_NULLIFICATION_CHANCE = BUILDER
            .comment("Chance to nullify harmful potion effects (1 in N). Higher values make nullification rarer.")
            .defineInRange("moonthreadArmorHarmfulNullificationChance", 3, 1, 100);

    private static final ForgeConfigSpec.IntValue MOONTHREAD_ARMOR_HARMFUL_NULLIFICATION_COOLDOWN = BUILDER
            .comment("How long (in seconds) before the harmful effect nullification can trigger again?")
            .defineInRange("moonthreadArmorHarmfulNullificationCooldown", 10, 1, 300);

    private static final ForgeConfigSpec.IntValue MOONTHREAD_ARMOR_SPAWN_PREVENTION_RADIUS = BUILDER
            .comment("Radius in blocks around players wearing full Moonthread armor where hostile mobs won't spawn at night above ground")
            .defineInRange("moonthreadArmorSpawnPreventionRadius", 32, 16, 128);

    private static final ForgeConfigSpec.IntValue WITCHS_CAULDRON_MOONLIGHT_COLLECTION_CHANCE = BUILDER
            .comment("Chance to collect moonlight each tick (1 in N). Higher values make collection slower.")
            .defineInRange("witchsCauldronMoonlightCollectionChance", 1200, 20, 12000);

    private static final ForgeConfigSpec.DoubleValue FLYING_BROOM_SPEED = BUILDER
            .comment("Flying broom flight speed in blocks per second")
            .defineInRange("flyingBroomSpeed", 7.0, 1.0, 30.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean bossesBlockSympathy;
    public static boolean bossesImmuneToSympathy;
    public static int dedicationCharges;
    public static double dedicationTierMultiplier;
    public static int effectForTier;
    public static int witchGossipCooldown;
    public static int witchGossipDistance;
    public static int witchGossipSpellHintChance;
    public static int moonthreadArmorEarthenRegenTime;
    public static int moonthreadArmorHarmfulNullificationChance;
    public static int moonthreadArmorHarmfulNullificationCooldown;
    public static int moonthreadArmorSpawnPreventionRadius;
    public static int witchsCauldronMoonlightCollectionChance;
    public static double flyingBroomSpeed;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        bossesBlockSympathy = BOSSES_BLOCK_SYMPATHY.get();
        bossesImmuneToSympathy = BOSSES_IMMUNE_TO_SYMPATHY.get();
        dedicationCharges = DEDICATION_CHARGES.get();
        dedicationTierMultiplier = DEDICATION_TIER_MULTIPLIER.get();
        effectForTier = EFFECT_FOR_TIER.get();
        witchGossipCooldown = WITCH_GOSSIP_COOLDOWN.get();
        witchGossipDistance = WITCH_GOSSIP_DISTANCE.get();
        witchGossipSpellHintChance = WITCH_GOSSIP_SPELL_HINT_CHANCE.get();
        moonthreadArmorEarthenRegenTime = MOONTHREAD_ARMOR_EARTHEN_REGEN_TIME.get();
        moonthreadArmorHarmfulNullificationChance = MOONTHREAD_ARMOR_HARMFUL_NULLIFICATION_CHANCE.get();
        moonthreadArmorHarmfulNullificationCooldown = MOONTHREAD_ARMOR_HARMFUL_NULLIFICATION_COOLDOWN.get();
        moonthreadArmorSpawnPreventionRadius = MOONTHREAD_ARMOR_SPAWN_PREVENTION_RADIUS.get();
        witchsCauldronMoonlightCollectionChance = WITCHS_CAULDRON_MOONLIGHT_COLLECTION_CHANCE.get();
        flyingBroomSpeed = FLYING_BROOM_SPEED.get();
    }
}
