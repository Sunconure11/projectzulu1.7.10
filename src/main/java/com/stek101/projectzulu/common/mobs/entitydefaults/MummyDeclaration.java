package com.stek101.projectzulu.common.mobs.entitydefaults;

import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;

import com.stek101.projectzulu.common.api.CustomMobData;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.core.entitydeclaration.EntityProperties;
import com.stek101.projectzulu.common.core.entitydeclaration.SpawnableDeclaration;
import com.stek101.projectzulu.common.mobs.entity.EntityMummy;
import com.stek101.projectzulu.common.mobs.models.ModelMummy;
import com.stek101.projectzulu.common.mobs.renders.RenderGenericLiving;
import com.stek101.projectzulu.common.mobs.renders.RenderWrapper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MummyDeclaration extends SpawnableDeclaration {

    public MummyDeclaration() {
        super("Mummy", 5, EntityMummy.class, EnumCreatureType.monster);
        setSpawnProperties(5, 10, 1, 2);
        setRegistrationProperties(128, 3, true);
        setDropAmount(0, 2);

        eggColor1 = (255 << 16) + (255 << 8) + 255;
        eggColor2 = (255 << 16) + (255 << 8) + 255;
    }

    @Override
    public void outputDataToList(Configuration config, CustomMobData customMobData) {
        customMobData.entityProperties = new EntityProperties(16f, 3.0f, 0.25f).createFromConfig(config, mobName);
        super.outputDataToList(config, customMobData);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderWrapper getEntityrender(Class<? extends EntityLivingBase> entityClass) {
        return new RenderGenericLiving(new ModelMummy(), 0.5f, new ResourceLocation(DefaultProps.mobKey, 
        		"textures/mummy.png"));
    }

    @Override
    public HashSet<String> getDefaultBiomesToSpawn() {
        HashSet<String> defaultBiomesToSpawn = new HashSet<String>();
        defaultBiomesToSpawn.add(BiomeGenBase.desert.biomeName);
        defaultBiomesToSpawn.add(BiomeGenBase.desertHills.biomeName);        
        return defaultBiomesToSpawn;
    }
}
