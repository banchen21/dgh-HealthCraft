package com.banchen.dghhealthcraft;

import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.PTSDCompatHandler;
import com.banchen.dghhealthcraft.compat.SepsisCompatHandler;
import com.banchen.dghhealthcraft.compat.URTICompatHandler;
import com.banchen.dghhealthcraft.compat.ZombieVirusCompatHandler;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DGH_HealthcraftMod.MODID)
public class DGH_HealthcraftMod {
    public static final String MODID = "dghhealthcraft";
    public static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MODID);

    public DGH_HealthcraftMod() {
        // 注册配置（但不要立即加载）
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC,
                "dghhealthcraft-common.toml");

        // 注册物品与创造标签
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        net.minecraftforge.registries.DeferredRegister<?>[] registers = new net.minecraftforge.registries.DeferredRegister<?>[]{
                com.banchen.dghhealthcraft.registry.DghHModItems.ITEMS,
                com.banchen.dghhealthcraft.registry.DghHModCreativeTabs.TABS
        };
        for (var r : registers) {
            r.register(bus);
        }

        // 监听配置加载事件
        bus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 先同步配置（此时 Forge 已经加载了配置）
            Config.updateConfigValues();

            // 注册所有疾病条件
            URTICompatHandler.register();
            SepsisCompatHandler.register();
            HIVCompatHandler.register();
            ZombieVirusCompatHandler.register();
            PTSDCompatHandler.register();

            LOGGER.info("All conditions registered successfully!");
        });
    }
}