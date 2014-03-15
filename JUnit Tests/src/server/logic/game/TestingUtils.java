package server.logic.game;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.HashSet;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import server.event.commands.SetupPhaseComplete;
import server.event.commands.StartSetupPhase;
import server.logic.game.handlers.CombatCommandHandler;
import server.logic.game.handlers.CommandHandler;
import server.logic.game.handlers.ConstructBuildingCommandHandler;
import server.logic.game.handlers.MovementCommandHandler;
import server.logic.game.handlers.RecruitSpecialCharacterCommandHandler;
import server.logic.game.handlers.RecruitingThingsCommandHandler;
import server.logic.game.handlers.SetupPhaseCommandHandler;
import common.Constants;
import common.event.AbstractUpdateReceiver;
import common.game.ITileProperties;
import common.game.LoadResources;
import common.game.PlayerInfo;

public abstract class TestingUtils
{
	private static final int EVENT_DISPATCH_WAITING_TIME = 500;
	
	private CommandHandlerManager game;
	protected GameState currentState;
	private StartGameReceiver receiver;
	protected Player p1;
	protected Player p2;
	protected Player p3;
	protected Player p4;

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

	protected ITileProperties getPlayerTrayThingByName(String name, Player p)
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
	
	protected ITileProperties getPlayerBoardThingByName(String name, Player p, Point location)
	{
		return getPlayerBoardThingByName(name, p, location,0);
	}
	
	protected ITileProperties getPlayerBoardThingByName(String name, Player p, Point location,int skipCount)
	{
		int matches = 0;
		for(ITileProperties tp : currentState.getBoard().getHexByXY(location.x, location.y).getThingsInHexOwnedByPlayer(p))
		{
			if(tp.getName().equals(name))
			{
				if(matches >= skipCount)
				{
					return tp;
				}
				matches++;
			}
		}
		
		return null;
	}
	
	protected CommandHandler getHandlerByClass(Class<? extends CommandHandler> clazz)
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
	
	protected CombatCommandHandler getCombatCommandHandler()
	{
		return (CombatCommandHandler) getHandlerByClass(CombatCommandHandler.class);
	}
	
	protected ConstructBuildingCommandHandler getConstructBuildingCommandHandler()
	{
		return (ConstructBuildingCommandHandler) getHandlerByClass(ConstructBuildingCommandHandler.class);
	}

	
	protected MovementCommandHandler getMovementCommandHandler()
	{
		return (MovementCommandHandler) getHandlerByClass(MovementCommandHandler.class);
	}

	
	protected RecruitingThingsCommandHandler getRecruitingThingsCommandHandler()
	{
		return (RecruitingThingsCommandHandler) getHandlerByClass(RecruitingThingsCommandHandler.class);
	}

	
	protected SetupPhaseCommandHandler getSetupPhaseCommandHandler()
	{
		return (SetupPhaseCommandHandler) getHandlerByClass(SetupPhaseCommandHandler.class);
	}
	
	protected RecruitSpecialCharacterCommandHandler getSpecialCharRecruitHandler()
	{
		return (RecruitSpecialCharacterCommandHandler) getHandlerByClass(RecruitSpecialCharacterCommandHandler.class);
	}

	protected void assertPlayerAtIndexIsActiveForPhase(int index)
	{
		assertEquals(currentState.getPlayerOrder().get(index).intValue(),currentState.getActivePhasePlayer().getID());
	}

	protected void assertPlayerAtIndexIsActiveForTurn(int index)
	{
		assertEquals(currentState.getPlayerOrder().get(index).intValue(),currentState.getActiveTurnPlayer().getID());
	}
	
	protected Player getPlayerAtIndex(int index)
	{
		return currentState.getPlayerByPlayerNumber(currentState.getPlayerOrder().get(index));
	}
	
	private class StartGameReceiver extends AbstractUpdateReceiver<SetupPhaseComplete>{

		protected StartGameReceiver() {
			super( INTERNAL, -1, TestingUtils.this);
		}

		@Override
		public void handlePublic( SetupPhaseComplete update) {
			currentState = update.getCurrentState();
		}
	}
}
