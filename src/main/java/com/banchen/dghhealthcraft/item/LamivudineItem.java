package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// 拉米夫定  - 具有一定概率治愈 HIV 感染
public class LamivudineItem extends Item {
    public LamivudineItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean hadHiv = HIVCompatHandler.isHIVActive(player);
        if (!hadHiv) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.lamivudine_no_hiv"), true);
        }

        HIVCompatHandler.cureHIV(player);

        boolean nowHiv = HIVCompatHandler.isHIVActive(player);
        if (hadHiv && !nowHiv) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.lamivudine_cured"), true);
        } else if (hadHiv) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.lamivudine_failed"), true);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, Config.CAPSULE_EFFECT_DELAY_SECONDS * 20);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
