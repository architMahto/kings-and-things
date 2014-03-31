package server.logic.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Test;

import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.handlers.RecruitSpecialCharacterCommandHandler;
import common.Constants;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Roll;

public class TestGameFlow extends TestingUtils{

	private static final Point FIRST_SPOT = new Point(1,2);
	private static final Point SECOND_SPOT = new Point(5,2);
	private static final Point THIRD_SPOT = new Point(1,10);
	private static final Point FOURTH_SPOT = new Point(5,10);
	
	@Test
	public void testStartNewGame() {
		assertEquals(4,currentState.getPlayers().size());
		assertEquals(currentState.getActivePhasePlayer(),currentState.getActiveTurnPlayer());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertEquals(SetupPhase.PICK_FIRST_HEX, currentState.getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_CHARACTERS, currentState.getCurrentRegularPhase());
		assertEquals(Constants.MAX_HEXES_ON_BOARD, currentState.getBoard().getHexesAsList().size());
	}

	@Test
	public void testGiveHexToPlayer() {
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		assertEquals(1,p1.getOwnedHexes().size());
		assertEquals(1,p2.getOwnedHexes().size());
		assertEquals(1,p3.getOwnedHexes().size());
		assertEquals(1,p4.getOwnedHexes().size());
		
		assertEquals(currentState.getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex(),p1.getOwnedHexes().iterator().next());
		assertEquals(currentState.getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex(),p2.getOwnedHexes().iterator().next());
		assertEquals(currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex(),p3.getOwnedHexes().iterator().next());
		assertEquals(currentState.getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex(),p4.getOwnedHexes().iterator().next());
		
		assertEquals(SetupPhase.EXCHANGE_SEA_HEXES,currentState.getCurrentSetupPhase());
	}

	@Test
	public void testGiveSecondHexToPlayer() {
		testGiveHexToPlayer();
		//skip exchange sea hex phase
		getSetupPhaseCommandHandler().endPlayerTurn(p1.getID());
		getSetupPhaseCommandHandler().endPlayerTurn(p2.getID());
		getSetupPhaseCommandHandler().endPlayerTurn(p3.getID());
		getSetupPhaseCommandHandler().endPlayerTurn(p4.getID());
		
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(4, 3).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(5, 8).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(1, 8).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(3);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(2, 3).getHex(), p4.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(2,p2.getOwnedHexes().size());
		assertEquals(2,p3.getOwnedHexes().size());
		assertEquals(2,p4.getOwnedHexes().size());
		
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex()));

		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 3).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(5, 8).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(1, 8).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 3).getHex()));
		
		assertEquals(SetupPhase.PICK_THIRD_HEX,currentState.getCurrentSetupPhase());
	}

	@Test
	public void testGiveThirdHexToPlayer() {
		testGiveSecondHexToPlayer();
		
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(4, 5).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(6, 7).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(2, 9).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(3);
		assertPlayerAtIndexIsActiveForTurn(0);
		getSetupPhaseCommandHandler().giveHexToPlayer(currentState.getBoard().getHexByXY(1, 4).getHex(), p4.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		validateThirdHexGivenState();
		
		assertEquals(SetupPhase.PLACE_FREE_TOWER, currentState.getCurrentSetupPhase());
	}

	@Test
	public void testConstructBuilding() {
		testGiveThirdHexToPlayer();
		assertEquals(10,p1.getGold());
		assertEquals(10,p2.getGold());
		assertEquals(10,p3.getGold());
		assertEquals(10,p4.getGold());
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p1.getID(),currentState.getBoard().getHexByXY(4, 5).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(4, 5).hasBuilding());
		assertEquals(1,p1.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p1.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p1.ownsThingOnBoard(currentState.getBoard().getHexByXY(4, 5).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(4, 5).getBuilding().isFaceUp());
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p2.getID(),currentState.getBoard().getHexByXY(5, 8).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(5, 8).hasBuilding());
		assertEquals(1,p2.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p2.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p2.ownsThingOnBoard(currentState.getBoard().getHexByXY(5, 8).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(5, 8).getBuilding().isFaceUp());
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p3.getID(),currentState.getBoard().getHexByXY(2, 9).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(2, 9).hasBuilding());
		assertEquals(1,p3.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p3.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p3.ownsThingOnBoard(currentState.getBoard().getHexByXY(2, 9).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(2, 9).getBuilding().isFaceUp());
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p4.getID(),currentState.getBoard().getHexByXY(2, 3).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(2, 3).hasBuilding());
		assertEquals(1,p4.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p4.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p4.ownsThingOnBoard(currentState.getBoard().getHexByXY(2, 3).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(2, 3).getBuilding().isFaceUp());

		validateThirdHexGivenState();
		
		assertEquals(SetupPhase.PLACE_FREE_THINGS,currentState.getCurrentSetupPhase());
	}

	@Test
	public void testPlaceThingOnBoard() {
		testConstructBuilding();
		
		assertEquals(10,p1.getTrayThings().size());
		assertEquals(10,p2.getTrayThings().size());
		assertEquals(10,p3.getTrayThings().size());
		assertEquals(10,p4.getTrayThings().size());
		
		String[] stackOneNames = {"Old_Dragon","Giant_Spider","Elephant","Brown_Knight","Giant","Dwarves"};
		for(String s : stackOneNames)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p1).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 5).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p1, new Point(4, 5)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p1, new Point(4, 5)).isFaceUp());
		}

		String[] stackTwoNames = {"Skeletons","Watusi","Goblins","Ogre"};
		for(String s : stackTwoNames)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p1).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 3).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p1, new Point(4, 3)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p1, new Point(4, 3)).isFaceUp());
		}
		
		assertEquals(0,p1.getTrayThings().size());
		assertEquals(11,p1.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		
		/**********************/

		String[] stackOneNames2 = {"Pterodactyl_Warriors","Sandworm","Green_Knight","Dervish","Crocodiles","Nomads","Druid","Walking_Tree","Crawling_Vines","Bandits"};
		for(String s : stackOneNames2)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p2).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p2), p2.getID(), currentState.getBoard().getHexByXY(5, 8).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p2, new Point(5, 8)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p2, new Point(5, 8)).isFaceUp());
		}

		assertEquals(0,p2.getTrayThings().size());
		assertEquals(11,p2.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		
		/*********************/

		String[] stackOneNames3 = {"Centaur","Camel_Corps","Farmers"};
		for(String s : stackOneNames3)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p3).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(2, 9).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p3, new Point(2, 9)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p3, new Point(2, 9)).isFaceUp());
		}
		getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName("Farmers",p3), p3.getID(), currentState.getBoard().getHexByXY(2, 9).getHex());

		String[] stackTwoNames3 = {"Genie","Skeletons","Pygmies"};
		for(String s : stackTwoNames3)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p3).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p3, new Point(THIRD_SPOT.x, THIRD_SPOT.y)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p3, new Point(THIRD_SPOT.x, THIRD_SPOT.y)).isFaceUp());
		}

		String[] stackThreeNames3 = {"Greathunter","Nomads","Witch_Doctor"};
		for(String s : stackThreeNames3)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p3).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(1, 8).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p3, new Point(1, 8)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p3, new Point(1, 8)).isFaceUp());
		}
		
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(11,p3.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		
		/*********************/

		String[] stackOneNames4 = {"Tribesmen","Giant_Lizard","Villains","Tigers"};
		for(String s : stackOneNames4)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p4).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(2, 3).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p4, new Point(2, 3)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p4, new Point(2, 3)).isFaceUp());
		}

		String[] stackTwoNames4 = {"Vampire_Bat","Tribesmen","Dark_Wizard","Black_Knight"};
		for(String s : stackTwoNames4)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p4).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(1, 4).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p4, new Point(1, 4)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p4, new Point(1, 4)).isFaceUp());
		}

		String[] stackThreeNames4 = {"Giant_Ape","Buffalo_Herd"};
		for(String s : stackThreeNames4)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p4).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p4, new Point(FIRST_SPOT.x, FIRST_SPOT.y)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p4, new Point(FIRST_SPOT.x, FIRST_SPOT.y)).isFaceUp());
		}
		
		assertEquals(0,p4.getTrayThings().size());
		assertEquals(11,p4.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(SetupPhase.EXCHANGE_THINGS,currentState.getCurrentSetupPhase());
	}

	@Test
	public void testAdvanceToRegularPlay() {
		testPlaceThingOnBoard();
		
		//skip exchange things phase
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		//skip place exchanged things phase
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(14,p1.getGold());
		assertEquals(14,p2.getGold());
		assertEquals(14,p3.getGold());
		assertEquals(14,p4.getGold());

		assertEquals(0,p1.getTrayThings().size());
		assertEquals(0,p2.getTrayThings().size());
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(0,p4.getTrayThings().size());
		
		assertEquals(SetupPhase.SETUP_FINISHED, currentState.getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_CHARACTERS,currentState.getCurrentRegularPhase());
		assertEquals(p1.getID(),currentState.getActivePhasePlayer().getID());
		assertEquals(p1.getID(),currentState.getActiveTurnPlayer().getID());
	}

	@Test
	public void testRecruitingThings() throws NoMoreTilesException {
		testAdvanceToRegularPlay();
		
		//skip recruiting characters phase
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(0,p1.getTrayThings().size());
		assertEquals(0,p2.getTrayThings().size());
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(0,p4.getTrayThings().size());
		
		assertEquals(SetupPhase.SETUP_FINISHED, currentState.getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_THINGS,currentState.getCurrentRegularPhase());
		assertEquals(p1.getID(),currentState.getActivePhasePlayer().getID());
		assertEquals(p1.getID(),currentState.getActiveTurnPlayer().getID());
		
		getRecruitingThingsCommandHandler().recruitThings(5, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(3,p1.getTrayThings().size());
		assertEquals(9,p1.getGold());

		String[] stackTwoNames = {"Cyclops","Mountain_Men","Goblins"};
		for(String s : stackTwoNames)
		{
			assertEquals(true,getPlayerTrayThingByName(s,p1).getValue()>0);
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 3).getHex());
			assertEquals(true,getPlayerBoardThingByName(s, p1, new Point(4,3)).getValue()>0);
			assertEquals(false,getPlayerBoardThingByName(s, p1, new Point(4, 3)).isFaceUp());
		}
		assertEquals(0,p1.getTrayThings().size());
		assertEquals(14,p1.getOwnedThingsOnBoard().size());
		
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		assertEquals(2,p2.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		assertEquals(2,p3.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		assertEquals(2,p4.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
	}
	
	@Test
	public void testMovingThings() throws NoMoreTilesException
	{
		testRecruitingThings();

		//skip random events phase
		getMovementCommandHandler().endPlayerTurn(p1.getID());
		getMovementCommandHandler().endPlayerTurn(p2.getID());
		getMovementCommandHandler().endPlayerTurn(p3.getID());
		getMovementCommandHandler().endPlayerTurn(p4.getID());
		
		for(HexState hs : currentState.getBoard().getHexesAsList())
		{
			for(ITileProperties tp : hs.getCreaturesInHex())
			{
				assertEquals(Constants.MAX_MOVE_SPEED, tp.getMoveSpeed());
			}
		}

		assertEquals(7,currentState.getBoard().getHexByXY(4, 3).getCreaturesInHex().size());
		
		ArrayList<ITileProperties> hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(4, 3).getHex());
		hexes.add(currentState.getBoard().getHexByXY(3, 2).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(4, 3).getCreaturesInHex(), p1.getID(), hexes);

		assertEquals(0,currentState.getBoard().getHexByXY(4, 3).getCreaturesInHex().size());
		assertEquals(7,currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex().size());
		
		for(ITileProperties tp : currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex())
		{
			assertEquals(Constants.MAX_MOVE_SPEED - 2,tp.getMoveSpeed());
		}
		
		getMovementCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(10,currentState.getBoard().getHexByXY(5, 8).getCreaturesInHex().size());
		
		hexes.clear();
		hexes.add(currentState.getBoard().getHexByXY(5, 8).getHex());
		hexes.add(currentState.getBoard().getHexByXY(4, 7).getHex());
		
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(5, 8).getCreaturesInHex(), p2.getID(), hexes);

		assertEquals(0,currentState.getBoard().getHexByXY(5, 8).getCreaturesInHex().size());
		assertEquals(10,currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex().size());

		for(ITileProperties tp : currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex())
		{
			assertEquals(Constants.MAX_MOVE_SPEED - 1,tp.getMoveSpeed());
		}
		
		getMovementCommandHandler().endPlayerTurn(p2.getID());

		//skip other player movement
		getMovementCommandHandler().endPlayerTurn(p3.getID());
		getMovementCommandHandler().endPlayerTurn(p4.getID());
	}

	@Test
	public void testExploration()
	{
		try
		{
			testMovingThings();
		}
		catch (NoMoreTilesException e)
		{
			fail(e.getMessage());
		}
		
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		
		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(3, 2).getHex(), p1.getID());
		for(ITileProperties tp : currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}
		getCombatCommandHandler().rollDice(new Roll(1,currentState.getBoard().getHexByXY(3, 2).getHex(),RollReason.EXPLORE_HEX, p1.getID(), 1));
		allDoneRolling();
		assertEquals(4, p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 2).getHex()));
		assertEquals(7,currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex().size());
		for(ITileProperties tp : currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}
		
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		

		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(4, 7).getHex(), p2.getID());
		getCombatCommandHandler().rollDice(new Roll(1,currentState.getBoard().getHexByXY(4, 7).getHex(),RollReason.EXPLORE_HEX, p2.getID(),1));
		allDoneRolling();
		assertEquals(4, p2.getOwnedHexes().size());
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 7).getHex()));
		assertEquals(10,currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex().size());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		
		//skip other player combat
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
	}
	
	@Test
	public void testTurnTwoIncome()
	{
		testExploration();

		//skip construction phase
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());

		//skip special powers phase
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		
		assertEquals(RegularPhase.RECRUITING_CHARACTERS, currentState.getCurrentRegularPhase());
		assertEquals(p2.getID(), currentState.getActivePhasePlayer().getID());
		assertEquals(p2.getID(), currentState.getActiveTurnPlayer().getID());

		assertEquals(14,p1.getGold());
		assertEquals(19,p2.getGold());
		assertEquals(18,p3.getGold());
		assertEquals(18,p4.getGold());
	}

	@Test
	public void testTurnTwoRecruitment() throws NoMoreTilesException
	{
		testTurnTwoIncome();

		//skip recruit characters phase
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(RegularPhase.RECRUITING_THINGS, currentState.getCurrentRegularPhase());
		assertEquals(p2.getID(), currentState.getActivePhasePlayer().getID());
		assertEquals(p2.getID(), currentState.getActiveTurnPlayer().getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		assertEquals(4,p2.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		assertEquals(4,p3.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		assertEquals(4,p4.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		assertEquals(2,p1.getTrayThings().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
	}

	@Test
	public void testTurnTwoMovement() throws NoMoreTilesException
	{
		testTurnTwoRecruitment();

		//skip random events phase
		getMovementCommandHandler().endPlayerTurn(p2.getID());
		getMovementCommandHandler().endPlayerTurn(p3.getID());
		getMovementCommandHandler().endPlayerTurn(p4.getID());
		getMovementCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(RegularPhase.MOVEMENT, currentState.getCurrentRegularPhase());
		assertEquals(p2.getID(), currentState.getActivePhasePlayer().getID());
		assertEquals(p2.getID(), currentState.getActiveTurnPlayer().getID());

		for(HexState hs : currentState.getBoard().getHexesAsList())
		{
			for(ITileProperties tp : hs.getCreaturesInHex())
			{
				assertEquals(Constants.MAX_MOVE_SPEED, tp.getMoveSpeed());
			}
		}

		ArrayList<ITileProperties> hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(4, 7).getHex());
		hexes.add(currentState.getBoard().getHexByXY(4, 5).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex(), p2.getID(), hexes);

		assertEquals(0,currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex().size());
		assertEquals(10,currentState.getBoard().getHexByXY(4, 5).getThingsInHexOwnedByPlayer(p2).size());

		for(ITileProperties tp : currentState.getBoard().getHexByXY(4, 5).getThingsInHexOwnedByPlayer(p2))
		{
			assertEquals(Constants.MAX_MOVE_SPEED - 1,tp.getMoveSpeed());
		}
		
		getMovementCommandHandler().endPlayerTurn(p2.getID());

		//skip player 3 and 4 movement
		getMovementCommandHandler().endPlayerTurn(p3.getID());
		getMovementCommandHandler().endPlayerTurn(p4.getID());

		hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(3, 2).getHex());
		hexes.add(currentState.getBoard().getHexByXY(2, 3).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex(), p1.getID(), hexes);

		assertEquals(0,currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex().size());
		assertEquals(7,currentState.getBoard().getHexByXY(2, 3).getThingsInHexOwnedByPlayer(p1).size());

		for(ITileProperties tp : currentState.getBoard().getHexByXY(2, 3).getThingsInHexOwnedByPlayer(p1))
		{
			assertEquals(Constants.MAX_MOVE_SPEED - 1,tp.getMoveSpeed());
		}
		
		getMovementCommandHandler().endPlayerTurn(p1.getID());
	}
	
	@Test
	public void testTurnTwoFirstCombat()
	{
		turnTwoFirstCombatUpToDetermineDamageRoll();
		
		getCombatCommandHandler().rollDice(new Roll(1,currentState.getCombatHex().getBuilding(),RollReason.CALCULATE_DAMAGE_TO_TILE, p2.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		assertEquals(5, p2.getOwnedHexes().size());
		assertEquals(3, p1.getOwnedHexes().size());
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 5).getHex()));
		assertEquals(3,currentState.getBoard().getHexByXY(4, 5).getCreaturesInHex().size());
		assertEquals(1,currentState.getBoard().getHexByXY(4, 5).getBuilding().getValue());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(4, 5).getThingsInHex())
		{
			assertEquals(true,p2.ownsThingOnBoard((thing)));
		}
		assertEquals(Building.Tower.name(),currentState.getCombatHex().getBuilding().getName());
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());
	}

	@Test
	public void testTurnTwoSecondCombat()
	{
		testTurnTwoFirstCombat();
		//no combat for p3
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p4,currentState.getActivePhasePlayer());

		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(2, 3).getHex(), p4.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(true,tp.isFaceUp());
		}
		
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tribesmen",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Lizard",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Villains",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tower",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Skeletons",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Goblins",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Mountain_Men",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Goblins",p1,new Point(2,3),1),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Skeletons",p1,new Point(2,3)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tribesmen",p4,new Point(2,3)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Lizard",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Villains",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tower",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Goblins",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Mountain_Men",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Goblins",p1,new Point(2,3),1),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Goblins",p1,new Point(2,3),1), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Lizard",p4,new Point(2,3)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Villains",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tower",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Mountain_Men",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Goblins",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Goblins",p1,new Point(2,3)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tower",p4,new Point(2,3)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Villains",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Mountain_Men",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Mountain_Men",p1,new Point(2,3)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Villains",p4,new Point(2,3)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tigers",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),3));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Ogre",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Ogre",p1,new Point(2,3)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tigers",p4,new Point(2,3)), p4.getID(), 1);

		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		assertEquals(CombatPhase.DETERMINE_DAMAGE, currentState.getCurrentCombatPhase());
		
		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(4, p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 3).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(2, 3).getCreaturesInHex().size());
		assertEquals(0,currentState.getBoard().getHexByXY(2, 3).getBuilding().getValue());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(2, 3).getThingsInHex())
		{
			assertEquals(true,p1.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		
		getCombatCommandHandler().rollDice(new Roll(1,currentState.getCombatHex().getBuilding(),RollReason.CALCULATE_DAMAGE_TO_TILE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());

		assertEquals(2, p4.getOwnedHexes().size());
		assertEquals(4, p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 3).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(2, 3).getCreaturesInHex().size());
		assertEquals(1,currentState.getBoard().getHexByXY(2, 3).getBuilding().getValue());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(2, 3).getThingsInHex())
		{
			assertEquals(true,p1.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		assertEquals(Building.Tower.name(),currentState.getCombatHex().getBuilding().getName());
		
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p1,currentState.getActivePhasePlayer());

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());
	}

	@Test
	public void testTurnTwoRegularConstruction()
	{
		testTurnTwoSecondCombat();
		
		assertEquals(19,p2.getGold());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Keep, p2.getID(), currentState.getBoard().getHexByXY(4, 5).getHex());
		assertEquals(Building.Keep.name(),currentState.getBoard().getHexByXY(4, 5).getBuilding().getName());
		assertEquals(true,p2.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(4, 5).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(4, 5).getBuilding().isFaceUp());
		assertEquals(2,currentState.getBoard().getHexByXY(4, 5).getBuilding().getValue());
		assertEquals(14,p2.getGold());

		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Keep, p2.getID(), currentState.getBoard().getHexByXY(5, 8).getHex());
		assertEquals(Building.Keep.name(),currentState.getBoard().getHexByXY(5, 8).getBuilding().getName());
		assertEquals(true,p2.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(5, 8).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(5, 8).getBuilding().isFaceUp());
		assertEquals(2,currentState.getBoard().getHexByXY(5, 8).getBuilding().getValue());
		assertEquals(9,p2.getGold());

		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p2.getID(), currentState.getBoard().getHexByXY(6, 7).getHex());
		assertEquals(Building.Tower.name(),currentState.getBoard().getHexByXY(6, 7).getBuilding().getName());
		assertEquals(true,p2.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(6, 7).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(6, 7).getBuilding().isFaceUp());
		assertEquals(1,currentState.getBoard().getHexByXY(6, 7).getBuilding().getValue());
		assertEquals(4,p2.getGold());

		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());

		assertEquals(18,p4.getGold());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p4.getID(), currentState.getBoard().getHexByXY(1, 2).getHex());
		assertEquals(Building.Tower.name(),currentState.getBoard().getHexByXY(1, 2).getBuilding().getName());
		assertEquals(true,p4.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(1, 2).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(1, 2).getBuilding().isFaceUp());
		assertEquals(1,currentState.getBoard().getHexByXY(1, 2).getBuilding().getValue());
		assertEquals(13,p4.getGold());

		getCombatCommandHandler().endPlayerTurn(p4.getID());

		assertEquals(14,p1.getGold());
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Keep, p1.getID(), currentState.getBoard().getHexByXY(2, 3).getHex());
		assertEquals(Building.Keep.name(),currentState.getBoard().getHexByXY(2, 3).getBuilding().getName());
		assertEquals(true,p1.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(2, 3).getBuilding()));
		assertEquals(true,currentState.getBoard().getHexByXY(2, 3).getBuilding().isFaceUp());
		assertEquals(2,currentState.getBoard().getHexByXY(2, 3).getBuilding().getValue());
		assertEquals(9,p1.getGold());

		getCombatCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(RegularPhase.SPECIAL_POWERS, currentState.getCurrentRegularPhase());
		assertEquals(p2,currentState.getActivePhasePlayer());
	}
	
	@Test
	public void testRecruitSpecialCharacters() throws NoMoreTilesException
	{
		testTurnTwoIncome();
		
		RecruitSpecialCharacterCommandHandler handler = getSpecialCharRecruitHandler();
		handler.handleSpecialCharacterRollModification(new SpecialCharacterManager(true).drawTileByName("Assassin_Primus"), p2.getID(), 5);
		handler.rollDice(new Roll(2,new SpecialCharacterManager(true).drawTileByName("Assassin_Primus"),RollReason.RECRUIT_SPECIAL_CHARACTER, p2.getID(), 6));
		allDoneRolling();
		assertEquals(false,p2.hasCardsInHand());
		
		handler.handleSpecialCharacterRollModification(new SpecialCharacterManager(true).drawTileByName("Assassin_Primus"), p2.getID(), 10);
		assertEquals(true,p2.hasCardsInHand());
		assertEquals(1,p2.getCardsInHand().size());
		assertEquals(new SpecialCharacterManager(true).drawTileByName("Assassin_Primus"),p2.getCardsInHand().iterator().next());

		int sizeBefore = currentState.getBoard().getHexByXY(6, 7).getThingsInHexOwnedByPlayer(p2).size();
		getRecruitingThingsCommandHandler().placeThingOnBoard(p2.getCardsInHand().iterator().next(), p2.getID(), currentState.getBoard().getHexByXY(6, 7).getHex());
		assertEquals(false,p2.hasCardsInHand());
		assertEquals(sizeBefore+1,currentState.getBoard().getHexByXY(6, 7).getThingsInHexOwnedByPlayer(p2).size());
		assertEquals(new SpecialCharacterManager(true).drawTileByName("Assassin_Primus"),getPlayerBoardThingByName("Assassin_Primus",p2,new Point(6,7)));
	}
	
	@Test
	public void testTurnTwoFirstCombatTowerRemoved()
	{
		turnTwoFirstCombatUpToDetermineDamageRoll();

		getCombatCommandHandler().rollDice(new Roll(1,currentState.getCombatHex().getBuilding(),RollReason.CALCULATE_DAMAGE_TO_TILE, p2.getID(), 1));
		allDoneRolling();

		assertEquals(CombatPhase.PLACE_THINGS, currentState.getCurrentCombatPhase());
		
		assertEquals(5, p2.getOwnedHexes().size());
		assertEquals(3, p1.getOwnedHexes().size());
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 5).getHex()));
		assertEquals(3,currentState.getBoard().getHexByXY(4, 5).getCreaturesInHex().size());
		assertEquals(false,currentState.getBoard().getHexByXY(4, 5).hasBuilding());
		assertEquals(null,currentState.getBoard().getHexByXY(4, 5).getBuilding());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(4, 5).getThingsInHex())
		{
			assertEquals(true,p2.ownsThingOnBoard((thing)));
		}
		int buildingCount = 0;
		for(ITileProperties thing : p2.getOwnedThingsOnBoard())
		{
			if(thing.isBuilding())
			{
				buildingCount++;
			}
		}
		assertEquals(1,buildingCount);
		
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());
	}
	
	@Test
	public void testTurnThreeAdvanceToCombat() throws NoMoreTilesException
	{
		testTurnTwoRegularConstruction();
		
		//skip special powers phase
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		//skip recruiting characters phase
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		//recruit free junk
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());

		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		
		getRecruitingThingsCommandHandler().recruitThings(0, new ArrayList<ITileProperties>(), p2.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		//skip random events
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());

		ArrayList<ITileProperties> hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(1, 8).getHex());
		hexes.add(currentState.getBoard().getHexByXY(1, 6).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(1, 8).getCreaturesInHex(), p3.getID(), hexes);

		hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(1, 10).getHex());
		hexes.add(currentState.getBoard().getHexByXY(2, 11).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(1, 10).getCreaturesInHex(), p3.getID(), hexes);

		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());

		hexes = new ArrayList<ITileProperties>();
		hexes.add(currentState.getBoard().getHexByXY(1, 2).getHex());
		hexes.add(currentState.getBoard().getHexByXY(2, 3).getHex());
		getMovementCommandHandler().moveThings(currentState.getBoard().getHexByXY(1, 2).getCreaturesInHex(), p4.getID(), hexes);

		getRecruitingThingsCommandHandler().endPlayerTurn(p4.getID());

		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
	}
	
	@Test
	public void testTurnThreeKeepDowngraded()
	{
		try
		{
			testTurnThreeAdvanceToCombat();
		}
		catch (NoMoreTilesException e)
		{
			fail(e.getMessage());
		}
		
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		
		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(2, 11).getHex(), p3.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}

		getCombatCommandHandler().rollDice(new Roll(1,currentState.getBoard().getHexByXY(2, 11).getHex(),RollReason.EXPLORE_HEX, p3.getID(),1));
		allDoneRolling();
		assertEquals(4, p3.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 11).getHex()));
		assertEquals(3,currentState.getBoard().getHexByXY(2, 11).getCreaturesInHex().size());
		for(ITileProperties tp : currentState.getBoard().getHexByXY(2, 11).getCreaturesInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());

		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(2, 3).getHex(), p4.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(true,tp.isFaceUp());
		}

		assertEquals(5,currentState.getCombatHex().getFightingThingsInHex().size());
		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Watusi",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Keep",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),1));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(2,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
		
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Watusi",p1,new Point(2,3)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Cyclops",p1,new Point(2,3)), p1.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(3,currentState.getCombatHex().getFightingThingsInHex().size());

		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Keep",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),6));
		allDoneRolling();

		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Keep",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),6));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Keep",p1,new Point(2,3)), p1.getID(), 1);
		assertEquals(1,currentState.getBoard().getHexByXY(2,3).getBuilding().getValue());

		assertEquals(CombatPhase.ATTACKER_THREE_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());

		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Keep",p1,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),6));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant_Ape",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),1));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(2,3)),RollReason.ATTACK_WITH_CREATURE, p4.getID(),1));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(2,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Keep",p1,new Point(2,3)), p1.getID(), 1);
		
		assertEquals(CombatPhase.DETERMINE_DAMAGE, currentState.getCurrentCombatPhase());
		
		assertEquals(3, p4.getOwnedHexes().size());
		assertEquals(3, p1.getOwnedHexes().size());
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2,3).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(2,3).getCreaturesInHex().size());
		assertEquals(0,currentState.getBoard().getHexByXY(2,3).getBuilding().getValue());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(2,3).getThingsInHex())
		{
			assertEquals(true,p4.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}

		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Keep",p4,new Point(2,3)),RollReason.CALCULATE_DAMAGE_TO_TILE, p4.getID(),6));
		allDoneRolling();

		assertEquals(3, p4.getOwnedHexes().size());
		assertEquals(3, p1.getOwnedHexes().size());
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2,3).getHex()));
		assertEquals(2,currentState.getBoard().getHexByXY(2,3).getCreaturesInHex().size());
		assertEquals(1,currentState.getBoard().getHexByXY(2,3).getBuilding().getValue());
		assertEquals(Building.Tower.name(),currentState.getBoard().getHexByXY(2,3).getBuilding().getName());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(2,3).getThingsInHex())
		{
			assertEquals(true,p4.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
		assertEquals(true,p4.getOwnedThingsOnBoard().contains(currentState.getBoard().getHexByXY(2,3).getBuilding()));

		getCombatCommandHandler().endPlayerTurn(p4.getID());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p4.getID()));
	}
	
	@Test
	public void testTurnThreeAllCombatsResolved()
	{
		testTurnThreeKeepDowngraded();

		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(RegularPhase.COMBAT,currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());

		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(1, 6).getHex(), p3.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}

		getCombatCommandHandler().rollDice(new Roll(1,currentState.getBoard().getHexByXY(1, 6).getHex(),RollReason.EXPLORE_HEX, p3.getID(),6));
		allDoneRolling();
		assertEquals(5, p3.getOwnedHexes().size());
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(1, 6).getHex()));
		assertEquals(3,currentState.getBoard().getHexByXY(1, 6).getCreaturesInHex().size());
		for(ITileProperties tp : currentState.getBoard().getHexByXY(1, 6).getCreaturesInHex())
		{
			assertEquals(false,tp.isFaceUp());
		}
		
		getCombatCommandHandler().endPlayerTurn(p3.getID());
		getCombatCommandHandler().endPlayerTurn(p4.getID());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.NO_COMBAT, currentState.getCurrentCombatPhase());
		assertEquals(RegularPhase.CONSTRUCTION, currentState.getCurrentRegularPhase());
		assertEquals(p3,currentState.getActivePhasePlayer());
	}
	
	private void validateThirdHexGivenState()
	{
		assertEquals(3,p1.getOwnedHexes().size());
		assertEquals(3,p2.getOwnedHexes().size());
		assertEquals(3,p3.getOwnedHexes().size());
		assertEquals(3,p4.getOwnedHexes().size());

		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex()));

		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 3).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(5, 8).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(1, 8).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 3).getHex()));
		
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 5).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(6, 7).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(currentState.getBoard().getHexByXY(2, 9).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(currentState.getBoard().getHexByXY(1, 4).getHex()));
		
	}
	
	private void turnTwoFirstCombatUpToDetermineDamageRoll()
	{
		try
		{
			testTurnTwoMovement();
		}
		catch (NoMoreTilesException e)
		{
			fail(e.getMessage());
		}
		
		assertEquals(RegularPhase.COMBAT, currentState.getCurrentRegularPhase());
		
		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(4, 5).getHex(), p2.getID());
		for(ITileProperties tp : currentState.getCombatHex().getThingsInHex())
		{
			assertEquals(true,tp.isFaceUp());
		}

		assertEquals(17,currentState.getCombatHex().getFightingThingsInHex().size());
		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Dervish",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Druid",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());
		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));
		
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant_Spider",p1,new Point(4,5)), p1.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(16,currentState.getCombatHex().getFightingThingsInHex().size());

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Nomads",p2,new Point(4,5)), p2.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(15,currentState.getCombatHex().getFightingThingsInHex().size());

		assertEquals(CombatPhase.RANGED_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Pterodactyl_Warriors",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Giant",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Dwarves",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_RANGED_HITS, currentState.getCurrentCombatPhase());
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Bandits",p2,new Point(4,5)), p2.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(14,currentState.getCombatHex().getFightingThingsInHex().size());

		assertEquals(CombatPhase.MELEE_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Elephant",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Brown_Knight",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Tower",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Sandworm",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(2,getPlayerBoardThingByName("Green_Knight",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crocodiles",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Walking_Tree",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Crawling_Vines",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(),3));
		allDoneRolling();

		assertEquals(CombatPhase.APPLY_MELEE_HITS, currentState.getCurrentCombatPhase());
		assertEquals(5,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(4,currentState.getHitsOnPlayer(p2.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Elephant",p1,new Point(4,5)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Brown_Knight",p1,new Point(4,5)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Tower",p1,new Point(4,5)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Dwarves",p1,new Point(4,5)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Giant",p1,new Point(4,5)), p1.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(9,currentState.getCombatHex().getFightingThingsInHex().size());
		
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Crocodiles",p2,new Point(4,5)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Dervish",p2,new Point(4,5)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Pterodactyl_Warriors",p2,new Point(4,5)), p2.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Sandworm",p2,new Point(4,5)), p2.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(CombatPhase.ATTACKER_ONE_RETREAT, currentState.getCurrentCombatPhase());
		assertEquals(5,currentState.getCombatHex().getFightingThingsInHex().size());

		//no reteating!
		getCombatCommandHandler().endPlayerTurn(p2.getID());
		assertEquals(CombatPhase.DEFENDER_RETREAT, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		
		assertEquals(CombatPhase.MAGIC_ATTACK, currentState.getCurrentCombatPhase());
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Old_Dragon",p1,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p1.getID(),3));
		getCombatCommandHandler().rollDice(new Roll(1,getPlayerBoardThingByName("Druid",p2,new Point(4,5)),RollReason.ATTACK_WITH_CREATURE, p2.getID(), 3));
		allDoneRolling();
		assertEquals(CombatPhase.APPLY_MAGIC_HITS, currentState.getCurrentCombatPhase());

		assertEquals(1,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(1,currentState.getHitsOnPlayer(p2.getID()));

		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Old_Dragon",p1,new Point(4,5)), p1.getID(), 1);
		getCombatCommandHandler().applyHits(getPlayerBoardThingByName("Green_Knight",p2,new Point(4,5)), p2.getID(), 1);
		assertEquals(0,currentState.getHitsOnPlayer(p1.getID()));
		assertEquals(0,currentState.getHitsOnPlayer(p2.getID()));
		assertEquals(3,currentState.getCombatHex().getFightingThingsInHex().size());
		
		assertEquals(CombatPhase.DETERMINE_DAMAGE, currentState.getCurrentCombatPhase());
		
		assertEquals(5, p2.getOwnedHexes().size());
		assertEquals(3, p1.getOwnedHexes().size());
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 5).getHex()));
		assertEquals(3,currentState.getBoard().getHexByXY(4, 5).getCreaturesInHex().size());
		assertEquals(0,currentState.getBoard().getHexByXY(4, 5).getBuilding().getValue());
		for(ITileProperties thing : currentState.getBoard().getHexByXY(4, 5).getThingsInHex())
		{
			assertEquals(true,p2.ownsThingOnBoard((thing)));
			if(thing.isCreature())
			{
				assertEquals(false,thing.isFaceUp());
			}
		}
	}
}
