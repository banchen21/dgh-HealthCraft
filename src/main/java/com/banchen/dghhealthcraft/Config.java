package com.banchen.dghhealthcraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    // ==================== 上呼吸道感染配置 ====================
    public static float UPPER_RESPIRATORY_INFECTION_RAIN_CHANCE = 0.3f;
    public static float UPPER_RESPIRATORY_INFECTION_LEFT_CLICK_CHANCE = 0.1f;
    public static float UPPER_RESPIRATORY_INFECTION_RIGHT_CLICK_CHANCE = 0.1f;
    public static int UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS = 3;
    public static int UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS = 5;
    public static float UPPER_RESPIRATORY_INFECTION_SLEEP_HEAL_CHANCE = 0.4f;
    public static float UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION = 5.0f;
    public static boolean UPPER_RESPIRATORY_INFECTION_SEVERE_CANNOT_HEAL = true;

    // ==================== 疾病概率加成配置 ====================
    public static float LOW_PROTEIN_DISEASE_BOOST = 1.5f;
    public static float AIDS_DISEASE_BOOST = 2.0f;

    // ==================== 脓毒症配置 ====================
    public static float SEPSIS_CONTAMINATED_SYRINGE_CHANCE = 0.25f;
    public static float SEPSIS_ACTION_EXTRA_SUGAR_COST = 1.5f;
    public static float SEPSIS_ACTION_EXTRA_FAT_COST = 1.5f;
    public static float SEPSIS_POISON_CHANCE = 0.3f;

    // ==================== 艾滋病配置 ====================
    public static float AIDS_CONTAMINATED_SYRINGE_CHANCE = 0.15f;

    // ==================== 尸毒感染配置 ====================
    public static float CORPSE_POISON_UNDEAD_ATTACK_CHANCE = 0.4f;
    public static List<String> CORPSE_POISON_CUSTOM_ENTITIES = null;

    // ==================== 尸变机制配置 ====================
    public static int CORPSE_TRANSFORMATION_DAYS = 7;
    public static float CORPSE_TRANSFORMATION_DAILY_DETERIORATION = 0.15f;
    public static boolean DEATH_ON_ALL_PARTS_LOST = true;

    // ==================== 创伤后应激障碍配置 ====================
    public static float PTSD_TRIGGER_CHANCE = 0.2f;
    public static float PTSD_LOW_HP_THRESHOLD = 0.3f;
    public static int PTSD_WEAKNESS_LEVEL = 1;
    public static int PTSD_SLOWNESS_LEVEL = 1;
    public static int PTSD_MINING_FATIGUE_LEVEL = 1;
    public static int PTSD_EFFECT_DURATION_TICKS = 200;
    public static float PTSD_SLEEP_RELIEF_FACTOR = 0.5f;

    // ==================== 营养系统配置 ====================
    public static float WATER_NORMAL_MIN = 30.0f;
    public static float WATER_NORMAL_MAX = 80.0f;
    public static boolean WATER_DEFICIENCY_BLOCK_METABOLISM = true;
    public static float WATER_DEFICIENCY_POISON_DAMAGE = 1.0f;
    public static int WATER_DEFICIENCY_POISON_INTERVAL_TICKS = 100;
    public static float WATER_SLEEP_DECREASE_RATE = 10.0f;

    public static float SACCHARIDES_NORMAL_MIN = 20.0f;
    public static float SACCHARIDES_NORMAL_MAX = 70.0f;
    public static float SACCHARIDES_LOW_BLINDNESS_INTERVAL_TICKS = 200;
    public static float SACCHARIDES_HIGH_JUMP_WATER_COST_MULTIPLIER = 1.5f;

    public static float FATS_NORMAL_MIN = 15.0f;
    public static float FATS_NORMAL_MAX = 60.0f;
    public static float FATS_LOW_DAMAGE_MULTIPLIER = 1.5f;
    public static int FATS_HIGH_SLOWNESS_LEVEL = 1;
    public static float FATS_HIGH_DAMAGE_ABSORPTION = 0.2f;

    public static float PROTEIN_NORMAL_MIN = 25.0f;
    public static float PROTEIN_NORMAL_MAX = 65.0f;
    public static boolean PROTEIN_LOW_TEAR_EFFECT = true;
    public static boolean PROTEIN_LOW_NO_NATURAL_REGENERATION = true;
    public static boolean PROTEIN_HIGH_HALF_HUNGER = true;
    public static boolean PROTEIN_HIGH_NAUSEA_EFFECT = true;

    public static float INORGANIC_SALT_NORMAL_MIN = 10.0f;
    public static float INORGANIC_SALT_NORMAL_MAX = 50.0f;
    public static boolean INORGANIC_SALT_LOW_BLOOD_TEST_DISABLED = true;
    public static int INORGANIC_SALT_HIGH_HUNGER_LEVEL = 2;

    public static float VITAMIN_NORMAL_MIN = 20.0f;
    public static float VITAMIN_NORMAL_MAX = 70.0f;
    public static float VITAMIN_LOW_INFECTION_BOOST = 1.8f;
    public static boolean VITAMIN_HIGH_HALF_HUNGER = true;

    public static float DIETARY_FIBER_NORMAL_MIN = 15.0f;
    public static float DIETARY_FIBER_NORMAL_MAX = 55.0f;
    public static float DIETARY_FIBER_LOW_DISEASE_RISK_BOOST = 1.4f;
    public static boolean DIETARY_FIBER_HIGH_MALABSORPTION = true;

    // ==================== 药物系统配置 ====================
    public static int CAPSULE_MIN_INTERVAL_SECONDS = 300;
    public static int CAPSULE_EFFECT_DELAY_SECONDS = 120;
    public static float LAMIVUDINE_AIDS_CURE_CHANCE = 0.10f;
    public static float DEXTROMETHORPHAN_MILD_CURE_CHANCE = 0.8f;
    public static float DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE = 0.5f;
    public static boolean IBUPROFEN_TEMPORARY_RELIEF = true;
    public static int IBUPROFEN_RELIEF_DURATION_SECONDS = 300;
    public static float TARGETED_AGENT_DETERIORATION_REDUCTION = 0.5f;
    public static float SEDATIVE_PTSD_RELIEF_FACTOR = 0.7f;
    public static boolean RIBAVIRIN_IMMEDIATE_EFFECT = true;
    public static float BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE = 0.9f;

    // ==================== 药针污染机制 ====================
    public static boolean SYRINGE_CONTAMINATION_ENABLED = true;
    public static int SYRINGE_CONTAMINATION_DURATION_SECONDS = 3600;

    // ==================== 阻断剂配置 ====================
    public static float BLOCKING_AGENT_CORPSE_POISON_IMMUNITY_DURATION_SECONDS = 300;

    /**
     * 初始化配置 - 从 Forge 配置同步
     */
    public static void init() {
        syncFromForgeConfig();
    }

    /**
     * 从 Forge 配置同步到静态字段
     */
    public static void syncFromForgeConfig() {
        ConfigSpec.sync();
    }

    /**
     * 重新加载配置
     */
    public static void reload() {
        syncFromForgeConfig();
    }

    /**
     * 获取配置项的描述信息（用于调试）
     */
    public static Map<String, Object> getAllConfigs() {
        Map<String, Object> configs = new HashMap<>();

        // 上呼吸道感染配置
        configs.put("upper_respiratory_infection_rain_chance", UPPER_RESPIRATORY_INFECTION_RAIN_CHANCE);
        configs.put("upper_respiratory_infection_left_click_chance", UPPER_RESPIRATORY_INFECTION_LEFT_CLICK_CHANCE);
        configs.put("upper_respiratory_infection_right_click_chance", UPPER_RESPIRATORY_INFECTION_RIGHT_CLICK_CHANCE);
        configs.put("upper_respiratory_infection_mild_to_moderate_days",
                UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS);
        configs.put("upper_respiratory_infection_moderate_to_severe_days",
                UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS);
        configs.put("upper_respiratory_infection_sleep_heal_chance", UPPER_RESPIRATORY_INFECTION_SLEEP_HEAL_CHANCE);
        configs.put("upper_respiratory_infection_nutrient_consumption",
                UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION);
        configs.put("upper_respiratory_infection_severe_cannot_heal", UPPER_RESPIRATORY_INFECTION_SEVERE_CANNOT_HEAL);

        // 疾病概率加成
        configs.put("low_protein_disease_boost", LOW_PROTEIN_DISEASE_BOOST);
        configs.put("aids_disease_boost", AIDS_DISEASE_BOOST);

        // 脓毒症配置
        configs.put("sepsis_contaminated_syringe_chance", SEPSIS_CONTAMINATED_SYRINGE_CHANCE);
        configs.put("sepsis_action_extra_sugar_cost", SEPSIS_ACTION_EXTRA_SUGAR_COST);
        configs.put("sepsis_action_extra_fat_cost", SEPSIS_ACTION_EXTRA_FAT_COST);
        configs.put("sepsis_poison_chance", SEPSIS_POISON_CHANCE);

        // 艾滋病配置
        configs.put("aids_contaminated_syringe_chance", AIDS_CONTAMINATED_SYRINGE_CHANCE);

        // 尸毒感染配置
        configs.put("corpse_poison_undead_attack_chance", CORPSE_POISON_UNDEAD_ATTACK_CHANCE);
        configs.put("corpse_poison_custom_entities", CORPSE_POISON_CUSTOM_ENTITIES);

        // 尸变机制配置
        configs.put("corpse_transformation_days", CORPSE_TRANSFORMATION_DAYS);
        configs.put("corpse_transformation_daily_deterioration", CORPSE_TRANSFORMATION_DAILY_DETERIORATION);
        configs.put("death_on_all_parts_lost", DEATH_ON_ALL_PARTS_LOST);

        // PTSD 配置
        configs.put("ptsd_trigger_chance", PTSD_TRIGGER_CHANCE);
        configs.put("ptsd_low_hp_threshold", PTSD_LOW_HP_THRESHOLD);
        configs.put("ptsd_weakness_level", PTSD_WEAKNESS_LEVEL);
        configs.put("ptsd_slowness_level", PTSD_SLOWNESS_LEVEL);
        configs.put("ptsd_mining_fatigue_level", PTSD_MINING_FATIGUE_LEVEL);
        configs.put("ptsd_effect_duration_ticks", PTSD_EFFECT_DURATION_TICKS);
        configs.put("ptsd_sleep_relief_factor", PTSD_SLEEP_RELIEF_FACTOR);

        // 药物配置
        configs.put("capsule_min_interval_seconds", CAPSULE_MIN_INTERVAL_SECONDS);
        configs.put("capsule_effect_delay_seconds", CAPSULE_EFFECT_DELAY_SECONDS);
        configs.put("lamivudine_aids_cure_chance", LAMIVUDINE_AIDS_CURE_CHANCE);
        configs.put("dextromethorphan_mild_cure_chance", DEXTROMETHORPHAN_MILD_CURE_CHANCE);
        configs.put("dextromethorphan_moderate_to_mild_chance", DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE);
        configs.put("ibuprofen_temporary_relief", IBUPROFEN_TEMPORARY_RELIEF);
        configs.put("ibuprofen_relief_duration_seconds", IBUPROFEN_RELIEF_DURATION_SECONDS);
        configs.put("targeted_agent_deterioration_reduction", TARGETED_AGENT_DETERIORATION_REDUCTION);
        configs.put("sedative_ptsd_relief_factor", SEDATIVE_PTSD_RELIEF_FACTOR);
        configs.put("ribavirin_immediate_effect", RIBAVIRIN_IMMEDIATE_EFFECT);
        configs.put("broad_spectrum_antibiotics_sepsis_cure_chance", BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE);

        // 药针污染
        configs.put("syringe_contamination_enabled", SYRINGE_CONTAMINATION_ENABLED);
        configs.put("syringe_contamination_duration_seconds", SYRINGE_CONTAMINATION_DURATION_SECONDS);
        configs.put("blocking_agent_corpse_poison_immunity_duration_seconds",
                BLOCKING_AGENT_CORPSE_POISON_IMMUNITY_DURATION_SECONDS);

        return configs;
    }
}