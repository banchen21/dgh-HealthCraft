package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.SepsisCompatHandler;
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
import java.util.Random;

/**
 * 药针
 * 通用注射器，可用于注射各种药物
 * 
 * 两种状态：
 * <ul>
 * <li>干净药针 - 使用后变为受污染药针</li>
 * <li>受污染药针 - 使用后有感染 HIV、脓毒症、尸毒的风险</li>
 * </ul>
 */
public class UniversalSyringeItem extends Item {

    private static final Random RANDOM = new Random();
    private final boolean isContaminated;

    public UniversalSyringeItem(Properties properties, boolean isContaminated) {
        super(properties);
        this.isContaminated = isContaminated;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (isContaminated) {
            // 使用受污染的药针 - 可能感染疾病
            applyContaminatedEffect(player);

            // 消耗物品
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        } else {
            // 使用干净药针 - 产生受污染药针
            if (!player.isCreative()) {
                stack.shrink(1);
                giveContaminatedSyringe(player);
            }
        }

        // 播放音效
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);

        // 冷却时间
        player.getCooldowns().addCooldown(this, Config.RIBAVIRIN_COOLDOWN_SECONDS * 20);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /**
     * 使用受污染药针的效果
     * 可能感染 HIV、脓毒症
     */
    private void applyContaminatedEffect(Player player) {


        // 感染 HIV
        if (!HIVCompatHandler.isHIVActive(player) &&
                RANDOM.nextFloat() < Config.AIDS_CONTAMINATED_SYRINGE_CHANCE) {
            HIVCompatHandler.applyInfection(player, HIVCompatHandler.HIV_AIDS, 0.05f);
        }

        // 感染脓毒症
        if (!SepsisCompatHandler.isSepsisActive(player) &&
                RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
            SepsisCompatHandler.applyInfection(player, SepsisCompatHandler.SEPSIS_MILD, 0.03f);
        }

    }

    /**
     * 给玩家一个受污染的药针
     */
    public static void giveContaminatedSyringe(Player player) {
        ItemStack contaminatedSyringe = new ItemStack(DghHModItems.CONTAMINATED_SYRINGE.get());
        if (!player.addItem(contaminatedSyringe)) {
            player.drop(contaminatedSyringe, false);
        }
    }

    /**
     * 检查药针是否受污染
     */
    public boolean isContaminated() {
        return isContaminated;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // ==================== 干净药针 ====================
        if (!isContaminated) {
            tooltip.add(Component.translatable("item.dghhealthcraft.syringe.tooltip")
                    .withStyle(ChatFormatting.GRAY));

            // 添加使用提示
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("item.dghhealthcraft.syringe.usage_hint"))
                    .withStyle(ChatFormatting.DARK_GREEN));

            // 冷却时间提示
            int cooldownSeconds = Config.RIBAVIRIN_COOLDOWN_SECONDS;
            tooltip.add(Component.translatable("item.dghhealthcraft.syringe.cooldown", cooldownSeconds)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        // ==================== 受污染药针 ====================
        else {
            tooltip.add(Component.translatable("item.dghhealthcraft.contaminated_syringe.tooltip")
                    .withStyle(ChatFormatting.RED));

            // 显示感染风险概率
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("item.dghhealthcraft.contaminated_syringe.infection_risks"))
                    .withStyle(ChatFormatting.GOLD));

            // HIV 感染概率
            int hivChancePercent = (int) (Config.AIDS_CONTAMINATED_SYRINGE_CHANCE * 100);
            tooltip.add(Component.literal("  • ")
                    .append(Component.translatable("item.dghhealthcraft.contaminated_syringe.hiv_risk",
                            hivChancePercent))
                    .withStyle(ChatFormatting.DARK_RED));

            // 脓毒症感染概率
            int sepsisChancePercent = (int) (Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE * 100);
            tooltip.add(Component.literal("  • ")
                    .append(Component.translatable("item.dghhealthcraft.contaminated_syringe.sepsis_risk",
                            sepsisChancePercent))
                    .withStyle(ChatFormatting.DARK_RED));

            // 尸毒感染概率
            int corpsePoisonChancePercent = (int) (Config.CORPSE_POISON_UNDEAD_ATTACK_CHANCE * 100);
            tooltip.add(Component.literal("  • ")
                    .append(Component.translatable("item.dghhealthcraft.contaminated_syringe.corpse_poison_risk",
                            corpsePoisonChancePercent))
                    .withStyle(ChatFormatting.DARK_RED));

            // 阻断剂保护提示
            tooltip.add(Component.translatable("item.dghhealthcraft.contaminated_syringe.blocker_protection")
                    .withStyle(ChatFormatting.DARK_GREEN));

            // 冷却时间
            int cooldownSeconds = Config.RIBAVIRIN_COOLDOWN_SECONDS;
            tooltip.add(Component.translatable("item.dghhealthcraft.syringe.cooldown", cooldownSeconds)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}