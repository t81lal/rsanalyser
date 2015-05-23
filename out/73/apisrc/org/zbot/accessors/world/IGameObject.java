package org.zbot.accessors.world;

import org.zbot.accessors.entity.IRenderable;

public interface IGameObject {

    int getHash();

    int getPlane();

    int getStrictX();

    int getStrictY();

    int getLocalX();

    int getLocalY();

    int getWidth();

    int getHeight();

    int getOrientation();

    int getFlags();

    IRenderable getMarkedRenderable();
}
