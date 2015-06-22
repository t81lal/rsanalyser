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
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.obfuscation.cfg.IControlFlowGraph;
import org.nullbool.api.util.DescFilter;
import org.nullbool.api.util.InstructionUtil;
import org.nullbool.api.util.NotifyNodeVisitor;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.nullbool.zbot.pi.core.hooks.api.ClassHook;
import org.nullbool.zbot.pi.core.hooks.api.FieldHook;
import org.nullbool.zbot.pi.core.hooks.api.MethodHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
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

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V",
		"writeVarByte&(I)V", "writeBytes&([BIII)V",
		"write8&(I)V", "write8Weird&(I)V", "write16&(I)V", "write16A&(I)V", "write16B&(I)V", "write24&(I)V", "write32&(I)V", /*"write40&(J)V", */ "write64&(J)V",
		"writeLE16&(I)V", "writeLE16A&(I)V", "writeLE32&(I)V", "write32Weird&(I)V",
		"writeInverted32&(I)V", /*"writeInverted24&(I)V",*/ "writeInvertedLE32&(I)V",
		"writeString&(Ljava/lang/String;)V", "writeJagexString&(Ljava/lang/String;)V", "writeCharSequence&(Ljava/lang/CharSequence;)V",
		"write8Offset128&(I)", "write8Neg0&(I)", "write8Neg128&(I)", 
		
		//TODO: Fix read methods
		/*"read16&()I", "readLE16&()I", "read16B&()I", "readLE16B&()I",*/
})
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

	// debug flag.
	private static boolean print = false;

	// TODO: do some merging
	public static final ShiftNode[] WRITE_LONG_SHIFTS = createIntSeq(LSHR, new int[]{56, 48, 40, 32, 24, 16, 8, 0});
	public static final ShiftNode[] WRITE_INT_SHIFTS = createIntSeq(ISHR, new int[]{24, 16, 8, 0});
	public static final ShiftNode[] WRITE_INV_32 = createIntSeq(ISHR, new int[]{16, 24, 0, 8});
	public static final ShiftNode[] WRITE_INV_LE_32 = createIntSeq(ISHR, new int[]{8, 0, 24, 16});
	public static final ShiftNode[] WRITE_LE_32 = createIntSeq(ISHR, new int[]{0, 8, 16, 24});
	public static final ShiftNode[] WRITE_24 = createIntSeq(ISHR, new int[]{16, 8, 0});
	@Deprecated
	public static final ShiftNode[] WRITE_INV_24 = createIntSeq(ISHR, new int[]{8, 16, 0});
	public static final ShiftNode[] WRITE_16 = createIntSeq(ISHR, new int[]{8, 0});
	public static final ShiftNode[] WRITE_LE_16 = createIntSeq(ISHR, new int[]{0, 8});
	public static final ShiftNode[] WRITE_8 = createIntSeq(ISHR, new int[]{0});
	public static final Object[] WRITE_16A = new Object[]{new ShiftNode(ISHR, 8), new ArithmeticOp(IADD, 128, null)};
	public static final Object[] WRITE_16B = new Object[]{new ArithmeticOp(IADD, 128, null), new ShiftNode(ISHR, 8)};
	public static final ArithmeticOp[] WRITE8OFFSET128 = new ArithmeticOp[]{new ArithmeticOp(IADD, 128, null)};
	public static final ArithmeticOp[] WRITE8NEG0 = new ArithmeticOp[]{new ArithmeticOp(ISUB, 0, ArithmeticOpOrder.LEFT)};
	public static final ArithmeticOp[] WRITE8NEG128 = new ArithmeticOp[]{new ArithmeticOp(ISUB, 128, ArithmeticOpOrder.LEFT)};
	public static final Sub2Node SUB2_NODE = new Sub2Node();

	public static final Object[] WRITE_VAR_BYTE = new Object[]{new CompPair(IF_ICMPGE, 32768), new CompPair(IF_ICMPGE, 128)};
	public static final ArraySet WRITE_BYTES = new ArraySet(ArraySetType.LOCAL, ArraySetType.FIELD);
	public static final ArraySet WRITE_BYTES2 = new ArraySet(ArraySetType.FIELD, ArraySetType.LOCAL);
	public static final ArithmeticOp[] WRITE_BYTES_SUB = new ArithmeticOp[]{new ArithmeticOp(ISUB, 128, ArithmeticOpOrder.RIGHT)};

	public static final ArithmeticOp[] READ_UNSIGNED_16 = new ArithmeticOp[]{null, new ArithmeticOp(ISHL, 8, null), new ArithmeticOp(IAND, 0xFF, null), new ArithmeticOp(IAND, 0xFF, null)};
	public static final ArithmeticOp[] READ_UNSIGNED_16_2 = new ArithmeticOp[]{null, new ArithmeticOp(IAND, 0xFF, null), new ArithmeticOp(ISHL, 8, null), new ArithmeticOp(IAND, 0xFF, null)};

	public static final ArithmeticOp[] READ_UNSIGNED_16B = new ArithmeticOp[]{null, new ArithmeticOp(ISHL, 8, null), new ArithmeticOp(IAND, 0xFF, null), new ArithmeticOp(IAND, 0xFF, null), new ArithmeticOp(ISUB, 128, null)};
	
	private static final Filter<FieldNode> BYTE_ARRAY_FILTER = new DescFilter("[B");
	private static final Filter<FieldNode> INT_FILTER        = new DescFilter("I");

	private static ShiftNode[] createIntSeq(int opcode, int[] shifts) {
		ShiftNode[] nodes = new ShiftNode[shifts.length];
		for(int i=0; i < shifts.length; i++) {
			nodes[i] = new ShiftNode(opcode, shifts[i]);
		}
		return nodes;
	}

	private final TreeBuilder treeBuilder = new TreeBuilder();
	private final ArrayStoreVisitor arrayStoreVisitor = new ArrayStoreVisitor();
	private final ArrayLoadVisitor arrayLoadVisitor = new ArrayLoadVisitor();
	private final ArrayMethodVisitor arrayMethodVisitor = new ArrayMethodVisitor();
	private final VarIntNodeVisitor varIntNodeVisitor = new VarIntNodeVisitor();
	
	public BufferAnalyser() {
		super("Buffer");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		ClassHook nodeHook = getAnalyser("Node").getFoundHook();
		if(cn.superName.equals(nodeHook.obfuscated())) {
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

	public class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
					list.add(asMethodHook(m, "enableEncryption").var(MethodHook.TYPE, MethodHook.CALLBACK));
				} else if(!Modifier.isStatic(m.access)) {
					
					IControlFlowGraph graph = null;
					
					try {
						graph = Context.current().getCFGCache().get(m);
					} catch (ControlFlowException e) {
						e.printStackTrace();
					}
					
					if(m.desc.endsWith(")V")) {
							run(treeBuilder, arrayStoreVisitor, m, graph);
							analyse(arrayStoreVisitor, graph, m, list);

							arrayStoreVisitor.end(m);
					} /* else {
						Type ret = Type.getReturnType(m.desc);
						if(BytecodeRefactorer.isPrimitive(ret.getDescriptor())) {
							run(treeBuilder, arrayLoadVisitor, m, graph);
							
							List<ArithmeticOp> found = arrayLoadVisitor.found;
							List<Integer> subs = arrayLoadVisitor.subs;
							if(match(found, READ_UNSIGNED_16)) {
								if(match(subs, new Object[]{1, 2})) {
									list.add(asMethodHook(m, "readLE16").var(MethodHook.TYPE, MethodHook.CALLBACK));
								} else if(match(subs, new Object[]{2, 1})) {
									// Looks like each side of this gets swapped sometimes
									list.add(asMethodHook(m, "read16").var(MethodHook.TYPE, MethodHook.CALLBACK));
								}
								//System.out.println("dong " + found);
								//System.out.println("long " + subs);
								//System.out.println("BufferAnalyser.MethodAnalyser.find() " + m + " " + graph.hasLoop() + " " + found + " " + subs);
							} else if(match(found, READ_UNSIGNED_16B)) {
								//if(match(subs, new Object[]{1, 2})) {
								//	list.add(asMethodHook(m, "readLE16B"));
								//} else if(match(subs, new Object[]{2, 1})) {
								//	list.add(asMethodHook(m, "read16B"));
								//}
							}
							
							arrayLoadVisitor.end(m);
						}
					}*/
				}
			}

			//TODO: remember to remove halt
			//Context.current().requestHalt();
			// System.out.println(list.size());
			
			for(MethodHook mh : list) {
				for(MethodHook mh1 : list) {
					if(mh != mh1) {
						if(mh.refactored().equals(mh1.refactored())) {
							System.err.println("dup " + mh.refactored());
						}
					}
				}
				
				boolean b = false;
				for(String s : supportedMethods()) {
					String name = s.substring(0, s.indexOf("&"));
					if(name.equals(mh.refactored())) {
						if(b) {
							System.out.println("what b " + name);
						} else {
							b = true;
						}
					}
				}
				
				if(!b) {
					System.out.println("Not " + mh.refactored());
				}
				
			}
			
			
			return list;
		}

		protected void analyse(ArrayStoreVisitor asv, IControlFlowGraph graph, MethodNode m, List<MethodHook> list) {
			List<Object> index = asv.found.get(ArrayStoreVisitor.INDEX);
			List<Object> value = asv.found.get(ArrayStoreVisitor.VALUE);
			boolean b = false;

			//FIXME: ghetto
			if(m.desc.startsWith("(Ljava/lang/String;")) {

				if(match(index, new Object[]{1, 1})) {
					list.add(asMethodHook(m, "writeJagexString").var(MethodHook.TYPE, MethodHook.CALLBACK));
					b = true;
				} else {
					list.add(asMethodHook(m, "writeString").var(MethodHook.TYPE, MethodHook.CALLBACK));
					b = true;
				}
			} else if(m.desc.startsWith("(Ljava/lang/CharSequence;")) {
				list.add(asMethodHook(m, "writeCharSequence").var(MethodHook.TYPE, MethodHook.CALLBACK));
				b = true;
			} else {
				if(m.desc.startsWith("(J")) {
					if(match(value, WRITE_LONG_SHIFTS)) {
						list.add(asMethodHook(m, "write64").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					}
				} else if(m.desc.startsWith("(I")) {
					if(match(value, WRITE_INT_SHIFTS)) {
						if(match(index, new Object[]{4, SUB2_NODE, 3, SUB2_NODE, 2, SUB2_NODE, 1, SUB2_NODE})) {
							list.add(asMethodHook(m, "write32Weird").var(MethodHook.TYPE, MethodHook.CALLBACK));
							b = true;
						} else if(match(index, new Object[]{1, 1, 1, 1})) {
							list.add(asMethodHook(m, "write32").var(MethodHook.TYPE, MethodHook.CALLBACK));
							b = true;
						}

					} else if(match(value, WRITE_INV_LE_32)) {
						list.add(asMethodHook(m, "writeInvertedLE32").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE_LE_32)) {
						list.add(asMethodHook(m, "writeLE32").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE_INV_32)) {
						list.add(asMethodHook(m, "writeInverted32").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE_24)) {
						list.add(asMethodHook(m, "write24").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					}/* else if(match(value, WRITE_INV_24)) {
						System.out.println("BufferAnalyser.MethodAnalyser.find() " + index);
					}*/
					else if(match(value, WRITE_16)) {
						if(match(index, new Object[]{2, SUB2_NODE, 1, SUB2_NODE})) {
							list.add(asMethodHook(m, "writeLE16A").var(MethodHook.TYPE, MethodHook.CALLBACK));
							b = true;
						} else if(match(index, new Object[]{1, 1})) {
							list.add(asMethodHook(m, "write16").var(MethodHook.TYPE, MethodHook.CALLBACK));
							b = true;
						}
					} else if(match(value, WRITE_LE_16)) {
						list.add(asMethodHook(m, "writeLE16").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE_16A)) {
						list.add(asMethodHook(m, "write16A").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE_16B)) {
						list.add(asMethodHook(m, "write16B").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE8OFFSET128)) {
						list.add(asMethodHook(m, "write8Offset128").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE8NEG0)) {
						list.add(asMethodHook(m, "write8Neg0").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(value, WRITE8NEG128)) {
						list.add(asMethodHook(m, "write8Neg128").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(index, new Object[]{1}) && match(value, WRITE_8)) {
						list.add(asMethodHook(m, "write8").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else if(match(index, new Object[]{1, SUB2_NODE}) && match(value, WRITE_8)) {
						list.add(asMethodHook(m, "write8Weird").var(MethodHook.TYPE, MethodHook.CALLBACK));
						b = true;
					} else {
						run(treeBuilder, varIntNodeVisitor, m, graph);

						List<Object> jmp = varIntNodeVisitor.found.get(VarIntNodeVisitor.JMP);
						List<Object> add = varIntNodeVisitor.found.get(VarIntNodeVisitor.ADD);
						if(match(jmp, WRITE_VAR_BYTE) && match(add, new Object[]{32768})) {
							list.add(asMethodHook(m, "writeVarByte").var(MethodHook.TYPE, MethodHook.CALLBACK));
							b = true;
						}
						varIntNodeVisitor.end(m);
					}
				} else if(m.desc.startsWith("([BII")) {
					run(treeBuilder, arrayMethodVisitor, m, graph);
					analyseMultiByte(asv, arrayMethodVisitor, graph, m, list);
					arrayMethodVisitor.end(m);
				}
			}

			if(!b) {
				//print = true;
				asv.end(m);
				//print = false;
			} else {
				asv.end(m);
			}
		}

		public void analyseMultiByte(ArrayStoreVisitor asv, ArrayMethodVisitor amv, IControlFlowGraph graph, MethodNode m, List<MethodHook> list) {
			if(WRITE_BYTES.equals(amv.set)) {
				list.add(asMethodHook(m, "writeBytes").var(MethodHook.TYPE, MethodHook.CALLBACK));
			}
		}
	}

	public static void run(TreeBuilder tb, NodeVisitor nv, MethodNode m, IControlFlowGraph graph) {
		// dfs search
		for(FlowBlock block : graph) {
			tb.build(m, block).accept(nv);
		}
	}

	public static boolean match(List<?> index, Object[] objects) {
		return match(index, objects, 0);
	}

	public static boolean match(List<?> index, Object[] objects, int offset) {
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

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 14:28:15
	 */
	public static class ArrayLoadVisitor extends NotifyNodeVisitor {
		
		public final List<Integer> subs = new ArrayList<Integer>();
		public final List<ArithmeticOp> found = new ArrayList<ArithmeticOp>();
		
	    @Override
		public void visitOperation(ArithmeticNode an) {
	    	if(an.children() == 2 && an.firstNumber() != null) {
	    		boolean bool = hasIndexParent(an);
	    		if(!bool) {
	    			found.add(new ArithmeticOp(an.opcode(), an.firstNumber().number(), null));
		    		//System.out.printf("%s %s BufferAnalyser.ArrayLoadVisitor.visitOperation(%b)%n", Printer.OPCODES[an.opcode()], an.firstNumber(), bool);
	    		} else if(an.opcode() == ISUB) {
	    			subs.add(an.firstNumber().number());
	    		}
	    	}
	    }
	    
	    public boolean hasIndexParent(AbstractNode n) {
	    	//boolean ret = true;
	    	
	    	while(n != null && n.hasParent()) {
	    		if(n.opcode() == BALOAD)
	    			return true;
	    		
	    		//if(Printer.OPCODES[n.opcode()].contains("RETURN"))
	    		//	ret = false;
	    		
	    		n = n.parent();
	    	}
	    	
	    	return false;
	    	//return ret;
	    }
		
		@Override
		public void end(MethodNode m) {
			// System.out.println("end  " + m + "  " + found);
			// System.out.println("end2 " + m + " " + subs);
			found.clear();
			subs.clear();
		}
	}
	
	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:27:11
	 */
	public static class ArrayStoreVisitor extends NotifyNodeVisitor {

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
				
				//if(n.method().name.equals("ba")) {
				//System.out.println("ba " + n);
				//}
				//				
				//if(n.method().name.equals("by")) {
				//System.out.println("by " + n);
				//}
				
				AbstractNode indexChild = n.child(1);
				if(indexChild.opcode() == BALOAD) {
					indexChild = indexChild.child(1);
				}

				if(indexChild.opcode() == ISUB) {
					found.getNonNull(INDEX).add(InstructionUtil.resolve(indexChild.firstNumber().insn()));
				} else if(indexChild.opcode() == ILOAD) {
					found.getNonNull(INDEX).add(0);
				}

				if(indexChild.children() > 0 && indexChild.child(0).opcode() == ISUB) {
					indexChild = indexChild.child(0);
					found.getNonNull(INDEX).add(new Sub2Node());
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
					//System.out.flush();
					//System.out.println("n3 " + n.method() + " " + n.child(2));
					//System.out.println("n1 " + valueChild);
				}
			}
		}

		@Override
		public void end(MethodNode m) {
			if(print)
				System.out.println(m + "  " + found);
			found.clear();
		}
	}

	/**
	 * Represents something like this: <br>
	 * <code>this.b[this.e * 1512989863 - var1 - 4] = (byte)(var1 >> 24);</code> <br>
	 * where the index is calculated by subtracting the parameter itself.
	 * 
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:19:02
	 */
	public static class Sub2Node {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			return result * prime;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Sub2Node []";
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:44:05
	 */
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

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:44:11
	 */
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

			return "[ArithOp " + left + " then " + right + " (" + order + ")]";
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:44:16
	 */
	public static enum ArithmeticOpOrder {
		RIGHT, LEFT;
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:08:12
	 */
	public static class VarIntNodeVisitor extends NotifyNodeVisitor {
		public static final int JMP = 0x01, ADD = 0x02;

		public final NullPermeableHashMap<Integer, List<Object>> found = new NullPermeableHashMap<Integer, List<Object>>(new ValueCreator<List<Object>>() {
			@Override
			public List<Object> create() {
				return new ArrayList<Object>();
			}
		});

		@Override
		public void visitJump(JumpNode jn) {
			NumberNode nn = jn.firstNumber();
			if(nn != null && jn.firstVariable() != null) {
				found.getNonNull(JMP).add(new CompPair(jn.opcode(), nn.number()));
			}
		}

		@Override
		public void visitOperation(ArithmeticNode an) {
			if(an.opcode() == IADD) {
				NumberNode nn = an.firstNumber();
				if(nn != null && an.firstVariable() != null) {
					found.getNonNull(ADD).add(nn.number());
				}
			}
		}

		@Override
		public void end(MethodNode m) {
			//if(m.name.equals("c"))
			//System.out.println(found);

			found.clear();
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:08:16
	 */
	public static class CompPair {
		private final int opcode;
		private final int cst;

		public CompPair(int opcode, int cst) {
			this.opcode = opcode;
			this.cst = cst;
		}

		public int opcode() {
			return opcode;
		}

		public int cst() {
			return cst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + cst;
			result = prime * result + opcode;
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
			CompPair other = (CompPair) obj;
			if(cst != -1 && other.cst != -1)
				if (cst != other.cst)
					return false;
			if(opcode != -1 && other.opcode != -1)
				if (opcode != other.opcode)
					return false;
			return true;
		}

		@Override
		public String toString() {
			return "CompPair [opcode=" + opcode + ", cst=" + cst + "]";
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:23:03
	 */
	public static class ArrayMethodVisitor extends NotifyNodeVisitor {
		public ArraySet set;
		public int opcode;

		@Override
		public void visit(AbstractNode n) {
			if(n.opcode() == BASTORE) {

				ArraySetType dst = null;
				// should be the dst
				AbstractNode n5 = n.child(0);
				if(n5 instanceof VariableNode) {
					dst = ArraySetType.LOCAL;
				} else if (n5 instanceof FieldMemberNode) {
					dst = ArraySetType.FIELD;
				} else {
					System.err.println("wtf(1) " + n5);
				}

				AbstractNode n2 = find(n, BALOAD);
				// should be the src
				AbstractNode n3 = n2.child(0);

				ArraySetType src = null;

				if(n3 instanceof VariableNode) {
					src = ArraySetType.LOCAL;
				} else if (n3 instanceof FieldMemberNode) {
					src = ArraySetType.FIELD;
				} else {
					System.err.println("wtf(2) " + n2);
				}

				set = new ArraySet(src, dst);

			}
		}

		@Override
		public void visitJump(JumpNode jn) {
			if(jn.opcode() != GOTO)
				opcode = jn.opcode();
		}

		@Override
		public void end(MethodNode m) {

		}

		public static AbstractNode find(AbstractNode n, int opcode) {
			if(n == null)
				return null;

			if(n.opcode() == opcode)
				return n;

			for(int i=0; i < n.children(); i++) {
				AbstractNode c = n.child(i);
				if(c != null) {
					AbstractNode c2 = find(c, opcode);
					if(c2 != null)
						return c2;
				}
			}

			return null;
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:40:48
	 */
	public static class ArraySet {
		private final ArraySetType src;
		private final ArraySetType dst;

		public ArraySet(ArraySetType src, ArraySetType dst) {
			this.src = src;
			this.dst = dst;
		}

		public ArraySetType src() {
			return src;
		}

		public ArraySetType dst() {
			return dst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			ArraySet other = (ArraySet) obj;
			if(dst != null && other.dst != null)
				if (dst != other.dst)
					return false;
			if(src != null && other.src != null)
				if (src != other.src)
					return false;
			return true;
		}

		@Override
		public String toString() {
			return "ArraySet [src=" + src + ", dst=" + dst + "]";
		}
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:40:53
	 */
	public static enum ArraySetType {
		FIELD, LOCAL;
	}

	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 11:23:07
	 */
	public static enum ArrayMethodType {
		READ, WRITE;
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