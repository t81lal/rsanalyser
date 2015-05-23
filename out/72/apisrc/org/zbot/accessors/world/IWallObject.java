package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IWallObject {

    IRenderable getMarkedRenderable1();

    IRenderable getMarkedRenderable2();

    int getStrictX();

    int getStrictY();

    int getPlane();

    int getUID();

    int getFlags();

    int getOrientation1();

    int getOrientation2();
}
