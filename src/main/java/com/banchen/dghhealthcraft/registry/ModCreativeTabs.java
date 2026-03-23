package com.banchen.dghhealthcraft.registry;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            DGH_HealthcraftMod.MODID);

    public static final RegistryObject<CreativeModeTab> MEDICAL_CRAFT = TABS.register("medical_craft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dghhealthcraft"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.LAMIVUDINE_CAPSULE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LAMIVUDINE_CAPSULE.get());
                        output.accept(ModItems.DEXTROMETHORPHAN_CAPSULE.get());
                        output.accept(ModItems.IBUPROFEN_CAPSULE.get());
                        output.accept(ModItems.RIBAVIRIN_CAPSULE.get());
                        output.accept(ModItems.ORAL_TARGETED_AGENT.get());
                        output.accept(ModItems.ORAL_SEDATIVE.get());
                        output.accept(ModItems.BLOCKER.get());
                        output.accept(ModItems.NUTRITION_SCANNER.get());
                    })
                    .build());
}
