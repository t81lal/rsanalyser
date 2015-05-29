package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowException;
import org.nullbool.api.obfuscation.cfg.FlowBlock;
import org.nullbool.api.util.DescFilter;
import org.nullbool.api.util.InstructionUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V", 
		"writeInvertedLEInt&(I)V", "writeLE24&(I)V", "writeInt&(I)V", "writeLEInt&(I)V", "write24Int&(I)V"})
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
					if(visitor.preVisit(m, ReadWriteMethodVisitor.MARK_SHIFTS | ReadWriteMethodVisitor.MARK_SUBS | ReadWriteMethodVisitor.MARK_ANDS, BASTORE)) {
						visitor.run();

						boolean b = false;

						int tCount = visitor.shiftOrders.size();
						if(tCount == 4) {
							if(subequals(visitor.shiftOrders, new int[]{8, 0, 24, 16})) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeInvertedLEInt"));
								b = true;
							} else if(subequals(visitor.shiftOrders, new int[]{24, 16, 8, 0})) {
								if(subequals(visitor.subOrders, new int[]{1, 1, 1, 1})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeInt"));
									b = true;
								} else if(subequals(visitor.subOrders, new int[]{4, 3, 2, 1})) {
									list.add(asMethodHook(MethodType.CALLBACK, m, "writeLEInt"));
									b = true;
								}
							}
						} else if(tCount == 3) {
							if(subequals(visitor.shiftOrders, new int[]{16, 8, 0})) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "writeLE24Int"));
								b = true;
							} else if(subequals(visitor.shiftOrders, new int[]{8, 16, 0})) {
								list.add(asMethodHook(MethodType.CALLBACK, m, "write24Int"));
								b = true;
							}
						}

						if(!b) {
							System.err.printf("%s: %s, %s, %s.%n", m, visitor.shiftOrders, visitor.subOrders, visitor.andOrders);
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

		public static final int MARK_SHIFTS = 0x01, MARK_SUBS = 0x02, MARK_ANDS = 0x04;

		private MethodNode method;
		private int flags;
		private int target;

		private List<Integer> shiftOrders;
		private List<Integer> subOrders;
		private List<Integer>  andOrders;

		private ReadWriteMethodVisitor() {
			shiftOrders = new ArrayList<Integer>();
			subOrders   = new ArrayList<Integer>();
			andOrders   = new ArrayList<Integer>();
		}

		public boolean preVisit(MethodNode m, int flags, int target) {
			if(m.desc.endsWith(")V")) {
				this.method = m;
				this.flags  = flags;
				this.target = target;
				shiftOrders.clear();
				subOrders.clear();
				return true;
			} else {
				return false;
			}
		}

		//TODO: Iterate through the (obfuscated) cfg to find the order
		//      instead of following gotos.
		public void run() {
			boolean shifts = (flags & MARK_SHIFTS) == MARK_SHIFTS;
			boolean subs   = (flags & MARK_SUBS)   == MARK_SUBS;
			boolean ands   = (flags & MARK_ANDS)   == MARK_ANDS;

			int tCount = 0;
			int sCount = 0;
			int aCount = 0;

			try {
				for(FlowBlock block : Context.current().getCFGCache().get(method)) {
					/*InsnList list = method.instructions;
					AbstractInsnNode ain = list.getFirst();*/
					AbstractInsnNode ain = block.first();
					while(ain != null && !ain.equals(block.last())) {
						if(shifts && ain.opcode() == target) {
							tCount++;
							followShift(ain);
						}

						if(subs && ain.opcode() == ISUB) {
							sCount++;
							followSub(ain);
						}

						if(ands && ain.opcode() == IAND) {
							aCount++;
							followAnd(ain);
						}

						/* 27/05/15, 18:07  We fix the execution path of the code now so the
						 * 					natural order of the code should be correct.
						else if(ain.opcode() == GOTO) {
							JumpInsnNode jin = (JumpInsnNode) ain;
							LabelNode label = jin.label;
							if(!visited.contains(label)) {
								visited.add(label);
								AbstractInsnNode after = label.getNext();
								ain = after;
							}
						}*/

						ain = ain.getNext();
					}
				}
			} catch(ControlFlowException e) {
				e.printStackTrace();
			}

			if(shifts && tCount != shiftOrders.size()) 
				trim(shiftOrders, tCount);

			if(subs && sCount != subOrders.size()) 
				trim(subOrders, sCount);

			if(ands && aCount != andOrders.size()) 
				trim(andOrders, aCount);

			//System.out.println(method + " " + shiftOrders);
			//System.out.println(method + " " + subOrders);
			//System.out.println(method + " " + andOrders);
		}

		private void trim(List<Integer> list, int count) {
			int diff = list.size() - count;
			if(diff <= 0) {
				return;
			}

			for(int i=list.size() - 1; i >= count; i--) {
				list.remove(i);
			}
		}

		private void followShift(AbstractInsnNode ain) {			
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

						/* now that it's been found, go forward to find either the cst shift or 
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
			}
		}

		private void followSub(AbstractInsnNode ain) {
			AbstractInsnNode prev = ain.getPrevious();
			if(prev != null) {
				int number = InstructionUtil.resolve(prev);
				if(number != -1) 
					subOrders.add(number);
			}
		}

		private void followAnd(AbstractInsnNode ain) {
			AbstractInsnNode prev = ain.getPrevious();
			if(prev != null) {
				int number = InstructionUtil.resolve(prev);
				if(number != -1) 
					andOrders.add(number);
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