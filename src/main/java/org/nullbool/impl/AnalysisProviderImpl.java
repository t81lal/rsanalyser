package org.nullbool.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.analysers.client.CanvasAnalyser;
import org.nullbool.impl.analysers.client.ExceptionReporterAnalyser;
import org.nullbool.impl.analysers.client.WrappedExceptionAnalyser;
import org.nullbool.impl.analysers.client.definitions.ItemDefinitionAnalyser;
import org.nullbool.impl.analysers.client.definitions.NPCDefinitionAnalyser;
import org.nullbool.impl.analysers.client.definitions.ObjectDefinitionAnalyser;
import org.nullbool.impl.analysers.client.widget.WidgetAnalyser;
import org.nullbool.impl.analysers.client.widget.WidgetNodeAnalyser;
import org.nullbool.impl.analysers.collections.DequeAnalyser;
import org.nullbool.impl.analysers.collections.DualNodeAnalyser;
import org.nullbool.impl.analysers.collections.HashtableAnalyser;
import org.nullbool.impl.analysers.collections.NodeAnalyser;
import org.nullbool.impl.analysers.entity.ActorAnalyser;
import org.nullbool.impl.analysers.entity.ModelAnalyser;
import org.nullbool.impl.analysers.entity.NPCAnalyser;
import org.nullbool.impl.analysers.entity.PlayerAnalyser;
import org.nullbool.impl.analysers.entity.RenderableAnalyser;
import org.nullbool.impl.analysers.net.BufferAnalyser;
import org.nullbool.impl.analysers.net.PacketAnalyser;
import org.nullbool.impl.analysers.world.GameObjectAnalyser;
import org.nullbool.impl.analysers.world.GroundDecorationAnalyser;
import org.nullbool.impl.analysers.world.GroundItemAnalyser;
import org.nullbool.impl.analysers.world.GroundObjectAnalyser;
import org.nullbool.impl.analysers.world.RegionAnalyser;
import org.nullbool.impl.analysers.world.TileAnalyser;
import org.nullbool.impl.analysers.world.WallDecorationAnalyser;
import org.nullbool.impl.analysers.world.WallObjectAnalyser;

public class AnalysisProviderImpl extends AbstractAnalysisProvider {

	public AnalysisProviderImpl(Revision revision) throws IOException {
		super(revision);
	}

	@Override
	protected List<ClassAnalyser> registerAnalysers() throws AnalysisException {
		return Arrays.asList(new WrappedExceptionAnalyser(), new ExceptionReporterAnalyser(), new CanvasAnalyser(), new NodeAnalyser(), new DualNodeAnalyser(),
				new RenderableAnalyser(), new ActorAnalyser(), new DequeAnalyser(), new NPCAnalyser(), new BufferAnalyser(), new PacketAnalyser(), new HashtableAnalyser(), new GroundItemAnalyser(),
				new RegionAnalyser(), new NPCDefinitionAnalyser(), new ObjectDefinitionAnalyser(), new ItemDefinitionAnalyser(), new ModelAnalyser(),
				new PlayerAnalyser(), new TileAnalyser(), new WidgetNodeAnalyser(), new GameObjectAnalyser(), new WallObjectAnalyser(), new WidgetAnalyser(),
				new WallDecorationAnalyser(), new GroundObjectAnalyser(), new GroundDecorationAnalyser(), new ClientAnalyser());
	}
}