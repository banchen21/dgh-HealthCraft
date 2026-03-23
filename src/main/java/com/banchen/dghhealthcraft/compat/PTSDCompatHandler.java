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

    // PTSD 条件定义
    public static final ResourceLocation PTSD = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "ptsd"),
            name -> createPtsdCondition(name));

    private static BodyCondition createPtsdCondition(ResourceLocation name) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", 0.0f);
            setField(cond, "minValue", 0.0f);
            setField(cond, "maxValue", 1.0f);
            setField(cond, "healingSpeed", 0.0005f); // 缓慢自然恢复
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
     * 玩家受到伤害时触发 PTSD 机制
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

        // 检查触发概率
        if (RANDOM.nextFloat() > Config.PTSD_TRIGGER_CHANCE)
            return;

        DamageSource source = event.getSource();
        LivingEntity attacker = null;

        // 获取伤害来源实体
        if (source.getEntity() instanceof LivingEntity) {
            attacker = (LivingEntity) source.getEntity();
        }

        float damage = event.getAmount();
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = currentHealth / maxHealth;

        // 获取当前 PTSD 值
        float ptsdValue = getPTSDValue(player);

        // 根据伤害和血量增加 PTSD
        float ptsdIncrease = calculatePTSDIncrease(damage, healthPercent, ptsdValue);

        if (ptsdIncrease > 0) {
            applyPTSDProgression(player, ptsdIncrease);

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

        // 基础伤害影响
        if (damage > 5.0f) {
            baseIncrease += 0.005f;
        } else if (damage > 2.0f) {
            baseIncrease += 0.002f;
        } else {
            baseIncrease += 0.001f;
        }

        // 低血量加成
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
            baseIncrease += 0.01f;
        } else if (healthPercent < 0.5f) {
            baseIncrease += 0.005f;
        }

        // 已有 PTSD 影响（越严重越容易加重）
        baseIncrease *= (1.0f + currentPTSD);

        // 限制最大增加量
        return Math.min(baseIncrease, 0.05f);
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
        if (damage > 5.0f)
            fearIncrease += 0.1f;
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD)
            fearIncrease += 0.15f;

        float currentFear = fears.getOrDefault(attackerId, 0.0f);
        float newFear = Math.min(currentFear + fearIncrease, 1.0f);
        fears.put(attackerId, newFear);
    }

    /**
     * 应用恐惧效果（低血量时触发）
     */
    private static void applyFearEffects(Player player, LivingEntity attacker, float ptsdValue) {
        if (attacker == null)
            return;

        UUID playerId = player.getUUID();
        UUID attackerId = attacker.getUUID();

        float fearLevel = PLAYER_FEAR_MAP.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(attackerId, 0.0f);

        // 综合 PTSD 值和特定恐惧值
        float totalFear = Math.min(ptsdValue + fearLevel, 1.0f);

        // 低血量时效果增强
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
            totalFear = Math.min(totalFear * 1.5f, 1.0f);
        }

        if (totalFear > 0.3f) {
            int duration = Config.PTSD_EFFECT_DURATION_TICKS;
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + (int) (totalFear * 2), 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + (int) (totalFear * 2), 4);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL + (int) (totalFear), 3);

            // 虚弱效果
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));

            // 挖掘疲劳效果
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));

            // 缓慢效果（低血量时触发）
            if (healthPercent < Config.PTSD_LOW_HP_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            }

            // 严重恐惧时附加恶心
            if (totalFear > 0.8f) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration / 2, 0));
            }

            // 发送恐惧提示
            if (player.tickCount % 100 == 0) {
                // TODO 发送恐惧提示
            }
        }
    }

    /**
     * 触发严重应激反应
     */
    private static void triggerSevereStressResponse(Player player, LivingEntity attacker) {
        float ptsdValue = getPTSDValue(player);

        if (ptsdValue > 0.5f) {
            // 严重应激导致更强烈的负面效果
            int duration = Config.PTSD_EFFECT_DURATION_TICKS;
            int weaknessLevel = Math.min(Config.PTSD_WEAKNESS_LEVEL + 2, 4);
            int fatigueLevel = Math.min(Config.PTSD_MINING_FATIGUE_LEVEL + 2, 4);
            int slownessLevel = Math.min(Config.PTSD_SLOWNESS_LEVEL + 1, 3);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueLevel));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

            // 增加 PTSD 值
            applyPTSDProgression(player, 0.02f);

        }
    }

    /**
     * 玩家交互时触发（用于检测是否遇到恐惧源）
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide())
            return;

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
            float ptsdValue = getPTSDValue(player);

            // 低血量时遇到恐惧源效果更强
            float healthPercent = player.getHealth() / player.getMaxHealth();
            float multiplier = healthPercent < Config.PTSD_LOW_HP_THRESHOLD ? 1.5f : 1.0f;

            // 遇到恐惧源时触发效果
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
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        // 恐惧记忆自然衰减（每 tick 衰减 0.0001，约每分钟衰减 0.12）
        decayFears(player, 0.0001f);

        // 检查玩家是否在睡觉（缓解 PTSD）
        if (player.isSleeping()) {
            handleSleepHealing(player);
        }

        // 自然恢复 PTSD（基于时间）
        float ptsdValue = getPTSDValue(player);
        if (ptsdValue > 0) {
            // 缓慢自然恢复
            float recovery = 0.00001f;

            // 睡眠加速恢复（使用配置的睡眠缓解系数）
            if (player.isSleeping()) {
                recovery *= (1 + Config.PTSD_SLEEP_RELIEF_FACTOR);
            }

            // 有舒适环境加速恢复
            if (hasComfortableEnvironment(player)) {
                recovery *= 1.5f;
            }

            applyPTSDHealing(player, recovery);
        }

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
     * 睡眠治疗 PTSD
     */
    private static void handleSleepHealing(Player player) {
        // 每 100 tick（5秒）检查一次
        if (player.tickCount % 100 == 0) {
            float ptsdValue = getPTSDValue(player);

            if (ptsdValue > 0) {
                // 睡眠时恢复 PTSD（使用配置的睡眠缓解系数）
                float sleepHealing = 0.005f * (1 + Config.PTSD_SLEEP_RELIEF_FACTOR);
                applyPTSDHealing(player, sleepHealing);

                // 减少恐惧记忆
                reduceFears(player, Config.PTSD_SLEEP_RELIEF_FACTOR * 0.2f);

                // TODO 发送睡眠缓解提示
                if (ptsdValue > 0.1f && player.tickCount % 400 == 0) {

                }
            }
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
        float ptsdValue = getPTSDValue(player);
        if (ptsdValue <= 0)
            return;

        float relief = ptsdValue * Config.SEDATIVE_PTSD_RELIEF_FACTOR;
        applyPTSDHealing(player, relief);

        // 减少恐惧记忆
        reduceFears(player, Config.SEDATIVE_PTSD_RELIEF_FACTOR * 0.3f);

        // TODO 发送镇静剂缓解提示
    }

    /**
     * 检查是否有舒适环境
     */
    private static boolean hasComfortableEnvironment(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // 检查光照等级（避免黑暗）
        int lightLevel = level.getMaxLocalRawBrightness(pos);
        if (lightLevel < 7)
            return false;

        // 检查是否有庇护所（头顶有方块）
        boolean hasShelter = level.getBlockState(pos.above()).isSolid();

        // 检查是否在村庄附近
        boolean nearVillage = isNearVillage(player);

        // 检查是否有床附近
        boolean nearBed = isNearBed(player);

        return hasShelter || nearVillage || nearBed;
    }

    /**
     * 检查是否在村庄附近
     */
    private static boolean isNearVillage(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // 检查周围 32 格内是否有村民
        List<net.minecraft.world.entity.npc.Villager> villagers = level.getEntitiesOfClass(
                net.minecraft.world.entity.npc.Villager.class,
                player.getBoundingBox().inflate(32));

        if (!villagers.isEmpty()) {
            return true;
        }

        // 检查周围是否有钟
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

        // 检查周围 8 格内是否有床
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
     * 获取 PTSD 值
     */
    private static float getPTSDValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head == null || !head.getBodyConditions().contains(PTSD))
                return 0f;
            return head.getConditionValue(PTSD);
        }, 0f);
    }

    /**
     * 应用 PTSD 进展
     */
    private static void applyPTSDProgression(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head != null && head.getBodyConditions().contains(PTSD)) {
                head.injury(PTSD, amount);
            }
            return null;
        }, null);
    }

    /**
     * 应用 PTSD 恢复
     */
    private static void applyPTSDHealing(Player player, float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            AbstractBody head = health.getComponent(BodyComponents.HEAD);
            if (head != null && head.getBodyConditions().contains(PTSD)) {
                head.injury(PTSD, -amount);
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
        return getPTSDValue(player) > 0.05f;
    }

    /**
     * 注册 PTSD 条件到头部
     */
    public static void register() {
        ConditionAccessor.get(PTSD);
        Head.addCondition(List.of(PTSD));
        ConditionAccessor.eyeVisible.add(PTSD);
    }
}