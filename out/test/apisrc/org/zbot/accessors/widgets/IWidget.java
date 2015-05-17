package org.zbot.accessors.widgets;

import org.zbot.accessors.collections.INode;

public interface IWidget extends INode {

    int[] getQuantities();

    int[] getItemIds();

    int getBoundsIndex();

    String[] getActions();

    int getUID();

    String getName();

    String getText();

    int getTextColor();

    int getTextAlpha();

    int getTextureId();

    int getBorderThickness();

    int getModelType();

    int getScrollX();

    int getScrollY();

    int getRelativeX();

    int getRelativeY();

    int getWidth();

    int getHeight();

    int getParentId();

    int getIndex();

    int getRotationX();

    int getRotationY();

    int getRotationZ();

    IWidget[] getChildren();

    int getWidgetType();

    int getItemId();

    int getStackSize();
}
