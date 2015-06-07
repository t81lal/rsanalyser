package org.nullbool.impl.r79;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.impl.analysers.ClientAnalyser;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 Jun 2015 17:51:10
 */
@SupportedHooks(
		fields = { "getNPCs&[NPC", "getPlayers&[Player", "getRegion&Region", /*"getWidgetPositionsX&[I", "getWidgetPositionsY&[I",*/
		"getCanvas&Ljava/awt/Canvas;", "getLocalPlayer&Player", "getWidgetNodes&Hashtable", "getMenuActions&[Ljava/lang/String;", "isSpellSelected&Z",
		"getSelectionState&I", "getMenuOptions&[Ljava/lang/String;", "getLoopCycle&I", "getCurrentWorld&I", "getGameState&I", "getCurrentLevels&[I",
		"getRealLevels&[I", "getSkillsExp&[I", "getSelectedItem&I", "isMenuOpen&Z", "getMenuX&I", "getMenuY&I", "getMenuWidth&I", "getMenuHeight&I",
		"getMenuSize&I", "getGroundItems&[[[Deque", "getTileSettings&[[[B", "getTileHeights&[[[I", "getMapScale&I", "getMapOffset&I", "getMapAngle&I",
		"getPlane&I", "getCameraX&I", "getCameraY&I", "getCameraZ&I", "getCameraYaw&I", "getCameraPitch&I", "getBaseX&I", "getBaseY&I", "getWidgets&[[Widget",
		"getClientSettings&[I", "getWidgetsSettings&[I","getHoveredRegionTileX&I","getHoveredRegionTileY&I"}, 
		
		methods = { "loadObjDefinition&(I)LObjectDefinition;", "loadItemDefinition&(I)LItemDefinition;",
		"getPlayerModel&()LModel;", "reportException&(Ljava/lang/Throwable;Ljava/lang/String;)WrappedException", "processAction&(IIIILjava/lang/String;Ljava/lang/String;II)V" })
public class ClientAnalyser79 extends ClientAnalyser {
	
	public ClientAnalyser79() throws AnalysisException {
		super();
	}
	
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return super.registerFieldAnalysers();
	}
}