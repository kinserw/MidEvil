import javax.swing.*;
import java.awt.Color;
import java.util.*;


public class HumanPlayer extends Player
{
	private boolean myTurnIsOver = false;
	private boolean myFirstTimeThru = true;
	private ArrayList<Cell> myAttackingPieces = new ArrayList<Cell>();
	
	public HumanPlayer()
	{
	}
	
	public void turnIsOver()
	{
		myTurnIsOver = true;
	}
	
	public boolean takeTurn()
	{
		// takeTurn gets called over and over until I return true to the caller
		// which allows the human to do all the mouse movements needed in their 
		// turn.
		// once they end their turn, it will set myTurnIsOver to true again
		// so that the next time it is their turn, all the code in this block
		// will be executed (but it doesn't need to run over and over while its
		// their turn.
		if (myFirstTimeThru) 
		{
		
			combineAdjacentCities();
			resetAttackingPieces();
			int count = 0;
			
			// loop through all cities, remove empty ones and process gold 
			boolean keepGoing = myCities.size() > 0;
			while (keepGoing)
			{
				City city = myCities.get(count);
				if (city.size() >1) 
				{
					city.calculateEdges(World.colOffset, World.rowOffset);
					city.processGold();
					count +=1;
				}
				else if (city.size() == 1)
				{
					// this will remove last cell and the city
					cityLostCell(city,city.get(0));
				}
				else // city must be empty
					myCities.remove(city);
				
				keepGoing =  (myCities.size() > count);
					
			}
			myFirstTimeThru = false;
			myTurnIsOver = false;

		} // if the first time takeTurn is called
		
	
	    try {
			Thread.sleep(500);
		}  // cause a pause
		catch (Exception exception) 
		{
			// do nothing for now

		} // end catch
		
		if (myTurnIsOver)
			myFirstTimeThru = true;
		return myTurnIsOver;
	}
	
	public ArrayList<Occupiers> whatICanBuy(City city)
	{
		ArrayList<Occupiers> buyable = new ArrayList<Occupiers>();
		if (myCities.contains(city) == false)
			return buyable;
		
		int gold = city.currentGoldValue();
		for (Occupiers piece : Occupiers.placeablePieces)
		{
			if (piece.getCost() < gold)
				buyable.add(piece);
		}
		return buyable;
	}
	public void resetAttackingPieces()
	{
		for (Cell cell : myAttackingPieces)
		{
			cell.ableToAttack(true);
		}
		myAttackingPieces.clear();
	}
	
	public void addToAttackingPieces (Cell cell)
	{
		cell.ableToAttack(false);
		myAttackingPieces.add(cell);
	}

}
