package com.banchen.dghhealthcraft.test;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.banchen.dghhealthcraft.compat.*;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestMoveHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dghh")
                        .then(Commands.literal("test")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    applyAllTestDiseases(player);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("已注入五个病症（URTI/SEPSIS/HIV/PTSD/ZOMBIE_VIRUS）"),
                                            true);
                                    return 1;
                                })));
    }

    private static void applyAllTestDiseases(ServerPlayer player) {
        if (!HealthCapability.has(player))
            return;

        // 我们将注入固定值给已注册体部条件
        applyCondition(player, URTICompatHandler.URTI, 0.2f);
        applyCondition(player, SepsisCompatHandler.SEPSIS, 0.2f);
        applyCondition(player, HIVCompatHandler.HIV, 0.2f);
        applyCondition(player, PTSDCompatHandler.PTSD, 0.2f);
        applyCondition(player, ZombieVirusCompatHandler.ZOMBIE_VIRUS, 0.2f);
    }

    private static void applyCondition(ServerPlayer player, net.minecraft.resources.ResourceLocation condition,
            float amount) {
        if (!HealthCapability.has(player))
            return;

        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;
                if (body.getBodyConditions().contains(condition)) {
                    body.injury(condition, amount);
                }
            }
            return null;
        }, null);
    }

}
