package edu.carleton.its.johnsoav;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

/**
 * This class is designed to work together with the ImageCompare class.
 * It should contain an image, and a string representation of all information
 * about that image necessary to compare it to others.
 * 
 * @author ajaugust44
 */


public class ImageInfo {
	
	public static void main(String[] args) {
		System.out.println("Did you mean to run ImageCompare instead?");
		
		ImageInfo img = new ImageInfo("/Users/ajaugust44/work2/esternayFileCompare/1-1.jpg");
//		System.out.println(img.greyThreshold);
		
		img.makeDuplicateImage();
	}
	
	public final static int NUM_CHARS = 10;	
	
	BufferedImage image;
	String imageName;
	String path;

	int width, height;
	
	
	Float[] averageBG;
	Float[] averageText;

	int greyThreshold;
	
	Float bgRatio;
	Float textRatio;
	Float aspectRatio;
	float[] highestLeftPoint;
	float[] lowestRightPoint;
	
	int[][] greyLevels;
	
	
	float allInfo[];
	
	public ImageInfo(String imagePath) {
		this.initImage(imagePath);
		this.initAll();
	}
	
	public ImageInfo(String imagePath, float[] allInfo) {
		this.path = imagePath;
//		this.initImage(path);
		this.allInfo = allInfo;
	}
	
	
	public void initImage(String imagePath) {
		this.path = imagePath;
		try {
        	image = ImageIO.read(new File(imagePath));
        	String[] splitPath = imagePath.split("/");
        	this.imageName = splitPath[splitPath.length - 1];
        	System.out.println("Reading image "+ imageName);
        } catch (IOException e) {
            System.err.println("Image could not be read\n"+e.getStackTrace());
        }
		
		if (image == null) {
			System.err.println("Null image: " + imageName);
			System.exit(0);
		}
	}
	
	public void initAll() {
		this.width = image.getWidth();
    	this.height = image.getHeight();
    	
    	this.initRGBVals();
    	
    	this.initThreshold();
    	
    	this.calculateAverages();
    	this.calculateRatio();
    	this.calculateLineInfo();
    	
    	this.getAllInfo();
    	this.clearImage();
	}
	
	
	private void initRGBVals() {
		averageBG = new Float[3];
        averageText = new Float[3];
        for (int i = 0; i < 3; i ++) {
        	averageBG[i] = (float) 0;
        	averageText[i] = (float) 0;
        }

	}
	
	public void initThreshold() {
		// create array of greyLevels
		calcGreyLevels();
		
		//calculate the probability that a pixel has a grey value for all 
		// grey values, 0 - 256
		
		// Also calculate the total average grey level for all pixels in image
		
		
		double[] levelProbs = new double[256];
		for (int x = 0; x < width; x ++) {
			for (int y=0; y < height; y ++ ) {
				levelProbs[this.greyLevels[x][y]] += 1;
			}
		}
		
		float total = 0;
		for (int i = 0; i < levelProbs.length; i ++) {
			total += levelProbs[i];
			levelProbs[i] /= width * height;
		}
		assert (total == width * height);
		
		// Calculate the probability a pixel is below a certain threshold
		// for all thresholds 0 - 256
		double[] probBelowThresh = new double[256];
		probBelowThresh[0] = levelProbs[0];
		for (int i = 1; i < levelProbs.length; i ++) {
			probBelowThresh[i] = probBelowThresh[i-1] + levelProbs[i];
		}
		
		//mean level for each threshold
		double[] allMeans = new double[256];
		allMeans[0] = levelProbs[0];
		for (int i = 1; i < 256; i++) {
			allMeans[i] = allMeans[i-1] + (i+1) * levelProbs[i];
		}
		
		double bestMeasure = calcObjective(allMeans[255], levelProbs[0], allMeans[0]);
		int bestThresh = 0;
		double measure;
		
		for (int i = 1; i < 200; i ++){
			measure = calcObjective(allMeans[255], levelProbs[i], allMeans[i]);
			if (measure > bestMeasure) {
				bestThresh = i;
				bestMeasure = measure;
			}
		}
		
		this.greyThreshold = bestThresh;
	}
	
	private double calcObjective(double totalAverage, double classProb, double threshMean) {
		double num = Math.pow((totalAverage * classProb) - threshMean, 2);
		double denom = classProb * (1-classProb);
		
		if (denom == 0) {
			return 0;
		}
		
		return num/denom;
	}
	
	
	private void calcGreyLevels() {
		int[] rgba;
		this.greyLevels = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y ++) {
				rgba = getRGBAFromPixel(image.getRGB(x, y));
				this.greyLevels[x][y] = (rgba[0] + rgba[1] + rgba[2]) / 3;
			}
		}
	}
	
	
	public void makeDuplicateImage() {
		BufferedImage img = new BufferedImage(this.width, this.height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x ++) {
			for (int y = 0; y < height; y ++) {
				if (this.greyLevels[x][y] > this.greyThreshold) {
					img.setRGB(x, y, intToRGB(255, 255, 255));
				} else {
					img.setRGB(x, y, intToRGB(0, 0, 0));
				}
				
			}
		}
		
		highlightTextBox(img);
		try {
			ImageIO.write(img, "jpg", new File(
					"/Users/ajaugust44/work2/esternayFileCompare/output/" + this.imageName + "Duplicate.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void highlightTextBox(BufferedImage img) {
		int color = intToRGB(255, 0, 0);
		int x= (int) (this.highestLeftPoint[0] * width) ;
		int y = (int) (this.highestLeftPoint[1] * height);
		int sideLength = 5;
		
		for (int i = Math.max(x - sideLength, 0); i < Math.min(x+sideLength, width); i ++) {
			for (int j = Math.max(y - sideLength, 0); j < Math.min(y+sideLength, height); j ++) {
				img.setRGB(i, j, color);
			}
		}
		
		color = intToRGB(0,0,255);
		x = (int) (this.lowestRightPoint[0] * width);
		y = (int) (this.lowestRightPoint[1] * height);
		for (int i = Math.max(x - sideLength, 0); i < Math.min(x+sideLength, width); i ++) {
			for (int j = Math.max(y - sideLength, 0); j < Math.min(y+sideLength, height); j ++) {
				img.setRGB(i, j, color);
			}
		}
				
		
	}
	
	/**
	 * This function calculates the average color above and below some
	 * thresholds
	 */
	public void calculateAverages() {
        int numPixelsBG =  0;
        int numPixelsText = 0;
        int[] rgba;
        
        for( int x = 0; x < width; x ++ ) {
        	for ( int y = 0; y < height; y ++) {
        		rgba = getRGBAFromPixel(image.getRGB(x, y));
        		if (aboveThreshold(rgba)) {
        			for (int i = 0; i < 3; i++) {
            			averageBG[i] += rgba[i];
            		}
        			numPixelsBG += 1;
        		} else if (belowThreshold(rgba)){
        			for (int i = 0; i < 3; i++) {
            			averageText[i] += rgba[i];
            		}
        			numPixelsText += 1;
        		}
        		
        		
        	}
        }
        for (int i = 0; i < 3; i++) {
        	averageBG[i] /= numPixelsBG;
		}
        for (int i = 0; i < 3; i++) {
        	averageText[i] /= numPixelsText;
		}
        
        float numPixels = width * height;
        this.bgRatio = numPixelsBG / numPixels;
        this.textRatio = numPixelsText / numPixels;
	}
	
	public void calculateRatio() {
		this.aspectRatio = (float) this.width/ (float) this.height;
	}
	
	
	public void calculateLineInfo() {
		/* go through image keeping track of line information:
		 	Initially: 
		 		- just a text box: lowest x, y, highest x, y;
		*/
		boolean setHighestPoint = false;
		
		highestLeftPoint = new float[2];
		lowestRightPoint = new float[2];
		
		int[] rgba;
		
		for (int x = 0; x < width; x ++) {
			for (int y = 0; y < height; y ++) {
        		rgba = getRGBAFromPixel(image.getRGB(x, y));
        		if (belowThreshold(rgba)) {
        			if (!setHighestPoint) {
        				setHighestPoint = true;
        				highestLeftPoint[0] = (float) x / width;
        				highestLeftPoint[1] = (float) y / height;
        			}
        			if (y >= lowestRightPoint[0] / height){
        				lowestRightPoint[0] = (float) x / width;
        				lowestRightPoint[1] = (float) y / height;
        			}
        		}
			}
		}
	}

	public boolean aboveThreshold(int[] rgba) {
		
		return ((rgba[0] + rgba[1] + rgba[2])/3.0) > this.greyThreshold;
	}
	
	public boolean belowThreshold(int[] rgba) {
		return ((rgba[0] + rgba[1] + rgba[2])/3.0) <= this.greyThreshold;
	}
	
	
	/**
	 * 
	 * This function is to be used to create data for mining information about 
	 * image similarity.
	 * 
	 * @return: list of all information about this particular image
	 */
	public float[] getAllInfo() {
		if (allInfo != null) {
			return allInfo;
		}
		
		allInfo = new float[NUM_CHARS];
		
		int index = 0;
		for ( int i = 0; i < 3; i++) {
			allInfo[index] = averageBG[i];
			index ++;
		}
		for ( int i = 0; i < 3; i++) {
			allInfo[index] = averageText[i];
			index ++;
		}
		
		allInfo[index] = bgRatio;
		index ++;
		allInfo[index] = textRatio;
		index ++;
		allInfo[index] = aspectRatio;
		index++;
		allInfo[index] = aspectRatio;
		
//		index++;
//		for (int i = 0; i < 2; i ++) {
//			allInfo[index] = highestLeftPoint[i] + "";
//			index++;
//			allInfo[index] = lowestRightPoint[i] + "";
//			index ++;
//		}
		
		return allInfo;
	}
	
	public int[] getRGBAFromPixel(int pixel) {
		int alpha = (pixel >> 24) & 0xff;
	    int red = (pixel >> 16) & 0xff;
	    int green = (pixel >> 8) & 0xff;
	    int blue = (pixel) & 0xff;
	    
	    int[] argb = {red, green, blue, alpha};
	    return argb;
	}
	
	public int intToRGB(int r, int g, int b) {
		return ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);
	}
	
	public void clearImage() {
		this.image = null;
		System.gc();
	}
	
	public String getImagePath() {
		return this.path;
	}
	
	
	
	private String generateFileString(double[] greySums){
		// in format:
		// fileName
		// levelNum		greySumAtLevel
		String outputString = "";
		
		outputString += this.imageName + "\n";
		for (int i = 0; i < greySums.length; i++) {
			outputString += i + "\t" + greySums[i] + "\n";
		}
		outputString += "\n";
		
		return outputString;
	}
	
	public void writeToFile(double[] greySums) {
		// write all resulting data to some file
		
		
		String outputString = generateFileString(greySums);
		try {
			File file = new File("/Users/ajaugust44/work2/esternayFileCompare/output/imageInfo.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(outputString);
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

