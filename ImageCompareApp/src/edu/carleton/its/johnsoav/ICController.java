package edu.carleton.its.johnsoav;

import java.util.ArrayList;

public class ICController {
	
	ICView view;
	ICModelInterface model;
	
	ArrayList<String> jpgImages;
	ArrayList<String> tifImages;
	
	int currMainImage;
	int compareImage;
	
	String[] selectedCompares;
	
	ImageCompare neighborGenerator;
	
	String[][] matched;
	String[][] approved;
	
	public ICController() {
		currMainImage = -1;
		compareImage = 0;
		this.model = new ICModel();
		this.model.setController(this);
	}
	
	public void setView(ICView view) {
		this.view = view;
	}
	
	public void setModel(ICModelInterface model) {
		this.model = model;
	}
	
	public void initImages() {
		jpgImages = model.getJPGImages();
		tifImages = model.getTIFImages();
		selectedCompares = new String[tifImages.size()];
	}
	
	public void initGenerator() {
		this.neighborGenerator = new ImageCompare(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER, ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER);
	}
	
	public void setSelected(String selectedPath) {
		this.selectedCompares[this.currMainImage] = selectedPath;
	}
	
	public void save() {
		matched = new String[this.currMainImage][2];
		for (int i = 0; i < this.currMainImage; i++) {
			matched[i][1] = this.selectedCompares[i];
			matched[i][0] = this.tifImages.get(i);
		}
		model.saveSession(matched);
	}
	
	
	public String getMainCompareImagePath() {
		if (tifImages == null) {
			this.initImages();
		}
		this.currMainImage += 1;
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		neighborGenerator.nextMainImage();
		if (this.currMainImage < tifImages.size())
			return tifImages.get(this.currMainImage);
		return null;
	}
	
	public String[] getCompareImagePaths(int numPaths) {
		if (jpgImages == null) {
			this.initImages();
		}
		if (neighborGenerator == null) {
			this.initGenerator();
		}
		String[] neighbors = neighborGenerator.getCompareImages(numPaths);
		return neighbors;
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
	
	
}
