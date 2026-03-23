package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.ZombieVirusCompatHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

///  靶向剂
public class TargetedAgentItem extends Item {
    public TargetedAgentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!ZombieVirusCompatHandler.isZombified(player)) {
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.targeted_agent_no_zomb"), true);
        } else {
            ZombieVirusCompatHandler.alleviateZombification(player);
            player.displayClientMessage(Component.translatable("dghhealthcraft.msg.targeted_agent_relief"), true);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, Config.TARGETED_AGENT_COOLDOWN_SECONDS * 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
