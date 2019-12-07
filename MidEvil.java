//import java.awt.geom.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

//import java.awt.event.*;

@SuppressWarnings("serial")
public class MidEvil extends JFrame
{
	public static World ourWorld;
	public static Thread ourThread;
	
	public static void main(String[] args)
	{

		
		if (args.length > 0)
		{
			World.rows = Integer.parseInt(args[0]);
			World.cols = Integer.parseInt(args[1]);
System.out.println( "system in : " + args[0] + "   " + args[1]);
			if (args.length > 2)
				World.density = Integer.parseInt(args[2]);
		}
		else
		{
			World.rows = 65;
			World.cols = 35;
			World.density = 85;
		}
		@SuppressWarnings("unused")
		MidEvil game = new MidEvil();
	
	}
	
	public MidEvil() 
	{
		// set up default operations and starting size/position of screen 
		setTitle("MidEvil");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(00,0);
		
		Occupiers.getIcons();
		Geology.getIcons();

		// World is runnable so, pass it to a thread once created
		ourWorld = new World();
		ourThread = new Thread (ourWorld);
		
		// the default content pane already has a border layout manager by default.
		// use that to add the intro panel (with buttons) and world (where the cells live)
		Container pane = getContentPane();
		pane.add(ourWorld, BorderLayout.CENTER);
		createMenuBar();
		pack();
		setSize((World.rows+1)*World.rowOffset,
				(World.cols+3)*World.colOffset);
		setVisible(true);
		setResizable(true);

		ourThread.start();
	}
	
	private boolean loadGame()
	{
		JFileChooser fileChooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("MidEvil files", "mid");
	    fileChooser.setFileFilter(filter);

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// load from file
		  	try 
			{
				if (file.exists() && file.canRead())
				{
					  World.theWorld.myKeepRunning = false;
					  String heading = "MidEvil Game";
					  FileInputStream fileIn = new FileInputStream(file); 
			          ObjectInputStream in = new ObjectInputStream(fileIn); 
			              
					
			          // Method for serialization of object 
			          String str = (String)in.readObject(); 
			          if (!str.equals(heading))
			        	  System.out.println("this is not a midevil game file");
			          else 
			        	  World.theWorld.loadGame(in);
			          in.close(); 
			          fileIn.close(); 
			          
				} // can read from file
				else
				{
					JOptionPane.showMessageDialog(null, "Unable to load game");
					return false;
				}
			}
		  	catch (Exception e)
			{
				  JOptionPane.showMessageDialog(null, "Unable to load game");
				  return false;
			}
		} // end get file
		return true;
	} // end load game
	
	private void saveGame()
	{
		JFileChooser fileChooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	            "MidEvil files", "mid");
	    fileChooser.setFileFilter(filter);	
	    fileChooser.addChoosableFileFilter(filter);
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    fileChooser.setSelectedFile(new File("*.mid"));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		  File file = fileChooser.getSelectedFile();
		  try 
		  {
			  if (file.exists())
				  file.delete();
			  file.createNewFile();
			  
			  if (file.canWrite())
			  {
				  String heading = "MidEvil Game";
				  FileOutputStream fileOut = new FileOutputStream(file); 
		          ObjectOutputStream out = new ObjectOutputStream(fileOut); 
		          
		          out.writeObject(heading); 

		          World.theWorld.saveGame(out);
		          
		          out.close(); 
		          fileOut.close(); 
				  
			  	}// can write to file
			  	else
					  JOptionPane.showMessageDialog(null, "Unable to save game");
		  }
		  catch (Exception e)
		  {
			  JOptionPane.showMessageDialog(null, "Unable to save game");
		  }
		  
		} // end get file
	} // end save game
	
	private void createMenuBar() 
	{

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.setMnemonic(KeyEvent.VK_L);
        loadMenuItem.setToolTipText("Load Game");
        // need to place action listener near end of this method
        // so that it can see all the menu variables and 
        // update them based on the loaded file.

        fileMenu.add(loadMenuItem);
        
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setToolTipText("Save Game");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener((event) -> this.saveGame());

        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        
        JMenu optionMenu = new JMenu("Options");
        JMenuItem newMenuItem = new JMenuItem("New Game");
        JMenuItem endMenuItem = new JMenuItem("End Game");
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setToolTipText("Start a new game");
        newMenuItem.addActionListener((event) -> {
						newMenuItem.setEnabled(false);
						optionMenu.setEnabled(false);
						loadMenuItem.setEnabled(false);
						saveMenuItem.setEnabled(true);
						endMenuItem.setEnabled(true);
						World.theWorld.newGame();
			        			
        			});

        fileMenu.add(newMenuItem);
 
        endMenuItem.setMnemonic(KeyEvent.VK_G);
        endMenuItem.setToolTipText("End current game");
        endMenuItem.setEnabled(false);
        endMenuItem.addActionListener((event) -> 
        			{
        				newMenuItem.setEnabled(true);
        				endMenuItem.setEnabled(false);
        				loadMenuItem.setEnabled(true);
        				saveMenuItem.setEnabled(false);
						optionMenu.setEnabled(true);
        				World.theWorld.resetWorld();
        			});

        fileMenu.add(endMenuItem);
        fileMenu.addSeparator();
         
        JMenuItem eMenuItem = new JMenuItem("Exit");
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((event) -> System.exit(0));

        fileMenu.add(eMenuItem);
        menuBar.add(fileMenu);

        optionMenu.setMnemonic(KeyEvent.VK_O);

       ButtonGroup difGroup = new ButtonGroup();

        JRadioButtonMenuItem easyRMenuItem = new JRadioButtonMenuItem("Easy");
        optionMenu.add(easyRMenuItem);
        easyRMenuItem.setToolTipText("Set difficulty of game play to Easy");

        easyRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                World.difficulty = 3;
            }
        });

        JRadioButtonMenuItem mediumRMenuItem = new JRadioButtonMenuItem("Normal");
        mediumRMenuItem.setSelected(true);
        optionMenu.add(mediumRMenuItem);
        mediumRMenuItem.setToolTipText("Set difficulty of game play to Normal");

        mediumRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                World.difficulty = 5;
            }
        });

        JRadioButtonMenuItem hardRMenuItem = new JRadioButtonMenuItem("Hard");
        optionMenu.add(hardRMenuItem);
        hardRMenuItem.setToolTipText("Set difficulty of game play to Hard");

        hardRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                World.difficulty = 8;
            }
        });

        difGroup.add(easyRMenuItem);
        difGroup.add(mediumRMenuItem);
        difGroup.add(hardRMenuItem);
        
        optionMenu.addSeparator();

        ButtonGroup sizeGroup = new ButtonGroup();

        JRadioButtonMenuItem smallRMenuItem = new JRadioButtonMenuItem("Small Map");
        optionMenu.add(smallRMenuItem);

        smallRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
    			World.rows = 65;
    			World.cols = 35;
    			World.density = 50;
    			World.numPlayers = 3;
    		}
        });

        JRadioButtonMenuItem medRMenuItem = new JRadioButtonMenuItem("Medium Map");
        optionMenu.add(medRMenuItem);

        medRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
    			World.rows = 65;
    			World.cols = 35;
    			World.numPlayers = 5;
    			World.density = 70;               }
        });

        JRadioButtonMenuItem largeRMenuItem = new JRadioButtonMenuItem("Large Map");
        largeRMenuItem.setSelected(true);
        optionMenu.add(largeRMenuItem);

        largeRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
    			World.rows = 65;
    			World.cols = 35;
    			World.numPlayers = 7;
    			World.density = 85;             }
        });

        sizeGroup.add(smallRMenuItem);
        sizeGroup.add(medRMenuItem);
        sizeGroup.add(largeRMenuItem);

        optionMenu.addSeparator();
        
        loadMenuItem.addActionListener((event) -> 
        		{
        			if (!this.loadGame())
        				return;
        			switch (World.density)
        			{
        			case 50 :
        				smallRMenuItem.setSelected(true);
        				break;
        			case 70 :
        		        medRMenuItem.setSelected(true);
        				break;
        			case 85 :
        		        largeRMenuItem.setSelected(true);
        				break;
        			}
        			switch (World.difficulty)
        			{
        			case 3 :
        				easyRMenuItem.setSelected(true);
        				break;
        			case 5 :
        				mediumRMenuItem.setSelected(true);
        				break;
        			case 8 :
        		        hardRMenuItem.setSelected(true);
        				break;
        			}
					newMenuItem.setEnabled(false);
					optionMenu.setEnabled(false);
					loadMenuItem.setEnabled(false);
					saveMenuItem.setEnabled(true);
					endMenuItem.setEnabled(true);
        			World.theWorld.myKeepRunning = true;
        		});

 
        menuBar.add(optionMenu);
        setJMenuBar(menuBar);
    }
	
	
}