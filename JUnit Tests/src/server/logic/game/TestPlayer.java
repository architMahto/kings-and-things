package server.logic.game;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import common.game.PlayerInfo;

public class TestPlayer {
	private Player p;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		p = new Player(new PlayerInfo("some name",1,true));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPlayer() {
		new Player(new PlayerInfo(null,2,true));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPlayer3() {
		new Player(new PlayerInfo("",2,true));
	}

	@Test
	public void testPlayer2() {
		new Player(new PlayerInfo("sjkdfh",-2,true));
		new Player(new PlayerInfo("dfghdfg",0,true));
	}
	
	@Test
	public void testAddGold() {
		p.addGold(5);
		assertEquals(5,p.getGold());
		p.addGold(2);
		assertEquals(7,p.getGold());
		p.addGold(0);
		assertEquals(7,p.getGold());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddGold2() {
		p.addGold(6);
		p.addGold(-1);
	}

	@Test
	public void testRemoveGold() {
		p.addGold(10);
		assertEquals(10,p.getGold());
		p.removeGold(3);
		assertEquals(7,p.getGold());
		p.removeGold(0);
		assertEquals(7,p.getGold());
		p.removeGold(4);
		assertEquals(3,p.getGold());
		p.removeGold(3);
		assertEquals(0,p.getGold());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRemoveGold2() {
		p.addGold(10);
		p.removeGold(-2);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRemoveGold3() {
		p.addGold(10);
		p.removeGold(11);
	}
}
