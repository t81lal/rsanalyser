package org.nullbool.impl.analysers.net;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.util.DescFilter;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
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
 * Notes:
 *    write40 was added in rev77.
 *    writeInverted24 isn't in every other revision (???).
 *    
 * 
 * @author Bibl (don't ban me pls)
 * @created 23 May 2015
 */
public class BufferAnalyser extends ClassAnalyser {

	private static boolean print = true;
	
	private static final ShiftNode[] WRITE_LONG_SHIFTS = createIntSeq(LSHR, new int[]{58, 48, 40, 32, 24, 16, 8, 0});
	private static final ShiftNode[] WRITE_INT_SHIFTS = createIntSeq(ISHR, new int[]{24, 16, 8, 0});
	private static final ShiftNode[] WRITE_INV_32 = createIntSeq(ISHR, new int[]{16, 24, 0, 8});
	private static final ShiftNode[] WRITE_INV_LE_32 = createIntSeq(ISHR, new int[]{8, 0, 24, 16});
	private static final ShiftNode[] WRITE_LE_32 = createIntSeq(ISHR, new int[]{0, 8, 16, 24});
	private static final ShiftNode[] WRITE_24 = createIntSeq(ISHR, new int[]{16, 8, 0});
	@Deprecated
	private static final ShiftNode[] WRITE_INV_24 = createIntSeq(ISHR, new int[]{8, 16, 0});
	private static final ShiftNode[] WRITE_16 = createIntSeq(ISHR, new int[]{8, 0});
	private static final ArithmeticOp[] WRITE8OFFSET128 = new ArithmeticOp[]{new ArithmeticOp(IADD, 128, null)};
	private static final ArithmeticOp[] WRITE8NEG0 = new ArithmeticOp[]{new ArithmeticOp(ISUB, 0, ArithmeticOpOrder.LEFT)};
	private static final ArithmeticOp[] WRITE8NEG128 = new ArithmeticOp[]{new ArithmeticOp(ISUB, 128, ArithmeticOpOrder.LEFT)};

	private static ShiftNode[] createIntSeq(int opcode, int[] shifts) {
		ShiftNode[] nodes = new ShiftNode[shifts.length];
		for(int i=0; i < shifts.length; i++) {
			nodes[i] = new ShiftNode(opcode, shifts[i]);
		}
		return nodes;
	}

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
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new FieldAnalyser());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().add(new MethodAnalyser());
	}

	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();

			TreeBuilder tb = new TreeBuilder();
			ArrayStoreVisitor asv = new ArrayStoreVisitor();

			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "enableEncryption"));
				} else {
					if(!Modifier.isStatic(m.access) && m.desc.endsWith(")V")) {
						try {
							ControlFlowGraph graph = Context.current().getCFGCache().get(m);
							if(print)
								System.out.println(m);
							asv.end();
							for(FlowBlock block : graph) {
								tb.build(m, block).accept(asv);
							}
							
							List<Object> index = asv.found.get(ArrayStoreVisitor.INDEX);
							List<Object> value = asv.found.get(ArrayStoreVisitor.VALUE);
							List<Object> arith = asv.found.get(ArrayStoreVisitor.VALUE_SUB);

							boolean b = false;

							//FIXME: ghetto
							if(m.desc.startsWith("(Ljava/lang/String;")) {

								if(match(index, new Object[]{1, 1})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeJagexString"));
									b = true;
								} else {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeString"));
									b = true;
								}
							} else if(m.desc.startsWith("(Ljava/lang/CharSequence;")) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeCharSequence"));
								b = true;
							} else {
								if(m.desc.startsWith("(J")) {
									if(match(value, WRITE_LONG_SHIFTS)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write64"));
										b = true;
									} else if(match(value, WRITE_LONG_SHIFTS, 2)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write40"));
										b = true;
									}
								} else if(m.desc.startsWith("(I")) {
									if(match(value, WRITE_INT_SHIFTS)) {
										if(match(index, new Object[]{4, 3, 2, 1})) {
											list.add(asMethodHook(MethodType.CALLBACK, m, "write32Weird"));
											b = true;
										} else if(match(index, new Object[]{1, 1, 1, 1})) {
											list.add(asMethodHook(MethodType.CALLBACK, m, "write32"));
											b = true;
										}
										
									} else if(match(value, WRITE_INV_LE_32)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "writeInverted32"));
										b = true;
									} else if(match(value, WRITE_LE_32)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE32"));
										b = true;
									} else if(match(value, WRITE_INV_32)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "writeInverted32"));
										b = true;
									} else if(match(value, WRITE_24)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write24"));
										b = true;
									}/* else if(match(value, WRITE_INV_24)) {
										System.out.println("BufferAnalyser.MethodAnalyser.find() " + index);
									}*/
									else if(match(value, WRITE_16)) {
										if(match(index, new Object[]{2, 1})) {
											list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE16A"));
											b = true;
										} else if(match(index, new Object[]{1, 1})) {
											list.add(asMethodHook(MethodType.CALLBACK, m, "write16"));
											b = true;
										}
									} else if(match(value, WRITE8OFFSET128)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write8Offset128"));
										b = true;
									} else if(match(value, WRITE8NEG0)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write8Neg0"));
										b = true;
									} else if(match(value, WRITE8NEG128)) {
										list.add(asMethodHook(MethodType.CALLBACK, m, "write8Neg128"));
										b = true;
									}
								}
							}

							if("".equals(""))
								continue;

							List<Integer> shifts = null;
							List<Integer> subs   = null;
							int tCount = shifts.size();
							
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
						} catch (ControlFlowException e) {
							e.printStackTrace();
						}
						
						
					}
				}
			}

			//TODO: remember to remove halt
			Context.current().requestHalt();
			return list;
		}
	}

	private static boolean match(List<Object> index, Object[] objects) {
		return match(index, objects, 0);
	}

	private static boolean match(List<Object> index, Object[] objects, int offset) {
		//int j = Math.min(index.size(), objects.length);
		if(index == null)
			return false;
		
		int j = index.size();
		//if(offset == 0) {
		//if(j != objects.length)
		//return false;
		//} else {
		//	
		//}

		if((offset + j) != objects.length)
			return false;

		for(int i=0; i < j; i++) {
			Object o1 = index.get(i);
			Object o2 = objects[offset + i];

			if(o2 == null)
				continue;

			if(o1 == null) {
				throw new RuntimeException();
			} else {

				//System.out.println("comp " + i + " (" + (offset + i) + ") " + o1 + " " + o2 + " == " + (o1.equals(o2)));

				if(!o1.equals(o2))
					return false;
			}
		}
		return true;
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

	public static class ArrayStoreVisitor extends NodeVisitor {

		public static final int INDEX = 0x01, VALUE = 0x02, VALUE_SUB = 0x03;
		public final NullPermeableHashMap<Integer, List<Object>> found = new NullPermeableHashMap<Integer, List<Object>>(new ValueCreator<List<Object>>() {
			@Override
			public List<Object> create() {
				return new ArrayList<Object>();
			}
		});

		@Override
		public void visit(AbstractNode n) {
			if(n.opcode() == BASTORE) {
				AbstractNode indexChild = n.child(1);
				if(indexChild.opcode() == ISUB) {
					found.getNonNull(INDEX).add(InstructionUtil.resolve(indexChild.firstNumber().insn()));
				} else if(indexChild.opcode() == ILOAD) {
					found.getNonNull(INDEX).add(0);
				} else {
					System.out.flush();
					System.err.flush();
					System.err.println("gat " + n);
				}

				AbstractNode valueChild = n.child(2);
				if(valueChild instanceof ConversionNode)
					valueChild = valueChild.child(0);
				if(valueChild instanceof ConversionNode)
					valueChild = valueChild.child(0);

				if(valueChild instanceof VariableNode) {
					found.getNonNull(VALUE).add(new ShiftNode(-1, 0));
				} else if(valueChild instanceof ArithmeticNode) {
					if(((ArithmeticNode) valueChild).rightShifting()) {
						found.getNonNull(VALUE).add(new ShiftNode(valueChild.opcode(), ((ArithmeticNode) valueChild).firstNumber().number()));
					} else {
						NumberNode nn = valueChild.firstNumber();
						ArithmeticOpOrder order = null;
						if(nn.equals(valueChild.child(0))) {
							order = ArithmeticOpOrder.LEFT;
						} else {
							order = ArithmeticOpOrder.RIGHT;
						}
						found.getNonNull(VALUE).add(new ArithmeticOp(valueChild.opcode(), nn.number(), order));
					}
				} else {
					//System.out.println("n " + n.child(2));
					//System.out.println("n1 " + valueChild);
				}
			}
		}

		public void end() {
			if(print)
				System.out.println(found);
			found.clear();
		}
	}

	public static class ShiftNode {
		private final int opcode;
		private final int shift;

		public ShiftNode(int opcode, int shift) {
			this.opcode = opcode;
			this.shift = shift;
		}

		public int opcode() {
			return opcode;
		}

		public int shift() {
			return shift;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + opcode;
			result = prime * result + shift;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ShiftNode other = (ShiftNode) obj;
			if (opcode != -1 && opcode != other.opcode)
				return false;
			if (shift != -1 && shift != other.shift)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[Shift " + (opcode == -1 ? "?" : Printer.OPCODES[opcode]) + " by " + shift + "]";
		}
	}

	public static class ArithmeticOp {
		private final int opcode;
		private final int cst;
		private final ArithmeticOpOrder order;

		public ArithmeticOp(int opcode, int cst, ArithmeticOpOrder order) {
			this.opcode = opcode;
			this.cst = cst;
			this.order = order;
		}

		public int opcode() {
			return opcode;
		}

		public int cst() {
			return cst;
		}

		public ArithmeticOpOrder order() {
			return order;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + cst;
			result = prime * result + opcode;
			result = prime * result + ((order == null) ? 0 : order.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArithmeticOp other = (ArithmeticOp) obj;
			
			if(cst != Integer.MIN_VALUE && other.cst != Integer.MIN_VALUE)
				if(cst != other.cst)
					return false;
			if(opcode != -1 && other.opcode != -1)
				if (opcode != other.opcode)
					return false;
			if(order != null && other.order != null)
				if (order != null && order != other.order)
					return false;
			return true;
		}

		@Override
		public String toString() {
			String left  = null;
			String right = null;
			if(order == ArithmeticOpOrder.LEFT) {
				left  = Printer.OPCODES[opcode];
				right = Integer.toString(cst);
			} else {
				right  = Printer.OPCODES[opcode];
				left = Integer.toString(cst);
			}

			return "[ArithOp " + left + " then " + right + "]";
		}
	}

	public static enum ArithmeticOpOrder {
		RIGHT, LEFT;
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