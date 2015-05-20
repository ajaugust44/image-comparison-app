package edu.carleton.its.johnsoav;

import processing.core.*;


/** 
 * This acts as the view-model: It initializes some set of
 * subViews that interact with each other in some way, draws them 
 * appropriately in the PApplet, and changes them as necessary.
 * 
 * @author Avery Johnson
 *
 */
public class ICView extends PApplet{
	
	private static final long serialVersionUID = 1L;	
	
	public static final int VIEW_COMPARE_ID = 0;
	public static final int VIEW_REVIEW_ID = 1;

	public static final int backgroundColor = 200;
	
	private final int WIDTH = 1500;
	private final int HEIGHT = 800;

	public int currView;
	
	ICController controller;
	
	SubView[] views;
	
	
	public void setup() {
		size(min(WIDTH, displayWidth), min(displayHeight - 100, HEIGHT));
		this.controller = new ICController();
		this.controller.setView(this);
		initViews();
		
		views[currView].setup();
		
	}
	
	public void draw() {
		if (currView != views[currView].switchViews()) {
			currView = views[currView].switchViews();
			views[currView].setup();
		}
		views[currView].draw();
	}
	
	public void initViews() {
		views = new SubView[2];
		
		views[0] = new ViewCompare(this);
		currView = 0;
		views[0].setController(this.controller);
		
		views[1] = new ViewReview(this);
		views[1].setController(this.controller);
	}
	
	
	public void mousePressed() {
		views[currView].mousePressed();
	}

	public void keyPressed() {
		views[currView].keyPressed(keyCode);
	}
	
	public void setController(ICController c) {
		this.controller = c;
	}

	public static void main(String[] args) {
	}


}
