package edu.carleton.its.johnsoav;

import java.util.ArrayList;
import java.lang.Math;

/**
 * This class is designed to work together with the ImageInfo class.
 * It should contain a set of ImageInfos, and a changeable main image such that
 * the k nearest ImageInfos to that main image can be calculated.
 * 
 * @author Avery Johnson
 */


public class ImageCompare {
	
	/*
	 * ----------------------
	 * 
	 * 	Instance Variables
	 * 
	 * ----------------------
	 */
	
	float[][] compareImages;
	String[] comparePaths;
	float[][] standardizedCompare;
	
	private float[][] mainList;
	private String[] mainPaths; 
	private float[][] standardizedMain;
	
	float[] mainImage;
	int mainImageID;
	
	int[] nearestNeighbors;
	

	/**
	 * Constructor: Opens and analyzes each image in preparation for the nearest neighbor
	 * operations
	 * 
	 * @param compare
	 * @param mains
	 */
	
	public ImageCompare(ArrayList<String> compare, ArrayList<String> mains) {
		this.getImageSet(compare, mains);
		this.standardizeData();
	}
	
	/**
	 * This function opens each image and creates an ImageInfo for it,
	 * then gets the comparable information about the image.
	 * 
	 * We cannot keep the ImageInfo because it will cause a memory overflow.
	 * 
	 * @param compare: List of jpg image paths
	 * @param mains: List of tif image paths
	 */
	public void getImageSet(ArrayList<String> compare, ArrayList<String> mains) {
		compareImages = new float[compare.size()][ImageInfo.NUM_CHARS];
		comparePaths = new String[compare.size()];
		ImageInfo img;
		for (int i = 0; i < compare.size(); i++) {
			img = new ImageInfo(compare.get(i));
			compareImages[i] = img.getAllInfo();
			comparePaths[i] = img.getImagePath();
		}
		this.mainList = new float[mains.size()][ImageInfo.NUM_CHARS];
		this.mainPaths = new String[mains.size()];
		for (int i = 0; i < mains.size(); i ++) {
			img = new ImageInfo(mains.get(i));
			mainList[i] = img.getAllInfo();
			mainPaths[i] = img.getImagePath();
		}
		this.mainImageID = -1;
		this.mainImage = mainList[0];
	}
	
	/**
	 * Calculates the nearestNeighbors for the current mainImage
	 * Doesn't take very long for any one image.
	 * 
	 * @param k
	 */
	public void getKNN(int k) {
		DistanceQueue neighbors = new DistanceQueue(k);
		for (int i = 0; i < this.compareImages.length; i ++) {
			DistanceNode node = new DistanceNode(i, this.standardizedMain[this.mainImageID], this.standardizedCompare[i]);
			neighbors.offer(node);
		}
		
		this.nearestNeighbors = new int[neighbors.size()];
		for (int i = neighbors.size() - 1; i >= 0; i --) {
			DistanceNode n = neighbors.poll();
			nearestNeighbors[i] = n.getID();
			
		}
	}
	
	
	/*
	 * ----------------------
	 * 
	 * 	Standardization
	 * 
	 * ----------------------
	 */
	public void standardizeData() {
		this.standardizedCompare = new float[this.compareImages.length][ImageInfo.NUM_CHARS];
		this.standardizedMain = new float[this.mainList.length][ImageInfo.NUM_CHARS];
		
		float[][] allData = new float[this.standardizedCompare.length + this.standardizedMain.length][ImageInfo.NUM_CHARS];
		
		for (int i = 0; i < this.standardizedCompare.length; i ++) {
			allData[i] = this.compareImages[i];
		}
		for (int i = 0; i < this.standardizedMain.length; i ++) {
			allData[i + this.standardizedCompare.length] = this.mainList[i];
		}
		allData = standardizeList(allData);
		
		for (int i = 0; i < this.standardizedCompare.length; i ++) {
			this.standardizedCompare[i] = allData[i];
		}
		for (int i = 0; i < this.standardizedMain.length; i ++) {
			this.standardizedMain[i] = allData[i + this.standardizedCompare.length];
		}
		
	}
	
	public float[][] standardizeList(float[][] data) {
		float[][] flipped = flipTable(data);
		
		float[][] standardized = new float[flipped.length][flipped[0].length];
		
		for (int c = 0; c < flipped.length; c ++ ) {
			standardized[c] = standardizeColumn(flipped[c], c);
		}
		
		return flipTable(standardized);
	}
	
	public float[][] flipTable(float[][] data) {
		float[][] flipped = new float[data[0].length][data.length];
		for (int i = 0; i < data.length; i ++) {
			for (int j = 0; j < data[i].length; j ++) {
				flipped[j][i] = data[i][j];
			}
		}
		return flipped;
	}
	
	public float[] standardizeColumn(float[] data, int columnID) {
		
		float average = 0;
		for (int i = 0; i < data.length; i++) {
			average += data[i];
		}
		average /= data.length;
		
		float variance = 0;
		for (int i = 0; i < data.length; i++) {
			variance += average - (data[i] * data[i]);
		}
		variance = (float) Math.sqrt(Math.abs(variance/data.length));
		
		
		float[] standardized = new float[data.length];
		
		for (int i = 0; i < data.length; i++) {
			standardized[i] = (data[i] - average)/variance;
		}
		return standardized;
	}	
	
	
	/*
	 * ----------------------
	 * 
	 * 	Getters and Setters
	 * 
	 * ----------------------
	 */
	public void setMainImage(int newID) {
		if (newID < 0 || newID >= this.mainList.length) {
			this.mainImage = null;
			this.nearestNeighbors = null;
			return;
		}
		this.mainImageID = newID;
		this.nearestNeighbors = null;
		
		System.out.println("Updating mainImage " + this.mainImageID + " " + this.mainList.length);
		this.mainImage = this.mainList[this.mainImageID];
		this.getKNN(ViewCompare.numCompareImages);
	}

	public String getMainImagePath() {
		return this.mainPaths[this.mainImageID];
	}

	public String[] getCompareImages(int k) {
		if (this.nearestNeighbors == null) {
			this.getKNN(k);
		}
		String[] res = new String[this.nearestNeighbors.length];
		for (int i = 0; i < this.nearestNeighbors.length; i ++) {
			res[i] = this.comparePaths[this.nearestNeighbors[i]];
		}
		return res;
	}
	

}
