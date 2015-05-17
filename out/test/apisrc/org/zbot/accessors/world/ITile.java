package org.zbot.accessors.world;

import org.zbot.accessors.collections.INode;

public interface ITile extends INode {

    IGameObject[] getObjects();

    IGroundObject getGroundObjects();

    IGroundDecoration getGroundDecorations();

    IWallObject getWallObjects();

    IWallDecoration getWallDecorations();
}
