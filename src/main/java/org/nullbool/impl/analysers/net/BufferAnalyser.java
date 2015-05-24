package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.flow.ControlFlowGraph;
import org.nullbool.api.util.DescFilter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
				methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V", 
							"writeInvertedLEInt&(I)V", "writeLE24&(I)V"})
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

			ReadWriteMethodVisitor visitor = new ReadWriteMethodVisitor();

			for(MethodNode m : cn.methods) {
				if(m.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "enableEncryption"));
				} else {
					if(visitor.preVisit(m, BASTORE)) {
						visitor.run();

						if(visitor.tCount == 4) {
							if(subequals(visitor.shiftOrders, new int[]{8, 0, 24, 16})) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeInvertedLEInt"));
								
								new ControlFlowGraph().create(m);
//								ControlFlowGraph.build(m);
//								FlowBuilder.create(m);
//								FlowDeobber deobber = new FlowDeobber();
//								Block.PRINT_INSNS = false;
//								deobber.accept(m);
//								System.out.println(m);
//								System.err.println(deobber.graph);

							}
						} else if(visitor.tCount == 3) {
							if(subequals(visitor.shiftOrders, new int[]{16, 8, 0})) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE24"));
							}
						}
					}
				}
			}

			return list;
		}
	}

	private static boolean subequals(List<Integer> list, int[] arr) {
		for(int i=0;i < arr.length; i++) {
			if(list.get(i) != arr[i])
				return false;
		}
		return true;
	}

	private static class ReadWriteMethodVisitor {

		private MethodNode method;
		private int target;

		private List<Integer> shiftOrders;
		private int tCount;

		public boolean preVisit(MethodNode m, int target) {
			if(m.desc.endsWith(")V")) {
				this.method = m;
				this.target = target;
				shiftOrders = new ArrayList<Integer>();
				tCount      = 0;

				return true;
			} else {
				return false;
			}
		}

		public void run() {
			List<LabelNode> visited = new ArrayList<LabelNode>();
			InsnList list = method.instructions;
			AbstractInsnNode ain = list.getFirst();
			while(ain != null) {
				if(ain.opcode() == target) {
					//					tCount++;

					follow(ain);
				} else if(ain.opcode() == GOTO) {
					JumpInsnNode jin = (JumpInsnNode) ain;
					LabelNode label = jin.label;
					if(!visited.contains(label)) {
						visited.add(label);
						AbstractInsnNode after = label.getNext();
						ain = after;
					}
				}

				ain = ain.getNext();
			}

			for(AbstractInsnNode ain1 : method.instructions.toArray()) {
				if(ain1.opcode() == target)
					tCount++;
			}

			if(tCount != shiftOrders.size()) {
				//				System.out.println(method + " for " + tCount + " " + shiftOrders);
				trim();
			}
		}

		private void trim() {
			int diff = shiftOrders.size() - tCount;
			if(diff <= 0) {
				return;
			}

			for(int i=shiftOrders.size() - 1; i >= tCount; i--) {
				shiftOrders.remove(i);
			}
		}

		private void follow(AbstractInsnNode ain) {			
			/*go backwards to find the iload 1 (the value we're writing)*/
			while(ain != null) {
				if(ain.opcode() == -1) {
					ain = ain.getPrevious();
					continue;
				}

				if(ain.opcode() == ILOAD) {
					VarInsnNode vin = (VarInsnNode) ain;
					if(vin.var == 1) {

						int shift = 0;

						/*now that it's been found, go forward to find either the cst shift or 
						 * if it has no shift, then just the cast.
						 * 
						 * since all shifts are done as integers using ishr or ishl, the value has
						 * to be cast to be added to the byte[], so it guaranteed to be there if the
						 * method is indeed a read/write method.*/

						AbstractInsnNode ain2 = vin;
						while(ain2 != null) {
							if(ain2.opcode() == I2B) {
								shift = 0;
								break;
							} else if(ain2.opcode() == BIPUSH || ain2.opcode() == SIPUSH) {
								//								if(ain2.opcode() == SIPUSH) {
								//									System.out.println("got a sipush");
								//								}

								shift = ((IntInsnNode) ain2).operand;
								break;
							}

							ain2 = ain2.getNext();
						}

						shiftOrders.add(shift);

						break;
					}
				}

				ain = ain.getPrevious();

				if(ain.opcode() == GOTO) {

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