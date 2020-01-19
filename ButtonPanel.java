import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.kinser.midevilworld.*;

/**
 * 
 */

/**
 * @author 16125
 *
 */



public class ButtonPanel extends JPanel {
	public JButton endTurnBtn = null;
	private ArrayList<JButton> myArmyBtns = new ArrayList<JButton>();
	private JTextField myTextBox = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2963470989503510811L;

	/**
	 * 
	 */
	public ButtonPanel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 */
	public ButtonPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isDoubleBuffered
	 */
	public ButtonPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public ButtonPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}
	
	public void createContent()
	{
		
		   endTurnBtn =new JButton("End Turn");  
		    endTurnBtn.addActionListener( (event) ->  World.theWorld.endTurn()); 
		    //this.setLayout(new BorderLayout());
		    //this.add(endTurnBtn,0);
		    GridBagLayout grid = new GridBagLayout();
		    this.setLayout(grid);
		    GridBagConstraints constraints = new GridBagConstraints();
		    constraints.fill = GridBagConstraints.BOTH;
		    constraints.weightx = 1.0;
		    endTurnBtn.setPreferredSize(new Dimension(100,16));
		    endTurnBtn.setSize(new Dimension(100,16));
		    grid.setConstraints(endTurnBtn, constraints);
		    endTurnBtn.setEnabled(false);
		    this.add(endTurnBtn);

		    for (Occupiers piece : Occupiers.placeablePieces)
		    {
				String file = Images.ourOccupierImageFiles[piece.ordinal()];
				ImageIcon icon = new ImageIcon(file);
				JButton btn = new JButton(icon);
				myArmyBtns.add(btn); 
				btn.addActionListener((event)-> this.buyingPiece(btn,piece));
				btn.setPreferredSize(new Dimension(16,32));
				btn.setMaximumSize(new Dimension(16,32));
				btn.setToolTipText(piece.name()+": "+piece.getCost() + ", " +piece.getConsumption());
			    //btn.setSize(new Dimension(16,16));
			    btn.setEnabled(false);
				grid.setConstraints(btn, constraints);
				this.add(btn);
		    }
		    
		    
		    myTextBox = new JTextField();
		    myTextBox.setText("Selected City Stats:");
		    myTextBox.setBounds(0,0,200,16);
		    myTextBox.setPreferredSize(new Dimension(200,20));
		    myTextBox.setSize(new Dimension(200,20));
	        constraints.gridwidth = GridBagConstraints.REMAINDER; //end row
	        grid.setConstraints(myTextBox,constraints);
		    this.add(myTextBox);
		    this.revalidate();
		// create a center map button to center the map view
		// optional: create a graph showing percentage each player has of all land available
		
		
		
	}
	
	public void buyingPiece(JButton btn, Occupiers piece)
	{
		//if (btn.getBackground() == endTurnBtn.getBackground())
			World.theWorld.buyingArmyUnit(piece);
			repaint();
			

	}
	
	public void setPlayerColor(Color color)
	{
		endTurnBtn.setBackground(color);
	}
	
	public void removeArmyButtons()
	{
		/*
		 * for (JButton btn : myArmyBtns) remove(btn); myArmyBtns.clear();
		 * this.revalidate();
		 */
		
		
	}
	
	public void setCitySelected(City citySelected, HumanPlayer human)
	{
		removeArmyButtons();
		
		ArrayList<Occupiers> buyable = new ArrayList<Occupiers>();
		
		if (citySelected != null )
		{
			myTextBox.setText("Gold: " + citySelected.currentGoldValue() + ", Using: " + citySelected.goldConsumptionEachTurn() 
			        + ", earning: " + citySelected.goldGeneratedEachTurn());
			
			buyable = human.whatICanBuy(citySelected);
		}
		
		this.endTurnBtn.setText("End Turn #" + human.currentTurnCount());
		
		int count = 0;
		for (Occupiers piece : Occupiers.placeablePieces)
		{
			
			boolean found = false;
			for (Occupiers bPiece : buyable)
			{
			
				if (piece == bPiece)
				{
					myArmyBtns.get(count).setEnabled(true);
					
					found = true;
				}
			}
			if (!found)
				myArmyBtns.get(count).setEnabled(false);
			count++;
			
		}
		repaint();


	}

}
