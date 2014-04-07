package server.logic.game;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.handlers.CombatCommandHandler;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public class TestExploration extends TestingUtils
{
	@Before
	public void setup() throws NoMoreTilesException
	{
		p1.addOwnedHex(currentState.getBoard().getHexByXY(1, 2).getHex());
		p2.addOwnedHex(currentState.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(currentState.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(currentState.getBoard().getHexByXY(5, 10).getHex());
		p1.addGold(50);

		addThingsToHexForPlayer(4,p1,3,6);
		currentState.setCurrentSetupPhase(SetupPhase.SETUP_FINISHED);
		currentState.setCurrentRegularPhase(RegularPhase.COMBAT);
		currentState.setActivePhasePlayer(p1.getID());
		currentState.setActiveTurnPlayer(p1.getID());
	}
	
	@Test
	public void testSimpleExploration()
	{
		CombatCommandHandler handler = getCombatCommandHandler();
		handler.resolveCombat(currentState.getBoard().getHexByXY(3, 6).getHex(), p1.getID());
		assertEquals(CombatPhase.DETERMINE_DEFENDERS,currentState.getCurrentCombatPhase());
		
		handler.rollDice(new Roll(1, currentState.getCombatHex().getHex(), RollReason.EXPLORE_HEX, p1.getID(), 6));
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 6).getHex()));
		assertEquals(CombatPhase.PLACE_THINGS,currentState.getCurrentCombatPhase());
	}
	
	@Test
	public void testAdvancedExploration()
	{
		CombatCommandHandler handler = getCombatCommandHandler();
		handler.resolveCombat(currentState.getBoard().getHexByXY(3, 6).getHex(), p1.getID());
		assertEquals(CombatPhase.DETERMINE_DEFENDERS,currentState.getCurrentCombatPhase());
		
		handler.rollDice(new Roll(1, currentState.getCombatHex().getHex(), RollReason.EXPLORE_HEX, p1.getID(), 5));
		
		assertEquals(CombatPhase.BRIBE_CREATURES,currentState.getCurrentCombatPhase());
		
		handler.bribeDefender(getBoardThingByName("Giant",3,6), p1.getID());
		assertEquals(46,p1.getGold());
		assertEquals(4,currentState.getCombatHex().getFightingThingsInHexNotOwnedByPlayers(currentState.getPlayers()).size());
		assertEquals(CombatPhase.BRIBE_CREATURES, currentState.getCurrentCombatPhase());
		handler.endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getBoardThingByName("Dwarves",3, 6), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Skeletons",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Watusi",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Goblins",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(3,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(5,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)), p1.getID(), 1);

		getCombatCommandHandler().applyHits(getBoardThingByName("Skeletons",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Watusi",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Goblins",3, 6), p4.getID(), 1);
		
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(1,p1.getOwnedThingsOnBoard().size());
	}

	@Test
	public void testAdvancedExploration2() throws NoMoreTilesException
	{
		CombatCommandHandler handler = getCombatCommandHandler();
		handler.resolveCombat(currentState.getBoard().getHexByXY(3, 6).getHex(), p1.getID());
		assertEquals(CombatPhase.DETERMINE_DEFENDERS,currentState.getCurrentCombatPhase());
		
		handler.rollDice(new Roll(1, currentState.getCombatHex().getHex(), RollReason.EXPLORE_HEX, p1.getID(), 5));

		currentState.getBoard().getHexByXY(3, 6).addThingToHexForExploration(getSpecialIncomeCounter());
		
		assertEquals(CombatPhase.BRIBE_CREATURES,currentState.getCurrentCombatPhase());
		
		handler.bribeDefender(getBoardThingByName("Giant",3,6), p1.getID());
		assertEquals(42,p1.getGold());
		assertEquals(4,currentState.getCombatHex().getFightingThingsInHexNotOwnedByPlayers(currentState.getPlayers()).size());
		assertEquals(CombatPhase.BRIBE_CREATURES, currentState.getCurrentCombatPhase());
		handler.endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getBoardThingByName("Dwarves",3, 6), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Skeletons",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Watusi",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Goblins",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(3,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(5,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)), p1.getID(), 1);

		getCombatCommandHandler().applyHits(getBoardThingByName("Skeletons",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Watusi",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Goblins",3, 6), p4.getID(), 1);
		
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(1,p1.getOwnedThingsOnBoard().size());
		assertEquals(1,p1.getTrayThings().size());
		assertEquals(true,p1.getTrayThings().iterator().next().isTreasure());
	}

	@Test
	public void testAdvancedExploration3() throws NoMoreTilesException
	{
		addThingsToHex(5,3,6);
		currentState.getBoard().getHexByXY(3, 6).addThingToHexForExploration(getSpecialIncomeCounter());
		
		CombatCommandHandler handler = getCombatCommandHandler();
		handler.resolveCombat(currentState.getBoard().getHexByXY(3, 6).getHex(), p1.getID());
		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getBoardThingByName("Dwarves",3, 6), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Giant",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 6));

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Skeletons",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Watusi",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getBoardThingByName("Goblins",3, 6),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 1));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(3,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(5,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)), p1.getID(), 1);

		getCombatCommandHandler().applyHits(getBoardThingByName("Skeletons",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Watusi",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Goblins",3, 6), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getBoardThingByName("Giant",3, 6), p4.getID(), 1);
		
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(1,p1.getOwnedThingsOnBoard().size());
		assertEquals(1,p1.getTrayThings().size());
		assertEquals(true,p1.getTrayThings().iterator().next().isTreasure());
	}
	
	private ITileProperties getBoardThingByName(String name, int x, int y)
	{
		for(ITileProperties thing : currentState.getBoard().getHexByXY(x, y).getThingsInHex())
		{
			if(thing.getName().equals(name))
			{
				return thing;
			}
		}
		
		throw new IllegalArgumentException("No board things named: " + name + " in hex at (" + x + "," + y + ")");
	}
	
	private void addThingsToHexForPlayer(int count, Player p, int x, int y) throws NoMoreTilesException
	{
		HexState hs = currentState.getBoard().getHexByXY(x, y);
		for(int i=0; i<count; i++)
		{
			ITileProperties thing = currentState.getCup().drawTile();
			p.addOwnedThingOnBoard(thing);
			hs.addThingToHex(thing);
		}
	}
	
	private void addThingsToHex(int count, int x, int y) throws NoMoreTilesException
	{
		HexState hs = currentState.getBoard().getHexByXY(x, y);
		for(int i=0; i<count; i++)
		{
			ITileProperties thing = currentState.getCup().drawTile();
			hs.addThingToHex(thing);
		}
	}
	
	private ITileProperties getSpecialIncomeCounter() throws NoMoreTilesException
	{
		ArrayList<ITileProperties> removedThings = new ArrayList<>();
		
		ITileProperties nextThing = currentState.getCup().drawTile();
		while(!nextThing.isTreasure() || nextThing.isSpecialIncomeCounter())
		{
			removedThings.add(nextThing);
			nextThing = currentState.getCup().drawTile();
		}
		
		for(ITileProperties thing : removedThings)
		{
			currentState.getCup().reInsertTile(thing);
		}
		
		return nextThing;
	}
}
