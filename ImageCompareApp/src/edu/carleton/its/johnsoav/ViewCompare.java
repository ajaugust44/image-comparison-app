package edu.carleton.its.johnsoav;

import java.awt.event.KeyEvent;

import processing.core.PApplet;

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

	boolean loading;

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
		System.out.println("IN VIEWCOMPARE SETUP");
		parent.background(255);
		
		parent.thread("callThread");
		nextImage();
	}

	public void initButtons() {
		int buttonHeight = Button.buttonHeight;
		int[][] buttonInfo = {
				//Save + Review
				{parent.width - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight},
				// Next
				{parent.width - buttonHeight - 10, parent.height / 2 + 10, 70, buttonHeight},
				// Save
				{parent.width - buttonHeight - 10, parent.height / 2 + buttonHeight + 20, 70, buttonHeight },
		};

		int[][] endButtonInfo = {
				{parent.width/2 - buttonHeight - 10, parent.height / 2 - buttonHeight, 50, buttonHeight},
		};

		String[] buttonNames = {
				"Save and\nReview",
				"Next",
				"Save"
		};

		String[] endButtonNames = {
				"Save and \nReview",
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

	public void draw() {
		
		if (parent.mousePressed) {
			System.out.println("mouse pressed in compare");
			parent.fill(255, 0, 0);
			parent.rect(0, 0, 50, 50);
		}
		
		this.drawSelectedBox();
		this.drawImages();
		this.drawButtons();
	}

	public void drawImages() {
		if (this.loading) {
			return;
		}
		if (mainImage != null) 
			mainImage.drawImage();
		if (compareImages == null) {
			return;
		}
		for (int i = 0; i < compareImages.length; i ++) {
			if (compareImages[i] != null)
				compareImages[i].drawImage();
		}
	}

	public void drawSelectedBox() {
		if (this.loading) {
			return;
		}
		if (this.compareImages == null) {
			return;
		}

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

	public void nextImage() {
		this.loading = true;
		this.selectedImage = null;
		this.currCompareImages = 1;

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
		if (mainImagePath == null) {
			System.out.println("REACHED END");

			mainImage = null;
			this.selectedImage = null;
			this.compareImages = new ClickableImage[6];
			System.gc();
			this.loading = false;
			return;
		}
		
		mainImage = new ClickableImage(parent, mainImagePath, 0, 0, (int)(parent.width/(2.5)), parent.height, true);
		mainImage.setClickable(false);
		
		compareImages = new ClickableImage[ViewCompare.numCompareImages];

		int imgWidth =  getSmallImageWidth();
		int imgHeight = getSmallImageHeight();

		int row, col, newX, newY;

		for (int i = 0; i < ViewCompare.numCompareImages; i++) {
			if (compareImagePaths[i] == null) { //TODO FIX BUG
				return;
			}
			row = i / (ViewCompare.numCompareImages/ViewCompare.numRows);
			col = i % (ViewCompare.numCompareImages/ViewCompare.numRows);

			newX = mainImage.x + mainImage.width + col * (imgWidth + padding) + padding;
			newY = row * (imgHeight + padding) + padding;
			compareImages[i] = new ClickableImage(parent, compareImagePaths[i], newX, newY, imgWidth, imgHeight, false);
		}

		System.gc();
		this.loading = false;
	}

	public void nextCompares() {
		this.loading = true;
		this.selectedImage = null;

		String[] compareImagePaths = controller.getCompareImagePaths(ViewCompare.numCompareImages * this.currCompareImages);

		if (compareImagePaths[0] == null) {
			this.selectedImage = null;
			this.compareImages = new ClickableImage[ViewCompare.numCompareImages];
			System.gc();
			this.loading = false;
			return;
		}
		compareImages = new ClickableImage[ViewCompare.numCompareImages];

		int imgWidth =  getSmallImageWidth();
		int imgHeight = getSmallImageHeight();

		int row, col, newX, newY;
		int pathIndex = ViewCompare.numCompareImages * (this.currCompareImages - 1);
		for (int i = 0; i < ViewCompare.numCompareImages; i++) {
			if (compareImagePaths.length <= pathIndex + i) {
				loading = false;
				return;
			}
			row = i / (ViewCompare.numCompareImages/ViewCompare.numRows);
			col = i % (ViewCompare.numCompareImages/ViewCompare.numRows);

			newX = mainImage.x + mainImage.width + col * (imgWidth + padding) + padding;
			newY = row * (imgHeight + padding) + padding;
			compareImages[i] = new ClickableImage(parent, compareImagePaths[pathIndex + i],
					newX, newY, imgWidth, imgHeight, false);
		}


		System.gc();
		this.loading = false;
	}

	public void selectImage(int imageID) {
		if (this.selectedImage != null && this.selectedImage.intValue() == imageID){
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
		if (this.mainImage != null) {
			for (int i = 0; i < buttonList.length; i++) {
				if (buttonList[i].clicked()) {
					switch(i) {
					case 0:
						// "End Session" -- switch to ViewReview
						if (selectedImage != null && selectedImage < compareImages.length){
							this.controller.setSelected(this.compareImages[this.selectedImage].path);
						}
						
						controller.save();
						this.switchViews = ICView.VIEW_REVIEW_ID;
						break;
					case 1:
						// next image: only for non-end -- next mainImage
						if (this.mainImage != null) {
							if (selectedImage != null && selectedImage < compareImages.length){
								this.controller.setSelected(this.compareImages[this.selectedImage].path);
								this.parent.background(ICView.backgroundColor);
								parent.thread("callThread");
							}
						}
						break;
					case 2:
						//Save
						controller.save();
						break;
					default:
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < endButtonList.length; i ++) {
				if (endButtonList[i].clicked()) {
					switch(i) {
					case 0:
						// Save and Review
						controller.save();
						this.switchViews = ICView.VIEW_REVIEW_ID;
						break;
					}
				}
			}
		}
		
		
	}

	@Override
	public void keyPressed(int key) {
		switch(key) {
		case PApplet.RETURN:
		case PApplet.ENTER:
			if (selectedImage != null && selectedImage < compareImages.length){
				this.controller.setSelected(this.compareImages[this.selectedImage].path);
				this.parent.background(ICView.backgroundColor);
				parent.thread("callThread");
			} else if (this.mainImage == null){
				controller.save();
				this.switchViews = ICView.VIEW_REVIEW_ID;
			}
			break;
		case KeyEvent.VK_LEFT:
			if (this.currCompareImages > 1) {
				this.currCompareImages -= 1;
				this.nextCompares();
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (this.compareImages[ViewCompare.numCompareImages-1] != null) {
				this.currCompareImages += 1;
				this.nextCompares();
			}
			break;
		case KeyEvent.VK_0:
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
		case KeyEvent.VK_5:
		case KeyEvent.VK_6:
			selectImage((key - 1) - KeyEvent.VK_0);
			break;
		case KeyEvent.VK_S:
			controller.save();
			break;
		case PApplet.ESC:
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
		return ((parent.width - Button.buttonHeight - padding) - (mainImage.x + mainImage.width) - (5 * padding))/3;
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

	public boolean isLoading() {
		return this.loading;
	}

	public void threadFunction() {
		this.nextImage();
	}

}


