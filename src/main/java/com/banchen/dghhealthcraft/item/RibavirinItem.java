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
 * 利巴韦林 - 立即将重型、中型上呼吸道感染降为轻型
 */
public class RibavirinItem extends Item {
    
    public RibavirinItem(Properties properties) {
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
                Component.translatable("dghhealthcraft.msg.ribavirin_no_urti"), 
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        int stage = URTICompatHandler.getInfectionStage(player);
        
        // 检查配置是否允许立即治疗
        if (!Config.RIBAVIRIN_IMMEDIATE_EFFECT) {
            player.displayClientMessage(
                Component.translatable("dghhealthcraft.msg.ribavirin_disabled"), 
                true
            );
            return InteractionResultHolder.fail(stack);
        }
        
        // 根据感染阶段处理
        if (stage == 2 || stage == 3) { // 中型(2)或重型(3)
            // 立即将重型、中型降为轻型
            URTICompatHandler.immediateTreatment(player);
            
            // 显示治疗成功消息
            player.displayClientMessage(
                Component.translatable("dghhealthcraft.msg.ribavirin_success"), 
                true
            );
            
            // 播放治疗效果音效
            player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);
        } else if (stage == 1) { // 轻型
            // 轻型可以直接治愈（可选功能）
            if (canCureMild(player)) {
                URTICompatHandler.cureURTI(player, URTICompatHandler.getInfectionValue(player));
                player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.ribavirin_cured"), 
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.translatable("dghhealthcraft.msg.ribavirin_mild_warning"), 
                    true
                );
                return InteractionResultHolder.fail(stack);
            }
        }
        
        // 如果不是创造模式，消耗物品
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        
        // 添加物品冷却（可配置）
        player.getCooldowns().addCooldown(this, Config.RIBAVIRIN_COOLDOWN_SECONDS * 20);
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    /**
     * 检查是否可以治愈轻型感染（可配置）
     */
    private boolean canCureMild(Player player) {
        // 可选：检查营养是否充足等条件
        // 默认允许治愈
        return true;
    }
}