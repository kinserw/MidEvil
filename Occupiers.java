
import java.awt.*;

import java.io.File;
import java.io.Serializable;

import javax.imageio.ImageIO;

public enum Occupiers implements Serializable
{ 
    // This will call enum constructor with two 
    // int arguments for amount of gold consumed per turn & cost to buy
	// the order (or ordinal) position indicates strength/defense
	// e.g. spikeman is stronger than peon but weaker than castle or knight
  	NONE(0,0), 
	PEON(2,6), 
	VILLAGE(0,0), 
	PIKEMAN(4,12), 
	CASTLE(0,20), 
	KNIGHT(10,26), 
	GENERAL(20,40);
	
	static public Occupiers[] placeablePieces = 
	{PEON,
	 PIKEMAN,
	 CASTLE,
	 KNIGHT,
	 GENERAL};	
	
	static public Occupiers[] moveablePieces = 
	{PEON,
	 PIKEMAN,
	 KNIGHT,
	GENERAL};

    // declaring private variable for getting values 
    private int myConsumption;
	private int myCost;
	private Image myIcon;
	static public Image[] ourImage = 
	{null,
	 null, // peon
	 null, // village
	 null, // pikeman
	 null, // castle
	 null, // knight
	 null // general
	};
	static public String[] ourIconFiles = 
	{"none.gif",
	 "peon.png", // peon
	 "village.png", // village
	 "pikeman.png", // pikeman
	 "castle.png", // castle
	 "knight.png", // knight
	 "general.png" // general
	};	
	static private int[] ourValue = 
	{0,
	 1, // peon
	 2, // village
	 6, // pikeman
	 10, // castle
	 16, // knight
	 32 // general
	};	
	static public char[] ourChar = 
	{' ',
	 'p', // peon
	 'v', // village
	 '!', // pikeman
	 'C', // castle
	 'K', // knight
	 'G' // general
	};	

    static public void getIcons()
	{
       try
        {
			for (int i= 0; i < ourImage.length ; i++)
			{
            ourImage[i] = ImageIO.read(new File(ourIconFiles[i]));
			}
		}
		catch (Exception e)
		{
		}
        		
	}

    // getter method 
    public int getCost() 
    { 
        return this.myCost; 
    } 
	public int getConsumption()
	{
		return this.myConsumption;
	}
	public Image getImage()
	{
		if (myIcon == null)
		{
			myIcon = ourImage[this.ordinal()];
		}
		return myIcon;
	}
  
    // enum constructor - cannot be public or protected 
    private Occupiers(int consumption, int cost) 
    { 
        this.myCost = cost; 
		this.myConsumption = consumption;
		myIcon = null;
    } 

	public int getValue()
	{
		return ourValue[this.ordinal()];
	}


	public String toString()
	{
		return " " + this.ordinal() + " " ;
	}
	
} 