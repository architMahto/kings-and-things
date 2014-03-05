package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ExchangeThingsCommand extends AbstractInternalEvent{
	
	private final HashSet<ITileProperties> things;
	
	public ExchangeThingsCommand(Collection<ITileProperties> things, final Object OWNER){
		super( OWNER);
		this.things = new HashSet<ITileProperties>();
		for(ITileProperties thing : things){
			this.things.add(thing);
		}
	}
	
	public Set<ITileProperties> getThings(){
		return Collections.unmodifiableSet(things);
	}
}
