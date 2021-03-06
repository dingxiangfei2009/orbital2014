package edu.nus.comp.dotagrid.logic;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ItemShop implements ActionListener{
	
	public static final int[] SENTINEL_ITEM_SHOP_AREA_POS = {0, 86};
	public static final int SENTINEL_ITEM_SHOP_X_OFFSET = 13;
	public static final int SENTINEL_ITEM_SHOP_Y_OFFSET = 13;
	
	public static final int[] SCOURGE_ITEM_SHOP_AREA_POS = {87, 0};
	public static final int SCOURGE_ITEM_SHOP_X_OFFSET = 12;
	public static final int SCOURGE_ITEM_SHOP_Y_OFFSET = 14;
	
	ItemDatabase itemDtabase; 
	
	public static boolean shouldUpdateItemInFo = false;
	
	public ItemShop(){
		JFrame frame = new JFrame("SHOP");
		
		GridFrame.popupJFrame = true;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 4));
		
		itemDtabase = new ItemDatabase();
		
		for (int i=0; i<ItemDatabase.TOTAL_ITEM_NUMBER; i++) {
			
			JButton button = new JButton(itemDtabase.itemDatabase[i].getItemName() + ", cost : " + itemDtabase.itemDatabase[i].getCost(), 
											itemDtabase.itemDatabase[i].getItemImage());

			button.addActionListener(this);
			button.setActionCommand("" + i);

			panel.add(button);
		}
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				GridFrame.popupJFrame = false;
			}
		});
		
		frame.add(panel);
		frame.pack();
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int itemNumber = Integer.parseInt(e.getActionCommand());
		
		boolean hasVacancy = false;
		
		// condition for buying a item : player's item list still has vacancy
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).items[i] == null) {
				hasVacancy = true;
				break;
			}
		}
		
		if (hasVacancy == true){			
			// condition for buying a item : player's hero has enough money
			if (((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).getMoney() 
					- itemDtabase.itemDatabase[itemNumber].getCost() >= 0){
				
				// add an item to player and deduce the required amount of money
				((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).addItem(itemDtabase.itemDatabase[itemNumber]);
				((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).setMoney(
						(((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).getMoney() 
								- itemDtabase.itemDatabase[itemNumber].getCost())); 
				
				// reset money display for player
				GameFrame.allCharacterInfoGameButtons.get(29).setString("Money : " + 
						((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).getMoney());
				
				// reselect the grid
				shouldUpdateItemInFo = true;
				GridFrame.invokeLeftClickEvent(GridFrame.getSelectedXCoordinatePos(), GridFrame.getSelectedYCoordinatePos());
				
				System.out.println("Player has bought an item : " + itemDtabase.itemDatabase[itemNumber].getItemName());
				
			} else {
				System.out.println("not enough money!");
				notEnoughMoneyFrame();
			}
		} else {
			System.out.println("item list is full!");
			inventoryIsFullList();
		}
		
		shouldUpdateItemInFo = false;
	}

	
	private void inventoryIsFullList() {
		// pop up a not inventory is full warning frame
		final JFrame frame = new JFrame("OOPS!");
		
		JPanel panel = new JPanel();
		final JLabel label1 = new JLabel("Your inventory is full!", JLabel.CENTER);
		final JLabel label2 = new JLabel("Press YES to continue", JLabel.CENTER);
		
		JButton yesButton = new JButton("YES");
		
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		
		panel.setLayout(new GridLayout(2, 1));
		
		panel.add(label1);
		panel.add(label2);
		
		frame.setLayout(new BorderLayout());
		frame.add("Center", panel);
		
		frame.add("South", yesButton);
		
		frame.pack();
		frame.setSize(200, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	
	private void notEnoughMoneyFrame() {
		// pop up a not enough money warning frame
		final JFrame frame = new JFrame("OOPS!");
		
		JPanel panel = new JPanel();
		final JLabel label1 = new JLabel("You don't have enough money!", JLabel.CENTER);
		final JLabel label2 = new JLabel("Press YES to continue", JLabel.CENTER);
		
		JButton yesButton = new JButton("YES");
		
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		
		panel.setLayout(new GridLayout(2, 1));
		
		panel.add(label1);
		panel.add(label2);
		
		frame.setLayout(new BorderLayout());
		frame.add("Center", panel);
		
		frame.add("South", yesButton);
		
		frame.pack();
		frame.setSize(200, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
}
