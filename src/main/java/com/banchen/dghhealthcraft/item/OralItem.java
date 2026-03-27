package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.PTSDCompatHandler;
import com.banchen.dghhealthcraft.compat.ZombieVirusCompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 口服液
 * 用于各种口服药剂
 * 
 * 支持两种类型：
 * <ul>
 *     <li>口服靶向剂 - 缓解尸毒和尸变带来的恶化</li>
 *     <li>口服镇静剂 - 缓解创伤后应激障碍症状</li>
 * </ul>
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        Item item = stack.getItem();

        // ==================== 口服靶向剂 ====================
        if (item == DghHModItems.ORAL_TARGETED_AGENT.get()) {
            int reductionPercent = (int) (Config.TARGETED_AGENT_DETERIORATION_REDUCTION * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_targeted_agent.tooltip.stabilize")
                .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_targeted_agent.tooltip.zombie_virus")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_targeted_agent.tooltip.zombification")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_targeted_agent.tooltip.reduction",
                reductionPercent).withStyle(ChatFormatting.GREEN));
            
            // 冷却时间
            int cooldownSeconds = Config.CAPSULE_EFFECT_DELAY_SECONDS;
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_targeted_agent.cooldown", cooldownSeconds)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        
        // ==================== 口服镇静剂 ====================
        else if (item == DghHModItems.ORAL_SEDATIVE.get()) {
            int reliefPercent = (int) ((1 - Config.SEDATIVE_PTSD_RELIEF_FACTOR) * 100);
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_sedative.tooltip.stabilize")
                .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_sedative.tooltip.ptsd")
                .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_sedative.tooltip.relief", reliefPercent)
                .withStyle(ChatFormatting.GREEN));
            
            // 冷却时间
            int cooldownSeconds = Config.CAPSULE_EFFECT_DELAY_SECONDS;
            tooltip.add(Component.translatable("item.dghhealthcraft.oral_sedative.cooldown", cooldownSeconds)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}