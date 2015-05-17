package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IGroundObject {

    IRenderable getMarkedRenderable();

    int getStrictX();

    int getStrictY();

    int getPlane();

    int getUID();

    int getFlags();
}
