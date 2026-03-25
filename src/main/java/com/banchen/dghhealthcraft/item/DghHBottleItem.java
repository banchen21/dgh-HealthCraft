package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import com.banchen.dghhealthcraft.compat.PTSDCompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * 胶囊瓶
 * 用于存放各种药物胶囊的瓶子
 */
public class DghHBottleItem extends Item {

    public DghHBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 客户端直接返回成功
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        // 检查是否为有效的胶囊瓶
        if (isValidBottle(stack)) {
            applyBottleEffect(player, stack);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        
        return InteractionResultHolder.pass(stack);
    }

    /**
     * 检查是否为有效的胶囊瓶
     */
    private boolean isValidBottle(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        return item == DghHModItems.LAMIVUDINE_BOTTLE.get()
                || item == DghHModItems.DEXTROMETHORPHAN_BOTTLE.get()
                || item == DghHModItems.IBUPROFEN_BOTTLE.get();
    }

    /**
     * 应用胶囊瓶效果
     * 根据不同的瓶子类型应用不同的效果
     */
    private void applyBottleEffect(Player player, ItemStack stack) {
        // 获取冷却时间（使用阻断剂的持续时间，因为这是对抗尸毒的药物）
        int durationTicks = Config.BLOCKING_AGENT_DURATION_SECONDS * 20;
        
        // 根据瓶子类型应用具体效果
        Item bottleType = stack.getItem();
        
        if (bottleType == DghHModItems.LAMIVUDINE_BOTTLE.get()) {
            // 拉米夫定瓶 - 用于治疗艾滋病
            applyLamivudineEffect(player);
        } else if (bottleType == DghHModItems.DEXTROMETHORPHAN_BOTTLE.get()) {
            // 右美沙芬瓶 - 用于治疗上呼吸道感染
            applyDextromethorphanEffect(player);
        } else if (bottleType == DghHModItems.IBUPROFEN_BOTTLE.get()) {
            // 布洛芬瓶 - 用于暂时缓解症状
            applyIbuprofenEffect(player);
        }
        
        // 消耗瓶子耐久度
        damageBottle(player, stack);
        
        // 添加冷却时间
        player.getCooldowns().addCooldown(this, durationTicks);
    }
    
    /**
     * 应用拉米夫定效果（治疗艾滋病）
     * 有 Config.LAMIVUDINE_AIDS_CURE_CHANCE 的概率完全治愈艾滋病
     */
    private void applyLamivudineEffect(Player player) {
        if (!HIVCompatHandler.isHIVActive(player)) {
            return;
        }
        
        // 调用 HIV 治疗
        HIVCompatHandler.cureHIV(player);
        
    }
    
    /**
     * 应用右美沙芬效果（治疗上呼吸道感染）
     * 轻型有概率治愈，中型有概率降为轻型
     */
    private void applyDextromethorphanEffect(Player player) {
        if (!URTICompatHandler.isURTIActive(player)) {
            return;
        }
        
        int stage = URTICompatHandler.getInfectionStage(player);
        
        if (stage == 1) {
            // 轻型，尝试治愈
            float chance = Config.DEXTROMETHORPHAN_MILD_CURE_CHANCE;
            if (player.getRandom().nextFloat() < chance) {
                URTICompatHandler.fullyCureURTI(player);
            }
        } else if (stage == 2) {
            // 中型，尝试降为轻型
            float chance = Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE;
            if (player.getRandom().nextFloat() < chance) {
                URTICompatHandler.treatMildURTI(player);
            }
        } else if (stage == 3) {
            // 重型，治疗效果较差
            URTICompatHandler.cureURTI(player, 0.1f);
        }
    }
    
    /**
     * 应用布洛芬效果（暂时缓解症状）
     * 暂时移除 PTSD 相关的负面效果
     */
    private void applyIbuprofenEffect(Player player) {
        // 检查是否有 PTSD
        if (PTSDCompatHandler.isPTSDActive(player)) {
            // 应用镇静剂效果缓解 PTSD
            PTSDCompatHandler.applySedativeRelief(player);
        } else {
            // 没有 PTSD 时，暂时清除一些负面效果
            player.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
            player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
            player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        }
        
        // 检查是否有上呼吸道感染症状，如果有也暂时缓解
        if (URTICompatHandler.isURTIActive(player)) {
            float infectionValue = URTICompatHandler.getInfectionValue(player);
            if (infectionValue >= 0.4f) {
                // 暂时移除症状效果
                player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
                if (infectionValue >= 0.7f) {
                    player.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
                }
            }
        }
        
        // 检查布洛芬暂时缓解效果配置
        if (Config.IBUPROFEN_TEMPORARY_RELIEF) {
            // 可以添加一个临时的抗性效果
            int duration = Config.PTSD_EFFECT_DURATION_TICKS / 2;
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, 0));
        }
    }

    /**
     * 减少耐久度并返回
     * @param player 玩家
     * @param stack 物品堆
     * @return 耐久度减少后的物品堆
     */
    public static ItemStack damageBottle(Player player, ItemStack stack) {
        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
        return stack;
    }
    
    /**
     * 获取瓶子对应的冷却时间（秒）
     * @param bottleType 瓶子类型
     * @return 冷却时间（秒）
     */
    public static int getCooldownSeconds(Item bottleType) {
        return Config.BLOCKING_AGENT_DURATION_SECONDS;
    }
}