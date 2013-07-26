package org.tiernolan.bitcoin.util.armory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class ArmoryInputGrid extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final ArmoryHexTextField[] fields;
	
	public ArmoryInputGrid() {
		this(new String[] {"Root key 1", "Root key 2", "Chain code 1", "Chain code 2"});
	}
	
	public ArmoryInputGrid(String[] names) {
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 10, 2, 2);

		fields = new ArmoryHexTextField[names.length];
		
		for (int row = 0; row < names.length; row++) {
			c.gridy = row;
			
			JLabel l = new JLabel(names[row]);
			c.gridx = 0;
			add(l, c);
			
			fields[row] = new ArmoryHexTextField();
			c.gridx = 1;
			add(fields[row], c);
			fields[row].setVisible(true);
		}
		
	}

}
