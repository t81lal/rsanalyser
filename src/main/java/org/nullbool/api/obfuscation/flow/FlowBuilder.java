package org.nullbool.api.obfuscation.flow;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.util.VBStyleCollection;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
public class FlowBuilder implements Opcodes{

	public static void create(MethodNode method) {
		short[] starts = findStartInstructions(method);
		Map<Integer, BasicBlock> mapInstrBlocks = new HashMap<Integer, BasicBlock>();
	    VBStyleCollection<BasicBlock, Integer> colBlocks = createBasicBlocks(starts, method, mapInstrBlocks);
	}
	
	private static short[] findStartInstructions(MethodNode method) {
		AbstractInsnNode[] insns   = method.instructions.toArray();
		Map<Label, Integer> labels = collectLabels(insns);
		int len                    = method.instructions.size();
		short[] inststates         = new short[len];

		Set<Integer> excSet = new HashSet<Integer>();
		Set<Label> exceptionLabels = new HashSet<Label>();
		for(TryCatchBlockNode block : method.tryCatchBlocks) {
			add(labels, exceptionLabels, block.start.getLabel());
			add(labels, exceptionLabels, block.end.getLabel());
			add(labels, exceptionLabels, block.handler.getLabel());
		}
		

		for (int i = 0; i < len; i++) {
			AbstractInsnNode ain = insns[i];
			
			// exception blocks
			if (excSet.contains(ain)) {
				inststates[i] = 1;
				continue;
			}

			switch(ain.type()) {
				case INSN: {
					switch(ain.opcode()) {
						case IRETURN:
						case LRETURN:
						case FRETURN:
						case DRETURN:
						case ARETURN:
						case RETURN:
							if (i + 1 < len) {
								inststates[i + 1] = 1;
							}
							break;
					}
					break;
				}
				case JUMP_INSN: {
					LabelNode target = ((JumpInsnNode) ain).label;
					markLabel(labels, inststates, target);
					break;
				}
				case TABLESWITCH_INSN: {
					TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
					markLabel(labels, inststates, tsin.dflt);
					for(LabelNode label : tsin.labels) {
						markLabel(labels, inststates, label);
					}
					if (i + 1 < len) {
						inststates[i + 1] = 1;
					}
					break;
				}
				case LOOKUPSWITCH_INSN: {
					LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) ain;
					markLabel(labels, inststates, lsin.dflt);
					for(LabelNode label : lsin.labels) {
						markLabel(labels, inststates, label);
					}
					if (i + 1 < len) {
						inststates[i + 1] = 1;
					}
					break;
				}
			}
		}

		// first instruction
		inststates[0] = 1;

		return inststates;
	}
	
	private static Map<Label, Integer> collectLabels(AbstractInsnNode[] insns) {
		Map<Label, Integer> map = new HashMap<Label, Integer>();
		for(int i=0; i < insns.length; i++) {
			AbstractInsnNode ain = insns[i];
			if(ain instanceof LabelNode) {
				LabelNode ln = (LabelNode) ain;
				map.put(ln.getLabel(), i);
			}
		}
		return map;
	}
	
	private static void add(Map<Label, Integer> labels, Set<Label> newSet, Label label) {
		if(!labels.containsKey(label)) {
			throw new RuntimeException("Wtf label...");
		}
		
		newSet.add(label);
	}
	
	private static void markLabel(Map<Label, Integer> labels, short[] arr, LabelNode ln) {
		markLabel(labels, arr, ln.getLabel());
	}
	
	private static void markLabel(Map<Label, Integer> labels, short[] arr, Label label) {
		int dest  = labels.get(label);
		arr[dest] = 1;
	}
}