package edu.carleton.its.johnsoav;

public interface SubView {

	
	public void setup();
	public void draw();
	
	public void mousePressed();
	
	public void setController(ICController controller);
	public void keyPressed(int key);
	
	public int switchViews();
}
