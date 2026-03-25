package com.banchen.dghhealthcraft.item;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.compat.HIVCompatHandler;
import com.banchen.dghhealthcraft.compat.SepsisCompatHandler;
import com.banchen.dghhealthcraft.compat.ZombieVirusCompatHandler;
import com.banchen.dghhealthcraft.registry.DghHModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * 药针
 * 通用注射器，可用于注射各种药物
 */
public class UniversalSyringeItem extends Item {
    
    private static final Random RANDOM = new Random();
    private final boolean isContaminated;

    public UniversalSyringeItem(Properties properties, boolean isContaminated) {
        super(properties);
        this.isContaminated = isContaminated;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (isContaminated) {
            // 使用受污染的药针 - 可能感染疾病
            applyContaminatedEffect(player);
            
            // 消耗物品
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        } else {
            // 使用干净药针 - 产生受污染药针
            if (!player.isCreative()) {
                stack.shrink(1);
                giveContaminatedSyringe(player);
            }
        }

        // 播放音效
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);

        // 冷却时间
        player.getCooldowns().addCooldown(this, Config.RIBAVIRIN_COOLDOWN_SECONDS * 20);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
    
    /**
     * 使用受污染药针的效果
     * 可能感染 HIV、脓毒症或尸毒
     */
    private void applyContaminatedEffect(Player player) {
        
        // 检查是否被阻断剂保护
        if (ZombieVirusCompatHandler.isBlockerActive(player)) {
            return;
        }
        
        // 感染 HIV
        if (!HIVCompatHandler.isHIVActive(player) && 
            RANDOM.nextFloat() < Config.AIDS_CONTAMINATED_SYRINGE_CHANCE) {
            HIVCompatHandler.applyInfection(player, HIVCompatHandler.HIV_AIDS, 0.05f);
        }
        
        // 感染脓毒症
        if (!SepsisCompatHandler.isSepsisActive(player) && 
            RANDOM.nextFloat() < Config.SEPSIS_CONTAMINATED_SYRINGE_CHANCE) {
            SepsisCompatHandler.applyInfection(player, SepsisCompatHandler.SEPSIS_MILD, 0.03f);
        }
        
        // 感染尸毒
        if (!ZombieVirusCompatHandler.isZombieVirusActive(player) && 
            RANDOM.nextFloat() < Config.CORPSE_POISON_UNDEAD_ATTACK_CHANCE) {
            ZombieVirusCompatHandler.applyInfection(player, ZombieVirusCompatHandler.ZOMBIE_VIRUS_EARLY, 0.03f);
        }
        
    }

    /**
     * 给玩家一个受污染的药针
     */
    public static void giveContaminatedSyringe(Player player) {
        ItemStack contaminatedSyringe = new ItemStack(DghHModItems.CONTAMINATED_SYRINGE.get());
        if (!player.addItem(contaminatedSyringe)) {
            player.drop(contaminatedSyringe, false);
        }
    }
    
    /**
     * 检查药针是否受污染
     */
    public boolean isContaminated() {
        return isContaminated;
    }
}