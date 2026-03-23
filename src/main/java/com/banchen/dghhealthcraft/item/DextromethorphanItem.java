package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// 右美沙芬
public class DextromethorphanItem extends Item {
    public DextromethorphanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        float before = URTICompatHandler.getInfectionValue(player);
        boolean hadUrti = URTICompatHandler.isURTIActive(player);

        URTICompatHandler.treatMildURTI(player);

        float after = URTICompatHandler.getInfectionValue(player);
        if (!hadUrti) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.dextromethorphan_no_urti"), true);
        } else if (after < before) {
            if (before < 0.4f) {
                player.displayClientMessage(Component.translatable("dghhealthcraft.msg.dextromethorphan_cured"), true);
            } else {
                player.displayClientMessage(Component.translatable("dghhealthcraft.msg.dextromethorphan_moderate_to_mild"), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.dextromethorphan_failed"), true);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS * 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
