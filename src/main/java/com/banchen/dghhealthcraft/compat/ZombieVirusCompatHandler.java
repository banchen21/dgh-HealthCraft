package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Blood;
import com.lastimp.dgh.common.enums.BodyComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieVirusCompatHandler {
    private static final Random RANDOM = new Random();

    // 尸毒条件定义
    public static final ResourceLocation ZOMBIE_VIRUS = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombie_virus"),
            name -> createZombieVirusCondition(name));

    // 尸变状态
    public static final ResourceLocation ZOMBIFICATION = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombification"),
            name -> createZombificationCondition(name));

    private static BodyCondition createZombieVirusCondition(ResourceLocation name) {
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
            setField(cond, "isPain", true);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

            ConditionAccessor.injuryConditions.add(name);
            ConditionAccessor.painConditions.add(name);
            ConditionAccessor.eyeVisible.add(name);
            return cond;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BodyCondition createZombificationCondition(ResourceLocation name) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", 0.0f);
            setField(cond, "minValue", 0.0f);
            setField(cond, "maxValue", 1.0f);
            setField(cond, "healingSpeed", 0.0f);
            setField(cond, "healingTS", 0.0f);
            setField(cond, "isInjury", true);
            setField(cond, "isPain", true);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);

            ConditionAccessor.injuryConditions.add(name);
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
     * 检查实体是否是可以传播尸毒的亡灵生物
     */
    private static boolean isValidZombieEntity(LivingEntity entity) {
        if (entity == null)
            return false;

        // 获取实体注册名
        ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (registryName == null)
            return false;

        String entityId = registryName.toString();

        // 检查自定义实体类型列表
        if (Config.CORPSE_POISON_CUSTOM_ENTITIES != null &&
                Config.CORPSE_POISON_CUSTOM_ENTITIES.contains(entityId)) {
            return true;
        }

        // 默认的亡灵生物类型
        return entity instanceof Zombie ||
                entity instanceof ZombieVillager ||
                entity instanceof Husk ||
                entity instanceof Drowned ||
                entity instanceof ZombifiedPiglin ||
                entity instanceof Skeleton ||
                entity instanceof Stray ||
                entity instanceof WitherSkeleton;
    }

    public static final String BLOCKER_TICKS_KEY = "dghhealthcraft:blocker_ticks";

    /**
     * 检查阻断剂是否激活
     */
    public static boolean isBlockerActive(Player player) {
        if (player == null)
            return false;
        return player.getPersistentData().getInt(BLOCKER_TICKS_KEY) > 0;
    }

    /**
     * 激活阻断剂
     * 
     * @param player 玩家
     * @param ticks  持续时间（ticks）
     */
    public static void activateBlocker(Player player, int ticks) {
        if (player == null)
            return;
        player.getPersistentData().putInt(BLOCKER_TICKS_KEY, Math.max(0, ticks));
    }

    /**
     * 更新阻断剂计时器（每 tick 调用）
     */
    private static void tickBlocker(Player player) {
        if (player == null)
            return;
        int ticks = player.getPersistentData().getInt(BLOCKER_TICKS_KEY);
        if (ticks <= 0)
            return;

        ticks = Math.max(0, ticks - 1);
        player.getPersistentData().putInt(BLOCKER_TICKS_KEY, ticks);

        // 阻断效果结束时提示
        if (ticks == 0) {
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.blocker_expired"),
                    true);
        }
    }

    /**
     * 玩家受到伤害时触发尸毒感染
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        LivingEntity attacker = null;
        if (event.getSource().getEntity() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getSource().getEntity();
        }

        // 如果阻断剂激活，免疫尸毒感染
        if (isBlockerActive(player)) {
            // 可选：显示免疫提示（避免过于频繁）
            if (player.tickCount % 100 == 0) {
                int remaining = getBlockerRemainingSeconds(player);
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.blocker_immune", remaining),
                        true);
            }
            return;
        }

        // 检查是否是亡灵生物攻击
        if (attacker != null && isValidZombieEntity(attacker)) {
            // 检查是否已经尸变
            float zombificationValue = getZombificationValue(player);
            if (zombificationValue > 0.1f) {
                return; // 已经尸变，不再重复感染
            }

            // 使用配置的感染概率
            float chance = Config.CORPSE_POISON_UNDEAD_ATTACK_CHANCE;
            if (RANDOM.nextFloat() < chance) {
                float infectionAmount = 0.05f + RANDOM.nextFloat() * 0.1f;
                applyZombieVirusInfection(player, infectionAmount);
                player.displayClientMessage(
                        Component.translatable("dghhealthcraft.msg.zombie_virus.infected"),
                        true);
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

        // 更新阻断剂计时器
        tickBlocker(player);

        float virusValue = getZombieVirusValue(player);
        float zombificationValue = getZombificationValue(player);

        // 如果已经尸变，处理尸变状态
        if (zombificationValue > 0) {
            handleZombification(player, zombificationValue);
            return;
        }

        // 处理尸毒感染
        if (virusValue > 0) {
            // 计算恶化速度（基于配置的中型到尸变天数）
            float progression = calculateProgression(virusValue);

            // 睡眠时恶化更快
            if (player.isSleeping()) {
                progression *= 1.5f;
            }

            // 低免疫力加速恶化
            if (hasCompromisedImmunity(player)) {
                progression *= Config.LOW_PROTEIN_DISEASE_BOOST;
            }

            // 艾滋病加速恶化
            if (HIVCompatHandler.isHIVActive(player)) {
                progression *= Config.AIDS_DISEASE_BOOST;
            }

            applyZombieVirusProgression(player, progression);

            // 根据感染程度施加症状
            applyVirusSymptoms(player, virusValue);

            // 检查是否达到尸变阈值
            if (virusValue >= 1.0f) {
                triggerZombification(player);
            }
        }
    }

    /**
     * 计算尸毒恶化速度
     * 基于配置的尸变天数：从感染到尸变需要 CORPSE_TRANSFORMATION_DAYS 天
     */
    private static float calculateProgression(float currentValue) {
        if (currentValue >= 1.0f) {
            return 0.0f; // 已经达到尸变上限，不再继续恶化
        }

        int transformationDays = Math.max(1, Config.CORPSE_TRANSFORMATION_DAYS);
        // 每天需要的进度
        float dailyProgress = 1.0f / transformationDays;
        // 每 tick 的基础进度
        float tickBaseProgress = dailyProgress / 24000f;

        // 还剩多少进度
        float remainingProgress = Math.max(0.0f, 1.0f - currentValue);

        // 进展随当前病情加速（越严重越快），但不会超过阈值；此处可进一步调整曲线
        float acceleration = 0.5f + currentValue * 0.5f;

        // 进度不会变为负数
        float progression = tickBaseProgress * Math.max(0.1f, acceleration) * remainingProgress;

        return progression;
    }

    /**
     * 处理尸变状态
     */
    private static void handleZombification(Player player, float zombificationValue) {
        // 每天开始（每 24000 tick = 20分钟）所有部位恶化
        if (player.tickCount % 24000 == 0) {
            float deterioration = Config.CORPSE_TRANSFORMATION_DAILY_DETERIORATION;
            deteriorateAllBodyParts(player, zombificationValue * deterioration);
        }

        // 施加尸变效果
        applyZombificationEffects(player, zombificationValue);

        // 检查是否所有部位都失去功能
        if (Config.DEATH_ON_ALL_PARTS_LOST && checkAllPartsFailed(player)) {
            player.kill();
        }
    }

    /**
     * 所有部位每天恶化
     */
    private static void deteriorateAllBodyParts(Player player, float deteriorationAmount) {
        if (deteriorationAmount <= 0f || !HealthCapability.has(player))
            return;

        HealthCapability.getAndApply(player, health -> {
            // 遍历所有身体部位并造成损伤
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                // 对所有可用的身体条件造成损伤
                for (ResourceLocation condition : body.getBodyConditions()) {
                    if (condition.equals(ZOMBIFICATION) || condition.equals(ZOMBIE_VIRUS)) {
                        continue;
                    }

                    BodyCondition cond = ConditionAccessor.get(condition);
                    if (cond == null)
                        continue;

                    // 只有伤害/疼痛条件才被尸变恶化处理，可根据需求扩展
                    if (!cond.isInjury() && !cond.isPain()) {
                        continue;
                    }

                    float currentValue = body.getConditionValue(condition);
                    if (currentValue >= cond.maxValue() - 1e-6f) {
                        continue;
                    }

                    float applyAmount = Math.min(deteriorationAmount, cond.maxValue() - currentValue);
                    if (applyAmount <= 0f) {
                        continue;
                    }

                    body.injury(condition, applyAmount);
                }
            }
            return null;
        }, null);
    }

    /**
     * 应用尸变效果
     */
    private static void applyZombificationEffects(Player player, float zombificationValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;
        int weaknessLevel = (int) (zombificationValue * 3);
        int fatigueLevel = (int) (zombificationValue * 2);
        int slownessLevel = (int) (zombificationValue * 2);

        // 尸变效果
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration,
                Math.min(weaknessLevel, 4)));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration,
                Math.min(fatigueLevel, 3)));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration,
                Math.min(slownessLevel, 3)));

        // 高级尸变效果
        if (zombificationValue > 0.5f) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
        }

        if (zombificationValue > 0.8f) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration / 2, 0));
        }

        // 对周围生物造成伤害（僵尸化）
        if (player.tickCount % 100 == 0) {
            player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(3))
                    .forEach(entity -> {
                        if (entity != player && !(entity instanceof Player)) {
                            entity.hurt(player.damageSources().mobAttack(player), 2.0f);
                        }
                    });
        }
    }

    /**
     * 检查是否所有部位都失去功能
     */
    private static boolean checkAllPartsFailed(Player player) {
        if (!HealthCapability.has(player))
            return true;

        return HealthCapability.getAndApply(player, health -> {
            int failedParts = 0;
            int totalParts = 0;

            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null) {
                    totalParts++;
                    // 检查部位是否严重受损（任意条件 > 0.8f 视为功能丧失）
                    boolean isFailed = false;
                    for (ResourceLocation condition : body.getBodyConditions()) {
                        float value = body.getConditionValue(condition);
                        if (value > 0.8f) {
                            isFailed = true;
                            break;
                        }
                    }
                    if (isFailed)
                        failedParts++;
                }
            }

            return totalParts > 0 && failedParts >= totalParts;
        }, true);
    }

    /**
     * 根据感染程度施加症状
     */
    private static void applyVirusSymptoms(Player player, float virusValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        if (virusValue > 0.2f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        if (virusValue > 0.4f) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 0));
        }

        if (virusValue > 0.6f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
        }

        // 接近尸变时出现幻觉
        if (virusValue > 0.85f) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
    }

    /**
     * 触发尸变
     */
    private static void triggerZombification(Player player) {
        float currentZombification = getZombificationValue(player);
        if (currentZombification > 0)
            return;

        // 开始尸变
        applyZombificationProgression(player, 0.1f);

        // 清除尸毒值（转化为尸变）
        applyZombieVirusHealing(player, 1.0f);

        // 播放僵尸声音
        player.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * 检查免疫力是否低下
     */
    private static boolean hasCompromisedImmunity(Player player) {
        if (!HealthCapability.has(player))
            return false;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(BodyCondition.IMMUNITY))
                return false;
            float immunityValue = blood.getConditionValue(BodyCondition.IMMUNITY);
            return immunityValue < 0.3f;
        }, false);
    }

    /**
     * 获取尸毒值
     */
    private static float getZombieVirusValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(ZOMBIE_VIRUS))
                return 0f;
            return blood.getConditionValue(ZOMBIE_VIRUS);
        }, 0f);
    }

    /**
     * 获取尸变值
     */
    private static float getZombificationValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(ZOMBIFICATION))
                return 0f;
            return blood.getConditionValue(ZOMBIFICATION);
        }, 0f);
    }

    /**
     * 应用尸毒感染
     */
    private static void applyZombieVirusInfection(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(ZOMBIE_VIRUS)) {
                blood.injury(ZOMBIE_VIRUS, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用尸毒进展
     */
    private static void applyZombieVirusProgression(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(ZOMBIE_VIRUS)) {
                blood.injury(ZOMBIE_VIRUS, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用尸毒恢复
     */
    private static void applyZombieVirusHealing(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(ZOMBIE_VIRUS)) {
                blood.injury(ZOMBIE_VIRUS, -amount);
            }
            return null;
        }, null);
    }

    /**
     * 缓解尸毒（可由靶向剂调用）
     */
    public static void alleviateZombieVirus(Player player) {
        float currentValue = getZombieVirusValue(player);
        if (currentValue <= 0)
            return;

        float reduction = Config.TARGETED_AGENT_DETERIORATION_REDUCTION;
        applyZombieVirusHealing(player, currentValue * reduction);
    }

    /**
     * 应用尸变进展
     */
    private static void applyZombificationProgression(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood != null && blood.getBodyConditions().contains(ZOMBIFICATION)) {
                blood.injury(ZOMBIFICATION, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用尸变缓解（使用靶向剂）
     */
    public static void alleviateZombification(Player player) {
        float currentValue = getZombificationValue(player);
        if (currentValue <= 0)
            return;

        float reduction = Config.TARGETED_AGENT_DETERIORATION_REDUCTION;
        applyZombificationProgression(player, -currentValue * reduction);

    }

    /**
     * 检查是否感染尸毒
     */
    public static boolean isZombieVirusActive(Player player) {
        return getZombieVirusValue(player) > 0.01f;
    }

    /**
     * 检查是否尸变
     */
    public static boolean isZombified(Player player) {
        return getZombificationValue(player) > 0.01f;
    }

    /**
     * 注册尸毒条件
     */
    public static void register() {
        ConditionAccessor.get(ZOMBIE_VIRUS);
        ConditionAccessor.get(ZOMBIFICATION);
        Blood.addCondition(List.of(ZOMBIE_VIRUS, ZOMBIFICATION));
        ConditionAccessor.bloodConditions.add(ZOMBIE_VIRUS);
        ConditionAccessor.bloodConditions.add(ZOMBIFICATION);

        // HealthScanner 不包含 bloodConditions，需要补入可扫描集合
        ConditionAccessor.injuryConditions.add(ZOMBIE_VIRUS);
        ConditionAccessor.painConditions.add(ZOMBIE_VIRUS);
        ConditionAccessor.eyeVisible.add(ZOMBIE_VIRUS);

        ConditionAccessor.injuryConditions.add(ZOMBIFICATION);
        ConditionAccessor.painConditions.add(ZOMBIFICATION);
        ConditionAccessor.eyeVisible.add(ZOMBIFICATION);

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

    /**
     * 获取阻断剂剩余时间（秒）
     */
    public static int getBlockerRemainingSeconds(Player player) {
        if (player == null)
            return 0;
        int ticks = player.getPersistentData().getInt(BLOCKER_TICKS_KEY);
        return ticks / 20;
    }

    /**
     * 获取阻断剂剩余时间（ticks）
     */
    public static int getBlockerRemainingTicks(Player player) {
        if (player == null)
            return 0;
        return player.getPersistentData().getInt(BLOCKER_TICKS_KEY);
    }
}