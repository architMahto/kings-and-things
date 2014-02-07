package common;

import static common.Constants.BUILDING;
import static common.Constants.CUP;
import static common.Constants.GOLD;
import static common.Constants.HEX;
import static common.Constants.SPECIAL;
import static common.Constants.STATE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import client.gui.LoadProgress;
import common.Constants.Ability;
import common.Constants.Category;
import common.Constants.Restriction;

public class LoadResources implements Runnable, FileVisitor< Path>{

	private int copyTile = 0;
	private Category currentCategory = null;
	private final Path RESOURCES_DIRECTORY;
	
	public LoadResources()
	{
		this(Constants.RESOURCE_PATH.toString());
	}
	
	public LoadResources(String directory)
	{
		RESOURCES_DIRECTORY = Paths.get(directory);
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep( 200);
		} catch ( InterruptedException e) {}
		try {
			Files.walkFileTree( RESOURCES_DIRECTORY, this);
		} catch ( IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep( 400);
		} catch ( InterruptedException e) {}
		new LoadProgress( Category.END).postCommand();
	}

	@Override
	public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs) throws IOException {
		try{
			if( !(currentCategory!=null && currentCategory==Category.Cup && dir.toString().contains( Category.Cup.name()))){
				currentCategory = Category.valueOf( dir.getFileName().toString());
			}
		}catch( IllegalArgumentException e){
			currentCategory = null;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs) throws IOException {
		if( currentCategory!=null && currentCategory!=Category.Resources  && currentCategory!=Category.Misc){
			TileProperties tile = createTile( file.getFileName().toString());
			switch( currentCategory){
				case Building:
					//TODO need to handle special income counters + actual num of city/village counters
					if(tile.isBuildableBuilding())
					{
						tile.setInfinite();
					}
					tile.setSpecialFlip();
					BUILDING.put( tile.hashCode(), tile);
					break;
				case Cup:
					if( copyTile==0){
						CUP.put( tile.hashCode(), tile);
					}else{
						for( int i=0; i<copyTile; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							CUP.put( tileCopy.hashCode(), tileCopy);
						}
					}
					break;
				case Gold:
					tile.setNoFlip();
					tile.setInfinite();
					GOLD.put( tile.hashCode(), tile);
					break;
				case Hex:
					for( int i=0; i<copyTile; i++){
						TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
						HEX.put( tileCopy.hashCode(), tileCopy);
					}
					break;
				case Special:
					tile.setSpecialFlip();
					SPECIAL.put( tile.hashCode(), tile);
					break;
				case State:
					tile.setNoFlip();
					tile.setInfinite();
					STATE.put( tile.getRestriction()[0], tile);
					break;
					
				case Resources:
				default:
					//will never be called
			}
			copyTile = 0;
		}
		new LoadProgress( currentCategory).postCommand();
		return FileVisitResult.CONTINUE;
	}
	
	private TileProperties createTile( String name){
		String[] array = name.substring( 0, name.lastIndexOf( ".")).split( " ");
		TileProperties tile = new TileProperties();
		for( int i=0; i<array.length-1; i++){
			switch( array[i]){
				case "-n": tile.setName( array[++i]);break;
				case "-t": tile.addRestriction( Restriction.valueOf( array[++i]));break;
				case "-c": copyTile = Integer.parseInt( array[++i]);break;
				case "-s": tile.addAbilities( Ability.valueOf( array[++i]));break;
				case "-a": tile.setValue( Integer.parseInt( array[++i]));break;
				default: 
					throw new IllegalArgumentException("ERROR - incorrect file name \"" + name + "\n");
			}
		}
		return tile;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}
}
