package com.stek101.projectzulu.common.blocks;

import net.minecraft.item.Item;

import com.stek101.projectzulu.common.ProjectZulu_Core;
import com.stek101.projectzulu.common.core.DefaultProps;

public class ItemCoconutShellFuel extends Item {

    public ItemCoconutShellFuel(boolean full3D, String name) {
        super();
        maxStackSize = 64;
        setMaxDamage(2);
        this.setCreativeTab(ProjectZulu_Core.projectZuluCreativeTab);
        bFull3D = full3D;
        setUnlocalizedName(name.toLowerCase());
        setTextureName(DefaultProps.blockKey + ":" + name.toLowerCase());
    }

    public int getMetadata(int par1) {
        return par1;
    }
}
