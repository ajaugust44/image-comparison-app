package edu.carleton.its.johnsoav;

import processing.core.*;
import gifAnimation.*;


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
	Gif loadingImg;


	public void setup() {
		System.out.println("arrived in setup **************");
		size(min(WIDTH, displayWidth-1), min(displayHeight - 100, HEIGHT));
		System.out.println("creating controller");
		this.controller = new ICController();
		System.out.println("Controller created");
		this.controller.setView(this);
		initViews();

		System.out.println("setting up viewCompare");
		views[currView].setup();

		loadingImg = new Gif(this, ICModel.MAIN_PATH + "/loading.gif");
		loadingImg.ignoreRepeat();
		loadingImg.loop();


	}

	public void draw() {

		if (currView != views[currView].switchViews()) {
			currView = views[currView].switchViews();
			views[currView].setup();
		} 
		if (views[currView].isLoading()) {
			this.drawLoading();
		} else {
			background(ICView.backgroundColor);
			views[currView].draw();
		}
	}

	public void initViews() {
		views = new SubView[2];

		views[0] = new ViewCompare(this);
		currView = 0;
		views[0].setController(this.controller);

		views[1] = new ViewReview(this);
		views[1].setController(this.controller);
	}

	public void drawLoading() {
		int gifHeight = 200, gifWidth = 400;
		image(this.loadingImg, this.width/2 - (gifWidth/2), this.height/2 - (gifHeight/2), gifWidth, gifHeight);
	}

	public void mousePressed() {
		if (views[currView].isLoading()) {
			return;
		}
		views[currView].mousePressed();
	}

	public boolean sketchFullScreen() {
		return false;
	}


	public void keyPressed() {
		if (views[currView].isLoading()) {
			return;
		}
		views[currView].keyPressed(keyCode);
	}

	public void setController(ICController c) {
		this.controller = c;
	}

	/**
	 * This function uses Processing's built in threading ability to run any
	 * loading a SubView requires separately from the animation thread.
	 */
	public void callThread() {
		this.views[currView].threadFunction();
	}


	public static void main(String[] args) {
		PApplet.main("edu.carleton.its.johnsoav.ICView");
	}

}
