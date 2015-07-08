package org.nullbool.impl.analysers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.util.EventCallGenerator;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.asm.insn.InstructionPattern;
import org.topdank.banalysis.asm.insn.InstructionSearcher;

/**
 * @author MalikDz
 */
@SupportedHooks(
		fields = { "getNPCs&[NPC", "getPlayers&[Player", "getRegion&Region", /*"getWidgetPositionsX&[I", "getWidgetPositionsY&[I",*/
		"getCanvas&Ljava/awt/Canvas;", "getLocalPlayer&Player", "getWidgetNodes&Hashtable", "getMenuActions&[Ljava/lang/String;", "isSpellSelected&Z",
		"getSelectionState&I", "getMenuOptions&[Ljava/lang/String;", "getLoopCycle&I", "getCurrentWorld&I", "getGameState&I", "getCurrentLevels&[I",
		"getRealLevels&[I", "getSkillsExp&[I", "getSelectedItem&I", "isMenuOpen&Z", "getMenuX&I", "getMenuY&I", "getMenuWidth&I", "getMenuHeight&I",
		"getMenuSize&I", "getGroundItems&[[[Deque", "getTileSettings&[[[B", "getTileHeights&[[[I", "getMapScale&I", "getMapOffset&I", "getMapAngle&I",
		"getPlane&I", "getCameraX&I", "getCameraY&I", "getCameraZ&I", "getCameraYaw&I", "getCameraPitch&I", "getBaseX&I", "getBaseY&I", "getWidgets&[[Widget",
		"getClientSettings&[I", "getWidgetsSettings&[I","getHoveredRegionTileX&I","getHoveredRegionTileY&I","getItemTables&Hashtable"}, 
		
		methods = { "loadObjDefinition&(I)LObjectDefinition;", "loadItemDefinition&(I)LItemDefinition;",
		/*"getPlayerModel&()LModel;",*/ "reportException&(Ljava/lang/Throwable;Ljava/lang/String;)WrappedException", "processAction&(IIIILjava/lang/String;Ljava/lang/String;II)V" })
public class ClientAnalyser extends ClassAnalyser {

	public ClientAnalyser() throws AnalysisException {
		super("Client");
	}

	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return new Builder<IFieldAnalyser>().addAll(new ActorArrayHook(), new CurrentRegionHook(), new WidgetPositionXY(), new CanvasPlayerHook(), new ClientArrayHooks(),
				new MenuScreenHooks(), new GroundItemsHook(), new TileInfoHooks(), new MinimapHooks(), new CameraHooks(), new BaseXYHooks(), new WidgetsHook(),
				new SettingsHook(), new CredentialAnalyser() , new RegionWalkingHooks() ,new ItemTableHook(), new CredentialHooks());
	}

	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return new Builder<IMethodAnalyser>().addAll(new LoadDefinitionHook(), new ReportMethodHookAnalyser(), new ProccessActionMethodHookAnalyser());
	}

	@Override
	public boolean matches(ClassNode c) {
		return c.name.equalsIgnoreCase("client");
	}
	
	public class CredentialHooks implements IFieldAnalyser {

		/* (non-Javadoc)
		 * @see org.nullbool.api.analysis.IFieldAnalyser#find(org.objectweb.asm.tree.ClassNode)
		 */
		@Override
		public List<FieldHook> find(ClassNode _cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
//            getstatic aq.ad:java.lang.String
//            invokestatic da v((Ljava/lang/CharSequence;)I);
//            invokestatic java/lang/Integer valueOf((I)Ljava/lang/Integer;);
//            invokevirtual java/util/LinkedHashMap containsKey((Ljava/lang/Object;)Z);

			InstructionPattern pattern = new InstructionPattern(new AbstractInsnNode[]{
					new FieldInsnNode(GETSTATIC, null, null, "Ljava/lang/String;"),
					new MethodInsnNode(INVOKESTATIC, null, null, "(Ljava/lang/CharSequence;)I", false),
					new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false),
					new MethodInsnNode(INVOKEVIRTUAL, "java/util/LinkedHashMap", "containsKey", "(Ljava/lang/Object;)Z", false)
			});
			
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";L.*;V", true);
			
			for(MethodNode m : mn) {
				InstructionSearcher searcher = new InstructionSearcher(m.instructions, pattern);
				if(searcher.search()) {
					
					System.out.println("Match in " + m.key());
					System.out.println("match; " + searcher.size());
					
					for(AbstractInsnNode[] ains : searcher.getMatches()) {
						AbstractInsnNode jin = ains[2].getNext();
						while(jin != null) {
							jin = jin.getNext();
							System.out.println(jin);
							if(jin.opcode() != -1)
								break;
						}
//						AbstractInsnNode target = jin.label.getNext();
//						System.out.println("targ: " + target.getNext());
					}
				}
			}
			return list;
		}
	}
	
	public class ItemTableHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn){
			List<FieldHook> hooks = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";IIII.{0,2};V", false);
			final MethodNode[] m = startWithBc(new String []{ "getstatic", "iload", "i2l" } , mn);

			String field = findField(m[0], false, true, 1, 's', "getstatic");
			hooks.add(asFieldHook(getNew(field.split("\\.")[0]), field,"getItemTables"));
			return hooks;
		}
	}
	
	public class RegionWalkingHooks implements IFieldAnalyser {
 
		@Override
		public List<FieldHook> find(ClassNode cn){
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), ";L.{0,3};IIIIII.{0,2};V", false);
			final MethodNode m = identifyMethod(mn, false, "iload 7", "bipush 7", "ishl");
 
			String h = findField(m, true, true, 1, 's', "iload 8","putstatic .* I");//wheres the class name?
			list.add(asFieldHook(getNew(h.split("\\.")[0]), h,"getHoveredRegionTileX"));
 
			h = findField(m, true, true, 1, 's', "iload 7","putstatic .* I");
			list.add(asFieldHook(getNew(h.split("\\.")[0]), h,"getHoveredRegionTileY"));
 
			return list;
		}
	}

	public class CanvasPlayerHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String type = "L" + "java/awt/Canvas" + ";";
			List<FieldHook> list = new ArrayList<FieldHook>();
			String hook = identifyField(Context.current().getClassNodes(), type);
			list.add(asFieldHook(hook, "getCanvas"));

			type = "L" + findObfClassName("Player") + ";";
			String p = identifyField(Context.current().getClassNodes(), type);
			list.add(asFieldHook(p, "getLocalPlayer"));

			return list;
		}
	}

	public class ActorArrayHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String hook, tempo;
			List<FieldHook> list = new ArrayList<FieldHook>();

			tempo = "[L" + findObfClassName("NPC") + ";";
			hook = identify(cn, tempo, 's');
			list.add(asFieldHook(hook, "getNPCs"));

			tempo = "[L" + findObfClassName("Player") + ";";
			hook = identify(cn, tempo, 's');
			list.add(asFieldHook(hook, "getPlayers"));

			return list;
		}
	}

	public class MinimapHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String h, regex = ";.{0,1};V";
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode m = identifyMethod(mn, false, "ldc 120.0");

			h = findField(m, true, true, 1, 's', "ldc 120.0");
			list.add(asFieldHook(h, "getMapScale"));

			h = findField(m, true, true, 1, 's', "ldc 30.0");
			list.add(asFieldHook(h, "getMapOffset"));

			h = findField(m, true, true, 1, 's', "ldc 20.0");
			list.add(asFieldHook(h, "getMapAngle"));

			return list;
		}
	}

	public class CameraHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String h, regex = ";III\\w{0,1};V";
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode m = startWithBc(Context.current().getPattern("camera"), mn)[0];

			h = findNearIns(m, "invokestatic", "put", "get");
			list.add(asFieldHook(h, "getPlane"));

			h = findField(m, true, false, 1, 's', "isub", "istore 0");
			list.add(asFieldHook(h, "getCameraX"));

			h = findField(m, true, false, 1, 's', "isub", "istore 1");
			list.add(asFieldHook(h, "getCameraY"));

			h = findField(m, true, false, 1, 's', "imul", "isub", "istore 4");
			list.add(asFieldHook(h, "getCameraZ"));

			h = findField(m, true, false, 1, 's', "iaload", "istore 7");
			list.add(asFieldHook(h, "getCameraYaw"));

			h = findField(m, true, false, 1, 's', "iaload", "istore 5");
			list.add(asFieldHook(h, "getCameraPitch"));

			return list;
		}
	}

	public class MenuScreenHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {

			String h, regex = ";I.{0,2};V";
			List<FieldHook> list = new ArrayList<FieldHook>();
			// The actual pattern is getstatic, getstatic, invokevirtual but
			// because the we inline strings and the 2nd getstatic is a 
			// string constant, the pattern has to be changed. 
			String[] p = { "getstatic", "getstatic", "invokevirtual" };
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);

			MethodNode[] m = startWithBc(p, mn);
			System.out.println("m: " + m[0]);
			AbstractInsnNode[] ins = followJump(m[0], 323);
			final String[] pattern = { "if_icmple", "iload 6", "ifge","iconst_1" };
			
			h = findField(ins, true, true, 1, 's', pattern);
			list.add(asFieldHook(h, "isMenuOpen"));

			h = findField(ins, true, true, 2, 's', pattern);
			list.add(asFieldHook(h, "getMenuX"));

			h = findField(ins, true, true, 3, 's', pattern);
			list.add(asFieldHook(h, "getMenuY"));

			h = findField(ins, true, true, 4, 's', pattern);
			list.add(asFieldHook(h, "getMenuWidth"));

			h = findField(ins, true, true, 5, 's', pattern);
			list.add(asFieldHook(h, "getMenuSize"));

			h = findField(ins, true, true, 6, 's', pattern);
			list.add(asFieldHook(h, "getMenuHeight"));

			return list;
		}
	}

	public class ClientArrayHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String h, regex = ";;V";
			String[] p = { "iconst_1", "putstatic" };
			String[] pattern = { "bipush 9", "iconst_2", "iastore" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, false);
			MethodNode[] ms = startWithBc(p, mn);

			MethodNode m = identifyMethod(ms, false, pattern);
			AbstractInsnNode[] ins = followJump(m, 323);

			String[] pat = { "iconst_1", "putstatic", "iconst_0" };
			String[] r = { "aconst_null", "putstatic .* Ljava/lang/String;", "iconst_0" };
			r = new String[] { "putstatic .*", "new", "dup", "bipush 8" };
			h = findField(ins, true, true, 2, 's', r);
			list.add(asFieldHook(h, "getWidgetNodes"));

			h = findField(ins, true, true, 1, 's', "sipush 500", "anewarray");
			list.add(asFieldHook(h, "getMenuActions"));

			h = findField(ins, true, true, 9, 's', "sipush 500", "anewarray");
			list.add(asFieldHook(h, "isSpellSelected"));

			h = findField(ins, true, true, 7, 's', "sipush 500", "anewarray");
			list.add(asFieldHook(h, "getSelectionState"));

			h = findField(ins, true, true, 2, 's', "sipush 500", "anewarray");
			list.add(asFieldHook(h, "getMenuOptions"));

			h = findField(ins, false, true, 2, 's', pat);
			list.add(asFieldHook(h, "getLoopCycle"));

			h = findField(ins, true, true, 2, 's', "iconst_1");
			list.add(asFieldHook(h, "getCurrentWorld"));

			h = findField(ins, true, true, 8, 's', "iconst_1");
			list.add(asFieldHook(h, "getGameState"));

			h = findField(ins, true, true, 1, 's', "bipush 25", "newarray 10");
			list.add(asFieldHook(h, "getCurrentLevels"));

			h = findField(ins, true, true, 2, 's', "bipush 25", "newarray 10");
			list.add(asFieldHook(h, "getRealLevels"));

			h = findField(ins, true, true, 3, 's', "bipush 25", "newarray 10");
			list.add(asFieldHook(h, "getSkillsExp"));

			h = findField(ins, true, true, 1, 's', r);
			list.add(asFieldHook(h, "getSelectedItem"));

			return list;
		}
	}

	public class TileInfoHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String hook, regex = ";III\\w{0,1};I";
			List<FieldHook> list = new ArrayList<FieldHook>();
			String bytesPattern = "getstatic \\w*.\\w* \\[\\[\\[B";
			String heightPattern = "getstatic \\w*.\\w* \\[\\[\\[I";

			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(mn, false, "bipush 103");

			hook = findField(method, true, false, 1, 's', bytesPattern);
			list.add(asFieldHook(hook, "getTileSettings"));

			hook = findField(method, true, false, 1, 's', heightPattern);
			list.add(asFieldHook(hook, "getTileHeights"));

			return list;
		}
	}

	public class BaseXYHooks implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String obj = "L" + findObfClassName("Actor");
			String h, regex = ";\\w*" + obj + ";" + "\\w*;V";
			List<FieldHook> list = new ArrayList<FieldHook>();
			String mPattern = "invokestatic java/lang/Math.atan2 (DD)D";
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
			MethodNode method = identifyMethod(mn, false, mPattern);
			AbstractInsnNode[] ins = followJump(method, 120);

			h = findField(ins, false, false, 1, 's', "isub", "isub", "istore");
			list.add(asFieldHook(h, "getBaseX"));

			h = findField(ins, false, true, 1, 's', "isub", "isub", "istore");
			list.add(asFieldHook(h, "getBaseY"));

			return list;
		}
	}

	public class SettingsHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String hook, r = ";;V";
			String[] pat = { "bipush", "newarray" };
			String[] pat2 = { "sipush 2000", "newarray 10" };
			List<FieldHook> list = new ArrayList<FieldHook>();
			MethodNode[] mn = findMethods(Context.current().getClassNodes(), r, true);
			MethodNode[] ms = startWithBc(pat, mn);
			MethodNode m = identifyMethod(ms, false, pat2);

			hook = findField(m, true, true, 1, 's', pat2);
			list.add(asFieldHook(hook, "getClientSettings"));

			hook = findField(m, true, true, 2, 's', pat2);
			list.add(asFieldHook(hook, "getWidgetsSettings"));

			return list;
		}
	}

	public class WidgetPositionXY implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			//TODO: WIDGETS
			
			// String hook, regex = ";;V";
			List<FieldHook> list = new ArrayList<FieldHook>();

			/*List<MethodNode> methods = new ArrayList<MethodNode>();
			Context.current()
					.getClassNodes()
					.values()
					.forEach(
							x -> methods.addAll(x.methods.stream().filter(m -> m.desc.startsWith("(IIIII")).filter(m1 -> fourIaloads(m1))
									.collect(Collectors.toList())));

			for (MethodNode m : methods) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out/test/" + m.owner.name + " " + m.name.replace("<", "").replace(">", "")
						+ " " + m.desc.replace("<", "").replace(">", ""))))) {
					bw.write(m.owner.name + " " + m.name + " " + m.desc);
					for (String s : InstructionPrinter.getLines(m)) {
						bw.write(s);
						bw.newLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/
			// String[] mPattern = { "iconst_1", "putstatic" };
			// MethodNode[] mn = findMethods(Context.current().getClassNodes(), regex, true);
			// MethodNode method = startWithBc(mPattern, mn)[0];

			// AbstractInsnNode[] ins = followJump(method, 220);
			// String[] p = { "bipush 100", "newarray 10", "putstatic client\\.\\w* \\[I", "bipush 100", "newarray 10", "putstatic client\\.\\w* \\[I"
			// };
			//
			// hook = findField(ins, true, true, 1, 's', p);
			//
			// list.add(asFieldHook(hook, "getWidgetPositionsX"));
			//
			// hook = findField(ins, true, true, 2, 's', p);
			// list.add(asFieldHook(hook, "getWidgetPositionsY"));

			return list;
		}

		private boolean fourIaloads(MethodNode m1) {
			for (AbstractInsnNode ain : m1.instructions.toArray()) {
				if (ain.opcode() == GETSTATIC || ain.opcode() == PUTSTATIC) {
					FieldInsnNode fin = (FieldInsnNode) ain;
					if (fin.name.equals("lv") || fin.name.equals("lu"))
						return true;
				}
			}
			return false;
		}
	}

	// TODO: METHODS
	public class LoadDefinitionHook implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode _unused) {
			List<MethodHook> list = new ArrayList<MethodHook>();
			String npcClass = findObfClassName("NPC");
			ClassNode cn = getClassNodeByRefactoredName("Renderable");
			MethodNode[] ms = getMethodNodes(cn.methods.toArray());
			String playerClass = findObfClassName("Player");
			MethodNode m = identifyMethod(ms, true, "aconst_null", "areturn");
			String v = "L" + findObfClassName("ItemDefinition") + ";";
			String mn, t = "L" + findObfClassName("ObjectDefinition") + ";";
			//
			MethodNode mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + t);
			MethodHook mhook = getAnalyser("ObjectDefinition").asMethodHook(mNode, "loadObjDefinition").var(Constants.METHOD_TYPE, Constants.CALLBACK);
			list.add(mhook);
			//
			mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + v);
			mhook = getAnalyser("ItemDefinition").asMethodHook(mNode, "loadItemDefinition").var(Constants.METHOD_TYPE, Constants.CALLBACK);
			list.add(mhook);
			//
			mn = npcClass + "." + m.name;
			mNode = findMethodNode(Context.current().getClassNodes(), ";I.{0,1};" + v);
			//
			//mn = playerClass + "." + m.name;
			//mhook = getAnalyser("Model").asMethodHook(mn, "getPlayerModel").var(Constants.METHOD_TYPE, Constants.CALLBACK);
			//list.add(mhook);

			// these hooks are actually method hook, I think we should make a
			// method format look

			// String name = "ObjectDefinition";
			// AbstractClassAnalyser analyser = getAnalyser(name);
			// ClassNode aCn = analyser.getFoundClass();
			// String defName = String.format("L%s;", aCn.name);
			// for (ClassNode cn : Context.current().getClassNodes().values()) {
			// for (MethodNode m : cn.methods) {
			// if (!Modifier.isStatic(m.access))
			// continue;
			// String desc = m.desc;
			// if (desc.startsWith("(I") && desc.endsWith(defName)) {
			// MethodHook hook = getAnalyser(name).asMethodHook(m, "loadObjDefinition");
			// list.add(hook);
			// }
			// }
			// }

			return list;
		}

		private MethodNode findMethodNode(Map<String, ClassNode> nodes, String r) {
			String g = "[()]";
			Iterator<ClassNode> it = nodes.values().iterator();
			while (it.hasNext()) {
				ClassNode cn = it.next();
				for (Object m : cn.methods)
					if ((Modifier.isStatic(((MethodNode) m).access)))
						if (((MethodNode) m).desc.replaceAll(g, ";").matches(r))
							return (MethodNode) m;
			}
			return null;
		}

		private String findMethod(Map<String, ClassNode> nodes, String r) {
			String g = "[()]", result = null;
			Iterator<ClassNode> it = nodes.values().iterator();
			while (it.hasNext()) {
				ClassNode cn = it.next();
				for (Object m : cn.methods)
					if ((Modifier.isStatic(((MethodNode) m).access)))
						if (((MethodNode) m).desc.replaceAll(g, ";").matches(r))
							return cn.name + "." + ((MethodNode) m).name;
			}
			return result;
		}
	}

	public class WidgetsHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			String t = "[[L" + findObfClassName("Widget") + ";";
			String widgetField = identifyField(Context.current().getClassNodes(), t);
			list.add(asFieldHook(widgetField, "getWidgets"));

			return list;
		}
	}

	public class GroundItemsHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			String t = "[[[L" + findObfClassName("Deque") + ";";
			String p = identifyField(Context.current().getClassNodes(), t);
			List<FieldHook> list = new ArrayList<FieldHook>();
			list.add(asFieldHook(p, "getGroundItems"));

			return list;
		}
	}

	public class CurrentRegionHook implements IFieldAnalyser {

		@Override
		public List<FieldHook> find(ClassNode cn) {
			List<FieldHook> list = new ArrayList<FieldHook>();

			String type = "L" + findObfClassName("Region") + ";";
			String p = identifyField(Context.current().getClassNodes(), type);
			list.add(asFieldHook(p, "getRegion"));

			return list;
		}
	}

	public class ReportMethodHookAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode _cn) {
			List<MethodHook> hooks = new ArrayList<MethodHook>();
			for(ClassNode cn : Context.current().getClassNodes().values()) {
				for(MethodNode m : cn.methods) {
					if (m.desc.startsWith("(Ljava/lang/Throwable;Ljava/lang/String;)") && m.desc.contains(")L")) {
						MethodHook mhook = asMethodHook(m, "reportException").var(Constants.METHOD_TYPE, Constants.PATCH);
						hooks.add(mhook);
						VarInsnNode beforeReturn = null;
						try {
							for (AbstractInsnNode ain : m.instructions.toArray()) {
								if (ain.opcode() == ARETURN) {
									if (beforeReturn != null)
										System.err.println("WTF BOI");
									beforeReturn = (VarInsnNode) ain.getPrevious();
								}
							}
							/* 1. Generate the event creation. 2. Call the dispatch method. */

							// InsnList objCreateList = new InsnList();
							// objCreateList.add(EventCallGenerator.generateEventCreate("org/zbot/api/event/ErrorEvent", "(Lorg/zbot/accessors/IWrappedException;)V",
							// new VarInsnNode(beforeReturn.getOpcode(), beforeReturn.var), // load the raw object
							// // cast it to an IWrappedException
							// new TypeInsnNode(CHECKCAST, APIGenerator.ACCESSOR_BASE + APIGenerator.API_CANONICAL_NAMES.get("WrappedException"))));
							// InsnList newInsns = EventCallGenerator.generateDispatch(objCreateList);

							InsnList newInsns = new InsnList();
							newInsns.add(EventCallGenerator.generatePrintLn(new LdcInsnNode("[Client] Create error.")));
							newInsns.add(EventCallGenerator.generatePrintLn(new VarInsnNode(ALOAD, 1)));

							newInsns.add(new VarInsnNode(ALOAD, 0));
							newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V", false));

							InsnList list2 = new InsnList();
							list2.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
							list2.add(new InsnNode(DUP));

							list2.add(new LdcInsnNode("[Client] "));
							list2.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));

							list2.add(new VarInsnNode(ALOAD, 0));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getCanonicalName", "()Ljava/lang/String;", false));
							list2.add(new MethodInsnNode(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));

							list2.add(new LdcInsnNode(" "));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));

							list2.add(new VarInsnNode(ALOAD, 0));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;", false));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
							list2.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "valueOf", "()Ljava/lang/String;", false));

							newInsns.add(EventCallGenerator.generatePrintLn(list2.toArray()));

							InsnList mInsns = m.instructions;
							mInsns.insertBefore(beforeReturn, newInsns);
							mhook.insns(mInsns);
							mhook.var(Constants.MAX_STACK, "7");
							mhook.var(Constants.MAX_LOCALS, Integer.toString(m.maxLocals));
							mInsns.reset();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			
			
			//for (MethodNode m : getClassNodeByRefactoredName("ExceptionReporter").methods) {
			//
			//}
			
			return hooks;
		}
	}

	public class ProccessActionMethodHookAnalyser implements IMethodAnalyser {

		@Override
		public List<MethodHook> find(ClassNode _cn) {
			List<MethodHook> hooks = new ArrayList<MethodHook>();
			String descStart = "(IIIILjava/lang/String;Ljava/lang/String;II";
			for (ClassNode cn : Context.current().getClassNodes().values()) {
				for (MethodNode m : cn.methods) {
					if (m.desc.startsWith(descStart)) {
						InsnList insns = new InsnList();
						final String sb = "java/lang/StringBuilder";
						final String intAppendDesc = "(I)Ljava/lang/StringBuilder;";
						final String stringAppendDesc = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
						final String append = "append";

						insns.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
						insns.add(new TypeInsnNode(NEW, sb));
						insns.add(new InsnNode(DUP));

						insns.add(new LdcInsnNode("[doAction] Op: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, "<init>", "(Ljava/lang/String;)V", false));

						insns.add(new VarInsnNode(ILOAD, 2));
						insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
						insns.add(new LdcInsnNode(", Arg1: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ILOAD, 0));
						insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
						insns.add(new LdcInsnNode(", Arg2: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ILOAD, 1));
						insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
						insns.add(new LdcInsnNode(", Arg0: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ILOAD, 3));
						insns.add(new MethodInsnNode(INVOKEVIRTUAL, sb, append, intAppendDesc, false));
						insns.add(new LdcInsnNode(", Action: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ALOAD, 4));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new LdcInsnNode(", Target: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ALOAD, 5));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));
						insns.add(new LdcInsnNode(", var6: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ILOAD, 6));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, intAppendDesc, false));
						insns.add(new LdcInsnNode(", var7: "));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, stringAppendDesc, false));

						insns.add(new VarInsnNode(ILOAD, 7));
						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, append, intAppendDesc, false));

						insns.add(new MethodInsnNode(INVOKESPECIAL, sb, "toString", "()Ljava/lang/String;", false));
						insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));

						MethodHook h = asMethodHook(m, "processAction")
								.var(Constants.METHOD_TYPE, Constants.PATCH)
								.var(Constants.PATCH_POSITION, Constants.START);
						h.insns(insns);

						hooks.add(h);
					}
				}
			}
			return hooks;
			// static final void processAction (
			// int arg1, int arg2, int opcode, int arg0, String action, String target,
			// int mouseX, int mouseY, xxx DUMMY
			// )
		}
	}
	
	public class CredentialAnalyser implements IFieldAnalyser {
		 
		@Override
		public List<FieldHook> find(ClassNode cn){
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			//TODO: FIX
			/*
			final String methodPattern = ";L" + findObfClassName("Gameshell") + ";.{0,1};V";
			MethodNode[] filteredMethods = findMethods(Context.current().getClassNodes(), methodPattern, true);
			MethodNode method = startWithBc(new String[] { "getstatic", "ifeq" }, filteredMethods)[0];
			final String[] fieldPattern = { "iconst_0", "putstatic", "ldc", "putstatic", "ldc", "putstatic", "iconst_0" };
 
			String h = findField(method, false, true, 2, 's', fieldPattern);
			list.add(asFieldHook("getUsername()", h));
 
			h = findField(method, false, true, 3, 's', fieldPattern);
			list.add(asFieldHook("getPassword()", h));*/
			
			return list;
		}
	}


	public static void test(Throwable t) {
		System.out.println("[Client] " + t.getClass().getCanonicalName() + " " + t.getMessage());
	}

	static final void processAction(int arg1, int arg2, int opcode, int arg0, String action, String target, int mouseX, int mouseY, int DUMMY) {
		System.out.println("[doAction] Op: " + opcode + ", Arg1: " + arg1 + ", Arg2: " + arg2 + ", Arg0: " + arg0 + ", Action: " + action + ", Target: "
				+ target + ", var6: " + mouseX + ", var7: " + mouseY);
	}
}