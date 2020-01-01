
import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class WorldPanel extends JPanel implements MouseListener, MouseWheelListener,
					CustomCursor, UIRefreshInterface, NotifyGameWon
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2112960996668811817L;
	public static WorldPanel theWorldPanel = null;
	
	public static JScrollPane scrollWorldPanel = null;
	
	private ButtonPanel myBtnPanel = null;
	private int blinking = 1;
	public double zoomFactor = 1;
	private double prevZoomFactor = 1;
	private boolean zoomer = false;
	
	// WorldPanel default constructor
	public WorldPanel() {

		addMouseListener(this); // for detecting mouse pressed
		addMouseWheelListener(this); // for detecting mouse pressed
		WorldPanel.theWorldPanel = this;
		
	} // end WorldPanel constructor
 

	
	@Override
	// override base class methods so that we can draw all the cells in our WorldPanel
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    //if (zoomer) {
	        AffineTransform at = new AffineTransform();
	        at.scale(zoomFactor, zoomFactor);
	        prevZoomFactor = zoomFactor;
	        g2.transform(at);
			if (zoomer) {
				setPreferredSize(new Dimension((int)((World.rows+1)*World.rowOffset*zoomFactor), 
						                       (int)((World.cols+3)*World.colOffset*zoomFactor)));
//				WorldPanel.scrollWorldPanel.revalidate();
				WorldPanel.scrollWorldPanel.getHorizontalScrollBar().setValue(
						(int) ((World.rowOffset*World.rows*zoomFactor -
						WorldPanel.scrollWorldPanel.getHorizontalScrollBar().getVisibleAmount())/2));
				WorldPanel.scrollWorldPanel.getVerticalScrollBar().setValue(
					(int) ((World.colOffset*World.cols*zoomFactor -
						WorldPanel.scrollWorldPanel.getVerticalScrollBar().getVisibleAmount())/2));
			}
	        zoomer = false;
	    //}
		
		g.setColor(Color.red);
		try {

			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
			for (int i = 0; i < World.theWorld.myMap.length ; i++)
			{
				for (int j = 0; j < World.theWorld.myMap[0].length; j++)
				{
					Cell cell = World.theWorld.myMap[i][j];

					g.drawImage(cell.getImage(),
							    i*World.rowOffset, 
							    j*World.colOffset,
							    new Color(cell.getBackground()), null);
					
					// draw occupier on top of cell image
					if (cell.getOccupiers() != Occupiers.NONE)
					{
						// blink the human pieces that can be moved
						if ((cell.getCity() != null) && 
						    (cell.getCity().getPlayer() == World.theWorld.human) &&
//							(cell.getOccupiers() != Occupiers.VILLAGE) &&
							(cell.getOccupiers() != Occupiers.CASTLE) &&
							(cell.ableToAttack()) &&
							(blinking < 0))
						{
							g.drawImage(cell.getGeology().getImage(), 
									i*World.rowOffset, 
									j*World.colOffset,
									new Color(cell.getBackground()), 
									null);
						}
						

					}
				} 
			}
			
			
			g.setColor(Color.BLACK);


			
			// clear the line of buyable pieces
			int number = 60/World.rowOffset;
			for (int i = 0; i < Occupiers.placeablePieces.length; i++)
				World.theWorld.myMap[number+i][0].setOccupiers(Occupiers.NONE);
			
			// display what the human player can buy for the city selected
			if (World.theWorld.currentPlayer() == World.theWorld.human)
			{
				myBtnPanel.setPlayerColor(new Color(World.theWorld.human.getColor()));
				myBtnPanel.setCitySelected(World.theWorld.citySelected, World.theWorld.human);

			}
			
			if (World.theWorld.citySelected != null)
			{
				// draw outline of selected City
				g.setColor(Color.BLACK);
				((Graphics2D)g).draw(World.theWorld.citySelected.getArea());
			}

		} // end try block
		catch (Exception exception) 
		{
			// do nothing; just capture errors for now
			// TODO: figure out why it's throwing exceptions (ConcurrentModificationException?)
			// 
			// I saw several places in the class documentation that Swing is NOT thread safe but
			// running my WorldPanel class in a thread was based on examples I found on 
			// Oracles and StackOverflow websites. Catching and ignoring these errors 
			// doesn't seem to cause a problem in quick runs of the program.			
		} // end catch all exceptions
		
		if (--blinking < -1)
			blinking = 1;
		
	} // end paintComponent

	
	public void setButtonPanel(ButtonPanel btnPanel)
	{
		myBtnPanel = btnPanel;
	}
	
	public void gameWasWonBy(Player player)
	{
		JOptionPane.showMessageDialog(this, "Player " + player.myColorDisplayName + " has won");
	}

	

	@Override
	public void mouseClicked(MouseEvent e) 
	{
	
		if (e.getButton() == 1) 
		{
			World.theWorld.mouseClicked((int) (e.getX()/zoomFactor), (int) (e.getY()/zoomFactor));
			
		}
		repaint();
	}

	public void refreshUI()
	{
		repaint();
	}
	public void createCustomCursor(Occupiers piece)
	{
		
		Cursor customCursor = null;
		if (piece != null)
		{
			customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					Occupiers.ourImage[piece.ordinal()], 
					new Point(0, 0), 
					"customCursor");
			Toolkit.getDefaultToolkit().getBestCursorSize(64, 64);
		}
		this.setCursor( customCursor );
		//repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{ 
		World.theWorld.mousePressed((e.getButton() == 1),(int) (e.getX()/zoomFactor), (int) (e.getY()/zoomFactor));
		repaint();

	} // mouse pressed 
	
	@Override
	// mouseEntered is used to detect when the cursor is moved to the canvas area
	public void mouseEntered(MouseEvent e) {
// TODO ??
	} // end mouseEntered
	@Override
	public void mouseExited(MouseEvent e) 
	{

	}
	

	@Override
	public void mouseReleased(MouseEvent e) 
	{ 
		World.theWorld.mouseReleased((int) (e.getX()/zoomFactor), (int) (e.getY()/zoomFactor));
		repaint();

	} // end mouseReleased



	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
	    zoomer = true;
	    //Zoom in
	    if (e.getWheelRotation() < 0) {
	        zoomFactor *= 1.1;
	        repaint();
	    }
	    //Zoom out
	    if (e.getWheelRotation() > 0) {
	        zoomFactor /= 1.1;
	        repaint();
	    }
	}
	
	
}
 