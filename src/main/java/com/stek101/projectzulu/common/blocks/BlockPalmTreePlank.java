package com.stek101.projectzulu.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.stek101.projectzulu.common.ProjectZulu_Core;

public class BlockPalmTreePlank extends Block {

    public BlockPalmTreePlank() {
        super(Material.wood);
        setCreativeTab(ProjectZulu_Core.projectZuluCreativeTab);
        setHardness(2.0F);
        setResistance(5.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }
}
