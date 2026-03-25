package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.PTSDCompatHandler;
import com.banchen.dghhealthcraft.compat.ZombieVirusCompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 口服液
 * 用于各种口服药剂
 */
public class OralItem extends Item {

    public OralItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        // 根据物品类型应用不同效果
        Item itemType = stack.getItem();

        if (itemType == DghHModItems.ORAL_TARGETED_AGENT.get()) {
            // 靶向口服剂 - 缓解尸毒/尸变
            applyTargetedAgentEffect(player);
        } else if (itemType == DghHModItems.ORAL_SEDATIVE.get()) {
            // 镇静口服剂 - 缓解 PTSD
            applySedativeEffect(player);
        } else {
            // 默认行为（兼容其他未定义的口服剂）
            return InteractionResultHolder.pass(stack);
        }

        // 消耗物品
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // 添加冷却时间
        player.getCooldowns().addCooldown(this, Config.CAPSULE_EFFECT_DELAY_SECONDS * 20);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /**
     * 应用靶向口服剂效果
     * 缓解尸毒感染和尸变恶化
     */
    private void applyTargetedAgentEffect(Player player) {

        // 检查是否有尸毒感染
        if (ZombieVirusCompatHandler.isZombieVirusActive(player)) {
            ZombieVirusCompatHandler.alleviateZombieVirus(player);
        }

        // 检查是否已经尸变
        if (ZombieVirusCompatHandler.isZombified(player)) {
            ZombieVirusCompatHandler.alleviateZombification(player);
        }
    }

    /**
     * 应用镇静口服剂效果
     * 缓解创伤后应激障碍
     */
    private void applySedativeEffect(Player player) {
        if (PTSDCompatHandler.isPTSDActive(player)) {
            PTSDCompatHandler.applySedativeRelief(player);
        }
    }
}