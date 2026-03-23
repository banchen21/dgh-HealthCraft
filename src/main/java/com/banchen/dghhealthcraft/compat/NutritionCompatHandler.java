package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.enums.BodyComponents;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NutritionCompatHandler {
    private static final Map<UUID, NutritionState> states = new WeakHashMap<>();
    private static final Random random = new Random();
    private static final String NBT_KEY = "dgh_nutrition_state";

    private record NutritionState(double water, double sugar, double fat, double protein, double salt, double vitamin,
            double fiber) {
        static NutritionState create() {
            return new NutritionState(1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }

        static NutritionState fromTag(CompoundTag tag) {
            if (tag == null || !tag.contains("water")) return create();
            return new NutritionState(
                    tag.getDouble("water"),
                    tag.getDouble("sugar"),
                    tag.getDouble("fat"),
                    tag.getDouble("protein"),
                    tag.getDouble("salt"),
                    tag.getDouble("vitamin"),
                    tag.getDouble("fiber"));
        }

        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("water", water);
            tag.putDouble("sugar", sugar);
            tag.putDouble("fat", fat);
            tag.putDouble("protein", protein);
            tag.putDouble("salt", salt);
            tag.putDouble("vitamin", vitamin);
            tag.putDouble("fiber", fiber);
            return tag;
        }

        NutritionState tick(boolean sleeping, boolean jumping) {
            // 基础水分消耗
            double waterDecrease = sleeping ? Config.NUTRITION_SLEEP_WATER_DECREASE_RATE : Config.NUTRITION_WATER_DECREASE_RATE;
            double w = this.water - waterDecrease;
            
            // 高糖跳跃额外消耗水分
            if (jumping && this.sugar > Config.SACCHARIDES_NORMAL_MAX / 100) {
                w -= 0.01 * Config.SACCHARIDES_HIGH_JUMP_WATER_COST_MULTIPLIER;
            }
            w = clamp(w);

            double s = this.sugar;
            double f = this.fat;
            double p = this.protein;
            double st = this.salt;
            double v = this.vitamin;
            double fi = this.fiber;

            // 缺水时，糖、脂、蛋白资源无法充分利用
            if (Config.WATER_DEFICIENCY_BLOCK_METABOLISM && w < Config.WATER_NORMAL_MIN / 100) {
                s -= 0.001;
                f -= 0.001;
                p -= 0.001;
            }

            s = clamp(s);
            f = clamp(f);
            p = clamp(p);
            st = clamp(st);
            v = clamp(v);
            fi = clamp(fi);

            return new NutritionState(w, s, f, p, st, v, fi);
        }

        static double clamp(double val) {
            return Math.max(0.0, Math.min(1.0, val));
        }
    }

    private static NutritionState state(Player player) {
        return states.computeIfAbsent(player.getUUID(), uuid -> loadFromPlayer(player));
    }

    private static void setState(Player player, NutritionState state) {
        states.put(player.getUUID(), state);
        saveToPlayer(player, state);
    }

    private static NutritionState loadFromPlayer(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent == null) {
            return NutritionState.create();
        }

        if (persistent.contains(NBT_KEY)) {
            return NutritionState.fromTag(persistent.getCompound(NBT_KEY));
        }

        return NutritionState.create();
    }

    private static void saveToPlayer(Player player, NutritionState state) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent == null) return;
        persistent.put(NBT_KEY, state.toTag());
    }

    // ==================== 获取营养值方法 ====================
    public static double getWater(Player player) {
        return state(player).water * 100;
    }

    public static double getSugar(Player player) {
        return state(player).sugar * 100;
    }

    public static double getFat(Player player) {
        return state(player).fat * 100;
    }

    public static double getProtein(Player player) {
        return state(player).protein * 100;
    }

    public static double getSalt(Player player) {
        return state(player).salt * 100;
    }

    public static double getVitamin(Player player) {
        return state(player).vitamin * 100;
    }

    public static double getFiber(Player player) {
        return state(player).fiber * 100;
    }

    // ==================== 修改营养值方法 ====================
    public static void addWater(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                NutritionState.clamp(old.water + amount / 100),
                old.sugar,
                old.fat,
                old.protein,
                old.salt,
                old.vitamin,
                old.fiber);
        setState(player, updated);
    }

    public static void addSugar(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                NutritionState.clamp(old.sugar + amount / 100),
                old.fat,
                old.protein,
                old.salt,
                old.vitamin,
                old.fiber);
        setState(player, updated);
    }

    public static void addFat(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                old.sugar,
                NutritionState.clamp(old.fat + amount / 100),
                old.protein,
                old.salt,
                old.vitamin,
                old.fiber);
        setState(player, updated);
    }

    public static void addProtein(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                old.sugar,
                old.fat,
                NutritionState.clamp(old.protein + amount / 100),
                old.salt,
                old.vitamin,
                old.fiber);
        setState(player, updated);
    }

    public static void addSalt(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                old.sugar,
                old.fat,
                old.protein,
                NutritionState.clamp(old.salt + amount / 100),
                old.vitamin,
                old.fiber);
        setState(player, updated);
    }

    public static void addVitamin(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                old.sugar,
                old.fat,
                old.protein,
                old.salt,
                NutritionState.clamp(old.vitamin + amount / 100),
                old.fiber);
        setState(player, updated);
    }

    public static void addFiber(Player player, double amount) {
        NutritionState old = state(player);
        NutritionState updated = new NutritionState(
                old.water,
                old.sugar,
                old.fat,
                old.protein,
                old.salt,
                old.vitamin,
                NutritionState.clamp(old.fiber + amount / 100));
        setState(player, updated);
    }

    public static void consumeProtein(Player player, double amount) {
        addProtein(player, -amount);
    }

    // ==================== 事件处理 ====================
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        NutritionState old = state(player);
        boolean sleeping = player.isSleeping();
        NutritionState updated = old.tick(sleeping, false);
        setState(player, updated);

        // 水分配置效果
        double water = updated.water * 100;
        if (water < Config.WATER_NORMAL_MIN) {
            if (player.tickCount % Config.WATER_DEFICIENCY_POISON_INTERVAL_TICKS == 0) {
                player.hurt(player.damageSources().magic(), Config.WATER_DEFICIENCY_POISON_DAMAGE);
            }
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0));
        }

        // 糖类配置效果
        double sugar = updated.sugar * 100;
        if (sugar < Config.SACCHARIDES_NORMAL_MIN) {
            if (player.tickCount % (int)Config.SACCHARIDES_LOW_BLINDNESS_INTERVAL_TICKS == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
        }

        // 油脂配置效果
        double fat = updated.fat * 100;
        if (fat < Config.FATS_NORMAL_MIN) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
        } else if (fat > Config.FATS_NORMAL_MAX) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, Config.FATS_HIGH_SLOWNESS_LEVEL));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 0));
        }

        // 蛋白质配置效果
        double protein = updated.protein * 100;
        if (protein < Config.PROTEIN_NORMAL_MIN) {
            if (Config.PROTEIN_LOW_TEAR_EFFECT) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0));
            }
            if (Config.PROTEIN_LOW_NO_NATURAL_REGENERATION) {
                // 阻止自然恢复通过持续伤害模拟
                if (player.tickCount % 100 == 0 && player.getHealth() < player.getMaxHealth()) {
                    player.hurt(player.damageSources().starve(), 0.5f);
                }
            }
        } else if (protein > Config.PROTEIN_NORMAL_MAX) {
            if (Config.PROTEIN_HIGH_NAUSEA_EFFECT) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
            }
        }

        // 无机盐配置效果
        double salt = updated.salt * 100;
        if (salt < Config.INORGANIC_SALT_NORMAL_MIN) {
            if (Config.INORGANIC_SALT_LOW_BLOOD_TEST_DISABLED) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 0));
            }
        } else if (salt > Config.INORGANIC_SALT_NORMAL_MAX) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, Config.INORGANIC_SALT_HIGH_HUNGER_LEVEL));
        }

        // 维生素配置效果
        double vitamin = updated.vitamin * 100;
        if (vitamin < Config.VITAMIN_NORMAL_MIN) {
            // 低维生素增加感染风险
            if (random.nextDouble() < 0.0005) {
                applyCondition(player, URTICompatHandler.URTI, 0.01f);
            }
        } else if (vitamin > Config.VITAMIN_NORMAL_MAX) {
            if (Config.VITAMIN_HIGH_HALF_HUNGER) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
            }
        }

        // 膳食纤维配置效果
        double fiber = updated.fiber * 100;
        if (fiber < Config.DIETARY_FIBER_NORMAL_MIN) {
            // 膳食纤维不足增加致病风险
            if (random.nextDouble() < 0.0005) {
                applyCondition(player, URTICompatHandler.URTI, 0.01f);
            }
        } else if (fiber > Config.DIETARY_FIBER_NORMAL_MAX) {
            if (Config.DIETARY_FIBER_HIGH_MALABSORPTION) {
                // 营养吸收不良，随机消耗营养
                if (random.nextDouble() < 0.01) {
                    addProtein(player, -0.5);
                    addSugar(player, -0.5);
                    addFat(player, -0.5);
                }
            }
        }

        // 低蛋白仅产生营养性症状，不再直接触发 HIV/脓毒症
        if (protein < Config.PROTEIN_NORMAL_MIN && random.nextDouble() < 0.0005) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
        }

        // 脓毒症相关衰竭
        if (HealthCapability.has(player)) {
            float sepsis = HealthCapability.getAndApply(player, 
                health -> health.getComponent(BodyComponents.BLOOD).getConditionValue(SepsisCompatHandler.SEPSIS), 0f);
            if (sepsis > 0.01f) {
                // 行动时额外消耗糖类和油脂
                if (player.isSprinting() || player.isSwimming() || player.isFallFlying()) {
                    double sugarCost = 0.01 * sepsis * Config.SEPSIS_ACTION_EXTRA_SUGAR_COST;
                    double fatCost = 0.005 * sepsis * Config.SEPSIS_ACTION_EXTRA_FAT_COST;
                    addSugar(player, -sugarCost);
                    addFat(player, -fatCost);
                }
                
                // 中毒效果
                if (random.nextDouble() < sepsis * Config.SEPSIS_POISON_CHANCE) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
                }
            }
        }

        // 缺水导致无法分解营养
        if (water < Config.WATER_NORMAL_MIN && Config.WATER_DEFICIENCY_BLOCK_METABOLISM) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1));
        }
    }

    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;
        Player player = (Player) entity;
        NutritionState st = state(player);
        // 睡眠导致水分下降
        double waterDecrease = Config.WATER_SLEEP_DECREASE_RATE / 100;
        st = new NutritionState(
                Math.max(0.0, st.water - waterDecrease / 100),
                st.sugar, st.fat, st.protein, st.salt, st.vitamin, st.fiber);
        setState(player, st);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;
        Player player = (Player) entity;
        NutritionState st = state(player);
        double sugar = st.sugar * 100;
        if (sugar > Config.SACCHARIDES_NORMAL_MAX) {
            double waterCost = 0.01 * Config.SACCHARIDES_HIGH_JUMP_WATER_COST_MULTIPLIER;
            st = new NutritionState(
                    NutritionState.clamp(st.water - waterCost / 100),
                    st.sugar, st.fat, st.protein, st.salt, st.vitamin, st.fiber);
            setState(player, st);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        NutritionState st = state(player);
        double protein = st.protein * 100;
        double fat = st.fat * 100;

        // 低蛋白撕裂效果
        if (protein < Config.PROTEIN_NORMAL_MIN && Config.PROTEIN_LOW_TEAR_EFFECT) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
        }

        // 低油脂增加受伤倍率
        if (fat < Config.FATS_NORMAL_MIN) {
            event.setAmount(event.getAmount() * Config.FATS_LOW_DAMAGE_MULTIPLIER);
        }
        
        // 高油脂吸收部分伤害
        if (fat > Config.FATS_NORMAL_MAX) {
            float absorbedDamage = event.getAmount() * Config.FATS_HIGH_DAMAGE_ABSORPTION;
            event.setAmount(event.getAmount() - absorbedDamage);
        }
    }

    @SubscribeEvent
    public static void onPlayerUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        // 食物营养值配置
        applyFoodNutrition(player, event.getItem());

        NutritionState st = state(player);
        double protein = st.protein * 100;
        double vitamin = st.vitamin * 100;

        // 蛋白质过高时进食饥饿值减半
        if (protein > Config.PROTEIN_NORMAL_MAX && Config.PROTEIN_HIGH_HALF_HUNGER) {
            player.getFoodData().addExhaustion(2.0f);
        }
        
        // 维生素过高时进食饥饿值减半
        if (vitamin > Config.VITAMIN_NORMAL_MAX && Config.VITAMIN_HIGH_HALF_HUNGER) {
            player.getFoodData().addExhaustion(2.0f);
        }
        
        // 蛋白质过高时反胃
        if (protein > Config.PROTEIN_NORMAL_MAX && Config.PROTEIN_HIGH_NAUSEA_EFFECT) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            // 消耗少量蛋白质
            addProtein(player, -2.0);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        if (original == null) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        if (event.isWasDeath()) {
            NutritionState defaultState = NutritionState.create();
            states.put(player.getUUID(), defaultState);
            saveToPlayer(player, defaultState);
        } else {
            NutritionState originalState = state(original);
            states.put(player.getUUID(), originalState);
            saveToPlayer(player, originalState);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        states.remove(player.getUUID());
    }

    private static void applyFoodNutrition(Player player, net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        String key = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        Config.NutritionValues values = Config.FOOD_NUTRITION_MAP.get(key);
        if (values == null)
            return;

        if (values.water != 0) addWater(player, values.water);
        if (values.sugar != 0) addSugar(player, values.sugar);
        if (values.fat != 0) addFat(player, values.fat);
        if (values.protein != 0) addProtein(player, values.protein);
        if (values.salt != 0) addSalt(player, values.salt);
        if (values.vitamin != 0) addVitamin(player, values.vitamin);
        if (values.fiber != 0) addFiber(player, values.fiber);

        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "dghhealthcraft.msg.nutrition_from_food", key), true);
    }

    private static void applyCondition(Player player, net.minecraft.resources.ResourceLocation condition,
            float amount) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                var body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(condition))
                    continue;
                body.injury(condition, amount);
                return null;
            }
            return null;
        }, null);
    }
}