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

    public DGH_HealthcraftMod() {
        // 注册配置（但不要立即加载）
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSpec.COMMON_SPEC,
                "dghhealthcraft-common.toml");

        // 监听配置加载事件
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        // 注意：不要在构造函数中调用 Config.init()！
        // 配置会在 onCommonSetup 中加载
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 先同步配置（此时 Forge 已经加载了配置）
            Config.syncFromForgeConfig();

            // 注册所有疾病条件
            URTICompatHandler.register();
            SepsisCompatHandler.register();
            HIVCompatHandler.register();
            ZombieVirusCompatHandler.register();
            PTSDCompatHandler.register();

            System.out.println("[DGH-HealthCraft] All conditions registered successfully!");
        });
    }
}