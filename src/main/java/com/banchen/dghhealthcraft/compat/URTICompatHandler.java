package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Head;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Torso;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.network.chat.Component;

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

    // 上呼吸道感染（Upper respiratory tract infection）
    public static final ResourceLocation URTI = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "urti"),
            name -> createUrtiCondition(name));

    private static BodyCondition createUrtiCondition(ResourceLocation name) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", 0.0f);
            setField(cond, "minValue", 0.0f);
            setField(cond, "maxValue", 1.0f);
            setField(cond, "healingSpeed", 0.0f); // 无法自愈
            setField(cond, "healingTS", 0.0f);
            setField(cond, "isInjury", true);
            setField(cond, "isPain", false);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

            ConditionAccessor.injuryConditions.add(name);
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
            chance = Config.UPPER_RESPIRATORY_INFECTION_LEFT_CLICK_CHANCE;
        } else if (isRightClick) {
            chance = Config.UPPER_RESPIRATORY_INFECTION_RIGHT_CLICK_CHANCE;
        }

        // 雨天加成
        chance *= Config.UPPER_RESPIRATORY_INFECTION_RAIN_CHANCE;

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
            applyInfection(player, URTI, 0.03f);
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

        // 根据感染阶段计算进展速度
        float progression = 0.0f;

        // 轻型（0-0.4）到中型（0.4-0.7）的进展
        if (infectionValue < 0.4f) {
            // 轻型到中型需要配置的天数
            int days = Config.UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS;
            progression = 0.4f / (days * 24000f); // 每天 24000 tick
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

        applyInfection(player, URTI, progression);

        // 检查睡眠自愈（仅限轻型和中型）
        if (player.isSleeping() && infectionValue < 0.7f) {
            handleSleepHealing(player, infectionValue);
        }

        // 根据感染程度施加症状
        applyURTISymptoms(player, infectionValue);
    }

    /**
     * 处理睡眠自愈
     */
    private static void handleSleepHealing(Player player, float infectionValue) {
        // 重型无法自愈
        if (infectionValue >= 0.7f && Config.UPPER_RESPIRATORY_INFECTION_SEVERE_CANNOT_HEAL) {
            return;
        }

        // 每 100 tick（5秒）检查一次自愈
        if (player.tickCount % 100 == 0) {
            float healChance = Config.UPPER_RESPIRATORY_INFECTION_SLEEP_HEAL_CHANCE;

            if (RANDOM.nextFloat() < healChance) {
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
                    applyInfection(player, URTI, -healAmount);

                }
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
     * 获取感染值
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player)) {
            return 0f;
        }
        return HealthCapability.getAndApply(player, health -> {
            float maxValue = 0f;
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(URTI)) {
                    continue;
                }
                maxValue = Math.max(maxValue, body.getConditionValue(URTI));
            }
            return maxValue;
        }, 0f);
    }

    /**
     * 应用感染（治疗使用负值）
     */
    private static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            boolean modified = false;
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(condition))
                    continue;
                body.injury(condition, amount);
                modified = true;
            }
            return modified ? null : null;
        }, null);
    }

    /**
     * 治疗上呼吸道感染（外部调用，可指定治疗量）
     */
    public static void cureURTI(Player player, float amount) {
        if (!isURTIActive(player))
            return;
        applyInfection(player, URTI, -amount);
        player.displayClientMessage(
                Component.translatable("dghhealthcraft.msg.urti.healed", amount),
                true);
    }

    /**
     * 完全治愈上呼吸道感染
     */
    public static void fullyCureURTI(Player player) {
        if (!isURTIActive(player))
            return;
        float infectionValue = getInfectionValue(player);
        applyInfection(player, URTI, -infectionValue);
        player.displayClientMessage(
                Component.translatable("dghhealthcraft.msg.urti.fully_cured"),
                true);
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
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.urti.cured_by_dextromethorphan"),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.urti.cure_failed"),
                        true);
            }
        } else if (infectionValue < 0.7f) {
            // 中型，有概率降为轻型
            if (RANDOM.nextFloat() < Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE) {
                float reduction = infectionValue - 0.3f;
                applyInfection(player, URTI, -reduction);
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.urti.reduced_to_mild"),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.urti.no_effect"),
                        true);
            }
        } else {
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.urti.too_severe_for_dextromethorphan"),
                    true);
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
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.ribavirin_disabled"),
                    true);
            return;
        }

        float infectionValue = getInfectionValue(player);
        if (infectionValue >= 0.4f) {
            // 重型或中型降为轻型（0.3f 是轻型的最大值）
            float targetValue = 0.3f;
            float reduction = infectionValue - targetValue;
            if (reduction > 0) {
                applyInfection(player, URTI, -reduction);
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.urti.reduced_by_ribavirin"),
                        true);
            }
        } else if (infectionValue > 0) {
            // 轻型可以直接治愈
            fullyCureURTI(player);
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.urti.cured_by_ribavirin"),
                    true);
        }
    }

    /**
     * 暂时缓解（使用布洛芬）
     */
    public static void temporaryRelief(Player player) {
        if (!isURTIActive(player))
            return;

        if (!Config.IBUPROFEN_TEMPORARY_RELIEF) {
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.ibuprofen_disabled"),
                    true);
            return;
        }

        float infectionValue = getInfectionValue(player);
        // 暂时将重型、中型降为轻型
        if (infectionValue >= 0.4f) {
            float reduction = infectionValue - 0.3f;
            applyInfection(player, URTI, -reduction);
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.urti.temporary_relief"),
                    true);
        } else {
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.urti.already_mild"),
                    true);
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
        ConditionAccessor.get(URTI);
        Head.addCondition(List.of(URTI));
        Torso.addCondition(List.of(URTI));
        ConditionAccessor.injuryConditions.add(URTI);
    }
}