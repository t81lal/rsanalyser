package org.zbot.accessors.definitions;

import org.zbot.accessors.collections.IDualNode;

public interface IItemDefinition extends IDualNode {

    String getName();

    int getModelZoom();

    int getRotation1();

    int getRotation2();

    int getDiagonalRotation();

    int getModelOffset();

    int getModelSine();

    int getValue();

    String[] getGroundActions();

    String[] getWidgetActions();

    int getMaleEquipPrimaryModel();

    int getMaleEquipSecondaryModel();

    int getFemaleEquipPrimaryModel();

    int getFemaleEquipSecondaryModel();

    int getMaleEquipOffset();

    int getFemaleEquipOffset();

    int getMaleEmblem();

    int getFemaleEmblem();

    int getMaleDialog();

    int getMaleDialogHat();

    int getFemaleDialog();

    int getFemaleDialogHat();

    int getNoteIndex();

    int getNoteTemplateIndex();

    int getModelWidth();

    int getModelHeight();

    int getModelBreadth();

    int getStackedModelLightModifier();

    int getStackedModelShadowModifier();

    int getTeamIndex();
}
