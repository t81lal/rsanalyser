package org.nullbool.api.obfuscation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.nullbool.api.Context;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class CatchBlockFixer implements Opcodes {
 
    private static int catchblockkills = 0;
 
    public static void rek(Collection<ClassNode> classes) {
        for (ClassNode cn : classes) {
            for (MethodNode mn : cn.methods) {
                List<TryCatchBlockNode> remove = mn.tryCatchBlocks.stream().filter(tcb ->
                        tcb.type != null && tcb.type.contains("Runtime")).collect(Collectors.toList());
                List<TryCatchBlockNode> skip = new LinkedList<>();
                for (TryCatchBlockNode tcb : remove) {
                    if (skip.contains(tcb)) {
                        mn.tryCatchBlocks.remove(tcb);
                        catchblockkills++;
                        continue;
                    }
                    skip.addAll(remove.stream().filter(check ->
                            check != tcb && check.handler == tcb.handler).collect(Collectors.toList()));
                    AbstractInsnNode cur = tcb.handler.getNext();
                    while (!isCodeKill(cur.getOpcode()))
                        cur = cur.getNext();
                    if (cur.getOpcode() == ATHROW) {
                        cur = tcb.handler.getNext();
                        while (!isCodeKill(cur.getOpcode())) {
                            AbstractInsnNode temp = cur;
                            cur = cur.getNext();
                            mn.instructions.remove(temp);
                        }
                        mn.instructions.remove(cur);
                        mn.tryCatchBlocks.remove(tcb);
                        catchblockkills++;
                    }
                }
            }
        }
        
        if(Context.current().getFlags().getOrDefault("basicout", true)) {
            System.err.println("Removed " + catchblockkills + " redundant exceptions.");
        }
    }
 
    private static boolean isCodeKill(int opcode) {
        return (opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW;
    }
}
