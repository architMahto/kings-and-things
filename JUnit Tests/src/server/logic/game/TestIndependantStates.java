package server.logic.game;

import static org.junit.Assert.assertEquals;

import java.awt.Point;

import org.junit.Before;
import org.junit.Test;

import server.logic.exceptions.NoMoreTilesException;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.ITileProperties;

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
	public void testFourPlayerCombat()
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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Dervish",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Druid",p2,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),4);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Witch_Doctor",p3,new Point(3, 6)),1);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Dark_Wizard",p4,new Point(3, 6)),4);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Giant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Dwarves",p1,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Pterodactyl_Warriors",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),4);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Watusi",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Ogre",p1,new Point(3, 6)),3);
		
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Sandworm",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Green_Knight",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Crocodiles",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Nomads",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Walking_Tree",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Bandits",p2,new Point(3, 6)),6);
		
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Centaur",p3,new Point(3, 6)),4);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),1);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Pygmies",p3,new Point(3, 6)),1);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Nomads",p3,new Point(3, 6)),1);
		
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Tribesmen",p4,new Point(3, 6),1),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Tigers",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Black_Knight",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Black_Knight",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Buffalo_Herd",p4,new Point(3, 6)),2);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Vampire_Bat",p4,new Point(3, 6)),8);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Old_Dragon",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),4);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Giant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Greathunter",p3,new Point(3, 6)),4);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Elephant",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p1.getID(), getPlayerBoardThingByName("Brown_Knight",p1,new Point(3, 6)),3);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p2.getID(), getPlayerBoardThingByName("Crawling_Vines",p2,new Point(3, 6)),8);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Camel_Corps",p3,new Point(3, 6)),1);
		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),2);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),4);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p4.getID(), getPlayerBoardThingByName("Giant_Ape",p4,new Point(3, 6)),2);

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

		getCombatCommandHandler().rollDice(RollReason.ATTACK_WITH_CREATURE, p3.getID(), getPlayerBoardThingByName("Genie",p3,new Point(3, 6)),4);

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

		assertEquals(1, p1.getOwnedHexes().size());
		assertEquals(1, p2.getOwnedHexes().size());
		assertEquals(2, p3.getOwnedHexes().size());
		assertEquals(1, p4.getOwnedHexes().size());
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
	
	private void addTenThingsToHexForPlayer(Player p) throws NoMoreTilesException
	{
		for(int i=0; i<10; i++)
		{
			ITileProperties thing = currentState.getCup().drawTile();
			p.addOwnedThingOnBoard(thing);
			currentState.getBoard().getHexByXY(3, 6).addThingToHex(thing);
		}
	}
}
