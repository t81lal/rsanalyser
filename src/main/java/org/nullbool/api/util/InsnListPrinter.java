package org.nullbool.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.FrameNode;
import org.objectweb.custom_asm.tree.IincInsnNode;
import org.objectweb.custom_asm.tree.InsnList;
import org.objectweb.custom_asm.tree.InsnNode;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.JumpInsnNode;
import org.objectweb.custom_asm.tree.LabelNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.LineNumberNode;
import org.objectweb.custom_asm.tree.LookupSwitchInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.MultiANewArrayInsnNode;
import org.objectweb.custom_asm.tree.TableSwitchInsnNode;
import org.objectweb.custom_asm.tree.TypeInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPrinter;

/**
 * @author Bibl (don't ban me pls)
 * @created 24 May 2015
 */
public class InsnListPrinter extends InstructionPrinter {

	private ListIterator<AbstractInsnNode> it;
	
	public InsnListPrinter(InsnList insns) {
		super(null);
		it = insns.iterator();
	}
	
	public InsnListPrinter(List<AbstractInsnNode> col) {
		super(null);
		it = col.listIterator();
	}

	@Override
	public ArrayList<String> createPrint() {
		ArrayList<String> info = new ArrayList<String>();
		int i = 0;
		while (it.hasNext()) {
			i++;
			AbstractInsnNode ain = (AbstractInsnNode) it.next();
			String line = "";
			if (ain instanceof VarInsnNode) {
				line = printVarInsnNode((VarInsnNode) ain, it);
			} else if (ain instanceof IntInsnNode) {
				line = printIntInsnNode((IntInsnNode) ain, it);
			} else if (ain instanceof FieldInsnNode) {
				line = printFieldInsnNode((FieldInsnNode) ain, it);
			} else if (ain instanceof MethodInsnNode) {
				line = printMethodInsnNode((MethodInsnNode) ain, it);
			} else if (ain instanceof LdcInsnNode) {
				line = printLdcInsnNode((LdcInsnNode) ain, it);
			} else if (ain instanceof InsnNode) {
				line = printInsnNode((InsnNode) ain, it);
			} else if (ain instanceof JumpInsnNode) {
				line = printJumpInsnNode((JumpInsnNode) ain, it);
			} else if (ain instanceof LineNumberNode) {
				line = printLineNumberNode((LineNumberNode) ain, it);
			} else if (ain instanceof LabelNode) {
				line = printLabelnode((LabelNode) ain);
			} else if (ain instanceof TypeInsnNode) {
				line = printTypeInsnNode((TypeInsnNode) ain);
			} else if (ain instanceof FrameNode) {
				line = printFrameNode((FrameNode) ain);
			} else if (ain instanceof IincInsnNode) {
				line = printIincInsnNode((IincInsnNode) ain);
			} else if (ain instanceof TableSwitchInsnNode) {
				line = printTableSwitchInsnNode((TableSwitchInsnNode) ain);
			} else if (ain instanceof LookupSwitchInsnNode) {
				line = printLookupSwitchInsnNode((LookupSwitchInsnNode) ain);
			} else if (ain instanceof MultiANewArrayInsnNode) {
				line = printMultiANewArrayInsnNode((MultiANewArrayInsnNode) ain);
			} else {
				line += "UNKNOWN-NODE: " + (ain.getOpcode() >= 0 ? nameOpcode(ain.getOpcode()) : "") + " " + ain.toString();
			}
			if (!line.equals("")) {
				if (match)
					if (matchedInsns.contains(ain))
						line = "   -> " + line;
				
				line = i + ". " + line;
				info.add(line);
			}
		}
		return info;
	}
	
	@Override
	protected String printLineNumberNode(LineNumberNode lin, ListIterator<?> it) {
		return "[Line " + lin.line + "]";
	}
}