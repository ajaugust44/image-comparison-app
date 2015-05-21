package edu.carleton.its.johnsoav;

import java.awt.event.KeyEvent;

import processing.core.PApplet;


/**
 * In this view a user can review, one by one, the images they have already
 * matched.
 * 
 * TODO: A user can mark a match as incorrect, or simply move on
 * 
 * TODO: It contains "help", "Mark as Incorrect", and "save" buttons
 * 	
 * A user is encouraged to use the keyboard:
 * TODO: - 's' to save
 * 	- enter or return to confirm match + move on
 * TODO: space to mark as incorrect
 * TODO: arrow keys to go back and forth
 * 
 * @author Avery Johnson
 *
 */
public class ViewReview implements SubView {

	/*
	 * ----------------------
	 * 
	 * 	Instance Variables
	 * 
	 * ----------------------
	 */
	
	
	PApplet parent;
	ICController controller;
	
	ClickableImage compareImage;
	ClickableImage mainImage;
	
	Boolean approved;
	Button[] buttonList;
	
	int switchViews;
	
	/*
	 * ----------------------
	 * 
	 * 	Initialization
	 * 
	 * ----------------------
	 */
	
	
	public ViewReview(PApplet p) {
		this.parent = p;
		this.switchViews = ICView.VIEW_REVIEW_ID;
	}
	
	@Override
	public void setup() {
		nextImage();
	}

	/*
	 * ----------------------
	 * 
	 * 	Drawing
	 * 
	 * ----------------------
	 */
	
	
	@Override
	public void draw() {
		drawImages();
		drawButtons();
	}

	private void drawImages() {
		if (mainImage != null) {
			mainImage.drawImage();
		}
		if (compareImage != null) {
			compareImage.drawImage();
		}
	}
	
	private void drawButtons() {
		// maybeDo draw buttons as you write buttons
		System.out.print("");
	}

	
	/*
	 * ----------------------
	 * 
	 * 	Workflow
	 * 
	 * ----------------------
	 */
	
	private void nextImage() {
		parent.background(ICView.backgroundColor);
		
		approved = null;
		if (mainImage != null) 
			mainImage.done();
		if (compareImage != null)
			compareImage.done();
		String[] paths = controller.getNextMatch();
		
		if (paths == null) {
			this.mainImage = null;
			this.compareImage = null;
			return;
		}
		
		mainImage = new ClickableImage(parent, paths[0], 0, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		mainImage.setClickable(false);
		
		compareImage = new ClickableImage(parent, paths[1], (int)(parent.width/(2)) + 10, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		compareImage.setClickable(false);
		
	}
	
	
	
	/*
	 * ----------------------
	 * 
	 * 	User Interaction
	 * 
	 * ----------------------
	 */
	
	
	@Override
	public void mousePressed() {
		clickButtons();
	}

	private void clickButtons() {
		// TODO create actions on buttons as you write buttons
		
	}

	@Override
	public void keyPressed(int key) {
		if ((this.mainImage != null && this.compareImage != null) && (key == PApplet.RETURN || key == PApplet.ENTER)) {
			this.controller.approve();
			this.parent.background(ICView.backgroundColor);
			nextImage();
		} else if ( key == KeyEvent.VK_S) {
			controller.save();
		} else if ( key == PApplet.ESC)  {
			controller.save();
			parent.exit();
		}
	}
	
	
	/*
	 * ----------------------
	 * 
	 * 	Helper Functions
	 * 
	 * ----------------------
	 */
	
	@Override
	public void setController(ICController controller) {
		this.controller = controller;
	}



	public int switchViews() {
		return this.switchViews;
	}
	
}
