package com.stek101.projectzulu.common.mobs.entity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.stek101.projectzulu.common.ProjectZulu_Core;
import com.stek101.projectzulu.common.api.CustomEntityList;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.core.packets.PZPacketTameParticle;
import com.stek101.projectzulu.common.mobs.EntityAFightorFlight;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIAttackOnCollide;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIFollowParent;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIHurtByTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIMate;
import com.stek101.projectzulu.common.mobs.entityai.EntityAINearestAttackableTarget;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIPanic;
import com.stek101.projectzulu.common.mobs.entityai.EntityAITempt;
import com.stek101.projectzulu.common.mobs.entityai.EntityAIWander;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class EntityCamel extends EntityGenericAnimal implements IInvBasic {
	
	private EntityAFightorFlight EAFF;
	private CustomEntityList entityEntry;
	private float aggroLevel;
	private double aggroRange;
	public boolean camelSaddled; 
	public AnimalChest camelChest;
	private static final int[] armorValues = new int[] {0, 5, 7, 11};
	private boolean animalChested;
	protected float jumpPower = 0.0f;
	protected boolean mountJumping;
	public boolean mountJump = false;
	private World parWorld;
	private int field_110285_bP;
		
	public EntityCamel(World par1World) {
        super(par1World);
        setSize(1.6f, 1.8f);
        this.setChested(false);
        this.setSaddled(false);
        this.camelSaddled = false;
        this.parWorld = par1World;
        this.setPosition(this.posX, this.posY, this.posZ);
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
        tasks.addTask(1, new EntityAIPanic(this, 1.0f));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0f, false));       
        tasks.addTask(3, new EntityAILookIdle(this));
        tasks.addTask(5, new EntityAIMate(this, 0.8f));
        tasks.addTask(6, new EntityAITempt(this, 0.9f, Items.water_bucket, false));
        tasks.addTask(7, new EntityAIFollowParent(this, 0.9f));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(9, new EntityAIWander(this, 0.8f, 120));

        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, false));
        targetTasks.addTask(2,
                new EntityAINearestAttackableTarget(this, EnumSet.of(EntityStates.attacking, EntityStates.looking),
                        EntityPlayer.class, 16.0F, 0, true));
        this.createCamelChest();
    }
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(22, Integer.valueOf(0)); //mount armor
        this.dataWatcher.addObject(26, Integer.valueOf(0)); //chested aka saddle bags
    }
	
    @Override
    protected boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord) {
        return worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord);
    }
    
    @Override
    public void onLivingUpdate() {
    	super.onLivingUpdate();
    	if (Math.round(this.aggroRange) != 0) {
    		EAFF.updateEntityAFF(worldObj, Items.water_bucket);
    	}
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "camellivingsound";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        return DefaultProps.mobKey + ":" + DefaultProps.entitySounds + "camelhurtsound";
    }
    
    @Override
    public int getTalkInterval() {
        return 160;
    }

    @Override
    public boolean isValidBreedingItem(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() == Item.getItemFromBlock(Blocks.leaves)) {
        	this.setAngerLevel(0);
        	this.setFleeTick(0);
            return true;
            
        } else {
            return super.isValidBreedingItem(itemStack);
        }
    }
    
    @Override
    public boolean isValidTamingItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getItem() == Items.water_bucket) {
            return true;
        }
        return super.isValidTamingItem(itemStack);
    }
    
    @Override
    public boolean isRideable() {
        return true;
    }
    
    private void createCamelChest()
    {
        AnimalChest animalchest = this.camelChest;
        this.camelChest = new AnimalChest("CamelChest", 17);
        this.camelChest.func_110133_a(this.getCommandSenderName());

        if (animalchest != null)
        {   
            animalchest.func_110132_b((IInvBasic) this);
            int i = Math.min(animalchest.getSizeInventory(), this.camelChest.getSizeInventory());

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = animalchest.getStackInSlot(j);

                if (itemstack != null)
                {
                    this.camelChest.setInventorySlotContents(j, itemstack.copy());
                }
            }
            animalchest = null;
        }

        this.camelChest.func_110134_a((IInvBasic) this);
    }
    
    public void func_146086_d(ItemStack p_146086_1_)
    {
        this.dataWatcher.updateObject(22, Integer.valueOf(this.getCamelArmorIndex(p_146086_1_)));
        //this.func_110230_cF();
    }
    
    /**
     * 0 = iron, 1 = gold, 2 = diamond
     */
    private int getCamelArmorIndex(ItemStack p_110260_1_)
    {
        if (p_110260_1_ == null)
        {
            return 0;
        }
        else
        {
            Item item = p_110260_1_.getItem();
            return item == Items.iron_horse_armor ? 1 : (item == Items.golden_horse_armor ? 2 : (item == Items.diamond_horse_armor ? 3 : 0));
        }
    }
    
    public void setCamelSaddled(boolean p_110251_1_)
    {
        //this.setHorseWatchableBoolean(4, p_110251_1_);
    	this.setSaddled(true);
    }
    
    /**
     * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
     */
    @Override
    public void onInventoryChanged(InventoryBasic p_76316_1_)
    {
        int i = this.getMountArmorValue();
        boolean flag = this.isCamelSaddled();
        //this.func_110232_cE();

        if (this.ticksExisted > 20)
        {
        	if (!(p_76316_1_.getStackInSlot(0) == null)) {
        		if (p_76316_1_.getStackInSlot(0).getItem() ==Items.saddle){
                	this.setSaddled(true);
                	this.playSound("mob.horse.leather", 0.5F, 1.0F);
        		}        		
        	}else
    		{
    			this.setSaddled(false);
    		}
        	
            if (i == 0 && i != this.getMountArmorValue())
            {
                this.playSound("mob.horse.armor", 0.5F, 1.0F);
            }
            else if (i != this.getMountArmorValue())
            {
                this.playSound("mob.horse.armor", 0.5F, 1.0F);
            }
        }
    }
    
    public boolean isCamelSaddled()
    {
       return this.getSaddled();
    }
    
/*    public boolean isChested()
    {
        return this.animalChested;
    }*/
    
    public int getMountArmorValue()
    {
        return this.dataWatcher.getWatchableObjectInt(22);
    } 

	
/*	public EntityGenericAgeable createChild(EntityGenericAgeable p_90011_1_) {
		//EntityCamel entity = (EntityCamel)p_90011_1_;
        EntityCamel entityCamel1 = new EntityCamel(this.worldObj);
        return entityCamel1;
		//return null;
	}*/
	
    /**
     * Called when the entity is attacked.
     */
	@Override
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        Entity entity = p_70097_1_.getEntity();
        return this.riddenByEntity != null && this.riddenByEntity.equals(entity) ? false : super.attackEntityFrom(p_70097_1_, p_70097_2_);
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
	@Override
    public int getTotalArmorValue()
    {
        //return armorValues[this.getMountArmorValue()];
		return 2;
    }

    public void dropChests()
    {
        if (!this.worldObj.isRemote && this.isChested())
        {
            this.dropItem(Item.getItemFromBlock(Blocks.chest), 1);
            this.setChested(false);
        }
    }
    
 /*   public void setChested(boolean p_110207_1_)
    {
        this.animalChested = p_110207_1_;
    }*/
    
    @Override
    public boolean shouldNotifySimilar(EntityPlayer attackingPlayer) {
        return true;
    }
    
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
    public boolean interact(EntityPlayer p_70085_1_)
    {
     ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();
     
      if(this.isTamed()){    	 
        if (itemstack != null && itemstack.getItem() == Items.spawn_egg)
        {
            return super.interact(p_70085_1_);
        }
        
        else
	      if (this.isAdultCamel() && p_70085_1_.isSneaking())
	        {
	            this.openGUI(p_70085_1_);
	            return true;
	        }
	        else if (this.isAdultCamel() && this.riddenByEntity != null)
	        {
	        	return super.interact(p_70085_1_);
	        }
	        else if (itemstack != null && this.isValidBreedingItem(itemstack))
	        {
	        	return super.interact(p_70085_1_);
	        }
        else
        {
            if (itemstack != null)
            {
                boolean flag = false;

               // if (this.func_110259_cr())
               // {
                    byte b0 = -1;
                    
                    if (itemstack.getItem() == Items.stick)
                    {
                        b0 = 1;
                    }

                  /*  if (itemstack.getItem() == Items.iron_horse_armor)
                    {
                        b0 = 1;
                    }
                    else if (itemstack.getItem() == Items.golden_horse_armor)
                    {
                        b0 = 2;
                    }
                    else if (itemstack.getItem() == Items.diamond_horse_armor)
                    {
                        b0 = 3;
                    }*/

                    if (b0 >= 0)
                    {
                        this.openGUI(p_70085_1_);
                        return true;
                    }
              //  }

                if (!flag )
                {
                    float f = 0.0F;
                    short short1 = 0;
                    byte b1 = 0;

                    if (itemstack.getItem() == Items.wheat)
                    {
                        f = 2.0F;
                        short1 = 60;
                        b1 = 3;
                    }
                    else if (itemstack.getItem() == Items.sugar)
                    {
                        f = 1.0F;
                        short1 = 30;
                        b1 = 3;
                    }
                    else if (itemstack.getItem() == Items.bread)
                    {
                        f = 7.0F;
                        short1 = 180;
                        b1 = 3;
                    }
                    else if (Block.getBlockFromItem(itemstack.getItem()) == Blocks.hay_block)
                    {
                        f = 20.0F;
                        short1 = 180;
                    }
                    else if (itemstack.getItem() == Items.apple)
                    {
                        f = 3.0F;
                        short1 = 60;
                        b1 = 3;
                    }
                    else if (itemstack.getItem() == Items.golden_carrot)
                    {
                        f = 4.0F;
                        short1 = 60;
                        b1 = 5;

                        if (this.isTamed() && this.getGrowingAge() == 0)
                        {
                            flag = true;
                            //this.func_146082_f(p_70085_1_);
                        }
                    }
                    else if (itemstack.getItem() == Items.golden_apple)                    {
                    	
                        f = 10.0F;
                        short1 = 240;
                        b1 = 10;

                        if (this.isTamed() && this.getGrowingAge() == 0)
                        {
                            flag = true;
                            //this.func_146082_f(p_70085_1_);
                        }
                    }

                    if (this.getHealth() < this.getMaxHealth() && f > 0.0F)
                    {
                        this.heal(f);
                        flag = true;
                    }

                    /*if (!this.isAdultHorse() && short1 > 0)
                    {
                        this.addGrowth(short1);
                        flag = true;
                    }*/

                    /*if (b1 > 0 && (flag || !this.isTamed()))
                    {
                        flag = true;
                        this.increaseTemper(b1);
                    }*/

                    if (flag)
                    {
                    	this.worldObj.playSoundAtEntity(this, "eating", 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                    }
                }

                if (!flag && !this.isChested() && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
                {
                    this.setChested(true);
                    this.playSound("mob.chickenplop", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                    flag = true;
                    this.createCamelChest();
                }

                if (!flag && this.func_110253_bW() && !this.isCamelSaddled() && itemstack.getItem() == Items.saddle)
                {
                    this.openGUI(p_70085_1_);
                    return true;
                }

                if (flag)
                {
                    if (!p_70085_1_.capabilities.isCreativeMode && --itemstack.stackSize == 0)
                    {
                        p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, (ItemStack)null);
                    }

                    return true;
                }
            }

            if (this.func_110253_bW() && this.riddenByEntity == null)
            {
                if (itemstack != null && itemstack.interactWithEntity(p_70085_1_, this))
                {
                    return true;
                }
                else
                {
                    this.func_110237_h(p_70085_1_);
                    return true;
                }
            }
            else
            {
                return super.interact(p_70085_1_);
            }
        }
    } 
    else if (itemstack != null && itemstack.getItem() == Items.saddle )
    {
    	return false;
    }
    else if (itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
    {
    	return false;
    }      
    else if(itemstack != null && this.isValidTamingItem(itemstack)) {
		if (!p_70085_1_.capabilities.isCreativeMode){
			p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem,
                        new ItemStack(Items.bucket));
		}

		if (itemstack.stackSize <= 0){
			p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, (ItemStack)null);
		}

		if(!worldObj.isRemote){
			boolean tameEffectSuccess = false;
			
			if(rand.nextInt(3) == 0){
				this.setTamed(true);
				this.setAttackTarget((EntityLiving)null);
                this.setHealth(getMaxHealth());
				this.setOwner(p_70085_1_.getCommandSenderName());
				setEntityTamed(getDefaultEntityName());
				tameEffectSuccess = true;
			}
			
			/* Send Tame Effect Packet */
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			/* Write PacketID into Packet */
			try {
				data.writeInt(5);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/* Write Temperature Into Packet */
            try {
                data.writeInt(getEntityId());
                data.writeBoolean(tameEffectSuccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
              PZPacketTameParticle message = new PZPacketTameParticle().setPacketData(getEntityId(), tameEffectSuccess); 
              ProjectZulu_Core.packetHandler.sendToAllAround(message, new TargetPoint(dimension, posX, posY, posZ, 10));
		}  
		return true;
      }
	  	if(super.interact(p_70085_1_)){
			return true;
	  	}
	  	
      return false;
    }
    
    public boolean isAdultCamel()
    {
        return !this.isChild();
    }
    
    public void openGUI(EntityPlayer player)
    {
        if (!this.worldObj.isRemote && (this.riddenByEntity == null || this.riddenByEntity == player) && this.isTamed())
        {
            this.camelChest.func_110133_a(this.getCommandSenderName());
            if (player.inventory == null){
            }
            
            if (this.camelChest == null) {
            }
            player.openGui(ProjectZulu_Core.modInstance, 5, player.worldObj, this.getEntityId(), 0, 0);
        }
    }
    
    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getCommandSenderName()
    {
        //if (this.hasCustomNameTag())
        //{
            return this.getCustomNameTag();
        //}
       
    }
    
    public boolean func_110253_bW()
    {
        return this.isAdultCamel();
    }

    private void func_110237_h(EntityPlayer p_110237_1_)
    {
        p_110237_1_.rotationYaw = this.rotationYaw;
        p_110237_1_.rotationPitch = this.rotationPitch;

        if (!this.worldObj.isRemote)
        {
            p_110237_1_.mountEntity(this);
        }
    }
    
    public static boolean func_146085_a(Item p_146085_0_)
    {
        return p_146085_0_ == Items.iron_horse_armor || p_146085_0_ == Items.golden_horse_armor || p_146085_0_ == Items.diamond_horse_armor;
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
   @Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        super.writeEntityToNBT(p_70014_1_);
        p_70014_1_.setBoolean("ChestedCamel", this.isChested());

        if (this.isChested())
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (int i = 2; i < this.camelChest.getSizeInventory(); ++i)
            {
                ItemStack itemstack = this.camelChest.getStackInSlot(i);

                if (itemstack != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("Slot", (byte)i);
                    itemstack.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }

            p_70014_1_.setTag("Items", nbttaglist);
        }

        if (this.camelChest.getStackInSlot(1) != null)
        {
            p_70014_1_.setTag("ArmorItem", this.camelChest.getStackInSlot(1).writeToNBT(new NBTTagCompound()));
        }

        if (this.camelChest.getStackInSlot(0) != null)
        {
            p_70014_1_.setTag("SaddleItem", this.camelChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }
    }
    
    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        super.readEntityFromNBT(p_70037_1_);        
        this.setChested(p_70037_1_.getBoolean("ChestedCamel"));

        if (this.isChested())
        {
            NBTTagList nbttaglist = p_70037_1_.getTagList("Items", 10);
            this.createCamelChest();

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j >= 2 && j < this.camelChest.getSizeInventory())
                {
                    this.camelChest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
                }
            }
        }

        ItemStack itemstack;

    /*    if (p_70037_1_.hasKey("ArmorItem", 10))
        {
            itemstack = ItemStack.loadItemStackFromNBT(p_70037_1_.getCompoundTag("ArmorItem"));

            if (itemstack != null && func_146085_a(itemstack.getItem()))
            {
                this.camelChest.setInventorySlotContents(1, itemstack);
            }
        }*/

        if (p_70037_1_.hasKey("SaddleItem", 10))
        {
            itemstack = ItemStack.loadItemStackFromNBT(p_70037_1_.getCompoundTag("SaddleItem"));

            if (itemstack != null && itemstack.getItem() == Items.saddle)
            {
                this.camelChest.setInventorySlotContents(0, itemstack);
            }
        }
        else if (p_70037_1_.getBoolean("Saddle"))
        {
            this.camelChest.setInventorySlotContents(0, new ItemStack(Items.saddle));
        }
    }
    
    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
  @Override
    public void moveEntityWithHeading(float moveStrafe, float moveForward)
    {   
    	
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLivingBase 
        		&& this.getSaddled())
        {   
        	// Apply Rider Movement:
        	EntityLivingBase rider = (EntityLivingBase)this.riddenByEntity;
        	
        	this.prevRotationYaw = this.rotationYaw = rider.rotationYaw;
        	this.rotationPitch = rider.rotationPitch * 0.5F;
        	this.setRotation(this.rotationYaw, this.rotationPitch);
        	this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
        	moveStrafe = ((EntityLivingBase)rider).moveStrafing * 0.5F;
        	moveForward = ((EntityLivingBase)rider).moveForward;
        	
        	// Jumping Controls:
        	if(!this.isMountJumping() && this.onGround) {
    	    	if(this.riddenByEntity instanceof EntityPlayer) {
    	    		EntityPlayer player = (EntityPlayer)this.riddenByEntity;
    	    		//ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
    	    		if(this.mountJump)
    	    		 this.startJumping();
    	    	}
        	}
        	
        	// Jumping Behaviour:
        	if(this.getJumpPower() > 0.0F && !this.isMountJumping() && this.onGround) {
    			this.motionY = this.getMountJumpHeight() * (double)this.getJumpPower();
                if(this.isPotionActive(Potion.jump))
                    this.motionY += (double)((float)(this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
                this.setMountJumping(true);
                this.isAirBorne = true;
                if(moveForward > 0.0F) {
                    float f2 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
                    float f3 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
                    this.motionX += (double)(-0.4F * f2 * this.jumpPower);
                    this.motionZ += (double)(0.4F * f3 * this.jumpPower);
                }
                if(!this.worldObj.isRemote)
                	this.playSound("mob.horse.jump", 0.4F, 1.0F);
                this.setJumpPower(0);
            }
            //this.jumpMovementFactor = (float)(this.getAIMoveSpeed() * this.getGlideScale());
            this.stepHeight = 1.0F;
            this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
            
         // Apply Movement:
            if(!this.worldObj.isRemote) {            	
               this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
                super.moveEntityWithHeading(moveStrafe, moveForward);
            }
            
            // Clear Jumping:
            if(this.onGround) {
                this.setJumpPower(0);
                this.setMountJumping(false);
                this.mountJump = false;
            }
        	
        	// Animate Limbs:
        	this.prevLimbSwingAmount = this.limbSwingAmount;
            double d0 = this.posX - this.prevPosX;
            double d1 = this.posZ - this.prevPosZ;
            float f4 = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;
            if (f4 > 1.0F)
                f4 = 1.0F;
            this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
            this.limbSwing += this.limbSwingAmount;
        }
        else
        {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            super.moveEntityWithHeading(moveStrafe, moveForward);
        }
    }
    
    /**
     * returns true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
     * by a player and the player is holding a carrot-on-a-stick
     */
    @Override
    public boolean canBeSteered()
    {
        return false;
    }
    
    /**
     * Called when the mob's health reaches 0.
     */
    @Override
    public void onDeath(DamageSource p_70645_1_)
    {
        super.onDeath(p_70645_1_);

        if (!this.worldObj.isRemote)
        {
            this.dropChestItems();
        }
    }
    
    public void dropChestItems()
    {
        this.dropItemsInChest(this, this.camelChest);
        this.dropChests();
    }
    
    private void dropItemsInChest(Entity p_110240_1_, AnimalChest p_110240_2_)
    {
        if (p_110240_2_ != null && !this.worldObj.isRemote)
        {
            for (int i = 0; i < p_110240_2_.getSizeInventory(); ++i)
            {
                ItemStack itemstack = p_110240_2_.getStackInSlot(i);

                if (itemstack != null)
                {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }
   
    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return this.riddenByEntity == null;
    }
    
    @Override
    public double getMountedYOffset() {
        return 2.6f;
    }
    
    // ========== Jumping Start ==========
    public void startJumping() {
    	this.setJumpPower();
    }
    
    // ========== Jump Power ==========
    public void setJumpPower(int power) {
    	if(power < 0)
    		power = 0;
    	if(power > 99)
    		power = 99;
    	if(power < 90)
            this.jumpPower = 1.0F * ((float)power / 89.0F);
    	else
        	this.jumpPower = 1.0F + (1.0F * ((float)(power - 89) / 10.0F));
    }
    
    public void setJumpPower() {
    	this.setJumpPower(89);
    }
    
    public float getJumpPower() {
    	return this.jumpPower;
    }
    
    // ========== Gliding ==========
    public double getGlideScale() {
    	return 0.1F;
    }
    
    // ========== Jumping ==========
    public double getMountJumpHeight() {
    	return 0.5D;
    }
    
    public boolean isMountJumping() {
    	return this.mountJumping;
    }
    
    public void setMountJumping(boolean set) {
    	this.mountJumping = set;
    }
    
    public void setMountJump(){
    	this.mountJump = true;
    }
    
    private boolean getCamelWatchableBoolean(int p_110233_1_)
    {
        return (this.dataWatcher.getWatchableObjectInt(26) & p_110233_1_) != 0;
    }
    
    private void setCamelWatchableBoolean(int p_110208_1_, boolean p_110208_2_)
    {
        int j = this.dataWatcher.getWatchableObjectInt(26);

        if (p_110208_2_)
        {
            this.dataWatcher.updateObject(26, Integer.valueOf(j | p_110208_1_));
        }
        else
        {
            this.dataWatcher.updateObject(26, Integer.valueOf(j & ~p_110208_1_));
        }
    }
    
    public boolean isChested()
    {
        return this.getCamelWatchableBoolean(8);
    }
    
    public void setChested(boolean p_110207_1_)
    {
        this.setCamelWatchableBoolean(8, p_110207_1_);
    }
    
    public void riderDismount(){
    	 if (!this.worldObj.isRemote)
         {
             if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayerMP)
             {
                 EntityPlayerMP entityplayermp = (EntityPlayerMP)this.riddenByEntity;
                 entityplayermp.setPositionAndUpdate(this.posX + 0.3D, this.posY, this.posZ);

             }
         }
    }
 
}