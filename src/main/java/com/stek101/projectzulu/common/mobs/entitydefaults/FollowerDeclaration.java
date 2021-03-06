package com.stek101.projectzulu.common.mobs.entitydefaults;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import com.stek101.projectzulu.common.api.CustomMobData;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.core.entitydeclaration.CreatureDeclaration;
import com.stek101.projectzulu.common.mobs.entity.EntityFollower;
import com.stek101.projectzulu.common.mobs.models.ModelFollower;
import com.stek101.projectzulu.common.mobs.renders.RenderGenericLiving;
import com.stek101.projectzulu.common.mobs.renders.RenderWrapper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FollowerDeclaration extends CreatureDeclaration {

    public FollowerDeclaration() {
        super("Follower", 41, EntityFollower.class, null);
        setRegistrationProperties(128, 3, true);
    }

    @Override
    public void loadCreaturesFromConfig(Configuration config) {
    }

    @Override
    public void outputDataToList(Configuration config, CustomMobData customMobData) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderWrapper getEntityrender(Class<? extends EntityLivingBase> entityClass) {
        return new RenderGenericLiving(new ModelFollower(), 0.5f, new ResourceLocation(DefaultProps.mobKey,
                "textures/serpent.png"));
    }
}
