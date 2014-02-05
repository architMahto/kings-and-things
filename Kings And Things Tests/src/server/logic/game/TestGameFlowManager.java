package server.logic.game;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import common.Constants;
import common.LoadResources;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.game.GameState;
import common.game.Player;

public class TestGameFlowManager {
	
	private GameFlowManager game;
	private Player p1;
	private Player p2;
	private Player p3;
	private Player p4;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LoadResources lr = new LoadResources();
		lr.run();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		game = new GameFlowManager();
		p1 = new Player("Erik",0);
		p2 = new Player("Archit",1);
		p3 = new Player("Shariar",2);
		p4 = new Player("Nadra",3);
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		game.startNewGame(false, players);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartNewGame() {
		GameState currentState = game.getCurrentState();
		assertEquals(4,currentState.getPlayers().size());
		assertEquals(currentState.getActivePhasePlayer(),currentState.getActiveTurnPlayer());
		assertEquals(currentState.getActivePhasePlayer().getPlayerNumber(),currentState.getPlayerOrder().get(0).intValue());
		assertEquals(SetupPhase.PICK_FIRST_HEX, currentState.getCurrentSetupPhase());
		assertEquals(RegularPhase.RECRUITING_CHARACTERS, currentState.getCurrentRegularPhase());
		assertEquals(Constants.MAX_HEXES_ON_BOARD, currentState.getBoard().getHexesAsList().size());
	}

	@Test
	public void testGiveHexToPlayer() {
		fail("Not yet implemented");
	}

	@Test
	public void testConstructBuilding() {
		fail("Not yet implemented");
	}

	@Test
	public void testPlaceThingOnBoard() {
		fail("Not yet implemented");
	}

	@Test
	public void testExchangeThings() {
		fail("Not yet implemented");
	}

	@Test
	public void testExchangeSeaHex() {
		fail("Not yet implemented");
	}

	@Test
	public void testEndPlayerTurn() {
		fail("Not yet implemented");
	}

	@Test
	public void testPaidRecruits() {
		fail("Not yet implemented");
	}

}
