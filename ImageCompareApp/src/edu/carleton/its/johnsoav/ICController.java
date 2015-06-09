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
	
	int compareImage;
	int currMatch;
	
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
		compareImage = 0;
		this.model = new ICModel();
		this.model.setController(this);
		this.currMatch = -1;
	}
	
	public void initImages() {
		jpgImages = model.getJPGImages();
		tifImages = model.getTIFImages();
		selectedCompares = new String[tifImages.size()];
	}
	
	public void setSelected(String selectedPath) {
		this.selectedCompares[model.getMainImageID()] = selectedPath;
	}
	
	
	/*
	 * ----------------------
	 * 
	 * 	View Workflow
	 * 
	 * ----------------------
	 */
	
	public void save() {
		if (model.getMainImageID() < 0) {
			return;
		}
		ArrayList<String[]> matchedAL = new ArrayList<String[]>(model.mainImageID);
		if (this.approved != null) {
			model.approved = new boolean[matchedAL.size()];
		}
		for (int i = 0; i < this.selectedCompares.length; i ++) {
			if (this.selectedCompares[i] == null) {
				break;
			}
			if(this.approved == null) {
				matchedAL.add(new String[] {this.selectedCompares[i], this.tifImages.get(i)});
			} else {
				if (this.approved[i][0] != null) {
					matchedAL.add(new String[] {this.approved[i][0], this.approved[i][1]});
					model.approved[i] = true;
				}
				
			}
		}
		String[][] a = new String[matchedAL.size()][2];
		matchedAL.toArray(a);
		if (this.approved == null) {
			this.matched = a;
		}
		model.saveSession(a);
	}

	public void quit() {
		if (model.getMainImageID() < 0) {
			return;
		}
		ArrayList<String[]> matchedAL = new ArrayList<String[]>(model.mainImageID);
		for (int i = 0; i < this.selectedCompares.length; i ++) {
			if (this.selectedCompares[i] == null) {
				break;
			}
			if(this.approved == null) {
				matchedAL.add(new String[] {this.selectedCompares[i], this.tifImages.get(i)});
			} else {
				if (this.approved[i][0] != null) {
					matchedAL.add(new String[] {this.approved[i][0], this.approved[i][1]});
				}
			}
		}
		String[][] a = new String[matchedAL.size()][2];
		matchedAL.toArray(a);
		if (this.approved == null) {
			this.matched = a;
		}
		model.completeSession(a);
		
	}
	
	
	public void nextCompareSet() {
		model.nextMainImage();
	}
	
	public String[] getNextMatch() {
		this.currMatch ++;
		if (currMatch >= matched.length) {
			return null;
		}
		return matched[currMatch];
	}

	public void approve() {
		if (this.approved == null) {
			System.out.println("Approve initialized!!!");
			this.approved = new String[this.matched.length][2];
		}
		this.approved[currMatch][0] = this.matched[currMatch][0];
		this.approved[currMatch][1] = this.matched[currMatch][1];
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
