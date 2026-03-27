package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 胶囊瓶
 * 用于存放各种药物胶囊的瓶子，支持多次使用
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
        // 获取冷却时间
        int durationTicks = getCooldownTicks(stack.getItem());

        // 根据瓶子类型应用具体效果
        Item bottleType = stack.getItem();

        if (bottleType == DghHModItems.LAMIVUDINE_BOTTLE.get()) {
            HIVCompatHandler.cureHIV(player);
        } else if (bottleType == DghHModItems.DEXTROMETHORPHAN_BOTTLE.get()) {
            URTICompatHandler.treatMildURTI(player);
        } else if (bottleType == DghHModItems.IBUPROFEN_BOTTLE.get()) {
            URTICompatHandler.temporaryRelief(player);
        }

        // 消耗瓶子耐久度
        damageBottle(player, stack);

        // 添加冷却时间
        player.getCooldowns().addCooldown(this, durationTicks);
    }

    /**
     * 减少耐久度并返回
     */
    public static ItemStack damageBottle(Player player, ItemStack stack) {
        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
        return stack;
    }

    /**
     * 获取瓶子对应的冷却时间（刻）
     */
    private int getCooldownTicks(Item bottleType) {
        if (bottleType == DghHModItems.LAMIVUDINE_BOTTLE.get()) {
            return Config.LAMIVUDINE_COOLDOWN_SECONDS * 20;
        } else if (bottleType == DghHModItems.DEXTROMETHORPHAN_BOTTLE.get()) {
            return Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS * 20;
        } else if (bottleType == DghHModItems.IBUPROFEN_BOTTLE.get()) {
            return Config.IBUPROFEN_COOLDOWN_SECONDS * 20;
        }
        return 0;
    }

    /**
     * 获取瓶子对应的冷却时间（秒）
     */
    public static int getCooldownSeconds(Item bottleType) {
        return Config.CAPSULE_EFFECT_DELAY_SECONDS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        Item item = stack.getItem();
        int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
        Style titleStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xFCFCFC));
        Style contentStyle = Style.EMPTY.withColor(TextColor.fromRgb(0x5454FC));

        // ==================== 拉米夫定瓶 ====================
        if (item == DghHModItems.LAMIVUDINE_BOTTLE.get()) {
            int cureChancePercent = (int) (Config.LAMIVUDINE_AIDS_CURE_CHANCE * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.tooltip.treat")
                .withStyle(titleStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.tooltip.aids", cureChancePercent)
                .withStyle(contentStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.returns_bottle")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.remaining_uses", remainingUses)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        // ==================== 右美沙芬瓶 ====================
        else if (item == DghHModItems.DEXTROMETHORPHAN_BOTTLE.get()) {
            int mildCurePercent = (int) (Config.DEXTROMETHORPHAN_MILD_CURE_CHANCE * 100);
            int moderateToMildPercent = (int) (Config.DEXTROMETHORPHAN_MODERATE_TO_MILD_CHANCE * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.dextromethorphan_bottle.tooltip.treat")
                .withStyle(titleStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.dextromethorphan_bottle.tooltip.mild",
                mildCurePercent)
                .withStyle(contentStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.dextromethorphan_bottle.tooltip.stabilize")
                .withStyle(titleStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.dextromethorphan_bottle.tooltip.moderate",
                moderateToMildPercent)
                .withStyle(contentStyle));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.returns_bottle")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.remaining_uses", remainingUses)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        // ==================== 布洛芬瓶 ====================
        else if (item == DghHModItems.IBUPROFEN_BOTTLE.get()) {
            tooltip.add(Component.translatable("item.dghhealthcraft.ibuprofen_bottle.tooltip.stabilize")
                .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("item.dghhealthcraft.ibuprofen_bottle.tooltip.urti")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.returns_bottle")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("item.dghhealthcraft.lamivudine_bottle.remaining_uses", remainingUses)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}