package com.stek101.projectzulu.common.mobs.entity;

import java.util.EnumSet;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.stek101.projectzulu.common.api.CustomEntityList;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.mobs.EntityAFightorFlight;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIAttackOnCollide;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIFollowParent;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIHurtByTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIMate;
import com.stek101.projectzulu.common.mobs.entityai.EntityAINearestAttackableTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIPanic;
import com.stek101.projectzulu.common.mobs.entityai.EntityAITempt;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIWander;

public class EntityGiantRat extends EntityGenericAnimal implements IAnimals {
	private EntityAFightorFlight EAFF;
	private CustomEntityList entityEntry;
	private float aggroLevel;
	private double aggroRange;
	
    public EntityGiantRat(World par1World) {
        super(par1World);
        setSize(1.5f, 0.9f);
        
        this.entityEntry = CustomEntityList.getByName(EntityList.getEntityString(this));
        
        /* Check if aggroLevel and aggroRange have valid values to activate AFoF */
    	  if (entityEntry != null && entityEntry.modData.get().entityProperties != null) {
            this.aggroLevel = entityEntry.modData.get().entityProperties.aggroLevel;
            this.aggroRange = entityEntry.modData.get().entityProperties.aggroRange;                    
        }
    	  
    	  if (Math.round(this.aggroRange) != 0) {
          EAFF = new EntityAFightorFlight().setEntity(this, worldObj, this.aggroLevel, this.aggroRange);
    	  }

        getNavigator().setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25f));

        tasks.addTask(3, new EntityAIAttackOnCollide(this, 1.0f, false));
        // tasks.addTask(4, new EntityAIFollowOwner(this, moveSpeed, 10.0F, 2.0F));

        tasks.addTask(5, new EntityAIMate(this, 1.0f));
        tasks.addTask(6, new EntityAITempt(this, 1.2f, Items.spider_eye, false));
        tasks.addTask(7, new EntityAIFollowParent(this, 1.1f));
        tasks.addTask(9, new EntityAIWander(this, 1.0f, 120));

        targetTasks.addTask(3, new EntityAIHurtByTarget(this, false, false));
        targetTasks.addTask(4,
                new EntityAINearestAttackableTarget(this, EnumSet.of(EntityStates.attacking, EntityStates.looking),
                        EntityPlayer.class, 16.0F, 0, true));
        targetTasks.addTask(5,
                new EntityAINearestAttackableTarget(this, EnumSet.of(EntityStates.attacking, EntityStates.looking),
                        EntityLiving.class, 16.0F, 0, false, true, IMob.mobSelector));
        // targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityLiving.class, 16.0F, 0, false, true,
        // IMob.mobSelector));
    }
    
    @Override
    protected void updateAITick() {    	   	
        super.updateAITick();
    }
    
    @Override
    public void onLivingUpdate() {
    	super.onLivingUpdate();
    	if (Math.round(this.aggroRange) != 0) {
    		EAFF.updateEntityAFF(worldObj, Items.spider_eye);
    	}
    }

    @Override
    protected boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord) {
        return worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord);
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
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "gratlivingsound";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "grathurtsound";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "gratdeathsound";
    }
    
    @Override
    public int getTalkInterval() {
        return 160;
    }

    @Override
    public boolean isValidBreedingItem(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() == Items.spider_eye) {
        	this.setAngerLevel(0);
        	this.setFleeTick(0);
            return true;
        } else {
            return super.isValidBreedingItem(itemStack);
        }
    }

}