package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.banchen.dghhealthcraft.nutrition.NutritionCompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Blood;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HIVCompatHandler {
    private static final Random RANDOM = new Random();

    // HIV 只有一个等级 - 艾滋病期（红色）
    public static final ResourceLocation HIV_AIDS = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "hiv_aids"),
            name -> createHivCondition(name));

    /**
     * 创建 HIV 条件（红色）
     */
    private static BodyCondition createHivCondition(ResourceLocation name) {
        return createHivCondition(name,
                0xFFFF0000, // 红色
                true, // isInjury
                true, // isPain
                0.0f, // healingSpeed (不自愈)
                0.0f, // healingTS
                0.0f, // minValue
                1.0f, // maxValue
                0.0f // defaultValue
        );
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
     * 玩家交互时触发（使用污染药针感染 HIV）
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        ItemStack stack = player.getItemInHand(event.getHand());
        if (stack.isEmpty())
            return;

        // 检查是否使用受污染的药针
        if (stack.getItem() == DghHModItems.CONTAMINATED_SYRINGE.get()) {
            // 以配置的概率感染 HIV
            if (RANDOM.nextFloat() < Config.AIDS_CONTAMINATED_SYRINGE_CHANCE) {
                applyInfection(player, HIV_AIDS, 0.1f); // 初始感染值为 0.1
            }
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

        float hivValue = getInfectionValue(player);
        if (hivValue <= 0f)
            return;

        // 计算进展速度
        float progression = calculateProgression(hivValue, player);

        // 应用进展
        applyInfection(player, HIV_AIDS, progression);

        // 确保感染值不超过最大值
        float newValue = getInfectionValue(player);
        if (newValue > 1.0f) {
            float excess = newValue - 1.0f;
            applyInfection(player, HIV_AIDS, -excess);
        }

        // 根据 HIV 感染程度施加效果
        applyHIVSymptoms(player, getInfectionValue(player));

        // 检查低蛋白感染艾滋病
        checkLowProteinInfection(player);
    }

    /**
     * 计算 HIV 进展速度
     */
    private static float calculateProgression(float currentValue, Player player) {
        float progression;

        // 感染初期 (0-0.3) 进展较慢
        if (currentValue < 0.3f) {
            progression = 0.000003f;
        }
        // 感染中期 (0.3-0.6) 进展中等
        else if (currentValue < 0.6f) {
            progression = 0.000007f;
        }
        // 感染晚期 (0.6-1.0) 进展加快
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
     * 检查低蛋白感染艾滋病
     */
    private static void checkLowProteinInfection(Player player) {
        // 如果已经感染，不再重复感染
        if (isHIVActive(player))
            return;

        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            // 蛋白质过低有概率感染艾滋病
            if (RANDOM.nextFloat() < Config.LOW_PROTEIN_AIDS_INFECTION_CHANCE) {
                applyInfection(player, HIV_AIDS, 0.05f);
            }
        }
    }

    /**
     * 应用 HIV 症状
     */
    private static void applyHIVSymptoms(Player player, float hivValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        // 初期症状 (0.1-0.3)
        if (hivValue >= 0.1f && hivValue < 0.3f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        // 中期症状 (0.3-0.6)
        if (hivValue >= 0.3f && hivValue < 0.6f) {
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL, 1);
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

        // 晚期症状 (0.8以上)
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

            // 严重阶段造成伤害
            if (hivValue > 0.95f && player.tickCount % 100 == 0) {
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
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null)
                return 0f;

            if (blood.getBodyConditions().contains(HIV_AIDS)) {
                return blood.getConditionValue(HIV_AIDS);
            }
            return 0f;
        }, 0f);
    }

    /**
     * 应用感染（公开方法）
     */
    public static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player))
            return;

        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(condition)) {
                blood.injury(condition, amount);
            }
            return null;
        }, null);
    }

    /**
     * 治疗 HIV（使用拉米夫定）
     * 有10%的概率完全治愈
     */
    public static void cureHIV(Player player) {
        if (!isHIVActive(player))
            return;

        float cureChance = Config.LAMIVUDINE_AIDS_CURE_CHANCE;
        if (RANDOM.nextFloat() < cureChance) {
            fullyCureHIV(player);
        } else {
            // 治疗失败，减少部分 HIV 值
            float currentValue = getInfectionValue(player);
            float reduction = Math.min(0.2f, currentValue);
            applyInfection(player, HIV_AIDS, -reduction);
        }
    }

    /**
     * 完全治愈 HIV
     */
    public static void fullyCureHIV(Player player) {
        if (!isHIVActive(player))
            return;

        float currentValue = getInfectionValue(player);
        if (currentValue > 0) {
            applyInfection(player, HIV_AIDS, -currentValue);
        }
    }

    /**
     * 检查玩家是否感染 HIV
     */
    public static boolean isHIVActive(Player player) {
        return getInfectionValue(player) > 0.01f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 初期, 2: 中期, 3: 艾滋病期
     */
    public static int getHIVStage(Player player) {
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
     * 注册 HIV 条件到身体部位
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        ConditionAccessor.get(HIV_AIDS);

        Blood.addCondition(List.of(HIV_AIDS));
        ConditionAccessor.bloodConditions.add(HIV_AIDS);
        ConditionAccessor.painConditions.add(HIV_AIDS);
        ConditionAccessor.injuryConditions.add(HIV_AIDS);
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