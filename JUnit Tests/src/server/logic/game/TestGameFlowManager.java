package server.logic.game;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.BuildableBuilding;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.LoadResources;
import common.PlayerInfo;
import common.TileProperties;

public class TestGameFlowManager {
	
	private GameFlowManager game;
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
		LoadResources lr = new LoadResources("..\\kings-and-things\\Kings And Things\\" + Constants.RESOURCE_PATH,false);
		lr.run();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		game = new GameFlowManager();
		p1 = new Player(new PlayerInfo("Erik",0,true));
		p2 = new Player(new PlayerInfo("Archit",1,true));
		p3 = new Player(new PlayerInfo("Shariar",2,true));
		p4 = new Player(new PlayerInfo("Nadra",3,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		game.startNewGame(true, players);
		
		p1 = getPlayerAtIndex(0);
		p2 = getPlayerAtIndex(1);
		p3 = getPlayerAtIndex(2);
		p4 = getPlayerAtIndex(3);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartNewGame() {
		GameState currentState = game.getCurrentState();
		assertEquals(4,currentState.getPlayers().size());
		assertEquals(currentState.getActivePhasePlayer(),currentState.getActiveTurnPlayer());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertEquals(SetupPhase.PICK_FIRST_HEX, currentState.getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_CHARACTERS, currentState.getCurrentRegularPhase());
		assertEquals(Constants.MAX_HEXES_ON_BOARD, currentState.getBoard().getHexesAsList().size());
	}

	@Test
	public void testGiveHexToPlayer() {
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		assertEquals(1,p1.getOwnedHexes().size());
		assertEquals(1,p2.getOwnedHexes().size());
		assertEquals(1,p3.getOwnedHexes().size());
		assertEquals(1,p4.getOwnedHexes().size());
		
		assertEquals(game.getCurrentState().getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex(),p1.getOwnedHexes().iterator().next());
		assertEquals(game.getCurrentState().getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex(),p2.getOwnedHexes().iterator().next());
		assertEquals(game.getCurrentState().getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex(),p3.getOwnedHexes().iterator().next());
		assertEquals(game.getCurrentState().getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex(),p4.getOwnedHexes().iterator().next());
		
		assertEquals(SetupPhase.EXCHANGE_SEA_HEXES,game.getCurrentState().getCurrentSetupPhase());
	}

	@Test
	public void testGiveSecondHexToPlayer() {
		testGiveHexToPlayer();
		//skip exchange sea hex phase
		game.endPlayerTurn(p1.getID());
		game.endPlayerTurn(p2.getID());
		game.endPlayerTurn(p3.getID());
		game.endPlayerTurn(p4.getID());
		
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(4, 3).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(5, 8).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(1, 8).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(3);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(2, 3).getHex(), p4.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		assertEquals(2,p1.getOwnedHexes().size());
		assertEquals(2,p2.getOwnedHexes().size());
		assertEquals(2,p3.getOwnedHexes().size());
		assertEquals(2,p4.getOwnedHexes().size());
		
		assertEquals(true,p1.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex()));

		assertEquals(true,p1.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(4, 3).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(5, 8).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(1, 8).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(2, 3).getHex()));
		
		assertEquals(SetupPhase.PICK_THIRD_HEX,game.getCurrentState().getCurrentSetupPhase());
	}

	@Test
	public void testGiveThirdHexToPlayer() {
		testGiveSecondHexToPlayer();
		
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(4, 5).getHex(), p1.getID());
		assertPlayerAtIndexIsActiveForPhase(1);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(6, 7).getHex(), p2.getID());
		assertPlayerAtIndexIsActiveForPhase(2);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(2, 9).getHex(), p3.getID());
		assertPlayerAtIndexIsActiveForPhase(3);
		assertPlayerAtIndexIsActiveForTurn(0);
		game.giveHexToPlayer(game.getCurrentState().getBoard().getHexByXY(1, 4).getHex(), p4.getID());
		assertPlayerAtIndexIsActiveForPhase(0);
		assertPlayerAtIndexIsActiveForTurn(0);
		
		validateThirdHexGivenState();
		
		assertEquals(SetupPhase.PLACE_FREE_TOWER, game.getCurrentState().getCurrentSetupPhase());
	}

	@Test
	public void testConstructBuilding() {
		testGiveThirdHexToPlayer();
		assertEquals(10,p1.getGold());
		assertEquals(10,p2.getGold());
		assertEquals(10,p3.getGold());
		assertEquals(10,p4.getGold());
		
		game.constructBuilding(BuildableBuilding.Tower, p1.getID(),game.getCurrentState().getBoard().getHexByXY(4, 5).getHex());
		
		assertEquals(true,game.getCurrentState().getBoard().getHexByXY(4, 5).hasBuilding());
		assertEquals(1,p1.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p1.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p1.ownsThingOnBoard(game.getCurrentState().getBoard().getHexByXY(4, 5).getBuilding()));
		
		game.constructBuilding(BuildableBuilding.Tower, p2.getID(),game.getCurrentState().getBoard().getHexByXY(5, 8).getHex());
		
		assertEquals(true,game.getCurrentState().getBoard().getHexByXY(5, 8).hasBuilding());
		assertEquals(1,p2.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p2.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p2.ownsThingOnBoard(game.getCurrentState().getBoard().getHexByXY(5, 8).getBuilding()));
		
		game.constructBuilding(BuildableBuilding.Tower, p3.getID(),game.getCurrentState().getBoard().getHexByXY(2, 9).getHex());
		
		assertEquals(true,game.getCurrentState().getBoard().getHexByXY(2, 9).hasBuilding());
		assertEquals(1,p3.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p3.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p3.ownsThingOnBoard(game.getCurrentState().getBoard().getHexByXY(2, 9).getBuilding()));
		
		game.constructBuilding(BuildableBuilding.Tower, p4.getID(),game.getCurrentState().getBoard().getHexByXY(2, 3).getHex());
		
		assertEquals(true,game.getCurrentState().getBoard().getHexByXY(2, 3).hasBuilding());
		assertEquals(1,p4.getOwnedThingsOnBoard().size());
		assertEquals(BuildableBuilding.Tower.name(),p4.getOwnedThingsOnBoard().iterator().next().getName());
		assertEquals(true,p4.ownsThingOnBoard(game.getCurrentState().getBoard().getHexByXY(2, 3).getBuilding()));

		validateThirdHexGivenState();
		
		assertEquals(SetupPhase.PLACE_FREE_THINGS,game.getCurrentState().getCurrentSetupPhase());
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
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), game.getCurrentState().getBoard().getHexByXY(4, 5).getHex());
		}

		String[] stackTwoNames = {"Skeletons","Watusi","Goblins","Ogre"};
		for(String s : stackTwoNames)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), game.getCurrentState().getBoard().getHexByXY(4, 3).getHex());
		}
		
		assertEquals(0,p1.getTrayThings().size());
		assertEquals(11,p1.getOwnedThingsOnBoard().size());
		game.endPlayerTurn(p1.getID());
		
		/**********************/

		String[] stackOneNames2 = {"Pterodactyl_Warriors","Sandworm","Green_Knight","Dervish","Crocodiles","Nomads","Druid","Walking_Tree","Crawling_Vines","Bandits"};
		for(String s : stackOneNames2)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p2), p2.getID(), game.getCurrentState().getBoard().getHexByXY(5, 8).getHex());
		}

		assertEquals(0,p2.getTrayThings().size());
		assertEquals(11,p2.getOwnedThingsOnBoard().size());
		game.endPlayerTurn(p2.getID());
		
		/*********************/

		String[] stackOneNames3 = {"Centaur","Camel_Corps","Farmers"};
		for(String s : stackOneNames3)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), game.getCurrentState().getBoard().getHexByXY(2, 9).getHex());
		}
		game.placeThingOnBoard(getPlayerTrayThingByName("Farmers",p3), p3.getID(), game.getCurrentState().getBoard().getHexByXY(2, 9).getHex());

		String[] stackTwoNames3 = {"Genie","Skeletons","Pygmies"};
		for(String s : stackTwoNames3)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), game.getCurrentState().getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex());
		}

		String[] stackThreeNames3 = {"Greathunter","Nomads","Witch_Doctor"};
		for(String s : stackThreeNames3)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p3), p3.getID(), game.getCurrentState().getBoard().getHexByXY(1, 8).getHex());
		}
		
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(11,p3.getOwnedThingsOnBoard().size());
		game.endPlayerTurn(p3.getID());
		
		/*********************/

		String[] stackOneNames4 = {"Tribesmen","Giant_Lizard","Villains","Tigers"};
		for(String s : stackOneNames4)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), game.getCurrentState().getBoard().getHexByXY(2, 3).getHex());
		}

		String[] stackTwoNames4 = {"Vampire_Bat","Tribesmen","Dark_Wizard","Black_Knight"};
		for(String s : stackTwoNames4)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), game.getCurrentState().getBoard().getHexByXY(1, 4).getHex());
		}

		String[] stackThreeNames4 = {"Giant_Ape","Buffalo_Herd"};
		for(String s : stackThreeNames4)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p4), p4.getID(), game.getCurrentState().getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex());
		}
		
		assertEquals(0,p4.getTrayThings().size());
		assertEquals(11,p4.getOwnedThingsOnBoard().size());
		game.endPlayerTurn(p4.getID());

		assertEquals(SetupPhase.EXCHANGE_THINGS,game.getCurrentState().getCurrentSetupPhase());
	}

	@Test
	public void testAdvanceToRegularPlay() {
		testPlaceThingOnBoard();
		
		//skip exchange things phase
		game.endPlayerTurn(p1.getID());
		game.endPlayerTurn(p2.getID());
		game.endPlayerTurn(p3.getID());
		game.endPlayerTurn(p4.getID());

		//skip place exchanged things phase
		game.endPlayerTurn(p1.getID());
		game.endPlayerTurn(p2.getID());
		game.endPlayerTurn(p3.getID());
		game.endPlayerTurn(p4.getID());
		
		assertEquals(14,p1.getGold());
		assertEquals(14,p2.getGold());
		assertEquals(14,p3.getGold());
		assertEquals(14,p4.getGold());

		assertEquals(0,p1.getTrayThings().size());
		assertEquals(0,p2.getTrayThings().size());
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(0,p4.getTrayThings().size());
		
		assertEquals(SetupPhase.SETUP_FINISHED, game.getCurrentState().getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_CHARACTERS,game.getCurrentState().getCurrentRegularPhase());
		assertEquals(p1.getID(),game.getCurrentState().getActivePhasePlayer().getID());
		assertEquals(p1.getID(),game.getCurrentState().getActiveTurnPlayer().getID());
	}

	@Test
	public void testRecruitingThings() throws NoMoreTilesException {
		testAdvanceToRegularPlay();
		
		//skip recruiting characters phase
		game.endPlayerTurn(p1.getID());
		game.endPlayerTurn(p2.getID());
		game.endPlayerTurn(p3.getID());
		game.endPlayerTurn(p4.getID());

		assertEquals(0,p1.getTrayThings().size());
		assertEquals(0,p2.getTrayThings().size());
		assertEquals(0,p3.getTrayThings().size());
		assertEquals(0,p4.getTrayThings().size());
		
		assertEquals(SetupPhase.SETUP_FINISHED, game.getCurrentState().getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_THINGS,game.getCurrentState().getCurrentRegularPhase());
		assertEquals(p1.getID(),game.getCurrentState().getActivePhasePlayer().getID());
		assertEquals(p1.getID(),game.getCurrentState().getActiveTurnPlayer().getID());
		
		game.recruitThings(5, new ArrayList<TileProperties>(), p1.getID());
		assertEquals(3,p1.getTrayThings().size());

		String[] stackTwoNames = {"Cyclops","Mountain_Men","Goblins"};
		for(String s : stackTwoNames)
		{
			game.placeThingOnBoard(getPlayerTrayThingByName(s,p1), p1.getID(), game.getCurrentState().getBoard().getHexByXY(4, 3).getHex());
		}
		assertEquals(0,p1.getTrayThings().size());
		assertEquals(14,p1.getOwnedThingsOnBoard().size());
		
		game.endPlayerTurn(p1.getID());
		
		game.recruitThings(0, new ArrayList<TileProperties>(), p2.getID());
		assertEquals(2,p2.getTrayThings().size());
		game.endPlayerTurn(p2.getID());

		game.recruitThings(0, new ArrayList<TileProperties>(), p3.getID());
		assertEquals(2,p3.getTrayThings().size());
		game.endPlayerTurn(p3.getID());

		game.recruitThings(0, new ArrayList<TileProperties>(), p4.getID());
		assertEquals(2,p4.getTrayThings().size());
		game.endPlayerTurn(p4.getID());
	}

	private void assertPlayerAtIndexIsActiveForPhase(int index)
	{
		assertEquals(game.getCurrentState().getActivePhasePlayer().getID(),game.getCurrentState().getPlayerOrder().get(index).intValue());
	}

	private void assertPlayerAtIndexIsActiveForTurn(int index)
	{
		assertEquals(game.getCurrentState().getActiveTurnPlayer().getID(),game.getCurrentState().getPlayerOrder().get(index).intValue());
	}
	
	private Player getPlayerAtIndex(int index)
	{
		return game.getCurrentState().getPlayerByPlayerNumber(game.getCurrentState().getPlayerOrder().get(index));
	}
	
	private void validateThirdHexGivenState()
	{
		assertEquals(3,p1.getOwnedHexes().size());
		assertEquals(3,p2.getOwnedHexes().size());
		assertEquals(3,p3.getOwnedHexes().size());
		assertEquals(3,p4.getOwnedHexes().size());

		assertEquals(true,p1.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(SECOND_SPOT.x, SECOND_SPOT.y).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(FOURTH_SPOT.x, FOURTH_SPOT.y).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(THIRD_SPOT.x, THIRD_SPOT.y).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(FIRST_SPOT.x, FIRST_SPOT.y).getHex()));

		assertEquals(true,p1.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(4, 3).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(5, 8).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(1, 8).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(2, 3).getHex()));
		
		assertEquals(true,p1.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(4, 5).getHex()));
		assertEquals(true,p2.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(6, 7).getHex()));
		assertEquals(true,p3.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(2, 9).getHex()));
		assertEquals(true,p4.getOwnedHexes().contains(game.getCurrentState().getBoard().getHexByXY(1, 4).getHex()));
		
	}
	
	private TileProperties getPlayerTrayThingByName(String name, Player p)
	{
		for(TileProperties tp : p.getTrayThings())
		{
			if(tp.getName().equals(name))
			{
				return tp;
			}
		}
		
		return null;
	}
}
