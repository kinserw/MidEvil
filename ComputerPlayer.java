import java.util.*;
import java.awt.Color;

public class ComputerPlayer extends Player
{
	Random myNumGen = new Random();
	
	public ComputerPlayer()
	{
	}
	
	
	public boolean takeTurn()
	{
		int count = 0;
		boolean keepGoing = myCities.size() > 0;
		while (keepGoing)
		{
			City city = myCities.get(count);
			if (city.size() <2) 
			{
				if (city.size() != 0)
				{
					city.get(0).setCity(null);
					city.get(0).setOccupiers(Occupiers.NONE);
					city.get(0).setBackground(Color.GREEN);
				}
				city.setPlayer(null);
				myCities.remove(count);
			}
			else 
				count +=1;
			
			keepGoing =  (myCities.size() > count);
				
		}
		
		combineAdjacentCities(); 
		
		int randomNum = myNumGen.nextInt(100);

		if (randomNum > 50) 
			takeOffensiveTurn();
		else
		
			takeDefensiveTurn();
			
		return true;
	}
	
	private void takeOffensiveTurn()
	{
		// TODO loop through each city (offensive approach)
		Random numGen = new Random();
		for (City city : myCities)
		{
			city.processGold();
			
			// take all moveable pieces off their cells for redistribution
			ArrayList<Occupiers> availableArmy = city.makeArmyAvailable();
			
			// get list of neighbors (any cell nearby not in the same city)
			ArrayList<Cell> neighbors = city.getNeighbors();
			
			int count = 0;
			boolean keepGoing = neighbors.size() > 0;
			while (keepGoing)
			{
				// remove neighbors that are allies (belong to the same player but
				// not in the same city being evaluated)
				Cell c = neighbors.get(count);
				if ((c.getCity() != null) && (c.getCity().getPlayer() == this))
					neighbors.remove(count);
				else
					count++;
				keepGoing = (neighbors.size() > count);
			}

			
			// first, look at my existing army and take as many neighbors
			// as I can with what I have
			while ((availableArmy.size() > 0) && (neighbors.size()>0)) 
			{
				// find neighbors that I can attack from this city now
				// attack them
				int i = numGen.nextInt(neighbors.size());
				Cell neighbor = neighbors.get(i);
				for (Occupiers armyGuy : availableArmy)
				{
					if (armyGuy.getValue() > neighbor.getDefense())
					{
						// if neighbor is in another city, disconnect it 
						if (neighbor.getCity() != null)
						{
							City ncity = neighbor.getCity();
							ncity.getPlayer().cityLostCell(ncity, neighbor);
							ncity.getPlayer().determineCityConnectivity(ncity);
						}
						availableArmy.remove(armyGuy);
						neighbor.setOccupiers(armyGuy);
						city.add(neighbor);
						neighbor.setCity(city);
						neighbor.setBackground(getColor());
						neighbors.remove(neighbor);
						break;
					}
					
				} // loop through army guys until find one that can attack
				// if no one could take this neighbor, remove from list
				neighbors.remove(neighbor);
				
			} // loop ends when we have attacked every neighbor or ran out of 
			// army guys
			
			// get a new list because we may have captured some
			neighbors = city.getNeighbors();
			count = 0;
			keepGoing = neighbors.size() > 0;
			while (keepGoing)
			{
				// remove neighbors that are allies (belong to the same player but
				// not in the same city being evaluated)
				Cell c = neighbors.get(count);
				if ((c.getCity() != null) && (c.getCity().getPlayer() == this))
					neighbors.remove(count);
				else
					count++;
				keepGoing = (neighbors.size() > count);
			}
			
			boolean canStillAttack = neighbors.size() > 0;

			int consumption = city.goldConsumptionEachTurn();
			
			// loop back and keep attacking neighbors by buying the 
			// necessary pieces if I can until I can't
			while (canStillAttack) 
			{
				// find weakest neighbors, build army up to attack them if possible (making sure that i don't have an army too big to support with the amount
				// of gold i produce each turn
				Cell weakestNeighbor = neighbors.get(0);
				for (Cell cell : neighbors) 
				{
					if (weakestNeighbor.getDefense() < cell.getDefense())
						weakestNeighbor = cell;
				}

				canStillAttack = false;
				// find the army piece needed to win this neighbor
				for (Occupiers piece : Occupiers.moveablePieces)
				{
					// find a piece if any that can take this neighbor
					// make sure I can afford it
					if ((piece.getValue() > weakestNeighbor.getDefense())
						&& (city.armyPieceAffordable(piece)))
					{
						// if neighbor is in another city, disconnect it 
						if (weakestNeighbor.getCity() != null)
						{
							City wcity = weakestNeighbor.getCity();
							wcity.getPlayer().cityLostCell(wcity, weakestNeighbor);
							wcity.getPlayer().determineCityConnectivity(wcity);
						}
						// I know I can afford it so just buy it
						city.buyAnArmyPieceForCity(piece,weakestNeighbor);
						city.add(weakestNeighbor);
						weakestNeighbor.setBackground(getColor());
						weakestNeighbor.setCity(city);
						neighbors.remove(weakestNeighbor);
						canStillAttack = true;
						break; // break out of looping through possible pieces
					} // piece can win and I can afford the piece
					
				} // loop through moveable pieces
				if ((neighbors.size() == 0) ||
				    (!city.armyPieceAffordable(Occupiers.PEON)))
					canStillAttack = false;
			} // can still attack
		
			
			// place any remaining army units this city has on empty cells
			boolean moreCells = true;
			while (availableArmy.size() > 0 && moreCells)
			{
				for(Cell c : city)
				{
					if (c.getOccupiers() == Occupiers.NONE)
					{
						c.setOccupiers(availableArmy.get(0));
						availableArmy.remove(0);
						break;
					}
					if (c == city.get(city.size() -1))
						moreCells = false;
				}

			} // place remaining army units
			
		} // loop through cities

	}
	
	private void takeDefensiveTurn()
	{

		Random numGen = new Random();
		for (City city : myCities)
		{
			city.processGold();
			
			// take all moveable pieces off their cells for redistribution
			ArrayList<Occupiers> availableArmy = city.makeArmyAvailable();
			int consumption = city.goldConsumptionEachTurn();
			
			// get list of cells that have 5 or more allied neighbors
			ArrayList<Cell> centerCells = new ArrayList<Cell>();
			// processing for castles if I can afford at least 1
			if (city.currentGoldValue() > Occupiers.CASTLE.getValue())
			{
				for (Cell cell : city)
				{
					ArrayList<Cell> allies = cell.getCityNeighbors();
					
					// if several allies nearby and doesn't already have a castle 
					// and isn't guarded by a castle nearby
					if ((allies.size() >= 4) && cell.getDefense() < Occupiers.CASTLE.getValue())
					{
						// add this as a center city but order them lowest defense first
						int i = 0;
						for (i = 0; i < centerCells.size(); i++)
						{
							if (cell.getDefense() < centerCells.get(i).getDefense())
								break;
						}
						centerCells.add(i, cell);
					} // is it a center city
				} // find all the center cities
				
				// now, add castles to the lowest defended cities at random until
				// out of money, or run out of center cities to defend (since 
				// we only want to defend the weakest, we only pull from the 
				// first 75% of the list which means, we'll always have at least 1 left)
				while ( city.currentGoldValue() > Occupiers.CASTLE.getCost()
					    && centerCells.size() > 1)
				{
					int i = numGen.nextInt((int)(centerCells.size()*0.75));
					city.buyAnArmyPieceForCity(Occupiers.CASTLE, centerCells.get(i));
					centerCells.remove(i);
				}
			
			} // end processing for castles
			
			// expanding definition of centerCity so forget any left from 
			// castle processing
			centerCells = new ArrayList<Cell>();
			
			// no need to adjust consumption #s because castles don't cost anything
			// to maintain

			ArrayList<Occupiers> pikemen = new ArrayList<Occupiers>();
			for (Occupiers piece : availableArmy)
			{			
				if (piece.ordinal() == Occupiers.PIKEMAN.ordinal())
					pikemen.add(piece);
			}
			
			// processing for pikemen, if I can afford at least 1
			// and am able to maintain it with existing army
			if (( pikemen.size() > 0) ||
			    ((city.currentGoldValue() > Occupiers.PIKEMAN.getCost() &&
				(consumption + Occupiers.PIKEMAN.getConsumption())  <= city.                                      goldGeneratedEachTurn())))
			{
				for (Cell cell : city)
				{
					ArrayList<Cell> allies = cell.getCityNeighbors();
					if (allies.size() >= 3)
					{
						// add this as a center city but order them lowest defense first
						int i = 0;
						for (i = 0; i < centerCells.size(); i++)
						{
							if (cell.getDefense() < centerCells.get(i).getDefense())
								break;
						}

						centerCells.add(i, cell);
					} // is it a center city
				} // find all the center cities
				
				// place all pikemen on center cells until we run out of one or the 
				// other
				for (int i = 0; (i < pikemen.size()) && (centerCells.size() > 0); i++)
				{
					Cell c = centerCells.remove(0);
					
					// only place the pikeman if the center cell isn't occupied
					if (c.getOccupiers() == Occupiers.NONE)
					{
						Occupiers pikeman = pikemen.get(0);
						c.setOccupiers(pikeman);
						availableArmy.remove(pikeman);
						pikemen.remove(pikeman);
					}
					// go ahead and remove it even if pikeman not placed, so we 
					// don't end up in an infinite loop
					centerCells.remove(c);
					
				}

				// now, add pikeman to the lowest defended cities at random until
				// out of money, or run out of center cities to defend (since 
				// we only want to defend the weakest, we only pull from the 
				// first 75% of the list which means, we'll always have 1 left)
				while ( city.currentGoldValue() > Occupiers.PIKEMAN.getCost()
					    && centerCells.size() > 0)
				{
					int i = 0; //numGen.nextInt((int)(centerCells.size()*0.75));
					city.buyAnArmyPieceForCity(Occupiers.PIKEMAN, centerCells.get(i));
					centerCells.remove(i);
				}
			
			} // end processing for Pikemen
			
			boolean moreCells = true;
			while (availableArmy.size() > 0 && moreCells)
			{
				for(Cell c : city)
				{
					if (c.getOccupiers() == Occupiers.NONE)
					{
						c.setOccupiers(availableArmy.get(0));
						availableArmy.remove(0);
						// if out of army pieces, end city loop
						if (availableArmy.size() == 0)
							break;
					}
				}
				moreCells = false;				
			}

// TODO : if enough gold left to buy peons and enough being generated to support peons
// then distribute them to the cells with the greatest number of enemy neighbors
		
		} // for each city
		

	}
	

	

}
