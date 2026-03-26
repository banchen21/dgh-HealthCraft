package com.banchen.dghhealthcraft.item;

import java.util.List;

import javax.annotation.Nullable;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 胶囊
 * 支持三种类型：拉米夫定胶囊、右美沙芬胶囊、布洛芬胶囊
 */
public class CapsuleItem extends Item {

    public CapsuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 客户端直接返回成功
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        // 检查是否为有效的胶囊
        Integer cooldownTicks = getCooldownForCapsule(stack);
        if (cooldownTicks != null) {
            applyCapsuleEffect(player, stack, cooldownTicks);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.pass(stack);
    }

    private Integer getCooldownForCapsule(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        Item item = stack.getItem();

        if (item == DghHModItems.LAMIVUDINE_CAPSULE.get()) {
            return (int) (Config.LAMIVUDINE_COOLDOWN_SECONDS * 20);
        } else if (item == DghHModItems.DEXTROMETHORPHAN_CAPSULE.get()) {
            return (int) (Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS * 20);
        } else if (item == DghHModItems.IBUPROFEN_CAPSULE.get()) {
            return (int) (Config.IBUPROFEN_COOLDOWN_SECONDS * 20);
        }

        return null;
    }

    /**
     * 应用胶囊效果
     * 
     * @param player        玩家
     * @param stack         物品堆
     * @param cooldownTicks 冷却时间（刻）
     */
    private void applyCapsuleEffect(Player player, ItemStack stack, int cooldownTicks) {
        // 消耗一个胶囊
        consumeOne(player, stack);

        // 添加冷却时间
        player.getCooldowns().addCooldown(this, cooldownTicks);

        // 根据胶囊类型应用不同的治疗效果
        applyTherapeuticEffect(player, stack.getItem());
    }

    /**
     * 根据胶囊类型应用治疗效果
     * 
     * @param player      玩家
     * @param capsuleItem 胶囊物品
     */
    private void applyTherapeuticEffect(Player player, Item capsuleItem) {
        if (capsuleItem == DghHModItems.LAMIVUDINE_CAPSULE.get()) {
            // 拉米夫定胶囊：治疗艾滋病
            HIVCompatHandler.cureHIV(player);
        } else if (capsuleItem == DghHModItems.DEXTROMETHORPHAN_CAPSULE.get()) {
            // 右美沙芬胶囊：治疗轻型上呼吸道感染
            URTICompatHandler.treatMildURTI(player);
        } else if (capsuleItem == DghHModItems.IBUPROFEN_CAPSULE.get()) {
            // 布洛芬胶囊：暂时缓解症状
            URTICompatHandler.temporaryRelief(player);
        }
    }

    /**
     * 消耗一个物品，创造模式不消耗
     * 
     * @param player 玩家
     * @param stack  物品堆
     * @return 消耗后的物品堆
     */
    public static ItemStack consumeOne(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return stack;
    }

    /**
     * 获取胶囊的冷却时间（秒）
     * 
     * @param capsuleType 胶囊类型
     * @return 冷却时间（秒）
     */
    public static int getCooldownSeconds(Item capsuleType) {
        if (capsuleType == DghHModItems.LAMIVUDINE_CAPSULE.get()) {
            return Config.LAMIVUDINE_COOLDOWN_SECONDS;
        } else if (capsuleType == DghHModItems.IBUPROFEN_CAPSULE.get()) {
            return Config.IBUPROFEN_COOLDOWN_SECONDS;
        } else if (capsuleType == DghHModItems.DEXTROMETHORPHAN_CAPSULE.get()) {
            return Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS;
        }
        return 60; // 默认冷却时间
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        Item item = stack.getItem();

        // ==================== 拉米夫定胶囊 ====================
        if (item == DghHModItems.LAMIVUDINE_CAPSULE.get()) {
            int cureChancePercent = (int) (Config.LAMIVUDINE_AIDS_CURE_CHANCE * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_capsule.tooltip", cureChancePercent)
                    .withStyle(ChatFormatting.GREEN));
        }

        // ==================== 右美沙芬胶囊 ====================
        else if (item == DghHModItems.DEXTROMETHORPHAN_CAPSULE.get()) {
            int mildCurePercent = (int) (Config.DEXTROMETHORPHAN_MILD_CURE_CHANCE * 100);
            int moderateToMildPercent = (int) (Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.dextromethorphan_capsule.effect",
                    mildCurePercent, moderateToMildPercent)
                    .withStyle(ChatFormatting.AQUA));
        }

        // ==================== 布洛芬胶囊 ====================
        else if (item == DghHModItems.IBUPROFEN_CAPSULE.get()) {
            tooltip.add(Component.translatable("item.dghhealthcraft.ibuprofen_capsule.tooltip")
                    .withStyle(ChatFormatting.GOLD));
            int durationSeconds = Config.PTSD_EFFECT_DURATION_TICKS / 20;
            tooltip.add(Component.translatable("item.dghhealthcraft.ibuprofen_capsule.duration", durationSeconds)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}