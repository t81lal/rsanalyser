package org.nullbool.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nullbool.api.util.BoundedInstructionIdentifier.DataPoint;
import org.objectweb.custom_asm.tree.AbstractInsnNode;
import org.objectweb.custom_asm.tree.FieldInsnNode;
import org.objectweb.custom_asm.tree.IntInsnNode;
import org.objectweb.custom_asm.tree.LdcInsnNode;
import org.objectweb.custom_asm.tree.MethodInsnNode;
import org.objectweb.custom_asm.tree.VarInsnNode;
import org.objectweb.custom_asm.util.Printer;

public class BoundedInstructionIdentifier implements Iterable<DataPoint> {

	private static PointCreator DEFAULT_CREATOR_IMPL;

	private final PointCreator defaultCreator;
	private final List<DataPoint> points;

	protected BoundedInstructionIdentifier() {
		this(getDefaultPointCreatorImpl());
	}

	public BoundedInstructionIdentifier(PointCreator creator) {
		defaultCreator = creator;
		points = new ArrayList<DataPoint>();
	}

	public BoundedInstructionIdentifier(AbstractInsnNode[] ains) {
		this(getDefaultPointCreatorImpl(), ains);
	}

	public BoundedInstructionIdentifier(PointCreator creator,
			AbstractInsnNode... ains) {
		this(creator);
		for (AbstractInsnNode ain : ains) {
			if(ain.getOpcode() != -1)
				addPoint(ain);
		}
	}

	public void addPoint(AbstractInsnNode ain) {
		addPoint(defaultCreator.create(ain));
	}

	public void addPoint(DataPoint point) {
		points.add(point);
	}
	
	public List<DataPoint> getPoints() {
		return points;
	}

	@Override
	public Iterator<DataPoint> iterator() {
		return points.listIterator();
	}

	static PointCreator getDefaultPointCreatorImpl() {
		if (DEFAULT_CREATOR_IMPL == null) {
			DEFAULT_CREATOR_IMPL = new ExplicitPointCreator();
		}
		return DEFAULT_CREATOR_IMPL;
	}

	public abstract static class DataPoint {
		@Override
		public abstract String toString();

		public abstract AbstractInsnNode instruction();
	}

	public abstract static interface PointCreator {
		public abstract DataPoint create(AbstractInsnNode ain);
	}

	public static class ExplicitPointCreator implements PointCreator {

		@Override
		public DataPoint create(AbstractInsnNode ain) {
			return new ExplicitDataPoint(ain);
		}
	}

	public static class ShyPointCreator implements PointCreator {

		@Override
		public DataPoint create(AbstractInsnNode ain) {
			return new ShyDataPoint(ain);
		}
	}

	public abstract static class CacheableDataPoint extends DataPoint {
		protected final AbstractInsnNode ain;
		private final String cached;

		public CacheableDataPoint(AbstractInsnNode ain) {
			this.ain = ain;
			cached = create(ain);
		}

		abstract String create(AbstractInsnNode ain);


		@Override
		public AbstractInsnNode instruction() {
			return ain;
		}
		
		@Override
		public String toString() {
			return cached;
		}
	}

	public static class ShyDataPoint extends CacheableDataPoint {

		public ShyDataPoint(AbstractInsnNode ain) {
			super(ain);
		}

		@Override
		String create(AbstractInsnNode ain) {
			return Printer.OPCODES[ain.getOpcode()].toLowerCase();
		}
	}

	public static class ExplicitDataPoint extends ShyDataPoint {

		public ExplicitDataPoint(AbstractInsnNode ain) {
			super(ain);
		}

		@Override
		public String create(AbstractInsnNode ain) {
			String extra = "";
			if (ain instanceof FieldInsnNode) {
				FieldInsnNode f = ((FieldInsnNode) ain);
				extra = f.owner + "." + f.name + " " + f.desc;
			}
			if (ain instanceof MethodInsnNode) {
				MethodInsnNode m = ((MethodInsnNode) ain);
				extra = m.owner + "." + m.name + " " + m.desc;
			}
			if (ain instanceof VarInsnNode) {
				VarInsnNode m = ((VarInsnNode) ain);
				extra = String.valueOf(m.var);
			}
			if (ain instanceof LdcInsnNode) {
				LdcInsnNode m = ((LdcInsnNode) ain);
				extra = String.valueOf(m.cst);
			}
			if (ain instanceof IntInsnNode) {
				IntInsnNode m = ((IntInsnNode) ain);
				extra = String.valueOf(m.operand);
			}

			return super.create(ain) + (extra.length() > 0 ? " " + extra : "");
		}
	}
}