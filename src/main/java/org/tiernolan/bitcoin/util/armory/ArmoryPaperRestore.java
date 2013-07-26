package org.tiernolan.bitcoin.util.armory;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class ArmoryPaperRestore extends JDialog {
	
	private static final long serialVersionUID = 1L;

	public static byte[] getKeyFromUser() {
		return null;
	}
	
	public ArmoryPaperRestore(JFrame parent) {
		super(parent);
		
		setLayout(new BorderLayout());
		
		ArmoryInputGrid g = new ArmoryInputGrid();
		add(g, BorderLayout.CENTER);
		
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(parent);
        
        pack();
	}

}
