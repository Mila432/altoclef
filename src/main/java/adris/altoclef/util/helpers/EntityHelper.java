package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug; // Added import
import adris.altoclef.multiversion.DamageSourceWrapper;
import adris.altoclef.multiversion.MethodWrapper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;


/**
 * Helper functions to interpret entity state
 */
public class EntityHelper {
    public static final double ENTITY_GRAVITY = 0.08; // per second

    public static boolean isAngryAtPlayer(AltoClef mod, Entity mob) {
        boolean hostile = isProbablyHostileToPlayer(mod, mob);
        if (mob instanceof LivingEntity entity) {
            return hostile && entity.canSee(mod.getPlayer());
        }
        return hostile;
    }

    public static boolean isProbablyHostileToPlayer(AltoClef mod, Entity entity) {
        if (entity instanceof MobEntity mob) {
            if (mob instanceof SlimeEntity slime) {
                double attackDamage = slime.getAttributeValue(
                    //#if MC >= 12111
                    EntityAttributes.ATTACK_DAMAGE
                    //#else
                    //$$ EntityAttributes.GENERIC_ATTACK_DAMAGE
                    //#endif
                );
                if (attackDamage <= 0) {
                }
                return attackDamage > 0;
            }
            if (mob instanceof PiglinEntity piglin) {
                boolean isAttacking = piglin.isAttacking();
                boolean isTrading = isTradingPiglin(mob);
                boolean isAdult = piglin.isAdult();
                boolean result = isAttacking && !isTrading && isAdult;
                return result;
            }
            if (mob instanceof EndermanEntity enderman) {
                boolean isAngry = enderman.isAngry();
                if (!isAngry && enderman.isAttacking()) {
                }
                return isAngry;
            }
            if (mob instanceof ZombifiedPiglinEntity zombifiedPiglin) {
                boolean isAttacking = zombifiedPiglin.isAttacking();
                if (isAttacking && zombifiedPiglin.isBaby()) {
                }
                return isAttacking;
            }

            return mob.isAttacking() || mob instanceof HostileEntity;
        }

        return false;
    }

    public static boolean isTradingPiglin(Entity entity) {
        if (entity instanceof PiglinEntity pig) {
            ItemStack stack1 = pig.getMainHandStack();
            ItemStack stack2 = pig.getOffHandStack();
            if (!stack1.isEmpty() && stack1.getItem().equals(Items.GOLD_INGOT)) {
                return true;
            }
            if (!stack2.isEmpty() && stack2.getItem().equals(Items.GOLD_INGOT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the resulting damage dealt to a player as a result of some damage.
     * If this player were to receive this damage, the player's health will be subtracted by the resulting value.
     */
    public static double calculateResultingPlayerDamage(PlayerEntity player, DamageSource src, double damageAmount) {
        if (player == null) {
            return 0;
        }
        
        if (damageAmount < 0) {
        }

        // Copied logic from `PlayerEntity.applyDamage`
        DamageSourceWrapper source = DamageSourceWrapper.of(src);

        //#if MC >= 12111
        if (player.getAbilities().invulnerable || player.isSpectator())
        //#else
        //$$ if (player.isInvulnerableTo(src))
        //#endif
            return 0;

        // Armor Base
        if (!source.bypassesArmor()) {
            damageAmount = MethodWrapper.getDamageLeft(player, damageAmount,src,player.getArmor(),player.getAttributeValue(
                //#if MC >= 12111
                EntityAttributes.ARMOR_TOUGHNESS
                //#else
                //$$ EntityAttributes.GENERIC_ARMOR_TOUGHNESS
                //#endif
            ));
            
            if (damageAmount < 0) {
            }
        }

        // Enchantments & Potions
        if (!source.bypassesShield()) {
            float k;
            if (player.hasStatusEffect(StatusEffects.RESISTANCE) && source.isOutOfWorld()) {
                //noinspection ConstantConditions
                k = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                float j = 25 - k;
                double f = damageAmount * (double) j;
                double g = damageAmount;
                damageAmount = Math.max(f / 25.0F, 0.0F);
                
                if (j <= 0) {
                }
            }

            if (damageAmount <= 0.0) {
                damageAmount = 0.0;
            } else {
                //#if MC >= 12100
                k = EnchantmentHelper.getProtectionAmount(null, player, src);
                //#else
                //$$ k = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), src);
                //#endif
                if (k > 0) {
                    damageAmount = DamageUtil.getInflictedDamage((float) damageAmount, (float) k);
                    
                    if (damageAmount < 0) {
                    }
                }
            }
        }

        // Absorption
        damageAmount = Math.max(damageAmount - player.getAbsorptionAmount(), 0.0F);
        
        if (damageAmount == 0 && player.getAbsorptionAmount() > 0) {
        }
        
        return damageAmount;
    }
}