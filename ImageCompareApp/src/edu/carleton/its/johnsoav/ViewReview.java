package edu.carleton.its.johnsoav;

import java.awt.event.KeyEvent;

import processing.core.PApplet;

public class ViewReview implements SubView {

	PApplet parent;
	ICController controller;
	
	ClickableImage compareImage;
	ClickableImage mainImage;
	
	Boolean approved;
	Button[] buttonList;
	
	int switchViews;
	
	public ViewReview(PApplet p) {
		this.parent = p;
		this.switchViews = ICView.VIEW_REVIEW_ID;
	}
	
	@Override
	public void setup() {
		parent.background(ICView.backgroundColor);
		nextImage();
	}

	private void nextImage() {
		approved = null;
		if (mainImage != null) 
			mainImage.done();
		if (compareImage != null)
			compareImage.done();
		String[] paths = controller.getNextMatch();
		if (paths == null) {
			return;
		}
		
		mainImage = new ClickableImage(parent, paths[0], 0, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		mainImage.setClickable(false);
		
		compareImage = new ClickableImage(parent, paths[1], (int)(parent.width/(2)) + 10, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		compareImage.setClickable(false);
		
	}

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed() {
		clickButtons();
	}

	private void clickButtons() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setController(ICController controller) {
		this.controller = controller;
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

	public int switchViews() {
		return this.switchViews;
	}
	
}
