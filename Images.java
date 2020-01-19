import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;

import com.kinser.midevilworld.*;


public class Images {

	// the following are static data/methods for icons/images for each Geology instance
	static private Image[] ourGeoImages = 
	{null, // unknown
	 null, // water
	 null, // coast
	 null, // land
	 null, // forest
	 null // mtn
	};
	

	static private String[] ourGeoImageFiles = 
	{"none.gif", // unknown
	 "water.png", // water
	 "none.gif", // coast
	 "land.png", // land
	 "forest.png", // forest
	 "mountain.png" // mtn
	};

    static public void loadGeoImages()
	{
       try
        {
			for (int i= 0; i < ourGeoImages.length ; i++)
			{
				ourGeoImages[i] = ImageIO.read(new File(ourGeoImageFiles[i]));
			}
		}
		catch (Exception e)
		{
		}
        		
	}


 	static public Image getImage(Geology geo)
	{
 		int index = geo.ordinal();

		return ourGeoImages[index];
	}
 	
 	//the following are static data/methods for images/icons of Occupier instances
	static public Image[] ourOccupierImages = 
	{null,
	 null, // peon
	 null, // village
	 null, // pikeman
	 null, // castle
	 null, // knight
	 null // general
	};
	
	static public String[] ourOccupierImageFiles = 
	{"none.gif",
	 "peon.png", // peon
	 "village.png", // village
	 "pikeman.png", // pikeman
	 "castle.png", // castle
	 "knight.png", // knight
	 "general.png" // general
	};	


    static public void loadOccupierImages()
	{
       try
        {
			for (int i= 0; i < ourOccupierImages.length ; i++)
			{
				ourOccupierImages[i] = ImageIO.read(new File(ourOccupierImageFiles[i]));
			}
		}
		catch (Exception e)
		{
		}
        		
	}
    


 	static public Image getImage(Occupiers piece)
	{
 		int index = piece.ordinal();

		return ourOccupierImages[index];
	}
 	
 	static public Image getImage(Cell cell)
	{
		Image i = Images.getImage(cell.getGeology());
		if (cell.getOccupiers().ordinal() > Occupiers.NONE.ordinal())
		{
			i = Images.getImage(cell.getOccupiers());
		}
		return i;	
	}
}
