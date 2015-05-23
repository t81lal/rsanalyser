package org.nullbool.api.util.filtactor;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class OpaquePredicateFilter implements Filter<AbstractInsnNode>, Opcodes {

	private static final List<Integer> CMP_INSNS = Arrays.asList(IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ICMPLT, IF_ICMPNE);

	private final int targetVar;

	public OpaquePredicateFilter(int targetVar) {
		this.targetVar = targetVar;
	}

	@Override
	public AbstractInsnNode accept(AbstractInsnNode ain) {
		if (ain.opcode() == NEW) {
			TypeInsnNode tin = (TypeInsnNode) ain;
			String desc = tin.desc;
			if (desc.equals("java/lang/IllegalStateException")) {
				AbstractInsnNode prev = ain.getPrevious();
				if (prev != null && CMP_INSNS.contains(prev.opcode())) {
					AbstractInsnNode numberNode = prev.getPrevious();
					if (numberNode != null) {
						AbstractInsnNode varNode = numberNode.getPrevious();
						if (varNode instanceof VarInsnNode) {
							int var = ((VarInsnNode) varNode).var;
							if (var == targetVar) {
								return varNode;
							}
						}
					}
				}
			}
		}
		return null;
	}
}