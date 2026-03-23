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

    // 脓毒症（Sepsis）
    public static final ResourceLocation SEPSIS = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "sepsis"),
            name -> createSepsisCondition(name));

    private static BodyCondition createSepsisCondition(ResourceLocation name) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", 0.0f);
            setField(cond, "minValue", 0.0f);
            setField(cond, "maxValue", 1.0f);
            setField(cond, "healingSpeed", 0.0f); // 无法自愈
            setField(cond, "healingTS", 0.5f);
            setField(cond, "isInjury", true);
            setField(cond, "isPain", true);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

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

        // 原 mod 广谱抗生素直接治疗脓毒症
        ItemStack used = player.getItemInHand(event.getHand());
        if (!used.isEmpty() && used.getItem() == ModItems.ANTIBIOTICS.get()) {
            // 立即尝试治愈脓毒症
            if (isSepsisActive(player)) {
                cureSepsis(player);
            }
            return;
        }

        // 检查是否使用药针（此处需要根据实际物品判断）
        // 如果使用污染药针，触发感染
        if (isContaminatedSyringe(used, player.level())) {
            if (RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
                applySepsisInfection(player, 0.05f);
                // TODO 发送感染提示
            }
        }
    }

    /**
     * 玩家受到伤害时触发（脓毒症加重）
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        float sepsisValue = getSepsisValue(player);
        if (sepsisValue <= 0)
            return;

        // 受到伤害时脓毒症加重
        float damage = event.getAmount();
        float progression = damage * 0.001f;
        applySepsisProgression(player, progression);
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

        float sepsisValue = getSepsisValue(player);
        if (sepsisValue <= 0)
            return;

        // 脓毒症恶化（无法自愈）
        float progression = 0.00005f;

        // 低蛋白加速恶化
        double protein = NutritionCompatHandler.getProtein(player);
        if (protein < 20.0f) {
            progression *= Config.LOW_PROTEIN_DISEASE_BOOST;
        }

        // 艾滋病加速恶化
        if (HIVCompatHandler.isHIVActive(player)) {
            progression *= Config.AIDS_DISEASE_BOOST;
        }

        applySepsisProgression(player, progression);

        // 行动时额外消耗糖类和油脂
        if (player.isSprinting() || player.isSwimming() || player.isFallFlying()) {
            consumeExtraNutrients(player);
        }

        // 定期中毒效果
        if (player.tickCount % 100 == 0 && RANDOM.nextFloat() < Config.SEPSIS_POISON_CHANCE) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }

        // 根据感染程度施加症状
        applySepsisSymptoms(player, sepsisValue);
    }

    /**
     * 检查是否为污染药针
     */
    private static boolean isContaminatedSyringe(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level) {
        if (!Config.SYRINGE_CONTAMINATION_ENABLED) return false;
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
    private static void consumeExtraNutrients(Player player) {
        double sugar = NutritionCompatHandler.getSugar(player);
        double fat = NutritionCompatHandler.getFat(player);

        float extraSugarCost = Config.SEPSIS_ACTION_EXTRA_SUGAR_COST;
        float extraFatCost = Config.SEPSIS_ACTION_EXTRA_FAT_COST;

        // 消耗额外的糖类
        if (sugar > 0.5f) {
            NutritionCompatHandler.addSugar(player, -0.5 * extraSugarCost);
        }

        // 消耗额外的油脂
        if (fat > 0.5f) {
            NutritionCompatHandler.addFat(player, -0.3 * extraFatCost);
        }
    }

    /**
     * 应用脓毒症症状
     */
    private static void applySepsisSymptoms(Player player, float sepsisValue) {
        int duration = 200; // 10秒

        if (sepsisValue >= 0.3f) {
            // 中度症状：虚弱、挖掘疲劳
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0));

            // 偶尔发烧效果
            if (RANDOM.nextFloat() < 0.01f) {
                player.setSecondsOnFire(2);
            }
        }

        if (sepsisValue >= 0.6f) {
            // 重度症状：更严重的虚弱和挖掘疲劳
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));

            // 周期性眩晕
            if (RANDOM.nextFloat() < 0.02f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }

        if (sepsisValue >= 0.8f) {
            // 极重度症状：严重虚弱、失明
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));

            // 周期性失明
            if (RANDOM.nextFloat() < 0.01f) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }

            // 持续伤害
            if (player.tickCount % 40 == 0) {
                player.hurt(player.damageSources().generic(), 1.0f);
            }
        }
    }

    /**
     * 获取脓毒症值
     */
    private static float getSepsisValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(SEPSIS))
                return 0f;
            return blood.getConditionValue(SEPSIS);
        }, 0f);
    }

    /**
     * 应用脓毒症感染
     */
    private static void applySepsisInfection(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(SEPSIS)) {
                blood.injury(SEPSIS, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用脓毒症进展
     */
    private static void applySepsisProgression(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(SEPSIS)) {
                blood.injury(SEPSIS, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用脓毒症治疗（外部调用）
     */
    public static void applySepsisHealing(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(SEPSIS)) {
                blood.injury(SEPSIS, -amount);
            }
            return null;
        }, null);
    }

    /**
     * 治疗脓毒症（使用广谱抗生素）
     */
    public static void cureSepsis(Player player) {
        if (!isSepsisActive(player))
            return;

        float cureChance = Config.BROAD_SPECTRUM_ANTIBIOTICS_SEPSIS_CURE_CHANCE;
        if (RANDOM.nextFloat() < cureChance) {
            applySepsisHealing(player, 1.0f);
        } else {
            // 治疗失败，减少部分脓毒症值
            applySepsisHealing(player, 0.3f);
        }
    }

    /**
     * 检查是否感染脓毒症
     */
    public static boolean isSepsisActive(Player player) {
        return getSepsisValue(player) > 0.01f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 轻度, 2: 中度, 3: 重度, 4: 极重度
     */
    public static int getSepsisStage(Player player) {
        float value = getSepsisValue(player);
        if (value <= 0.01f)
            return 0;
        if (value < 0.3f)
            return 1;
        if (value < 0.6f)
            return 2;
        if (value < 0.8f)
            return 3;
        return 4;
    }

    /**
     * 注册
     */
    public static void register() {
        ConditionAccessor.get(SEPSIS);
        Blood.addCondition(List.of(SEPSIS));
        ConditionAccessor.bloodConditions.add(SEPSIS);

        // HealthScanner 不包含 bloodConditions，故这里补上常规扫描集
        ConditionAccessor.injuryConditions.add(SEPSIS);
        ConditionAccessor.painConditions.add(SEPSIS);
        ConditionAccessor.eyeVisible.add(SEPSIS);

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