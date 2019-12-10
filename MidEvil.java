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
	public static MidEvil ourGame;
	
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
		//@SuppressWarnings("unused")
		MidEvil.ourGame = new MidEvil();
	
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
		

		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(400, 300));
		ourWorld.setPreferredSize(new Dimension((World.rows+1)*World.rowOffset, (World.cols+3)*World.colOffset));
		World.scrollWorld =  new JScrollPane(ourWorld, 
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel buttonPanel = createButtonPanel();
		//Create a split pane with the two scroll panes in it.
		buttonPanel.setMinimumSize(new Dimension(20,20));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		                           buttonPanel, World.scrollWorld);
		//Provide minimum sizes for the two components in the split pane
		splitPane.setOneTouchExpandable(false);
		splitPane.setAutoscrolls(false);
		splitPane.setDividerSize(0);
		splitPane.setResizeWeight(.05);
		
		add(BorderLayout.CENTER, splitPane);
		
		createMenuBar();
		
		pack();
		setSize(800,600);
		World.scrollWorld.getHorizontalScrollBar().setValue(
				(World.rowOffset*World.rows -
				World.scrollWorld.getHorizontalScrollBar().getVisibleAmount())/2);
		World.scrollWorld.getVerticalScrollBar().setValue(
			(World.colOffset*World.cols -
				World.scrollWorld.getVerticalScrollBar().getVisibleAmount())/2);

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
			          
				  		setSize(800,600);
		    			World.scrollWorld.getHorizontalScrollBar().setValue(
	    						World.scrollWorld.getHorizontalScrollBar().getVisibleAmount()/2);
	    			World.scrollWorld.getVerticalScrollBar().setValue(
	    						World.scrollWorld.getVerticalScrollBar().getVisibleAmount()/2);	
			          
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
		    			World.scrollWorld.getHorizontalScrollBar().setValue(
		    					(World.rowOffset*World.rows -
	    						World.scrollWorld.getHorizontalScrollBar().getVisibleAmount())/2);
		    			World.scrollWorld.getVerticalScrollBar().setValue(
	    					(World.colOffset*World.cols -
	    						World.scrollWorld.getVerticalScrollBar().getVisibleAmount())/2);			        			
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
    			this.setSize(600,400);
    			World.scrollWorld.getHorizontalScrollBar().setValue(
    					(World.rowOffset*World.rows -
						World.scrollWorld.getHorizontalScrollBar().getVisibleAmount())/2);
    			World.scrollWorld.getVerticalScrollBar().setValue(
					(World.colOffset*World.cols -
						World.scrollWorld.getVerticalScrollBar().getVisibleAmount())/2)	;        			
    		}
        });

        JRadioButtonMenuItem medRMenuItem = new JRadioButtonMenuItem("Medium Map");
        optionMenu.add(medRMenuItem);

        medRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
    			World.rows = 65;
    			World.cols = 35;
    			World.numPlayers = 5;
    			World.density = 70;
    			this.setSize(900,600);

    			World.scrollWorld.getHorizontalScrollBar().setValue(
    					(World.rowOffset*World.rows -
						World.scrollWorld.getHorizontalScrollBar().getVisibleAmount())/2);
    			World.scrollWorld.getVerticalScrollBar().setValue(
					(World.colOffset*World.cols -
						World.scrollWorld.getVerticalScrollBar().getVisibleAmount())/2)	;        			
    			}
        });

        JRadioButtonMenuItem largeRMenuItem = new JRadioButtonMenuItem("Large Map");
        largeRMenuItem.setSelected(true);
        optionMenu.add(largeRMenuItem);

        largeRMenuItem.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
    			World.rows = 65;
    			World.cols = 35;
    			World.numPlayers = 7;
    			World.density = 85;
    			this.setSize(1100,800);
    			World.scrollWorld.getHorizontalScrollBar().setValue(
    					(World.rowOffset*World.rows -
						World.scrollWorld.getHorizontalScrollBar().getVisibleAmount())/2);
    			World.scrollWorld.getVerticalScrollBar().setValue(
					(World.colOffset*World.cols -
						World.scrollWorld.getVerticalScrollBar().getVisibleAmount())/2)	;	        			
    			}
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
        
        JMenuItem helpMenu = new JMenuItem("How to Play");
        helpMenu.addActionListener( (event) -> JOptionPane.showMessageDialog(World.theWorld, 
        		"<html><h1>MidEvil</h1><p style='width:400'>This is a strategy board game in which players compete to gain territory. Whoever eliminates their competition, Wins!!<br>"
        		+ "<br>Each player is assigned a color and the same number of cities. Cities are collections of adjacent squares of the same color. If two or more cities of the same color touch, they are combined into a single city. So, it may appear that some players have fewer cities at the start but this is because two or more of their cities were touching and combined before play started." +
        				"<br>Each turn, every city gets gold based on the number of squares contained within the city. Click on one of your cities to see the current amount of gold, show in the upper left corner." + 
        		"<br><br>A player can buy army units to place on their city or attack neighboring squares to build on the size of their city. The more gold you have, the more powerful army piece you can buy." + 
        				"<br><br>To buy a unit, click on a city, then click on the army unit displayed in the upper left corner, dragging it down to the square you want to place it on or the one you want to attack (note: you can only attack squares touching your city)."
        				+ "<br>To move or attack with an existing unit, click on and drag that unit where you want it to be. You can only move units that are flashing (that is, once a unit is used to attack, it can't be used or moved again until the next turn)."
        		));
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
	
	private JPanel createButtonPanel()
	{
		ButtonPanel panel = new ButtonPanel();
		panel.createContent();
		
		World.theWorld.setButtonPanel(panel);

		return panel;
	}
	
	
}