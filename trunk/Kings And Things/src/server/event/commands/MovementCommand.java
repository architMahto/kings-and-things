package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import java.util.Set;

import common.TileProperties;
import common.event.AbstractEvent;

public class MovementCommand extends AbstractEvent {
	
	// List of hexes that creatures want to move through
	private final HashSet<TileProperties> Hexes;
	// List of creatures that want to move through the hexes
	private final HashSet<TileProperties> Creatures;
	
	/*
	 * Constructor
	 */
	public MovementCommand(Collection<TileProperties> newHexes, Collection<TileProperties> newCreatures) {
		// initializes Hexes
		this.Hexes = new HashSet<TileProperties>();
		for (TileProperties tp : newHexes) {
			this.Hexes.add(tp);
		}
		
		// initializes Creatures
		this.Creatures = new HashSet<TileProperties>();
		for (TileProperties tp : newCreatures) {
			this.Creatures.add(tp);
		}
	}
	
	// Getter methods
	
	/*
	 * retrieves Hexes
	 */
	public Set<TileProperties> getHexes () {
		return Collections.unmodifiableSet(Hexes);
	}
	
	/*
	 * retrieves Creatures
	 */
	public Set<TileProperties> getCreatures () {
		return Collections.unmodifiableSet(Creatures);
	}
}
