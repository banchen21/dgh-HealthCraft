package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Blood;
import com.lastimp.dgh.common.entry.register.ModItems;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SepsisCompatHandler {
    private static final Random RANDOM = new Random();

    // 定义三个不同等级的脓毒症
    // 轻度 - 黄色
    public static final ResourceLocation SEPSIS_MILD = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "sepsis_mild"),
            name -> createMildSepsisCondition(name));

    // 中度 - 橙色
    public static final ResourceLocation SEPSIS_MODERATE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "sepsis_moderate"),
            name -> createModerateSepsisCondition(name));

    // 重度 - 红色
    public static final ResourceLocation SEPSIS_SEVERE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "sepsis_severe"),
            name -> createSevereSepsisCondition(name));

    // 兼容旧代码的引用
    public static ResourceLocation SEPSIS = SEPSIS_MILD;

    /**
     * 创建轻度脓毒症条件（黄色）
     */
    private static BodyCondition createMildSepsisCondition(ResourceLocation name) {
        return createSepsisCondition(name,
                0xFFFFFF55, // 黄色
                true, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建中度脓毒症条件（橙色）
     */
    private static BodyCondition createModerateSepsisCondition(ResourceLocation name) {
        return createSepsisCondition(name,
                0xFFFFA500, // 橙色
                true, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建重度脓毒症条件（红色）
     */
    private static BodyCondition createSevereSepsisCondition(ResourceLocation name) {
        return createSepsisCondition(name,
                0xFFFF0000, // 红色
                true, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 通用的创建脓毒症条件方法
     */
    private static BodyCondition createSepsisCondition(ResourceLocation name,
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

            setField(cond, "defaultValue", defaultValue);
            setField(cond, "minValue", minValue);
            setField(cond, "maxValue", maxValue);
            setField(cond, "healingSpeed", healingSpeed);
            setField(cond, "healingTS", healingTS);
            setField(cond, "isInjury", isInjury);
            setField(cond, "isPain", isPain);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);
            setField(cond, "color", color);

            if (isInjury) {
                ConditionAccessor.injuryConditions.add(name);
            }
            if (isPain) {
                ConditionAccessor.painConditions.add(name);
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
     * 玩家交互时触发（用于检测使用污染药针）
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        // 使用广谱抗生素治疗脓毒症
        ItemStack used = player.getItemInHand(event.getHand());
        if (!used.isEmpty() && used.getItem() == ModItems.ANTIBIOTICS.get()) {
            if (isSepsisActive(player)) {
                boolean cured = cureSepsis(player);
                player.sendSystemMessage(Component.translatable(
                        cured ? "dghhealthcraft.msg.antibiotic_treated_sepsis"
                                : "dghhealthcraft.msg.antibiotic_failed_sepsis"));
            } else {
                player.sendSystemMessage(Component.translatable("dghhealthcraft.msg.antibiotic_no_sepsis"));
            }
            return;
        }

        // 检查是否使用污染药针
        if (isContaminatedSyringe(used, player.level())) {
            if (RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
                applyInfection(player, SEPSIS_MILD, 0.03f);
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.sepsis.infected"), true);
            }
        }
    }

    /**
     * 玩家受到伤害时触发（脓毒症加重）
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (player.level().isClientSide())
            return;

        float sepsisValue = getInfectionValue(player);
        if (sepsisValue <= 0)
            return;

        float damage = event.getAmount();
        float progression = damage * 0.001f;

        ResourceLocation currentCondition = getCurrentCondition(player);
        if (currentCondition != null) {
            applyInfection(player, currentCondition, progression);
            // 检查等级转换
            checkAndUpdateInfectionLevel(player, sepsisValue + progression);
        }
    }

    /**
     * 玩家 tick 处理
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        float sepsisValue = getInfectionValue(player);
        if (sepsisValue <= 0)
            return;

        // 获取当前感染等级
        ResourceLocation currentCondition = getCurrentCondition(player);

        // 计算恶化速度
        float progression = calculateProgression(sepsisValue, player);

        // 应用进展
        applyInfection(player, currentCondition, progression);

        // 检查等级转换
        checkAndUpdateInfectionLevel(player, sepsisValue + progression);

        // 重新获取感染值
        float newInfectionValue = getInfectionValue(player);

        // 行动时额外消耗糖类和油脂
        if (player.isSprinting() || player.isSwimming() || player.isFallFlying()) {
            consumeExtraNutrients(player, newInfectionValue);
        }

        // 定期中毒效果
        if (player.tickCount % 100 == 0 && RANDOM.nextFloat() < Config.SEPSIS_POISON_CHANCE) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }

        // 根据感染程度施加症状
        applySepsisSymptoms(player, newInfectionValue);
    }

    /**
     * 计算脓毒症恶化速度
     */
    private static float calculateProgression(float currentValue, Player player) {
        float progression;

        // 轻度（0-0.3）到中度（0.3-0.6）的进展
        if (currentValue < 0.3f) {
            progression = 0.00003f;
        }
        // 中度（0.3-0.6）到重度（0.6-1.0）的进展
        else if (currentValue < 0.6f) {
            progression = 0.00005f;
        }
        // 重度持续恶化
        else {
            progression = 0.00008f;
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
        ResourceLocation targetCondition;
        float targetValue = infectionValue;

        if (infectionValue < 0.3f) {
            targetCondition = SEPSIS_MILD;
        } else if (infectionValue < 0.6f) {
            targetCondition = SEPSIS_MODERATE;
        } else {
            targetCondition = SEPSIS_SEVERE;
            targetValue = Math.min(infectionValue, 1.0f);
        }

        ResourceLocation currentCondition = getCurrentCondition(player);

        if (currentCondition == null) {
            applyInfection(player, targetCondition, targetValue);
            return;
        }

        if (!currentCondition.equals(targetCondition)) {
            float currentValue = getInfectionValue(player);
            clearAllInfections(player);
            applyInfection(player, targetCondition, currentValue);
        } else {
            float currentValue = getInfectionValue(player);
            if (currentValue > 1.0f) {
                float excess = currentValue - 1.0f;
                applyInfection(player, targetCondition, -excess);
            }
        }
    }

    /**
     * 获取当前活动的感染条件
     */
    private static ResourceLocation getCurrentCondition(Player player) {
        if (!HealthCapability.has(player))
            return null;

        return HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                if (body.getBodyConditions().contains(SEPSIS_MILD))
                    return SEPSIS_MILD;
                if (body.getBodyConditions().contains(SEPSIS_MODERATE))
                    return SEPSIS_MODERATE;
                if (body.getBodyConditions().contains(SEPSIS_SEVERE))
                    return SEPSIS_SEVERE;
            }
            return null;
        }, null);
    }

    /**
     * 清除所有等级的感染
     */
    private static void clearAllInfections(Player player) {
        if (!HealthCapability.has(player))
            return;
        clearInfection(player, SEPSIS_MILD);
        clearInfection(player, SEPSIS_MODERATE);
        clearInfection(player, SEPSIS_SEVERE);
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
     * 检查是否为污染药针
     */
    private static boolean isContaminatedSyringe(ItemStack stack, net.minecraft.world.level.Level level) {
        if (!Config.SYRINGE_CONTAMINATION_ENABLED)
            return false;
        if (stack.isEmpty())
            return false;

        if (stack.hasTag() && stack.getTag().contains("Contaminated") && stack.getTag().getBoolean("Contaminated")) {
            return true;
        }

        if (stack.hasTag() && stack.getTag().contains("ContaminationTimestamp")) {
            long timestamp = stack.getTag().getLong("ContaminationTimestamp");
            long duration = Config.SYRINGE_CONTAMINATION_DURATION_SECONDS * 20L;
            if (level.getGameTime() - timestamp <= duration) {
                stack.getOrCreateTag().putBoolean("Contaminated", true);
                return true;
            }
            stack.getOrCreateTag().remove("Contaminated");
            stack.getOrCreateTag().remove("ContaminationTimestamp");
        }

        return false;
    }

    /**
     * 行动时额外消耗营养
     */
    private static void consumeExtraNutrients(Player player, float sepsisValue) {
        double sugar = NutritionCompatHandler.getSugar(player);
        double fat = NutritionCompatHandler.getFat(player);

        float extraSugarCost = Config.SEPSIS_ACTION_EXTRA_SUGAR_COST;
        float extraFatCost = Config.SEPSIS_ACTION_EXTRA_FAT_COST;

        // 根据脓毒症严重程度增加消耗
        float multiplier = 1.0f + sepsisValue;

        if (sugar > 0.5f) {
            NutritionCompatHandler.addSugar(player, -0.5 * extraSugarCost * multiplier);
        }
        if (fat > 0.5f) {
            NutritionCompatHandler.addFat(player, -0.3 * extraFatCost * multiplier);
        }
    }

    /**
     * 应用脓毒症症状
     */
    private static void applySepsisSymptoms(Player player, float sepsisValue) {
        int duration = 200; // 10秒

        // 轻度症状 (0.3以下)
        if (sepsisValue >= 0.15f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        // 中度症状 (0.3-0.6)
        if (sepsisValue >= 0.3f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0));

            if (RANDOM.nextFloat() < 0.01f) {
                player.setSecondsOnFire(2);
            }
        }

        // 重度症状 (0.6以上)
        if (sepsisValue >= 0.6f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));

            if (RANDOM.nextFloat() < 0.02f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }

        // 极重度症状 (0.8以上)
        if (sepsisValue >= 0.8f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));

            if (RANDOM.nextFloat() < 0.01f) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }

            if (player.tickCount % 40 == 0) {
                player.hurt(player.damageSources().generic(), 1.0f);
            }
        }
    }

    /**
     * 获取感染值
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            float maxValue = 0f;
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                if (body.getBodyConditions().contains(SEPSIS_MILD)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(SEPSIS_MILD));
                }
                if (body.getBodyConditions().contains(SEPSIS_MODERATE)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(SEPSIS_MODERATE));
                }
                if (body.getBodyConditions().contains(SEPSIS_SEVERE)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(SEPSIS_SEVERE));
                }
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

        if (amount > 0) {
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
     * 治疗脓毒症（使用广谱抗生素）
     * 
     * @return 是否完全治愈
     */
    public static boolean cureSepsis(Player player) {
        if (!isSepsisActive(player))
            return false;

        float cureChance = Config.BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE;
        if (RANDOM.nextFloat() < cureChance) {
            fullyCureSepsis(player);
            return true;
        } else {
            // 治疗失败，减少部分脓毒症值
            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition != null) {
                applyInfection(player, currentCondition, -0.3f);
            }
            return false;
        }
    }

    /**
     * 完全治愈脓毒症
     */
    public static void fullyCureSepsis(Player player) {
        if (!isSepsisActive(player))
            return;
        clearAllInfections(player);
    }

    /**
     * 检查是否感染脓毒症
     */
    public static boolean isSepsisActive(Player player) {
        return getInfectionValue(player) > 0.01f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 轻度, 2: 中度, 3: 重度
     */
    public static int getSepsisStage(Player player) {
        float value = getInfectionValue(player);
        if (value <= 0.01f)
            return 0;
        if (value < 0.3f)
            return 1;
        if (value < 0.6f)
            return 2;
        return 3;
    }

    /**
     * 注册
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        ConditionAccessor.get(SEPSIS_MILD);
        ConditionAccessor.get(SEPSIS_MODERATE);
        ConditionAccessor.get(SEPSIS_SEVERE);

        Blood.addCondition(List.of(SEPSIS_MILD, SEPSIS_MODERATE, SEPSIS_SEVERE));
        ConditionAccessor.bloodConditions.add(SEPSIS_MILD);
        ConditionAccessor.bloodConditions.add(SEPSIS_MODERATE);
        ConditionAccessor.bloodConditions.add(SEPSIS_SEVERE);

        ConditionAccessor.injuryConditions.add(SEPSIS_MILD);
        ConditionAccessor.injuryConditions.add(SEPSIS_MODERATE);
        ConditionAccessor.injuryConditions.add(SEPSIS_SEVERE);

        ConditionAccessor.painConditions.add(SEPSIS_MILD);
        ConditionAccessor.painConditions.add(SEPSIS_MODERATE);
        ConditionAccessor.painConditions.add(SEPSIS_SEVERE);

        ConditionAccessor.eyeVisible.add(SEPSIS_MILD);
        ConditionAccessor.eyeVisible.add(SEPSIS_MODERATE);
        ConditionAccessor.eyeVisible.add(SEPSIS_SEVERE);

        refreshBloodConditionsCache();
    }

    private static void refreshBloodConditionsCache() {
        try {
            var field = Blood.class.getDeclaredField("BLOOD_CONDITIONS");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean registered = false;
}