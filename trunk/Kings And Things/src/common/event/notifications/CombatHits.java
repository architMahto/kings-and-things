package common.event.notifications;

import common.event.AbstractNetwrokEvent;

public class CombatHits extends AbstractNetwrokEvent {
	
	
	private static final long serialVersionUID = 8022414721166100889L;
	private final int playerApplyingHit;
	private final int playerReceivingHit;
	private int numberOfHits;
	
	// constructor
	public CombatHits(final int playerApplyingHit, final int playerReceivingHit, int numberOfHits) {
		this.playerApplyingHit = playerApplyingHit;
		this.playerReceivingHit = playerReceivingHit;
		this.numberOfHits = numberOfHits;
	}
	
	/*Getter Methods*/
	
	/*
	 * getPlayerApplyingHitID retrieves the ID of the player delivering hits
	 */
	public final int getPlayerApplyingHitID() {
		return playerApplyingHit;
	}
	
	/*
	 * getPlayerReceivingHitID retrieves the ID of the player receiving hits
	 */
	public final int getPlayerReceivingHitID() {
		return playerReceivingHit;
	}
	
	/*
	 * getNumberOfHits() retrieves the number of hits delivered
	 */
	public int getNumberOfHits() {
		return numberOfHits;
	}

}
