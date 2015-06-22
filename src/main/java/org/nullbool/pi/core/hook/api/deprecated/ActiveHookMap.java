package org.nullbool.pi.core.hook.api.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.nullbool.pi.core.hook.api.ClassHook;
import org.nullbool.pi.core.hook.api.HookMap;

@Deprecated
public class ActiveHookMap extends HookMap {

	private List<AbstractActor> actors;
	
	public ActiveHookMap() {
		super();
		actors = new ArrayList<AbstractActor>();
	}
	
	public ActiveHookMap(int ver) {
		super(ver);
		actors = new ArrayList<AbstractActor>();
	}
	
	public ActiveHookMap(List<ClassHook> classes) {
		super(classes);
		actors = new ArrayList<AbstractActor>();
	}

	public ActiveHookMap(int ver, List<ClassHook> classes) {
		super(ver, classes);
		actors = new ArrayList<AbstractActor>();
	}

	public List<AbstractActor> getActors() {
		return actors;
	}
	
	public void addActor(AbstractActor actor) {
		actors.add(actor);
	}

	public void setActors(List<AbstractActor> actor) {
		this.actors = actor;
	}
}