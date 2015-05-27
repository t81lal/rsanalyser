package org.nullbool.impl.analysers.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields = {"getResults&[I", "getMem&[I", "getCount&I"}, methods = {"init&()V", "next&()I", "isaac&()V"})
/**
 * @author Bibl (don't ban me pls)
 * @created 23 May 2015
 */
public class IsaacCipherAnalyser extends ClassAnalyser {
	
	private static final int[] CREATE_ARRAY_PATTERN = new int[]{SIPUSH, NEWARRAY, PUTFIELD};
	private static final int[] SEED_SET_PATTERN = new int[]{GETFIELD, ILOAD, ALOAD, ILOAD, IALOAD, IASTORE};
	private static final int[] INVOKE_PATTERN = new int[]{INVOKEVIRTUAL};
	private static final int[] RESET_COUNT_PATTERN = new int[]{ALOAD, LDC, PUTFIELD};
	
	private FieldInsnNode rslFin;
	private FieldInsnNode memFin;
	private MethodInsnNode initMin;
	
	public IsaacCipherAnalyser() {
		super("IsaacCipher");
	}

	@Override
	protected boolean matches(ClassNode cn) {
		for(MethodNode m : cn.methods) {
			if(m.name.equals("<init>")) {
				if(m.desc.startsWith("([I")) {
					List<AbstractInsnNode[]> arrayInits = findAllOpcodePatterns(m, CREATE_ARRAY_PATTERN);
					if(arrayInits.size() >= 2) {
						rslFin = (FieldInsnNode) findOpcodePattern(m, SEED_SET_PATTERN);
						if(rslFin != null) {
							memFin = (FieldInsnNode) getOther(arrayInits, rslFin);
							initMin = (MethodInsnNode) findOpcodePattern(m, INVOKE_PATTERN);
							return true;
						}
					}
				}
			}
		}
		
		return false;
		
//		if(!cn.superName.equals("java/lang/Object"))
//			return false;
//		
//		int ints = (int) getFieldOfTypeCount(cn, "I", false);
//		int intarrs = (int) getFieldOfTypeCount(cn, "\\[I", false);
//		
//		if((ints + intarrs) != cn.fields.size())
//			return false;
//	
//		return ints == 4 && intarrs == 2;
	}
	
	private static FieldInsnNode getOther(List<AbstractInsnNode[]> set, FieldInsnNode a) {
		for(AbstractInsnNode[] ains : set) {
			for(AbstractInsnNode ain : ains) {
				if(ain instanceof FieldInsnNode) {
					FieldInsnNode fin = (FieldInsnNode) ain;
					if(!fin.owner.equals(a.owner) || !fin.name.equals(a.name) || !fin.desc.equals(a.desc))
						return fin;
				}
			}
		}
		return null;
	}

	@Override
	protected List<IFieldAnalyser> registerFieldAnalysers() {		
		return Arrays.asList(new FieldAnalyser());
	}

	@Override
	protected List<IMethodAnalyser> registerMethodAnalysers() {
		return Arrays.asList(new MethodAnalyser());
	}
	
	private MethodNode nextMethod;
	private MethodInsnNode isaacMethod;
	
	private void findNext(ClassNode cn) {
		NextMethodNodeVisitor visitor = new NextMethodNodeVisitor();
		TreeBuilder treeBuilder = new TreeBuilder();
		
		for(MethodNode m : cn.methods) {
			if(visitor.preVisit(m)) {
				treeBuilder.build(m).accept(visitor);
				
				if(visitor.isNext()) {
					/*if more than 1 thing was found the pattern is borked*/
					if(nextMethod != null || isaacMethod != null) {
						nextMethod  = null;
						isaacMethod = null;
						return;
					}
					
					nextMethod  = m;
					isaacMethod = visitor.isaac;
				}
			}
		}
	}
	
	private class FieldAnalyser implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			findNext(cn);
			
			if(nextMethod != null) {
				List<AbstractInsnNode[]> ainsList = findAllOpcodePatterns(nextMethod, RESET_COUNT_PATTERN);
				//if it's there, the length will be 3 since there can only be full matches
				if(ainsList.size() > 0) {
					FieldInsnNode fin = (FieldInsnNode) ainsList.get(0)[2];
					list.add(asFieldHook(fin, "getCount"));
				}
			}
			
			list.add(asFieldHook(rslFin, "getResults"));
			list.add(asFieldHook(memFin, "getMem"));
			
			return list;
		}
	}
	
	private class MethodAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode cn) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			
			/*initMin will be set if this method is called.*/
			list.add(asMethodHook(MethodType.CALLBACK, initMin, "init"));
			
			if(nextMethod != null) {
				list.add(asMethodHook(MethodType.CALLBACK, nextMethod, "next"));
			}
			
			if(isaacMethod != null) {
				list.add(asMethodHook(MethodType.CALLBACK, isaacMethod, "isaac"));
			}
	             
			return list;
		}
	}
	
	private static class NextMethodNodeVisitor extends NodeVisitor {
		
		/*
		 * r78 next
		 * 
		 *final e(int arg0) { //(I)I
         *TryCatch: L1 to L2 handled by L2: java/lang/RuntimeException
         *L1 {
         *    iconst_0
         *    aload0 // reference to self
         *    dup
         *    getfield IsaacCipher.w:int
         *    ldc 1804347247 (java.lang.Integer)
         *    isub
         *    dup_x1
         *    putfield IsaacCipher.w:int
         *    ldc 1879381903 (java.lang.Integer)
         *    imul
         *    iconst_1
         *    iadd
         *    if_icmpne L3
         *    iload1
         *    ldc 465793668 (java.lang.Integer)
         *    if_icmplt L4
         *    new java/lang/IllegalStateException
         *    dup
         *    invokespecial java/lang/IllegalStateException <init>(()V);
         *    athrow
         *}
         *L4 {
         *    aload0 // reference to self
         *    bipush 115
         *    invokevirtual IsaacCipher a((B)V);
         *}
         *L5 {
         *    aload0 // reference to self
         *    ldc 547047313 (java.lang.Integer)
         *    putfield IsaacCipher.w:int
         *}
         *L3 {
         *    aload0 // reference to self
         *    getfield IsaacCipher.getResults:int[]
         *    ldc 1879381903 (java.lang.Integer)
         *    aload0 // reference to self
         *    getfield IsaacCipher.w:int
         *    imul
         *    iaload
         *    ireturn
         *}
         *L2 {
         *    new java/lang/StringBuilder
         *    dup
         *    invokespecial java/lang/StringBuilder <init>(()V);
         *    ldc "dt.e(" (java.lang.String)
         *    invokevirtual java/lang/StringBuilder append((Ljava/lang/String;)Ljava/lang/StringBuilder;);
         *    ldc 41 (java.lang.Integer)
         *    invokevirtual java/lang/StringBuilder append((C)Ljava/lang/StringBuilder;);
         *    invokevirtual java/lang/StringBuilder toString(()Ljava/lang/String;);
         *    invokestatic ExceptionReporter a((Ljava/lang/Throwable;Ljava/lang/String;)LWrappedException;);
         *    athrow
         * }
     	 * }
		 *
		 *
		 */
		
		private MethodNode method;
		
		private boolean forceFail;
		private boolean isaacCall;
		private MethodInsnNode isaac;
		private boolean iaload;
		
		public boolean isNext() {
			if(forceFail){
				return false;
			}
			return isaacCall && iaload;
		}
		
		public boolean preVisit(MethodNode m) {
			if(m.desc.endsWith(")I")) {
				this.method = m;
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void visitCode() {
	    	if(method == null)
	    		throw new RuntimeException("No method!");
	    	
	    	super.visitCode();
	    	
	    	forceFail = false;
	    	isaacCall = false;
	    	iaload    = false;
	    	isaac     = null;
	    }

	    @Override
		public void visitEnd() {
	    	super.visitEnd();
	    	method = null;
	    }
	    
	    @Override
		public void visitMethod(MethodMemberNode mmn) {
	    	super.visitMethod(mmn);
	    	
	    	if(forceFail){
	    		return;
	    	}
	    	
	    	MethodInsnNode min = mmn.min();
	    	String isaacClass  = method.owner.name;
	    	if(min.owner.equals(isaacClass)) {
	    		isaacCall = true;
	    		isaac     = min;
	 	  	}
	    }
	    
		@Override
		public void visit(AbstractNode n) {
			super.visit(n);
			
			if(forceFail) {
				return;
			}
			/*The next method only has 1 array access*/
			if(n.opcode() == IALOAD && n.children() == 2 && n.hasChild(GETFIELD)) {
				if(iaload) {
					forceFail = true;
				} else {
					iaload    = true;
				}
			}
		}
	}
}