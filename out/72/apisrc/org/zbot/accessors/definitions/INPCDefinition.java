package org.zbot.accessors.definitions;

import org.zbot.accessors.collections.IDualNode;

public interface INPCDefinition extends IDualNode {

    String[] getActions();

    boolean isOnMap();

    boolean isVisible();

    boolean isClickable();

    String getName();

    int getCombatLevel();

    int getWidth();

    int getHeight();

    int getBrightness();

    int getContrast();

    int getHeadIcon();

    int getNpcDegToTurn();

    int getVarpId();

    int getSettingId();

    int getNpcBoundDim();

    int getIdleAnimationId();

    int getWalkAnimationId();

    int getNpcTurnAround();

    int getNpcTurnRight();

    int getNpcTurnLeft();
}
