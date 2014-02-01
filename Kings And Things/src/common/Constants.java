package common;

import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Color;
import java.awt.Image;
import java.awt.Dimension;

import javax.imageio.ImageIO;

public final class Constants {
	
	public enum Level { Error, Warning, Notice, Plain, END}
	public enum Category { Building, Cup, Extra, Gold, Hex, Special, State}
	public enum Ability { Charge, Fly, Range, Special, Magic, Armor, Neutralised}
	public enum Restriction { Gold, Magic, Income, Building, Event, Special, State}
	public enum Biome { Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp}
	
	//Resources
	public static final Image HEX_REVERSE; 
	public static final Image TILE_REVERSE; 
	public static final HashMap< Integer, TileProperties> cup = new HashMap<>();
	public static final HashMap< Integer, TileProperties> bank = new HashMap<>();
	
	//Colors
	public static final Color RED = new Color( 255, 0, 0);
	public static final Color DARK_RED = new Color( 128, 0, 0);
	public static final Color GREEN = new Color( 0, 255, 0);
	public static final Color DARK_GREEN = new Color( 0, 100, 0);
	public static final Color BLUE = new Color( 0, 200, 255);
	public static final Color DARK_BLUE = new Color( 0, 75, 128);
	public static final Color YELLOW = new Color( 255, 255, 0);
	public static final Color DARK_YELLOW = new Color( 128, 128, 0);
	public static final Color COLOR_ERROR = RED;
	public static final Color COLOR_PLAIN = Color.BLACK;
	public static final Color COLOR_NOTICE = DARK_GREEN;
	public static final Color COLOR_WARNNING = DARK_YELLOW;
	
	//keys for events
	public static final int HOLD = 0;         //00000000
	public static final int WAIT = 1;         //00000001
	public static final int UPDATE = 2;       //00000010
	public static final int ANALYSE = 3;      //00000011
	public static final int CONSOLE = 4;      //00000100
	public static final int ENDGAME = 5;      //00000101
	public static final int PLAYER = 128;     //10000000
	public static final int PLAYER_INC = 16;  //00010000
	
	//Maximums
	public static final int MAX_HEXES = 48;
	public static final int MAX_PLAYERS = 4;
	public static final int MAX_HEXES_ON_BOARD = 37;
	
	//Minimums
	public static final int MIN_PLAYERS = 2;
	public static final int MIN_HEXES_ON_BOARD = 19;
	public static final Dimension MIN_CLIENT_SIZE = new Dimension( 1300,720);
	
	//Sizes
	public static final int LOCK_SIZE = 28;
	public static final int HEX_HEIGHT = 70;
	public static final int HEX_SPACING = 16;
	public static final int LABEL_FONT_SIZE = 25;
	public static final int IP_COLUMN_COUNT = 12;
	public static final int PLAYER_FONT_SIZE = 12;
	public static final int PORT_COLUMN_COUNT = 7;
	public static final int BOARD_TOP_PADDING = 100;
	public static final int BOARD_WIDTH_SEGMENT = 8;
	public static final int BOARD_PLAYERS_STATE = 450;
	public static final int BOARD_HEIGHT_SEGMENT = 14;
	public static final double HEX_RATIO = 752.0/658.0;
	public static final int BOARD_BOTTOM_PADDING = 120;
	public static final Dimension LABEL_SIZE = new Dimension( 50,50);
	public static final Dimension CONSOLE_SIZE = new Dimension( 100,40);
	public static final Dimension LOADING_SIZE = new Dimension( 350,425);
	public static final Dimension PROGRESS_SIZE = new Dimension( LOADING_SIZE.width,170);
	public static final Dimension HEX_SIZE = new Dimension( (int)(HEX_HEIGHT*HEX_RATIO),HEX_HEIGHT);
	public static final Dimension HEX_BOARD_SIZE = new Dimension( HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING, HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING);
	public static final Dimension BOARD_SIZE = new Dimension( HEX_BOARD_SIZE.width + BOARD_PLAYERS_STATE, HEX_BOARD_SIZE.height + BOARD_BOTTOM_PADDING);
	
	//Defaults
	public static final Path RESOURCE_PATH = Paths.get( "Resources/");
	public static final int SPIRAL_DELAY = 5;
	public static final int SERVER_TIMEOUT = 10;
	public static final int SERVER_PORT = 12345;
	public static final String SERVER_IP = "127.0.0.1";
	public static final int HEX_MOVE_DISTANCE = (int) (HEX_HEIGHT*0.5);
	public static final int BOARD_LOAD_ROW[][] = { { 7, 5, 6, 8, 9, 8, 6},
													{4, 3, 4, 5, 7, 9, 10, 11, 10, 9, 7, 5},
													{3, 2, 1, 2, 3, 4, 6, 8, 10, 11, 12, 13, 12, 11, 10, 8, 6, 4}};
	public static final int BOARD_LOAD_COL[][] = { { 4, 4, 5, 5, 4, 3, 3},
													{3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 2, 2},
													{2, 3, 4, 5, 6, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1}};
	
	static{
		HEX_REVERSE = loadImage( "Resources\\-n HexReverse.png");
		TILE_REVERSE = loadImage( "Resources\\-n TileReverse.png");
	}
	
	private static Image loadImage( String path){
		try {
			return ImageIO.read( new File( path));
		} catch ( IOException e) {
			return null;
		}
	}
	
	//to prevent instances
	private Constants(){}
}