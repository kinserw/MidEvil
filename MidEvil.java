import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

import java.awt.event.*;

public class MidEvil extends JFrame
{
	World myWorld;
	
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
		myWorld = new World();
		Thread thread = new Thread (myWorld);
		
		// the default content pane already has a border layout manager by default.
		// use that to add the intro panel (with buttons) and world (where the cells live)
		Container pane = getContentPane();
		pane.add(myWorld, BorderLayout.CENTER);
		pack();
		setSize((World.rows+1)*World.rowOffset,
				(World.cols+3)*World.colOffset);
		setVisible(true);
		setResizable(true);

		thread.start();
	}
	

	
	
}