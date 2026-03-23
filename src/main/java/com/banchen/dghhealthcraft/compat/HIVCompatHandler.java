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

    public static final ResourceLocation HIV = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "hiv"),
            name -> createHivCondition(name));

    private static BodyCondition createHivCondition(ResourceLocation name) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", 0.0f);
            setField(cond, "minValue", 0.0f);
            setField(cond, "maxValue", 1.0f);
            setField(cond, "healingSpeed", 0.0f); // HIV 无法自愈
            setField(cond, "healingTS", 0.5f);
            setField(cond, "isInjury", false);
            setField(cond, "isPain", true);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

            ConditionAccessor.painConditions.add(name);
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
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        // 检查是否是主手交互
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty())
            return;

        Item syringe = ForgeRegistries.ITEMS.getValue(DRUG_SYRINGE);
        if (syringe == null || stack.getItem() != syringe)
            return;

        // 检查药针是否污染
        boolean isContaminated = isSyringeContaminated(stack);
        if (!isContaminated) return;

        // 使用污染药针有概率感染 HIV
        if (RANDOM.nextFloat() < Config.AIDS_CONTAMINATED_SYRINGE_CHANCE) {
            applyInfection(player, HIV, 0.05f);
        }
        
        // 同时有概率感染脓毒症
        if (RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
            try {
                applyInfection(player, SepsisCompatHandler.SEPSIS, 0.07f);
            } catch (NoClassDefFoundError e) {
                Logger.getLogger(HIVCompatHandler.class.getName())
                        .warning("SepsisCompatHandler not found, skipping sepsis infection");
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

        // 获取 HIV 感染值
        float hivValue = getHIVValue(player);
        if (hivValue <= 0f)
            return;

        // HIV 进展速度（每天约 0.001-0.002，每 tick 约 0.000005-0.00001）
        float progress = 0.000005f;
        if (hivValue > 0.7f) {
            progress = 0.00001f; // 晚期进展更快
        } else if (hivValue > 0.4f) {
            progress = 0.0000075f;
        }

        // 低蛋白加速进展
        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            progress *= Config.LOW_PROTEIN_DISEASE_BOOST;
        }

        applyHIVProgression(player, progress);

        // 根据 HIV 感染程度施加效果
        applyHIVSymptoms(player, hivValue);
    }

    /**
     * 检查药针是否污染
     */
    private static boolean isSyringeContaminated(ItemStack stack) {
        if (!Config.SYRINGE_CONTAMINATION_ENABLED) return false;
        
        // 检查药针是否有污染标记
        if (stack.hasTag() && stack.getTag().contains("Contaminated")) {
            return stack.getTag().getBoolean("Contaminated");
        }
        
        // 如果没有标记，随机决定是否污染（模拟真实场景）
        return RANDOM.nextFloat() < 0.3f;
    }

    /**
     * 应用 HIV 症状
     */
    private static void applyHIVSymptoms(Player player, float hivValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;
        
        if (hivValue >= 0.3f) {
            // 免疫力下降，更容易感染其他疾病
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
        }
        
        if (hivValue >= 0.6f) {
            // 中度症状
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 1, 3);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL, 2);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));
            
            // 偶尔发烧
            if (RANDOM.nextFloat() < 0.01f) {
                player.setSecondsOnFire(2);
            }
        }
        
        if (hivValue >= 0.8f) {
            // 严重症状（艾滋病期）
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 2, 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + 1, 3);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL, 2);
            
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
            
            // 周期性眩晕
            if (RANDOM.nextFloat() < 0.02f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
            
            // 严重时持续伤害
            if (hivValue > 0.95f && player.tickCount % 100 == 0) {
                player.hurt(player.damageSources().generic(), 1.0f);
            }
        }
    }

    /**
     * 获取玩家的 HIV 感染值
     */
    private static float getHIVValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(HIV))
                return 0f;
            return blood.getConditionValue(HIV);
        }, 0f);
    }

    /**
     * 应用 HIV 进展
     */
    private static void applyHIVProgression(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(HIV)) {
                blood.injury(HIV, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用 HIV 治疗
     */
    public static void applyHIVHealing(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(HIV)) {
                blood.injury(HIV, -amount);
            }
            return null;
        }, null);
    }

    /**
     * 治疗 HIV（使用拉米夫定）
     */
    public static void cureHIV(Player player) {
        if (!isHIVActive(player)) return;
        
        float cureChance = Config.LAMIVUDINE_AIDS_CURE_CHANCE;
        if (RANDOM.nextFloat() < cureChance) {
            applyHIVHealing(player, 1.0f);
        } else {
            // 治疗失败，减少部分 HIV 值
            applyHIVHealing(player, 0.2f);
        }
    }

    /**
     * 应用感染（通用方法，支持任意条件）
     */
    private static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            // 优先在血液中查找条件
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(condition)) {
                blood.injury(condition, amount);
                return null;
            }
            // 如果血液中没有，遍历所有身体部位
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(condition))
                    continue;
                body.injury(condition, amount);
                return null;
            }
            return null;
        }, null);
    }

    /**
     * 检查玩家是否感染 HIV
     */
    public static boolean isHIVActive(Player player) {
        return getHIVValue(player) > 0.001f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 潜伏期, 2: 中度, 3: 重度, 4: 艾滋病期
     */
    public static int getHIVStage(Player player) {
        float value = getHIVValue(player);
        if (value <= 0.001f) return 0;
        if (value < 0.3f) return 1;
        if (value < 0.6f) return 2;
        if (value < 0.8f) return 3;
        return 4;
    }

    /**
     * 注册 HIV 条件到身体部位
     */
    public static void register() {
        ConditionAccessor.get(HIV);
        Blood.addCondition(List.of(HIV));
        ConditionAccessor.bloodConditions.add(HIV);

        // HealthScanner 不引用 bloodConditions，因此也要加入可识别集合
        ConditionAccessor.painConditions.add(HIV);
        ConditionAccessor.injuryConditions.add(HIV);
        ConditionAccessor.eyeVisible.add(HIV);

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
}