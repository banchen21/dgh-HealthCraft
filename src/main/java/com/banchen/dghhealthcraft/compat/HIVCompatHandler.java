package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Blood;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HIVCompatHandler {
    private static final Random RANDOM = new Random();
    private static final ResourceLocation DRUG_SYRINGE = new ResourceLocation("dghhealthcraft", "drug_syringe");

    // 定义三个不同等级的 HIV
    // 潜伏期 - 黄色
    public static final ResourceLocation HIV_LATENT = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "hiv_latent"),
            name -> createLatentHivCondition(name));

    // 发病期 - 橙色
    public static final ResourceLocation HIV_ACTIVE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "hiv_active"),
            name -> createActiveHivCondition(name));

    // 艾滋病期 - 红色
    public static final ResourceLocation HIV_AIDS = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "hiv_aids"),
            name -> createAidsHivCondition(name));

    // 兼容旧代码的引用
    public static ResourceLocation HIV = HIV_LATENT;

    /**
     * 创建潜伏期 HIV 条件（黄色）
     */
    private static BodyCondition createLatentHivCondition(ResourceLocation name) {
        return createHivCondition(name,
                0xFFFFFF55, // 黄色
                false, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建发病期 HIV 条件（橙色）
     */
    private static BodyCondition createActiveHivCondition(ResourceLocation name) {
        return createHivCondition(name,
                0xFFFFA500, // 橙色
                false, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建艾滋病期 HIV 条件（红色）
     */
    private static BodyCondition createAidsHivCondition(ResourceLocation name) {
        return createHivCondition(name,
                0xFFFF0000, // 红色
                false, true, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 通用的创建 HIV 条件方法
     */
    private static BodyCondition createHivCondition(ResourceLocation name,
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
     * 玩家交互时触发（使用污染药针感染）
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide()) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;

        Item syringe = ForgeRegistries.ITEMS.getValue(DRUG_SYRINGE);
        if (syringe == null || stack.getItem() != syringe) return;

        boolean isContaminated = isSyringeContaminated(stack, player.level());

        if (isContaminated && RANDOM.nextFloat() < Config.AIDS_CONTAMINATED_SYRINGE_CHANCE) {
            applyInfection(player, HIV_LATENT, 0.03f);
        }

        if (isContaminated && RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
            try {
                applyInfection(player, SepsisCompatHandler.SEPSIS_MILD, 0.05f);
            } catch (NoClassDefFoundError e) {
                Logger.getLogger(HIVCompatHandler.class.getName())
                        .warning("SepsisCompatHandler not found, skipping sepsis infection");
            }
        }

        markSyringeContaminated(stack, player.level());
    }

    /**
     * 玩家 tick 处理
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        float hivValue = getInfectionValue(player);
        if (hivValue <= 0f) return;

        // 获取当前感染等级
        ResourceLocation currentCondition = getCurrentCondition(player);

        // 计算进展速度
        float progression = calculateProgression(hivValue, player);

        // 应用进展
        if (currentCondition != null) {
            applyInfection(player, currentCondition, progression);
        }

        // 检查等级转换
        checkAndUpdateInfectionLevel(player, hivValue + progression);

        // 重新获取感染值
        float newInfectionValue = getInfectionValue(player);

        // 根据 HIV 感染程度施加效果
        applyHIVSymptoms(player, newInfectionValue);
    }

    /**
     * 计算 HIV 进展速度
     */
    private static float calculateProgression(float currentValue, Player player) {
        float progression;

        // 潜伏期 (0-0.3) 进展较慢
        if (currentValue < 0.3f) {
            progression = 0.000003f;
        }
        // 发病期 (0.3-0.6) 进展中等
        else if (currentValue < 0.6f) {
            progression = 0.000007f;
        }
        // 艾滋病期 (0.6-1.0) 进展加快
        else {
            progression = 0.000012f;
        }

        // 低蛋白加速进展
        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            progression *= Config.LOW_PROTEIN_DISEASE_BOOST;
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
            targetCondition = HIV_LATENT;
        } else if (infectionValue < 0.6f) {
            targetCondition = HIV_ACTIVE;
        } else {
            targetCondition = HIV_AIDS;
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
        if (!HealthCapability.has(player)) return null;

        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null) return null;

            if (blood.getBodyConditions().contains(HIV_LATENT)) return HIV_LATENT;
            if (blood.getBodyConditions().contains(HIV_ACTIVE)) return HIV_ACTIVE;
            if (blood.getBodyConditions().contains(HIV_AIDS)) return HIV_AIDS;
            return null;
        }, null);
    }

    /**
     * 清除所有等级的感染
     */
    private static void clearAllInfections(Player player) {
        if (!HealthCapability.has(player)) return;
        clearInfection(player, HIV_LATENT);
        clearInfection(player, HIV_ACTIVE);
        clearInfection(player, HIV_AIDS);
    }

    /**
     * 清除特定感染
     */
    private static void clearInfection(Player player, ResourceLocation condition) {
        if (!HealthCapability.has(player)) return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(condition)) {
                float currentValue = blood.getConditionValue(condition);
                blood.injury(condition, -currentValue);
            }
            return null;
        }, null);
    }

    /**
     * 检查药针是否污染
     */
    private static boolean isSyringeContaminated(ItemStack stack, Level level) {
        if (!Config.SYRINGE_CONTAMINATION_ENABLED) return false;
        if (stack.isEmpty()) return false;

        if (stack.hasTag()) {
            if (stack.getTag().contains("Contaminated") && stack.getTag().getBoolean("Contaminated")) {
                return true;
            }

            if (stack.getTag().contains("ContaminationTimestamp")) {
                long timestamp = stack.getTag().getLong("ContaminationTimestamp");
                long duration = Config.SYRINGE_CONTAMINATION_DURATION_SECONDS * 20L;
                if (level.getGameTime() - timestamp <= duration) {
                    stack.getOrCreateTag().putBoolean("Contaminated", true);
                    return true;
                }
                stack.getOrCreateTag().remove("Contaminated");
                stack.getOrCreateTag().remove("ContaminationTimestamp");
            }
        }

        boolean initial = RANDOM.nextFloat() < 0.3f;
        if (initial) {
            markSyringeContaminated(stack, level);
        }
        return initial;
    }

    private static void markSyringeContaminated(ItemStack stack, Level level) {
        if (!Config.SYRINGE_CONTAMINATION_ENABLED || stack.isEmpty()) return;
        stack.getOrCreateTag().putBoolean("Contaminated", true);
        stack.getOrCreateTag().putLong("ContaminationTimestamp", level.getGameTime());
    }

    /**
     * 应用 HIV 症状
     */
    private static void applyHIVSymptoms(Player player, float hivValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        // 潜伏期症状 (0.3以下)
        if (hivValue >= 0.15f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        // 发病期症状 (0.3-0.6)
        if (hivValue >= 0.3f) {
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
        }

        // 艾滋病期症状 (0.6以上)
        if (hivValue >= 0.6f) {
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 1, 3);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            if (RANDOM.nextFloat() < 0.01f) {
                player.setSecondsOnFire(2);
            }
        }

        // 晚期艾滋病症状 (0.8以上)
        if (hivValue >= 0.8f) {
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 2, 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + 1, 3);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL, 2);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));

            if (RANDOM.nextFloat() < 0.02f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }

            if (hivValue > 0.95f && player.tickCount % 100 == 0) {
                player.hurt(player.damageSources().generic(), 1.0f);
            }
        }
    }

    /**
     * 获取感染值
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player)) return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null) return 0f;

            if (blood.getBodyConditions().contains(HIV_LATENT)) {
                return blood.getConditionValue(HIV_LATENT);
            }
            if (blood.getBodyConditions().contains(HIV_ACTIVE)) {
                return blood.getConditionValue(HIV_ACTIVE);
            }
            if (blood.getBodyConditions().contains(HIV_AIDS)) {
                return blood.getConditionValue(HIV_AIDS);
            }
            return 0f;
        }, 0f);
    }

    /**
     * 应用感染（通用方法，支持任意条件）
     */
    private static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player)) return;

        if (amount > 0) {
            ResourceLocation current = getCurrentCondition(player);
            if (current != null && !current.equals(condition)) {
                clearAllInfections(player);
            }
        }

        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(condition)) {
                blood.injury(condition, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用 HIV 治疗
     */
    public static void applyHIVHealing(Player player, float amount) {
        if (!isHIVActive(player)) return;
        ResourceLocation currentCondition = getCurrentCondition(player);
        if (currentCondition != null) {
            applyInfection(player, currentCondition, -amount);
        }
    }

    /**
     * 治疗 HIV（使用拉米夫定）
     */
    public static void cureHIV(Player player) {
        if (!isHIVActive(player)) return;

        float cureChance = Config.LAMIVUDINE_AIDS_CURE_CHANCE;
        if (RANDOM.nextFloat() < cureChance) {
            fullyCureHIV(player);
        } else {
            // 治疗失败，减少部分 HIV 值
            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition != null) {
                applyInfection(player, currentCondition, -0.2f);
            }
        }
    }

    /**
     * 完全治愈 HIV
     */
    public static void fullyCureHIV(Player player) {
        if (!isHIVActive(player)) return;
        clearAllInfections(player);
    }

    /**
     * 检查玩家是否感染 HIV
     */
    public static boolean isHIVActive(Player player) {
        return getInfectionValue(player) > 0.01f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 潜伏期, 2: 发病期, 3: 艾滋病期
     */
    public static int getHIVStage(Player player) {
        float value = getInfectionValue(player);
        if (value <= 0.01f) return 0;
        if (value < 0.3f) return 1;
        if (value < 0.6f) return 2;
        return 3;
    }

    /**
     * 注册 HIV 条件到身体部位
     */
    public static void register() {
        if (registered) return;
        registered = true;

        ConditionAccessor.get(HIV_LATENT);
        ConditionAccessor.get(HIV_ACTIVE);
        ConditionAccessor.get(HIV_AIDS);

        Blood.addCondition(List.of(HIV_LATENT, HIV_ACTIVE, HIV_AIDS));
        ConditionAccessor.bloodConditions.add(HIV_LATENT);
        ConditionAccessor.bloodConditions.add(HIV_ACTIVE);
        ConditionAccessor.bloodConditions.add(HIV_AIDS);

        ConditionAccessor.painConditions.add(HIV_LATENT);
        ConditionAccessor.painConditions.add(HIV_ACTIVE);
        ConditionAccessor.painConditions.add(HIV_AIDS);

        ConditionAccessor.injuryConditions.add(HIV_LATENT);
        ConditionAccessor.injuryConditions.add(HIV_ACTIVE);
        ConditionAccessor.injuryConditions.add(HIV_AIDS);

        ConditionAccessor.eyeVisible.add(HIV_LATENT);
        ConditionAccessor.eyeVisible.add(HIV_ACTIVE);
        ConditionAccessor.eyeVisible.add(HIV_AIDS);

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