package org.nullbool.impl;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Builder;
import org.nullbool.api.Revision;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.impl.analysers.ClientAnalyser;
import org.nullbool.impl.analysers.GameshellAnalyser;
import org.nullbool.impl.analysers.client.CanvasAnalyser;
import org.nullbool.impl.analysers.client.VarpbitAnalyser;
import org.nullbool.impl.analysers.client.VerificationDataAnalyser;
import org.nullbool.impl.analysers.client.WrappedExceptionAnalyser;
import org.nullbool.impl.analysers.client.definitions.ItemDefinitionAnalyser;
import org.nullbool.impl.analysers.client.definitions.NPCDefinitionAnalyser;
import org.nullbool.impl.analysers.client.definitions.ObjectDefinitionAnalyser;
import org.nullbool.impl.analysers.client.grandexchange.ExchangeOfferAnalyser;
import org.nullbool.impl.analysers.client.script.ScriptEventAnalyser;
import org.nullbool.impl.analysers.client.task.TaskAnalyser;
import org.nullbool.impl.analysers.client.task.TaskHandlerAnalyser;
import org.nullbool.impl.analysers.client.widget.ItemContainerAnalyser;
import org.nullbool.impl.analysers.client.widget.WidgetAnalyser;
import org.nullbool.impl.analysers.client.widget.WidgetNodeAnalyser;
import org.nullbool.impl.analysers.collections.*;
import org.nullbool.impl.analysers.entity.*;
import org.nullbool.impl.analysers.friend.FriendAnalyser;
import org.nullbool.impl.analysers.friend.IgnoredPlayerAnalyser;
import org.nullbool.impl.analysers.net.BufferAnalyser;
import org.nullbool.impl.analysers.net.IsaacCipherAnalyser;
import org.nullbool.impl.analysers.net.PacketAnalyser;
import org.nullbool.impl.analysers.render.RasteriserAnalyser;
import org.nullbool.impl.analysers.world.*;

import java.io.IOException;

public class AnalysisProviderImpl extends AbstractAnalysisProvider {

	public AnalysisProviderImpl(Revision revision) throws IOException {
		super(revision);
	}

	@Override
	protected Builder<ClassAnalyser> registerAnalysers() throws AnalysisException {
		Builder<ClassAnalyser> builder = new Builder<ClassAnalyser>();
		builder.addAll(new ClassAnalyser[]
				{	new VerificationDataAnalyser(), new WrappedExceptionAnalyser(), /* deprecated new ExceptionReporterAnalyser(),*/ new CanvasAnalyser(),
					new TaskHandlerAnalyser(), new TaskAnalyser(),
					new NodeAnalyser(), new DualNodeAnalyser(), new QueueAnalyser(), new DequeAnalyser(), new IterableNodeAnalyser(), new NodeIteratorAnalyser(), new IterableDualNodeAnalyser(), new DualNodeIteratorAnalyser(),
					new RasteriserAnalyser(), new RenderableAnalyser(), new ActorAnalyser(), new ProjectileAnalyser(), new NPCAnalyser(), new IsaacCipherAnalyser(), 
					new BufferAnalyser(), new PacketAnalyser(), new HashTableAnalyser(), new GroundItemAnalyser(), new RegionAnalyser(), 
					new VarpbitAnalyser(), new WorldListDownloaderAnalyser(), new WorldAnalyser(), new ExchangeOfferAnalyser(),
					new FriendAnalyser(), new IgnoredPlayerAnalyser(),
					new NPCDefinitionAnalyser(), new ObjectDefinitionAnalyser(), new ItemDefinitionAnalyser(), new ModelAnalyser(),
					new PlayerAnalyser(), new TileAnalyser(),new ItemContainerAnalyser() ,new WidgetNodeAnalyser(), new GameObjectAnalyser(), new WallObjectAnalyser(), new WidgetAnalyser(),
					new WallDecorationAnalyser(), new GroundObjectAnalyser(), new GroundDecorationAnalyser(), 
					new ScriptEventAnalyser(),
					new GameshellAnalyser(), new ClientAnalyser()
				}, true);
		return builder;
	}
}