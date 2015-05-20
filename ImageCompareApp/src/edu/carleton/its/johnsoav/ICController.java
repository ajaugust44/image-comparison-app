package edu.carleton.its.johnsoav;

import java.util.ArrayList;


/**
 * The controller organizes the running of the program.
 * It gets information about images from the model and transmits
 * the relevant information to the view.
 * 
 * Additional Features: TODO:
 *	- Load old session
 *	- save + load calculated information
 * FIXME:  - LOG changed names 
 * - asynch loading of images 
 * 
 * 
 * @author Avery Johnson
 *
 */
public class ICController {
	
	/*
	 * ----------------------
	 * 
	 * 	Instance Variables
	 * 
	 * ----------------------
	 */
	
	ICView view;
	ICModel model;
	
	ArrayList<String> jpgImages;
	ArrayList<String> tifImages;
	
	int currMainImage;
	int compareImage;
	
	String[] selectedCompares;

	String[][] matched;
	String[][] approved;
	
	
	/*
	 * ----------------------
	 * 
	 * 	Initialization
	 * 
	 * ----------------------
	 */
	
	public ICController() {
		currMainImage = -1;
		compareImage = 0;
		this.model = new ICModel();
		this.model.setController(this);
	}
	
	public void initImages() {
		jpgImages = model.getJPGImages();
		tifImages = model.getTIFImages();
		selectedCompares = new String[tifImages.size()];
	}
	
	public void setSelected(String selectedPath) {
		this.selectedCompares[this.currMainImage] = selectedPath;
	}
	
	/*
	 * ----------------------
	 * 
	 * 	View Workflow
	 * 
	 * ----------------------
	 */
	
	public void save() {
		if (this.currMainImage < 0) {
			return;
		}
		matched = new String[this.currMainImage][2];
		for (int i = 0; i < this.currMainImage; i++) {
			matched[i][1] = this.selectedCompares[i];
			matched[i][0] = this.tifImages.get(i);
		}
		model.saveSession(matched);
	}

	
	public void resetCounts() {
		this.currMainImage = -1;
	}
	
	public String[] getNextMatch() {
		this.currMainImage ++;
		if (currMainImage >= matched.length) {
			return null;
		}
		return matched[currMainImage];
	}

	public void approve() {
		if (this.approved == null) {
			this.approved = new String[this.matched.length][2];
		}
		this.approved[currMainImage][0] = this.matched[currMainImage][0];
		this.approved[currMainImage][1] = this.matched[currMainImage][1];
	}
	
	/*
	 * ----------------------
	 * 
	 * 	Getters/Setters
	 * 
	 * ----------------------
	 */
	
	public void setView(ICView view) {
		this.view = view;
	}
	
	public void setModel(ICModel model) {
		this.model = model;
	}
	
	
	public String getMainCompareImagePath() {
		return model.getMainPath();
	}
	
	public String[] getCompareImagePaths(int numPaths) {
		if (jpgImages == null) {
			this.initImages();
		}
		String[] neighbors = model.getCompareImages(numPaths);
		return neighbors;
	}
	
}
