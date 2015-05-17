package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IGroundItem extends IRenderable {

    int getId();

    int getStackSize();
}
