package com.stek101.projectzulu.common.blocks.itemblockdeclarations;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Optional;
import com.stek101.projectzulu.common.api.BlockList;
import com.stek101.projectzulu.common.blocks.ItemUniversalFlowerPot;
import com.stek101.projectzulu.common.blocks.universalpot.BlockUniversalFlowerPot;
import com.stek101.projectzulu.common.blocks.universalpot.RenderUniversalFlowerPot;
import com.stek101.projectzulu.common.blocks.universalpot.TileEntityUniversalFlowerPot;
import com.stek101.projectzulu.common.blocks.universalpot.TileEntityUniversalFlowerPotRenderer;
import com.stek101.projectzulu.common.core.ProjectZuluLog;
import com.stek101.projectzulu.common.core.itemblockdeclaration.BlockDeclaration;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class UniversalFlowerPotDeclaration extends BlockDeclaration {

    private int renderID = -1;

    public UniversalFlowerPotDeclaration() {
        super("UniversalFlowerPot");
    }

    @Override
    protected void preCreateLoadConfig(Configuration config) {
        renderID = config.get("Do Not Touch", "Universal Flower Pot Render ID", renderID).getInt(renderID);
        renderID = renderID == -1 ? RenderingRegistry.getNextAvailableRenderId() : renderID;
    }

    @Override
    protected boolean createBlock() {
        BlockList.universalFlowerPot = Optional.of(new BlockUniversalFlowerPot(renderID).setBlockName("uniFlowerPot")
                .setBlockTextureName("flower_pot"));
        return true;
    }

    @Override
    protected void registerBlock() {
        Block block = BlockList.universalFlowerPot.get();
        GameRegistry.registerBlock(block, name.toLowerCase());
        GameRegistry.registerItem(new ItemUniversalFlowerPot(block), name);
        GameRegistry.registerTileEntity(TileEntityUniversalFlowerPot.class, "TileEntityUniversalFlowerPot");
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void clientRegisterBlock() {
        RenderingRegistry.registerBlockHandler(renderID, new RenderUniversalFlowerPot());
        ProjectZuluLog.info("Universal Flower Pot Render ID Registed to %s", renderID);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalFlowerPot.class,
                new TileEntityUniversalFlowerPotRenderer());
    }
}
