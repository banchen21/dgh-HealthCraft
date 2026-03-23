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

/**
 * 右美沙芬 - 治疗轻型上呼吸道感染，中型有概率降为轻型
 */
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

        // 检查是否感染上呼吸道感染
        if (!URTICompatHandler.isURTIActive(player)) {
            player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.dextromethorphan.no_urti"),
                    true);
            return InteractionResultHolder.fail(stack);
        }

        // 治疗
        URTICompatHandler.treatMildURTI(player);

        // 播放音效
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);

        // 消耗物品
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // 冷却时间
        player.getCooldowns().addCooldown(this, Config.DEXTROMETHORPHAN_COOLDOWN_SECONDS * 20);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}