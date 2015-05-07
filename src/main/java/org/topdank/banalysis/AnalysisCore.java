package org.topdank.banalysis;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.topdank.banalysis.gson.FieldInsnNodeSerializer;
import org.topdank.banalysis.gson.FrameNodeSerializer;
import org.topdank.banalysis.gson.IincInsNodeSerializer;
import org.topdank.banalysis.gson.InsnNodeSerializer;
import org.topdank.banalysis.gson.IntInsnNodeSerializer;
import org.topdank.banalysis.gson.JumpInsnNodeSerializer;
import org.topdank.banalysis.gson.LabelNodeSerializer;
import org.topdank.banalysis.gson.LdcInsnNodeSerializer;
import org.topdank.banalysis.gson.LineNumberNodeSerializer;
import org.topdank.banalysis.gson.LookupSwitchInsnNodeSerializer;
import org.topdank.banalysis.gson.MethodInsnNodeSerializer;
import org.topdank.banalysis.gson.MultiANewArrayInsnNodeSerializer;
import org.topdank.banalysis.gson.TableSwitchInsnNodeSerializer;
import org.topdank.banalysis.gson.TypeInsnNodeSerializer;
import org.topdank.banalysis.gson.VarInsnNodeSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class AnalysisCore {
	
	public static Gson GSON_INSTANCE = getBuilder().create();
	
	public static GsonBuilder getBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(FieldInsnNode.class, new FieldInsnNodeSerializer());
		builder.registerTypeAdapter(FrameNode.class, new FrameNodeSerializer());
		builder.registerTypeAdapter(IincInsnNode.class, new IincInsNodeSerializer());
		builder.registerTypeAdapter(InsnNode.class, new InsnNodeSerializer());
		builder.registerTypeAdapter(IntInsnNode.class, new IntInsnNodeSerializer());
		builder.registerTypeAdapter(JumpInsnNode.class, new JumpInsnNodeSerializer());
		builder.registerTypeAdapter(LabelNode.class, new LabelNodeSerializer());
		builder.registerTypeAdapter(LdcInsnNode.class, new LdcInsnNodeSerializer());
		builder.registerTypeAdapter(LineNumberNode.class, new LineNumberNodeSerializer());
		builder.registerTypeAdapter(LookupSwitchInsnNode.class, new LookupSwitchInsnNodeSerializer());
		builder.registerTypeAdapter(MethodInsnNode.class, new MethodInsnNodeSerializer());
		builder.registerTypeAdapter(MultiANewArrayInsnNode.class, new MultiANewArrayInsnNodeSerializer());
		builder.registerTypeAdapter(TableSwitchInsnNode.class, new TableSwitchInsnNodeSerializer());
		builder.registerTypeAdapter(TypeInsnNode.class, new TypeInsnNodeSerializer());
		builder.registerTypeAdapter(VarInsnNode.class, new VarInsnNodeSerializer());
		builder.setPrettyPrinting();
		return builder;
	}
}