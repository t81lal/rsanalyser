package org.nullbool.impl.analysers.world;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.StaticDescFilter;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 17 Jul 2015 16:13:50
 */
@SupportedHooks(fields = { "state&I", "timeout&J", "stream&Ljava/io/DataInputStream;", "payload&[B", "sizePayload&[B" }, methods = { })
public class WorldListDownloaderAnalyser extends ClassAnalyser {

	/**
	 * @param name
	 */
	public WorldListDownloaderAnalyser() {
		super("WorldListDownloader");
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#matches(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected boolean matches(ClassNode cn) {
		int longs = getFieldCount(cn, new StaticDescFilter("J"));
		int ints = getFieldCount(cn, new StaticDescFilter("I"));
		int dis = getFieldCount(cn, new StaticDescFilter("Ljava/io/DataInputStream;"));
		int barrs = getFieldCount(cn, new StaticDescFilter("[B"));

		if(longs < 1 || ints < 3 || dis < 1 || barrs < 2)
			return false;
		
		boolean getMethod = false;
		boolean init = false;
	
		
		for(MethodNode m : cn.methods) {
			if(!Modifier.isStatic(m.access)) {
				if(m.name.equals("<init>")) {
					if(m.desc.startsWith("(L") && m.desc.endsWith("Ljava/net/URL;)V")) {
						init = true;
					}
				} else if(m.desc.equals("()[B")) {
					getMethod = true;
				}
			}
		}
		
		return init && getMethod;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerFieldAnalysers()
	 */
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().add(new ConstructorFieldsAnalyser());
	}

	/* (non-Javadoc)
	 * @see org.nullbool.api.analysis.ClassAnalyser#registerMethodAnalysers()
	 */
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return null;
	}
	
	public class ConstructorFieldsAnalyser implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			TreeBuilder tb = new TreeBuilder();

			NodeVisitor ctorVisitor = new NodeVisitor() {
				@Override
				public void visitField(FieldMemberNode fmn) {
					FieldInsnNode fin = fmn.fin();
					if(fmn.opcode() == PUTFIELD && fin.owner.equals(cn.name)) {						
						if(fin.desc.equals("J")) {
							for(AbstractNode an : fmn.traverse()) {
								if(an instanceof NumberNode) {
									NumberNode nn = (NumberNode) an;
									// 30000 is the timeout value												
									if(nn.type().equals(Long.TYPE) && nn.number() == 30000) {
										list.add(asFieldHook(fin, "timeout"));
									}
								}
							}
						} else if(fin.desc.equals("I")) {
							NumberNode nn = fmn.firstNumber();
							if(nn != null && nn.number() == 0) {
								list.add(asFieldHook(fin, "state"));
							}
						}
					}
				}
			};
			
			NodeVisitor payloadVisitor = new NodeVisitor() {
				@Override
				public void visitField(FieldMemberNode fmn) {
					FieldInsnNode fin = fmn.fin();
					if(fmn.opcode() == PUTFIELD) {
						if(fin.desc.equals("Ljava/io/DataInputStream;")) {
							if(fmn.layer(CHECKCAST) != null) {
								list.add(asFieldHook(fin, "stream"));
							}
						}
					}
				}
				
				@Override
				public void visitMethod(MethodMemberNode mmn) {
					if(mmn.opcode() == INVOKEVIRTUAL) {
			    		ClassNode buffer = getClassNodeByRefactoredName("Buffer");
			    		if(mmn.owner().equals(buffer.name)) {
			    			MethodMemberNode rmmn = mmn.firstMethod();
			    			if(rmmn != null) {
			    				FieldMemberNode fmn = rmmn.firstField();
			    				if(fmn != null && fmn.desc().equals("[B")) {
			    					FieldInsnNode fin = fmn.fin();
			    					list.add(asFieldHook(fin, "sizePayload"));
			    				}
			    			}
			    		}
					}
				}
				
				@Override
				public void visit(AbstractNode an) {
					if(an.opcode() == ARETURN) {
						FieldMemberNode fmn = an.firstField();
						if(fmn != null) {
							FieldInsnNode fin = fmn.fin();
							if(fmn.opcode() == GETFIELD && fin.desc.equals("[B")) {
								list.add(asFieldHook(fin, "payload"));
							}
						}
					}
				}
			};
			
			for(MethodNode m : cn.methods) {
				NodeVisitor nv = null;
				if(m.name.equals("<init>") && m.desc.endsWith("Ljava/net/URL;)V")) {
					nv = ctorVisitor;
				} else if(m.desc.equals("()[B")) {
					nv = payloadVisitor;
				}
				if(nv != null){
					tb.build(m).accept(nv);
				}
			}
			
			return list;
		}
	}
}