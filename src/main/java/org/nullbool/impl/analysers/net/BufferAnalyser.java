package org.nullbool.impl.analysers.net;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.util.DescFilter;
import org.nullbool.api.util.map.NullPermeableMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V",
		"write8&(I)V", "write8Weird&(I)V", "write16&(I)V", "write16A&(I)V", "write16B&(I)V", "write24&(I)V", "write32&(I)V", "write40&(J)V", "write64&(J)V",
		"writeLE16&(I)V", "writeLE16A&(I)V", "writeLE32&(I)V", "write32Weird&(I)V",
		"writeInverted24&(I)V", "writeInverted32&(I)V", "writeInvertedLE32&(I)V",
		"writeString&(Ljava/lang/String;)V", "writeJagexString&(Ljava/lang/String;)V", "writeCharSequence&(Ljava/lang/CharSequence;)V"})
/**
 * @author Bibl (don't ban me pls)
 * @created 23 May 2015
 */
public class BufferAnalyser extends ClassAnalyser {

	private static final Filter<FieldNode> BYTE_ARRAY_FILTER = new DescFilter("[B");
	private static final Filter<FieldNode> INT_FILTER        = new DescFilter("I");

	public BufferAnalyser() {
		super("Buffer");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		ClassHook nodeHook = getAnalyser("Node").getFoundHook();
		if(cn.superName.equals(nodeHook.getObfuscated())) {
			int bytesFieldCount = getFieldCount(cn, BYTE_ARRAY_FILTER);
			int intFieldsCount  = getFieldCount(cn, INT_FILTER);
			return bytesFieldCount == 1 && intFieldsCount > 0;
		}
		return false;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {
		return Arrays.asList(new FieldAnalyser());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return Arrays.asList(new MethodAnalyser());
	}

	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			TreeBuilder tb = new TreeBuilder();
			DataVisitor dv = new DataVisitor();

			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "enableEncryption"));
				} else {
					if(!Modifier.isStatic(m.access) && m.desc.endsWith(")V")) {
						try {
							ControlFlowGraph graph = Context.current().getCFGCache().get(m);
							for(FlowBlock block : graph) {
								tb.build(m, block).accept(dv);
							}

							List<Integer> shifts = dv.map.getNotNull(ISHR);
							List<Integer> subs   = dv.map.getNotNull(ISUB);
							int tCount = shifts.size();

							boolean b = false;

							//FIXME: ghetto
							if(m.desc.startsWith("(Ljava/lang/String;")) {
								if(subequals(subs, new int[]{1})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeString"));
									b = true;
								} else if(subequals(subs, new int[]{1, 1})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeJagexString"));
									b = true;
								}
							} else if(m.desc.startsWith("(Ljava/lang/CharSequence;")) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeCharSequence"));
								b = true;
							}

							if(tCount == 8) {
								if(subequals(shifts, new int[]{56, 48, 40, 32, 24, 16, 8, 0})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "write64"));
									b = true;
								}
							} else if(tCount == 6) {
								if(subequals(shifts, new int[]{40, 32, 24, 16, 8, 0})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "write40"));
									b = true;
								}
							} else if(tCount == 4) {
								if(subequals(shifts, new int[]{8, 0, 24, 16})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeInvertedLE32"));
									b = true;
								} else if(subequals(shifts, new int[]{24, 16, 8, 0})) {
									if(subequals(subs, new int[]{1, 1, 1, 1})) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write32"));
										b = true;
									} else if(subequals(subs, new int[]{4, 3, 2, 1})) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write32Weird"));
										b = true;
									}
								} else if(subequals(shifts, new int[]{0, 8, 16, 24})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE32"));
									b = true;
								} else if(subequals(shifts, new int[]{16, 24, 0, 8})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeInverted32"));
									b = true;
								}
							} else if(tCount == 3) {
								if(subequals(shifts, new int[]{16, 8, 0})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "write24"));
									b = true;
								} else if(subequals(shifts, new int[]{8, 16, 0})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeInverted24"));
									b = true;
								}
							} else if(tCount == 2) {
								if(subequals(shifts, new int[]{8, 0})) {
									if(subequals(subs, new int[]{2, 1})) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE16A"));
										b = true;
									} else if(subequals(subs, new int[]{1, 1})) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write16"));
										b = true;
									}
								} else if(subequals(shifts, new int[]{8, 128})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "write16A"));
									b = true;
								} else if(subequals(shifts, new int[]{128, 8})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "write16B"));
									b = true;
								} else if(subequals(shifts, new int[]{0, 8})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE16"));
									b = true;
								}
							} else if(tCount == 1) {
								if(subequals(shifts, new int[]{0})) {
									if(subequals(subs, new int[]{1})) {
										if(findAllOpcodePatterns(m, new int[]{ ILOAD, ISUB}).size() == 1) {
											list.add(asMethodHook(MethodType.CALLBACK, m, "write8Weird"));
											System.out.println("BufferAnalyser.MethodAnalyser.find()1 " + m);
											b = true;
										} else {
											list.add(asMethodHook(MethodType.CALLBACK, m, "write8"));
											b = true;
										}
									}
								}
							}

							//							if(!b) {
							System.err.println(m + " " + shifts + " " + subs);
							//							}
						} catch (ControlFlowException e) {
							e.printStackTrace();
						}

						dv.map.clear();
					}
				}
			}

			return list;
		}
	}

	private static boolean subequals(List<Integer> list, int[] arr) {
		if(list.size() != arr.length)
			return false;

		for(int i=0;i < arr.length; i++) {
			if(list.get(i) != arr[i])
				return false;
		}
		return true;
	}

	private static class DataVisitor extends NodeVisitor {

		private final NullPermeableMap<Integer, List<Integer>> map = new NullPermeableMap<Integer, List<Integer>>(new ValueCreator<List<Integer>>() {
			@Override
			public List<Integer> create() {
				return new ArrayList<Integer>();
			}
		});

		@Override
		public void visit(AbstractNode n) {
			if(n.opcode() == BASTORE) {
				AbstractNode indexNode = n.child(1);
				AbstractNode valNode = n.child(2);
				
				System.out.println(valNode);
				
				if(indexNode.children() == 2 && indexNode.child(1) instanceof NumberNode) {
					NumberNode nn = (NumberNode) indexNode.child(1);
					map.getNotNull(ISUB).add(nn.number());
				}
				
				if((valNode.opcode() == I2B || (valNode.opcode() == L2I && valNode.hasParent())) && valNode.children() > 0) {
					AbstractNode c1 = valNode.child(0);

					if(c1.opcode() == L2I) {
						c1 = c1.child(0);
					}

					if(c1.firstNumber() != null) {
						map.getNotNull(ISHR).add(c1.firstNumber().number());
					} else {
						map.getNotNull(ISHR).add(0);
					}
				}
			}
		}
	}

	private class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			for(MethodNode m : cn.methods) {
				if(m.name.equals("<init>") && m.desc.startsWith("(I")) {
					for(AbstractInsnNode ain : m.instructions.toArray()) {
						if(ain.opcode() == PUTFIELD) {
							FieldInsnNode fin = (FieldInsnNode) ain;
							String source = String.format("%s.%s", fin.owner, fin.name);
							if(ain.getPrevious().opcode() == ICONST_0) {
								list.add(asFieldHook(fin, "getCaret", false, findMultiplier(source, false)));
							} else {
								list.add(asFieldHook(fin, "getPayload", false, 1));
							}
						}
					}
				}
			}

			return list;
		}
	}
}