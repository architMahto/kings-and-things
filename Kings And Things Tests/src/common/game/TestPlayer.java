package common.game;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
		p = new Player("some name",1);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPlayer() {
		new Player(null,2);
	}

	@Test
	public void testPlayer2() {
		new Player("",-2);
		new Player("",0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetGold() {
		p.setGold(-1);
	}

	@Test
	public void testSetGold2() {
		p.setGold(0);
		assertEquals(0, p.getGold());
		p.setGold(7);
		assertEquals(7,p.getGold());
		p.setGold(3);
		assertEquals(3,p.getGold());
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
