package edu.carleton.its.johnsoav;

import java.awt.event.KeyEvent;

import processing.core.PApplet;


/**
 * In this view a user can review, one by one, the images they have already
 * matched.
 * 
 * A user can mark a match as incorrect/correct
 * 
 * TODO: It contains "help", "reject", and "save" buttons
 * 	
 * A user is encouraged to use the keyboard:
 *  - 's' to save
 * 	- enter or return to confirm match + move on
 *  - space to mark as incorrect
 * 
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
	Button[] endButtonList;

	int switchViews;
	boolean loading;

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
		parent.thread("callThread");
	}

	public void initButtons() {
		int buttonHeight = Button.buttonHeight;
		int[][] buttonInfo = {
				//Save
				{parent.width - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight},
				// Accept
				{parent.width - buttonHeight - 10, parent.height / 2 + 10, 70, buttonHeight},
				// Reject
				{parent.width - buttonHeight - 10, parent.height / 2 + buttonHeight + 20, 70, buttonHeight },
		};

		int[][] endButtonInfo = {
				{parent.width/2 - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight},
		};

		String[] buttonNames = {
				"Save",
				"Accept\nMatch",
				"Reject\nMatch",
		};
		String[] endButtonNames = {
				"Save and\nquit"
		};

		this.buttonList = new Button[buttonNames.length];
		this.endButtonList = new Button[endButtonNames.length];
		for (int i = 0; i < buttonNames.length; i++) {
			this.buttonList[i] = new Button(this.parent, buttonInfo[i][0],
					buttonInfo[i][1], buttonInfo[i][2], buttonInfo[i][3],
					buttonNames[i]);
		}
		for (int i = 0; i < endButtonNames.length; i ++) {
			this.endButtonList[i] = new Button(this.parent, endButtonInfo[i][0],
					endButtonInfo[i][1], endButtonInfo[i][2], endButtonInfo[i][3],
					endButtonNames[i]); 
		}
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
		if (this.loading) {
			return;
		}
		if (mainImage != null) {
			mainImage.drawImage();
		}
		if (compareImage != null) {
			compareImage.drawImage();
		}
	}

	private void drawButtons() {
		if (this.loading) {
			return;
		}
		if (buttonList == null) {
			initButtons();
		}
		if (this.mainImage == null) {
			for (int i = 0; i < endButtonList.length; i++) {
				if (i == 1) {
					continue;
				}
				endButtonList[i].draw();
			}
		}
		else{
			for (int i = 0; i < buttonList.length; i++) {
				buttonList[i].draw();
			}
		}

	}


	/*
	 * ----------------------
	 * 
	 * 	Workflow
	 * 
	 * ----------------------
	 */

	private void nextImage() {
		this.loading = true;
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
			this.loading = false;
			return;
		}

		mainImage = new ClickableImage(parent, paths[0], 0, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		mainImage.setClickable(false);

		compareImage = new ClickableImage(parent, paths[1], (int)(parent.width/(2)) + 10, 0, (int)(parent.width/(2)) - 5, parent.height, true);
		compareImage.setClickable(false);
		this.loading = false;
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
		if (this.buttonList == null || this.loading) {
			return;
		}
		if (this.mainImage != null) {
			for (int i = 0 ; i< buttonList.length; i++) {
				if (buttonList[i].clicked()){
					switch(i) {
					case 0: // save
						controller.save();
						break;
					case 1: // accept
						this.controller.approve();
						this.parent.background(ICView.backgroundColor);
						parent.thread("callThread");
						break;
					case 2: // reject
						this.approved = false;
						this.parent.background(ICView.backgroundColor);
						parent.thread("callThread");
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < endButtonList.length; i++) {
				if (endButtonList[i].clicked()) {
					switch(i) {
					case 0: // save and quit
						controller.save();
						parent.exit();
					}
				}
			}
		}

	}

	@Override
	public void keyPressed(int key) {
		if ((this.mainImage != null && this.compareImage != null) && (key == PApplet.RETURN || key == PApplet.ENTER)) {
			this.controller.approve();
			this.parent.background(ICView.backgroundColor);
			parent.thread("callThread");			
		} else if ( key == KeyEvent.VK_S) {
			controller.save();
		} else if ( key == PApplet.ESC)  {
			controller.save();
			parent.exit();
		} else if ( key == KeyEvent.VK_SPACE) {
			this.approved = false;
			this.parent.background(ICView.backgroundColor);
			parent.thread("callThread");
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

	public boolean isLoading() {
		return this.loading;
	}

	public int switchViews() {
		return this.switchViews;
	}

	public void threadFunction() {
		this.nextImage();
		return;
	}

}
