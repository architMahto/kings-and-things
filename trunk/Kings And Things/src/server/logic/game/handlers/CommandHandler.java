package server.logic.game.handlers;

import static common.Constants.ALL_PLAYERS_ID;

import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import server.event.DiceRolled;
import server.event.GameStarted;
import server.event.PlayerWaivedRetreat;
import server.event.PlayerRemovedThingsFromHex;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.GameState;
import server.logic.game.RollModification;
import server.logic.game.validators.CommandValidator;
import server.event.internal.RollDiceCommand;
import server.event.internal.DoneRollingCommand;
import server.event.internal.EndPlayerTurnCommand;
import server.event.internal.RemoveThingsFromHexCommand;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.game.Roll;
import common.game.Player;
import common.game.HexState;
import common.game.ITileProperties;
import common.Constants;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.event.EventDispatch;
import common.event.network.CurrentPhase;
import common.event.network.DieRoll;
import common.event.network.PlayerState;
import common.event.network.RackPlacement;
import common.event.network.CommandRejected;
import common.event.network.HexOwnershipChanged;

public abstract class CommandHandler
{
	private static final Random rand = new Random(); 
	
	//sub classes can not and should not change these fields,
	//they are to be set only after handling a start game command
	private GameState currentState;
	private boolean isDemoMode;
	
	/**
	 * call this method to initialize this class before sending it commands
	 */
	public void initialize()
	{
		EventDispatch.registerOnInternalEvents(this);
	}
	
	/**
	 * call this method when you are done with the instance
	 */
	public void dispose()
	{
		EventDispatch.unregisterFromInternalEvents(this);
	}

	/**
	 * Call this to end the current players turn (progresses to the next phase)
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 */
	public void endPlayerTurn(int playerNumber){
		CommandValidator.validateCanEndPlayerTurn(playerNumber, currentState);
		if(currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_TWO_RETREAT || currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_ONE_RETREAT || 
				currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_THREE_RETREAT || currentState.getCurrentCombatPhase() == CombatPhase.DEFENDER_RETREAT)
		{
			new PlayerWaivedRetreat().postInternalEvent(playerNumber);
		}
		else
		{
			advanceActivePhasePlayer();
		}
	}

	/**
	 * Call this to roll dice for a player
	 * @param reasonForRoll The reason for this dice roll
	 * @param playerNumber The player who sent the command
	 * @param tile The target of the role, (could be hex, creature, building etc)
	 * @param rollValue The desired outcome of the roll, this value is ignored unless
	 * we are running in demo mode
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public void rollDice(RollReason reasonForRoll, int playerNumber, ITileProperties tile, int rollValue)
	{
		CommandValidator.validateCanRollDice(reasonForRoll, playerNumber, tile, currentState);
		makeDiceRoll(reasonForRoll, playerNumber, tile, rollValue);
	}
	
	public void removeThingsFromBoard(int playerNumber, ITileProperties hex, Set<ITileProperties> thingsToRemove)
	{
		CommandValidator.validateCanRemoveThingsFromHex(playerNumber, hex, thingsToRemove, getCurrentState());
		if(thingsToRemove.size() == 1 && thingsToRemove.iterator().next().isSpecialIncomeCounter())
		{
			//just remove it ourselves
			ITileProperties counter = thingsToRemove.iterator().next();
			currentState.getBoard().getHexStateForHex(hex).removeSpecialIncomeCounterFromHex();
			currentState.getPlayerByPlayerNumber(playerNumber).removeOwnedThingOnBoard(counter);
			currentState.getCup().reInsertTile(counter);
		}
		else
		{
			new PlayerRemovedThingsFromHex(hex, thingsToRemove).postInternalEvent(playerNumber);
		}
	}

	protected final GameState getCurrentState()
	{
		return currentState;
	}

	protected final boolean isDemoMode()
	{
		return isDemoMode;
	}

	protected void makeHexOwnedByPlayer(ITileProperties hex, int playerNumber)
	{
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				p.removeHexFromOwnership(hex);
				break;
			}
		}
		currentState.getPlayerByPlayerNumber(playerNumber).addOwnedHex(hex);

		HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
		hs.setMarker( Constants.getPlayerMarker( playerNumber));
		new HexOwnershipChanged(hs).postNetworkEvent( ALL_PLAYERS_ID);
	}
	
	protected void removePlayerThingFromBoard(int playerNumber, ITileProperties hex, ITileProperties thing)
	{
		thing.resetValue();
		currentState.getBoard().getHexStateForHex(hex).removeThingFromHex(thing);
		currentState.getPlayerByPlayerNumber(playerNumber).removeOwnedThingOnBoard(thing);
		if(thing.isCreature() || thing.isSpecialIncomeCounter())
		{
			currentState.getCup().reInsertTile(thing);
		}
		else if(thing.isSpecialCharacter())
		{
			//TODO let player decide to flip
			thing.flip();
			currentState.getBankHeroes().reInsertTile(thing);
		}
	}

	protected void advanceActivePhasePlayer(){
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
		int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
		
		int indexOfActiveTurnPlayer = currentState.getPlayerOrder().indexOf(currentState.getActiveTurnPlayer().getID());
		if(indexOfActiveTurnPlayer == ((activePhasePlayerOrderIndex + 1) % currentState.getPlayers().size()))
		{
			if(nextSetupPhase != SetupPhase.SETUP_FINISHED)
			{
				nextSetupPhase = getNextSetupPhase();
			}
			else
			{
				nextRegularPhase = getNextRegularPhase();
				regularPhaseChanged(nextRegularPhase);
			}
		}
		else
		{
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
		}
		currentState.setCurrentSetupPhase(nextSetupPhase);
		currentState.setCurrentRegularPhase(nextRegularPhase);
		currentState.setCurrentCombatPhase(CombatPhase.NO_COMBAT);
		currentState.setCombatLocation(null);
		currentState.recordRollForSpecialCharacter(null);
		currentState.setDefendingPlayerNumber(-1);
		currentState.clearAllPlayerTargets();
		
		if( nextSetupPhase != SetupPhase.SETUP_FINISHED){
			new CurrentPhase<SetupPhase>( currentState.getPlayerInfoArray(), nextSetupPhase).postNetworkEvent( ALL_PLAYERS_ID);
		}else{
			new CurrentPhase<RegularPhase>( currentState.getPlayerInfoArray(), nextRegularPhase).postNetworkEvent( ALL_PLAYERS_ID);
		}
	}
	
	protected void advanceActiveTurnPlayer(){
		int activeTurnPlayerNumber = currentState.getActiveTurnPlayer().getID();
		int activeTurnPlayerOrderIndex = currentState.getPlayerOrder().indexOf(activeTurnPlayerNumber);
		int nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(++activeTurnPlayerOrderIndex % currentState.getPlayers().size());
		
		//in a 2 player game turn order doesn't swap
		if(currentState.getPlayers().size() == 2)
		{
			nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(0);
		}

		currentState.setActivePhasePlayer(nextActiveTurnPlayerNumber);
		currentState.setActiveTurnPlayer(nextActiveTurnPlayerNumber);
		currentState.removeAllHexesWithBuiltInObjects();
	}
	
	private SetupPhase getNextSetupPhase(){
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		
		if(nextSetupPhase == SetupPhase.SETUP_FINISHED)
		{
			return SetupPhase.SETUP_FINISHED;
		}
		else
		{
			int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
			int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
			
			int currentSetupPhaseIndex = nextSetupPhase.ordinal();
			for(SetupPhase sp : SetupPhase.values())
			{
				if(sp.ordinal() == (currentSetupPhaseIndex + 1))
				{
					setupPhaseChanged(sp);
					return sp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid SetupPhase constant");
	}

	private RegularPhase getNextRegularPhase(){
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		if(nextRegularPhase == RegularPhase.SPECIAL_POWERS)
		{
			advanceActiveTurnPlayer();
			return RegularPhase.RECRUITING_CHARACTERS;
		}
		else
		{
			int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
			int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
			
			if(currentState.getBoard().getContestedHexes(currentState.getPlayers()).size() > 0)
			{
				return RegularPhase.COMBAT;
			}
			int currentRegularPhaseIndex = nextRegularPhase.ordinal();
			for(RegularPhase rp : RegularPhase.values())
			{
				if(rp.ordinal() == (currentRegularPhaseIndex + 1))
				{
					return rp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid RegularPhase constant");
	}

	private void setupPhaseChanged(SetupPhase setupPhase)
	{
		switch(setupPhase)
		{
			case EXCHANGE_SEA_HEXES:
			{
				//we need to flip all board hexes face up
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					hs.getHex().flip();
				}
				break;
			}
			case PLACE_FREE_TOWER:
			{
				//give players 10 gold each
				for(Player p : currentState.getPlayers())
				{
					p.addGold(10);
				}
				break;
			}
			case PLACE_FREE_THINGS:
			{
				//give all players 10 free things from cup, things are drawn randomly so player order
				//doesn't matter, unless we are in demo mode, in which case we must do it in player
				//order, so let's just do it in player order all the time
				ArrayList<Player> players = new ArrayList<Player>();
				ArrayList<Integer> playerOrder = new ArrayList<Integer>(currentState.getPlayerOrder());
				for(Integer i : playerOrder)
				{
					players.add(currentState.getPlayerByPlayerNumber(i));
				}

				for(Player p : players)
				{
					RackPlacement tray = new RackPlacement(10);
					for(int i=0; i<10; i++)
					{
						try
						{
							ITileProperties thing = currentState.getCup().drawTile();
							p.addThingToTrayOrHand(thing);
							tray.getArray()[i] = thing;
						}
						catch (NoMoreTilesException e)
						{
							// should never happen
							Logger.getErrorLogger().error("Unable to draw 10 free things for: " + currentState.getActivePhasePlayer() + ", due to: ", e);
						}
					}
					
					tray.postNetworkEvent(p.getID());
					new PlayerState(p.getPlayerInfo()).postNetworkEvent(p.getID());
				}
				//TODO send full player list?
				break;
			}
			case SETUP_FINISHED:
			{
				currentState.getBoardGenerator().setupFinished();
				regularPhaseChanged(currentState.getCurrentRegularPhase());
			}
			default:
				break;
		}
	}

	private void regularPhaseChanged(RegularPhase regularPhase)
	{
		switch(regularPhase)
		{
			case RECRUITING_CHARACTERS:
			{
				//do income phase automagically
				makeGoldCollected();
				break;
			}
			case COMBAT:
			{
				//replenish move points of all creatures in preparation for next round
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					for(ITileProperties tp : hs.getCreaturesInHex())
					{
						tp.setMoveSpeed(4);
					}
				}
			}
			default:
				break;
		}
	}

	private void makeGoldCollected()
	{
		for(Player p : currentState.getPlayers())
		{
			p.addGold(p.getIncome());
		}
	}

	private void makeDiceRoll(RollReason reasonForRoll, int playerNumber, ITileProperties tile, int rollValue)
	{
		if(reasonForRoll == RollReason.ENTERTAINMENT)
		{
			currentState.addNeededRoll(new Roll(1, null, RollReason.ENTERTAINMENT, playerNumber));
		}
		
		Roll rollToAddTo = null;
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tile) && r.needsRoll())
			{
				rollToAddTo = r;
				break;
			}
		}
		if(rollToAddTo == null && reasonForRoll == RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			rollToAddTo = new Roll(2, tile, RollReason.RECRUIT_SPECIAL_CHARACTER, playerNumber);
			currentState.addNeededRoll(rollToAddTo);
		}

		rollToAddTo.addBaseRoll(rollDie(rollValue));
		if(currentState.hasRollModificationFor(rollToAddTo))
		{
			List<RollModification> modifications = currentState.getRollModificationsFor(rollToAddTo);
			for(RollModification rm : modifications)
			{
				rollToAddTo.addRollModificationFor(rm.getRollIndexToModify(), rm.getAmountToAdd());
				currentState.removeRollModification(rm);
			}
		}
		//notifies players of die roll
		new DieRoll(rollToAddTo).postNetworkEvent( playerNumber);
	}

	private int rollDie(int rollValue)
	{
		//generate a number between 10 to 69 and divide by 10
		//previous method produced way to many ties
		return isDemoMode? rollValue : (rand.nextInt( 60)+10)/10;
	}
	
	@Subscribe
	public void receiveDoneRolling( DoneRollingCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				currentState.addDoneRolling( command.getID());
				if( currentState.allRolled() && !currentState.isWaitingForRolls()){
					if( currentState.getCurrentSetupPhase()!=SetupPhase.SETUP_FINISHED){
						new DiceRolled().postInternalEvent();
					}else{
						if( currentState.getCurrentRegularPhase()==RegularPhase.COMBAT){
							new CurrentPhase<CombatPhase>( currentState.getPlayerInfoArray(), currentState.getCurrentCombatPhase()).postNetworkEvent( ALL_PLAYERS_ID);
						}else{
							new CurrentPhase<RegularPhase>( currentState.getPlayerInfoArray(), currentState.getCurrentRegularPhase()).postNetworkEvent( ALL_PLAYERS_ID);
						}
					}
				}
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process DoneRollingCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage()).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}

	@Subscribe
	public void receiveGameStartedEvent(GameStarted event)
	{
		currentState = event.getCurrentState();
		isDemoMode = event.isDemoMode();
	}

	@Subscribe
	public void receiveRemoveThingFromBoardCommand(RemoveThingsFromHexCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				removeThingsFromBoard(command.getID(),command.getHexToRemoveSomethingFrom(),command.getThingsToRemove());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RemoveThingFromHexCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage()).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
	
	@Subscribe
	public void recieveEndPlayerTurnCommand(EndPlayerTurnCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				endPlayerTurn(command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process EndPlayerTurnCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage()).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}

	@Subscribe
	public void receiveRollDiceCommand(RollDiceCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				rollDice(command.getReasonForRoll(), command.getID(), command.getTileToRollFor(), command.getRollValue());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RollDieCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage()).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
}
