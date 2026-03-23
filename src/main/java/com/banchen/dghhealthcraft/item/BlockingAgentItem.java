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

/**
 * 阻断剂 - 短时间内免疫尸毒感染
 */
public class BlockingAgentItem extends Item {
    
    public BlockingAgentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // 检查是否已经激活阻断效果
        if (ZombieVirusCompatHandler.isBlockerActive(player)) {
            int remainingSeconds = ZombieVirusCompatHandler.getBlockerRemainingSeconds(player);
            player.displayClientMessage(
                Component.translatable("dghhealthcraft.msg.blocker_already_active", remainingSeconds), 
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        // 计算持续时间（ticks）
        int durationSeconds = (int) Config.BLOCKING_AGENT_CORPSE_POISON_IMMUNITY_DURATION_SECONDS;
        int durationTicks = durationSeconds * 20;
        
        // 激活阻断效果
        ZombieVirusCompatHandler.activateBlocker(player, durationTicks);
        
        // 发送提示消息
        player.displayClientMessage(
            Component.translatable("dghhealthcraft.msg.blocker_activated", durationSeconds), 
            true
        );
        
        // 如果不是创造模式，消耗物品
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        
        // 添加物品冷却（可配置）
        player.getCooldowns().addCooldown(this, durationTicks);
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}