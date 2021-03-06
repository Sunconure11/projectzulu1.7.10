package com.stek101.projectzulu.common.mobs.entitydefaults;

import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.config.Configuration;

import com.stek101.projectzulu.common.api.CustomMobData;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.core.entitydeclaration.EntityProperties;
import com.stek101.projectzulu.common.core.entitydeclaration.SpawnableDeclaration;
import com.stek101.projectzulu.common.mobs.entity.EntityBloomDoom;
import com.stek101.projectzulu.common.mobs.models.ModelBloomDoom;
import com.stek101.projectzulu.common.mobs.renders.RenderGenericIdle;
import com.stek101.projectzulu.common.mobs.renders.RenderWrapper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BloomDoomDeclaration extends SpawnableDeclaration {
	//private final List <String> entityTextures = new ArrayList <String>();
	
    public BloomDoomDeclaration() {
        super("BloomDoom", 64, EntityBloomDoom.class, EnumCreatureType.creature);
        setSpawnProperties(10, 5, 1, 1);
        setRegistrationProperties(128, 3, true);
        setDropAmount(0, 1);

        eggColor1 = (133 << 16) + (233 << 8) + 0;
        eggColor2 = (101 << 16) + (200 << 8) + 143;
        
       // entityTextures.add("textures/beetlebs1.png");
       // entityTextures.add("textures/beetlebs2.png");
       // entityTextures.add("textures/beetlebs3.png");
    }

    @Override
    public void outputDataToList(Configuration config, CustomMobData customMobData) {
        //ConfigHelper.configDropToMobData(config, "MOB CONTROLS." + mobName, customMobData, Items.feather, 0, 8);
        customMobData.entityProperties = new EntityProperties(15f, 2.0f, 0.25f).createFromConfig(config, mobName);
        //customMobData.entityProperties = new EntityProperties(15f, 3.0f, 0.3f, 0.0f, 0.0f, 32.0f, 50f, 16D).createFromConfig(config, mobName);
        super.outputDataToList(config, customMobData);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderWrapper getEntityrender(Class<? extends EntityLivingBase> entityClass) {
        //return new RenderGenericLiving(new ModelBloomDoom(), 0.5f, new ResourceLocation(DefaultProps.mobKey,
        //        "textures/bloomdoom.png"));
        
        return new RenderGenericIdle(new ModelBloomDoom(), 0.5f, new ResourceLocation(DefaultProps.mobKey,
        		"textures/bloomdoom0.png"), new ResourceLocation(DefaultProps.mobKey, "textures/bloomdoomidle0.png"));
    	
    }

    @Override
    public HashSet<String> getDefaultBiomesToSpawn() {
        HashSet<String> defaultBiomesToSpawn = new HashSet<String>();
        defaultBiomesToSpawn.add(BiomeGenBase.forest.biomeName);
        defaultBiomesToSpawn.add(BiomeGenBase.forestHills.biomeName);
        defaultBiomesToSpawn.add("Autumn Woods");
        defaultBiomesToSpawn.add("Birch Forest");
        defaultBiomesToSpawn.add("Forested Hills");
        defaultBiomesToSpawn.add("Forested Island");
        defaultBiomesToSpawn.add("Green Hills");
        defaultBiomesToSpawn.add("Redwood Forest");
        defaultBiomesToSpawn.add("Lush Redwoods");
        defaultBiomesToSpawn.add("Temperate Rainforest");
        defaultBiomesToSpawn.add("Woodlands");

        HashSet<String> nonFrozenForest = new HashSet<String>();
        nonFrozenForest.addAll(typeToArray(Type.FOREST));
        nonFrozenForest.addAll(typeToArray(Type.PLAINS));
        nonFrozenForest.removeAll(typeToArray(Type.FROZEN));
        defaultBiomesToSpawn.addAll(nonFrozenForest);

        return defaultBiomesToSpawn;
    }
}
