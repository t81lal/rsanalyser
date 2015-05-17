package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IGroundDecoration {

    IRenderable getBottomRenderable();

    IRenderable getMiddleRenderable();

    IRenderable getTopRenderable();

    int getRegionX();

    int getRegionY();

    int getPlane();

    int getUID();
}
