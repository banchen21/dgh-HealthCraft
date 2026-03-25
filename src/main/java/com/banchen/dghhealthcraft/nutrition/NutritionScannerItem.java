package com.banchen.dghhealthcraft.nutrition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 营养扫描仪：右键显示玩家或目标实体的营养成分信息
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

        Player targetPlayer = findTargetPlayer(player, 6.0D);
        Player scanTarget = targetPlayer != null ? targetPlayer : player;

        double water = NutritionCompatHandler.getWater(scanTarget);
        double sugar = NutritionCompatHandler.getSugar(scanTarget);
        double fat = NutritionCompatHandler.getFat(scanTarget);
        double protein = NutritionCompatHandler.getProtein(scanTarget);
        double salt = NutritionCompatHandler.getSalt(scanTarget);
        double vitamin = NutritionCompatHandler.getVitamin(scanTarget);
        double fiber = NutritionCompatHandler.getFiber(scanTarget);

        if (scanTarget == player) {
            player.displayClientMessage(Component.literal("扫描对象：自己"), false);
        } else {
            player.displayClientMessage(Component.literal("扫描对象：" + scanTarget.getName().getString()), false);
        }
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

    private static Player findTargetPlayer(Player player, double range) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachPos = eyePos.add(lookVec.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                reachPos,
                searchBox,
                entity -> entity instanceof Player && entity != player,
                range * range);

        if (hitResult == null) {
            return null;
        }

        Entity target = hitResult.getEntity();
        return target instanceof Player targetPlayer ? targetPlayer : null;
    }
}
