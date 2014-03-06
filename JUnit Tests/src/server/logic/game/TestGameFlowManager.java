package server.logic.game;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.HashSet;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.log4j.PropertyConfigurator;

import client.gui.CombatPanel;
import server.event.commands.SetupPhaseComplete;
import server.event.commands.StartSetupPhase;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.handlers.CommandHandler;
import server.logic.game.handlers.CombatCommandHandler;
import server.logic.game.handlers.MovementCommandHandler;
import server.logic.game.handlers.SetupPhaseCommandHandler;
import server.logic.game.handlers.RecruitingThingsCommandHandler;
import server.logic.game.handlers.ConstructBuildingCommandHandler;
import common.Constants;
import common.event.AbstractUpdateReceiver;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.LoadResources;
import common.game.ITileProperties;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Constants.RegularPhase;
import common.Constants.BuildableBuilding;

public class TestGameFlowManager {
	
	private static final int EVENT_DISPATCH_WAITING_TIME = 500;
	
	private CommandHandlerManager game;
	private GameState currentState;
	private StartGameReceiver receiver;
	private Player p1;
	private Player p2;
	private Player p3;
	private Player p4;

	private static final Point FIRST_SPOT = new Point(1,2);
	private static final Point SECOND_SPOT = new Point(5,2);
	private static final Point THIRD_SPOT = new Point(1,10);
	private static final Point FOURTH_SPOT = new Point(5,10);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LoadResources lr = new LoadResources("..\\Kings And Things\\" + Constants.RESOURCE_PATH,false);
		lr.run();
		PropertyConfigurator.configure("Log Settings\\serverLog4j.properties");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		game = new CommandHandlerManager();
		game.initialize();
		receiver = new StartGameReceiver();
		
		p1 = new Player(new PlayerInfo("Erik",0,true));
		p2 = new Player(new PlayerInfo("Archit",1,true));
		p3 = new Player(new PlayerInfo("Shariar",2,true));
		p4 = new Player(new PlayerInfo("Nadra",3,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		new StartSetupPhase(true, players, this).postInternalEvent();
		Thread.sleep(EVENT_DISPATCH_WAITING_TIME);
		
		p1 = getPlayerAtIndex(0);
		p2 = getPlayerAtIndex(1);
		p3 = getPlayerAtIndex(2);
		p4 = getPlayerAtIndex(3);
	}

	@After
	public void tearDown() throws Exception {
		game.dispose();
		receiver.unregisterFromEventBus();
	}

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
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p2.getID(),currentState.getBoard().getHexByXY(5, 8).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(5, 8).hasBuilding());
		assertEquals(1,p2.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p2.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p2.ownsThingOnBoard(currentState.getBoard().getHexByXY(5, 8).getBuilding()));
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p3.getID(),currentState.getBoard().getHexByXY(2, 9).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(2, 9).hasBuilding());
		assertEquals(1,p3.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p3.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p3.ownsThingOnBoard(currentState.getBoard().getHexByXY(2, 9).getBuilding()));
		
		getConstructBuildingCommandHandler().constructBuilding(BuildableBuilding.Tower, p4.getID(),currentState.getBoard().getHexByXY(2, 3).getHex());
		
		assertEquals(true,currentState.getBoard().getHexByXY(2, 3).hasBuilding());
		assertEquals(1,p4.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p4.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p4.ownsThingOnBoard(currentState.getBoard().getHexByXY(2, 3).getBuilding()));

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
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 5).getHex());
		}

		String[] stackTwoNames = {"Skeletons","Watusi","Goblins","Ogre"};
		for(String s : stackTwoNames)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 3).getHex());
		}
		
		assertEquals(0,p1.getTrayThings().size());
		assertEquals(11,p1.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p1.getID());
		
		/**********************/

		String[] stackOneNames2 = {"Pterodactyl_Warriors","Sandworm","Green_Knight","Dervish","Crocodiles","Nomads","Druid","Walking_Tree","Crawling_Vines","Bandits"};
		for(String s : stackOneNames2)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p2), p2.getID(), currentState.getBoard().getHexByXY(5, 8).getHex());
		}

		assertEquals(0,p2.getTrayThings().size());
		assertEquals(11,p2.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p2.getID());
		
		/*********************/

		String[] stackOneNames3 = {"Centaur","Camel_Corps","Farmers"};
		for(String s : stackOneNames3)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(2, 9).getHex());
		}
		getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName("Farmers",p3), p3.getID(), currentState.getBoard().getHexByXY(2, 9).getHex());

		String[] stackTwoNames3 = {"Genie","Skeletons","Pygmies"};
		for(String s : stackTwoNames3)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex());
		}

		String[] stackThreeNames3 = {"Greathunter","Nomads","Witch_Doctor"};
		for(String s : stackThreeNames3)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), currentState.getBoard().getHexByXY(1, 8).getHex());
		}
		
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(11,p3.getOwnedThingsOnBoard().size());
		getRecruitingThingsCommandHandler().endPlayerTurn(p3.getID());
		
		/*********************/

		String[] stackOneNames4 = {"Tribesmen","Giant_Lizard","Villains","Tigers"};
		for(String s : stackOneNames4)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(2, 3).getHex());
		}

		String[] stackTwoNames4 = {"Vampire_Bat","Tribesmen","Dark_Wizard","Black_Knight"};
		for(String s : stackTwoNames4)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(1, 4).getHex());
		}

		String[] stackThreeNames4 = {"Giant_Ape","Buffalo_Herd"};
		for(String s : stackThreeNames4)
		{
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), currentState.getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex());
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
			getRecruitingThingsCommandHandler().placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), currentState.getBoard().getHexByXY(4, 3).getHex());
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
			assertEquals(Constants.MAX_MOVE_SPEED - 1,tp.getMoveSpeed());
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
		getCombatCommandHandler().rollDice(RollReason.EXPLORE_HEX, p1.getID(), currentState.getBoard().getHexByXY(3, 2).getHex());
		assertEquals(4, p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 2).getHex()));
		assertEquals(7,currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex().size());
		
		getCombatCommandHandler().endPlayerTurn(p1.getID());
		

		getCombatCommandHandler().resolveCombat(currentState.getBoard().getHexByXY(4, 7).getHex(), p2.getID());
		getCombatCommandHandler().rollDice(RollReason.EXPLORE_HEX, p2.getID(), currentState.getBoard().getHexByXY(4, 7).getHex());
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
			assertEquals(Constants.MAX_MOVE_SPEED - 2,tp.getMoveSpeed());
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
	public void testTurnTwoCombat()
	{
		fail("not yet implemented");
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
		
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run()
			{
				JFrame p2Frame = new JFrame("Player 2");
				CombatPanel cp2 = new CombatPanel(currentState.getBoard().getHexByXY(4, 5),p2);
				p2Frame.getContentPane().add(cp2);
				p2Frame.setVisible(true);

				JFrame p1Frame = new JFrame("Player 1");
				CombatPanel cp1 = new CombatPanel(currentState.getBoard().getHexByXY(4, 5),p1);
				p1Frame.getContentPane().add(cp1);
				p1Frame.setVisible(true);
			}});
		
		while(true)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Dervish",p2,new Point(4,5)));
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Old_Dragon",p1,new Point(4,5)));
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Druid",p2,new Point(4,5)));
		
		while(currentState.getHitsOnPlayer(p1.getID()) > 0)
		{
			getCombatCommandHandler().applyHits(currentState.getCombatHex().getThingsInHexOwnedByPlayer(p1).iterator().next(), p1.getID(), 1);
		}

		while(currentState.getHitsOnPlayer(p2.getID()) > 0)
		{
			getCombatCommandHandler().applyHits(currentState.getCombatHex().getThingsInHexOwnedByPlayer(p2).iterator().next(), p2.getID(), 1);
		}

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Dervish",p2,new Point(4,5)));
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Old_Dragon",p1,new Point(4,5)));
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Druid",p2,new Point(4,5)));
		*/
		/*
		assertEquals(4, p1.getOwnedHexes().size());
		assertEquals(true,p1.getOwnedHexes().contains(currentState.getBoard().getHexByXY(3, 2).getHex()));
		assertEquals(7,currentState.getBoard().getHexByXY(3, 2).getCreaturesInHex().size());
			
		game.endPlayerTurn(p1.getID());
			

		game.resolveCombat(currentState.getBoard().getHexByXY(4, 7).getHex(), p2.getID());
		game.rollDice(RollReason.EXPLORE_HEX, p2.getID(), currentState.getBoard().getHexByXY(4, 7).getHex());
		assertEquals(4, p2.getOwnedHexes().size());
		assertEquals(true,p2.getOwnedHexes().contains(currentState.getBoard().getHexByXY(4, 7).getHex()));
		assertEquals(10,currentState.getBoard().getHexByXY(4, 7).getCreaturesInHex().size());

		game.endPlayerTurn(p2.getID());
			
		//skip other player combat
		game.endPlayerTurn(p3.getID());
		game.endPlayerTurn(p4.getID());*/
	}
	
	private void assertPlayerAtIndexIsActiveForPhase(int index)
	{
		assertEquals(currentState.getPlayerOrder().get(index).intValue(),currentState.getActivePhasePlayer().getID());
	}

	private void assertPlayerAtIndexIsActiveForTurn(int index)
	{
		assertEquals(currentState.getPlayerOrder().get(index).intValue(),currentState.getActiveTurnPlayer().getID());
	}
	
	private Player getPlayerAtIndex(int index)
	{
		return currentState.getPlayerByPlayerNumber(currentState.getPlayerOrder().get(index));
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
	
	private ITileProperties getPlayerTrayThingByName(String name, Player p)
	{
		for(ITileProperties tp : p.getTrayThings())
		{
			if(tp.getName().equals(name))
			{
				return tp;
			}
		}
		
		return null;
	}
	
	private ITileProperties getPlayerBoardThingByName(String name, Player p, Point location)
	{
		for(ITileProperties tp : currentState.getBoard().getHexByXY(location.x, location.y).getThingsInHexOwnedByPlayer(p))
		{
			if(tp.getName().equals(name))
			{
				return tp;
			}
		}
		
		return null;
	}
	
	private CommandHandler getHandlerByClass(Class<? extends CommandHandler> clazz)
	{
		for(CommandHandler h : game.getCommandHandlers())
		{
			if(h.getClass().equals(clazz))
			{
				return h;
			}
		}
		return null;
	}
	
	private CombatCommandHandler getCombatCommandHandler()
	{
		return (CombatCommandHandler) getHandlerByClass(CombatCommandHandler.class);
	}
	
	private ConstructBuildingCommandHandler getConstructBuildingCommandHandler()
	{
		return (ConstructBuildingCommandHandler) getHandlerByClass(ConstructBuildingCommandHandler.class);
	}

	
	private MovementCommandHandler getMovementCommandHandler()
	{
		return (MovementCommandHandler) getHandlerByClass(MovementCommandHandler.class);
	}

	
	private RecruitingThingsCommandHandler getRecruitingThingsCommandHandler()
	{
		return (RecruitingThingsCommandHandler) getHandlerByClass(RecruitingThingsCommandHandler.class);
	}

	
	private SetupPhaseCommandHandler getSetupPhaseCommandHandler()
	{
		return (SetupPhaseCommandHandler) getHandlerByClass(SetupPhaseCommandHandler.class);
	}
	
	private class StartGameReceiver extends AbstractUpdateReceiver<SetupPhaseComplete>{

		protected StartGameReceiver() {
			super( INTERNAL, -1, TestGameFlowManager.this);
		}

		@Override
		public void handle( SetupPhaseComplete update) {
			currentState = update.getCurrentState();
		}
	}
}
