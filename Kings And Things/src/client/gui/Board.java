package client.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import client.gui.die.DiceRoller;
import client.gui.util.LockManager;
import client.gui.util.LockManager.Lock;
import client.gui.util.animation.CanvasParent;
import client.gui.util.animation.FlipAll;
import client.gui.util.animation.MoveAnimation;
import client.gui.util.undo.UndoManager;
import client.gui.util.undo.ThingPlacmentUndo;
import common.game.Roll;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.TileProperties;
import common.game.ITileProperties;
import common.Constants;
import common.Constants.Ability;
import common.Constants.Category;
import common.Constants.UpdateKey;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Constants.RegularPhase;
import common.Constants.UpdateInstruction;
import common.event.UpdatePackage;
import common.event.AbstractUpdateReceiver;
import static common.Constants.HEX_SIZE;
import static common.Constants.DICE_SIZE;
import static common.Constants.TILE_SIZE;
import static common.Constants.BOARD_SIZE;
import static common.Constants.HEX_OUTLINE_IMAGE;
import static common.Constants.TILE_OUTLINE;
import static common.Constants.HEX_BOARD_SIZE;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_RIGHT_PADDING;
import static common.Constants.PLAYERS_STATE_PADDING;

@SuppressWarnings("serial")
public class Board extends JPanel implements CanvasParent{
	
	private static final BufferedImage IMAGE;
	public static final int HEIGHT_SEGMENT = (int) ((HEX_BOARD_SIZE.getHeight())/Constants.BOARD_HEIGHT_SEGMENT);
	public static final int WIDTH_SEGMENT = (int) ((HEX_BOARD_SIZE.getWidth())/Constants.BOARD_WIDTH_SEGMENT);
	//used for placing bank outlines
	public static final int INITIAL_TILE_X_SHIFT = WIDTH_SEGMENT/2;
	public static final int TILE_X_SHIFT = (int) (WIDTH_SEGMENT*1.2);
	public static final int TILE_Y_SHIFT = 13;
	private static final int HEX_Y_SHIFT = 8-3;
	private static final int HEX_X_SHIFT = 8-2;
	public static final int PADDING = 10;
	public static final Font STATUS_INDICATOR_FONT = new Font("default", Font.BOLD, 30);
	
	/**
	 * create a static image with background and all outlines for faster drawing in Game 
	 */
	static{
		//create image for outlines on board
		IMAGE = new BufferedImage( BOARD_SIZE.width, BOARD_SIZE.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = IMAGE.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//draw background on the new image
		g2d.drawImage( Constants.IMAGE_BACKGROUND, 0, 0, BOARD_SIZE.width, BOARD_SIZE.height, null);
		int x=0, y=0;
		//create a thicker stroke
		g2d.setStroke( new BasicStroke( 5));
		g2d.setColor( Color.BLACK);
		//position the outline for hex
		HEX_OUTLINE_IMAGE.translate( HEX_X_SHIFT, HEX_Y_SHIFT);
		g2d.drawPolygon( HEX_OUTLINE_IMAGE);
		HEX_OUTLINE_IMAGE.translate( -HEX_X_SHIFT, -HEX_Y_SHIFT);
		//draw hex board
		for( int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (WIDTH_SEGMENT*Constants.BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+Constants.BOARD_TOP_PADDING;
				HEX_OUTLINE_IMAGE.translate( ((int) (x-HEX_SIZE.getWidth()/2))-2, ((int) (y-HEX_SIZE.getHeight()/2)-3));
				g2d.drawPolygon( HEX_OUTLINE_IMAGE);
				HEX_OUTLINE_IMAGE.translate( -((int) (x-HEX_SIZE.getWidth()/2)-2), -((int) (y-HEX_SIZE.getHeight()/2)-3));
			}
		}
		//draw bank tiles
		TILE_OUTLINE.translate( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT);
		for( int i=0; i<4; i++){
			TILE_OUTLINE.translate( TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		//draw rack tiles
		TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_OUTLINE.height-PADDING);
		for( int i=0; i<Constants.MAX_RACK_SIZE; i++){
			if(i==5){
				TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(2*PADDING));
			}
			TILE_OUTLINE.translate( -TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		TILE_OUTLINE.setLocation( 0, 0);
		g2d.dispose();
	}

	private volatile boolean useDice = false;
	private volatile boolean isActive = false;
	private volatile boolean phaseDone = false;

	private JButton jbSkip;
	private DiceRoller dice;
	private LockManager locks;
	private JTextField jtfStatus;
	private Controller controller;
	private RollReason lastRollReason;
	private MoveAnimation moveAnimation;
	private ITileProperties playerMarker;
	private PlayerInfo players[], currentPlayer;
	
	/**
	 * basic super constructor warper for JPanel
	 */
	public Board(){
		super( null, true);
	}

	public void setActive( boolean active) {
		this.isActive = active;
		controller.ignoreAll( !active);
	}
	
	/**
	 * create LockManager and mouse listeners with specific player count
	 * @param playerCount - number of players to be playing on this board
	 */
	public void init( int playerCount){
		moveAnimation = new MoveAnimation( this);
		
		controller = new Controller();
		addMouseListener( controller);
		addMouseWheelListener( controller);
		addMouseMotionListener( controller);
		
		dice = new DiceRoller();
		dice.setBounds( HEX_BOARD_SIZE.width+DICE_SIZE/2, getHeight()-DICE_SIZE-10, DICE_SIZE, DICE_SIZE);
		dice.init();
		dice.addMouseListener( controller);
		add( dice);
		
		locks = new LockManager( playerCount);
		jtfStatus = new JTextField("Welcome to Kings & Things");
		jtfStatus.setBounds( 10, getHeight()-40, HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, 35);
		jtfStatus.setEditable( false);
		jtfStatus.setOpaque( false);
		jtfStatus.setBorder( null);
		jtfStatus.setFont( STATUS_INDICATOR_FONT);
		add(jtfStatus);
		
		jbSkip = new JButton( new ImageIcon( Constants.IMAGE_SKIP));
		jbSkip.setContentAreaFilled(false);
		jbSkip.setBorderPainted(false);
		jbSkip.setOpaque( false);
		jbSkip.addActionListener( controller);
		jbSkip.setToolTipText( "Skip this phase");
		jbSkip.setBounds( HEX_BOARD_SIZE.width-DICE_SIZE, getHeight()-DICE_SIZE-10, DICE_SIZE, DICE_SIZE);
		add(jbSkip);
		Rectangle bound = new Rectangle( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT, TILE_SIZE.width, TILE_SIZE.height);
		bound.translate( TILE_X_SHIFT, 0);
		//Tower
		bound.translate( TILE_X_SHIFT, 0);
		//Marker
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Gold)), bound, true);
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Cup)), bound, true);
		new UpdateReceiver();
	}
	
	public void setCurrentPlayer( PlayerInfo player){
		currentPlayer = player;
		playerMarker = Constants.getPlayerMarker( currentPlayer.getID());
	}
	
	public boolean matchPlayer( final int ID){
		return currentPlayer.getID()==ID;
	}
	
	/**
	 * add a tile to the board
	 * @param tile - tile to be added, must not be null
	 * @param bound - bounds to be used in placing the tile, must nut be null
	 * @param lock - if true this tile is fake and cannot be animated, and uses a Permanent Lock
	 * @return fully created tile that was added to board
	 */
	private Tile addTile( Tile tile, Rectangle bound, boolean lock){
		tile.init();
		tile.setBounds( bound);
		tile.setLockArea( locks.getPermanentLock( tile));
		tile.setCanAnimate( !lock);
		add(tile,0);
		return tile;
	}
	
	/**
	 * paint the background with already drawn outlines.
	 * paint players information
	 * paint locks if Constants.DRAW_LOCKS is true
	 */
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( IMAGE, 0, 0, getWidth(), getHeight(), null);
		g2d.setFont( STATUS_INDICATOR_FONT);
		if( players!=null && currentPlayer!=null){
			for( int i=0, y=PLAYERS_STATE_PADDING; i<players.length; i++, y+=PLAYERS_STATE_PADDING){
				if( players[i].getID()!=currentPlayer.getID()){
					g2d.drawString( (players[i].isActive()?"*":"")+players[i].getName(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, y);
					g2d.drawString( "Gold: " + players[i].getGold(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+165, y);
					g2d.drawString( "Rack: " + players[i].getCradsOnRack(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+355, y);
				}else{
					y-=PLAYERS_STATE_PADDING;
				}
			}
			g2d.drawString( (currentPlayer.isActive()?"*":"")+currentPlayer.getName(), HEX_BOARD_SIZE.width+160, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
			g2d.drawString( "Gold: " + currentPlayer.getGold(), HEX_BOARD_SIZE.width+360, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
		}
		if( Constants.DRAW_LOCKS){
			locks.draw( g2d);
		}
	}


	/**
	 * add new hexes to bank lock to be send to board, max of 37
	 * this placement uses the predetermined order stored in 
	 * arrays Constants.BOARD_LOAD_ROW and Constants.BOARD_LOAD_COL
	 * @param hexes - list of hexStates to be used in placing Hexes, if null fakes will be created
	 * @return array of tiles in order they were created
	 */
	private Tile[] setupHexesForPlacement( HexState[] hexes) {
		Tile tile = null;
		int x, y, hexCount = hexes==null?Constants.MAX_HEXES_ON_BOARD:hexes.length;
		Tile[] list = new Tile[hexCount];
		for(int ring=0, drawIndex=0; ring<BOARD_LOAD_ROW.length&&drawIndex<hexCount; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length&&drawIndex<hexCount; count++, drawIndex++){
				tile = addTile( new Hex( hexes==null?new HexState():hexes[drawIndex]), new Rectangle( 8,8,HEX_SIZE.width, HEX_SIZE.height), false);
				x = (WIDTH_SEGMENT*Constants.BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+Constants.BOARD_TOP_PADDING;
				tile.setDestination( x, y);
				list[drawIndex] = tile;
			}
		}
		return list;
	}

	/**
	 * add new tiles to cup lock to be send to player rack, max of 10
	 * @param prop - list of tiles to be placed, if null fakes will be created
	 * @return array of tiles in order they were created
	 */
	private Tile[] setupTilesForRack( ITileProperties[] prop) {
		Tile tile = null;
		Tile[] list = new Tile[Constants.MAX_RACK_SIZE];
		Lock lock = locks.getPermanentLock( Category.Cup);
		Point center = lock.getCenter();
		//create bound for starting position of tile
		Rectangle start = new Rectangle( center.x-TILE_SIZE.width/2, center.y-TILE_SIZE.height/2, TILE_SIZE.width, TILE_SIZE.height);
		addTile( new Tile( new TileProperties( Category.Cup)), start, true);
		//create bound for destination location, this bound starts from outside of board
		Rectangle bound = new Rectangle( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_SIZE.height-PADDING, TILE_SIZE.width, TILE_SIZE.height);
		for( int count=0; count<Constants.MAX_RACK_SIZE; count++){
			tile = addTile( new Tile( prop==null?new TileProperties(Category.Cup):prop[count]), start, false);
			if( count==5){
				// since rack is two rows of five, at half all bounds must be shifted up, this bound starts from outside of board
				bound.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(PADDING*2));
			}
			bound.translate( -TILE_X_SHIFT, 0);
			//set final destination for tile to be animated later
			tile.setDestination( bound.x+TILE_SIZE.width/2, bound.y+TILE_SIZE.height/2);
			list[count] = tile;
		}
		return list;
	}
	
	private void noneAnimatedPlacement( Tile[] tiles){
		Point end;
		Dimension size;
		for( Tile tile : tiles){
			if( tile==null){
				continue;
			}
			size = tile.getSize();
			end = tile.getDestination();
			tile.setLocation( end.x-size.width/2, end.y-size.height/2);
			tile.setLockArea( locks.getLock( tile));
			placeTileOnHex( tile);
		}
	}
	
	private void noneAnimtedFlipAll() {
		for( Component comp : getComponents()){
			if( comp instanceof Hex){
				((Tile)comp).flip();
			}
		}
	}
	
	/**
	 * animate placement of hex tiles
	 * @param hexes - list of HexState to populate the hexes on board
	 */
	private void animateHexPlacement( HexState[] hexes){
		addTile( new Hex( new HexState()), new Rectangle(8,8,HEX_SIZE.width,HEX_SIZE.height), true);
		if( !isActive){
			noneAnimatedPlacement( setupHexesForPlacement( hexes));
			phaseDone = true;
		}else{
			moveAnimation.start( setupHexesForPlacement( hexes));
		}
	}
	
	/**
	 * animate placement of rack tiles
	 */
	private void animateRackPlacement( ITileProperties[] rack){
		if( !isActive){
			noneAnimatedPlacement( setupTilesForRack( rack));
			phaseDone = true;
		}else{
			moveAnimation.start( setupTilesForRack( rack));
		}
	}
	
	/**
	 * Flip all hexes
	 */
	private void FlipAllHexes(){
		if( !isActive){
			noneAnimtedFlipAll();
			phaseDone = true;
		}else{
			new FlipAll( getComponents(), this).start();
		}
	}
	
	private boolean firstMarker = true;
	private void placeMarkers(){
		Point point = locks.getPermanentLock( Category.State).getCenter();
		Rectangle bound = new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height);
		if( firstMarker){
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			firstMarker = false;
		}
		addTile( new Tile( playerMarker), bound, false).flip();
	}
	
	private boolean firstTower = true;
	private ITileProperties tower = null;
	private void placeTower(){
		Point point = locks.getPermanentLock( Category.Buildable).getCenter();
		Rectangle bound = new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height);
		if( firstTower){
			for( TileProperties tile : Constants.BUILDING.values()){
				if( tile.getName().equals("Tower") && !tile.hasAbility( Ability.Neutralised)){
					tower = tile;
				}
			}
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			firstTower = false;
		}
		addTile( new Tile( tower), bound, false).flip();
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, Constants.BOARD, Board.this);
		}

		@Override
		protected void handlePrivate( UpdatePackage update) {
			updateBoard( update);
		}

		@Override
		protected boolean verifyPrivate( UpdatePackage update) {
			return update.isValidID(ID|currentPlayer.getID());
		}
	}
	
	/**
	 * update the board with new information, such as
	 * hex placement, flip all, player order and rack info.
	 * @param update - event wrapper containing update information
	 */
	public void updateBoard( UpdatePackage update){
		HexState hex = null;
		for( UpdateInstruction instruction : update.getInstructions()){
			switch( instruction){
				case UpdatePlayers:
					setCurrentPlayer( (PlayerInfo)update.getData( UpdateKey.Player));
					players = (PlayerInfo[]) update.getData( UpdateKey.Players);
					repaint();
					phaseDone = true;
					break;
				case Rejected:
					manageRejection( (UpdateInstruction)update.getData( UpdateKey.Instruction));
					break;
				case PlaceBoard:
					animateHexPlacement( (HexState[]) update.getData( UpdateKey.Hex));
					break;
				case SetupPhase:
					manageSetupPhase( (SetupPhase)update.getData( UpdateKey.Phase));
					break;
				case RegularPhase:
					manageRegularPhase( (RegularPhase)update.getData( UpdateKey.Phase));
					break;
				case DieValue:
					Roll roll = (Roll)update.getData( UpdateKey.Roll);
					dice.setResult( roll.getBaseRolls());
					break;
				case HexOwnership:
					hex = (HexState)update.getData( UpdateKey.HexState);
					locks.getLockForHex( hex.getLocation()).getHex().setState( hex);
					break;
				case FlipAll:
					FlipAllHexes();
					break;
				case SeaHexChanged:
					hex = (HexState)update.getData( UpdateKey.HexState);
					locks.getLockForHex( hex.getLocation()).getHex().setState( hex);
					break;
				case GameState:
					animateHexPlacement( (HexState[]) update.getData( UpdateKey.Hex));
					waitForPhase();
					if( (boolean) update.getData( UpdateKey.Flipped)){
						FlipAllHexes();
					}
					waitForPhase();
					animateRackPlacement( (ITileProperties[]) update.getData( UpdateKey.Rack));
					waitForPhase();
					break;
				default:
					throw new IllegalStateException( "ERROR - No handle for " + update.peekFirstInstruction());
			}
			while( !phaseDone){
				try {
					Thread.sleep( 100);
				} catch ( InterruptedException e) {}
			}
		}
	}

	/**
	 * used primarily for animation wait time.
	 * thread sleeps till animation is over.
	 */
	private synchronized void waitForPhase(){
		while( !phaseDone){
			try {
				Thread.sleep( 50);
			} catch ( InterruptedException e) {}
		}
	}
	
	private void prepareForRollDice( int count, RollReason reason, String message){
		useDice = true;
		dice.setDiceCount( count);
		jtfStatus.setText( message);
		lastRollReason = reason;
	}
	
	private void manageRejection( UpdateInstruction data) {
		//TODO Handle More Rejections
		switch( data){
			case Skip:
				jtfStatus.setText( "Cannot skip this phase");
				break;
			case TieRoll:
				prepareForRollDice(2, lastRollReason, "Tie Roll, Roll again");
				break;
			case SeaHexChanged:
				controller.undo();
				break;
			case HexOwnership:
				jtfStatus.setText( "WARN - cannot own this hex");
				controller.undo();
				break;
			default:
				throw new IllegalStateException( "No handle for rejection of: " + data);
		}
	}
	
	private void manageRegularPhase( RegularPhase phase) {
		switch( phase){
			case COMBAT:
				break;
			case CONSTRUCTION:
				break;
			case MOVEMENT:
				break;
			case RANDOM_EVENTS:
				break;
			case RECRUITING_CHARACTERS:
				break;
			case RECRUITING_THINGS:
				break;
			case SPECIAL_POWERS:
				break;
			default:
				break;
		}
	}

	private void manageSetupPhase( SetupPhase phase){
		switch( phase){
			case DETERMINE_PLAYER_ORDER:
				controller.setRollDice(true);
				//TODO add support for custom roll
				prepareForRollDice(2, RollReason.DETERMINE_PLAYER_ORDER, "Roll dice to determine order");
				break;
			case EXCHANGE_SEA_HEXES:
				controller.setMoveHex( true);
				jtfStatus.setText( "Exchange sea hexes, if any");
				break;
			case EXCHANGE_THINGS:
				jtfStatus.setText( "Exchange things, if any");
				break;
			case PICK_FIRST_HEX:
				controller.setMoveBank( true);
				placeMarkers();
				jtfStatus.setText( "Pick your first Hex");
				break;
			case PICK_SECOND_HEX:
				controller.setMoveBank( true);
				jtfStatus.setText( "Pick your second Hex");
				break;
			case PICK_THIRD_HEX:
				controller.setMoveBank( true);
				jtfStatus.setText( "Pick your third Hex");
				break;
			case PLACE_EXCHANGED_THINGS:
				jtfStatus.setText( "Place exchanged things on board, if any");
				break;
			case PLACE_FREE_THINGS:
				jtfStatus.setText( "Place things on board, if any");
				break;
			case PLACE_FREE_TOWER:
				placeTower();
				jtfStatus.setText( "Place one free tower on board");
				break;
			case SETUP_FINISHED:
				jtfStatus.setText( "Setup Phase Complete");
				break;
			default:
				break;
		}
		
	}

	/**
	 * place any tile on hex, however only battle and markers will be drawn
	 * current Tile will be removed and added as TileProperties to the Hex
	 * @param tile - thing to be placed
	 */
	private HexState placeTileOnHex( Tile tile) {
		if( tile.isTile() && tile.hasLock() && tile.getLock().canHold( tile) ){
			return tile.getLock().getHex().getState().addThingToHexGUI( tile.getProperties());
		}
		return null;
	}
	
	/**
	 * input class for mouse, used for like assignment and current testing phases suck as placement
	 */
	private class Controller extends MouseAdapter implements ActionListener{

		private Tile lastRemovedTile;
		private UndoManager undoManger;
		private ThingPlacmentUndo thingUndo;
		private Rectangle bound, boardBound;
		private Lock newLock;
		private Tile currentTile;
		private Point lastPoint;
		private HexState movingState;
		private boolean ignore = true;
		private boolean moveHex = false;
		private boolean moveStack = false;
		private boolean moveBank = false;
		private boolean rollDice = false;
		
		public Controller(){
			this.undoManger = new UndoManager();
		}
		
		public void undo(){
			if( lastRemovedTile!=null){
				add( lastRemovedTile, 0);
				revalidate();
			}
			undoManger.undo( moveAnimation);
		}
		
		public void ignoreAll( boolean ignore){
			this.ignore = ignore;
		}
		
		private void setControls( boolean stack, boolean hex, boolean bank, boolean roll){
			ignore = false;
			moveHex = hex;
			moveStack = stack;
			moveBank = bank;
			rollDice = roll;
		}
		
		private boolean noMove(){
			return moveHex==false&&moveBank==false&&moveStack==false;
		}
		
		public void setMoveStack( boolean stack){
			setControls( stack, false, false, false);
		}
		
		public void setMoveHex( boolean hex){
			setControls( false, hex, false, false);
		}
		
		public void setMoveBank( boolean bank){
			setControls( false, false, bank, false);
		}
		
		public void setRollDice( boolean roll){
			setControls( false, false, false, roll);
		}
		
		/**
		 * checks to see if movement is still inside the board,
		 * check to see if a new lock can be placed,	`
		 * check to see if old lock can be released/
		 */
		@Override
	    public void mouseDragged(MouseEvent e){
			if( ignore|| noMove()){
				return;
			}
			e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, Board.this);
			if( phaseDone && (moveStack || moveHex || moveBank) && currentTile!=null){
				boardBound = getBounds();
				bound = currentTile.getBounds();
				lastPoint = bound.getLocation();
				//TODO adjust click to prevent centering all the time
				bound.x = e.getX()-(bound.width/2);
				if( !boardBound.contains( bound)){
					bound.x = lastPoint.x;
				}
				bound.y = e.getY()-(bound.height/2);
				if( !boardBound.contains( bound)){
					bound.y= lastPoint.y;
				}
				if( currentTile.hasLock()){
					if( locks.canLeaveLock( currentTile, e.getPoint())){
						currentTile.removeLock();
						currentTile.setBounds( bound);
					}
				}else{
					if( moveHex || moveStack){
						newLock = locks.getDropLock( currentTile);
					}else{
						newLock = locks.getLock( currentTile, bound.x+(bound.width/2), bound.y+(bound.height/2));
					}
					if( newLock!=null){
						currentTile.setLockArea( newLock);
						Point center = newLock.getCenter();
						bound.setLocation( center.x-(bound.width/2), center.y-(bound.height/2));
					}
					currentTile.setBounds( bound);
				}
			}
		}
		
		@Override
		public void mouseReleased( MouseEvent e){
			if( ignore|| noMove()){
				return;
			}
			e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, Board.this);
			if(currentTile!=null){
				if(newLock!=null){
					if( moveHex){
						new UpdatePackage( UpdateInstruction.SeaHexChanged, UpdateKey.HexState, ((Hex)currentTile).getState(), "Board.Input").postNetworkEvent( currentPlayer.getID());
					}else if(moveStack){
						//TODO support for moving stack, some of stack and dropping to Cup
					}else if(newLock.canHold( currentTile)){//TODO check canHold, might be unnecessary condition
						HexState hex = placeTileOnHex( currentTile);
						lastRemovedTile = currentTile;
						remove( currentTile);
						revalidate();
						repaint();
						thingUndo.addEnd( ThingPlacmentUndo.createOperation( currentTile, hex, e.getPoint()));
						undoManger.addUndo( thingUndo);
						if( hex!=null){
							new UpdatePackage( UpdateInstruction.HexOwnership, UpdateKey.HexState, hex, "Board.Input").postNetworkEvent( currentPlayer.getID());
						}
					}
				}else{
					thingUndo.addEnd( ThingPlacmentUndo.createOperation( currentTile, null, e.getPoint()));
					undoManger.addUndo( thingUndo);
					undo();
				}
			}
			thingUndo = null;
			currentTile = null; 
			lastPoint = null;
			newLock = null;
			bound = null;
			repaint();
		}

		/**
		 * record initial mouse press for later drag, locking and move
		 */
		@Override
		public void mousePressed( MouseEvent e){
			if( ignore || noMove()){
				return;
			}
			e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, Board.this);
			//get the deepest component in the given point
			Component deepestComponent = SwingUtilities.getDeepestComponentAt( Board.this, e.getX(), e.getY());
			if( phaseDone && deepestComponent!=null){
				if( deepestComponent instanceof Tile){
					currentTile = (Tile) deepestComponent;
					//bring the component to the top, to prevent overlapping
					remove( currentTile);
					add( currentTile, 0);
					revalidate();
					repaint();
					thingUndo = new ThingPlacmentUndo();
					thingUndo.addStart( ThingPlacmentUndo.createOperation( currentTile, null, currentTile.getLock().getCenter()));
					//check to see if it is hex
					if( !currentTile.isTile() && currentTile.hasLock()){
						if( !moveHex){
							currentTile = null;
							thingUndo = null;
						}else if( moveHex){
							
						}else if( moveStack){
							newLock = currentTile.getLock();
							movingState = newLock.getHex().getState();
							if( movingState.hasMarker()){
								if( movingState.hasThings()){
									lastPoint = newLock.getCenter();
									Rectangle bound = new Rectangle( TILE_SIZE);
									bound.setLocation( lastPoint.x-(TILE_SIZE.width/2), lastPoint.y-(TILE_SIZE.height/2));
									currentTile = addTile( new Tile( playerMarker), bound, false);
									currentTile.setLockArea( newLock);
									currentTile.flip();
									revalidate();
									moveStack = true;
								} else {
									currentTile = null;
								}
							}
						}
					}
				}
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e){
			dice.shrink();
		}

		@Override
		public void mouseClicked( MouseEvent e){
			if( ignore){
				return;
			}
			if( !rollDice){
				return;
			}
			if( SwingUtilities.isLeftMouseButton(e)){
				if( e.getSource()==dice){
					if( useDice && dice.canRoll()){
						useDice = false;
						int rollValue = Integer.parseInt(JOptionPane.showInputDialog(Board.this, "Select desired roll value", "RollValue", JOptionPane.PLAIN_MESSAGE));
						Roll roll = new Roll( dice.getDiceCount(), null, lastRollReason, currentPlayer.getID(), rollValue);
						new UpdatePackage( UpdateInstruction.NeedRoll, UpdateKey.Roll, roll,"Board "+currentPlayer.getID()).postNetworkEvent( currentPlayer.getID());
						dice.roll();
						new Thread( new Runnable() {
							@Override
							public void run() {
								while( dice.isRolling()){
									try {
										Thread.sleep( 10);
									} catch ( InterruptedException e) {}
								}
								jtfStatus.setText( "Done Rolling: " + dice.getResults());
								new UpdatePackage( UpdateInstruction.DoneRolling, "Board.Input").postNetworkEvent( currentPlayer.getID());
							}
						}, "Dice Wait").start();
					}else{
						dice.expand();
					}
				}
			}else if( SwingUtilities.isRightMouseButton( e)){
				//TODO mouse right click support
			}else if( SwingUtilities.isMiddleMouseButton( e)){
				//TODO mouse middle click support
			}
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			new UpdatePackage( UpdateInstruction.Skip, "Board.Input").postNetworkEvent( currentPlayer.getID());
		}
	}

	@Override
	public void phaseDone() {
		phaseDone = true;
	}

	@Override
	public void phaseStarted() {
		phaseDone = false;
	}

	@Override
	public void repaintCanvas() {
		repaint();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public Lock getLock(Tile tile) {
		return locks.getLock(tile);
	}

	@Override
	public HexState placeOnHex(Tile tile) {
		return placeTileOnHex(tile);
	}
}
