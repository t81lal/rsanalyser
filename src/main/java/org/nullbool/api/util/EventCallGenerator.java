package org.nullbool.api.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.zbot.topdank.eventbus.BusRegistry;
import org.zbot.topdank.eventbus.Event;

public class EventCallGenerator implements Opcodes {

	public static InsnList generate(String busName, AbstractInsnNode... loadInsns) {
		// INVOKESTATIC org/zbot/topdank/eventbus/BusRegistry.getInstance ()Lorg/zbot/topdank/eventbus/BusRegistry;
		// ALOAD 0
		// INVOKEVIRTUAL org/zbot/topdank/eventbus/BusRegistry.get (Ljava/lang/String;)Lorg/zbot/topdank/eventbus/EventBus;
		// ALOAD 1
		// INVOKEINTERFACE org/zbot/topdank/eventbus/EventBus.dispatch (Lorg/zbot/topdank/eventbus/Event;)V
		InsnList list = new InsnList();
		list.add(new MethodInsnNode(INVOKESTATIC, "org/zbot/topdank/eventbus/BusRegistry", "getInstance", "()Lorg/zbot/topdank/eventbus/BusRegistry;", false));
		list.add(new LdcInsnNode(busName));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "org/zbot/topdank/eventbus/BusRegistry", "get", "(Ljava/lang/String;)Lorg/zbot/topdank/eventbus/EventBus;",
				false));
		for (AbstractInsnNode ain : loadInsns) {
			list.add(ain);
		}
		list.add(new MethodInsnNode(INVOKESPECIAL, "org/zbot/topdank/eventbus/EventBus", "dispatch", "(Lorg/zbot/topdank/eventbus/Event;)V", true));
		return list;
	}

	public static InsnList generate(AbstractInsnNode... loadInsns) {
		// INVOKESTATIC org/zbot/topdank/eventbus/BusRegistry.getInstance ()Lorg/zbot/topdank/eventbus/BusRegistry;
		// INVOKEVIRTUAL org/zbot/topdank/eventbus/BusRegistry.getGlobalBus ()Lorg/zbot/topdank/eventbus/EventBus;
		// ALOAD 0
		// INVOKEINTERFACE org/zbot/topdank/eventbus/EventBus.dispatch (Lorg/zbot/topdank/eventbus/Event;)V

		InsnList list = new InsnList();
		list.add(new MethodInsnNode(INVOKESTATIC, "org/zbot/topdank/eventbus/BusRegistry", "getInstance", "()Lorg/zbot/topdank/eventbus/BusRegistry;", false));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "org/zbot/topdank/eventbus/BusRegistry", "getGlobalBus", "()Lorg/zbot/topdank/eventbus/EventBus;", false));
		for (AbstractInsnNode ain : loadInsns) {
			list.add(ain);
		}
		list.add(new MethodInsnNode(INVOKESPECIAL, "org/zbot/topdank/eventbus/EventBus", "dispatch", "(Lorg/zbot/topdank/eventbus/Event;)V", true));
		return list;
	}

	public static InsnList generateDispatch(InsnList eventCreateList) {
		// INVOKESTATIC org/zbot/topdank/eventbus/BusRegistry.getInstance ()Lorg/zbot/topdank/eventbus/BusRegistry;
		// INVOKEVIRTUAL org/zbot/topdank/eventbus/BusRegistry.getGlobalBus ()Lorg/zbot/topdank/eventbus/EventBus;
		// ICONST_1
		// ANEWARRAY org/zbot/topdank/eventbus/Event
		// DUP
		// ICONST_0
		// ALOAD 2 <- event
		// AASTORE
		// INVOKEINTERFACE org/zbot/topdank/eventbus/EventBus.dispatch ([Lorg/zbot/topdank/eventbus/Event;)V
		InsnList list = new InsnList();
		list.add(new MethodInsnNode(INVOKESTATIC, "org/zbot/topdank/eventbus/BusRegistry", "getInstance", "()Lorg/zbot/topdank/eventbus/BusRegistry;", false));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "org/zbot/topdank/eventbus/BusRegistry", "getGlobalBus", "()Lorg/zbot/topdank/eventbus/EventBus;", false));
		list.add(new InsnNode(ICONST_1));
		list.add(new TypeInsnNode(ANEWARRAY, "org/zbot/topdank/eventbus/Event"));
		list.add(new InsnNode(DUP));
		list.add(new InsnNode(ICONST_0));
		list.add(eventCreateList);
		list.add(new InsnNode(AASTORE));
		list.add(new MethodInsnNode(INVOKEINTERFACE, "org/zbot/topdank/eventbus/EventBus", "dispatch", "([Lorg/zbot/topdank/eventbus/Event;)V", true));
		return list;
	}

	/**
	 * Generates an instruction list which creates an object and leaves it on the top of the stack.
	 * 
	 * @param eventClass
	 * @param constructorDesc
	 * @param varsInsns
	 * @return
	 */
	public static InsnList generateEventCreate(String eventClass, String constructorDesc, AbstractInsnNode... varsInsns) {
		// NEW org/nullbool/api/util/EventCallGenerator$TestEvent
		// DUP
		// ALOAD 0
		// INVOKESPECIAL org/nullbool/api/util/EventCallGenerator$TestEvent.<init> (Ljava/lang/Object;)V
		// ASTORE 1
		InsnList list = new InsnList();
		list.add(new TypeInsnNode(NEW, eventClass));
		list.add(new InsnNode(DUP));
		for (AbstractInsnNode ain : varsInsns) {
			list.add(ain);
		}
		list.add(new MethodInsnNode(INVOKESPECIAL, eventClass, "<init>", constructorDesc, false));
		return list;
	}

	public static void test2() {
		Object o = new Object();
		Event e = new TestEvent(o);
		BusRegistry.getInstance().getGlobalBus().dispatch(e);
	}

	private static class TestEvent implements Event {

		public TestEvent(Object o) {
		}
	}

	public static void test() {
		Event e = null;
		BusRegistry.getInstance().getGlobalBus().dispatch(e);
	}
}