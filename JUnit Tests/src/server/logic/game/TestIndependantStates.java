package server.logic.game;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants.BuildableBuilding;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public class TestIndependantStates extends TestingUtils
{
	@Before
	public void setup() throws NoMoreTilesException
	{
		p1.addOwnedHex(currentState.getBoard().getHexByXY(1, 2).getHex());
		p2.addOwnedHex(currentState.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(currentState.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(currentState.getBoard().getHexByXY(5, 10).getHex());

		p1.addOwnedHex(currentState.getBoard().getHexByXY(3, 6).getHex());
		p1.addOwnedHex(currentState.getBoard().getHexByXY(2, 5).getHex());
		p4.addOwnedHex(currentState.getBoard().getHexByXY(2, 7).getHex());
		addTenThingsToHexForPlayer(p1);
		addTenThingsToHexForPlayer(p2);
		addTenThingsToHexForPlayer(p3);
		addTenThingsToHexForPlayer(p4);
		currentState.setCurrentSetupPhase(SetupPhase.SETUP_FINISHED);
		currentState.setCurrentRegularPhase(RegularPhase.COMBAT);
		currentState.setActivePhasePlayer(p1.getID());
		currentState.setActiveTurnPlayer(p1.getID());
	}
	
	@Test
	public void testRemoveSpecialIncomeCounter() throws NoMoreTilesException
	{
		currentState.setCurrentRegularPhase(RegularPhase.MOVEMENT);
		ITileProperties thing = currentState.getCup().drawTile();
		ArrayList<ITileProperties> thingsRemoved = new ArrayList<ITileProperties>();
		while(!thing.isSpecialIncomeCounter())
		{
			thingsRemoved.add(thing);
			thing = currentState.getCup().drawTile();
		}
		for(ITileProperties removedThing : thingsRemoved)
		{
			currentState.getCup().reInsertTile(removedThing);
		}
		
		currentState.getBoard().getHexByXY(1, 2).addThingToHex(thing);
		p1.addOwnedThingOnBoard(thing);
		
		HashSet<ITileProperties> thingSet = new HashSet<>();
		thingSet.add(thing);
		getCombatCommandHandler().removeThingsFromBoard(p1.getID(), currentState.getBoard().getHexByXY(1, 2).getHex(), thingSet);

		assertEquals(RegularPhase.MOVEMENT, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());
		assertEquals(false,currentState.getBoard().getHexByXY(1, 2).hasSpecialIncomeCounter());
		for(ITileProperties tp : p1.getOwnedThingsOnBoard())
		{
			assertEquals(false, tp.isSpecialIncomeCounter());
		}
		for(ITileProperties tp : p1.getTrayThings())
		{
			assertEquals(false, tp.isSpecialIncomeCounter());
		}
	}
	
	@Test
	public void testWinFromSingleCitadelConstruction() throws NoMoreTilesException
	{
		p1.addGold(25);
		HashSet<ITileProperties> unownedHexes = new HashSet<ITileProperties>();
		for(HexState hs : currentState.getBoard().getHexesAsList())
		{
			if(!p1.getOwnedHexes().contains(hs.getHex()) && !p2.getOwnedHexes().contains(hs.getHex()) && !p3.getOwnedHexes().contains(hs.getHex()) && !p4.getOwnedHexes().contains(hs.getHex()))
			{
				unownedHexes.add(hs.getHex());
			}
		}
		for(ITileProperties tp : unownedHexes)
		{
			p1.addOwnedHex(tp);
		}
		removeAllThingsFromHexForPlayer(p1);
		removeAllThingsFromHexForPlayer(p2);
		removeAllThingsFromHexForPlayer(p3);
		removeAllThingsFromHexForPlayer(p4);
		//RECRUITING_CHARACTERS, RECRUITING_THINGS, RANDOM_EVENTS, MOVEMENT, COMBAT, CONSTRUCTION, SPECIAL_POWERS
		currentState.setCurrentRegularPhase(RegularPhase.RECRUITING_CHARACTERS);
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(10,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().discardThings(new HashSet<ITileProperties>(p1.getCardsInHand()), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		for(int i=0; i<3; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p1.getID());
			getCombatCommandHandler().endPlayerTurn(p2.getID());
			getCombatCommandHandler().endPlayerTurn(p3.getID());
			getCombatCommandHandler().endPlayerTurn(p4.getID());
		}

		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p1.getID(), currentState.getBoard().getHexByXY(1, 2).getHex());

		for(int i=0; i<2; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p1.getID());
			getCombatCommandHandler().endPlayerTurn(p2.getID());
			getCombatCommandHandler().endPlayerTurn(p3.getID());
			getCombatCommandHandler().endPlayerTurn(p4.getID());
		}
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(10,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().discardThings(new HashSet<ITileProperties>(p1.getCardsInHand()), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());

		for(int i=0; i<3; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p2.getID());
			getCombatCommandHandler().endPlayerTurn(p3.getID());
			getCombatCommandHandler().endPlayerTurn(p4.getID());
			getCombatCommandHandler().endPlayerTurn(p1.getID());
		}

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Keep, p1.getID(), currentState.getBoard().getHexByXY(1, 2).getHex());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(10,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().discardThings(new HashSet<ITileProperties>(p1.getCardsInHand()), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		for(int i=0; i<3; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p3.getID());
			getCombatCommandHandler().endPlayerTurn(p4.getID());
			getCombatCommandHandler().endPlayerTurn(p1.getID());
			getCombatCommandHandler().endPlayerTurn(p2.getID());
		}

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Castle, p1.getID(), currentState.getBoard().getHexByXY(1, 2).getHex());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(10,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().discardThings(new HashSet<ITileProperties>(p1.getCardsInHand()), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());

		for(int i=0; i<3; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p4.getID());
			getCombatCommandHandler().endPlayerTurn(p1.getID());
			getCombatCommandHandler().endPlayerTurn(p2.getID());
			getCombatCommandHandler().endPlayerTurn(p3.getID());
		}

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Citadel, p1.getID(), currentState.getBoard().getHexByXY(1, 2).getHex());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());

		assertEquals(true,null == currentState.getWinningPlayer());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(10,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().discardThings(new HashSet<ITileProperties>(p1.getCardsInHand()), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		for(int i=0; i<3; i++)
		{
			getCombatCommandHandler().endPlayerTurn(p1.getID());
			getCombatCommandHandler().endPlayerTurn(p2.getID());
			getCombatCommandHandler().endPlayerTurn(p3.getID());
			getCombatCommandHandler().endPlayerTurn(p4.getID());
		}

		assertEquals(true,null == currentState.getWinningPlayer());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(true,null == currentState.getWinningPlayer());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(true,null == currentState.getWinningPlayer());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(true,null == currentState.getWinningPlayer());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(p1,currentState.getWinningPlayer());
	}
	
	@Test
	public void testFourPlayerCombatDefenderRetreat()
	{
		fourPlayerCombatUpToFirstRetreat();
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(4,currentState.getBoard().getHexByXY(3, 6).getThingsInHexOwnedByPlayer(p1).size());
		getCombatCommandHandler().retreatFromCombat(p1.getID(), currentState.getBoard().getHexByXY(2, 5).getHex());
		assertEquals(4,currentState.getBoard().getHexByXY(2, 5).getThingsInHexOwnedByPlayer(p1).size());
		assertEquals(4, p1.getOwnedThingsOnBoard().size());
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p4.getID(), p3.getID());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p2.getID());
		getCombatCommandHandler().setPlayersTarget(p2.getID(), p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 8));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)), p3.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		assertEquals(2, p1.getOwnedHexes().size());
		assertEquals(1, p2.getOwnedHexes().size());
		assertEquals(2, p3.getOwnedHexes().size());
		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 6).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(3, 6).getCreaturesInHex().size());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(3,6).getThingsInHex())
		{
			assertEquals(true,p3.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p4,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());
	}

	
	@Test
	public void testFourPlayerCombatDefenderRetreatCreatureOverflow() throws NoMoreTilesException
	{
		ArrayList<ITileProperties> removedThings = new ArrayList<>();
		ArrayList<ITileProperties> removedCreatures = new ArrayList<>();
		while(removedCreatures.size() < 8)
		{
			ITileProperties thing = currentState.getCup().drawTile();
			if(thing.isCreature())
			{
				removedCreatures.add(thing);
			}
			else
			{
				removedThings.add(thing);
			}
		}
		
		for(ITileProperties tp : removedCreatures)
		{
			currentState.getBoard().getHexByXY(2, 5).addThingToHex(tp);
			p1.addOwnedThingOnBoard(tp);
		}
		removedCreatures.clear();
		for(ITileProperties tp : removedThings)
		{
			currentState.getCup().reInsertTile(tp);
		}
		removedThings.clear();
		
		fourPlayerCombatUpToFirstRetreat();
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(4,currentState.getBoard().getHexByXY(3, 6).getThingsInHexOwnedByPlayer(p1).size());
		getCombatCommandHandler().retreatFromCombat(p1.getID(), currentState.getBoard().getHexByXY(2, 5).getHex());
		assertEquals(12,currentState.getBoard().getHexByXY(2, 5).getThingsInHexOwnedByPlayer(p1).size());
		assertEquals(12, p1.getOwnedThingsOnBoard().size());

		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(true, currentState.hasHexesThatNeedThingsRemoved());
		assertEquals(2, currentState.getThingsToRemoveFromHex(currentState.getBoard().getHexByXY(2, 5)));
		
		HashSet<ITileProperties> thingsToRemove = new HashSet<>();
		thingsToRemove.add(getPlayerBoardThingByName("Giant", p1, new Point(2,5)));
		getCombatCommandHandler().removeThingsFromBoard(p1.getID(), currentState.getBoard().getHexByXY(2, 5).getHex(), thingsToRemove);

		
		assertEquals(11,currentState.getBoard().getHexByXY(2, 5).getThingsInHexOwnedByPlayer(p1).size());
		assertEquals(11, p1.getOwnedThingsOnBoard().size());

		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(true, currentState.hasHexesThatNeedThingsRemoved());
		assertEquals(1, currentState.getThingsToRemoveFromHex(currentState.getBoard().getHexByXY(2, 5)));
		
		thingsToRemove.clear();
		thingsToRemove.add(getPlayerBoardThingByName("Elephant", p1, new Point(2,5)));
		getCombatCommandHandler().removeThingsFromBoard(p1.getID(), currentState.getBoard().getHexByXY(2, 5).getHex(), thingsToRemove);

		assertEquals(10,currentState.getBoard().getHexByXY(2, 5).getThingsInHexOwnedByPlayer(p1).size());
		assertEquals(10, p1.getOwnedThingsOnBoard().size());
		
		assertEquals(false, currentState.hasHexesThatNeedThingsRemoved());
		assertEquals(0, currentState.getThingsToRemoveFromHex(currentState.getBoard().getHexByXY(2, 5)));
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p4.getID(), p3.getID());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p2.getID());
		getCombatCommandHandler().setPlayersTarget(p2.getID(), p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 8));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)), p3.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		assertEquals(2, p1.getOwnedHexes().size());
		assertEquals(1, p2.getOwnedHexes().size());
		assertEquals(2, p3.getOwnedHexes().size());
		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 6).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(3, 6).getCreaturesInHex().size());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(3,6).getThingsInHex())
		{
			assertEquals(true,p3.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p4,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());
	}

	@Test
	public void testFourPlayerCombatAttackerRetreat()
	{
		fourPlayerCombatUpToFirstRetreat();
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getBoard().getHexByXY(3, 6).getThingsInHexOwnedByPlayer(p4).size());
		getCombatCommandHandler().retreatFromCombat(p4.getID(), currentState.getBoard().getHexByXY(2, 7).getHex());
		assertEquals(1,currentState.getBoard().getHexByXY(2, 7).getThingsInHexOwnedByPlayer(p4).size());
		assertEquals(1, p4.getOwnedThingsOnBoard().size());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p1.getID());
		getCombatCommandHandler().setPlayersTarget(p1.getID(), p2.getID());
		getCombatCommandHandler().setPlayersTarget(p2.getID(), p3.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(4,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)), p3.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)), p1.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		assertEquals(2, p1.getOwnedHexes().size());
		assertEquals(1, p2.getOwnedHexes().size());
		assertEquals(2, p3.getOwnedHexes().size());
		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 6).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(3, 6).getCreaturesInHex().size());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(3,6).getThingsInHex())
		{
			assertEquals(true,p3.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p4,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());
	}
	
	@Test
	public void testFourPlayerCombat()
	{
		fourPlayerCombatUpToFirstRetreat();
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p4.getID(), p3.getID());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p1.getID());
		getCombatCommandHandler().setPlayersTarget(p1.getID(), p2.getID());
		getCombatCommandHandler().setPlayersTarget(p2.getID(), p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)), p2.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 8));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(4,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)), p3.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p4.getID(), p3.getID());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p1.getID());
		getCombatCommandHandler().setPlayersTarget(p1.getID(), p3.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)), p1.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)), p3.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_TWO_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		assertEquals(2, p1.getOwnedHexes().size());
		assertEquals(1, p2.getOwnedHexes().size());
		assertEquals(2, p3.getOwnedHexes().size());
		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 6).getHex()));
		assertEquals(1,currentState.getBoard().getHexByXY(3, 6).getCreaturesInHex().size());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(3,6).getThingsInHex())
		{
			assertEquals(true,p3.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		
		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p4,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());
	}
	
	private void fourPlayerCombatUpToFirstRetreat()
	{
		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(3, 6).getHex(), p1.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(true,tp.isFaceUp());
		}
		assertEquals(40, currentState.getCombatHex().getThingsInHex().size());
		assertEquals(40, currentState.getCombatHex().getFightingThingsInHex().size());
		
		assertEquals(CombatPhase.SELECT_TARGET_PLAYER, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().setPlayersTarget(p4.getID(), p2.getID());
		getCombatCommandHandler().setPlayersTarget(p3.getID(), p1.getID());
		getCombatCommandHandler().setPlayersTarget(p1.getID(), p3.getID());
		getCombatCommandHandler().setPlayersTarget(p2.getID(), p4.getID());

		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Dervish",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Druid",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Witch_Doctor",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Dark_Wizard",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 4));

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(2,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(2,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Skeletons",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Goblins",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Skeletons",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Dark_Wizard",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Lizard",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Dwarves",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Pterodactyl_Warriors",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(2,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Spider",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Farmers",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Farmers",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Villains",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p1.getID(), 3));
		
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Sandworm",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crocodiles",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Nomads",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Bandits",p2,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 6));
		
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Centaur",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 4));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Pygmies",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Nomads",p3,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p3.getID(), 1));
		
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6),1),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Black_Knight",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Black_Knight",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 2));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Vampire_Bat",p4,new Point(3, 6)),RollReason.ATTACK_WITH_CREATURE, p4.getID(), 8));

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(3,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(7,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(4,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(6,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Ogre",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Watusi",p1,new Point(3, 6)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Dwarves",p1,new Point(3, 6)), p1.getID(), 1);

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Nomads",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Dervish",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crocodiles",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Bandits",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Sandworm",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Druid",p2,new Point(3, 6)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Pterodactyl_Warriors",p2,new Point(3, 6)), p2.getID(), 1);
		
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Nomads",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Centaur",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Pygmies",p3,new Point(3, 6)), p3.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Witch_Doctor",p3,new Point(3, 6)), p3.getID(), 1);
		
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tigers",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Black_Knight",p4,new Point(3, 6)), p4.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Vampire_Bat",p4,new Point(3, 6)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p3.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());
	}
	
	private void addTenThingsToHexForPlayer(Player p) throws NoMoreTilesException
	{
		for(int i=0; i<10; i++)
		{
			ITileProperties thing = currentState.getCup().drawTile();
			p.addOwnedThingOnBoard(thing);
			currentState.getBoard().getHexByXY(3, 6).addThingToHex(thing);
		}
	}
	
	private void removeAllThingsFromHexForPlayer(Player p)
	{
		HexState hex = currentState.getBoard().getHexByXY(3, 6);
		HashSet<ITileProperties> things = new HashSet<ITileProperties>(p.getOwnedThingsOnBoard());
		for(ITileProperties thing : things)
		{
			p.removeOwnedThingOnBoard(thing);
			hex.removeThingFromHex(thing);
			currentState.getCup().reInsertTile(thing);
		}
	}
}
