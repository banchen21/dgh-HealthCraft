package com.banchen.dghhealthcraft.client;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.Config.NutritionValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = "dghhealthcraft", value = Dist.CLIENT)
public class NutritionTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return;
        NutritionValues values = Config.FOOD_NUTRITION_MAP.get(id.toString());
        if (values == null) return;

        // 只显示有数值的营养（多语言）
        if (values.water != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.water", values.water).withStyle(ChatFormatting.AQUA));
        }
        if (values.sugar != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.sugar", values.sugar).withStyle(ChatFormatting.GOLD));
        }
        if (values.fat != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.fat", values.fat).withStyle(ChatFormatting.YELLOW));
        }
        if (values.protein != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.protein", values.protein).withStyle(ChatFormatting.GREEN));
        }
        if (values.salt != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.salt", values.salt).withStyle(ChatFormatting.GRAY));
        }
        if (values.vitamin != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.vitamin", values.vitamin).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (values.fiber != 0) {
            event.getToolTip().add(Component.translatable("tooltip.dghhealthcraft.nutrition.fiber", values.fiber).withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
