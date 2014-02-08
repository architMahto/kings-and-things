package server.event.commands;

import common.event.AbstractEvent;
import common.game.TileProperties;

public class ExchangeSeaHexCommand extends AbstractEvent{
	
	private final TileProperties hex;
	
	public ExchangeSeaHexCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}
