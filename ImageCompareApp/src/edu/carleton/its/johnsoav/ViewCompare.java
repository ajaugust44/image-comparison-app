package edu.carleton.its.johnsoav;

import java.awt.event.KeyEvent;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * This view is designed so that a user can compare a single large
 * image on the left with 6 smaller images on the right.
 * They can select one, then move on to the next screen as necessary
 * 
 * TODO: It can also scroll through the nearest neighbors of that image
 * if a match cannot be found in the first 6 neighbors.
 * 
 * TODO: It contains "help", "save and review", "skip", and "save" buttons
 * 
 * A user is encouraged to use the keyboard:
 *  - 's' to save
 *  - enter or return to move on
 *  - TODO: space to skip
 *  - TODO: arrow keys to go back and forth
 * 
 * 
 * @author Avery Johnson
 *
 */
public class ViewCompare implements SubView{

	/*
	 * ----------------------
	 * 
	 * 	Instance variables
	 * 
	 * ----------------------
	 */


	public static final int numRows = 2;
	public static final int numCompareImages = 6;
	public final int buttonHeight = 50;

	private final int padding = 10;


	PApplet parent;
	ICController controller;


	int currCompareImages;
	ClickableImage[] compareImages;
	ClickableImage mainImage;

	Integer selectedImage;

	Button[] buttonList;
	Button[] endButtonList;


	int switchViews;

	/*
	 * ----------------------
	 * 
	 * 	Initializing
	 * 
	 * ----------------------
	 */

	public ViewCompare(PApplet p) {
		this.parent = p;
		switchViews = ICView.VIEW_COMPARE_ID;
	}

	public void setup() {
		nextImage();
	}

	public void initButtons() {
		int[][] buttonInfo = {
				{parent.width - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight}
		};

		int[][] endButtonInfo = {
				{parent.width/2 - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight}
		};

		String[] buttonNames = {
				"Save and\nReview"
		};
		this.buttonList = new Button[buttonNames.length];
		this.endButtonList = new Button[buttonNames.length];
		for (int i = 0; i < buttonNames.length; i++) {
			this.buttonList[i] = new Button(this.parent, buttonInfo[i][0],
					buttonInfo[i][1], buttonInfo[i][2], buttonInfo[i][3],
					buttonNames[i]);
			this.endButtonList[i] = new Button(this.parent, endButtonInfo[i][0],
					endButtonInfo[i][1], endButtonInfo[i][2], endButtonInfo[i][3],
					buttonNames[i]);
		}
	}

	/*
	 * ----------------------
	 * 
	 * 	Drawing
	 * 
	 * ----------------------
	 */

	public void draw() {

		this.drawSelectedBox();
		this.drawImages();
		this.drawButtons();

	}

	public void drawImages() {
		if (mainImage != null) 
			mainImage.drawImage();
		for (int i = 0; i < compareImages.length; i ++) {
			if (compareImages[i] != null)
				compareImages[i].drawImage();
		}
	}

	public void drawSelectedBox() {
		parent.pushStyle();
		parent.noStroke();
		for (int i = 0; i < ViewCompare.numCompareImages; i ++) {
			if (this.compareImages[i] == null) {
				return;
			}
			if (this.selectedImage != null && i == this.selectedImage) {
				parent.fill(0);
			}
			else{
				parent.fill(ICView.backgroundColor);
			}
			int[] info = getImageBoxInfo(i);
			parent.rect(info[0]-5, info[1]-5, info[2]+10, info[3]+10);
		}
		parent.popStyle();
	}

	public void drawButtons() {
		if (buttonList == null) {
			initButtons();
		}
		if (this.mainImage == null) {
			for (int i = 0; i < endButtonList.length; i++) {
				endButtonList[i].draw();
			}
		}
		else {
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

	public void nextImage() {
		this.selectedImage = null;

		if (mainImage != null) {
			mainImage.done();
		}
		if (compareImages != null) {
			for (int i = 0; i < compareImages.length; i ++) {
				if (compareImages[i] != null)
					compareImages[i].done();
			}
		}

		controller.nextCompareSet();

		String mainImagePath = controller.getMainCompareImagePath();
		String[] compareImagePaths = controller.getCompareImagePaths(ViewCompare.numCompareImages);

		System.out.println("Set mainImagePath to " + mainImagePath);

		if (mainImagePath == null) {
			System.out.println("REACHED END");

			mainImage = null;
			this.selectedImage = null;
			this.compareImages = new ClickableImage[6];
			System.gc();
			return;
		}
		mainImage = new ClickableImage(parent, mainImagePath, 0, 0, (int)(parent.width/(2.5)), parent.height, true);
		mainImage.setClickable(false);

		compareImages = new ClickableImage[ViewCompare.numCompareImages];

		int imgWidth =  getSmallImageWidth();
		int imgHeight = getSmallImageHeight();

		int row, col, newX, newY;

		for (int i = 0; i < ViewCompare.numCompareImages; i++) {
			if (compareImagePaths[i] == null) {
				return;
			}
			row = i / (ViewCompare.numCompareImages/ViewCompare.numRows);
			col = i % (ViewCompare.numCompareImages/ViewCompare.numRows);

			newX = mainImage.x + mainImage.width + col * (imgWidth + padding) + padding;
			newY = row * (imgHeight + padding) + padding;
			compareImages[i] = new ClickableImage(parent, compareImagePaths[i], newX, newY, imgWidth, imgHeight, false);
		}


		System.gc();
	}

	public void selectImage(int imageID) {
		if (selectedImage != null) {
			System.out.println("selected: " + this.selectedImage.intValue() + " selecting " + imageID);
		}
		if (this.selectedImage != null && this.selectedImage.intValue() == imageID){
			System.out.println("Setting to null");
			this.selectedImage = null;
		} else {
			this.selectedImage = new Integer(imageID);
		}
	}


	/*
	 * ----------------------
	 * 
	 * 	User Interaction
	 * 
	 * ----------------------
	 */

	public void mousePressed() {
		for (int i = 0; i < compareImages.length; i++) {
			if (compareImages[i] != null && compareImages[i].clicked()) {
				this.selectImage(i);
			}
		}
		clickButtons();
	}


	public void clickButtons() {

		for (int i = 0; i < buttonList.length; i++) {
			if ((this.mainImage == null && endButtonList[i] != null && endButtonList[i].clicked()) || (buttonList[i] != null && buttonList[i].clicked())) {
				switch(i) {
				case 0:
					// "End Session" -- switch to ViewReview
					controller.save();
					this.switchViews = ICView.VIEW_REVIEW_ID;
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void keyPressed(int key) {
		if (selectedImage != null && selectedImage < compareImages.length && (key == PApplet.RETURN || key == PApplet.ENTER)) {
			this.controller.setSelected(this.compareImages[this.selectedImage].path);
			this.parent.background(ICView.backgroundColor);
			nextImage();
		} else if( key > KeyEvent.VK_0 && key <= KeyEvent.VK_6) {
			selectImage((key - 1) - KeyEvent.VK_0);
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

	public int switchViews() {
		return this.switchViews;
	}

	public int getSmallImageWidth() {
		return ((parent.width - buttonHeight - padding) - (mainImage.x + mainImage.width) - (5 * padding))/3;
	}
	public int getSmallImageHeight() {
		return ((parent.height) - (padding * 3)) / 2;
	}

	public void setController(ICController controller) {
		this.controller = controller;
	}

	public int[] getImageBoxInfo(int imageID) {
		ClickableImage img = this.compareImages[imageID];
		if (img == null) {
			return null;
		}
		return new int[] {img.x, img.y, img.width, img.height};
	}

}


