import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class City extends ArrayList<Cell>
{
	//private ArrayList<Cell> myLand = new ArrayList<Cell>();
	private ArrayList<Cell> myNeighbors = new ArrayList<Cell>();
	private ArrayList<Occupiers> myArmy = new ArrayList<Occupiers>(); // army
	private int myGoldReserve = 0;
	private Player myPlayer = null;
	private Area myArea = new Area();
	
	public City()
	{
	}
	public City(ArrayList<Cell> city)
	{
		//myLand = city;
		for (Cell cell : city)
			this.add(cell);
	}	
	
	public int currentGoldValue()
	{
		return myGoldReserve;
	}
	public void addGold(int gold)
	{
		myGoldReserve += gold;
	}	
	
	public Player getPlayer()
	{
		return myPlayer;
	}
	public void setPlayer(Player p)
	{
		myPlayer = p;
	}
	
	public int goldGeneratedEachTurn()
	{
		//return myLand.
		return size();
	}
	
	public int goldConsumptionEachTurn()
	{
		int total = 0;
		for (int i = 0; i < myArmy.size(); i++)
			total += myArmy.get(i).getConsumption();
		return total;
	}
	
	public void processGold()
	{
		myGoldReserve += goldGeneratedEachTurn() - goldConsumptionEachTurn();
		if (myGoldReserve < 0) // lose my army if I'm out of gold
		{
			makeArmyAvailable(); // pull them from cells
			myArmy = new ArrayList<Occupiers>();
		}
		
	}
	
	public boolean armyPieceAffordable(Occupiers piece)
	{
		boolean canAffordIt = false;
		// if I have enough gold to buy the piece
		// AND I'll have enough to cover all costs next turn
		if (( myGoldReserve >= piece.getCost()) &&
			((piece.getConsumption() + goldConsumptionEachTurn()) < 
						(goldGeneratedEachTurn() + (myGoldReserve-piece.getCost()))))
		{
			canAffordIt = true;
		}
		
		return canAffordIt;
	}
	
	public boolean buyAnArmyPieceForCity(Occupiers piece, Cell c)
	{
		boolean canAffordIt = armyPieceAffordable(piece);
		if (canAffordIt && (c.getOccupiers() == Occupiers.NONE))
		{
			myGoldReserve -= piece.getCost();
			c.setOccupiers(piece);
			myArmy.add(piece);
			// don't need to do anything with the old piece???
		}
		else
			canAffordIt = false;
		
		return canAffordIt;
	}

	
	public ArrayList<Cell> getNeighbors()
	{
		findMyNeighbors();
		return myNeighbors;
	}
	
	// find all the cells adjacent to this city's cells (may or may not
	// belong to the same player)
	private void findMyNeighbors()
	{
		ArrayList<Cell> n = new ArrayList<Cell>();
		for (Cell cell : /* myLand */this)
		{
			// some cells have the same neighbors so don't add the same one twice
			// also, don't add allied cells (cells owned by the same player)
			// NOTE: getNeighbors at the cell level returns all 8 neighbors
			// regardless of color or city affiliation
			ArrayList<Cell> cn = cell.getNeighbors();
			for (Cell neighbor : cn) 
			{
				// every city belongs to a player so I belong to a player but I 
				// need to check to see if the neighbor belongs to a city and
				// if that city isn't me.
				// and that I haven't added this neighbor already
				// NOTE: this mean I could return neighbors who belong to the 
				// same player but just in a different city
				if ((neighbor.getCity() != null) &&  
				    (neighbor.getCity() != this) &&
				    (!n.contains(neighbor)))
					n.add(neighbor);
				// if neighbor isn't in a city and I don't have it in the list
				// already, add it
				else if ((neighbor.getCity() == null) && 
						(!n.contains(neighbor)))
					n.add(neighbor);
			}
		}
		myNeighbors = n;
	}
	
	// returns only the neighbor cells that are the same color but not in
	// this city (may or may not be part of a city)
	public ArrayList<Cell> findMyAlliedNeighbors()
	{
		ArrayList<Cell> n = new ArrayList<Cell>();
		for (Cell cell : /* myLand */this)
		{
			// getAlliedNeighbors returns neighbors of cell that are 
			// the same color (but NOT the same city)
			ArrayList<Cell> cn = cell.getAlliedNeighbors();
			for (Cell neighbor : cn) 
			{
				if (!n.contains(neighbor))
					n.add(neighbor);
			}
		}
		return n;
	}
	
	public ArrayList<Occupiers> getArmy()
	{
		return myArmy;
	}
	public ArrayList<Occupiers> makeArmyAvailable()
	{
		ArrayList<Occupiers> moveablePieces = new ArrayList<Occupiers>();
		for (Cell cell : /* myLand */this)
		{
			Occupiers piece = cell.getOccupiers();
			if ((piece.ordinal() != Occupiers.CASTLE.ordinal()) &&
			    (piece.ordinal() != Occupiers.VILLAGE.ordinal()))
			{
				cell.setOccupiers(Occupiers.NONE);
				moveablePieces.add(piece);
			}
		}
		return moveablePieces;
	}
	
		
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		else if (obj.getClass() != this.getClass())
			return false;
		City city = (City)obj;
		return (this.myGoldReserve == city.myGoldReserve) && (this.myPlayer == city.myPlayer) && (this.myArmy == city.myArmy);
	}
	
	public void calculateEdges(int height, int width)
	{
		myArea.reset();
		for (Cell c : this)
			myArea.add(c.getArea());
	}
	
	public Area getArea()
	{
		return myArea;
	}

	
}