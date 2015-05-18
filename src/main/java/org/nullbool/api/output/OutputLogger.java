package org.nullbool.api.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AbstractClassAnalyser;
import org.zbot.hooks.ClassHook;
import org.zbot.hooks.FieldHook;
import org.zbot.hooks.HookMap;
import org.zbot.hooks.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 19 Apr 2015 at 09:11:53 <br>
 */
public class OutputLogger {

	public static HookMap output() {
		AbstractAnalysisProvider provider = Context.current();
		int longestLine = 0;

		List<AbstractClassAnalyser> analysers = provider.getAnalysers();
		List<ClassHook> classes = new ArrayList<ClassHook>();
		analysers.forEach(a -> classes.add(a.getFoundHook()));

		Map<String, Boolean> flags = provider.getFlagsMap();
		boolean logResults = flags.getOrDefault("logresults", true);
		// if (!logResults)
		// return new HookMap(classes);

		boolean debug = flags.getOrDefault("debug", false);
		boolean printMultis = flags.getOrDefault("multis", true);
		boolean verify = flags.getOrDefault("verify", false);

		if (logResults)
			System.out.println();

		int fieldTotalFound = 0;
		int fieldTotalSupported = 0;
		int methodTotalFound = 0;
		int methodTotalSupported = 0;

		final int maxLength = 40;

		for (AbstractClassAnalyser analyser : analysers) {
			ClassHook classHook = analyser.getFoundHook();
			StringBuilder nameSb = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			nameSb.append("> ");
			nameSb.append(classHook.getRefactored());
			nameSb.append(" (-> ");
			boolean b = false;
			String toFind = provider.getClassNodes().get(classHook.getObfuscated()).superName;
			for (ClassHook c : classes) {
				if (c.equals(classHook))
					continue;
				if (c.getObfuscated().equals(toFind)) {
					b = true;
					nameSb.append(c.getRefactored());
				}
			}
			if (!b) {
				String t = toFind.substring(toFind.lastIndexOf('/') + 1);
				if (t.equals(classHook.getRefactored())) {
					nameSb.append(toFind);
				} else {
					nameSb.append(t);
				}
			}

			nameSb.append(") identified as ");
			nameSb.append(classHook.getObfuscated());

			if (debug) {
				sb.append("@SupportedHooks(fields = { ");
				for (FieldHook hook : classHook.getFields()) {
					sb.append("\"").append(hook.getName().getRefactored()).append("&").append(hook.getDesc().getRefactoredDesc(classes)).append("\", ");
				}
				sb.append("},");

				sb.append("\n");
				sb.append("methods = { ");
				for (MethodHook hook : classHook.getMethods()) {
					System.out.println(hook.getDesc().getClass());
					sb.append("\"").append(hook.getName().getRefactored()).append("&").append(hook.getDesc().getRefactoredDesc(classes)).append("\", ");
				}
				sb.append("})");
				sb.append("\n");
			}

			int fieldsFound = 0;
			int methodsFound = 0;

			fieldTotalSupported += analyser.supportedFields().length;
			methodTotalSupported += analyser.supportedMethods().length;

			for (String s : analyser.supportedFields()) {
				String[] parts = s.split("&");
				if (parts.length != 2) {
					System.out.println("wtf:" + s + " for " + analyser.getName());
					continue;
				}
				try {
				} catch (Exception e) {
					System.out.println("yolo " + s + " " + classHook.getRefactored());
					e.printStackTrace();
				}
				FieldHook hook = foundField(verify, parts[0], parts[1], classHook.getFields(), classes);

				if (hook == null) {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" ^  ");
					sb1.append(parts[0]);
					sb1.append(" (");
					sb1.append(parts[1]);
					sb1.append(") couldn't be identified.");
					sb.append(sb1.toString());
					sb.append("\n");
				} else {
					fieldsFound++;
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" ^  ");
					sb1.append(longstring(parts[0], maxLength));
					// sb1.append(longstring(" " + niceDesc(parts[1]) + "", maxLength));
					sb1.append(longstring(" " + hook.getDesc().getRefactoredDesc(classes) + "", maxLength));
					if (sb1.length() > longestLine) {
						longestLine = sb1.length();
					}
					sb.append(longstring(sb1.toString(), 47));
					sb.append("identified as ");
					StringBuilder sb2 = new StringBuilder();
					sb2.append(hook.getOwner().getObfuscated());
					sb2.append(".");
					sb2.append(hook.getName().getObfuscated());
					if (printMultis) {
						StringBuilder sb4 = new StringBuilder();
						sb4.append(longstring(sb2.toString(), 10));
						if (hook.getMultiplier() != 1) {
							sb4.append(" * ");
							if (hook.getMultiplier() >= 0)
								sb4.append(" ");
							sb4.append(hook.getMultiplier());
						}
						sb.append(longstring(sb4.toString(), 25));
					} else {
						sb.append(longstring(sb2.toString(), 12));
					}
					sb.append(" (");
					sb.append(hook.getDesc().getObfuscated());
					sb.append(")");
					sb.append("\n");
				}
			}

			for (String s : analyser.supportedMethods()) {
				String[] parts = s.split("&");
				if (parts.length != 2) {
					System.out.println("wtf:" + s + " for " + analyser.getName());
					continue;
				}
				try {
				} catch (Exception e) {
					System.out.println("yolo " + s + " " + classHook.getRefactored());
					e.printStackTrace();
				}
				MethodHook hook = foundMethod(verify, parts[0], parts[1], classHook.getMethods(), classes);
				if (hook == null) {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" ยบ  ");
					sb1.append(parts[0]);
					sb1.append(parts[1]);
					sb1.append(" couldn't be identified.");
					sb.append(sb1.toString());
					sb.append("\n");
				} else {
					methodsFound++;
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" บ  ");
					sb1.append(longstring(parts[0], maxLength));
					// sb1.append(longstring(" " + niceDescMethod(parts[1]), maxLength));
					sb1.append(longstring(" " + hook.getDesc().getRefactoredDesc(classes) + " ", maxLength));
					if (sb1.length() > longestLine) {
						longestLine = sb1.length();
					}
					sb.append(longstring(sb1.toString(), 47));
					sb.append("identified as ");
					StringBuilder sb2 = new StringBuilder();
					sb2.append(hook.getOwner().getObfuscated());
					sb2.append(".");
					sb2.append(hook.getName().getObfuscated());
					// if (PRINT_MUTLIS) {
					// sb.append(longstring(sb2.toString(), 25));
					// } else {
					// sb.append(longstring(sb2.toString(), 12));
					// }
					sb.append(longstring(sb2.toString(), printMultis ? 25 : 12));
					sb.append(" ");
					sb.append(hook.getDesc().getObfuscated());
					sb.append("");
					sb.append("\n");
				}
			}

			nameSb.append(" (").append(fieldsFound + methodsFound).append("/").append(analyser.supportedFields().length + analyser.supportedMethods().length)
					.append(")   ");
			boolean broke = (fieldsFound != analyser.supportedFields().length) || (methodsFound != analyser.supportedMethods().length);
			if (broke) {
				nameSb.append(" <--- BROKE");
			}
			fieldTotalFound += fieldsFound;
			methodTotalFound += methodsFound;
			if (logResults) {
				System.out.println(nameSb.toString());
				System.out.println(sb.toString());
			} else if (broke) {
				System.out.println("(Broke)");
			}
		}

		System.out.printf("Results for rev %s%n", provider.getRevision().getName());
		System.out.printf("(%d/%d) fields.%n", fieldTotalFound, fieldTotalSupported);
		System.out.printf("(%d/%d) methods.%n", methodTotalFound, methodTotalSupported);
		
		printDetails(classes);
		if (debug)
			System.out.println("Longest: " + longestLine);

		return new HookMap(classes);
	}

	private static void printDetails(List<ClassHook> classes) {
		System.out.printf("%d class hooks.%n", classes.size());
		int fCount = 0;
		int mCount = 0;
		for(ClassHook c : classes){
			fCount += c.getFields().size();
			mCount += c.getMethods().size();
		}

		System.out.printf("%d field hooks.%n", fCount);
		System.out.printf("%d method hooks.%n", mCount);
	}

	private static String longstring(String s, int l) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i = 0; i < (l - s.length()); i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	private static FieldHook foundField(boolean verify, String name, String desc, List<FieldHook> hooks, List<ClassHook> classes) {
		FieldHook h = null;
		for (FieldHook hook : hooks) {
			if (hook.getName().getRefactored().equals(name)) {
				if (h != null) {
					System.err.println(h);
					System.err.println(hook);
					throw new IllegalStateException("Found " + h.getOwner().getRefactored() + "." + h.getName().getRefactored() + " twice!");
				} else {
					hook.setMarked(true);
					h = hook;
				}
			}
		}
		if (verify)
			try {
				verifyField(classes, h);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return null;
			}
		return h;
	}

	private static void verifyField(List<ClassHook> cs, FieldHook h) {
		String owner = h.getOwner().getObfuscated();
		String name = h.getName().getObfuscated();
		String desc = h.getDesc().getObfuscated();

		for (ClassHook c : cs) {
			for (FieldHook f : c.getFields()) {
				if (h.equals(f))
					continue;
				String owner1 = f.getOwner().getObfuscated();
				String name1 = f.getName().getObfuscated();
				String desc1 = f.getDesc().getObfuscated();
				if (owner1.equals(owner)) {
					if (name1.equals(name)) {
						if (desc1.equals(desc)) {
							String refac = String.format("%s.%s %s", f.getOwner().getRefactored(), f.getName().getRefactored(),
									f.getDesc().getRefactoredDesc(cs));
							throw new IllegalStateException(String.format("Found %s.%s %s (%s) twice!", owner1, name1, desc1, refac));
						}
					}
				}
			}
		}
	}

	private static void verifyMethod(List<ClassHook> cs, MethodHook h) {
		String owner = h.getOwner().getObfuscated();
		String name = h.getName().getObfuscated();
		String desc = h.getDesc().getObfuscated();

		for (ClassHook c : cs) {
			for (MethodHook m : c.getMethods()) {
				if (h.equals(m))
					continue;
				String owner1 = m.getOwner().getObfuscated();
				String name1 = m.getName().getObfuscated();
				String desc1 = m.getDesc().getObfuscated();
				if (owner1.equals(owner)) {
					if (name1.equals(name)) {
						if (desc1.equals(desc)) {
							String refac = String.format("%s.%s %s", m.getOwner().getRefactored(), m.getName().getRefactored(),
									m.getDesc().getRefactoredDesc(cs));
							throw new IllegalStateException(String.format("Found %s.%s %s (%s) twice!", owner1, name1, desc1, refac));
						}
					}
				}
			}
		}
	}

	private static MethodHook foundMethod(boolean verify, String name, String desc, List<MethodHook> hooks, List<ClassHook> classes) {
		MethodHook h = null;
		for (MethodHook hook : hooks) {
			if (hook.getName().getRefactored().equals(name)) {
				if (h != null) {
					throw new IllegalStateException(h + ", " + hook + " found twice.");
				} else {
					hook.setMarked(true);
					h = hook;
				}
			}
		}
		if (verify)
			try {
				verifyMethod(classes, h);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return null;
			}
		return h;
	}
}