package com.banchen.dghhealthcraft.registry;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;

public class DghHModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            DGH_HealthcraftMod.MODID);

    public static final RegistryObject<CreativeModeTab> HEALTH_CRAFT = TABS.register("health_craft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dghhealthcraft"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> DghHModItems.LAMIVUDINE_CAPSULE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(DghHModItems.LAMIVUDINE_CAPSULE.get());
                        output.accept(DghHModItems.DEXTROMETHORPHAN_CAPSULE.get());
                        output.accept(DghHModItems.IBUPROFEN_CAPSULE.get());
                        output.accept(DghHModItems.CONTAINER.get());
                        output.accept(DghHModItems.LAMIVUDINE_BOTTLE.get());
                        output.accept(DghHModItems.DEXTROMETHORPHAN_BOTTLE.get());
                        output.accept(DghHModItems.IBUPROFEN_BOTTLE.get());
                        output.accept(DghHModItems.SYRINGE.get());
                        output.accept(DghHModItems.CONTAMINATED_SYRINGE.get());
                        output.accept(DghHModItems.BLOCKER_INJECTION.get());
                        output.accept(DghHModItems.RIBAVIRIN_INJECTION.get());
                        output.accept(DghHModItems.ORAL_TARGETED_AGENT.get());
                        output.accept(DghHModItems.ORAL_SEDATIVE.get());
                        output.accept(DghHModItems.NUTRITION_SCANNER.get());
                    })
                    .build());
}
