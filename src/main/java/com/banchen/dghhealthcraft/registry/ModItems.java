package com.banchen.dghhealthcraft.registry;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.banchen.dghhealthcraft.item.BlockingAgentItem;
import com.banchen.dghhealthcraft.item.DextromethorphanItem;
import com.banchen.dghhealthcraft.item.IbuprofenItem;
import com.banchen.dghhealthcraft.item.LamivudineItem;
import com.banchen.dghhealthcraft.item.NutritionScannerItem;
import com.banchen.dghhealthcraft.item.RibavirinItem;
import com.banchen.dghhealthcraft.item.SedativeItem;
import com.banchen.dghhealthcraft.item.TargetedAgentItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            DGH_HealthcraftMod.MODID);

    public static final RegistryObject<Item> LAMIVUDINE_CAPSULE = ITEMS.register("lamivudine_capsule",
            () -> new LamivudineItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DEXTROMETHORPHAN_CAPSULE = ITEMS.register("dextromethorphan_capsule",
            () -> new DextromethorphanItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> IBUPROFEN_CAPSULE = ITEMS.register("ibuprofen_capsule",
            () -> new IbuprofenItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> RIBAVIRIN_SYRINGE = ITEMS.register("ribavirin_syringe",
            () -> new RibavirinItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> ORAL_TARGETED_AGENT = ITEMS.register("oral_targeted_agent",
            () -> new TargetedAgentItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ORAL_SEDATIVE = ITEMS.register("oral_sedative",
            () -> new SedativeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> BLOCKER = ITEMS.register("blocker",
            () -> new BlockingAgentItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> NUTRITION_SCANNER = ITEMS.register("nutrition_scanner",
            () -> new NutritionScannerItem(new Item.Properties().stacksTo(1).durability(32)));
}
