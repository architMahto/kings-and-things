package server.logic.game;

import static org.junit.Assert.*;

import java.awt.Point;
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
import common.PlayerInfo;

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
		
		assertEquals(SetupPhase.PLACE_FREE_TOWER, game.getCurrentState().getCurrentSetupPhase());
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
}
