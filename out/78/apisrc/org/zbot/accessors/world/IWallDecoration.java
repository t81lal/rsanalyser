package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IWallDecoration {

    IRenderable getMarkerRenderable1();

    IRenderable getMarkerRenderable2();

    int getStrictX();

    int getStrictY();

    int getPlane();

    int getHash();

    int getUID();

    int getOrientation1();

    int getOrientation2();
}
