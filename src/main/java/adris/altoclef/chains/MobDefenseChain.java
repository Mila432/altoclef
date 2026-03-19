package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.control.KillAura;
import adris.altoclef.multiversion.versionedfields.Entities;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasks.movement.DodgeProjectilesTask;
import adris.altoclef.tasks.movement.RunAwayFromCreepersTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.Baritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
//#if MC < 12111
//$$ import net.minecraft.item.SwordItem;
//#endif
//#if MC >= 12111
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
//#endif
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;


import java.util.*;


// TODO: Optimise shielding against spiders and skeletons

public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 30;
    private static final double CREEPER_KEEP_DISTANCE = 10;
    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2;
    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10;
    private static final double SAFE_KEEP_DISTANCE = 8;
    private static final List<Class<? extends Entity>> ignoredMobs = List.of(Entities.WARDEN, WitherEntity.class, EndermanEntity.class, BlazeEntity.class,
            WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class, MagmaCubeEntity.class);

    private static boolean shielding = false;
    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();
    private final KillAura killAura = new KillAura();
    private Entity targetEntity;
    private boolean doingFunkyStuff = false;
    private boolean wasPuttingOutFire = false;
    private CustomBaritoneGoalTask runAwayTask;
    private float prevHealth = 20;
    private boolean needsChangeOnAttack = false;
    private Entity lockedOnEntity = null;

    private float cachedLastPriority;

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
        double distance = creeper.squaredDistanceTo(pos);
//#if MC >= 12111
        float fuse = creeper.getLerpedFuseTime(1.0F);
//#else
//$$        float fuse = creeper.getClientFuseTime(1);
//#endif

        // Not fusing.
        if (fuse <= 0.001f) return distance;
        return distance * 0.2; // less is WORSE
    }

    private static void startShielding(AltoClef mod) {
        shielding = true;
        mod.getClientBaritone().getPathingBehavior().requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                for (ItemStack spaceSlot : spaceSlots) {
                    if (spaceSlot.isEmpty()) {
                        mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.QUICK_MOVE);
                        return;
                    }
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
    }

    private static int getDangerousnessScore(List<LivingEntity> toDealWithList) {
        int numberOfProblematicEntities = toDealWithList.size();
        for (LivingEntity toDealWith : toDealWithList) {
            if (toDealWith instanceof EndermanEntity || toDealWith instanceof SlimeEntity || toDealWith instanceof BlazeEntity) {

                numberOfProblematicEntities += 1;
//#if MC >= 12111
            } else if (toDealWith instanceof DrownedEntity drowned && drowned.getEquippedStack(EquipmentSlot.MAINHAND).getItem() == Items.TRIDENT) {
//#else
//$$            } else if (toDealWith instanceof DrownedEntity && toDealWith.getEquippedItems() == Items.TRIDENT) {
//#endif
                // Drowned with tridents are also REALLY dangerous, maybe we should increase this??
                numberOfProblematicEntities += 5;
            }
        }
        return numberOfProblematicEntities;
    }

    @Override
    public float getPriority() {
        cachedLastPriority = getPriorityInner();
        prevHealth = AltoClef.getInstance().getPlayer().getHealth();
        return cachedLastPriority;
    }

    private void stopShielding(AltoClef mod) {
        if (shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (ItemVer.isFood(cursor)) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            shielding = false;
        }
    }

    public boolean isShielding() {
        return shielding || killAura.isShielding();
    }

    private boolean escapeDragonBreath(AltoClef mod) {
        dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer()) {
            if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                return true;
            }
        }
        return false;
    }

    private float getPriorityInner() {
        if (!AltoClef.inGame()) {
            return Float.NEGATIVE_INFINITY;
        }
        AltoClef mod = AltoClef.getInstance();

        if (!mod.getModSettings().isMobDefense()) {
            return Float.NEGATIVE_INFINITY;
        }

        if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) return Float.NEGATIVE_INFINITY;

        if (needsChangeOnAttack && (mod.getPlayer().getHealth() < prevHealth || killAura.attackedLastTick)) {
            needsChangeOnAttack = false;
        }

        // Put out fire if we're standing on one like an idiot
        BlockPos fireBlock = isInsideFireAndOnFire(mod);
        if (fireBlock != null) {
            putOutFire(mod, fireBlock);
            wasPuttingOutFire = true;
        } else {
            // Stop putting stuff out if we no longer need to put out a fire.
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
            wasPuttingOutFire = false;
        }

        // Run away if a weird mob is close by.
        Optional<Entity> universallyDangerous = getUniversallyDangerousMob(mod);
        if (universallyDangerous.isPresent() && mod.getPlayer().getHealth() <= 10) {
            runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
            setTask(runAwayTask);
            return 70;
        }

        doingFunkyStuff = false;
        PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
        ItemStack offhandStack = StorageHelper.getItemStackInSlot(offhandSlot);
        Item offhandItem = offhandStack.getItem();
        // Run away from creepers
        CreeperEntity blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp != null) {
            boolean needsToEat = mod.getFoodChain().needsToEat();
            float playerHealth = mod.getPlayer().getHealth();
            boolean hasShield = hasShield(mod);
            boolean potionFound = mod.getEntityTracker().entityFound(PotionEntity.class);
//#if MC >= 12111
            boolean cooldownActive = mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandStack);
            float fuseTime = blowingUp.getLerpedFuseTime(blowingUp.getFuseSpeed());
//#else
//$$            boolean cooldownActive = mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem);
//$$            float fuseTime = blowingUp.getClientFuseTime(blowingUp.getFuseSpeed());
//#endif
            boolean safeToCancel = mod.getClientBaritone().getPathingBehavior().isSafeToCancel();

            boolean shouldShield = (!needsToEat || playerHealth < 9)
                    && hasShield
                    && !potionFound
                    && !cooldownActive
                    && safeToCancel
                    && fuseTime > 0.5;
            
            if (shouldShield) {
                LookHelper.lookAt(mod, blowingUp.getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                doingFunkyStuff = true;
                runAwayTask = new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE);
                setTask(runAwayTask);
//#if MC >= 12111
                return 50 + blowingUp.getLerpedFuseTime(1.0F) * 50;
//#else
//$$                return 50 + blowingUp.getClientFuseTime(1) * 50;
//#endif
            }
        }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            // Block projectiles with shield
            boolean dodgeProjectilesSetting = mod.getModSettings().isDodgeProjectiles();
            boolean hasShield = hasShield(mod);
//#if MC >= 12111
            boolean cooldownActive = mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandStack);
//#else
//$$            boolean cooldownActive = mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem);
//#endif
            boolean safeToCancel = mod.getClientBaritone().getPathingBehavior().isSafeToCancel();
            boolean potionFound = mod.getEntityTracker().entityFound(PotionEntity.class);
            boolean projectileClose = isProjectileClose(mod);

            boolean shouldBlockProjectiles = dodgeProjectilesSetting
                    && hasShield
                    && !cooldownActive
                    && safeToCancel
                    && !potionFound
                    && projectileClose;
            
            if (shouldBlockProjectiles) {
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
                return 60;
            }
            if (blowingUp == null && !projectileClose) {
                stopShielding(mod);
            }
        }

        if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFalling(mod)
                || !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting()) {
            killAura.stopShielding(mod);
            stopShielding(mod);
            return Float.NEGATIVE_INFINITY;
        }

        // Force field
        doForceField(mod);

        // Dodge projectiles
        if (mod.getPlayer().getHealth() <= 10 && !hasShield(mod)) {

            if (StorageHelper.getNumberOfThrowawayBlocks(mod) > 0 && !mod.getFoodChain().needsToEat()
                    && mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod)) {
                doingFunkyStuff = true;
                setTask(new ProjectileProtectionWallTask(mod));
                return 65;
            }

            runAwayTask = new DodgeProjectilesTask(ARROW_KEEP_DISTANCE_HORIZONTAL, ARROW_KEEP_DISTANCE_VERTICAL);
            setTask(runAwayTask);
            return 65;
        }
        // Dodge all mobs cause we boutta die son
        if (isInDanger(mod) && !escapeDragonBreath(mod) && !mod.getFoodChain().isShouldStop()) {
            if (targetEntity == null || WorldHelper.isSurroundedByHostiles()) {
                runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                setTask(runAwayTask);
                return 70;
            }
        }

        if (mod.getModSettings().shouldDealWithAnnoyingHostiles()) {
            // Deal with hostiles because they are annoying.
            List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();

            List<LivingEntity> toDealWithList = new ArrayList<>();

            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                for (LivingEntity hostile : hostiles) {
                    boolean isRangedOrPoisonous = (hostile instanceof SkeletonEntity
                            || hostile instanceof WitchEntity || hostile instanceof PillagerEntity
                            || hostile instanceof PiglinEntity || hostile instanceof StrayEntity
                            || hostile instanceof CaveSpiderEntity);
                    int annoyingRange = 10;

                    if (isRangedOrPoisonous) {
                        annoyingRange = 20;
                        if (!hasShield(mod)) {
                            annoyingRange = 35;
                        }
                    }

                    // Give each hostile a timer, if they're close for too long deal with them.
                    if (hostile.isInRange(mod.getPlayer(), annoyingRange) && LookHelper.seesPlayer(hostile, mod.getPlayer(), annoyingRange)) {

                        boolean isIgnored = false;
                        for (Class<? extends Entity> ignored : ignoredMobs) {
                            if (ignored.isInstance(hostile)) {
                                isIgnored = true;
                                break;
                            }
                        }

                        // do not go and "attack" these mobs, just hit them if on low HP, or they are close
                        if (isIgnored) {
                            if (mod.getPlayer().getHealth() <= 10) {
                                toDealWithList.add(hostile);
                            }
                        } else {
                            toDealWithList.add(hostile);
                        }
                    }
                }
            }

            // attack entities closest to the player first
            toDealWithList.sort(Comparator.comparingDouble((entity) -> mod.getPlayer().distanceTo(entity)));

            if (!toDealWithList.isEmpty()) {

                // Depending on our weapons/armor, we may choose to straight up kill hostiles if we're not dodging their arrows.
                float bestSwordDamage = getBestSwordDamage(mod);

                int armor = mod.getPlayer().getArmor();
                float damage = bestSwordDamage;

                int shield = hasShield(mod) && bestSwordDamage > 0 ? 3 : 0;

                int canDealWith = (int) Math.ceil((armor * 3.6 / 20.0) + (damage * 0.8) + (shield));
                int dangerousness = getDangerousnessScore(toDealWithList);


                if (canDealWith >= dangerousness || needsChangeOnAttack) {
                    // we just decided to attack, so we should either get it, or hit something before running away again
                    if (!(mainTask instanceof KillEntitiesTask)) {
                        needsChangeOnAttack = true;
                    }

                    // We can deal with it.
                    runAwayTask = null;
                    Entity toKill = toDealWithList.get(0);
                    lockedOnEntity = toKill;

                    setTask(new KillEntitiesTask(toKill.getClass()));
                    return 65;
                } else {
                    // We can't deal with it
                    runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                    setTask(runAwayTask);
                    return 80;
                }
            }
        }
        // By default, if we aren't "immediately" in danger but were running away, keep
        // running away until we're good.
        if (runAwayTask != null && !runAwayTask.isFinished()) {
            setTask(runAwayTask);
            return cachedLastPriority;
        } else {
            runAwayTask = null;
        }

        if (needsChangeOnAttack && lockedOnEntity != null && lockedOnEntity.isAlive()) {
            setTask(new KillEntitiesTask(lockedOnEntity.getClass()));
            return 65;
        } else {
            needsChangeOnAttack = false;
            lockedOnEntity = null;
        }

        return 0;
    }

    private static boolean hasShield(AltoClef mod) {
        return mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
    }

    private static float getBestSwordDamage(AltoClef mod) {
        Item[] SWORDS = new Item[]{Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD,
                Items.STONE_SWORD, Items.WOODEN_SWORD};

        float bestDamage = 0;
        for (Item item : SWORDS) {
            if (mod.getItemStorage().hasItem(item)) {
                float damage = 0;
//#if MC >= 12111
                // In 1.21.11+, swords are Items with attribute modifiers.
                ItemStack stack = new ItemStack(item);
                AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
                damage = (float) modifiers.applyOperations(EntityAttributes.ATTACK_DAMAGE, 0.0, EquipmentSlot.MAINHAND);
//#else
//$$                // In older versions, we can cast to SwordItem and get the material's attack damage.
//$$                if (item instanceof SwordItem sword) {
//$$                    damage = sword.getMaterial().getAttackDamage() + 1;
//$$                }
//#endif
                if (damage > bestDamage) {
                    bestDamage = damage;
                }
                break; // We break at the first (best) sword we have, because the array is ordered by best to worst.
            }
        }
        return bestDamage;
    }

    private BlockPos isInsideFireAndOnFire(AltoClef mod) {
        boolean onFire = mod.getPlayer().isOnFire();
        if (!onFire) return null;
        BlockPos p = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = new BlockPos[]{
                p,
                p.add(1,0,0),
                p.add(1,0,-1),
                p.add(0,0,-1),
                p.add(-1,0,-1),
                p.add(-1,0,0),
                p.add(-1,0,1),
                p.add(0,0,1),
                p.add(1,0,1)
        };
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof AbstractFireBlock) {
                return check;
            }
        }
        return null;
    }

    private void putOutFire(AltoClef mod, BlockPos pos) {
        Optional<Rotation> reach = LookHelper.getReach(pos);
        if (reach.isPresent()) {
            Baritone b = mod.getClientBaritone();
            if (LookHelper.isLookingAt(mod, pos)) {
                b.getPathingBehavior().requestPause();
                b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                return;
            }
            LookHelper.lookAt(reach.get());
        }
    }

    private void doForceField(AltoClef mod) {
        killAura.tickStart();

        // Hit all hostiles close to us.
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            for (Entity entity : entities) {
                boolean shouldForce = false;
                if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
                if (entity instanceof MobEntity) {
                    if (EntityHelper.isProbablyHostileToPlayer(mod, entity)) {
                        if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10)) {
                            shouldForce = true;
                        }
                    }
                } else if (entity instanceof FireballEntity) {
                    // Ghast ball
                    shouldForce = true;
                }

                if (shouldForce) {
                    killAura.applyAura(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        killAura.tickEnd(mod);
    }


    private CreeperEntity getClosestFusingCreeper(AltoClef mod) {
        double worstSafety = Float.POSITIVE_INFINITY;
        CreeperEntity target = null;
        try {
            List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
            for (CreeperEntity creeper : creepers) {
                if (creeper == null) continue;
//#if MC >= 12111
                if (creeper.getLerpedFuseTime(1.0F) < 0.001) continue;
//#else
//$$                if (creeper.getClientFuseTime(1) < 0.001) continue;
//#endif

                // We want to pick the closest creeper, but FIRST pick creepers about to blow
                // At max fuse, the cost goes to basically zero.
//#if MC >= 12111
                double safety = getCreeperSafety(mod.getPlayer().getEntityPos(), creeper);
//#else
//$$                double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
//#endif
                if (safety < worstSafety) {
                    target = creeper;
                }
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            // IDK why but these exceptions happen sometimes. It's extremely bizarre and I
            // have no idea why.
            return target;
        }
        return target;
    }

    private boolean isProjectileClose(AltoClef mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
        try {
            for (CachedProjectile projectile : projectiles) {
//#if MC >= 12111
                if (projectile.position.squaredDistanceTo(mod.getPlayer().getEntityPos()) < 150) {
//#else
//$$                if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150) {
//#endif
                    boolean isGhastBall = projectile.projectileType == FireballEntity.class;
                    if (isGhastBall) {
                        Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(FireballEntity.class);
                        Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(GhastEntity.class);
                        if (ghastBall.isPresent() && ghast.isPresent() && runAwayTask == null
                                && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, ghast.get().getEyePos());
                        }
                        return false;
                        // Ignore ghast balls
                    }
                    if (projectile.projectileType == DragonFireballEntity.class) {
                        // Ignore dragon fireballs
                        continue;
                    }
                    if (projectile.projectileType == ArrowEntity.class || projectile.projectileType == SpectralArrowEntity.class || projectile.projectileType == SmallFireballEntity.class) {
                        // check if the projectile is going away from us
                        // not so fancy math... this should work better than the previous approach (I hope just adding the velocity doesn't cause any issues..)
                        PlayerEntity player = mod.getPlayer();
                        if (player.squaredDistanceTo(projectile.position) < player.squaredDistanceTo(projectile.position.add(projectile.velocity))) {
                            continue;
                        }
                    }

                    Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

//#if MC >= 12111
                    Vec3d delta = mod.getPlayer().getEntityPos().subtract(expectedHit);
//#else
//$$                    Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);
//#endif

                    double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                    double verticalDistance = Math.abs(delta.y);
                    if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL
                            && verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL) {
                        if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
                                && hasShield(mod)) {
                            mod.getClientBaritone().getPathingBehavior().requestPause();
                            LookHelper.lookAt(mod, projectile.position.add(0, 0.3, 0));
                        }
                        return true;
                    }
                }
            }

        } catch (ConcurrentModificationException e) {
        }

        // TODO refactor this into something more reliable for all mobs
        for (SkeletonEntity skeleton : mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class)) {
            if (skeleton.distanceTo(mod.getPlayer()) > 10 || !skeleton.canSee(mod.getPlayer())) continue;

            // when the skeleton is about to shoot (it takes 5 ticks to raise the shield)
            if (skeleton.getItemUseTime() > 15) {
                return true;
            }
        }

        return false;
    }

    private Optional<Entity> getUniversallyDangerousMob(AltoClef mod) {
        // Wither skeletons are dangerous because of the wither effect. Oof kinda obvious.
        // If we merely force field them, we will run into them and get the wither effect which will kill us.

        Class<?>[] dangerousMobs = new Class[]{Entities.WARDEN, WitherEntity.class, WitherSkeletonEntity.class,
                HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class};

        double range = SAFE_KEEP_DISTANCE - 2;

        for (Class<?> dangerous : dangerousMobs) {
            Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(dangerous);

            if (entity.isPresent()) {
                if (entity.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, entity.get())) {
                    return entity;
                }
            }
        }

        return Optional.empty();
    }

    private boolean isInDanger(AltoClef mod) {
        boolean witchNearby = mod.getEntityTracker().entityFound(WitchEntity.class);

        float health = mod.getPlayer().getHealth();
        if (health <= 10 && !witchNearby) {
            return true;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER) ||
                (mod.getPlayer().hasStatusEffect(StatusEffects.POISON) && !witchNearby)) {
            return true;
        }
        if (WorldHelper.isVulnerable()) {
            // If hostile mobs are nearby...
            try {
                ClientPlayerEntity player = mod.getPlayer();
                List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();

                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity entity : hostiles) {
//#if MC >= 12111
                        if (entity.isInRange(player, SAFE_KEEP_DISTANCE)
//#else
//$$                        if (entity.isInRange(player, SAFE_KEEP_DISTANCE)
//#endif
                                && !mod.getBehaviour().shouldExcludeFromForcefield(entity)
                                && EntityHelper.isAngryAtPlayer(mod, entity)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public void setTargetEntity(Entity entity) {
        targetEntity = entity;
    }

    public void resetTargetEntity() {
        targetEntity = null;
    }

    public void setForceFieldRange(double range) {
        killAura.setRange(range);
    }

    public void resetForceField() {
        killAura.setRange(Double.POSITIVE_INFINITY);
    }

    public boolean isDoingAcrobatics() {
        return doingFunkyStuff;
    }

    public boolean isPuttingOutFire() {
        return wasPuttingOutFire;
    }

    @Override
    public boolean isActive() {
        // We're always checking for mobs
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // Task is done, so I guess we move on?
    }

    @Override
    public String getName() {
        return "Mob Defense";
    }
}
