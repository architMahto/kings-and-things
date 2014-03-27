package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ExchangeSeaHexCommand extends AbstractInternalEvent{
	
	private final ITileProperties hex;
	
	public ExchangeSeaHexCommand(ITileProperties hex){
		super();
		this.hex = hex;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}