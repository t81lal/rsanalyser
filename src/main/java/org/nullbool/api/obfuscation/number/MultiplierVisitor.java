package org.nullbool.api.obfuscation.number;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

public class MultiplierVisitor extends NodeVisitor {

	private final MultiplierHandler handler;
	private int eCount;
	private int dCount;

	public MultiplierVisitor(MultiplierHandler handler) {
		this.handler = handler;
	}

	public MultiplierVisitor() {
		this(new MultiplierHandler());
	}

	public void log() {
		System.err.printf("Found %d encoders and %d decoders.%n", eCount, dCount);
	}

	public MultiplierHandler getHandler() {
		return handler;
	}

	private boolean isSetting(final ArithmeticNode an) {
		return an.hasParent() && ((an.parent().opcode() == PUTSTATIC) || (an.parent().opcode() == PUTFIELD));
	}

	@Override
	public void visitOperation(final ArithmeticNode an) {
		if (isSetting(an)) {
			final FieldMemberNode fmn = (FieldMemberNode) an.parent();
			final NumberNode nn = an.firstNumber();
			if (!fmn.desc().equals("I") || (nn == null) || (nn.opcode() != LDC))
				return;
			final int encoder = nn.number();
			if ((encoder % 2) != 0) {
				handler.addEncoder(fmn.key(), encoder);
				eCount++;
			}
		} else if (an.multiplying() && (an.children() == 2)) {
			final FieldMemberNode fmn = an.firstField();
			final NumberNode nn = an.firstNumber();
			if ((fmn == null) || !fmn.getting() || (nn == null) || (nn.opcode() != LDC))
				return;
			final int decoder = nn.number();
			if ((decoder % 2) != 0) {
				handler.addDecoder(fmn.key(), decoder);
				dCount++;
			}
		}
		// TODO check if encoder identification is good enough. if it is, use it to validate
		// multipliers
		// and inverse it for fields that decoders were not identified for
	}
}