package com.banchen.dghhealthcraft.item;

import java.util.List;

import javax.annotation.Nullable;

import com.banchen.dghhealthcraft.Config;
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
 * 注射液: 必须左手注射液，右手持有药针/受污染药针才能使用
 */
public class InjectionItem extends Item {

        public InjectionItem(Properties properties) {
                super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                ItemStack stack = player.getItemInHand(hand);

                if (level.isClientSide()) {
                        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }

                // 播放音效
                player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);

                // 消耗物品耐久度
                if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }

                // 冷却时间
                player.getCooldowns().addCooldown(this, Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS * 20);

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                        TooltipFlag flag) {
                super.appendHoverText(stack, level, tooltip, flag);

                Item item = stack.getItem();
                int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
                // ==================== 阻断剂注射液 ====================
                if (item == DghHModItems.BLOCKER_INJECTION.get()) {
                        tooltip.add(Component.translatable("item.dghhealthcraft.ribavirin_injection.tooltip.usage")
                                        .withStyle(ChatFormatting.GREEN));
                        tooltip.add(Component
                                        .translatable("item.dghhealthcraft.blocker_injection.remaining_uses",
                                                        remainingUses)
                                        .withStyle(ChatFormatting.YELLOW));
                }
                // ==================== 利巴韦林注射液 ====================
                else if (item == DghHModItems.RIBAVIRIN_INJECTION.get()) {
                        tooltip.add(Component.translatable("item.dghhealthcraft.ribavirin_injection.tooltip.usage")
                                        .withStyle(ChatFormatting.GREEN));
                        tooltip.add(Component
                                        .translatable("item.dghhealthcraft.ribavirin_injection.remaining_uses",
                                                        remainingUses)
                                        .withStyle(ChatFormatting.YELLOW));
                }

        }
}