/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.gui;

import java.text.BreakIterator;
import java.util.ArrayList;

import android.graphics.Color;

public class TextObj implements ScreenObj {
	String txt;
	float fontSize;
	float width, height;
	float areaWidth, areaHeight;
	String lines[];
	float lineWidths[];
	float lineHeight;
	float maxLineWidth;
	float pad;
	int borderColor, bgColor, textColor;

	public TextObj(String txtInit, float fontSizeInit, float maxWidth,
			PaintScreen dw) {
		this(txtInit, fontSizeInit, maxWidth, Color.rgb(255, 255, 255), Color
				.rgb(0, 0, 0), Color.rgb(255, 255, 255),
				dw.getTextAsc() / 2, dw);
	}

	public TextObj(String txtInit, float fontSizeInit, float maxWidth,
			int borderColor, int bgColor, int textColor, float pad,
			PaintScreen dw) {
		this.pad = pad;
		this.bgColor = bgColor;
		this.borderColor = borderColor;
		this.textColor = textColor;

		try {
			prepTxt(txtInit, fontSizeInit, maxWidth, dw);
		} catch (Exception ex) {
			ex.printStackTrace();
			prepTxt("TEXT PARSE ERROR", 12, 200, dw);
		}
	}

	private void prepTxt(String txtInit, float fontSizeInit, float maxWidth,
			PaintScreen dw) {
		dw.setFontSize(fontSizeInit);

		txt = txtInit;
		fontSize = fontSizeInit;
		areaWidth = maxWidth - pad * 2;
		lineHeight = dw.getTextAsc() + dw.getTextDesc()
				+ dw.getTextLead();

		ArrayList<String> lineList = new ArrayList<String>();

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(txt);

		int start = boundary.first();
		int end = boundary.next();
		int prevEnd = start;
		while (end != BreakIterator.DONE) {
			String line = txt.substring(start, end);
			String prevLine = txt.substring(start, prevEnd);
			float lineWidth = dw.getTextWidth(line);

			if (lineWidth > areaWidth) {
				// If the first word is longer than lineWidth 
				// prevLine is empty and should be ignored
				if(prevLine.length()>0)
					lineList.add(prevLine);

				start = prevEnd;
			}

			prevEnd = end;
			end = boundary.next();
		}
		String line = txt.substring(start, prevEnd);
		lineList.add(line);

		lines = new String[lineList.size()];
		lineWidths = new float[lineList.size()];
		lineList.toArray(lines);

		maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++) {
			lineWidths[i] = dw.getTextWidth(lines[i]);
			if (maxLineWidth < lineWidths[i])
				maxLineWidth = lineWidths[i];
		}
		areaWidth = maxLineWidth;
		areaHeight = lineHeight * lines.length;

		width = areaWidth + pad * 2;
		height = areaHeight + pad * 2;
	}

	public void paint(PaintScreen dw) {
		dw.setFontSize(fontSize);

		dw.setFill(true);
		dw.setColor(bgColor);
		dw.paintRect(0, 0, width, height);

		dw.setFill(false);
		dw.setColor(borderColor);
		dw.paintRect(0, 0, width, height);

		dw.setFill(true);
		dw.setColor(textColor);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			dw.paintText(pad, pad + lineHeight * i + dw.getTextAsc(), line);
		}
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}
