package com.stek101.projectzulu.common.blocks.itemblockdeclarations;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Optional;
import com.stek101.projectzulu.common.api.BlockList;
import com.stek101.projectzulu.common.blocks.spike.BlockSpikes;
import com.stek101.projectzulu.common.blocks.spike.RenderSpike;
import com.stek101.projectzulu.common.core.DefaultProps;
import com.stek101.projectzulu.common.core.ProjectZuluLog;
import com.stek101.projectzulu.common.core.itemblockdeclaration.BlockDeclaration;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class SpikesDeclaration extends BlockDeclaration {

    private int renderID = -1;

    public SpikesDeclaration() {
        super("Spikes");
    }

    @Override
    protected void preCreateLoadConfig(Configuration config) {
        renderID = config.get("Do Not Touch", "Spike Render ID", renderID).getInt(renderID);
        renderID = renderID == -1 ? RenderingRegistry.getNextAvailableRenderId() : renderID;
    }

    @Override
    protected boolean createBlock() {
        BlockList.spike = Optional
                .of(new BlockSpikes(renderID).setHardness(0.5F).setStepSound(Block.soundTypeMetal)
                        .setBlockName(name.toLowerCase())
                        .setBlockTextureName(DefaultProps.blockKey + ":" + name.toLowerCase()));
        return true;
    }

    @Override
    protected void registerBlock() {
        Block block = BlockList.spike.get();
        GameRegistry.registerBlock(block, name.toLowerCase());
    }

    @Override
    protected void clientRegisterBlock() {
        RenderingRegistry.registerBlockHandler(renderID, new RenderSpike());
        ProjectZuluLog.info("Spike Render ID Registed to %s", renderID);
    }
}
