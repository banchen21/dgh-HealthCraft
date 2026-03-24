package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// 布洛芬 
public class IbuprofenItem extends Item {
    public IbuprofenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (URTICompatHandler.isURTIActive(player)) {
            URTICompatHandler.temporaryRelief(player);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, Config.IBUPROFEN_COOLDOWN_SECONDS * 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
