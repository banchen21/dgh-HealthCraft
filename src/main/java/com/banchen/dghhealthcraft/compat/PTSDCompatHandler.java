package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Head;
import com.lastimp.dgh.common.enums.BodyComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PTSDCompatHandler {

    private static final Random RANDOM = new Random();

    // 存储每个玩家对特定伤害来源的恐惧程度
    private static final Map<UUID, Map<UUID, Float>> PLAYER_FEAR_MAP = new HashMap<>();

    // 定义三个不同等级的 PTSD
    // 轻度 - 黄色
    public static final ResourceLocation PTSD_MILD = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "ptsd_mild"),
            name -> createMildPtsdCondition(name));

    // 中度 - 橙色
    public static final ResourceLocation PTSD_MODERATE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "ptsd_moderate"),
            name -> createModeratePtsdCondition(name));

    // 重度 - 红色
    public static final ResourceLocation PTSD_SEVERE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "ptsd_severe"),
            name -> createSeverePtsdCondition(name));

    // 兼容旧代码的引用
    public static ResourceLocation PTSD = PTSD_MILD;

    /**
     * 创建轻度 PTSD 条件（黄色）
     */
    private static BodyCondition createMildPtsdCondition(ResourceLocation name) {
        return createPtsdCondition(name,
                0xFFFFFF55, // 黄色
                false, true, 0.0005f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建中度 PTSD 条件（橙色）
     */
    private static BodyCondition createModeratePtsdCondition(ResourceLocation name) {
        return createPtsdCondition(name,
                0xFFFFA500, // 橙色
                false, true, 0.0003f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建重度 PTSD 条件（红色）
     */
    private static BodyCondition createSeverePtsdCondition(ResourceLocation name) {
        return createPtsdCondition(name,
                0xFFFF0000, // 红色
                false, true, 0.0001f, 0.5f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 通用的创建 PTSD 条件方法
     */
    private static BodyCondition createPtsdCondition(ResourceLocation name,
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
     * 玩家受到伤害时触发 PTSD 机制
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide()) return;

        // 检查触发概率
        if (RANDOM.nextFloat() > Config.PTSD_TRIGGER_CHANCE) return;

        DamageSource source = event.getSource();
        LivingEntity attacker = null;

        if (source.getEntity() instanceof LivingEntity) {
            attacker = (LivingEntity) source.getEntity();
        }

        float damage = event.getAmount();
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = currentHealth / maxHealth;

        // 获取当前 PTSD 值
        float ptsdValue = getInfectionValue(player);

        // 根据伤害和血量增加 PTSD
        float ptsdIncrease = calculatePTSDIncrease(damage, healthPercent, ptsdValue);

        if (ptsdIncrease > 0) {
            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition == null) {
                applyInfection(player, PTSD_MILD, ptsdIncrease);
            } else {
                applyInfection(player, currentCondition, ptsdIncrease);
            }

            // 检查等级转换
            checkAndUpdateInfectionLevel(player, ptsdValue + ptsdIncrease);

            // 如果有攻击者，增加对该攻击者的恐惧
            if (attacker != null) {
                addFearForAttacker(player, attacker, damage, healthPercent);

                // 低血量时触发恐惧效果
                if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
                    applyFearEffects(player, attacker, ptsdValue + ptsdIncrease);
                }
            }
        }

        // 低血量时被攻击，触发严重应激反应
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD && damage > 2.0f) {
            triggerSevereStressResponse(player, attacker);
        }
    }

    /**
     * 计算 PTSD 增加量
     */
    private static float calculatePTSDIncrease(float damage, float healthPercent, float currentPTSD) {
        float baseIncrease = 0.0f;

        if (damage > 5.0f) {
            baseIncrease += 0.005f;
        } else if (damage > 2.0f) {
            baseIncrease += 0.002f;
        } else {
            baseIncrease += 0.001f;
        }

        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
            baseIncrease += 0.01f;
        } else if (healthPercent < 0.5f) {
            baseIncrease += 0.005f;
        }

        baseIncrease *= (1.0f + currentPTSD);
        return Math.min(baseIncrease, 0.05f);
    }

    /**
     * 检查并更新感染等级
     */
    private static void checkAndUpdateInfectionLevel(Player player, float infectionValue) {
        ResourceLocation targetCondition;
        float targetValue = infectionValue;

        if (infectionValue < 0.3f) {
            targetCondition = PTSD_MILD;
        } else if (infectionValue < 0.6f) {
            targetCondition = PTSD_MODERATE;
        } else {
            targetCondition = PTSD_SEVERE;
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
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head == null) return null;

            if (head.getBodyConditions().contains(PTSD_MILD)) return PTSD_MILD;
            if (head.getBodyConditions().contains(PTSD_MODERATE)) return PTSD_MODERATE;
            if (head.getBodyConditions().contains(PTSD_SEVERE)) return PTSD_SEVERE;
            return null;
        }, null);
    }

    /**
     * 清除所有等级的感染
     */
    private static void clearAllInfections(Player player) {
        if (!HealthCapability.has(player)) return;
        clearInfection(player, PTSD_MILD);
        clearInfection(player, PTSD_MODERATE);
        clearInfection(player, PTSD_SEVERE);
    }

    /**
     * 清除特定感染
     */
    private static void clearInfection(Player player, ResourceLocation condition) {
        if (!HealthCapability.has(player)) return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head != null && head.getBodyConditions().contains(condition)) {
                float currentValue = head.getConditionValue(condition);
                head.injury(condition, -currentValue);
            }
            return null;
        }, null);
    }

    /**
     * 添加对特定攻击者的恐惧
     */
    private static void addFearForAttacker(Player player, LivingEntity attacker, float damage, float healthPercent) {
        UUID playerId = player.getUUID();
        UUID attackerId = attacker.getUUID();

        PLAYER_FEAR_MAP.computeIfAbsent(playerId, k -> new HashMap<>());
        Map<UUID, Float> fears = PLAYER_FEAR_MAP.get(playerId);

        float fearIncrease = 0.05f;
        if (damage > 5.0f) fearIncrease += 0.1f;
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) fearIncrease += 0.15f;

        float currentFear = fears.getOrDefault(attackerId, 0.0f);
        float newFear = Math.min(currentFear + fearIncrease, 1.0f);
        fears.put(attackerId, newFear);
    }

    /**
     * 应用恐惧效果（低血量时触发）
     */
    private static void applyFearEffects(Player player, LivingEntity attacker, float ptsdValue) {
        if (attacker == null) return;

        UUID playerId = player.getUUID();
        UUID attackerId = attacker.getUUID();

        float fearLevel = PLAYER_FEAR_MAP.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(attackerId, 0.0f);

        float totalFear = Math.min(ptsdValue + fearLevel, 1.0f);

        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
            totalFear = Math.min(totalFear * 1.5f, 1.0f);
        }

        if (totalFear > 0.3f) {
            int duration = Config.PTSD_EFFECT_DURATION_TICKS;
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + (int) (totalFear * 2), 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + (int) (totalFear * 2), 4);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL + (int) (totalFear), 3);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            }

            if (totalFear > 0.8f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration / 2, 0));
            }
        }
    }

    /**
     * 触发严重应激反应
     */
    private static void triggerSevereStressResponse(Player player, LivingEntity attacker) {
        float ptsdValue = getInfectionValue(player);

        if (ptsdValue > 0.5f) {
            int duration = Config.PTSD_EFFECT_DURATION_TICKS;
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 2, 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + 2, 4);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL + 1, 3);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

            ResourceLocation currentCondition = getCurrentCondition(player);
            if (currentCondition != null) {
                applyInfection(player, currentCondition, 0.02f);
            }
        }
    }

    /**
     * 玩家交互时触发（用于检测是否遇到恐惧源）
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide()) return;

        if (event.getTarget() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getTarget();
            checkFearTrigger(player, target);
        }
    }

    /**
     * 检查是否遇到恐惧源
     */
    private static void checkFearTrigger(Player player, LivingEntity entity) {
        UUID playerId = player.getUUID();
        UUID entityId = entity.getUUID();

        float fearLevel = PLAYER_FEAR_MAP.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(entityId, 0.0f);

        if (fearLevel > 0.5f) {
            float ptsdValue = getInfectionValue(player);
            float healthPercent = player.getHealth() / player.getMaxHealth();
            float multiplier = healthPercent < Config.PTSD_LOW_HP_THRESHOLD ? 1.5f : 1.0f;

            int duration = (int) (Config.PTSD_EFFECT_DURATION_TICKS * 0.3f + fearLevel * 100 * multiplier);
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + (int) ((ptsdValue + fearLevel) * 1.5f), 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + (int) ((ptsdValue + fearLevel) * 1.5f), 4);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            if (fearLevel > 0.7f || healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
                int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL + 1, 3);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel / 2));
            }
        }
    }

    /**
     * 玩家 tick 处理
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        // 恐惧记忆自然衰减
        decayFears(player, 0.0001f);

        float ptsdValue = getInfectionValue(player);
        if (ptsdValue <= 0) return;

        // 获取当前感染等级
        ResourceLocation currentCondition = getCurrentCondition(player);

        // 自然恢复
        float recovery = calculateRecovery(player, ptsdValue);

        // 应用恢复
        if (currentCondition != null) {
            applyInfection(player, currentCondition, -recovery);
        }

        // 检查等级转换
        checkAndUpdateInfectionLevel(player, ptsdValue - recovery);

        // 低血量时的持续效果
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD && ptsdValue > 0.3f) {
            int duration = Config.PTSD_EFFECT_DURATION_TICKS / 10;
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL, 2);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL, 2);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL, 2);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel / 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel / 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel / 3));
        }

        // 根据 PTSD 程度施加症状
        applyPtsdSymptoms(player, ptsdValue);
    }

    /**
     * 计算恢复速度
     */
    private static float calculateRecovery(Player player, float ptsdValue) {
        float recovery = 0.00001f;

        if (player.isSleeping()) {
            recovery *= (1 + Config.PTSD_SLEEP_RELIEF_FACTOR);
        }

        if (hasComfortableEnvironment(player)) {
            recovery *= 1.5f;
        }

        // 严重 PTSD 恢复更慢
        if (ptsdValue > 0.6f) {
            recovery *= 0.5f;
        }

        return recovery;
    }

    /**
     * 应用 PTSD 症状
     */
    private static void applyPtsdSymptoms(Player player, float ptsdValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        // 轻度症状 (0.3以下)
        if (ptsdValue >= 0.15f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        // 中度症状 (0.3-0.6)
        if (ptsdValue >= 0.3f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0));
        }

        // 重度症状 (0.6以上)
        if (ptsdValue >= 0.6f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
        }

        // 极重度症状 (0.8以上)
        if (ptsdValue >= 0.8f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
            
            if (RANDOM.nextFloat() < 0.005f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
    }

    /**
     * 恐惧记忆自然衰减
     */
    private static void decayFears(Player player, float decayAmount) {
        UUID playerId = player.getUUID();
        Map<UUID, Float> fears = PLAYER_FEAR_MAP.get(playerId);

        if (fears != null) {
            fears.replaceAll((id, fear) -> Math.max(0, fear - decayAmount));
            fears.entrySet().removeIf(entry -> entry.getValue() <= 0.01f);
        }
    }

    /**
     * 减少恐惧记忆
     */
    private static void reduceFears(Player player, float reduction) {
        UUID playerId = player.getUUID();
        Map<UUID, Float> fears = PLAYER_FEAR_MAP.get(playerId);

        if (fears != null) {
            fears.replaceAll((id, fear) -> Math.max(0, fear - reduction));
            fears.entrySet().removeIf(entry -> entry.getValue() <= 0.01f);
        }
    }

    /**
     * 应用镇静剂缓解 PTSD
     */
    public static void applySedativeRelief(Player player) {
        float ptsdValue = getInfectionValue(player);
        if (ptsdValue <= 0) return;

        float relief = ptsdValue * Config.SEDATIVE_PTSD_RELIEF_FACTOR;
        ResourceLocation currentCondition = getCurrentCondition(player);
        if (currentCondition != null) {
            applyInfection(player, currentCondition, -relief);
        }

        reduceFears(player, Config.SEDATIVE_PTSD_RELIEF_FACTOR * 0.3f);
    }

    /**
     * 检查是否有舒适环境
     */
    private static boolean hasComfortableEnvironment(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        int lightLevel = level.getMaxLocalRawBrightness(pos);
        if (lightLevel < 7) return false;

        boolean hasShelter = level.getBlockState(pos.above()).isSolid();
        boolean nearVillage = isNearVillage(player);
        boolean nearBed = isNearBed(player);

        return hasShelter || nearVillage || nearBed;
    }

    /**
     * 检查是否在村庄附近
     */
    private static boolean isNearVillage(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        List<net.minecraft.world.entity.npc.Villager> villagers = level.getEntitiesOfClass(
                net.minecraft.world.entity.npc.Villager.class,
                player.getBoundingBox().inflate(32));

        if (!villagers.isEmpty()) return true;

        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() instanceof net.minecraft.world.level.block.BellBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查是否在床附近
     */
    private static boolean isNearBed(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (level.getBlockState(checkPos).getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取感染值
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player)) return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head == null) return 0f;

            if (head.getBodyConditions().contains(PTSD_MILD)) {
                return head.getConditionValue(PTSD_MILD);
            }
            if (head.getBodyConditions().contains(PTSD_MODERATE)) {
                return head.getConditionValue(PTSD_MODERATE);
            }
            if (head.getBodyConditions().contains(PTSD_SEVERE)) {
                return head.getConditionValue(PTSD_SEVERE);
            }
            return 0f;
        }, 0f);
    }

    /**
     * 应用感染（治疗使用负值）
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
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head != null && head.getBodyConditions().contains(condition)) {
                head.injury(condition, amount);
            }
            return null;
        }, null);
    }

    /**
     * 获取玩家对特定实体的恐惧程度
     */
    public static float getFearLevel(Player player, LivingEntity entity) {
        UUID playerId = player.getUUID();
        UUID entityId = entity.getUUID();
        return PLAYER_FEAR_MAP.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(entityId, 0.0f);
    }

    /**
     * 检查玩家是否有活跃的 PTSD
     */
    public static boolean isPTSDActive(Player player) {
        return getInfectionValue(player) > 0.01f;
    }

    /**
     * 获取 PTSD 阶段
     * 0: 未感染, 1: 轻度, 2: 中度, 3: 重度
     */
    public static int getPtsdStage(Player player) {
        float value = getInfectionValue(player);
        if (value <= 0.01f) return 0;
        if (value < 0.3f) return 1;
        if (value < 0.6f) return 2;
        return 3;
    }

    /**
     * 注册 PTSD 条件到头部
     */
    public static void register() {
        if (registered) return;
        registered = true;

        ConditionAccessor.get(PTSD_MILD);
        ConditionAccessor.get(PTSD_MODERATE);
        ConditionAccessor.get(PTSD_SEVERE);

        Head.addCondition(List.of(PTSD_MILD, PTSD_MODERATE, PTSD_SEVERE));

        ConditionAccessor.painConditions.add(PTSD_MILD);
        ConditionAccessor.painConditions.add(PTSD_MODERATE);
        ConditionAccessor.painConditions.add(PTSD_SEVERE);

        ConditionAccessor.eyeVisible.add(PTSD_MILD);
        ConditionAccessor.eyeVisible.add(PTSD_MODERATE);
        ConditionAccessor.eyeVisible.add(PTSD_SEVERE);
    }

    private static boolean registered = false;
}