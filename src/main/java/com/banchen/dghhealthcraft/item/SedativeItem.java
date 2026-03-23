package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.PTSDCompatHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// SedativeItem 代表镇静剂，使用后尝试缓解 PTSD 症状。
public class SedativeItem extends Item {
    public SedativeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!PTSDCompatHandler.isPTSDActive(player)) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.sedative_no_ptsd"), true);
        } else {
            PTSDCompatHandler.applySedativeRelief(player);
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.sedative_relief"), true);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, Config.SEDATIVE_COOLDOWN_SECONDS * 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
