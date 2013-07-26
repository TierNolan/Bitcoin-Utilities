package org.tiernolan.bitcoin.util.armory;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

public class ArmoryHexTextField extends JFormattedTextField implements KeyListener {

	private static final long serialVersionUID = 1L;
	
	private String lastText = "";
	
	public ArmoryHexTextField() {
		super(getMask());
		setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		this.addKeyListener(this);
	}
	
	public byte[] getHex() {
		return getHex(0);
	}
	public byte[] getHex(int fix) {
		byte[] result = Armory.decodeLine(getText().replace(" ", ""), fix);
		return result;
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		String text = getText();
		if (!lastText.equals(text)) {
			checkText();
		}
		lastText = text;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	private void checkText() {
		char[] chars = getText().toCharArray();
		if (!getText().contains("-")) {
			byte[] bytes = getHex();
			if (bytes == null) {
				bytes = getHex(1);
				if (bytes == null) {
					setBackground(Color.RED);
				} else {
					setBackground(Color.ORANGE);
				}
				return;
			} else {
				setBackground(Color.GREEN);
				return;
			}
		} else {
			int cnt = 0;
			for (char c : chars) {
				if (c == '-' || c == ' ') {
					continue;
				}
				if (Armory.getSymbolValue(c) == -1) {
					cnt++;
				}
			}
			switch (cnt) {
				case 0: setBackground(Color.WHITE); break;
				case 1: setBackground(Color.ORANGE); break;
				default : setBackground(Color.RED);
			}
			
		}
	}
	
	private static MaskFormatter getMask() {
		MaskFormatter mask = null;
	    try {
	        mask = new MaskFormatter("LLLL LLLL LLLL LLLL LLLL LLLL LLLL LLLL LLLL");
	        mask.setPlaceholderCharacter('-');
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return mask;
	}
	
}
