package edu.carleton.its.johnsoav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;


/**
 * The model interacts solely with the controller, providing information from
 * the file system about images.
 * 
 * This is where the controller gets information about an image's
 * nearest neighbors
 * 
 * @author Avery Johnson
 *
 */
public class ICModel {

	/*
	 * ----------------------
	 * 
	 * 	Path information
	 * 
	 * ----------------------
	 */
	
	public static final String IMAGE_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/";
	public static final String JPEG_FOLDER = "jpgSmallTest/";
	public static final String TIFF_FOLDER = "tiffSmallTest/";
	public static final String OUTPUT_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/output/";
//	public static final String OUTPUT_PATH = System.getProperty("user.home") + "/Dropbox/";
	/*
	 * ----------------------
	 * 
	 * 	Instance Variables
	 * 
	 * ----------------------
	 */
	private ArrayList<String> tifSet;
	private ArrayList<String> jpgSet;
	public ICController controller;
	
	public String[][] matchedImages;
	public boolean[] approved;
	
	ImageCompare neighborGenerator;
	int mainImageID;
	
	int sessionID;
	
	/*
	 * ----------------------
	 * 
	 * 	Set initialization
	 * 
	 * ----------------------
	 */
	
	public ICModel() {
		this.sessionID = -1;
	}
	
	public ArrayList<String> getJPGImages() {
		if(jpgSet == null) {
			jpgSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
		}
		
		return jpgSet;
	}
	
	public ArrayList<String> getTIFImages() {
		if(tifSet == null) {
			tifSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		}
		
		return tifSet;
	}
	
	public void initImageSets() {
		tifSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		jpgSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
	}
	
	public void initSessionNumber() {
		ArrayList<String> sessionNames = this.getAllImagesInFolder(
				new File(ICModel.OUTPUT_PATH));
		int largest = 0;
		int curr = -1;
		for (int i = 0; i < sessionNames.size(); i++) {
			curr = this.getSessionNumberFromPath(sessionNames.get(i));
			largest = Math.max(curr, largest);
		}
		this.sessionID = largest + 1;
	}
	
	
	/*
	 * ----------------------
	 * 
	 * 	Neighbor Generator
	 * 
	 * ----------------------
	 */
	
	public void initGenerator() {
		if (this.jpgSet == null || this.tifSet == null) {
			this.initImageSets();
		}
		this.neighborGenerator = new ImageCompare(this.jpgSet, this.tifSet);
		this.mainImageID = 0;
		neighborGenerator.setMainImage(this.mainImageID);
	}
	
	public void nextMainImage() {
		if (this.neighborGenerator == null) {
			this.initGenerator();
			return;
		}
		this.mainImageID ++;
		this.neighborGenerator.setMainImage(this.mainImageID);
	}
	
	public void prevMainImage() {
		if (this.neighborGenerator == null) {
			this.initGenerator();
			return;
		}
		this.mainImageID --;
		this.neighborGenerator.setMainImage(this.mainImageID);
	}
	
	public int getMainImageID() {
		return this.mainImageID;
	}
	
	public String[] getNearestImages(int numNeighbors) {
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		return this.neighborGenerator.getCompareImages(numNeighbors);
	}
	

	
	public String getMainPath() {
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		return this.neighborGenerator.getMainImagePath();
	}
	
	public String[] getCompareImages(int numPaths) {
		System.out.println("Entering getCompareImages with k " + numPaths);
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		return this.neighborGenerator.getCompareImages(numPaths);
	}
	
	
	/*
	 * ----------------------
	 * 
	 * 	File System Methods
	 * 
	 * ----------------------
	 */
	
	public ArrayList<String> getAllImagesInFolder(final File folder) {
		ArrayList<String> res = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory()) {
	        	String fileName = fileEntry.getName();
	        	if (fileName.charAt(0) != '.')
	        		res.add(fileEntry.getPath());
	        }
	    }
	    return res;
	}
	
	public boolean saveSession(String[][] imagesMatched) {
		this.matchedImages = imagesMatched; 
		return this.writeToFile();
	}

	public String[] restoreSession(String sessionName) {
		// TODO model: create load function
		return new String[] {"SESSION"};
	}

	public boolean writeToLog(String imagesMatched) {
		// TODO model: write changed files to master log
		return false;
	}
	
	/*
	 * ----------------------
	 * 
	 * 	FIXME Methods
	 * 
	 * ----------------------
	 */
	
	private String generateFileString(){
		// in format:
		// decimalAccuracy (if known)	size of tifSet	size of jpgSet
		// tifName1	neighbor1	neighbor2	etc.
		String outputString = "timeStamp\ttif name\tjpg name\n";
		String timeStamp = getTimeStamp();
		for (int i = 0; i < this.matchedImages.length; i++) {
			outputString += timeStamp + "\t" + this.matchedImages[i][0] + "\t" + this.matchedImages[i][1];
			if (this.approved != null) {
				outputString += (approved[i]) ? "approved" : "condemned";
			}
			outputString += "\n";
		}
		
		return outputString;
	}
	
	public boolean writeToFile() {
		// write all resulting data to some file
		
		int sessionNumber = this.getSessionNumber();
		
		String outputString = generateFileString();
		
		try {
			File file = new File(ICModel.OUTPUT_PATH + "session"+ sessionNumber +".txt");
 
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(outputString);
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	
	/*
	 * ----------------------
	 * 
	 * 	Helper Methods
	 * 
	 * ----------------------
	 */
	
	public String getImageName(String filePath) {
		String[] splitPath = filePath.split("/");
    	return splitPath[splitPath.length - 1];
	}
	
	private int getSessionNumber() {
		if (this.sessionID < 0) {
			initSessionNumber();
		}
		return this.sessionID;
	}
	
	public void setController(ICController controller) {
		this.controller = controller;
	}
	
	public int getSessionNumberFromPath(String path) {
		Pattern p = Pattern.compile("([0-9]+).txt");
		Matcher m = p.matcher(path);
		m.find();
		String num = m.group(1);		
		return Integer.parseInt(num);
	}

	public String getTimeStamp()  {
		return (new Date()).toString();
	}
	
	
}
