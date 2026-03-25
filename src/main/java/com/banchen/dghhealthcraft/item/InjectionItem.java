package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
}