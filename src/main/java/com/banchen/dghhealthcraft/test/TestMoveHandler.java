package com.banchen.dghhealthcraft.test;

import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.banchen.dghhealthcraft.compat.*;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.enums.BodyComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestMoveHandler {
    // 上一帧玩家位置（防抖检测移动）
    private static final Map<UUID, Vec3> LAST_POSITION = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Player player = event.player;
        if (player.level().isClientSide())
            return;

        Vec3 current = player.position();
        Vec3 last = LAST_POSITION.get(player.getUUID());

        if (last != null && current.distanceTo(last) > 0.1) {
            // 玩家移动，给予全部病症
            testApplyPlayer(player);
        }

        LAST_POSITION.put(player.getUUID(), current);
    }

    /**
     * 给玩家注入特定病症
     * 
     * @param player 玩家实体
     */
    private static void testApplyPlayer(Player player) {
        // 只有具备健康能力的玩家才处理
        if (!HealthCapability.has(player))
            return;

        HealthCapability.getAndApply(player, health -> {
            // 遍历所有身体部位
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                // 检查并注入上呼吸道感染
                if (body.getBodyConditions().contains(URTICompatHandler.URTI)) {
                    body.injury(URTICompatHandler.URTI, 0.03f);
                }

                // 检查并注入脓毒症
                if (body.getBodyConditions().contains(SepsisCompatHandler.SEPSIS)) {
                    body.injury(SepsisCompatHandler.SEPSIS, 0.03f);
                }

                // 检查并注入艾滋病
                if (body.getBodyConditions().contains(HIVCompatHandler.HIV)) {
                    body.injury(HIVCompatHandler.HIV, 0.03f);
                }

                // 检查并注入创伤后应激障碍
                if (body.getBodyConditions().contains(PTSDCompatHandler.PTSD)) {
                    body.injury(PTSDCompatHandler.PTSD, 0.03f);
                }

                // 检查并注入尸毒病毒
                if (body.getBodyConditions().contains(ZombieVirusCompatHandler.ZOMBIE_VIRUS)) {
                    body.injury(ZombieVirusCompatHandler.ZOMBIE_VIRUS, 0.03f);
                }
            }
            return null;
        }, null);
    }
}