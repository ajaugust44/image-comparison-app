package edu.carleton.its.johnsoav;

import processing.core.PApplet;


/**
 * This class creates a clickable object on the screen that can contain text
 * @author ajaugust44
 *
 */
public class Button {
	
	public final int[] color = {100, 120, 200};
	public final int[] lineColor = {44, 44, 66};
	
	PApplet parent;
	int x, y;
	int width, height;
	String text;
	
	
	public Button(PApplet parent) {
		this.parent = parent;
		this.x = 0;
		this.y = 0;
		this.width = 10;
		this.height = 10;
	}
	
	public Button(PApplet parent, int x, int y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.width = 10;
		this.height = 10;
	}
	
	public Button(PApplet parent, int x, int y, int w, int h) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}
	
	public Button(PApplet parent, String text) {
		this.parent = parent;
		this.x = 0;
		this.y = 0;
		this.width = 10;
		this.height = 10;
		this.text = text;
	}
	
	public Button(PApplet parent, int x, int y, String text) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.width = 10;
		this.height = 10;
		this.text = text;
	}
	
	public Button(PApplet parent, int x, int y, int w, int h, String text) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.width = Math.max(w, (int)parent.textWidth(text) + 10);
		this.height = h;
		this.text = text;
	}
	
	public void draw() {
		parent.pushStyle();
		parent.fill(color[0], color[1], color[2]);
		parent.noStroke();
		parent.rect(x, y, width, height, 3);
		parent.stroke(lineColor[0], lineColor[1], lineColor[2]);
		parent.strokeWeight(3);
		parent.line(x, y + height, x + width - 1, y + height);
		
		parent.strokeWeight(1);
		parent.fill(255);
		parent.text(this.text, x + 5 , y + height/2);
		
		parent.popStyle();
	}
	
	public boolean clicked() {
		if (parent.mouseX >= this.x && parent.mouseX <= this.x + this.width
				&& parent.mouseY >= this.y && parent.mouseY <= this.y + this.height) {
			return true;
		}
		return false;
	}
	
	
}
