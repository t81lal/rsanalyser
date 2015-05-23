package org.zbot.accessors.entity;

public interface IActor extends IRenderable {

    int getLocalX();

    int getLocalY();

    int getHealthBarCycle();

    int getAnimationId();

    int getInteractingId();

    int getHealth();

    int getMaxHealth();

    int[] getHitTypes();

    String getMessage();

    int[] getHitDamages();
}
