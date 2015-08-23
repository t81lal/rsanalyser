/** pi-rs, a generic framework for loading Java Applets in a contained environment.
 * Copyright (C) 2015  NullBool
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nullbool.impl.r90;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nullbool.api.Builder;
import org.nullbool.api.Context;
import org.nullbool.api.analysis.AnalysisException;
import org.nullbool.api.analysis.IFieldAnalyser;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.impl.analysers.entity.ActorAnalyser;
import org.nullbool.pi.core.hook.api.FieldHook;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Aug 2015 12:20:04
 * 
 * move method was removed
 * queueposition method was removed
 * (thats how we used to hook the queue stuff)
 */
public class ActorAnalyser90 extends ActorAnalyser {

	public ActorAnalyser90() throws AnalysisException {
		super();
	}
	
	@Override
	public String[] supportedMethods() {
		return new Builder<String>(super.supportedMethods()).remove(new Filter<String>() {
			@Override
			public boolean accept(String t) {
				return t.contains("queuePosition") || t.contains("move");
			}
		}).asArray(new String[0]);
	}
	
	@Override
	public String[] supportedFields() {
		return new Builder<String>(super.supportedFields()).replace(new Filter<String>(){
			@Override
			public boolean accept(String t) {
				return t.equals("queueRun&[Z");
			}
		}, "queueRun&[B").asArray(new String[0]);
	}
	
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return super.registerMethodAnalysers().remove(new Filter<IMethodAnalyser>() {
			@Override
			public boolean accept(IMethodAnalyser t) {
				return t instanceof QueuePositionMethodAnalyser || t instanceof MoveMethodAnalyser;
			}
		});
	}
	
	@Override
	protected Builder<IFieldAnalyser> registerFieldAnalysers() {
		return super.registerFieldAnalysers().replace(new Filter<IFieldAnalyser>(){
			@Override
			public boolean accept(IFieldAnalyser t) {
				return t instanceof QueueFieldsAnalyser;
			}
		}, new QueueFieldsAnalyser90());
	}
	
	public class QueueFieldsAnalyser90 extends QueueFieldsAnalyser {
		@Override
		public List<FieldHook> findFields(ClassNode actor) {
			List<FieldHook> list = new ArrayList<FieldHook>();
			
			TreeBuilder tb = new TreeBuilder();
			
			String desc = "(L" + actor.name + ";)V";
			for(ClassNode cn : Context.current().getClassNodes().values()) {
				for(MethodNode m : cn.methods) {
					if(Modifier.isStatic(m.access)) {
						if(m.desc.equals(desc)) {
							Map<Integer, FieldMemberNode> fmns = new HashMap<Integer, FieldMemberNode>();
							
							NodeVisitor nv = new NodeVisitor() {
								@Override
								public void visitVariable(VariableNode vn) {
									ArithmeticNode an = null;
									if(vn.opcode() == ISTORE && (an = vn.firstOperation()) != null && an.opcode() == IADD) {
										List<ArithmeticNode> muls = an.t_findChildren(IMUL);
										if(muls.size() == 2) {
											for(ArithmeticNode mul : muls) {
												AbstractNode iaload = mul.t_first(IALOAD);
												NumberNode nn = mul.firstNumber();
												if(iaload != null && nn != null) {
													if(nn.number() == 128) {
														fmns.put(vn.var(), iaload.firstField());
													}
												}
											}
										}
									}
								}
							};
							tb.build(m).accept(nv);
							
							if(fmns.size() == 2) {
								List<Integer> keys = new ArrayList<Integer>(fmns.keySet());
								Collections.sort(keys);
								FieldMemberNode x = fmns.get(keys.get(0));
								FieldMemberNode y = fmns.get(keys.get(1));
								list.add(asFieldHook(x.fin(), "queueX"));
								list.add(asFieldHook(y.fin(), "queueY"));
								
								AbstractNode parent = x.parent();
								ArithmeticNode sub = parent.t_first(ISUB);
								FieldMemberNode length = sub.<FieldMemberNode>t_deepFindChildren(GETFIELD).get(0);
								list.add(asFieldHook(length.fin(), "queueLength"));
							}
						}
					}
				}
			}
			
			return list;
		}
	}
}