package org.nullbool.api.obfuscation;

import java.util.concurrent.atomic.AtomicInteger;

import org.nullbool.api.Context;
import org.nullbool.api.util.map.NullPermeableHashMap;
import org.nullbool.api.util.map.ValueCreator;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.util.Printer;

/**
 * @author Bibl (don't ban me pls)
 * @created 12 Sep 2015 23:10:13
 */
public class OperationNeatener extends NodeVisitor {

	private static final String DOUBLE_CONST = "duplicate constant expression";
	
	private final NullPermeableHashMap<String, AtomicInteger> results = new NullPermeableHashMap<String, AtomicInteger>(new ValueCreator<AtomicInteger>() {
		@Override
		public AtomicInteger create() {
			return new AtomicInteger(0);
		}
	});

	@Override
	public void visitOperation(ArithmeticNode an) {
		if (an.children() != 2)
			return;

		NumberNode nn = an.firstNumber();
		AbstractNode other = an.child(0);
		if (other == nn) {
			other = an.child(1);
		}

		if (nn == null || other == null)
			return;

		if (other instanceof NumberNode) {
			/* If they're both constants, then what... */
			String opname = Printer.OPCODES[an.opcode()];
			char type = opname.charAt(0);
			opname = opname.substring(1);
			String key = String.format("%s (%c_%s)", DOUBLE_CONST, Character.toUpperCase(type), opname.toLowerCase());
			results.getNonNull(key).incrementAndGet();
			return;
		}
		
		if(an.adding()) {
			/* Possible expressions:
			 * 
			 * variable  + const     =
			 * const     + variable  =
			 * 
			 *  variable + -const    = 
			 *  -const   + variable  = 
			 *  
			 *  So what do we do?
			 *    1. If the constant is before the variable,
			 *       we put it after the variable (in reality
			 *       we insert it just before the operation
			 *       instruction so that we can ensure that
			 *       it comes after the constant 
			 */
		}
		
	}
	
	public void output() {
		boolean print = Context.current().getFlags().getOrDefault("basicout", true);
		if(print) {
			System.out.println();
		}
		
	}
}