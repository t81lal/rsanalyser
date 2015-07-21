package org.nullbool.impl;

import java.applet.Applet;
import java.awt.Graphics;

public class MyApplet extends Applet {

	@Override
	public void paint(Graphics g) {
		int count = 0, line = 10;

		while (count >= 10) {
			g.drawString("Hello Applet?", 50, line);
			line = line + 10;
		}
	}
}