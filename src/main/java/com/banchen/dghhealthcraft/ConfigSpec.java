package com.banchen.dghhealthcraft;

import net.minecraftforge.common.ForgeConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class ConfigSpec {
    
    public static class Common {
        public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionRainChance;
        public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionLeftClickChance;
        public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionRightClickChance;
        public final ForgeConfigSpec.IntValue upperRespiratoryInfectionMildToModerateDays;
        public final ForgeConfigSpec.IntValue upperRespiratoryInfectionModerateToSevereDays;
        public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionSleepHealChance;
        public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionNutrientConsumption;
        public final ForgeConfigSpec.BooleanValue upperRespiratoryInfectionSevereCannotHeal;
        
        public final ForgeConfigSpec.DoubleValue lowProteinDiseaseBoost;
        public final ForgeConfigSpec.DoubleValue aidsDiseaseBoost;
        
        public final ForgeConfigSpec.DoubleValue sepsisContaminatedSyringeChance;
        public final ForgeConfigSpec.DoubleValue sepsisActionExtraSugarCost;
        public final ForgeConfigSpec.DoubleValue sepsisActionExtraFatCost;
        public final ForgeConfigSpec.DoubleValue sepsisPoisonChance;
        
        public final ForgeConfigSpec.DoubleValue aidsContaminatedSyringeChance;
        
        public final ForgeConfigSpec.DoubleValue corpsePoisonUndeadAttackChance;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> corpsePoisonCustomEntities;
        
        public final ForgeConfigSpec.IntValue corpseTransformationDays;
        public final ForgeConfigSpec.DoubleValue corpseTransformationDailyDeterioration;
        public final ForgeConfigSpec.BooleanValue deathOnAllPartsLost;
        
        public final ForgeConfigSpec.DoubleValue ptsdTriggerChance;
        public final ForgeConfigSpec.DoubleValue ptsdLowHpThreshold;
        public final ForgeConfigSpec.IntValue ptsdWeaknessLevel;
        public final ForgeConfigSpec.IntValue ptsdSlownessLevel;
        public final ForgeConfigSpec.IntValue ptsdMiningFatigueLevel;
        public final ForgeConfigSpec.IntValue ptsdEffectDurationTicks;
        public final ForgeConfigSpec.DoubleValue ptsdSleepReliefFactor;
        
        public final ForgeConfigSpec.IntValue capsuleMinIntervalSeconds;
        public final ForgeConfigSpec.IntValue capsuleEffectDelaySeconds;
        public final ForgeConfigSpec.DoubleValue lamivudineAidsCureChance;
        public final ForgeConfigSpec.DoubleValue dextromethorphanMildCureChance;
        public final ForgeConfigSpec.DoubleValue dextromethorphanModerateToMildChance;
        public final ForgeConfigSpec.BooleanValue ibuprofenTemporaryRelief;
        public final ForgeConfigSpec.IntValue ibuprofenReliefDurationSeconds;
        public final ForgeConfigSpec.DoubleValue targetedAgentDeteriorationReduction;
        public final ForgeConfigSpec.DoubleValue sedativePtsdReliefFactor;
        public final ForgeConfigSpec.BooleanValue ribavirinImmediateEffect;
        public final ForgeConfigSpec.DoubleValue broadSpectrumAntibioticsSepsisCureChance;
        
        public final ForgeConfigSpec.BooleanValue syringeContaminationEnabled;
        public final ForgeConfigSpec.IntValue syringeContaminationDurationSeconds;
        
        public final ForgeConfigSpec.IntValue blockingAgentCorpsePoisonImmunityDurationSeconds;
        
        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("DGH-HealthCraft Configuration")
                   .push("general");
            
            builder.comment("Upper Respiratory Tract Infection (URTI) Settings")
                   .push("urti");
            upperRespiratoryInfectionRainChance = builder
                    .comment("Chance of infection when raining (multiplier)")
                    .defineInRange("rainChance", 0.3, 0.0, 1.0);
            upperRespiratoryInfectionLeftClickChance = builder
                    .comment("Base chance of infection on left click")
                    .defineInRange("leftClickChance", 0.1, 0.0, 1.0);
            upperRespiratoryInfectionRightClickChance = builder
                    .comment("Base chance of infection on right click")
                    .defineInRange("rightClickChance", 0.1, 0.0, 1.0);
            upperRespiratoryInfectionMildToModerateDays = builder
                    .comment("Days to progress from mild to moderate")
                    .defineInRange("mildToModerateDays", 3, 1, 30);
            upperRespiratoryInfectionModerateToSevereDays = builder
                    .comment("Days to progress from moderate to severe")
                    .defineInRange("moderateToSevereDays", 5, 1, 30);
            upperRespiratoryInfectionSleepHealChance = builder
                    .comment("Chance to heal while sleeping")
                    .defineInRange("sleepHealChance", 0.4, 0.0, 1.0);
            upperRespiratoryInfectionNutrientConsumption = builder
                    .comment("Nutrients consumed when healing")
                    .defineInRange("nutrientConsumption", 5.0, 0.0, 100.0);
            upperRespiratoryInfectionSevereCannotHeal = builder
                    .comment("Whether severe infection cannot heal naturally")
                    .define("severeCannotHeal", true);
            builder.pop();
            
            builder.comment("Disease Boost Settings")
                   .push("disease_boosts");
            lowProteinDiseaseBoost = builder
                    .comment("Disease chance multiplier when protein is low")
                    .defineInRange("lowProteinBoost", 1.5, 1.0, 10.0);
            aidsDiseaseBoost = builder
                    .comment("Disease chance multiplier when HIV is active")
                    .defineInRange("aidsBoost", 2.0, 1.0, 10.0);
            builder.pop();
            
            builder.comment("Sepsis Settings")
                   .push("sepsis");
            sepsisContaminatedSyringeChance = builder
                    .comment("Chance to get sepsis from contaminated syringe")
                    .defineInRange("contaminatedSyringeChance", 0.25, 0.0, 1.0);
            sepsisActionExtraSugarCost = builder
                    .comment("Extra sugar consumption multiplier during actions")
                    .defineInRange("actionExtraSugarCost", 1.5, 1.0, 5.0);
            sepsisActionExtraFatCost = builder
                    .comment("Extra fat consumption multiplier during actions")
                    .defineInRange("actionExtraFatCost", 1.5, 1.0, 5.0);
            sepsisPoisonChance = builder
                    .comment("Chance to get poison effect")
                    .defineInRange("poisonChance", 0.3, 0.0, 1.0);
            builder.pop();
            
            builder.comment("HIV/AIDS Settings")
                   .push("hiv");
            aidsContaminatedSyringeChance = builder
                    .comment("Chance to get HIV from contaminated syringe")
                    .defineInRange("contaminatedSyringeChance", 0.15, 0.0, 1.0);
            builder.pop();
            
            builder.comment("Zombie Virus Settings")
                   .push("zombie_virus");
            corpsePoisonUndeadAttackChance = builder
                    .comment("Chance to get infected when attacked by undead")
                    .defineInRange("undeadAttackChance", 0.4, 0.0, 1.0);
            corpsePoisonCustomEntities = builder
                    .comment("Custom entity types that can spread the virus (use registry names)")
                    .defineList("customEntities", Arrays.asList(
                            "minecraft:zombie",
                            "minecraft:husk",
                            "minecraft:drowned",
                            "minecraft:zombie_villager",
                            "minecraft:zombified_piglin",
                            "minecraft:skeleton",
                            "minecraft:stray",
                            "minecraft:wither_skeleton"
                    ), obj -> obj instanceof String);
            builder.pop();
            
            builder.comment("Zombification Settings")
                   .push("zombification");
            corpseTransformationDays = builder
                    .comment("Days required for full zombification")
                    .defineInRange("transformationDays", 7, 1, 30);
            corpseTransformationDailyDeterioration = builder
                    .comment("Daily deterioration rate after zombification")
                    .defineInRange("dailyDeterioration", 0.15, 0.0, 1.0);
            deathOnAllPartsLost = builder
                    .comment("Die when all body parts lose function")
                    .define("deathOnAllPartsLost", true);
            builder.pop();
            
            builder.comment("PTSD Settings")
                   .push("ptsd");
            ptsdTriggerChance = builder
                    .comment("Chance to trigger PTSD when damaged")
                    .defineInRange("triggerChance", 0.2, 0.0, 1.0);
            ptsdLowHpThreshold = builder
                    .comment("Health percentage threshold for low HP effects")
                    .defineInRange("lowHpThreshold", 0.3, 0.0, 1.0);
            ptsdWeaknessLevel = builder
                    .comment("Weakness effect level")
                    .defineInRange("weaknessLevel", 1, 0, 4);
            ptsdSlownessLevel = builder
                    .comment("Slowness effect level")
                    .defineInRange("slownessLevel", 1, 0, 4);
            ptsdMiningFatigueLevel = builder
                    .comment("Mining fatigue effect level")
                    .defineInRange("miningFatigueLevel", 1, 0, 4);
            ptsdEffectDurationTicks = builder
                    .comment("Effect duration in ticks (20 ticks = 1 second)")
                    .defineInRange("effectDurationTicks", 200, 20, 6000);
            ptsdSleepReliefFactor = builder
                    .comment("PTSD relief multiplier while sleeping")
                    .defineInRange("sleepReliefFactor", 0.5, 0.0, 1.0);
            builder.pop();
            
            builder.comment("Medicine Settings")
                   .push("medicine");
            capsuleMinIntervalSeconds = builder
                    .comment("Minimum interval between taking medicine (seconds)")
                    .defineInRange("minIntervalSeconds", 300, 0, 3600);
            capsuleEffectDelaySeconds = builder
                    .comment("Delay before medicine takes effect (seconds)")
                    .defineInRange("effectDelaySeconds", 120, 0, 600);
            lamivudineAidsCureChance = builder
                    .comment("Chance to cure HIV with Lamivudine")
                    .defineInRange("lamivudineCureChance", 0.10, 0.0, 1.0);
            dextromethorphanMildCureChance = builder
                    .comment("Chance to cure mild URTI with Dextromethorphan")
                    .defineInRange("dextromethorphanMildCureChance", 0.8, 0.0, 1.0);
            dextromethorphanModerateToMildChance = builder
                    .comment("Chance to reduce moderate URTI to mild")
                    .defineInRange("dextromethorphanModerateToMildChance", 0.5, 0.0, 1.0);
            ibuprofenTemporaryRelief = builder
                    .comment("Whether Ibuprofen provides temporary relief")
                    .define("ibuprofenTemporaryRelief", true);
            ibuprofenReliefDurationSeconds = builder
                    .comment("Duration of Ibuprofen relief (seconds)")
                    .defineInRange("ibuprofenReliefDurationSeconds", 300, 0, 3600);
            targetedAgentDeteriorationReduction = builder
                    .comment("Zombification deterioration reduction factor")
                    .defineInRange("targetedAgentDeteriorationReduction", 0.5, 0.0, 1.0);
            sedativePtsdReliefFactor = builder
                    .comment("PTSD relief factor from sedatives")
                    .defineInRange("sedativePtsdReliefFactor", 0.7, 0.0, 1.0);
            ribavirinImmediateEffect = builder
                    .comment("Whether Ribavirin has immediate effect")
                    .define("ribavirinImmediateEffect", true);
            broadSpectrumAntibioticsSepsisCureChance = builder
                    .comment("Chance to cure sepsis with broad-spectrum antibiotics")
                    .defineInRange("broadSpectrumAntibioticsCureChance", 0.9, 0.0, 1.0);
            builder.pop();
            
            builder.comment("Syringe Contamination Settings")
                   .push("syringe");
            syringeContaminationEnabled = builder
                    .comment("Enable syringe contamination mechanic")
                    .define("contaminationEnabled", true);
            syringeContaminationDurationSeconds = builder
                    .comment("How long a syringe stays contaminated (seconds)")
                    .defineInRange("contaminationDurationSeconds", 3600, 0, 86400);
            builder.pop();
            
            builder.comment("Blocking Agent Settings")
                   .push("blocking_agent");
            blockingAgentCorpsePoisonImmunityDurationSeconds = builder
                    .comment("Duration of corpse poison immunity (seconds)")
                    .defineInRange("immunityDurationSeconds", 300, 0, 3600);
            builder.pop();
            
            builder.pop();
        }
    }
    
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
    
    /**
     * 将 Forge 配置值同步到 Config 类的静态字段
     */
    public static void sync() {
        // URTI
        Config.UPPER_RESPIRATORY_INFECTION_RAIN_CHANCE = COMMON.upperRespiratoryInfectionRainChance.get().floatValue();
        Config.UPPER_RESPIRATORY_INFECTION_LEFT_CLICK_CHANCE = COMMON.upperRespiratoryInfectionLeftClickChance.get().floatValue();
        Config.UPPER_RESPIRATORY_INFECTION_RIGHT_CLICK_CHANCE = COMMON.upperRespiratoryInfectionRightClickChance.get().floatValue();
        Config.UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS = COMMON.upperRespiratoryInfectionMildToModerateDays.get();
        Config.UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS = COMMON.upperRespiratoryInfectionModerateToSevereDays.get();
        Config.UPPER_RESPIRATORY_INFECTION_SLEEP_HEAL_CHANCE = COMMON.upperRespiratoryInfectionSleepHealChance.get().floatValue();
        Config.UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION = COMMON.upperRespiratoryInfectionNutrientConsumption.get().floatValue();
        Config.UPPER_RESPIRATORY_INFECTION_SEVERE_CANNOT_HEAL = COMMON.upperRespiratoryInfectionSevereCannotHeal.get();
        
        // Disease boosts
        Config.LOW_PROTEIN_DISEASE_BOOST = COMMON.lowProteinDiseaseBoost.get().floatValue();
        Config.AIDS_DISEASE_BOOST = COMMON.aidsDiseaseBoost.get().floatValue();
        
        // Sepsis
        Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE = COMMON.sepsisContaminatedSyringeChance.get().floatValue();
        Config.SEPSIS_ACTION_EXTRA_SUGAR_COST = COMMON.sepsisActionExtraSugarCost.get().floatValue();
        Config.SEPSIS_ACTION_EXTRA_FAT_COST = COMMON.sepsisActionExtraFatCost.get().floatValue();
        Config.SEPSIS_POISON_CHANCE = COMMON.sepsisPoisonChance.get().floatValue();
        
        // HIV
        Config.AIDS_CONTAMINATED_SYRINGE_CHANCE = COMMON.aidsContaminatedSyringeChance.get().floatValue();
        
        // Zombie virus
        Config.CORPSE_POISON_UNDEAD_ATTACK_CHANCE = COMMON.corpsePoisonUndeadAttackChance.get().floatValue();
        Config.CORPSE_POISON_CUSTOM_ENTITIES = (List<String>) COMMON.corpsePoisonCustomEntities.get();
        
        // Zombification
        Config.CORPSE_TRANSFORMATION_DAYS = COMMON.corpseTransformationDays.get();
        Config.CORPSE_TRANSFORMATION_DAILY_DETERIORATION = COMMON.corpseTransformationDailyDeterioration.get().floatValue();
        Config.DEATH_ON_ALL_PARTS_LOST = COMMON.deathOnAllPartsLost.get();
        
        // PTSD
        Config.PTSD_TRIGGER_CHANCE = COMMON.ptsdTriggerChance.get().floatValue();
        Config.PTSD_LOW_HP_THRESHOLD = COMMON.ptsdLowHpThreshold.get().floatValue();
        Config.PTSD_WEAKNESS_LEVEL = COMMON.ptsdWeaknessLevel.get();
        Config.PTSD_SLOWNESS_LEVEL = COMMON.ptsdSlownessLevel.get();
        Config.PTSD_MINING_FATIGUE_LEVEL = COMMON.ptsdMiningFatigueLevel.get();
        Config.PTSD_EFFECT_DURATION_TICKS = COMMON.ptsdEffectDurationTicks.get();
        Config.PTSD_SLEEP_RELIEF_FACTOR = COMMON.ptsdSleepReliefFactor.get().floatValue();
        
        // Medicine
        Config.CAPSULE_MIN_INTERVAL_SECONDS = COMMON.capsuleMinIntervalSeconds.get();
        Config.CAPSULE_EFFECT_DELAY_SECONDS = COMMON.capsuleEffectDelaySeconds.get();
        Config.LAMIVUDINE_AIDS_CURE_CHANCE = COMMON.lamivudineAidsCureChance.get().floatValue();
        Config.DEXTROMETHORPHAN_MILD_CURE_CHANCE = COMMON.dextromethorphanMildCureChance.get().floatValue();
        Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE = COMMON.dextromethorphanModerateToMildChance.get().floatValue();
        Config.IBUPROFEN_TEMPORARY_RELIEF = COMMON.ibuprofenTemporaryRelief.get();
        Config.IBUPROFEN_RELIEF_DURATION_SECONDS = COMMON.ibuprofenReliefDurationSeconds.get();
        Config.TARGETED_AGENT_DETERIORATION_REDUCTION = COMMON.targetedAgentDeteriorationReduction.get().floatValue();
        Config.SEDATIVE_PTSD_RELIEF_FACTOR = COMMON.sedativePtsdReliefFactor.get().floatValue();
        Config.RIBAVIRIN_IMMEDIATE_EFFECT = COMMON.ribavirinImmediateEffect.get();
        Config.BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE = COMMON.broadSpectrumAntibioticsSepsisCureChance.get().floatValue();
        
        // Syringe
        Config.SYRINGE_CONTAMINATION_ENABLED = COMMON.syringeContaminationEnabled.get();
        Config.SYRINGE_CONTAMINATION_DURATION_SECONDS = COMMON.syringeContaminationDurationSeconds.get();
        
        // Blocking agent
        Config.BLOCKING_AGENT_CORPSE_POISON_IMMUNITY_DURATION_SECONDS = COMMON.blockingAgentCorpsePoisonImmunityDurationSeconds.get().floatValue();
        
        System.out.println("[DGH-HealthCraft] Configuration synced!");
    }
}