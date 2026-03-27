package com.banchen.dghhealthcraft;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import net.minecraftforge.common.ForgeConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public class Config {

        public static final Common COMMON;
        public static final ForgeConfigSpec COMMON_SPEC;

        // ==================== 上呼吸道感染配置 ====================
        // 下雨时感染轻型上呼吸道感染的概率
        public static float URTI_RAIN_INFECTION_CHANCE = 0.3f;
        // 左键点击时感染轻型上呼吸道感染的概率
        public static float URTI_LEFT_CLICK_INFECTION_CHANCE = 0.1f;
        // 右键点击时感染轻型上呼吸道感染的概率
        public static float URTI_RIGHT_CLICK_INFECTION_CHANCE = 0.1f;
        // 睡觉时自愈的概率
        public static float URTI_SLEEP_HEAL_CHANCE = 0.4f;

        // ==================== 其他疾病相关配置 ====================
        // 蛋白质过低时感染艾滋病的概率
        public static float LOW_PROTEIN_AIDS_INFECTION_CHANCE = 0.2f;
        // 低蛋白疾病加成倍数
        public static float LOW_PROTEIN_DISEASE_BOOST = 1.5f;
        // 艾滋病疾病加成倍数
        public static float AIDS_DISEASE_BOOST = 2.0f;
        // 维生素缺乏感染加成倍数
        public static float VITAMIN_LOW_INFECTION_BOOST = 1.3f;
        // 膳食纤维缺乏疾病风险加成倍数
        public static float DIETARY_FIBER_LOW_DISEASE_RISK_BOOST = 1.2f;

        // ==================== 疾病进展配置 ====================
        // 轻型到中型的天数
        public static int UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS = 3;
        // 中型到重型的的天数
        public static int UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS = 5;
        // 睡眠自愈营养消耗
        public static float UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION = 2.0f;

        // ==================== 营养正常值配置 ====================
        public static float PROTEIN_NORMAL_MIN = 30.0f;
        public static float VITAMIN_NORMAL_MIN = 20.0f;
        public static float DIETARY_FIBER_NORMAL_MIN = 15.0f;

        // ==================== 症状效果配置 ====================
        public static int PTSD_EFFECT_DURATION_TICKS = 200;
        public static int PTSD_MINING_FATIGUE_LEVEL = 1;
        public static int PTSD_WEAKNESS_LEVEL = 1;

        // ==================== 药物效果配置 ====================
        // 右美沙芬治愈轻型概率
        public static float DEXTROMETHORPHAN_MILD_CURE_CHANCE = 0.7f;
        // 右美沙芬中型转轻型概率
        public static float DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE = 0.5f;
        // 利巴韦林立即治疗效果
        public static boolean RIBAVIRIN_IMMEDIATE_EFFECT = true;
        // 布洛芬暂时缓解效果
        public static boolean IBUPROFEN_TEMPORARY_RELIEF = true;

        // ==================== 胶囊冷却时间配置 ====================
        // 拉米夫定冷却时间（秒）
        public static int LAMIVUDINE_COOLDOWN_SECONDS = 5;
        // 布洛芬冷却时间（秒）
        public static int IBUPROFEN_COOLDOWN_SECONDS = 5;
        // 右美沙芬冷却时间（秒）
        public static int DEXTROMETHORPHAN_COOLDOWN_SECONDS = 5;

        // ==================== HIV 相关配置 ====================
        // 污染药针感染 HIV 的概率
        public static float AIDS_CONTAMINATED_SYRINGE_CHANCE = 0.3f;
        // 拉米夫定治愈艾滋病的概率
        public static float LAMIVUDINE_AIDS_CURE_CHANCE = 0.1f;
        // PTSD 缓慢效果等级
        public static int PTSD_SLOWNESS_LEVEL = 1;

        // ==================== PTSD 相关配置 ====================
        // PTSD 触发概率
        public static float PTSD_TRIGGER_CHANCE = 0.5f;
        // 低血量阈值
        public static float PTSD_LOW_HP_THRESHOLD = 0.3f;
        // 睡眠缓解系数
        public static float PTSD_SLEEP_RELIEF_FACTOR = 2.0f;
        // 镇静剂 PTSD 缓解系数
        public static float SEDATIVE_PTSD_RELIEF_FACTOR = 0.5f;

        // ==================== 脓毒症相关配置 ====================
        // 污染药针感染脓毒症的概率
        public static float SEPSIS_CONTAMINATED_SYRINGE_CHANCE = 0.2f;
        // 广谱抗生素治愈脓毒症的概率
        public static float BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE = 0.3f;
        // 脓毒症中毒概率
        public static float SEPSIS_POISON_CHANCE = 0.1f;
        // 行动时额外消耗糖类
        public static float SEPSIS_ACTION_EXTRA_SUGAR_COST = 0.5f;
        // 行动时额外消耗油脂
        public static float SEPSIS_ACTION_EXTRA_FAT_COST = 0.3f;

        // ==================== 尸毒感染相关配置 ====================
        // 亡灵生物攻击感染尸毒的概率
        public static float CORPSE_POISON_UNDEAD_ATTACK_CHANCE = 0.15f;
        // 尸毒转化为尸变的天数
        public static int CORPSE_TRANSFORMATION_DAYS = 7;
        // 尸变后每天身体部位恶化程度
        public static float CORPSE_TRANSFORMATION_DAILY_DETERIORATION = 0.1f;
        // 所有部位失去功能后是否死亡
        public static boolean DEATH_ON_ALL_PARTS_LOST = true;
        // 自定义可传播尸毒的实体类型（列表）
        public static List<String> CORPSE_POISON_CUSTOM_ENTITIES = new ArrayList<>();
        // 靶向剂缓解尸变恶化的效果系数
        public static float TARGETED_AGENT_DETERIORATION_REDUCTION = 0.3f;
        // 阻断剂注射液效果持续时间（秒）
        public static int BLOCKING_AGENT_DURATION_SECONDS = 600;

        // ==================== 口服剂冷却时间配置 ====================
        public static int CAPSULE_EFFECT_DELAY_SECONDS = 5;

        // ==================== 注射器冷却时间配置 ====================
        public static int RIBAVIRIN_COOLDOWN_SECONDS = 5;

        // ==================== 食物营养映射配置 ====================
        public static Map<String, NutritionValues> FOOD_NUTRITION_MAP = new java.util.HashMap<>();
        public static java.util.List<String> FOOD_NUTRITION_BY_NUTRIENT = new java.util.ArrayList<>();

        // ==================== 营养消耗配置 ====================
        // 正常情况水分消耗速率（每秒）
        public static float NUTRITION_WATER_DECREASE_RATE = 0.001f;
        // 睡眠时水分消耗速率（每秒）
        public static float NUTRITION_SLEEP_WATER_DECREASE_RATE = 0.005f;
        // 正常情况下糖分消耗速率（每秒）
        public static float NUTRITION_SUGAR_DECREASE_RATE = 0.0005f;
        // 正常情况下脂肪消耗速率（每秒）
        public static float NUTRITION_FAT_DECREASE_RATE = 0.0003f;
        // 正常情况下蛋白质消耗速率（每秒）
        public static float NUTRITION_PROTEIN_DECREASE_RATE = 0.0002f;
        // 正常情况下盐分消耗速率（每秒）
        public static float NUTRITION_SALT_DECREASE_RATE = 0.0001f;
        // 正常情况下维生素消耗速率（每秒）
        public static float NUTRITION_VITAMIN_DECREASE_RATE = 0.0001f;
        // 正常情况下膳食纤维消耗速率（每秒）
        public static float NUTRITION_FIBER_DECREASE_RATE = 0.0001f;
        // 缺水是否阻止新陈代谢
        public static boolean WATER_DEFICIENCY_BLOCK_METABOLISM = true;
        // 缺水中毒间隔（刻）
        public static int WATER_DEFICIENCY_POISON_INTERVAL_TICKS = 100;
        // 缺水中毒伤害
        public static float WATER_DEFICIENCY_POISON_DAMAGE = 1.0f;
        // 睡眠水分消耗（每秒）
        public static float WATER_SLEEP_DECREASE_RATE = 0.002f;

        // ==================== 糖类配置 ====================
        public static float SACCHARIDES_NORMAL_MIN = 20.0f;
        public static float SACCHARIDES_NORMAL_MAX = 80.0f;
        public static float SACCHARIDES_LOW_BLINDNESS_INTERVAL_TICKS = 200;
        public static float SACCHARIDES_HIGH_JUMP_WATER_COST_MULTIPLIER = 0.5f;

        // ==================== 脂肪配置 ====================
        public static float FATS_NORMAL_MIN = 20.0f;
        public static float FATS_NORMAL_MAX = 80.0f;
        public static int FATS_HIGH_SLOWNESS_LEVEL = 1;
        public static float FATS_LOW_DAMAGE_MULTIPLIER = 1.5f;
        public static float FATS_HIGH_DAMAGE_ABSORPTION = 0.3f;

        // ==================== 蛋白质配置 ====================
        public static boolean PROTEIN_LOW_TEAR_EFFECT = true;
        public static boolean PROTEIN_LOW_NO_NATURAL_REGENERATION = true;
        public static boolean PROTEIN_HIGH_HALF_HUNGER = true;
        public static boolean PROTEIN_HIGH_NAUSEA_EFFECT = true;
        public static float PROTEIN_NORMAL_MAX = 70.0f;

        // ==================== 无机盐配置 ====================
        public static float INORGANIC_SALT_NORMAL_MIN = 20.0f;
        public static float INORGANIC_SALT_NORMAL_MAX = 80.0f;
        public static boolean INORGANIC_SALT_LOW_BLOOD_TEST_DISABLED = true;
        public static int INORGANIC_SALT_HIGH_HUNGER_LEVEL = 1;

        // ==================== 维生素配置 ====================
        public static boolean VITAMIN_HIGH_HALF_HUNGER = true;

        // ==================== 膳食纤维配置 ====================
        public static float DIETARY_FIBER_NORMAL_MAX = 80.0f;
        public static boolean DIETARY_FIBER_HIGH_MALABSORPTION = true;

        public static float WATER_NORMAL_MIN = 30.0f;
        public static float VITAMIN_NORMAL_MAX = 70.0f;
        static {
                final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
                COMMON_SPEC = specPair.getRight();
                COMMON = specPair.getLeft();
        }

        /**
         * 更新配置值
         */
        public static void updateConfigValues() {
                // 上呼吸道感染配置
                URTI_RAIN_INFECTION_CHANCE = COMMON.upperRespiratoryInfectionRainChance.get().floatValue();
                URTI_LEFT_CLICK_INFECTION_CHANCE = COMMON.upperRespiratoryInfectionLeftClickChance.get().floatValue();
                URTI_RIGHT_CLICK_INFECTION_CHANCE = COMMON.upperRespiratoryInfectionRightClickChance.get().floatValue();
                URTI_SLEEP_HEAL_CHANCE = COMMON.upperRespiratoryInfectionSleepHealChance.get().floatValue();

                // 疾病加成配置
                LOW_PROTEIN_DISEASE_BOOST = COMMON.lowProteinDiseaseBoost.get().floatValue();
                AIDS_DISEASE_BOOST = COMMON.aidsDiseaseBoost.get().floatValue();
                VITAMIN_LOW_INFECTION_BOOST = COMMON.vitaminLowInfectionBoost.get().floatValue();
                DIETARY_FIBER_LOW_DISEASE_RISK_BOOST = COMMON.dietaryFiberLowDiseaseRiskBoost.get().floatValue();

                // 疾病进展配置
                UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS = COMMON.urtiMildToModerateDays.get();
                UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS = COMMON.urtiModerateToSevereDays.get();
                UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION = COMMON.urtiNutrientConsumption.get().floatValue();

                // 营养正常值配置
                PROTEIN_NORMAL_MIN = COMMON.proteinNormalMin.get().floatValue();
                VITAMIN_NORMAL_MIN = COMMON.vitaminNormalMin.get().floatValue();
                DIETARY_FIBER_NORMAL_MIN = COMMON.dietaryFiberNormalMin.get().floatValue();

                // 症状效果配置
                PTSD_EFFECT_DURATION_TICKS = COMMON.ptsdEffectDurationTicks.get();
                PTSD_MINING_FATIGUE_LEVEL = COMMON.ptsdMiningFatigueLevel.get();
                PTSD_WEAKNESS_LEVEL = COMMON.ptsdWeaknessLevel.get();

                // 药物效果配置
                DEXTROMETHORPHAN_MILD_CURE_CHANCE = COMMON.dextromethorphanMildCureChance.get().floatValue();
                DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE = COMMON.dextromethorphanModerateToMildChance.get()
                                .floatValue();
                RIBAVIRIN_IMMEDIATE_EFFECT = COMMON.ribavirinImmediateEffect.get();
                IBUPROFEN_TEMPORARY_RELIEF = COMMON.ibuprofenTemporaryRelief.get();

                // 艾滋病相关配置
                LOW_PROTEIN_AIDS_INFECTION_CHANCE = COMMON.lowProteinAidsInfectionChance.get().floatValue();

                // 胶囊冷却时间配置
                LAMIVUDINE_COOLDOWN_SECONDS = COMMON.lamivudineCooldownSeconds.get();
                IBUPROFEN_COOLDOWN_SECONDS = COMMON.ibuprofenCooldownSeconds.get();
                DEXTROMETHORPHAN_COOLDOWN_SECONDS = COMMON.dextromethorphanCooldownSeconds.get();

                // HIV 相关配置
                AIDS_CONTAMINATED_SYRINGE_CHANCE = COMMON.aidsContaminatedSyringeChance.get().floatValue();
                LAMIVUDINE_AIDS_CURE_CHANCE = COMMON.lamivudineAidsCureChance.get().floatValue();
                PTSD_SLOWNESS_LEVEL = COMMON.ptsdSlownessLevel.get();

                // PTSD 相关配置
                PTSD_TRIGGER_CHANCE = COMMON.ptsdTriggerChance.get().floatValue();
                PTSD_LOW_HP_THRESHOLD = COMMON.ptsdLowHpThreshold.get().floatValue();
                PTSD_SLEEP_RELIEF_FACTOR = COMMON.ptsdSleepReliefFactor.get().floatValue();
                SEDATIVE_PTSD_RELIEF_FACTOR = COMMON.sedativePtsdReliefFactor.get().floatValue();

                // 脓毒症相关配置
                SEPSIS_CONTAMINATED_SYRINGE_CHANCE = COMMON.sepsisContaminatedSyringeChance.get().floatValue();
                BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE = COMMON.broadSpectrumAntibioticsSepsisCureChance.get()
                                .floatValue();
                SEPSIS_POISON_CHANCE = COMMON.sepsisPoisonChance.get().floatValue();
                SEPSIS_ACTION_EXTRA_SUGAR_COST = COMMON.sepsisActionExtraSugarCost.get().floatValue();
                SEPSIS_ACTION_EXTRA_FAT_COST = COMMON.sepsisActionExtraFatCost.get().floatValue();

                // 尸毒感染相关配置
                CORPSE_POISON_UNDEAD_ATTACK_CHANCE = COMMON.corpsePoisonUndeadAttackChance.get().floatValue();
                CORPSE_TRANSFORMATION_DAYS = COMMON.corpseTransformationDays.get();
                CORPSE_TRANSFORMATION_DAILY_DETERIORATION = COMMON.corpseTransformationDailyDeterioration.get()
                                .floatValue();
                DEATH_ON_ALL_PARTS_LOST = COMMON.deathOnAllPartsLost.get();
                CORPSE_POISON_CUSTOM_ENTITIES = new ArrayList<>(COMMON.corpsePoisonCustomEntities.get());
                TARGETED_AGENT_DETERIORATION_REDUCTION = COMMON.targetedAgentDeteriorationReduction.get().floatValue();
                BLOCKING_AGENT_DURATION_SECONDS = COMMON.blockingAgentDurationSeconds.get();

                // 口服剂冷却时间配置
                CAPSULE_EFFECT_DELAY_SECONDS = COMMON.capsuleEffectDelaySeconds.get();

                // 注射器冷却时间配置
                RIBAVIRIN_COOLDOWN_SECONDS = COMMON.ribavirinCooldownSeconds.get();

                // 食物营养映射配置
                loadFoodNutritionMap(COMMON.foodNutritionWater.get(),
                                COMMON.foodNutritionSugar.get(),
                                COMMON.foodNutritionFat.get(),
                                COMMON.foodNutritionProtein.get(),
                                COMMON.foodNutritionSalt.get(),
                                COMMON.foodNutritionVitamin.get(),
                                COMMON.foodNutritionFiber.get());

                // 营养消耗配置
                NUTRITION_WATER_DECREASE_RATE = COMMON.nutritionWaterDecreaseRate.get().floatValue();
                NUTRITION_SLEEP_WATER_DECREASE_RATE = COMMON.nutritionSleepWaterDecreaseRate.get().floatValue();
                NUTRITION_SUGAR_DECREASE_RATE = COMMON.nutritionSugarDecreaseRate.get().floatValue();
                NUTRITION_FAT_DECREASE_RATE = COMMON.nutritionFatDecreaseRate.get().floatValue();
                NUTRITION_PROTEIN_DECREASE_RATE = COMMON.nutritionProteinDecreaseRate.get().floatValue();
                NUTRITION_SALT_DECREASE_RATE = COMMON.nutritionSaltDecreaseRate.get().floatValue();
                NUTRITION_VITAMIN_DECREASE_RATE = COMMON.nutritionVitaminDecreaseRate.get().floatValue();
                NUTRITION_FIBER_DECREASE_RATE = COMMON.nutritionFiberDecreaseRate.get().floatValue();
                WATER_DEFICIENCY_BLOCK_METABOLISM = COMMON.waterDeficiencyBlockMetabolism.get();
                WATER_DEFICIENCY_POISON_INTERVAL_TICKS = COMMON.waterDeficiencyPoisonIntervalTicks.get();
                WATER_DEFICIENCY_POISON_DAMAGE = COMMON.waterDeficiencyPoisonDamage.get().floatValue();
                WATER_SLEEP_DECREASE_RATE = COMMON.waterSleepDecreaseRate.get().floatValue();

                // 糖类配置
                SACCHARIDES_NORMAL_MIN = COMMON.saccharidesNormalMin.get().floatValue();
                SACCHARIDES_NORMAL_MAX = COMMON.saccharidesNormalMax.get().floatValue();
                SACCHARIDES_LOW_BLINDNESS_INTERVAL_TICKS = COMMON.saccharidesLowBlindnessIntervalTicks.get()
                                .floatValue();
                SACCHARIDES_HIGH_JUMP_WATER_COST_MULTIPLIER = COMMON.saccharidesHighJumpWaterCostMultiplier.get()
                                .floatValue();

                // 脂肪配置
                FATS_NORMAL_MIN = COMMON.fatsNormalMin.get().floatValue();
                FATS_NORMAL_MAX = COMMON.fatsNormalMax.get().floatValue();
                FATS_HIGH_SLOWNESS_LEVEL = COMMON.fatsHighSlownessLevel.get();
                FATS_LOW_DAMAGE_MULTIPLIER = COMMON.fatsLowDamageMultiplier.get().floatValue();
                FATS_HIGH_DAMAGE_ABSORPTION = COMMON.fatsHighDamageAbsorption.get().floatValue();

                // 蛋白质配置
                PROTEIN_LOW_TEAR_EFFECT = COMMON.proteinLowTearEffect.get();
                PROTEIN_LOW_NO_NATURAL_REGENERATION = COMMON.proteinLowNoNaturalRegeneration.get();
                PROTEIN_HIGH_HALF_HUNGER = COMMON.proteinHighHalfHunger.get();
                PROTEIN_HIGH_NAUSEA_EFFECT = COMMON.proteinHighNauseaEffect.get();
                PROTEIN_NORMAL_MAX = COMMON.proteinNormalMax.get().floatValue();

                // 无机盐配置
                INORGANIC_SALT_NORMAL_MIN = COMMON.inorganicSaltNormalMin.get().floatValue();
                INORGANIC_SALT_NORMAL_MAX = COMMON.inorganicSaltNormalMax.get().floatValue();
                INORGANIC_SALT_LOW_BLOOD_TEST_DISABLED = COMMON.inorganicSaltLowBloodTestDisabled.get();
                INORGANIC_SALT_HIGH_HUNGER_LEVEL = COMMON.inorganicSaltHighHungerLevel.get();

                // 维生素配置
                VITAMIN_HIGH_HALF_HUNGER = COMMON.vitaminHighHalfHunger.get();

                // 膳食纤维配置
                DIETARY_FIBER_NORMAL_MAX = COMMON.dietaryFiberNormalMax.get().floatValue();
                DIETARY_FIBER_HIGH_MALABSORPTION = COMMON.dietaryFiberHighMalabsorption.get();

                // 水分配置
                WATER_NORMAL_MIN = COMMON.waterNormalMin.get().floatValue();
                VITAMIN_NORMAL_MAX = COMMON.vitaminNormalMax.get().floatValue();
        }

        public static class Common {
                // 上呼吸道感染配置
                public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionRainChance;
                public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionLeftClickChance;
                public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionRightClickChance;
                public final ForgeConfigSpec.DoubleValue upperRespiratoryInfectionSleepHealChance;

                // 疾病加成配置
                public final ForgeConfigSpec.DoubleValue lowProteinDiseaseBoost;
                public final ForgeConfigSpec.DoubleValue aidsDiseaseBoost;
                public final ForgeConfigSpec.DoubleValue vitaminLowInfectionBoost;
                public final ForgeConfigSpec.DoubleValue dietaryFiberLowDiseaseRiskBoost;

                // 疾病进展配置
                public final ForgeConfigSpec.IntValue urtiMildToModerateDays;
                public final ForgeConfigSpec.IntValue urtiModerateToSevereDays;
                public final ForgeConfigSpec.DoubleValue urtiNutrientConsumption;

                // 营养正常值配置
                public final ForgeConfigSpec.DoubleValue proteinNormalMin;
                public final ForgeConfigSpec.DoubleValue vitaminNormalMin;
                public final ForgeConfigSpec.DoubleValue dietaryFiberNormalMin;

                // 症状效果配置
                public final ForgeConfigSpec.IntValue ptsdEffectDurationTicks;
                public final ForgeConfigSpec.IntValue ptsdMiningFatigueLevel;
                public final ForgeConfigSpec.IntValue ptsdWeaknessLevel;

                // 药物效果配置
                public final ForgeConfigSpec.DoubleValue dextromethorphanMildCureChance;
                public final ForgeConfigSpec.DoubleValue dextromethorphanModerateToMildChance;
                public final ForgeConfigSpec.BooleanValue ribavirinImmediateEffect;
                public final ForgeConfigSpec.BooleanValue ibuprofenTemporaryRelief;

                // 艾滋病配置
                public final ForgeConfigSpec.DoubleValue lowProteinAidsInfectionChance;

                // 胶囊冷却时间配置
                public final ForgeConfigSpec.IntValue lamivudineCooldownSeconds;
                public final ForgeConfigSpec.IntValue ibuprofenCooldownSeconds;
                public final ForgeConfigSpec.IntValue dextromethorphanCooldownSeconds;

                // HIV 相关配置
                public final ForgeConfigSpec.DoubleValue aidsContaminatedSyringeChance;
                public final ForgeConfigSpec.DoubleValue lamivudineAidsCureChance;
                public final ForgeConfigSpec.IntValue ptsdSlownessLevel;

                // PTSD 相关配置
                public final ForgeConfigSpec.DoubleValue ptsdTriggerChance;
                public final ForgeConfigSpec.DoubleValue ptsdLowHpThreshold;
                public final ForgeConfigSpec.DoubleValue ptsdSleepReliefFactor;
                public final ForgeConfigSpec.DoubleValue sedativePtsdReliefFactor;

                // 脓毒症相关配置
                public final ForgeConfigSpec.DoubleValue sepsisContaminatedSyringeChance;
                public final ForgeConfigSpec.DoubleValue broadSpectrumAntibioticsSepsisCureChance;
                public final ForgeConfigSpec.DoubleValue sepsisPoisonChance;
                public final ForgeConfigSpec.DoubleValue sepsisActionExtraSugarCost;
                public final ForgeConfigSpec.DoubleValue sepsisActionExtraFatCost;

                // 尸毒感染相关配置
                public final ForgeConfigSpec.DoubleValue corpsePoisonUndeadAttackChance;
                public final ForgeConfigSpec.IntValue corpseTransformationDays;
                public final ForgeConfigSpec.DoubleValue corpseTransformationDailyDeterioration;
                public final ForgeConfigSpec.BooleanValue deathOnAllPartsLost;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> corpsePoisonCustomEntities;
                public final ForgeConfigSpec.DoubleValue targetedAgentDeteriorationReduction;
                public final ForgeConfigSpec.IntValue blockingAgentDurationSeconds;

                // 口服剂冷却时间配置
                public final ForgeConfigSpec.IntValue capsuleEffectDelaySeconds;

                // 注射器冷却时间配置
                public final ForgeConfigSpec.IntValue ribavirinCooldownSeconds;

                // 营养参数配置
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionWater;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionSugar;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionFat;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionProtein;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionSalt;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionVitamin;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodNutritionFiber;

                // 营养消耗配置
                public final ForgeConfigSpec.DoubleValue nutritionWaterDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionSleepWaterDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionSugarDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionFatDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionProteinDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionSaltDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionVitaminDecreaseRate;
                public final ForgeConfigSpec.DoubleValue nutritionFiberDecreaseRate;
                public final ForgeConfigSpec.BooleanValue waterDeficiencyBlockMetabolism;
                public final ForgeConfigSpec.IntValue waterDeficiencyPoisonIntervalTicks;
                public final ForgeConfigSpec.DoubleValue waterDeficiencyPoisonDamage;
                public final ForgeConfigSpec.DoubleValue waterSleepDecreaseRate;

                // 糖类配置
                public final ForgeConfigSpec.DoubleValue saccharidesNormalMin;
                public final ForgeConfigSpec.DoubleValue saccharidesNormalMax;
                public final ForgeConfigSpec.IntValue saccharidesLowBlindnessIntervalTicks;
                public final ForgeConfigSpec.DoubleValue saccharidesHighJumpWaterCostMultiplier;
                // 脂肪配置
                public final ForgeConfigSpec.DoubleValue fatsNormalMin;
                public final ForgeConfigSpec.DoubleValue fatsNormalMax;
                public final ForgeConfigSpec.IntValue fatsHighSlownessLevel;
                public final ForgeConfigSpec.DoubleValue fatsLowDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue fatsHighDamageAbsorption;

                // 蛋白质配置
                public final ForgeConfigSpec.BooleanValue proteinLowTearEffect;
                public final ForgeConfigSpec.BooleanValue proteinLowNoNaturalRegeneration;
                public final ForgeConfigSpec.BooleanValue proteinHighHalfHunger;
                public final ForgeConfigSpec.BooleanValue proteinHighNauseaEffect;
                public final ForgeConfigSpec.DoubleValue proteinNormalMax;

                // 无机盐配置
                public final ForgeConfigSpec.DoubleValue inorganicSaltNormalMin;
                public final ForgeConfigSpec.DoubleValue inorganicSaltNormalMax;
                public final ForgeConfigSpec.BooleanValue inorganicSaltLowBloodTestDisabled;
                public final ForgeConfigSpec.IntValue inorganicSaltHighHungerLevel;

                // 维生素配置
                public final ForgeConfigSpec.BooleanValue vitaminHighHalfHunger;

                // 膳食纤维配置
                public final ForgeConfigSpec.DoubleValue dietaryFiberNormalMax;
                public final ForgeConfigSpec.BooleanValue dietaryFiberHighMalabsorption;

                // 水分配置
                public final ForgeConfigSpec.DoubleValue waterNormalMin;
                public final ForgeConfigSpec.DoubleValue vitaminNormalMax;

                Common(ForgeConfigSpec.Builder builder) {
                        builder.comment("DGH-医疗工艺 配置文件")
                                        .push("general");

                        // ==================== 上呼吸道感染配置 ====================
                        builder.comment("上呼吸道感染 (URTI) 设置")
                                        .push("urti");

                        upperRespiratoryInfectionRainChance = builder
                                        .comment("下雨时感染上呼吸道感染的几率")
                                        .defineInRange("rainChance", 0.3, 0.0, 1.0);

                        upperRespiratoryInfectionLeftClickChance = builder
                                        .comment("左键点击时感染上呼吸道感染的几率")
                                        .defineInRange("leftClickChance", 0.1, 0.0, 1.0);

                        upperRespiratoryInfectionRightClickChance = builder
                                        .comment("右键点击时感染上呼吸道感染的几率")
                                        .defineInRange("rightClickChance", 0.1, 0.0, 1.0);

                        upperRespiratoryInfectionSleepHealChance = builder
                                        .comment("睡觉时自愈上呼吸道感染的几率")
                                        .defineInRange("sleepHealChance", 0.4, 0.0, 1.0);

                        urtiMildToModerateDays = builder
                                        .comment("上呼吸道感染从轻型发展到中型所需的天数")
                                        .defineInRange("mildToModerateDays", 3, 1, 30);

                        urtiModerateToSevereDays = builder
                                        .comment("上呼吸道感染从中型发展到重型所需的天数")
                                        .defineInRange("moderateToSevereDays", 5, 1, 30);

                        urtiNutrientConsumption = builder
                                        .comment("睡眠自愈时消耗的营养元素量")
                                        .defineInRange("nutrientConsumption", 2.0, 0.0, 10.0);

                        builder.pop();

                        // ==================== 营养缺乏加成配置 ====================
                        builder.comment("营养缺乏对疾病的影响")
                                        .push("nutrition_deficiency");

                        lowProteinDiseaseBoost = builder
                                        .comment("低蛋白状态对疾病感染和恶化的加成倍数")
                                        .defineInRange("lowProteinDiseaseBoost", 1.5, 1.0, 5.0);

                        vitaminLowInfectionBoost = builder
                                        .comment("维生素缺乏对疾病感染的加成倍数")
                                        .defineInRange("vitaminLowInfectionBoost", 1.3, 1.0, 5.0);

                        dietaryFiberLowDiseaseRiskBoost = builder
                                        .comment("膳食纤维缺乏对疾病风险的加成倍数")
                                        .defineInRange("dietaryFiberLowDiseaseRiskBoost", 1.2, 1.0, 5.0);

                        proteinNormalMin = builder
                                        .comment("蛋白质正常值下限")
                                        .defineInRange("proteinNormalMin", 30.0, 0.0, 100.0);

                        vitaminNormalMin = builder
                                        .comment("维生素正常值下限")
                                        .defineInRange("vitaminNormalMin", 20.0, 0.0, 100.0);

                        dietaryFiberNormalMin = builder
                                        .comment("膳食纤维正常值下限")
                                        .defineInRange("dietaryFiberNormalMin", 15.0, 0.0, 100.0);

                        builder.pop();

                        // ==================== 艾滋病配置 ====================
                        builder.comment("艾滋病 (AIDS) 设置")
                                        .push("aids");

                        lowProteinAidsInfectionChance = builder
                                        .comment("蛋白质过低时感染艾滋病的几率")
                                        .defineInRange("lowProteinInfectionChance", 0.2, 0.0, 1.0);

                        aidsDiseaseBoost = builder
                                        .comment("艾滋病对疾病感染和恶化的加成倍数")
                                        .defineInRange("aidsDiseaseBoost", 2.0, 1.0, 5.0);

                        builder.pop();

                        // ==================== 症状效果配置 ====================
                        builder.comment("症状效果设置")
                                        .push("symptoms");

                        ptsdEffectDurationTicks = builder
                                        .comment("症状效果持续时间（刻）")
                                        .defineInRange("effectDurationTicks", 200, 20, 1200);

                        ptsdMiningFatigueLevel = builder
                                        .comment("挖掘疲劳效果等级")
                                        .defineInRange("miningFatigueLevel", 1, 0, 4);

                        ptsdWeaknessLevel = builder
                                        .comment("虚弱效果等级")
                                        .defineInRange("weaknessLevel", 1, 0, 4);

                        builder.pop();

                        // ==================== 药物效果配置 ====================
                        builder.comment("药物效果设置")
                                        .push("medicines");

                        dextromethorphanMildCureChance = builder
                                        .comment("右美沙芬治愈轻型上呼吸道感染的概率")
                                        .defineInRange("dextromethorphanMildCureChance", 0.7, 0.0, 1.0);

                        dextromethorphanModerateToMildChance = builder
                                        .comment("右美沙芬将中型上呼吸道感染降为轻型的概率")
                                        .defineInRange("dextromethorphanModerateToMildChance", 0.5, 0.0, 1.0);

                        ribavirinImmediateEffect = builder
                                        .comment("利巴韦林是否具有立即治疗效果")
                                        .define("ribavirinImmediateEffect", true);

                        ibuprofenTemporaryRelief = builder
                                        .comment("布洛芬是否具有暂时缓解效果")
                                        .define("ibuprofenTemporaryRelief", true);

                        builder.pop();
                        // ==================== 胶囊冷却时间配置 ====================

                        builder.comment("胶囊药物设置")
                                        .push("capsules");

                        lamivudineCooldownSeconds = builder
                                        .comment("拉米夫定胶囊使用后的冷却时间（秒）")
                                        .defineInRange("lamivudineCooldownSeconds", 60, 1, 3600);

                        ibuprofenCooldownSeconds = builder
                                        .comment("布洛芬胶囊使用后的冷却时间（秒）")
                                        .defineInRange("ibuprofenCooldownSeconds", 60, 1, 3600);

                        dextromethorphanCooldownSeconds = builder
                                        .comment("右美沙芬胶囊使用后的冷却时间（秒）")
                                        .defineInRange("dextromethorphanCooldownSeconds", 60, 1, 3600);

                        builder.pop();

                        builder.comment("艾滋病 (HIV) 设置")
                                        .push("hiv");

                        aidsContaminatedSyringeChance = builder
                                        .comment("使用受污染的药针感染艾滋病的概率")
                                        .defineInRange("contaminatedSyringeChance", 0.3, 0.0, 1.0);

                        lamivudineAidsCureChance = builder
                                        .comment("拉米夫定胶囊治愈艾滋病的概率")
                                        .defineInRange("lamivudineCureChance", 0.1, 0.0, 1.0);

                        ptsdSlownessLevel = builder
                                        .comment("缓慢效果等级")
                                        .defineInRange("slownessLevel", 1, 0, 4);

                        builder.pop();

                        // ==================== PTSD 配置 ====================
                        builder.comment("创伤后应激障碍 (PTSD) 设置")
                                        .push("ptsd");

                        ptsdTriggerChance = builder
                                        .comment("受到攻击时触发 PTSD 的概率")
                                        .defineInRange("triggerChance", 0.5, 0.0, 1.0);

                        ptsdLowHpThreshold = builder
                                        .comment("低血量阈值（血量百分比低于此值时会触发更强烈的恐惧效果）")
                                        .defineInRange("lowHpThreshold", 0.3, 0.0, 1.0);

                        ptsdSleepReliefFactor = builder
                                        .comment("睡眠时 PTSD 恢复速度加成倍数")
                                        .defineInRange("sleepReliefFactor", 2.0, 1.0, 10.0);

                        sedativePtsdReliefFactor = builder
                                        .comment("镇静剂对 PTSD 的缓解效果系数")
                                        .defineInRange("sedativeReliefFactor", 0.5, 0.0, 1.0);

                        builder.pop();

                        // ==================== 脓毒症配置 ====================
                        builder.comment("脓毒症 (Sepsis) 设置")
                                        .push("sepsis");

                        sepsisContaminatedSyringeChance = builder
                                        .comment("使用受污染的药针感染脓毒症的概率")
                                        .defineInRange("contaminatedSyringeChance", 0.2, 0.0, 1.0);

                        broadSpectrumAntibioticsSepsisCureChance = builder
                                        .comment("广谱抗生素治愈脓毒症的概率")
                                        .defineInRange("broadSpectrumAntibioticsCureChance", 0.3, 0.0, 1.0);

                        sepsisPoisonChance = builder
                                        .comment("脓毒症引发中毒的概率")
                                        .defineInRange("poisonChance", 0.1, 0.0, 1.0);

                        sepsisActionExtraSugarCost = builder
                                        .comment("行动时额外消耗的糖类")
                                        .defineInRange("actionExtraSugarCost", 0.5, 0.0, 5.0);

                        sepsisActionExtraFatCost = builder
                                        .comment("行动时额外消耗的油脂")
                                        .defineInRange("actionExtraFatCost", 0.3, 0.0, 5.0);

                        builder.pop();

                        // ==================== 尸毒感染配置 ====================
                        builder.comment("尸毒感染 (Zombie Virus) 设置")
                                        .push("zombie_virus");

                        corpsePoisonUndeadAttackChance = builder
                                        .comment("被亡灵生物攻击时感染尸毒的概率")
                                        .defineInRange("undeadAttackChance", 0.15, 0.0, 1.0);

                        corpseTransformationDays = builder
                                        .comment("尸毒转化为尸变所需的天数")
                                        .defineInRange("transformationDays", 7, 1, 30);

                        corpseTransformationDailyDeterioration = builder
                                        .comment("尸变后每天身体部位恶化的程度")
                                        .defineInRange("dailyDeterioration", 0.1, 0.0, 1.0);

                        deathOnAllPartsLost = builder
                                        .comment("所有身体部位都失去功能后是否死亡")
                                        .define("deathOnAllPartsLost", true);

                        corpsePoisonCustomEntities = builder
                                        .comment("可传播尸毒的自定义实体类型（格式：modid:entity_id）")
                                        .defineList("customEntities", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains(":"));

                        targetedAgentDeteriorationReduction = builder
                                        .comment("靶向剂缓解尸变恶化的效果系数")
                                        .defineInRange("targetedAgentDeteriorationReduction", 0.3, 0.0, 1.0);

                        blockingAgentDurationSeconds = builder
                                        .comment("阻断剂注射液效果持续时间（秒）")
                                        .defineInRange("blockingAgentDurationSeconds", 600, 10, 3600);

                        builder.pop();

                        builder.comment("口服剂设置")
                                        .push("oral_medicines");

                        capsuleEffectDelaySeconds = builder
                                        .comment("口服剂使用后的冷却时间（秒）")
                                        .defineInRange("effectDelaySeconds", 5, 1, 3600);

                        builder.pop();

                        // 在 Common 构造函数中添加配置定义（在胶囊冷却时间配置附近）
                        builder.comment("注射器设置")
                                        .push("syringes");

                        ribavirinCooldownSeconds = builder
                                        .comment("药针使用后的冷却时间（秒）")
                                        .defineInRange("ribavirinCooldownSeconds", 5, 1, 3600);

                        builder.pop();

                        // ==================== 食物营养映射配置 ====================
                        builder.comment("食物营养映射配置")
                                        .push("food_nutrition");

                        foodNutritionWater = builder
                                        .comment("食物水分含量配置（格式：item_id=value）")
                                        .defineList("water", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionSugar = builder
                                        .comment("食物糖分含量配置（格式：item_id=value）")
                                        .defineList("sugar", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionFat = builder
                                        .comment("食物脂肪含量配置（格式：item_id=value）")
                                        .defineList("fat", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionProtein = builder
                                        .comment("食物蛋白质含量配置（格式：item_id=value）")
                                        .defineList("protein", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionSalt = builder
                                        .comment("食物盐分含量配置（格式：item_id=value）")
                                        .defineList("salt", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionVitamin = builder
                                        .comment("食物维生素含量配置（格式：item_id=value）")
                                        .defineList("vitamin", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        foodNutritionFiber = builder
                                        .comment("食物膳食纤维含量配置（格式：item_id=value）")
                                        .defineList("fiber", new ArrayList<>(),
                                                        o -> o instanceof String && ((String) o).contains("="));

                        builder.pop();

                        // ==================== 营养消耗配置 ====================
                        builder.comment("营养消耗配置")
                                        .push("nutrition_consumption");

                        nutritionWaterDecreaseRate = builder
                                        .comment("正常情况下水分消耗速率（每秒）")
                                        .defineInRange("waterDecreaseRate", 0.1, 0.0, 1.0);

                        nutritionSleepWaterDecreaseRate = builder
                                        .comment("睡眠时水分消耗速率（每秒）")
                                        .defineInRange("sleepWaterDecreaseRate", 0.05, 0.0, 1.0);

                        nutritionSugarDecreaseRate = builder
                                        .comment("正常情况下糖分消耗速率（每秒）")
                                        .defineInRange("sugarDecreaseRate", 0.0005, 0.0, 1.0);

                        nutritionFatDecreaseRate = builder
                                        .comment("正常情况下脂肪消耗速率（每秒）")
                                        .defineInRange("fatDecreaseRate", 0.0003, 0.0, 1.0);

                        nutritionProteinDecreaseRate = builder
                                        .comment("正常情况下蛋白质消耗速率（每秒）")
                                        .defineInRange("proteinDecreaseRate", 0.0002, 0.0, 1.0);

                        nutritionSaltDecreaseRate = builder
                                        .comment("正常情况下盐分消耗速率（每秒）")
                                        .defineInRange("saltDecreaseRate", 0.0001, 0.0, 1.0);

                        nutritionVitaminDecreaseRate = builder
                                        .comment("正常情况下维生素消耗速率（每秒）")
                                        .defineInRange("vitaminDecreaseRate", 0.0001, 0.0, 1.0);

                        nutritionFiberDecreaseRate = builder
                                        .comment("正常情况下膳食纤维消耗速率（每秒）")
                                        .defineInRange("fiberDecreaseRate", 0.0001, 0.0, 1.0);

                        waterDeficiencyBlockMetabolism = builder
                                        .comment("缺水时是否阻止新陈代谢")
                                        .define("waterDeficiencyBlockMetabolism", true);

                        waterDeficiencyPoisonIntervalTicks = builder
                                        .comment("缺水中毒间隔（刻）")
                                        .defineInRange("waterDeficiencyPoisonIntervalTicks", 100, 20, 600);

                        waterDeficiencyPoisonDamage = builder
                                        .comment("缺水中毒伤害")
                                        .defineInRange("waterDeficiencyPoisonDamage", 1.0, 0.0, 10.0);

                        waterSleepDecreaseRate = builder
                                        .comment("睡眠水分消耗（每秒）")
                                        .defineInRange("waterSleepDecreaseRate", 0.02, 0.0, 1.0);

                        builder.pop();

                        // ==================== 糖类配置 ====================
                        builder.comment("糖类配置")
                                        .push("saccharides");

                        saccharidesNormalMin = builder
                                        .comment("糖类正常值下限")
                                        .defineInRange("normalMin", 20.0, 0.0, 100.0);

                        saccharidesNormalMax = builder
                                        .comment("糖类正常值上限")
                                        .defineInRange("normalMax", 80.0, 0.0, 100.0);

                        saccharidesLowBlindnessIntervalTicks = builder
                                        .comment("低糖导致失明的间隔（刻）")
                                        .defineInRange("lowBlindnessIntervalTicks", 200, 20, 1200);

                        saccharidesHighJumpWaterCostMultiplier = builder
                                        .comment("高糖跳跃额外水分消耗倍数")
                                        .defineInRange("highJumpWaterCostMultiplier", 0.5, 0.0, 2.0);

                        builder.pop();

                        // ==================== 脂肪配置 ====================
                        builder.comment("脂肪配置")
                                        .push("fats");

                        fatsNormalMin = builder
                                        .comment("脂肪正常值下限")
                                        .defineInRange("normalMin", 20.0, 0.0, 100.0);

                        fatsNormalMax = builder
                                        .comment("脂肪正常值上限")
                                        .defineInRange("normalMax", 80.0, 0.0, 100.0);

                        fatsHighSlownessLevel = builder
                                        .comment("高脂肪缓慢效果等级")
                                        .defineInRange("highSlownessLevel", 1, 0, 4);

                        fatsLowDamageMultiplier = builder
                                        .comment("低脂肪受伤倍率")
                                        .defineInRange("lowDamageMultiplier", 1.5, 1.0, 3.0);

                        fatsHighDamageAbsorption = builder
                                        .comment("高脂肪伤害吸收比例")
                                        .defineInRange("highDamageAbsorption", 0.3, 0.0, 0.8);

                        builder.pop();

                        // ==================== 蛋白质配置 ====================
                        builder.comment("蛋白质配置")
                                        .push("protein");

                        proteinLowTearEffect = builder
                                        .comment("低蛋白是否造成撕裂效果")
                                        .define("lowTearEffect", true);

                        proteinLowNoNaturalRegeneration = builder
                                        .comment("低蛋白是否阻止自然恢复")
                                        .define("lowNoNaturalRegeneration", true);

                        proteinHighHalfHunger = builder
                                        .comment("高蛋白是否饥饿值减半")
                                        .define("highHalfHunger", true);

                        proteinHighNauseaEffect = builder
                                        .comment("高蛋白是否造成反胃")
                                        .define("highNauseaEffect", true);

                        proteinNormalMax = builder
                                        .comment("蛋白质正常值上限")
                                        .defineInRange("normalMax", 70.0, 0.0, 100.0);

                        builder.pop();

                        // ==================== 无机盐配置 ====================
                        builder.comment("无机盐配置")
                                        .push("inorganic_salt");

                        inorganicSaltNormalMin = builder
                                        .comment("无机盐正常值下限")
                                        .defineInRange("normalMin", 20.0, 0.0, 100.0);

                        inorganicSaltNormalMax = builder
                                        .comment("无机盐正常值上限")
                                        .defineInRange("normalMax", 80.0, 0.0, 100.0);

                        inorganicSaltLowBloodTestDisabled = builder
                                        .comment("低无机盐是否造成挖掘疲劳")
                                        .define("lowBloodTestDisabled", true);

                        inorganicSaltHighHungerLevel = builder
                                        .comment("高无机盐饥饿等级")
                                        .defineInRange("highHungerLevel", 1, 0, 4);

                        builder.pop();

                        // ==================== 维生素配置 ====================
                        builder.comment("维生素配置")
                                        .push("vitamin");

                        vitaminHighHalfHunger = builder
                                        .comment("高维生素是否饥饿值减半")
                                        .define("highHalfHunger", true);

                        builder.pop();

                        // ==================== 膳食纤维配置 ====================
                        builder.comment("膳食纤维配置")
                                        .push("dietary_fiber");

                        dietaryFiberNormalMax = builder
                                        .comment("膳食纤维正常值上限")
                                        .defineInRange("normalMax", 80.0, 0.0, 100.0);

                        dietaryFiberHighMalabsorption = builder
                                        .comment("高膳食纤维是否造成营养吸收不良")
                                        .define("highMalabsorption", true);

                        builder.pop();

                        // ==================== 水分配置 ====================
                        builder.comment("水分配置")
                                        .push("water");
                        waterNormalMin = builder
                                        .comment("水分正常值下限")
                                        .defineInRange("normalMin", 30.0, 0.0, 100.0);
                        vitaminNormalMax = builder
                                        .comment("维生素正常值上限")
                                        .defineInRange("normalMax", 80.0, 0.0, 100.0);

                        builder.pop(); // 退出 general
                }
        }

        public static class NutritionValues {
                public float water;
                public float sugar;
                public float fat;
                public float protein;
                public float salt;
                public float vitamin;
                public float fiber;

                public NutritionValues(float water, float sugar, float fat, float protein,
                                float salt, float vitamin,
                                float fiber) {
                        this.water = water;
                        this.sugar = sugar;
                        this.fat = fat;
                        this.protein = protein;
                        this.salt = salt;
                        this.vitamin = vitamin;
                        this.fiber = fiber;
                }

        }

        /**
         * 从多个配置列表加载食物营养映射
         */
        public static void loadFoodNutritionMap(List<? extends String> waterList,
                        List<? extends String> sugarList,
                        List<? extends String> fatList,
                        List<? extends String> proteinList,
                        List<? extends String> saltList,
                        List<? extends String> vitaminList,
                        List<? extends String> fiberList) {
                FOOD_NUTRITION_MAP.clear();

                // 加载水分
                loadNutrientMap(waterList, "water");
                // 加载糖分
                loadNutrientMap(sugarList, "sugar");
                // 加载脂肪
                loadNutrientMap(fatList, "fat");
                // 加载蛋白质
                loadNutrientMap(proteinList, "protein");
                // 加载盐分
                loadNutrientMap(saltList, "salt");
                // 加载维生素
                loadNutrientMap(vitaminList, "vitamin");
                // 加载膳食纤维
                loadNutrientMap(fiberList, "fiber");
        }

        /**
         * 加载单个营养素映射
         */
        private static void loadNutrientMap(List<? extends String> list, String nutrientType) {
                if (list == null)
                        return;

                for (String line : list) {
                        if (line == null || line.trim().isEmpty() || line.startsWith("#"))
                                continue;

                        String[] parts = line.split("=", 2);
                        if (parts.length != 2)
                                continue;

                        String itemId = parts[0].trim();
                        try {
                                float value = Float.parseFloat(parts[1].trim());

                                NutritionValues nv = FOOD_NUTRITION_MAP.getOrDefault(itemId,
                                                new NutritionValues(0f, 0f, 0f, 0f, 0f, 0f, 0f));

                                switch (nutrientType) {
                                        case "water" -> nv.water = value;
                                        case "sugar" -> nv.sugar = value;
                                        case "fat" -> nv.fat = value;
                                        case "protein" -> nv.protein = value;
                                        case "salt" -> nv.salt = value;
                                        case "vitamin" -> nv.vitamin = value;
                                        case "fiber" -> nv.fiber = value;
                                }
                                FOOD_NUTRITION_MAP.put(itemId, nv);
                        } catch (NumberFormatException e) {
                                DGH_HealthcraftMod.LOGGER.warn("无法解析食物营养配置行: " + line);
                        }
                }
        }
}
