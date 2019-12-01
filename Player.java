import java.util.*;
import java.awt.Color;

public class Player
{
	protected ArrayList<City> myCities;
	protected Color myColor;
	
	public Player()
	{
		myCities = new ArrayList<City>();
	}
	public ArrayList<City> getCities()
	{
		return myCities;
	}
	public void addCity(City city)
	{
		if (!myCities.contains(city))
		{
			myCities.add(city);
			city.setPlayer(this);
		}
	}	
	public void addCity(ArrayList<Cell> city)
	{
		addCity(new City(city));
	}
	public City findCityWithCell (Cell cell)
	{
		City c = cell.getCity();
		if (c != null)
		{
			if (c.getPlayer() != this)
				c = null; // not my city
		}
		return c;
	}
	public void removeCity(City city)
	{
		myCities.remove(city);
	}
	
	public void setColor(Color clr)
	{
		myColor = clr;
	}
	public Color getColor()
	{
		return myColor;
	}
	
	
	// returns true if done with turn (this is needed for the human players)
	public boolean takeTurn()
	{
		return true;
	}
	
	
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		else if (obj.getClass() != this.getClass())
			return false;
		Player p = (Player)obj;
		return this.myColor == p.myColor;
	}	
	
	public void combineAdjacentCities()
	{
/*
	iterate through each city
		iterate through the cities neighbors
		if a neighbor is a member of another city, merge them together (removing village that appears later in the collection in order to avoid messing up the loop through the collection)
*/
		int index = 0;
		boolean keepGoing = (getCities().size() > index);
		while (keepGoing)
		{
			City city = getCities().get(index);
			
			// findMyAlliedNeighbors only returns cells that are the same color
			// but belong to a different city or no city at all
			for(Cell cell : city.findMyAlliedNeighbors())
			{
				City otherCity = findCityWithCell(cell);
				//Note: if this cell is part of another city, that city 
				// must come later in the collection otherwise, or that city
				// would have identified this city as a neighbor and already
				// merged
				// Note: city might have multiple cells touching another
				// city which can lead to a situation where findCityWithCell
				// returns this city instead of another one because it already/
				// merged so check that otherCity isn't me

				if ((null != otherCity) && 
					(otherCity != city))
				{
					city.addGold(otherCity.currentGoldValue());
					while (otherCity.size() >= 1)
					{
						// move each cell from otherCity to this city
						Cell c = otherCity.remove(0);
						city.add(c);
						
						// no need to keep otherCity village
						if (c.getOccupiers() == Occupiers.VILLAGE)
							c.setOccupiers(Occupiers.NONE);
						// transfer otherCity army units to city army
						else if ((c.getOccupiers() != Occupiers.NONE) &&
								(c.getOccupiers() != Occupiers.CASTLE))
							city.getArmy().add(c.getOccupiers());
							
						// tell cell that it belongs to different city now
						c.setCity(city);
						// note: cell's background color already set
						
					}
					// other city should be empty now so remove it 
					getCities().remove(otherCity);
					
					// now that cities are merged, redraw perimeter
					city.calculateEdges(World.colOffset, World.rowOffset);
				}
				else if (null == otherCity) // then this is just an unaffiliated cell
				{
					city.add(cell);
					cell.setCity(city);
					cell.setOccupiers(Occupiers.NONE);
					// cell's background color already set
					city.calculateEdges(World.colOffset, World.rowOffset);
				}
				// else otherCity must be this city so do nothing
			}// loop through neighbors
			index += 1;
			keepGoing = (getCities().size() > index);
		}// loop through cities
	}// merge cities

	public void cityLostCell(City city, Cell cell)
	{
		// quick validation of parameters in to ensure they are mine
		if (city == null || cell == null)
			return;
		// this is not my city
		if (!myCities.contains(city))
			return;
		
		if (city != cell.getCity())
		{
			System.out.println("Cell passed in does not belong to city passed in");
			return;
		}

		// remove cell from city right away so we don't double count
		city.remove(cell);

		// remember lostPiece for later processing if city continues to exist
		Occupiers lostPiece = cell.getOccupiers();
		cell.setOccupiers(Occupiers.NONE);
		cell.setBackground(Color.GREEN);
		
		// does city only have one cell or less?
		if (city.size() < 2)
		{
			cell.setCity(null);
			if (city.size() == 1)
			{
				city.get(0).setCity(null);
				city.get(0).setOccupiers(Occupiers.NONE);
				city.get(0).setBackground(Color.GREEN);
				city.remove(city.get(0));
			}
			myCities.remove(city);
			cell.setCity(null);
			return;
		} // city can't exist with only one cell
		
		// don't need to do the rest of this if city doesn't exist
		
		// relocate village if taken
		if (lostPiece == Occupiers.VILLAGE)
		{
			// lose all gold when village taken
			city.addGold(-1 * city.currentGoldValue()); 
			findNewVillage(city);
		}
		// if lost cell had an army piece on it, remove this from city's army
		else if (lostPiece != Occupiers.NONE)
		{
			cell.getCity().getArmy().remove(cell.getOccupiers());
		}		
		cell.setCity(null);
	} // city lost cell
	
	public void determineCityConnectivity(City city)
	{

		if (city == null || city.size() < 2)
			return;
		
		// remove any cells that are all by themselves.
		int index = 0;
		for (index = 0; index < city.size(); index++)
		{
			Cell cell = city.get(index);
			ArrayList<Cell> cells = cell.getCityNeighbors();
			if (cells.size() == 0)
			{ // then this cell is isolated and should be removed
				cityLostCell(city,cell);
				index--; // redo this index now that a different cell is there
			}
		}
		
		// if this left the city with zero cells, remove it from player list
		if (city.size() == 0)
		{
			myCities.remove(city);
			return;
		}
		
		// if the city is disjointed, then picking any cell at random and building
		// a list of all cells connected to that cell will result in the new list
		// being of a different size than the original city list.
		
		
		// pick any cell in the city
		// add it to a new list
		City newCity = new City();
		newCity.add(city.get(0));
		int newCityIndex = 0;
		
		// find all its allied neighbors (that aren't already in the new list)
		// if 1 or more, 
			// add them to the list, 
			// take next cell in new list 
			// and try again
		//NOTE: at this point, all cells think they are part of the original 
		// city even if disjointed (and that's okay for now)
		while (newCityIndex < newCity.size())
		{
			ArrayList<Cell> cells = newCity.get(newCityIndex).getCityNeighbors();

			for(Cell cell : cells)
			{
				if (!newCity.contains(cell))
					newCity.add(cell);
			}
			newCityIndex++;
		}
		System.out.println("new city size = " + newCity.size() + "  and city is " + city.size());
		
		//once no more allied neighbors can be found and all the cells in the new
		//          list have been checked, compare #in new list to #in original city
		// if the # is same then it is not disjointed.
		// if # is different, take new list and make it its own city (add village, etc)
		// Take all cells in new list out of original city list
		// start over at the beginning (recursive call?)
		if (newCity.size() == city.size())
			return;
		
		if (newCity.size() == 1)
		{ 
			cityLostCell(city,newCity.get(0));
			newCity.remove(0);
		}
		
		// only gets here if city was disjointed
		boolean villageFound = false;
		for (Cell cell : newCity)
		{
			city.remove(cell);
			cell.setCity(newCity);
			
			// if cell has village keep it for newCity but remember to find a 
			// new village for the original city later
			if (cell.getOccupiers() == Occupiers.VILLAGE)
				villageFound = true;
			
			// transfer army units to newCity
			else if ((cell.getOccupiers() != Occupiers.NONE) && 
					(cell.getOccupiers() != Occupiers.CASTLE))
			{
				newCity.getArmy().add(cell.getOccupiers());
				city.getArmy().remove(cell.getOccupiers());
			}
		}
		
		// if resulting original city has only 1 cell, remove it from player list
		if (city.size() == 1)
		{
			cityLostCell(city,city.get(0));
			myCities.remove(city);
		}
		
		if (newCity.size() > 0)
		{
			newCity.setPlayer(this);
			myCities.add(newCity);		
		}

		// NOTE: original city gets to keep all the gold accumulated thus far
		if (villageFound && (city.size() > 0))  // village is in newCity, not here so add one here
		{
			findNewVillage(city);
		}
		
		// newcity is either empty or complete at this point.
		// now, recursively call this method again to check for 
		// multiple disjoint cities (i.e. it is possible that a single move 
		// could cause the original city to break up into 3 or more parts
		// especially if it was large.
		if (city.size() > 1) 
			determineCityConnectivity(city);
	} // end determineCityConnectivity
	
	private void findNewVillage(City city)
	{
		boolean villagePlaced = false;
		for (int index = 0;!villagePlaced && index < city.size(); index++)
		{
			if (city.get(index).getOccupiers() == Occupiers.NONE)
			{
				villagePlaced = true;
				city.get(index).setOccupiers(Occupiers.VILLAGE);
			}
		}
		// if village not placed then all cells occupied, delete occupier
		// in first cell and place village there
		if (!villagePlaced)
		{
			city.getArmy().remove(city.get(0).getOccupiers());
			city.get(0).setOccupiers(Occupiers.VILLAGE);
		}
		
	}

	
	public void debugDump()
	{
			System.out.println("My background color is " + myColor +  " and I have " + myCities.size() + " cities ");
			int count = 0;
		for (City city : myCities)
		{
			System.out.println("city # " + count + " has " + city.size() + " cells and " + city.getArmy().size() + " armies");
			count++;
			for (Cell cell : city)
			{
				if (cell.getBackground() != myColor) {
					System.out.println("cell at " + cell.getRow() + ", " + cell.getCol() + " has the wrong color " + cell.getBackground());
				    System.out.println(" cell thinks its in the right city : " + (cell.getCity().equals(city)));
				}
				
			}
		}
	}

}
