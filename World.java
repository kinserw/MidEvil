import java.awt.geom.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.*;
import java.util.*;
import javax.swing.*;

import java.awt.event.*;

public class World extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, Runnable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2112960996668811817L;
	public static World theWorld = null;
	public static int rows = 30;
	public static int cols = 30;
	public static int density = 50;
	public static int numPlayers = 6;
	public static Cell currentCell = null;

	public static int rowOffset = 20;
	public static int colOffset = 20;
	
	public static JScrollPane scrollWorld = null;
	
	public static int difficulty = 8;
	
	private Cell[][] myMap = null;
	
	public transient boolean myKeepRunning = false;
	private ButtonPanel myBtnPanel = null;
	private int whosTurnItIs = 0;
	private transient HumanPlayer human = null;
	private int humanIndex = 0;
	private transient City citySelected = null;
	private transient Cell movingPieceFrom = null;
	private int blinking = 1;
	private transient Occupiers movingPiece = Occupiers.NONE;
	private boolean multiPieceMove = false;
	private transient boolean buyingAPiece = false;
	private transient ArrayList<Cell> myPlayableMap;
	private ArrayList<Player> myPlayers;
	
	private static final Color[] playerColors =
	{Color.LIGHT_GRAY,
	 Color.CYAN,
	 Color.PINK,
	 Color.RED,
	 Color.ORANGE,
	 Color.MAGENTA,
	 Color.YELLOW,
	 Color.DARK_GRAY};	
	
	private static final String[] playerColorNames =
	{"LIGHT_GRAY",
	 "CYAN",
	 "PINK",
	 "RED",
	 "ORANGE",
	 "MAGENTA",
	 "YELLOW",
	 "DARK_GRAY"
	};

	// World default constructor
	public World() {

		addComponentListener(this); // resizing of frame => resize the world
		addMouseListener(this); // for detecting mouse pressed
		addMouseMotionListener(this); // for detecting mouse movement
		World.theWorld = this;
		
	} // end World constructor
 

	
	@Override
	// override base class methods so that we can draw all the cells in our world
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		g.setColor(Color.red);
		try {

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
			for (int i = 0; i < myMap.length ; i++)
			{
				for (int j = 0; j < myMap[0].length; j++)
				{
					Cell cell = myMap[i][j];

					g.drawImage(cell.getImage(),i*rowOffset, j*colOffset,cell.getBackground(), null);
					
					// draw occupier on top of cell image
					if (cell.getOccupiers() != Occupiers.NONE)
					{
						// blink the human pieces that can be moved
						if ((cell.getCity() != null) && 
						    (cell.getCity().getPlayer() == human) &&
//							(cell.getOccupiers() != Occupiers.VILLAGE) &&
							(cell.getOccupiers() != Occupiers.CASTLE) &&
							(cell.ableToAttack()) &&
							(blinking < 0))
						{
							g.drawImage(cell.getGeology().getImage(), i*rowOffset, j*colOffset,cell.getBackground(), null);
						}

					}
				} 
			}
			
			
			g.setColor(Color.BLACK);


			
			// clear the line of buyable pieces
			int number = 60/rowOffset;
			for (int i = 0; i < Occupiers.placeablePieces.length; i++)
				myMap[number+i][0].setOccupiers(Occupiers.NONE);
			
			// display what the human player can buy for the city selected
			if (myPlayers.get(whosTurnItIs) == human)
			{
				myBtnPanel.setPlayerColor(human.getColor());
				myBtnPanel.setCitySelected(citySelected, human);

			}
			
			if (citySelected != null)
			{
				// draw outline of selected City
				g.setColor(Color.BLACK);
				((Graphics2D)g).draw(citySelected.getArea());
			}

		} // end try block
		catch (Exception exception) 
		{
			// do nothing; just capture errors for now
			// TODO: figure out why it's throwing exceptions (ConcurrentModificationException?)
			// 
			// I saw several places in the class documentation that Swing is NOT thread safe but
			// running my world class in a thread was based on examples I found on 
			// Oracles and StackOverflow websites. Catching and ignoring these errors 
			// doesn't seem to cause a problem in quick runs of the program.			
		} // end catch all exceptions
		
		if (--blinking < -1)
			blinking = 1;
		
	} // end paintComponent

	
	public void setButtonPanel(ButtonPanel btnPanel)
	{
		myBtnPanel = btnPanel;
	}
	
	private Cell findCellAt(int relativeX, int relativeY)
	{
		if (!myKeepRunning || myMap == null)
			return null;
		
		Cell cell = null;

		Rectangle rectangle = new Rectangle(0,0,rowOffset, colOffset);
		for (int i = 0; i < myMap.length ; i++)
		{
			for (int j = 0; j < myMap[0].length; j++)
			{
				rectangle.setLocation(i*rowOffset, j*colOffset);
				if (rectangle.contains(relativeX, relativeY))
					return myMap[i][j];
			}
		}

		return cell;
	}

	@Override
	// allow the window to be resized by the user, and adjust the world size 
	public void componentResized(ComponentEvent e) {

		// not sure I need to do anything;
	} // end componentResized 
	
	// override all the abstract methods even if nothing is done
	// note: "@override" is a compiler instruction to force validation against base class methods
	@Override
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void componentShown(ComponentEvent e) {

	}
	@Override
	public void componentHidden(ComponentEvent e) {} 
	@Override
	public void mouseClicked(MouseEvent e) 
	{
	
		if (e.getButton() == 1) 
		{
			Cell cell = findCellAt(e.getX(),e.getY());
			if ((cell != null) &&  
			    ((cell.getCity() != null ) 
			    && (cell.getCity().getPlayer() == human)
			    ))
			{
				citySelected = cell.getCity();
			}
			
		}
	}

	public void buyingArmyUnit(Occupiers piece)
	{
		buyingAPiece = true;
		movingPiece = piece;
		movingPieceFrom = null;
		Cursor customCursor = 
Toolkit.getDefaultToolkit().createCustomCursor(Occupiers.ourImage[piece.ordinal()], new Point(0, 0), "customCursor");
		Toolkit.getDefaultToolkit().getBestCursorSize(64, 64);
		this.setCursor( customCursor );		
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) 
	{ 
		Cell cell = findCellAt(e.getX(),e.getY());
		// only process if not btn 1 and not already in a multi piece move action
		if ((e.getButton() != 1) && !multiPieceMove && (cell != null))
		{

			// if cell is the human's then enable moving the pieces
			if ((cell.ableToAttack() &&
			    ((cell.getCity() != null ) &&  
			    (cell.getCity().getPlayer() == human))))
			{
				citySelected = cell.getCity();
				for (Occupiers piece : Occupiers.moveablePieces)
				{
					// find the piece they clicked on
					if (cell.getOccupiers().ordinal() == piece.ordinal())
					{
						// make sure they have more than one
						if (citySelected.setMultiMovePiece(piece))
						{
							movingPieceFrom = cell;
							movingPiece = piece;
							this.multiPieceMove = true;
							Cursor customCursor = 
		Toolkit.getDefaultToolkit().createCustomCursor(Occupiers.ourImage[piece.ordinal()], new Point(0, 0), "customCursor");
							Toolkit.getDefaultToolkit().getBestCursorSize(64, 64);
							this.setCursor( customCursor );
						}
					}

				}
			} // end of moving human pieces on the board
		} // end multiple piece move
			
		// see if the player is moving an existing piece or buying one
		if ((e.getButton() == 1) && (cell != null)) 
		{
			// if btn 1 pressed then cancel multiPieceMove if active
			if (this.multiPieceMove)
			{
				citySelected.cancelMultiMovePiece();
				multiPieceMove = false;
				movingPieceFrom = null;
			}
			
			// if cell is the human's then enable moving the pieces
			if ((cell.ableToAttack() &&
			    ((cell.getCity() != null ) && 
			    (cell.getCity().getPlayer() == human))))
			{
				citySelected = cell.getCity();
				for (Occupiers piece : Occupiers.moveablePieces)
				{
					if (cell.getOccupiers().ordinal() == piece.ordinal())
					{
						movingPiece = piece;
						movingPieceFrom = cell;
						Cursor customCursor = 
	Toolkit.getDefaultToolkit().createCustomCursor(Occupiers.ourImage[piece.ordinal()], new Point(0, 0), "customCursor");
						Toolkit.getDefaultToolkit().getBestCursorSize(64, 64);
						this.setCursor( customCursor );
					}

				}
			} // end of moving human pieces on the board
			// now check if human is buying a piece
			/*
			 * else if ((cell.getGeology() == Geology.WATER) && (cell.getOccupiers() !=
			 * Occupiers.NONE)) { buyingAPiece = true; movingPiece = cell.getOccupiers();
			 * movingPieceFrom = null; Cursor customCursor =
			 * Toolkit.getDefaultToolkit().createCustomCursor(Occupiers.ourImage[cell.
			 * getOccupiers().ordinal()], new Point(0, 0), "customCursor");
			 * Toolkit.getDefaultToolkit().getBestCursorSize(64, 64); this.setCursor(
			 * customCursor );
			 * 
			 * }
			 */

			repaint();
		} // if btn one and cell not null

	} // mouse pressed 
	
	@Override
	// mouseEntered is used to detect when the cursor is moved to the canvas area
	public void mouseEntered(MouseEvent e) {
// TODO ??
	} // end mouseEntered
	@Override
	public void mouseExited(MouseEvent e) 
	{

	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if (myMap != null)
		{
			Cell cell = findCellAt(e.getX(),e.getY());
			if ((cell != null) && (cell.getCity() != null))
			{
				currentCell = cell;
			}
			else
				currentCell = null;
		}
	}

	public void endTurn()
	{
		if (human != null)
		{
			this.citySelected = null;
			this.buyingAPiece = false;
			this.movingPiece = Occupiers.NONE;
			this.movingPieceFrom = null;
			this.multiPieceMove = false;
			this.setCursor( null );

			human.turnIsOver();
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) 
	{ 
		// this logic is for placing the selected army piece in the same
		// city as selected (either moving an existing piece or buying one)
		// TODO consider moving a lot if not all this logic to the City class
		Cell cell = findCellAt(e.getX(),e.getY());
		if ((cell != null) && (cell.getCity() != null) &&
		    (cell.getCity().equals(citySelected)))
		{
			currentCell = cell;
			// can't move it to a cell with an piece already there
			if (cell.getOccupiers() != Occupiers.NONE) 
			{
				
			}
			// target cell is available
			else 
			{
				// if human is buying a piece, go ahead and move it
				if (buyingAPiece)
				{
					citySelected.buyAnArmyPieceForCity(movingPiece, cell);
				}
				// else if human is moving it within the same city,...
				else if (movingPieceFrom != null)
				{
					if (this.multiPieceMove)
					{
						cell.setOccupiers(movingPiece);
						if (!citySelected.decrementMultiMovePieces())
						{
							movingPieceFrom = null;
							multiPieceMove = false;
						}
					}
					else
					{
						cell.setOccupiers(movingPiece);
						movingPieceFrom.setOccupiers(Occupiers.NONE);
					}
				}
			}
		}
		else
			currentCell = null;
		
		// check to see if human is attacking someone (movingPieceFrom !null)
		// cell needs to be not null and in the selected cities neighbor list
		// NOTE: if a piece can't attack, it can't be "picked up" so we don't need
		// to check for able to attack
		if ((cell != null) && 
		       ((movingPieceFrom != null) || 
			         (buyingAPiece && (movingPiece != Occupiers.CASTLE)))
			&& (citySelected != null) 
			&& (!citySelected.equals(cell.getCity())) && (citySelected.getNeighbors().contains(cell)))
		{
			// get defense value, compare to moving piece attack value
			if (cell.getDefense() < movingPiece.getValue())
			{

				// tell enemy player their city lost a cell.
				// player will adjust the status of it's city accordingly.

				City city = cell.getCity();
				if (city != null)
				{
					city.getPlayer().cityLostCell(city,cell);
					city.getPlayer().determineCityConnectivity(city);
				}
				
				cell.setCity(citySelected);
				
				cell.setBackground(citySelected.getPlayer().getColor());
				citySelected.add(cell);
				if (movingPieceFrom != null)
					movingPieceFrom.setOccupiers(Occupiers.NONE);
				
				if (buyingAPiece)
				{
					// buyAnArmyPieceForCity expects the cell to be empty
					cell.setOccupiers(Occupiers.NONE);
					citySelected.buyAnArmyPieceForCity(movingPiece, cell);
				}
				else // buyAnArmyPieceForCity handles this so do it if not buying
				{
					cell.setOccupiers(movingPiece);
					if (this.multiPieceMove)
					{
						if (!citySelected.decrementMultiMovePieces())
						{
							movingPieceFrom = null;
							multiPieceMove = false;
						}
					}

				}
				
				// can't move this piece again this turn since I attacked with it
				human.addToAttackingPieces(cell);

				
				// check if new cell is near another allied city
				if (cell.getAlliedNeighbors().size() > 0)
				{
					// cancel multi piece move in case city gets eliminated
					if (this.multiPieceMove)
						citySelected.cancelMultiMovePiece();
					
					human.combineAdjacentCities();
					// just in case the selected city was combined and lost...
					// use the city the taken cell is associated with cause/
					// it will either be the citySelected or the city it was combined with
					
					citySelected = cell.getCity();
					if (this.multiPieceMove)
					{
						citySelected.setMultiMovePiece(this.movingPiece);
					}

				}

				citySelected.calculateEdges(colOffset, rowOffset);
			} // can human take enemy occupier
		}

		repaint();
		
		if (!this.multiPieceMove)
		{
			movingPiece = Occupiers.NONE;
			movingPieceFrom = null;
			this.setCursor( null );
		}
		buyingAPiece = false;

	} // end mouseReleased
	
	@Override
	public void mouseDragged(MouseEvent e) 
// TODO is this where I need to draw the piece being moved?
	{ 
	
	} // end mouseDragged

	public void newGame()
	{
		myMap = null;
		myKeepRunning = true;
	}
  
	public void resetWorld()
	{
		
		myKeepRunning = false;
		for (int i = 0; myMap != null && i < rows; i++)
		{
			for (int j = 0; j< cols; j++)
			{
				myMap[i][j].setGeology(Geology.WATER);
				myMap[i][j].setBackground(Color.BLUE);
				myMap[i][j].setCity(null);
				myMap[i][j].setOccupiers(Occupiers.NONE);
				citySelected = null;
				currentCell = null;
			}
		}
		repaint();
	}
	
	public void saveGame(ObjectOutputStream out) throws IOException
	{
        // Method for serialization of object 
        out.writeInt(rows);
        out.writeInt(cols);
        out.writeInt(density);
        out.writeInt(difficulty);
        out.writeInt(rowOffset);
        out.writeInt(colOffset);
        out.writeInt(myPlayers.size());
        out.writeInt(humanIndex);
        out.writeInt(human.currentTurnCount());
        
        // can't save the whole world because it is 
        // runnable and it is too complicated to
        // drop existing world and reconnect.
        // so we only save the map (all cells)
        for (int i = 0; i < rows; i++)
        	for (int j = 0; j < cols; j++)
        	{
        		out.writeObject(myMap[i][j]);
        	}
       
        
        for (int i = 0; i < myPlayers.size(); i++)
        {
        	Player player = myPlayers.get(i);
        	out.writeObject(player.getColor());
        	out.writeObject(player.getColorName());
        	
        	// rebuild player's cities
        	int count = player.getCities().size(); // city count
        	out.writeInt(count);
        	for (int a = 0; a < count; a++)
        	{
        		City city = player.getCities().get(a);
        		out.writeObject(city); // save non-reference items
        		
        		city.saveCity(out); // save reference items
        	}
        	
        } // end loop through players to save their cities
	} // end saveGame
	
	public void loadGame(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		myKeepRunning = false; // stop game if not already stopped
		citySelected = null;
		currentCell = null;
		movingPieceFrom = null;
		buyingAPiece = false;
		movingPiece = Occupiers.NONE;
		multiPieceMove = false;

		
        rows = in.readInt();
        cols = in.readInt();
        density = in.readInt();
        difficulty = in.readInt();
        rowOffset = in.readInt();
        colOffset = in.readInt();
        int playerCount = in.readInt();
        humanIndex = in.readInt();
		whosTurnItIs = humanIndex;
		int turn = in.readInt(); // don't have a human yet so store here temporarily

		myMap = new Cell[rows][cols];
    	for (int r = 0; r < rows; r++)	
    		for (int c = 0; c < cols; c++)
    		{
    			myMap[r][c] = (Cell)in.readObject();
    			myMap[r][c].computeArea();
    		}
    	
        // now World has everything it needs to rebuild
        
        myPlayers = new ArrayList<Player>();
        for (int i = 0; i < playerCount; i++)
        {
        	Player player = null;
        	if (i == humanIndex)
        	{
        		human = new HumanPlayer(false,false);
        		player = human;
        		human.currentTurnCount(turn);
        	}
        	else
        		player = new ComputerPlayer(difficulty);
        	myPlayers.add(player);
        	Color color = (Color)in.readObject();
        	player.setColor(color);
        	String colorName = (String)in.readObject();
        	player.setColorName(colorName);
        	
        	
        	// rebuild player's cities
        	int count = in.readInt(); // city count
        	for (int a = 0; a < count; a++)
        	{
        		City city = (City)in.readObject(); // load non-reference items
        		city.loadCity(in, myMap); // loads reference items
        		city.calculateEdges(colOffset,rowOffset);
        		
        		// associate city with this player
        		player.getCities().add(city);
        		city.setPlayer(player);
        	}
        	
        }
        
        // rebuild everything that depends on Cells
        myPlayableMap = new ArrayList<Cell>();
    	for (int r = 0; r < rows; r++)	
    		for (int c = 0; c < cols; c++)
    		{
    			Cell cell = myMap[r][c];
    			if (cell.getGeology() != Geology.WATER)
    				myPlayableMap.add(cell);
    			else
    				// in case they saved with a selected city
    				// need to clear the army pieces that are 
    				// available to buy
    				cell.setOccupiers(Occupiers.NONE);
    			
    			// rebuild reference data for each cell
    			setNeighbors(r,c);
    		}

	}
  
	@Override
	public void run() 
	{
		while (true)
		{
			if (myKeepRunning)
			{
				if (myMap == null) // if we haven't created a world yet, do so now.
				{
					myPlayableMap = new ArrayList<Cell>();
					buildMap(rows, cols, density);
					humanIndex = (int)(Math.random()*numPlayers);
					assignPlayers(numPlayers, humanIndex);
				}
			
				do 
				{
				repaint(); // force a repaint of canvas
				takeTurn();
				
				} while (myKeepRunning);
			} 
			else
			synchronized(this)
			{
				try {
					wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
} // end run
	
	
	public void buildMap(int row, int col, int landDensity)
	{
		// first pass builds an oval shaped island
		myMap = new Cell[row][col];

		for (int i = 0; i < row; i++)
		{
			for (int j = 0; j< col; j++)
			{
				myMap[i][j] = createCell(i,j,landDensity);
				
				if (myMap[i][j].getGeology() != Geology.WATER)
				{
					myPlayableMap.add(myMap[i][j]);
				}
				else 
					myMap[i][j].setBackground(Color.BLUE);
			}
		}
		
		// TODO: second pass will build outward any areas of 4 or more cells
		// adjacent to each other on the coast line but nothing on the outer
		// edge of the map (0,?) or (?,0) or (max,0) or (0,max)
		
		// third pass will tell each cell who its neighbors are .
		// note that all playable cells will have 8 neighbors 
		for (int i = 0; i < row; i++)
		{
			for (int j = 0; j< col; j++)
			{
				Cell cell = myMap[i][j];
				if (cell.getGeology() != Geology.WATER) // no need to do this for water
				{
					setNeighbors(i,j);
				} // find neighbors for cells that aren't water
			}
		}		
	
		// TODO this pass will also convert any land cells to be coastal cells
		
		
	}
	private Cell createCell(int row, int col, int landDensity)
	{
		int maxRow = myMap.length;
		int maxCol = myMap[0].length;
		int centerX = maxRow/2;
		int centerY = maxCol/2;
//double distance = Math.sqrt( Math.pow((row-centerX),2) + 
//                          Math.pow((col-centerY), 2));		
		// if cell location is beyond land mass, it is a water cell
		Geology landType = Geology.WATER;
		Random numGen = new Random();
		int maxRowDistanceFromCenter = (int)(landDensity/100.0 * maxRow/2.0);
		int maxColDistanceFromCenter = (int)(landDensity/100.0 * maxCol/2.0);
//int rowDistanceFromCenter = Math.abs(row-centerX);
//int colDistanceFromCenter = Math.abs(col-centerY);

		
		// if near the edge, increase probability of water and zero mtns
		Ellipse2D.Double eggIsland = new Ellipse2D.Double
									(centerX-maxRowDistanceFromCenter, 
									 centerY-maxColDistanceFromCenter, 
									 maxRowDistanceFromCenter*2.0,
									 maxColDistanceFromCenter*2.0);
		 
		 int offset = (int)(Math.min(maxRow, maxCol)*0.10);
		 Ellipse2D.Double innerEggIsland = new Ellipse2D.Double
									(centerX-maxRowDistanceFromCenter+offset, 
									 centerY-maxColDistanceFromCenter+offset, 
									 (maxRowDistanceFromCenter-offset)*2.0,
									 (maxColDistanceFromCenter-offset)*2.0);
		 
		int densityOfWater = 10;
		int densityOfLand = 50;
		int densityOfForest = 20;
		
		if (eggIsland.contains(row,col,1,1))
		{
			if (!innerEggIsland.contains(row,col,1,1)) // along outer edge
			{
				densityOfWater = 50;
				densityOfLand = 50;
				densityOfForest = 0;				
			}
			int randomValue = numGen.nextInt(100);
			if (randomValue < densityOfWater)
				landType = Geology.WATER;
			else if (randomValue < densityOfLand+densityOfWater)
				landType = Geology.LAND;
			else if (randomValue < densityOfLand+densityOfWater+densityOfForest)
				landType = Geology.FOREST;
			else
				landType = Geology.MTN;
		}
		
		return new Cell(row,col,landType);
	}
	
	public void assignPlayers(int numberOfPlayers, int theHuman)
	{
		myPlayers = new ArrayList<Player>();
		
		@SuppressWarnings("unchecked")
		ArrayList<Cell> availableCells = (ArrayList<Cell>)myPlayableMap.clone();
		Player newPlayer;
//int avgNumCellsPerPlayer = 12-myDifficulty;
//int variance = 5-myDifficulty/2;
		
		for (int i = 0; i < numberOfPlayers; i++)
		{
			if (theHuman == (i))
			{
				human = new HumanPlayer();
				newPlayer = human;
			}
			else 
			{
				newPlayer = new ComputerPlayer(World.difficulty);
			}
			myPlayers.add(newPlayer);
							
			newPlayer.setColor(playerColors[i+1]);
			newPlayer.setColorName(playerColorNames[i+1]);

			// assign territory
			for (int j=0;j < 10; j++) // each player gets 10 cities
			{
				City city = buildThisCity(i, availableCells);
				newPlayer.addCity(city);
				city.addGold(city.size());
			}				
			newPlayer.combineAdjacentCities();
			
		} // create each player
	} // end assign player
	
	private City buildThisCity(int player, ArrayList<Cell> availableCells)
	{
/*
redesign:
find 10 random cells from available list
if it has < 2 cells in its neighbors list that are available, pick a new cell
make cell the capital and add 1 or 2 of its avail neighbors to the same city
once all cities are made, 

*/
		City city = new City();
		Cell branchCell = null;
		Random numGen = new Random();
		
		// establish capitol cell
		branchCell = findCapitol(availableCells);
		city.add(branchCell);
		branchCell.setCity(city);
		branchCell.setOccupiers(Occupiers.VILLAGE);
		branchCell.setBackground(playerColors[player+1]);
		availableCells.remove(branchCell);

		// add 1 or 2 more cells to the city from capitol's neighbors
		int count = numGen.nextInt(2) + 1;
		for (Cell c : branchCell.getNeighbors())
		{
			if (-1 != (availableCells.indexOf(c)))
			{
				count -= 1;	
				city.add(c);
				c.setCity(city);
				availableCells.remove(c);
				c.setBackground(playerColors[player+1]);
			}
			if (count <= 0)
				break;
		}

		return city;
	}
	
	private int countAvailNeighbors(Cell cell, ArrayList<Cell> availableCells)
	{
		// loop through cells around the cell ensure it isn't all
		// alone (i.e. surrounded by water or unavailable cells
		int count = 0;
		for (Cell c : cell.getNeighbors())
		{
			if (-1 != (availableCells.indexOf(c)))
				count += 1;
		}

		return count;
	}
	
	private Cell findCapitol(ArrayList<Cell> availableCells)
	{
		Cell capitol = null;
		while (capitol == null)
		{
			int index = (int)(availableCells.size()*Math.random());
			capitol = availableCells.get(index);
			int count = countAvailNeighbors(capitol, availableCells);
			if (count <2)
				capitol = null;
		}
		return capitol;
	}
	

	
	public void takeTurn()
	{
		// if numberOfPlayers is 1, then there is a winner
		if (myPlayers.size() == 1) 
		{
			if (myKeepRunning)
			{
				JOptionPane.showMessageDialog(null, "player " + myPlayers.get(0).getColorName() + " wins");
				myKeepRunning = false;
				this.multiPieceMove = false;
				this.movingPiece = Occupiers.NONE;
				this.movingPieceFrom = null;
				this.citySelected = null;
				
			}
			return;

		}
		if (whosTurnItIs >= myPlayers.size()) // start over with first player
			whosTurnItIs = 0;

		// only increments to the next player if current player returns true
		if (myPlayers.get(whosTurnItIs).takeTurn())
		{
			whosTurnItIs++;
			citySelected = null;

			int count = 0;
			
			// remove any players that have zero cities left
			boolean keepGoing = (myPlayers.size()> count);
			while (keepGoing)
			{
				Player player = myPlayers.get(count);

				if (player.getCities().size() == 0)
				{
					myPlayers.remove(player);
				}
				else 
					count+= 1;
				keepGoing = (myPlayers.size()> count);
			}	
		}
		
		repaint();
	

		// if total defense of all computer is < 25% of human's then surrender
	}
	
	private void setNeighbors(int row, int col)
	{
		ArrayList<Cell> neighbors = new ArrayList<Cell>();
		Cell cell = myMap[row][col];
		
		for (int a = 0; a < 3; a++) {
			for (int b = 0; b < 3; b++) {
				// catch and ignore any out of bounds exceptions as this 
				// indicates we are looking for neighbors outside the mapped area
				try {
					// don't add the cell as its own neighbor
					if (!(a == 1 && b == 1))
					{
						// don't add water cells as neighbors either
						Cell n = myMap[row-1+a][col-1+b];
						if (n.getGeology() != Geology.WATER)
							neighbors.add(n);
					}
				}
				catch (Exception e)
				{}
			}
		}
		cell.setNeighbors(neighbors);
		
		// Find far neighbors (cells one past neighbors)
		ArrayList<Cell> farNeighbors = new ArrayList<Cell>();
		
		for (int a = 0; a < 5; a++) {
			for (int b = 0; b < 5; b++) {
				// catch and ignore any out of bounds exceptions as this 
				// indicates we are looking for neighbors outside the mapped area
				try {
					// don't add the cell as its own neighbor
					if (!(a == 1 && b == 1))
					{
						// don't add water cells or neighbors either
						Cell n = myMap[row-2+a][col-2+b];
						if ((n.getGeology() != Geology.WATER) &&
							(!neighbors.contains(n)))
							farNeighbors.add(n);
					}
				}
				catch (Exception e)
				{}
			}
		}
		cell.setFarNeighbors(farNeighbors);
	}
}
 