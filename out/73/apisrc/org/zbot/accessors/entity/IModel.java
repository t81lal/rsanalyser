package org.zbot.accessors.entity;

public interface IModel extends IRenderable {

    int getVertexCount();

    int getIndicesCount();

    int getTriangleCount();

    int[] getVerticesX();

    int[] getVerticesY();

    int[] getVerticesZ();

    int[] getIndicesX();

    int[] getIndicesY();

    int[] getIndicesZ();
}
