package org.zbot.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.zbot.hooks.serialisation.IMapDeserialiser;
import org.zbot.hooks.serialisation.IMapSerialiser;
import org.zbot.hooks.serialisation.impl.MapDeserialiserImpl;
import org.zbot.hooks.serialisation.impl.MapSerialiserImpl;

public class HookMap implements Serializable, IMapSerialiser, IMapDeserialiser {

	private static final long serialVersionUID = -2199521127019909810L;

	private static final int CURRENT_VERSION = 2;
	
	private final int version;
	private List<ClassHook> classes;

	public HookMap() {
		this(CURRENT_VERSION);
	}
	
	public HookMap(int ver) {
		this.version = ver;
		classes = new ArrayList<ClassHook>();
	}
	
	public HookMap(List<ClassHook> classes) {
		this(CURRENT_VERSION, classes);
	}

	public HookMap(int ver, List<ClassHook> classes) {
		this.version = ver;
		this.classes = classes;
	}

	public List<ClassHook> getClasses() {
		return classes;
	}

	public void setClasses(List<ClassHook> classes) {
		this.classes = classes;
	}
	
	public int getVersion(){
		return version;
	}

	/* (non-Javadoc)
	 * @see org.nullbool.zbot.pi.core.hook.serialisation.IMapDeserialiser#deserialise(java.io.InputStream)
	 */
	@Override
	public HookMap deserialise(InputStream is) throws IOException {
		return new MapDeserialiserImpl().deserialise(is);
	}

	/* (non-Javadoc)
	 * @see org.nullbool.zbot.pi.core.hook.serialisation.IMapSerialiser#serialise(org.nullbool.zbot.pi.core.hook.HookMap, java.io.OutputStream)
	 */
	@Override
	public void serialise(HookMap map, OutputStream os) throws IOException {
		new MapSerialiserImpl().serialise(map, os);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ClassHook c : classes) {
			sb.append(c).append("\n");
			for (FieldHook h : c.getFields()) {
				sb.append("\t").append(h).append("\n");
			}
			for (MethodHook h : c.getMethods()) {
				sb.append("\t").append(h).append("\n");
			}
		}
		return sb.toString();
	}
}