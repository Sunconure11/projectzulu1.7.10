package com.stek101.projectzulu.common.mobs.entity;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.stek101.projectzulu.common.api.CustomEntityList;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.mobs.EntityAFightorFlight;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIAttackOnCollide;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIFlyingWander;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIHurtByTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAINearestAttackableTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIStayStill;

public class EntityPZBat extends EntityGenericAnimal implements IAnimals {
    int stayStillTimer = 0;
	private EntityAFightorFlight EAFF;
	private float aggroLevel;
	private double aggroRange;
	boolean manyBats = false;
    //private int maxTargetHealthToAttack = (Integer) CustomEntityList.getByEntity(this).modData.get().customData
    //        .get("maxTargetHealth");
    float curiosity = 0;
    int ticksToCheckAbilities = 3;

    public EntityPZBat(World par1World) {
        super(par1World);
        // noClip = true;
        setSize(1.0f, 1.4f);

        float moveSpeed = 0.22f;
        CustomEntityList entityEntry = CustomEntityList.getByName(EntityList.getEntityString(this));
        if (entityEntry != null && entityEntry.modData.get().entityProperties != null) {
            // TODO: Switch to this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue()???
            moveSpeed = entityEntry.modData.get().entityProperties.moveSpeed;
            this.aggroLevel = entityEntry.modData.get().entityProperties.aggroLevel;
            this.aggroRange = entityEntry.modData.get().entityProperties.aggroRange;                    
        }
        
  	  if (Math.round(this.aggroRange) != 0) {
          EAFF = new EntityAFightorFlight().setEntity(this, worldObj, this.aggroLevel, this.aggroRange);
    	  }

        this.maxFlightHeight = 7;
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(2, new EntityAIStayStill(this, EntityStates.posture));
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, 1.0f, false));
        this.tasks.addTask(4, new EntityAIFlyingWander(this, (float) moveSpeed));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));

        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, false));
        targetTasks.addTask(2,
                new EntityAINearestAttackableTarget(this, EnumSet.of(EntityStates.attacking, EntityStates.looking),
                        EntityPlayer.class, 16.0F, 0, true));
    }

    @Override
    public boolean defaultGrounded() {
        return false;
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    @Override
    protected void fall(float par1) {
    }

    /**
     * Takes in the distance the entity has fallen this tick and whether its on the ground to update the fall distance
     * and deal fall damage if landing on the ground. Args: distanceFallenThisTick, onGround
     */
    @Override
    protected void updateFallState(double par1, boolean par3) {
    }
    
    @Override
    public int getTotalArmorValue() {
        return 1;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "pzbathurtsound";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "pzbatlivingsound";
    }
    
    @Override
    public int getTalkInterval() {
        return 120;
    }

    @Override
    public void onLivingUpdate() {
    	super.onLivingUpdate();
    	if (Math.round(this.aggroRange) != 0) {
    		EAFF.updateEntityAFF(worldObj);
    	}
    }
    
    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void func_145780_a(int xCoord, int yCoord, int zCoord, Block stepBlock) {
        this.worldObj.playSoundAtEntity(this, "sounds.birdplop", 1.0F, 1.0F);
    }

    @Override
    public boolean isValidBreedingItem(ItemStack itemStack) {
        return false;
    }

   @Override
   protected void updateAITasks() {
       super.updateAITasks();

       if (ticksExisted % ticksToCheckAbilities == 0) {

           /* Check if their is a nearby Player to Follow */
           EntityPlayer nearbyPlayer = this.worldObj.getClosestVulnerablePlayerToEntity(this, 100.0D);
           if (nearbyPlayer != null) {
               int distToTargetXZ = (int) Math.sqrt(Math.pow(nearbyPlayer.posX - this.posX, 2)
                       + Math.pow(nearbyPlayer.posZ - this.posZ, 2));
               if (distToTargetXZ < 16) {
                   curiosity = 140;
               }
           }
           shouldFollow = curiosity > 0 ? true : false;
           curiosity = Math.max(curiosity - ticksToCheckAbilities, 0);

           /*
            * Assuming we're following a Player, check if We Should Attack by Comparing number of Nearby Vultures to
            * the Health of our Target
            */
           Entity targetedEntity = nearbyPlayer;
           if (curiosity > 0 && targetedEntity != null) {
               int nearbyVultures = 0;
               AxisAlignedBB var15 = this.boundingBox.copy();
               var15 = var15.expand(10, 10, 10);
               List nearbyEntities = this.worldObj.getEntitiesWithinAABB(EntityVulture.class, var15);
               if (nearbyEntities != null && !nearbyEntities.isEmpty()) {
                   Iterator var10 = nearbyEntities.iterator();

                   while (var10.hasNext()) {
                       Entity var4 = (Entity) var10.next();
                       if (var4 instanceof EntityPZBat
                               && (((EntityPZBat) var4).getEntityState() == EntityStates.following || ((EntityVulture) var4)
                                       .getEntityState() == EntityStates.attacking)) {
                           nearbyVultures += 1;
                       }
                   }
               }
            //   if (((EntityLivingBase) targetedEntity).getHealth() < maxTargetHealthToAttack
            //           && ((EntityLivingBase) targetedEntity).getHealth() < nearbyVultures * 4) {
            //       setAngerLevel(400);
            //   }
           }
       }
   }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

 //   @Override
 //   protected boolean isValidLightLevel(World world, int xCoord, int yCoord, int zCoord) {
 //       return this.worldObj.getSavedLightValue(EnumSkyBlock.Block, xCoord, yCoord, zCoord) < 1;
 //   }

    @Override
    protected boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord) {
        return worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord);
    }

  //  @Override
  //  protected void dropRareDrop(int par1) {
  //      if (Loader.isModLoaded(DefaultProps.BlocksModId) && BlockList.mobHeads.isPresent()) {
  //          entityDropItem(new ItemStack(BlockList.mobHeads.get(), 1, 0), 1);
  //      }
  //      super.dropRareDrop(par1);
   // }
    
    @Override
    public boolean attackEntityAsMob(Entity entity) {
        boolean success = super.attackEntityAsMob(entity);
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.poison.id, 40, 1));            
            success = true;
        }
        return success;
    }
    
    
}
