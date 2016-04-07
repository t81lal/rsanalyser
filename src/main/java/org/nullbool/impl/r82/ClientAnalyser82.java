package org.nullbool.impl.r82;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IMultiAnalyser;
import org.nullbool.impl.analysers.world.WorldAnalyser;
import org.nullbool.impl.r79.ClientAnalyser79;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.custom_asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.custom_asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.custom_asm.commons.cfg.tree.node.TypeNode;
import org.objectweb.custom_asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.custom_asm.tree.ClassNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 18 Jul 2015 00:56:49
 */
public class ClientAnalyser82 extends ClientAnalyser79 {

	public ClientAnalyser82() throws AnalysisException {
		super();
	}
	
	@Override
	public String[] supportedFields() {
		return new Builder<String>(super.supportedFields()).addAll("worldCount&I", "worlds&[World", "worldListDownloader&WorldListDownloader").asArray(new String[0]);
	}
	
	@Override
	public String[] supportedMethods() {
		return new Builder<String>(super.supportedMethods()).addAll("loadWorlds&Z").asArray(new String[0]);
	}

	private static boolean same(Set<FieldInsnNode> fins) {
		String key = null;
		for (FieldInsnNode fin : fins) {
			if (key == null) {
				key = fin.key();
			} else {
				if (!fin.key().equals(key))
					return false;
			}
		}
		return true;
	}

	public class StaticWorldFieldsAnalyser implements IMultiAnalyser {
		@Override
		public List<ObfuscatedData> findAny(ClassNode _cn) {
			List<ObfuscatedData> list = new ArrayList<ObfuscatedData>();

			ClassNode world = getClassNodeByRefactoredName("World");
			if (world == null)
				return list;

			MethodNode lm = ((WorldAnalyser) getAnalyser("World")).loadMethod;

			TreeBuilder tb = new TreeBuilder();
			{
				NodeVisitor nv = new NodeVisitor() {
					@Override
					public void visitField(FieldMemberNode fmn) {
						if (fmn.opcode() == PUTSTATIC) {
							TypeNode newarr = fmn.firstType();
							if (newarr != null && newarr.type().equals(world.name)) {
								for (AbstractNode an : fmn.traverse()) {
									if (an != fmn) {
										if (an instanceof FieldMemberNode) {
											FieldMemberNode fmn2 = (FieldMemberNode) an;
											if (fmn2.opcode() == GETSTATIC) {
												list.add(asFieldHook(fmn.fin(), "worlds"));
												list.add(asFieldHook(fmn2.fin(), "worldCount"));
												break;
											}
										}
									}
								}
							}
						}
					}
				};
				tb.build(lm).accept(nv);
			}

			{
				list.add(asMethodHook(lm, "loadWorlds"));

				// aconst_null
				// putstatic rs/World.b:rs.WorldListDownloader

				Set<FieldInsnNode> fins = new HashSet<FieldInsnNode>();
				NodeVisitor nv = new NodeVisitor() {
					@Override
					public void visitField(FieldMemberNode fmn) {
						if (fmn.opcode() == PUTSTATIC) {
							AbstractNode an = fmn.first(ACONST_NULL);
							if (an != null) {
								fins.add(fmn.fin());
							}
						}
					}
				};

				tb.build(lm).accept(nv);

				if (fins.size() == 2 && same(fins)) {
					list.add(asFieldHook(fins.iterator().next(), "worldListDownloader"));
				}
			}

			return list;
		}
	}
	
	@Override
	public Builder<IMultiAnalyser> registerMultiAnalysers() {
		return new Builder<IMultiAnalyser>().add(new StaticWorldFieldsAnalyser());
	}
}