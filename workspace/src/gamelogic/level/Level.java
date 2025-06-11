package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.hitbox.Hitbox;
import gameengine.hitbox.RectHitbox;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;
	private boolean touchingGas = false;
	private long lastTouchedGas;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waters = new ArrayList<>();
	private ArrayList<Gas> gasses = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public static double linearInterpolation(double Start, double Goal, double Alpha) {
		return Start + (Goal - Start) * Alpha;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		touchingGas = false;

		gasses.clear();
		waters.clear();
		flowers.clear();

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18) {
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
					waters.add((Water)tiles[x][y]);
				} 
				else if (values[x][y] == 19) {
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
					waters.add((Water)tiles[x][y]);
				}
				else if (values[x][y] == 20) {
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
					waters.add((Water)tiles[x][y]);
				}
				else if (values[x][y] == 21) {
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
					waters.add((Water)tiles[x][y]);
				}
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			RectHitbox playerHitbox = player.getHitbox();
			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				Flower currentFlower = flowers.get(i);
				RectHitbox flowerHitbox = currentFlower.getHitbox();
				int flowerType = currentFlower.getType();

				if (flowerHitbox.isIntersecting(playerHitbox)) {
					if(flowerType == 1)
					water(currentFlower.getCol(), currentFlower.getRow(), map, 3);
				else
					addGas(currentFlower.getCol(), currentFlower.getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			boolean touchingWater = false;
			boolean justTouchedGas = false;


			for (Water w : waters) {
				RectHitbox waterHitbox = w.getHitbox();
				// int fullness = w.getFullness();

				if (waterHitbox.isIntersecting(playerHitbox)) {
					// System.out.println("touching");
					touchingWater = true;
				}
			}

			for (Gas bro : gasses) {
				if (bro.getHitbox().isIntersecting(playerHitbox)) {
					justTouchedGas = true;
				}
			}

			if (justTouchedGas) {
				if (!touchingGas) {
					touchingGas = true;
					lastTouchedGas = System.currentTimeMillis();
				}
			} else {
				touchingGas = false;
				lastTouchedGas = System.currentTimeMillis();
			};

			if (touchingGas && System.currentTimeMillis() - lastTouchedGas >= 3000) {
				onPlayerDeath();
			} else if (touchingGas) {
				
			}
			
			
			if (!touchingWater) {
				player.setSwimmingState(false);
				// not touching water;
			} else {
				player.setSwimmingState(true);
				// touching water
			}



			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!

	//precondition: must be valid column in bounds of map, must be valid row in bounds of map.
	//post condition: disperses water all over till it cant anymore.

	/* private void addGas(int col, int row, Map map, int maxTiles, ArrayList<Gas> placedThisRound) {
		int currentCol = col;
		int currentRow = row;
		// int rowsSubtracted = 0;

		//Water waterAt = new Water(col, row, tileSize, tile.get)
		Gas g = new Gas (col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(currentCol, currentRow, g);

			ArrayList<Integer> ColumnIndexes = new ArrayList<Integer>();
			ArrayList<Integer> RowIndexes = new ArrayList<Integer>();

			for (int i = 0; i <= 8; i++) {
				ColumnIndexes.add(col);
				RowIndexes.add(row);
			}

		while (placedThisRound.size() < 20) {
			int rowInd = row;
			int colInd = col;

			//int currentSize = placedThisRound.size();
			boolean Placed = false;


			for (int i = 1; i <= 8; i++) {
				if (placedThisRound.size() >= 20) {
					break;
				}
				
				if (i == 1) {
					//if (row - 1 >= 0) {
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound-1;
					   int columMovement = columnFound;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
					//}
				} else if (i == 2) {
	                   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound-1;
					   int columMovement = currentCol-1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
				} else if (i == 3) {
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound-1;
					   int columMovement = currentCol + 1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
					
				} else if (i == 4) {
                       int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound;
					   int columMovement = columnFound - 1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
					
				} else if (i == 5) {
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound;
					   int columMovement = columnFound + 1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }

				} else if (i == 6) {
					   //int downRow = (currentRow - rowsSubtracted) - 1;
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound + 1;
					   int columMovement = columnFound;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
				} else if (i == 7) {
					//int downRow = (currentRow - rowsSubtracted) - 1;
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound + 1;
					   int columMovement = currentCol + 1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
				} else if (i == 8) {
					//int downRow = (currentRow - rowsSubtracted) - 1;
					   int rowFound = RowIndexes.get(i);
					   int columnFound = ColumnIndexes.get(i);
					   
					   int rowMovement = rowFound + 1;
					   int columMovement = currentCol - 1;

					   if (rowMovement >= 0 && rowMovement <= map.getTiles()[0].length && columMovement >= 0 && columMovement <= map.getTiles().length) {
					   Tile foundTile = map.getTiles()[columMovement][rowMovement];
 
					   if (!(foundTile.isSolid()) && !(placedThisRound.contains(foundTile))) {
						 Gas ToPlace = new Gas(columMovement, rowMovement, tileSize, tileset.getImage("GasOne"), this, 0);
						 map.addTile(columMovement, rowMovement, ToPlace);
						 placedThisRound.add(ToPlace);
						 Placed = true;
					   }

					   RowIndexes.set(i, rowMovement);
					   ColumnIndexes.set(i, columMovement);
					   }
				}
			}

			if (!Placed) {
				break;
			}
		}
		//placedThisRound.add(g); 
		// do while loop, inside: 
		// make the desired pattenrn centered at the location of placedThisRound.get(0);
		// remove the tile once processed from placedThisRound
		// be sure to ad every tile you make to placed this round
	};*/

	// precondition: valid column, valid row, valid map, valid maxTiles, and validPlacedThisRound
	// postcondition: creates a bunch of gas at different positions
	private void addGas(int col, int row, Map map, int maxTiles, ArrayList<Gas> placedThisRound) {
	    int currentCol = col;
	    int currentRow = row;

	    Gas g = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
	    map.addTile(currentCol, currentRow, g);

	    placedThisRound.add(g);

	    ArrayList<Integer> ColumnIndexes = new ArrayList<>();
	    ArrayList<Integer> RowIndexes = new ArrayList<>();

		// store all possible directions in corresponding arraylists in order
	    ColumnIndexes.add(col);
	    RowIndexes.add(row - 1);

	    ColumnIndexes.add(col - 1);
	    RowIndexes.add(row);

	    ColumnIndexes.add(col + 1);
	    RowIndexes.add(row);

	    ColumnIndexes.add(col);
	    RowIndexes.add(row + 1);

		// begin while loop to check if size() >= 20 then it'llbreak and stop
	     while (placedThisRound.size() < 20) {
		   boolean placedAny = false;

		   // -- iterate through the directions arraylist, will contain directions
		    for (int i = 0; i < ColumnIndexes.size(); i++) {
			 if (placedThisRound.size() >= 20) {
				break;
			}
			// get current index at for current col
			int colInd = ColumnIndexes.get(i);
			int rowInd = RowIndexes.get(i); // they have the same indexes so it is fair to have to iterate through only one array, hence the reason behind having two

			// prevention for index out of bounds errors

			if (rowInd < 0 || colInd < 0 || rowInd >= map.getTiles()[0].length || colInd >= map.getTiles().length) {
				continue;
			}

			Tile foundTile = map.getTiles()[colInd][rowInd];
			// -- sanity check for tile underneath;

			if (!foundTile.isSolid() && !(foundTile instanceof Gas) && !placedThisRound.contains(foundTile)) {
				Gas toPlace = new Gas(colInd, rowInd, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(colInd, rowInd, toPlace);
				gasses.add(toPlace);
				
				placedThisRound.add(toPlace);
				placedAny = true;

				//--append new variants of possible directions to arraylist to be iterated over again
				ColumnIndexes.add(colInd);
				RowIndexes.add(rowInd - 1);
				ColumnIndexes.add(colInd - 1);
				RowIndexes.add(rowInd); 
				ColumnIndexes.add(colInd + 1);
				RowIndexes.add(rowInd); 
				ColumnIndexes.add(colInd);
				RowIndexes.add(rowInd + 1); 
			}
		}

		// if the loop couldn't place anything there aren't any valid directions anymore so it'll just end early 🤷‍♀️
		if (!placedAny) {
			break;
		} 
	  }
    }


	private void water(int col, int row, Map map, int fullness) {

		// loop ideas
		// loop checking direction on both ends, store current column and row that im on; 
		// if not on surface go down, use the line water
		// if on surface disperse right and left
		// its just a loop
		//System.out.println(fullness);
		String img = (fullness == 3) ? "Full_water" : (fullness == 2) ? "Half_water" : (fullness == 1) ? "Quarter_water" : "Falling_water";

		Water w = new Water (col, row, tileSize, tileset.getImage(img), this, fullness);
		map.addTile(col, row, w);
		waters.add(w);
		
	// check for row underneath;
	   if (row + 1 < map.getTiles()[1].length) {
		Tile placeAt = map.getTiles()[col][row+1];
		int fullnessLol = 0;

		if (row + 2 < map.getTiles()[1].length) {
			Tile underLol = map.getTiles()[col][row+2];;
			if (underLol.isSolid()) {
				fullnessLol = 3;
			}
		}

		if (!placeAt.isSolid() && !(placeAt instanceof Water)) {
			water(col, row+1, map, fullnessLol);
			return;
		}
	}


	// check for column to side;
	if (col - 1 >= 0 && row + 1 < map.getTiles()[1].length) {
		Tile placeAt = map.getTiles()[col-1][row];
		Tile underNeath = map.getTiles()[col-1][row+1];

		if (!placeAt.isSolid() && !(placeAt instanceof Water)) {
			int fullnessQuantity = (!underNeath.isSolid()) ? 0 : 3;
			
			if (col + 1 < map.getTiles().length) {
				Tile behind = map.getTiles()[col][row];

				if ((behind instanceof Water)) {
					int full = ((Water) (behind)).getFullness();

					if (full == 3) {
						fullnessQuantity = 2;
					} else if (full == 2 || full == 1) {
						fullnessQuantity = 1;
					}
				 }
			}

			water(col-1, row, map, fullnessQuantity);
		}

		
	}

	// check for column to side;
	if (col + 1 < map.getTiles().length && row + 1 < map.getTiles()[1].length) {
		Tile underNeath = map.getTiles()[col+1][row+1];
		Tile toPlace = map.getTiles()[col+1][row];

		if (!toPlace.isSolid() && !(toPlace instanceof Water)) {
			int fullnessQuantity = (!underNeath.isSolid()) ? 0 : 3;

			if (col - 1 >= 0) {
				
				//if (col - 1 >= 0) {
				Tile behind = map.getTiles()[col][row];

				if ((behind instanceof Water)) {
					int full = ((Water) (behind)).getFullness();
					//setData = true;
					if (full == 3) {
						fullnessQuantity = 2;
					} else if (full == 2 || full == 1) {
						fullnessQuantity = 1;
					}
				  }
			}
			water(col+1, row, map, fullnessQuantity);
		}
	}
   }



	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }


	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}