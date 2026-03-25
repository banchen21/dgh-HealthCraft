package com.banchen.dghhealthcraft.registry;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.banchen.dghhealthcraft.item.InjectionItem;
import com.banchen.dghhealthcraft.item.DghHBottleItem;
import com.banchen.dghhealthcraft.item.CapsuleItem;
import com.banchen.dghhealthcraft.item.OralItem;
import com.banchen.dghhealthcraft.nutrition.NutritionScannerItem;
import com.banchen.dghhealthcraft.item.UniversalSyringeItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DghHModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            DGH_HealthcraftMod.MODID);

    // 拉米夫定胶囊，叠堆上限16
    public static final RegistryObject<Item> LAMIVUDINE_CAPSULE = ITEMS.register("lamivudine_capsule",
            () -> new CapsuleItem(new Item.Properties().stacksTo(16)));
    // 右美沙芬胶囊，叠堆上限16
    public static final RegistryObject<Item> DEXTROMETHORPHAN_CAPSULE = ITEMS.register("dextromethorphan_capsule",
            () -> new CapsuleItem(new Item.Properties().stacksTo(16)));
    // 布洛芬胶囊，叠堆上限16
    public static final RegistryObject<Item> IBUPROFEN_CAPSULE = ITEMS.register("ibuprofen_capsule",
            () -> new CapsuleItem(new Item.Properties().stacksTo(16)));

    // 胶囊瓶（含8次用量）
    public static final RegistryObject<Item> LAMIVUDINE_BOTTLE = ITEMS.register("lamivudine_bottle",
            () -> new DghHBottleItem(new Item.Properties().stacksTo(1).durability(8)));
    public static final RegistryObject<Item> DEXTROMETHORPHAN_BOTTLE = ITEMS.register("dextromethorphan_bottle",
            () -> new DghHBottleItem(new Item.Properties().stacksTo(1).durability(8)));
    public static final RegistryObject<Item> IBUPROFEN_BOTTLE = ITEMS.register("ibuprofen_bottle",
            () -> new DghHBottleItem(new Item.Properties().stacksTo(1).durability(8)));

    // 药针/受污染药针（主手），叠堆上限16
    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe",
            () -> new UniversalSyringeItem(new Item.Properties().stacksTo(16), false));
    public static final RegistryObject<Item> CONTAMINATED_SYRINGE = ITEMS.register("contaminated_syringe",
            () -> new UniversalSyringeItem(new Item.Properties().stacksTo(16), true));

    // 阻断剂注射液（副手），耐久32，叠堆上限1
    public static final RegistryObject<Item> BLOCKER_INJECTION = ITEMS.register("blocker_injection",
            () -> new InjectionItem(new Item.Properties().stacksTo(1).durability(32)));
    // 利巴韦林注射液（副手），耐久32，叠堆上限1
    public static final RegistryObject<Item> RIBAVIRIN_INJECTION = ITEMS.register("ribavirin_injection",
            () -> new InjectionItem(new Item.Properties().stacksTo(1).durability(32)));

    // 靶向口服剂，叠堆上限16
    public static final RegistryObject<Item> ORAL_TARGETED_AGENT = ITEMS.register("oral_targeted_agent",
            () -> new OralItem(new Item.Properties().stacksTo(16)));
    // 镇静口服剂，叠堆上限16
    public static final RegistryObject<Item> ORAL_SEDATIVE = ITEMS.register("oral_sedative",
            () -> new OralItem(new Item.Properties().stacksTo(16)));

    // 营养扫描仪，耐久32，叠堆上限1
    public static final RegistryObject<Item> NUTRITION_SCANNER = ITEMS.register("nutrition_scanner",
            () -> new NutritionScannerItem(new Item.Properties().stacksTo(1).durability(32)));
}
