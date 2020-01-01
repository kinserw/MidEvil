import java.awt.*;

import java.io.File;
import java.io.Serializable;

import javax.imageio.ImageIO;

public enum Geology implements Serializable
{ 	
  	UNKNOWN, 
	WATER, 
	COAST, 
	LAND, 
	FOREST, 
	MTN;

	private Image myIcon;
	private Image myScaledIcon = null;
	private int myScaledWidth = 0;
	private int myScaledHeight = 0;

	static private char[] ourChar = 
	{'?', // unknown
	 '~', // water
	 '-', // coast
	 '_', // land
	 '#', // forest
	 '^' // mtn
	};	
	static private Image[] ourImage = 
	{null, // unknown
	 null, // water
	 null, // coast
	 null, // land
	 null, // forest
	 null // mtn
	};
	static private String[] ourIconFiles = 
	{"none.gif", // unknown
	 "water.png", // water
	 "none.gif", // coast
	 "land.png", // land
	 "forest.png", // forest
	 "mountain.png" // mtn
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

 	public Image getImage(int width, int height)
	{
    	if (this.myScaledIcon == null)
    	{
    		this.myScaledHeight = height;
    		this.myScaledWidth = width;
    		this.myScaledIcon = getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);    	
    	}
    	else if (myScaledHeight != height || myScaledWidth != width)
    	{
    		this.myScaledHeight = height;
    		this.myScaledWidth = width;
    		this.myScaledIcon = getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);    	
    	}
		return myScaledIcon;
	}

 	public Image getImage()
	{
		if (myIcon == null)
		{
			myIcon = ourImage[this.ordinal()];
		}
		return myIcon;
	}
	
	public char getChar()
	{
		return ourChar[this.ordinal()];
	}
  
    // enum constructor - cannot be public or protected 
    private Geology() 
    { 
		myIcon = null;
    } 

	public String toString()
	{
		return " " + ourChar[this.ordinal()] + " " ;
	}
	
} // end class Geology