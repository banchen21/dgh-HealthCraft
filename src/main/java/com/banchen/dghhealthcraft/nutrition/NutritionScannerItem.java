package com.banchen.dghhealthcraft.nutrition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 营养扫描仪：右键显示玩家当前营养状态。 
 */
public class NutritionScannerItem extends Item {
    public NutritionScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        double water = NutritionCompatHandler.getWater(player);
        double sugar = NutritionCompatHandler.getSugar(player);
        double fat = NutritionCompatHandler.getFat(player);
        double protein = NutritionCompatHandler.getProtein(player);
        double salt = NutritionCompatHandler.getSalt(player);
        double vitamin = NutritionCompatHandler.getVitamin(player);
        double fiber = NutritionCompatHandler.getFiber(player);

        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_header"), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_water", String.format("%.1f", water)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_sugar", String.format("%.1f", sugar)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_fat", String.format("%.1f", fat)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_protein", String.format("%.1f", protein)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_salt", String.format("%.1f", salt)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_vitamin", String.format("%.1f", vitamin)), false);
        player.displayClientMessage(Component.translatable("dghhealthcraft.msg.nutritionscanner_fiber", String.format("%.1f", fiber)), false);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
