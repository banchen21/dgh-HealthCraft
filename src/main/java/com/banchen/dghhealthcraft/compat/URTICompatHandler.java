package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.banchen.dghhealthcraft.nutrition.NutritionCompatHandler;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Head;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Torso;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class URTICompatHandler {
    private static final Random RANDOM = new Random();

    // 定义三个不同等级的上呼吸道感染
    // 轻型 - 黄色（-256 是黄色）
    public static final ResourceLocation URTI_MILD = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "urti_mild"),
            name -> createMildUrtiCondition(name)); // 轻型专用创建方法

    // 中型 - 橙色（自定义橙色）
    public static final ResourceLocation URTI_MODERATE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "urti_moderate"),
            name -> createModerateUrtiCondition(name)); // 中型专用创建方法

    // 重型 - 红色（-65536 是红色）
    public static final ResourceLocation URTI_SEVERE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "urti_severe"),
            name -> createSevereUrtiCondition(name)); // 重型专用创建方法

    // 兼容旧代码的 URTI 引用（指向当前活动等级的病症）
    public static ResourceLocation URTI = URTI_MILD;

    /**
     * 创建轻型 URTI 条件
     */
    private static BodyCondition createMildUrtiCondition(ResourceLocation name) {
        return createUrtiCondition(name,
                0xFFFFFF55, // 黄色 (ARGB格式: A=FF, R=FF, G=FF, B=55)
                true, // isInjury
                true, // isPain
                0.0f, // healingSpeed
                0.0f, // healingTS
                0.0f, // minValue
                1.0f, // maxValue
                0.0f // defaultValue
        );
    }

    /**
     * 创建中型 URTI 条件
     */
    private static BodyCondition createModerateUrtiCondition(ResourceLocation name) {
        return createUrtiCondition(name,
                0xFFFFA500, // 橙色 (ARGB格式)
                true, // isInjury
                true, // isPain
                0.0f, // healingSpeed
                0.0f, // healingTS
                0.0f, // minValue
                1.0f, // maxValue
                0.0f // defaultValue
        );
    }

    /**
     * 创建重型 URTI 条件
     */
    private static BodyCondition createSevereUrtiCondition(ResourceLocation name) {
        return createUrtiCondition(name,
                0xFFFF0000, // 红色 (ARGB格式)
                true, // isInjury
                true, // isPain
                0.0f, // healingSpeed
                0.0f, // healingTS
                0.0f, // minValue
                1.0f, // maxValue
                0.0f // defaultValue
        );
    }

    /**
     * 通用的创建 URTI 条件的方法
     * 
     * @param name         条件名称
     * @param color        显示颜色
     * @param isInjury     是否为伤害类型
     * @param isPain       是否为疼痛类型
     * @param healingSpeed 自愈速度
     * @param healingTS    自愈时间尺度
     * @param minValue     最小值
     * @param maxValue     最大值
     * @param defaultValue 默认值
     */
    private static BodyCondition createUrtiCondition(ResourceLocation name,
            int color,
            boolean isInjury,
            boolean isPain,
            float healingSpeed,
            float healingTS,
            float minValue,
            float maxValue,
            float defaultValue) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            // 设置基础数值
            setField(cond, "defaultValue", defaultValue);
            setField(cond, "minValue", minValue);
            setField(cond, "maxValue", maxValue);
            setField(cond, "healingSpeed", healingSpeed);
            setField(cond, "healingTS", healingTS);

            // 设置条件类型
            setField(cond, "isInjury", isInjury);
            setField(cond, "isPain", isPain);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

            // 设置显示颜色
            setField(cond, "color", color);

            // 注册到全局列表
            if (isInjury) {
                ConditionAccessor.injuryConditions.add(name);
            }
            ConditionAccessor.eyeVisible.add(name);

            return cond;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = BodyCondition.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * 玩家交互时触发感染检测
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        // 检查是否已经感染，如果已感染则不重复感染
        if (isURTIActive(player))
            return;

        // 检查是否是左击或右击事件
        boolean isLeftClick = event instanceof PlayerInteractEvent.LeftClickBlock
                || event instanceof PlayerInteractEvent.LeftClickEmpty;
        boolean isRightClick = event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickItem
                || event instanceof PlayerInteractEvent.EntityInteract
                || event instanceof PlayerInteractEvent.EntityInteractSpecific;

        if (!isLeftClick && !isRightClick)
            return;

        // 检查是否下雨
        if (!player.level().isRainingAt(player.blockPosition()))
            return;

        float chance = 0.0f;

        // 根据交互类型设置基础概率
        if (isLeftClick) {
            chance = Config.URTI_LEFT_CLICK_INFECTION_CHANCE;
        } else if (isRightClick) {
            chance = Config.URTI_RIGHT_CLICK_INFECTION_CHANCE;
        }

        // 雨天加成（乘以雨天感染概率）
        chance *= Config.URTI_RAIN_INFECTION_CHANCE;

        // 低蛋白加成
        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            chance *= Config.LOW_PROTEIN_DISEASE_BOOST;
        }

        // 艾滋病加成（免疫力低下）
        if (HIVCompatHandler.isHIVActive(player)) {
            chance *= Config.AIDS_DISEASE_BOOST;
        }

        // 维生素缺乏加成
        if (hasVitaminDeficiency(player)) {
            chance *= Config.VITAMIN_LOW_INFECTION_BOOST;
        }

        // 膳食纤维缺乏加成
        if (hasDietaryFiberDeficiency(player)) {
            chance *= Config.DIETARY_FIBER_LOW_DISEASE_RISK_BOOST;
        }

        if (RANDOM.nextFloat() < chance) {
            // 初始感染为轻型
            applyInfection(player, URTI_MILD, 0.03f);
        }
    }

    /**
     * 玩家 tick 处理（病情进展）
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        float infectionValue = getInfectionValue(player);
        if (infectionValue <= 0f)
            return;

        // 获取当前感染等级
        ResourceLocation currentCondition = getCurrentCondition(player);

        // 根据感染阶段计算进展速度
        float progression = calculateProgression(infectionValue, player);

        // 应用进展（向当前等级添加）
        applyInfection(player, currentCondition, progression);

        // 检查等级转换（当感染值跨过阈值时）
        checkAndUpdateInfectionLevel(player, infectionValue + progression);

        // 重新获取感染值（可能已经转换等级）
        float newInfectionValue = getInfectionValue(player);

        // 检查睡眠自愈（仅限轻型和中型）
        if (player.isSleeping() && newInfectionValue < 0.7f) {
            handleSleepHealing(player, newInfectionValue);
        }

        // 根据感染程度施加症状
        applyURTISymptoms(player, newInfectionValue);
    }

    /**
     * 计算疾病进展速度
     */
    private static float calculateProgression(float infectionValue, Player player) {
        float progression = 0.0f;

        // 轻型（0-0.4）到中型（0.4-0.7）的进展
        if (infectionValue < 0.4f) {
            int days = Config.UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS;
            progression = 0.4f / (days * 24000f);
        }
        // 中型（0.4-0.7）到重型（0.7-1.0）的进展
        else if (infectionValue < 0.7f) {
            int days = Config.UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS;
            progression = 0.3f / (days * 24000f);
        }
        // 重型持续恶化
        else {
            progression = 0.00015f;
        }

        // 睡眠时无法消耗营养元素自我调节，恶化更快
        if (player.isSleeping()) {
            progression *= 1.5f;
        }

        // 低蛋白加速恶化
        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            progression *= Config.LOW_PROTEIN_DISEASE_BOOST;
        }

        // 艾滋病加速恶化
        if (HIVCompatHandler.isHIVActive(player)) {
            progression *= Config.AIDS_DISEASE_BOOST;
        }

        return progression;
    }

    /**
     * 检查并更新感染等级
     */
    private static void checkAndUpdateInfectionLevel(Player player, float infectionValue) {
        ResourceLocation targetCondition = null;
        float targetValue = infectionValue;

        // 根据感染值确定应该使用哪个等级
        if (infectionValue < 0.4f) {
            targetCondition = URTI_MILD;
        } else if (infectionValue < 0.7f) {
            targetCondition = URTI_MODERATE;
            targetValue = infectionValue; // 中型保持原值
        } else {
            targetCondition = URTI_SEVERE;
            targetValue = Math.min(infectionValue, 1.0f);
        }

        ResourceLocation currentCondition = getCurrentCondition(player);

        // 如果当前没有感染，直接应用目标等级
        if (currentCondition == null) {
            applyInfection(player, targetCondition, targetValue);
            return;
        }

        // 如果等级发生变化，需要迁移感染值
        if (!currentCondition.equals(targetCondition)) {
            // 获取当前感染值
            float currentValue = getInfectionValue(player);

            // 清除所有等级的感染（确保彻底清除）
            clearAllInfections(player);

            // 应用新的感染，使用当前感染值（不是 targetValue，因为 targetValue 可能已经包含了新加的 progression）
            applyInfection(player, targetCondition, currentValue);
        } else {
            // 等级相同，确保感染值不超过最大值
            float currentValue = getInfectionValue(player);
            if (currentValue > 1.0f) {
                float excess = currentValue - 1.0f;
                applyInfection(player, targetCondition, -excess);
            }
        }
    }

    /**
     * 清除所有等级的感染
     */
    private static void clearAllInfections(Player player) {
        if (!HealthCapability.has(player))
            return;

        // 清除所有三个等级的感染
        clearInfection(player, URTI_MILD);
        clearInfection(player, URTI_MODERATE);
        clearInfection(player, URTI_SEVERE);
    }

    /**
     * 获取当前活动的感染条件（确保只有一个）
     */
    private static ResourceLocation getCurrentCondition(Player player) {
        if (!HealthCapability.has(player))
            return null;

        return HealthCapability.getAndApply(player, health -> {
            ResourceLocation found = null;

            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                // 检查所有三个等级
                if (body.getBodyConditions().contains(URTI_MILD)) {
                    if (found != null && !found.equals(URTI_MILD)) {
                    }
                    found = URTI_MILD;
                }
                if (body.getBodyConditions().contains(URTI_MODERATE)) {
                    if (found != null && !found.equals(URTI_MODERATE)) {
                    }
                    found = URTI_MODERATE;
                }
                if (body.getBodyConditions().contains(URTI_SEVERE)) {
                    if (found != null && !found.equals(URTI_SEVERE)) {
                    }
                    found = URTI_SEVERE;
                }
            }

            return found;
        }, null);
    }

    /**
     * 检查玩家是否有特定条件
     */
    private static boolean hasCondition(Player player, ResourceLocation condition) {
        if (!HealthCapability.has(player))
            return false;
        return HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(condition)) {
                    return true;
                }
            }
            return false;
        }, false);
    }

    /**
     * 清除特定感染
     */
    private static void clearInfection(Player player, ResourceLocation condition) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(condition)) {
                    float currentValue = body.getConditionValue(condition);
                    body.injury(condition, -currentValue);
                }
            }
            return null;
        }, null);
    }

    /**
     * 处理睡眠自愈
     */
    private static void handleSleepHealing(Player player, float infectionValue) {
        // 检查睡眠自愈概率
        float healChance = Config.URTI_SLEEP_HEAL_CHANCE;

        // 每 100 tick（5秒）检查一次自愈
        if (player.tickCount % 100 == 0 && RANDOM.nextFloat() < healChance) {
            // 检查是否有足够的营养元素
            double protein = NutritionCompatHandler.getProtein(player);
            double sugar = NutritionCompatHandler.getSugar(player);
            double fat = NutritionCompatHandler.getFat(player);

            float nutrientCost = Config.UPPER_RESPIRATORY_INFECTION_NUTRIENT_CONSUMPTION;

            // 检查营养是否充足
            if (protein >= nutrientCost && sugar >= nutrientCost && fat >= nutrientCost) {
                // 消耗营养元素
                NutritionCompatHandler.addProtein(player, -nutrientCost);
                NutritionCompatHandler.addSugar(player, -nutrientCost);
                NutritionCompatHandler.addFat(player, -nutrientCost);

                // 治疗感染
                float healAmount = 0.1f;
                ResourceLocation currentCondition = getCurrentCondition(player);
                applyInfection(player, currentCondition, -healAmount);
            }
        }
    }

    /**
     * 应用上呼吸道感染症状
     */
    private static void applyURTISymptoms(Player player, float infectionValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        if (infectionValue >= 0.4f) {
            // 中型症状：挖掘疲劳
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            // 偶尔咳嗽（随机效果）
            if (RANDOM.nextFloat() < 0.01f) {
                player.playSound(SoundEvents.PLAYER_HURT, 0.5f, 1.0f);
            }
        }

        if (infectionValue >= 0.7f) {
            // 重型症状：更严重的挖掘疲劳
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + 1, 3);
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            // 虚弱效果
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));

            // 偶尔眩晕
            if (RANDOM.nextFloat() < 0.005f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
            }
        }
    }

    /**
     * 检查是否有维生素缺乏
     */
    private static boolean hasVitaminDeficiency(Player player) {
        double vitamin = NutritionCompatHandler.getVitamin(player);
        return vitamin < Config.VITAMIN_NORMAL_MIN;
    }

    /**
     * 检查是否有膳食纤维缺乏
     */
    private static boolean hasDietaryFiberDeficiency(Player player) {
        double fiber = NutritionCompatHandler.getFiber(player);
        return fiber < Config.DIETARY_FIBER_NORMAL_MIN;
    }

    /**
     * 获取感染值（从当前活动的感染条件中获取）
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player)) {
            return 0f;
        }
        return HealthCapability.getAndApply(player, health -> {
            float maxValue = 0f;
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                // 检查所有三个等级
                if (body.getBodyConditions().contains(URTI_MILD)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(URTI_MILD));
                }
                if (body.getBodyConditions().contains(URTI_MODERATE)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(URTI_MODERATE));
                }
                if (body.getBodyConditions().contains(URTI_SEVERE)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(URTI_SEVERE));
                }
            }
            return maxValue;
        }, 0f);
    }

    /**
     * 应用感染（治疗使用负值）
     */
    public static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player))
            return;

        // 防止在已有其他等级时添加新等级
        if (amount > 0) { // 增加感染
            ResourceLocation current = getCurrentCondition(player);
            if (current != null && !current.equals(condition)) {
                clearAllInfections(player);
            }
        }

        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(condition))
                    continue;
                body.injury(condition, amount);
            }
            return null;
        }, null);
    }

    /**
     * 治疗上呼吸道感染（外部调用，可指定治疗量）
     */
    public static void cureURTI(Player player, float amount) {
        if (!isURTIActive(player))
            return;
        ResourceLocation currentCondition = getCurrentCondition(player);
        if (currentCondition != null) {
            applyInfection(player, currentCondition, -amount);
        }
    }

    /**
     * 完全治愈上呼吸道感染
     */
    public static void fullyCureURTI(Player player) {
        if (!isURTIActive(player))
            return;

        // 清除所有三个等级
        if (hasCondition(player, URTI_MILD))
            clearInfection(player, URTI_MILD);
        if (hasCondition(player, URTI_MODERATE))
            clearInfection(player, URTI_MODERATE);
        if (hasCondition(player, URTI_SEVERE))
            clearInfection(player, URTI_SEVERE);

    }

    /**
     * 治疗轻型上呼吸道感染（使用右美沙芬）
     */
    public static void treatMildURTI(Player player) {
        if (!isURTIActive(player))
            return;

        float infectionValue = getInfectionValue(player);

        if (infectionValue < 0.4f) {
            // 轻型，直接治愈
            if (RANDOM.nextFloat() < Config.DEXTROMETHORPHAN_MILD_CURE_CHANCE) {
                fullyCureURTI(player);
            }
        } else if (infectionValue < 0.7f) {
            // 中型，有概率降为轻型
            if (RANDOM.nextFloat() < Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE) {
                float reduction = infectionValue - 0.3f;
                ResourceLocation currentCondition = getCurrentCondition(player);
                if (currentCondition != null) {
                    applyInfection(player, currentCondition, -reduction);
                }
            }
        }
    }

    /**
     * 立即治疗（使用利巴韦林）
     * 将重型、中型上呼吸道感染降为轻型
     */
    public static void immediateTreatment(Player player) {
        if (!isURTIActive(player))
            return;

        if (!Config.RIBAVIRIN_IMMEDIATE_EFFECT) {
            return;
        }

        float infectionValue = getInfectionValue(player);
        if (infectionValue >= 0.4f) {
            // 清除当前感染
            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition != null) {
                clearInfection(player, currentCondition);
            }
            // 重新应用为轻型
            applyInfection(player, URTI_MILD, 0.3f);

        } else if (infectionValue > 0) {
            // 轻型可以直接治愈
            fullyCureURTI(player);

        }
    }

    /**
     * 暂时缓解（使用布洛芬）
     */
    public static void temporaryRelief(Player player) {
        if (!isURTIActive(player))
            return;

        if (!Config.IBUPROFEN_TEMPORARY_RELIEF) {

            return;
        }

        float infectionValue = getInfectionValue(player);
        // 暂时将重型、中型降为轻型
        if (infectionValue >= 0.4f) {
            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition != null) {
                clearInfection(player, currentCondition);
            }
            applyInfection(player, URTI_MILD, 0.3f);

        }
    }

    /**
     * 检查是否感染
     */
    public static boolean isURTIActive(Player player) {
        return getInfectionValue(player) > 0.001f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 轻型, 2: 中型, 3: 重型
     */
    public static int getInfectionStage(Player player) {
        float value = getInfectionValue(player);
        if (value <= 0.001f)
            return 0;
        if (value < 0.4f)
            return 1;
        if (value < 0.7f)
            return 2;
        return 3;
    }

    /**
     * 注册
     */
    public static void register() {
        // 确保只注册一次
        if (registered)
            return;
        registered = true;

        // 注册所有三个等级
        ConditionAccessor.get(URTI_MILD);
        ConditionAccessor.get(URTI_MODERATE);
        ConditionAccessor.get(URTI_SEVERE);

        Head.addCondition(List.of(URTI_MILD, URTI_MODERATE, URTI_SEVERE));
        Torso.addCondition(List.of(URTI_MILD, URTI_MODERATE, URTI_SEVERE));
    }

    private static boolean registered = false;
}