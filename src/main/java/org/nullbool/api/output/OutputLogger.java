package org.nullbool.api.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.DynamicDesc;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.MethodHook;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 19 Apr 2015 at 09:11:53 <br>
 */
@Deprecated
public class OutputLogger {

	public static boolean hasMulti(String desc) {
		return desc.equals("I") || desc.equals("B") || desc.equals("S") || desc.equals("J");
	}
	
	public static void resolveMultis(List<ClassHook> classes) {
		MultiplierHandler mh = Context.current().getMultiplierHandler();
		for(ClassHook ch : classes) {
			for(FieldHook fh : ch.fields()) {
				String desc = fh.val(Constants.DESC);
				if(hasMulti(desc)) {
					String src = fh.owner().obfuscated() + "." + fh.obfuscated();
					
					long m = mh.getEncoder(src);
					if(m == 0) {
						m = mh.getDecoder(src);
					}
					
					if(m == 0)
						m = 1;
					
					fh.var(Constants.ENCODER, Long.toString(m));
				}
			}
		}
	}
	
	public static HookMap output() {
		long start = System.currentTimeMillis();
		AbstractAnalysisProvider provider = Context.current();
		int longestLine = 0;

		List<ClassAnalyser> analysers = provider.getAnalysers();
		List<ClassHook> classes = new ArrayList<ClassHook>();
		analysers.forEach(a -> classes.add(a.getFoundHook()));

		Map<String, Boolean> flags = provider.getFlags();
		boolean logResults = flags.getOrDefault("logresults", true);
		// if (!logResults)
		// return new HookMap(classes);

		boolean debug = flags.getOrDefault("debug", false);
		boolean printMultis = flags.getOrDefault("multis", true);
		boolean verify = flags.getOrDefault("verify", false);
		
		if(printMultis) {
			resolveMultis(classes);
		}

		if (logResults)
			System.out.println();

		int fieldTotalSupported = 0;
		int methodTotalSupported = 0;
		
		int fhf = 0;
		int mhf = 0;

		final int maxLength = 40;

		for (ClassAnalyser analyser : analysers) {
			
			List<String> unidf = new ArrayList<String>();
			List<String> unidm = new ArrayList<String>();
			
			ClassHook classHook = analyser.getFoundHook();
			StringBuilder nameSb = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			nameSb.append("> ");
			nameSb.append(classHook.refactored());
			nameSb.append(" (-> ");
			boolean b = false;
			String toFind = provider.getClassNodes().get(classHook.obfuscated()).superName;
			for (ClassHook c : classes) {
				if (c.equals(classHook))
					continue;
				if (c.obfuscated().equals(toFind)) {
					b = true;
					nameSb.append(c.refactored());
				}
			}
			if (!b) {
				String t = toFind.substring(toFind.lastIndexOf('/') + 1);
				if (t.equals(classHook.refactored())) {
					nameSb.append(toFind);
				} else {
					nameSb.append(t);
				}
			}

			nameSb.append(") identified as ");
			nameSb.append(classHook.obfuscated());

			if (debug) {
				sb.append("@SupportedHooks(fields = { ");
				for (FieldHook hook : classHook.fields()) {
					DynamicDesc dd = new DynamicDesc(hook.val(Constants.DESC), false);
					sb.append("\"").append(hook.refactored()).append("&").append(dd.getRefactoredDesc(classes)).append("\", ");
				}
				sb.append("},");

				sb.append("\n");
				sb.append("methods = { ");
				for (MethodHook hook : classHook.methods()) {
					DynamicDesc dd = new DynamicDesc(hook.val(Constants.DESC), true);
					System.out.println(dd.getClass());
					sb.append("\"").append(hook.refactored()).append("&").append(dd.getRefactoredDesc(classes)).append("\", ");
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
					System.out.println("yolo " + s + " " + classHook.refactored());
					e.printStackTrace();
				}
				FieldHook hook = foundField(verify, parts[0], parts[1], classHook.fields(), classes);

				if (hook == null) {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" ^  ");
					sb1.append(parts[0]);
					sb1.append(" (");
					sb1.append(parts[1]);
					sb1.append(") couldn't be identified.");
					sb.append(sb1.toString());
					sb.append("\n");
					
					unidf.add(s);
					// System.out.printf("%s %s broke!%n", parts[0], parts[1]);
				} else {
					fieldsFound++;
					fhf++;
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" ^  ");
					sb1.append(longstring(parts[0], maxLength));
					// sb1.append(longstring(" " + niceDesc(parts[1]) + "", maxLength));
					DynamicDesc fdd = new DynamicDesc(hook.val(Constants.DESC), false);
					sb1.append(longstring(" " + fdd.getRefactoredDesc(classes) + "", maxLength));
					if (sb1.length() > longestLine) {
						longestLine = sb1.length();
					}
					sb.append(longstring(sb1.toString(), 47));
					sb.append("identified as ");
					StringBuilder sb2 = new StringBuilder();
					sb2.append(hook.owner().obfuscated());
					sb2.append(".");
					sb2.append(hook.obfuscated());
					if (printMultis) {
						StringBuilder sb4 = new StringBuilder();
						
						if(hasMulti(fdd.getObfuscated())) {
							sb4.append(longstring(sb2.toString(), 10));
							long multi = Long.parseLong(hook.val(Constants.ENCODER, "1"));
							sb4.append(" * ");
							if (multi >= 0)
								sb4.append(" ");
							sb4.append(multi);
						} else {
							sb4.append(longstring(sb2.toString(), 10));
						}
						
						sb.append(longstring(sb4.toString(), 25));
					} else {
						sb.append(longstring(sb2.toString(), 12));
					}
					sb.append(" (");
					sb.append(hook.val(Constants.DESC));
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
					System.out.println("yolo " + s + " " + classHook.refactored());
					e.printStackTrace();
				}
				MethodHook hook = foundMethod(verify, parts[0], parts[1], classHook.methods(), classes);
				if (hook == null) {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" º  ");
					sb1.append(parts[0]);
					sb1.append(parts[1]);
					sb1.append(" couldn't be identified.");
					sb.append(sb1.toString());
					sb.append("\n");
					
					unidm.add(s);
				} else {
					methodsFound++;
					mhf++;
					StringBuilder sb1 = new StringBuilder();
					sb1.append(" º  ");
					sb1.append(longstring(parts[0], maxLength));
					// sb1.append(longstring(" " + niceDescMethod(parts[1]), maxLength));
					DynamicDesc mdd = new DynamicDesc(hook.val(Constants.DESC), true);
					sb1.append(longstring(" " + mdd.getRefactoredDesc(classes) + " ", maxLength));
					if (sb1.length() > longestLine) {
						longestLine = sb1.length();
					}
					sb.append(longstring(sb1.toString(), 47));
					sb.append("identified as ");
					StringBuilder sb2 = new StringBuilder();
					sb2.append(hook.owner().obfuscated());
					sb2.append(".");
					sb2.append(hook.obfuscated());
					// if (PRINT_MUTLIS) {
					// sb.append(longstring(sb2.toString(), 25));
					// } else {
					// sb.append(longstring(sb2.toString(), 12));
					// }
					sb.append(longstring(sb2.toString(), printMultis ? 25 : 12));
					sb.append(" ");
					sb.append(hook.val(Constants.DESC));
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
//			fieldTotalFound += fieldsFound;
//			methodTotalFound += methodsFound;
			if (logResults) {
				System.out.println(nameSb.toString());
				System.out.println(sb.toString());
			} else if (broke) {
				System.err.printf("%s broke.%n", classHook.refactored());
				System.err.flush();
				System.out.flush();
				
				for(String uf : unidf) {
					String[] parts = uf.split("&");
					System.err.flush();
					System.err.printf("  ^ %s %s broke.%n", parts[0], parts[1]);
				}
				
				for(String um : unidm) {
					String[] parts = um.split("&");
					System.err.flush();
					System.err.printf("  ^ %s %s broke.%n", parts[0], parts[1]);
				}
				
				System.err.flush();
				System.out.flush();
			}
		}

		System.out.printf("Results for rev %s (d=%dms, a=%dms, o=%dms)%n", provider.getRevision().getName(), provider.getDeobTime(), provider.getAnalysisTime(), (System.currentTimeMillis() - start));
		System.out.printf("(%d/%d) classes (%d hooks).%n", classes.size(), analysers.size(), classes.size());
		System.out.printf("(%d/%d) fields (%d hooks).%n", fhf, fieldTotalSupported, f_collect(classes).size());
		System.out.printf("(%d/%d) methods (%d hooks).%n", mhf, methodTotalSupported, m_collect(classes).size());
		
		if(logResults)
			printDetails(classes);
		
		if (debug)
			System.out.println("Longest: " + longestLine);

		
		

		MultiplierHandler mh = Context.current().getMultiplierHandler();
		mh.test();
		
		
		
		return new HookMap(classes);
	}
	
	private static Set<FieldHook> f_collect(Collection<ClassHook> classes) {
		Set<FieldHook> set = new HashSet<FieldHook>();
		for(ClassHook c : classes) {
			set.addAll(c.fields());
		}
		return set;
	}

	private static Set<MethodHook> m_collect(Collection<ClassHook> classes) {
		Set<MethodHook> set = new HashSet<MethodHook>();
		for(ClassHook c : classes) {
			set.addAll(c.methods());
		}
		return set;
	}
	
	private static void printDetails(List<ClassHook> classes) {
		System.out.printf("%d class hooks.%n", classes.size());
		int fCount = 0;
		int mCount = 0;
		for(ClassHook c : classes){
			fCount += c.fields().size();
			mCount += c.methods().size();
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
			if (hook.refactored().equals(name)) {
				if (h != null) {
					if(hook != h) {
						System.err.println(h);
						System.err.println(hook);
						throw new IllegalStateException("Found " + h.owner().refactored() + "." + h.refactored() + " twice!");
					}
				} else {
					hook.var("marked", "true");
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
		String owner = h.owner().obfuscated();
		String name = h.obfuscated();
		String desc = h.val(Constants.DESC);

		for (ClassHook c : cs) {
			for (FieldHook f : c.fields()) {
				if (h.equals(f))
					continue;
				String owner1 = f.owner().obfuscated();
				String name1 = f.obfuscated();
				String desc1 = f.val(Constants.DESC);
				if (owner1.equals(owner)) {
					if (name1.equals(name)) {
						if (desc1.equals(desc)) {
							DynamicDesc dd = new DynamicDesc(desc1, false);
							String refac = String.format("%s.%s %s", f.owner().refactored(), f.refactored(),
									dd.getRefactoredDesc(cs));
							throw new IllegalStateException(String.format("Found %s.%s %s (%s) twice!", owner1, name1, desc1, refac));
						}
					}
				}
			}
		}
	}

	private static void verifyMethod(List<ClassHook> cs, MethodHook h) {
		String owner = h.owner().obfuscated();
		String name = h.obfuscated();
		String desc = h.val(Constants.DESC);

		for (ClassHook c : cs) {
			for (MethodHook m : c.methods()) {
				if (h.equals(m))
					continue;
				String owner1 = m.owner().obfuscated();
				String name1 = m.obfuscated();
				String desc1 = m.val(Constants.DESC);
				if (owner1.equals(owner)) {
					if (name1.equals(name)) {
						if (desc1.equals(desc)) {
							DynamicDesc dd = new DynamicDesc(desc1, true);
							String refac = String.format("%s.%s %s", m.owner().refactored(), m.refactored(),
									dd.getRefactoredDesc(cs));
							throw new IllegalStateException(String.format("Found %s.%s %s (%s) twice!", owner1, name1, desc1, refac));
						}
					}
				}
			}
		}
	}

	private static MethodHook foundMethod(boolean verify, String name, String desc, List<MethodHook> hooks, List<ClassHook> classes) {
		MethodHook h = null;
		try {
			for (MethodHook hook : hooks) {
				if (hook.refactored().equals(name)) {
					if (h != null) {
						if(hook != h) {
							throw new IllegalStateException(h + ", " + hook + " found twice.");
						}
					} else {
						hook.var("marked", "true");
						h = hook;
					}
				}
			}
		} catch(Exception e) {
			System.out.flush();
			System.err.flush();
			System.err.printf("Wtf at %s.%n", name);
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