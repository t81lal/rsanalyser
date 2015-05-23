package org.zbot.accessors;

import java.awt.Canvas;
import org.zbot.accessors.collections.IDeque;
import org.zbot.accessors.collections.IHashTable;
import org.zbot.accessors.definitions.IItemDefinition;
import org.zbot.accessors.definitions.IObjectDefinition;
import org.zbot.accessors.entity.IModel;
import org.zbot.accessors.entity.INPC;
import org.zbot.accessors.entity.IPlayer;
import org.zbot.accessors.widgets.IWidget;
import org.zbot.accessors.world.IRegion;
import org.zbot.api.IGameClient;

public interface IOldschoolClient extends IGameClient {

    INPC[] getNPCs();

    IPlayer[] getPlayers();

    IRegion getRegion();

    Canvas getCanvas();

    IPlayer getLocalPlayer();

    IHashTable getWidgetNodes();

    String[] getMenuActions();

    boolean isSpellSelected();

    int getSelectionState();

    String[] getMenuOptions();

    int getLoopCycle();

    int getCurrentWorld();

    int getGameState();

    int[] getCurrentLevels();

    int[] getRealLevels();

    int[] getSkillsExp();

    int getSelectedItem();

    boolean isMenuOpen();

    int getMenuX();

    int getMenuY();

    int getMenuWidth();

    int getMenuSize();

    int getMenuHeight();

    IDeque[][][] getGroundItems();

    byte[][][] getTileSettings();

    int[][][] getTileHeights();

    int getMapScale();

    int getMapOffset();

    int getMapAngle();

    int getPlane();

    int getCameraX();

    int getCameraY();

    int getCameraZ();

    int getCameraYaw();

    int getCameraPitch();

    int getBaseX();

    int getBaseY();

    IWidget[][] getWidgets();

    int[] getClientSettings();

    int[] getWidgetsSettings();

    IObjectDefinition loadObjDefinition(int i);

    IItemDefinition loadItemDefinition(int i);

    IModel getPlayerModel(byte b0);

    IWrappedException reportException(Throwable throwable, String s);

    void processAction(int i, String s);
}
