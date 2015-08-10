/**
 * 
 */
package org.nullbool.api.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.nullbool.api.AbstractAnalysisProvider;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.ClassAnalyser;
import org.nullbool.api.obfuscation.number.MultiplierHandler;
import org.nullbool.api.obfuscation.refactor.MethodCache;
import org.nullbool.api.obfuscation.refactor.ReverseMethodDescCache;
import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.Constants;
import org.nullbool.pi.core.hook.api.DynamicDesc;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.nullbool.pi.core.hook.api.HookMap;
import org.nullbool.pi.core.hook.api.MethodHook;
import org.nullbool.pi.core.hook.api.ObfuscatedData;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bibl (don't ban me pls)
 * @created 22 Jun 2015 01:34:49
 */
public class NewOutputLogger {

	public static HookMap output() {
		// TODO: do
		AbstractAnalysisProvider provider = Context.current();
		Map<String, Boolean> flags = provider.getFlags();
		boolean printMultis = flags.getOrDefault("multis", true);
		boolean logResults = flags.getOrDefault("logresults", true);
		boolean headers = flags.getOrDefault("generateheaders", false);

		List<ClassAnalyser> analysers = provider.getAnalysers();

		int col_width = 40;

		List<String> errors = new ArrayList<String>();

		int total_class_count = 0;
		int total_correct_class_count = 0;
		int total_field_count = 0;
		int total_correct_field_count = 0;
		int total_method_count = 0;
		int total_correct_method_count = 0;

		List<ClassHook> classes = new ArrayList<ClassHook>();
		Map<String, String> mappedNames = new HashMap<String, String>();
		for(ClassAnalyser analyser : analysers) {
			ClassHook ch = analyser.getFoundHook();
			if(ch != null) {
				classes.add(ch);
				mappedNames.put(ch.obfuscated(), ch.refactored());
			}
		}

		if(printMultis) {
			resolveMultis(classes);
		}
		
		resolveDescs(classes);

		StringBuilder mainSb = new StringBuilder();

		for(ClassAnalyser analyser : analysers) {
			total_class_count++;

			ClassHook ch = analyser.getFoundHook();
			ClassNode cn = analyser.getFoundClass();

			if(ch == null) {
				mainSb.append(analyser.getName()).append(" couldn't be identified.");
				mainSb.append('\n').append('\n');
				continue;
			}

			total_correct_class_count++;

			StringBuilder classSb = new StringBuilder();
			classSb.append("> ").append(ch.refactored());

			String superName = cn.superName;
			if(mappedNames.containsKey(superName)) {
				superName = mappedNames.get(superName);
			}
			superName = superName.substring(superName.lastIndexOf('/') + 1);
			classSb.append(" (-> ").append(superName).append(")");

			classSb.append(" identified as ");
			classSb.append('\'').append(ch.obfuscated()).append('\'');

			// =========================FIELDS===========================
			StringBuilder fieldSb = new StringBuilder();

			int correct_field_counter = 0;
			List<FieldHook> visited_fields = new ArrayList<FieldHook>();
			String[] supportedFields = analyser.supportedFields();
			for(String sf : supportedFields) {
				String[] parts = sf.split("&");
				if(parts.length != 2) {
					fieldSb.append(" ^  Malformed supported field input: ").append(sf);
					errors.add(sf);
					continue;
				}

				List<FieldHook> matches = findMatches(ch.fields(), parts[0]);
				visited_fields.addAll(matches);
				if(matches.size() > 1) {
					for(FieldHook fh : matches) {
						fieldSb.append(" ^  Error, found ").append(fh.refactored()).append(" multiple times (").append(format(fh)).append(")");
						fieldSb.append('\n');
						errors.add(sf);
					}
				} else if(matches.size() == 0){
					fieldSb.append(" ^  Error, ").append(parts[0]).append(" (").append(parts[1]).append(") couldn't be identified.");
					fieldSb.append('\n');
					errors.add(sf);
				} else {
					correct_field_counter++;

					StringBuilder sb = new StringBuilder();
					FieldHook fh = matches.get(0);
					DynamicDesc desc = new DynamicDesc(fh.val(Constants.DESC), false);
					sb.append(" ^  ").append(fh.refactored());
					longstring(sb, 0, col_width); // 40
					sb.append(desc.getRefactoredDesc(classes)).append(" ");
					longstring(sb, 40, col_width); // 80
					sb.append("identified as ").append(format(fh));
					longstring(sb, 80, 25); // 105

					if (printMultis) {						
						if(hasMulti(desc.getObfuscated())) {
							long multi = Long.parseLong(fh.val(Constants.ENCODER, "1"));
							sb.append(" * ");
							if (multi >= 0)
								sb.append(" ");
							sb.append(multi);
						}
					}

					longstring(sb, 105, 25); // 130
					sb.append("(").append(desc.getObfuscated()).append(")");
					sb.append('\n');

					fieldSb.append(sb);
				}
			}

			for(FieldHook fh : ch.fields()) {
				if(!visited_fields.contains(fh)) {
					StringBuilder sb = new StringBuilder();
					sb.append(" ^  Error, extra hook found: ").append(format(fh)).append(" (").append(fh.val(Constants.DESC)).append(")");
					longstring(sb, 0, 80 - 1);
					sb.append(" (").append(fh.refactored()).append(")");
					sb.append('\n');

					fieldSb.append(sb);

					errors.add(fh.refactored() + "&" + fh.val(Constants.DESC));
				}

				for(FieldHook fh1 : ch.fields()) {
					if(fh1 != fh && fh.val(Constants.REAL_OWNER).equals(fh1.val(Constants.REAL_OWNER)) && fh.obfuscated().equals(fh1.obfuscated()) && !fh.refactored().equals(fh1.refactored())) {
						fieldSb.append(" ^  Error, field found twice as ").append(fh.refactored()).append(" and ").append(fh1.refactored());
						fieldSb.append('\n');
						
						errors.add(fh.refactored() + "&" + fh.val(Constants.DESC));
					}
				}
			}

			// =========================METHODS===========================
			StringBuilder methodSb = new StringBuilder();

			int correct_method_counter = 0;
			List<MethodHook> visited_methods = new ArrayList<MethodHook>();
			String[] supportedMethods = analyser.supportedMethods();
			for(String sm : supportedMethods) {
				String[] parts = sm.split("&");
				if(parts.length != 2) {
					methodSb.append(" ^  Malformed supported method input: ").append(sm);
					errors.add(sm);
					continue;
				}

				List<MethodHook> matches = findMatches(ch.methods(), parts[0]);
				visited_methods.addAll(matches);
				if(matches.size() > 1) {
					for(MethodHook mh : matches) {
						methodSb.append(" ^  Error, found ").append(mh.refactored()).append(" multiple times (").append(format(mh)).append(")");
						methodSb.append('\n');
						errors.add(sm);
					}
				} else if(matches.size() == 0){
					methodSb.append(" ^  Error, ").append(parts[0]).append(" (").append(parts[1]).append(") couldn't be identified.");
					methodSb.append('\n');
					errors.add(sm);
				} else {
					correct_method_counter++;

					StringBuilder sb = new StringBuilder();
					MethodHook mh = matches.get(0);
					DynamicDesc desc = new DynamicDesc(mh.val(Constants.REFACTORED_DESC), true);
					sb.append(" º  ").append(mh.refactored());
					longstring(sb, 0, col_width); // 40
					sb.append(desc.getRefactoredDesc(classes)).append(" ");
					longstring(sb, 40, col_width); // 80
					sb.append("identified as ").append(format(mh));
					longstring(sb, 80, 25); // 105
					longstring(sb, 105, 25); // 130

					sb.append(desc.getObfuscated());
					sb.append('\n');

					methodSb.append(sb);
				}
			}

			for(MethodHook mh : ch.methods()) {
				if(!visited_methods.contains(mh)) {
					StringBuilder sb = new StringBuilder();
					sb.append(" ^  Error, extra hook found: ").append(format(mh)).append(" ").append(mh.val(Constants.DESC)).append("");
					longstring(sb, 0, 80 - 1);
					sb.append(" (").append(mh.refactored()).append(")");
					sb.append('\n');

					methodSb.append(sb);
					errors.add(mh.refactored() + "&" + mh.val(Constants.REFACTORED_DESC));
				}
			}

			classSb.append(" (");
			classSb.append(correct_field_counter).append("/").append(supportedFields.length);
			total_field_count += supportedFields.length;
			total_correct_field_count += correct_field_counter;
			classSb.append(", ");
			classSb.append(correct_method_counter).append("/").append(supportedMethods.length);
			total_method_count += supportedMethods.length;
			total_correct_method_count += correct_method_counter;
			classSb.append(", ");
			classSb.append(ch.fields().size()).append("fhs");
			classSb.append(", ");
			classSb.append(ch.methods().size()).append("mhs");
			classSb.append(")");
			classSb.append('\n');

			if(headers) {
				mainSb.append("@SupportedHooks(");
				mainSb.append('\n').append('\t');
				mainSb.append("fields = {");
				ListIterator<FieldHook> fit = ch.fields().listIterator();
				while(fit.hasNext()) {
					FieldHook fh = fit.next();
					mainSb.append('"');
					mainSb.append(fh.refactored()).append("&");
					String desc = fh.val(Constants.DESC);
					String stripped = desc.replace("[", "");
					if(DynamicDesc.isPrimitive(stripped)) {
						mainSb.append(desc);
					} else {
						desc = DynamicDesc.convertSingleJavaStyle(classes, desc);
						mainSb.append(desc);
					}
					mainSb.append('"');

					if(fit.hasNext()) {
						mainSb.append(", ");
					}
				}

				mainSb.append("},");
				mainSb.append('\n');

				mainSb.append('\t');
				mainSb.append("methods = {");
				ListIterator<MethodHook> mit = ch.methods().listIterator();
				while(mit.hasNext()) {
					MethodHook mh = mit.next();
					mainSb.append('"');
					mainSb.append(mh.refactored()).append("&");
					String desc = mh.val(Constants.REFACTORED_DESC);
					// if(DynamicDesc.isPrimitive(stripped)) {
					// 	mainSb.append(desc);
					// } else {
					//	desc = DynamicDesc.convertSingleJavaStyle(classes, desc);
					//	mainSb.append(desc);
					//}
					mainSb.append(DynamicDesc.convertMultiJavaStyle(classes, desc));
					mainSb.append('"');

					if(mit.hasNext()) {
						mainSb.append(", ");
					}
				}
				mainSb.append("}");
				mainSb.append('\n');

				mainSb.append(")");
				mainSb.append('\n');
			}

			mainSb.append(classSb);
			mainSb.append(fieldSb);
			mainSb.append(methodSb);
			mainSb.append('\n');
		}

		if(logResults) {
			System.out.println(mainSb.toString());
		}

		System.out.printf("Found %d/%d classes.%n", total_correct_class_count, total_class_count);
		System.out.printf("Found %d/%d fields.%n", total_correct_field_count, total_field_count);
		System.out.printf("Found %d/%d methods.%n", total_correct_method_count, total_method_count);
		
		if(errors.size() > 0) {
			System.out.println("===ERRORS===");
			for(String s : errors) {
				System.out.println(" " + s);
			}
		}

		return new HookMap(classes);
	}

	private static int longstring(StringBuilder sb, int col_start, int col_end) {
		for (int i = sb.length(); i < (col_end + col_start); i++) {
			sb.append(' ');
		}
		return sb.length();
	}

	private static String format(MethodHook data) {
		return data.val(Constants.REAL_OWNER) + "." + data.obfuscated();
	}

	private static String format(FieldHook data) {
		return data.val(Constants.REAL_OWNER) + "." + data.obfuscated();
	}

	private static <T extends ObfuscatedData> List<T> findMatches(List<T> allHooks, String target) {
		List<T> matched = new ArrayList<T>();
		for(T t : allHooks) {
			if(t.refactored().equals(target)) {
				matched.add(t);
			}
		}
		return matched;
	}

	public static boolean hasMulti(String desc) {
		return desc.equals("I") || desc.equals("B") || desc.equals("S") || desc.equals("J");
	}
	
	public static void resolveDescs(List<ClassHook> classes) {
		ReverseMethodDescCache cache = Context.current().getMethodCache();
		Map<String, ClassNode> cns = Context.current().getClassNodes();
		MethodCache currentCache = new MethodCache(cns.values());
		for(ClassHook ch : classes) {
			for(MethodHook m : ch.methods()) {
				ClassNode cn = cns.get(m.val(Constants.REAL_OWNER));
				if(cn != null) {
					MethodNode mn = currentCache.get(cn.name, m.obfuscated(), m.val(Constants.DESC));
					if(mn == null) {
						System.out.println("NULL METHODNODE? " + m);
					} else {
						String newDesc = cache.get(mn);
						if(newDesc == null) {
							System.out.println("NULL NEW DESC? " + m);
						} else {
							m.var(Constants.DESC, newDesc);
						}
					}
				} else {
					System.out.println("NULL CLASSNODE? " + ch.obfuscated());
				}
			}
		}
	}

	public static void resolveMultis(List<ClassHook> classes) {
		MultiplierHandler mh = Context.current().getMultiplierHandler();
		for(ClassHook ch : classes) {
			for(FieldHook fh : ch.fields()) {
				String desc = fh.val(Constants.DESC);
				if(hasMulti(desc)) {
					String src = fh.val(Constants.REAL_OWNER) + "." + fh.obfuscated();

					long m = mh.getDecoder(src);
					if(m == 0) {
						// if we have to decoder, inverse the encoder
						m = mh.getEncoder(src);
						if(m != 0) {
							m = mh.inverse(m, desc.equals("J"));
						}
					}

					if(m == 0)
						m = 1;

					fh.var(Constants.ENCODER, Long.toString(m));
				}
			}
		}
	}
}