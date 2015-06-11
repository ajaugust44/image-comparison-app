package edu.carleton.its.johnsoav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
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


	public static void main(String[] args) {
		ICModel m = new ICModel();

		m.serializeImageSets();
	}

	/*
	 * ----------------------
	 * 
	 * 	Path information
	 * 
	 * ----------------------
	 */

	public static final String IMAGE_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/";
	public static final String JPEG_FOLDER = "jpgTestSet/";
	public static final String TIFF_FOLDER = "tiffTestSet/";
	public static final String OUTPUT_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/output/";
	//	public static final String OUTPUT_PATH = System.getProperty("user.home") + "/Dropbox/";

	public static final String RENAMED_FOLDER = System.getProperty("user.home") + "/work2/esternayFileCompare/renamedTiffs/";
	public static final String USED_JPG_FOLDER = System.getProperty("user.home") + "/work2/esternayFileCompare/usedJPGs/";

	public static final String MASTER_LOG_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/output/masterLog.txt";

	/*
	 * ----------------------
	 * 
	 * 	Instance Variables
	 * 
	 * ----------------------
	 */

	// These are lists of strings that contain the paths of all tifs and 
	// jpgs, respectively
	private ArrayList<String> tifSet;
	private ArrayList<String> jpgSet;

	private ImageData[] tifData;
	private ImageData[] jpgData;

	public ICController controller;

	// This array contains a list of all images that have been matched
	// Element i contains a list [tifImagePath, jpgImagePath]
	public String[][] matchedImages;

	// This object generates the nearest (jpg) neighbors for the currently 
	// selected "main image" (a tif)
	ImageCompare neighborGenerator;

	// This contains the index in the tifSet of the current main image
	int mainImageID;

	// This is an integer identifier that is used to name the session save file
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

	/**
	 * Initializes both image sets.
	 */
	public void initImageSets() {
		tifSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		jpgSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
	}


	/**
	 * Looks through the file system and finds a list of all jpg images
	 * 
	 * FIXME CAN READ IN FROM TEXT FILE INSTEAD TO GET INFO
	 * @return ArrayList of string jpg paths
	 */
	public ArrayList<String> getJPGImages() {
		if(jpgSet == null) {
			jpgSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
		}

		return jpgSet;
	}

	/**
	 * Looks through the file system and finds a list of all tif images
	 * 
	 * FIXME Why not these too? (then we can standardize? And it's faster!)
	 * 
	 * @return ArrayList of string tif paths
	 */
	public ArrayList<String> getTIFImages() {
		if(tifSet == null) {
			tifSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		}

		return tifSet;
	}

	public void initData() {
		if (this.tifSet == null || this.jpgSet == null) {
			this.initImageSets();
		}
		this.tifData = new ImageData[tifSet.size()];

		for (int i = 0; i < this.tifSet.size(); i ++) {
			tifData[i] = new ImageData(tifSet.get(i));
		}

		this.jpgData = new ImageData[jpgSet.size()];

		for (int i = 0; i < this.jpgSet.size(); i ++) {
			jpgData[i] = new ImageData(jpgSet.get(i));
		}

	}

	public void serializeImageSets() {
		if (this.tifSet == null || this.jpgSet == null) {
			this.initImageSets();
		}
		if (this.tifData == null || this.jpgData == null) {
			this.initData();
		}


		try{ 
			FileOutputStream fout = new FileOutputStream(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER + "/jpgData.est");
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(jpgData);

			fout = new FileOutputStream(ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER + "/tifData.est");
			oos = new ObjectOutputStream(fout);
			oos.writeObject(tifData);


			oos.close();
			System.out.println("Serialization done");

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Looks through the file system to find the most recent session number, then increments
	 *  to get the current session number
	 */
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

	/**
	 * Initializes the nearest-neighbor generator, then sets it up to find the nearest
	 * neighbors of the first tif image
	 */
	public void initGenerator() {
		if (this.jpgSet == null || this.tifSet == null) {
			this.initImageSets();
		}
		if (this.jpgData != null && this.tifData != null) {
			this.neighborGenerator = new ImageCompare(this.jpgData, this.tifData);
		} else {
			this.neighborGenerator = new ImageCompare(this.jpgSet, this.tifSet);
		}
		this.mainImageID = 0;
		neighborGenerator.setMainImage(this.mainImageID);
	}

	/**
	 * Selects the next main image for comparison
	 */
	public void nextMainImage() {
		if (this.neighborGenerator == null) {
			this.initGenerator();
			return;
		}
		this.mainImageID ++;
		this.neighborGenerator.setMainImage(this.mainImageID);
	}


	/**
	 * Selects the previous main image for comparison
	 */
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

	public String getMainPath() {
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		return this.neighborGenerator.getMainImagePath();
	}

	/**
	 * Has the neighborGenerator calculate some number of nearest neighbors
	 * 
	 * @param numNeighbors: How many neighbors the neighborGenerator should calculate
	 * @return the numNeighbors nearest neighbors to the currently selected main image.
	 */
	public String[] getCompareImages(int numNeighbors) {
		if (this.neighborGenerator == null) {
			this.initGenerator();
		}
		return this.neighborGenerator.getCompareImages(numNeighbors);
	}

	public void setData(String fileName) {
		System.out.println("DATAFILE " + fileName);

		try{

			FileInputStream fin = null;
			ObjectInputStream ois = null;
			if (fileName.contains("jpg")) {
				fin = new FileInputStream(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER + "/jpgData.est");
				ois = new ObjectInputStream(fin);
				jpgData = (ImageData[]) ois.readObject();
			} else if (fileName.contains("tif")) {
				fin = new FileInputStream(ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER + "/tifData.est");
				ois = new ObjectInputStream(fin);
				tifData = (ImageData[]) ois.readObject();
			} else {
				return;
			}
			ois.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	/*
	 * ----------------------
	 * 
	 * 	File System Methods
	 * 
	 * ----------------------
	 */

	/**
	 * Goes through all of a given folder and creates an arrayList of all
	 * filenames in that folder. Ignores folders.
	 * 
	 * @param folder
	 * @return: ArrayList of all filenames in a given folder.
	 */
	public ArrayList<String> getAllImagesInFolder(final File folder) {
		ArrayList<String> res = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				String fileName = fileEntry.getName();
				if (fileName.contains(".est")){
					setData(fileName);
				} else if (fileName.charAt(0) != '.')
					res.add(fileEntry.getPath());
			}
		}
		return res;
	}

	/** 
	 * Intermediate save: session file only
	 * @param imagesMatched
	 * @return
	 */
	public boolean saveSession(String[][] imagesMatched) {
		this.matchedImages = imagesMatched; 
		return this.writeToFile();
	}


	/**
	 *  Final save: master log and session file
	 * @param imagesMatched: list of images that have been matched so far
	 * 
	 */
	public void completeSession(String[][] imagesMatched) {
		this.matchedImages = imagesMatched;
		this.writeToFile();
		this.renameAndReserialize();
		this.writeToMasterFile();

	}

	/*
	 * ----------------------
	 * 
	 * 	Methods
	 * 
	 * ----------------------
	 */

	private String generateFileString(){
		// in format:
		// timestamp	tifName1	match
		String outputString = "timeStamp\ttif name\tjpg name\n";
		String timeStamp = getTimeStamp();
		for (int i = 0; i < this.matchedImages.length; i++) {
			outputString += timeStamp + "\t" + this.matchedImages[i][1] + "\t" + this.matchedImages[i][0];
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

	public void renameAndReserialize() {
		// Go through approved tif/jpg matches
		// Rename the tifs to have the same name as the jpgs + tif

		ImageData[] newTifData = new ImageData[this.tifData.length - matchedImages.length];
		ImageData[] newJPGData = new ImageData[this.jpgData.length - matchedImages.length];

		String jpgName, newTifName, oldTifName;
		for (int i = 0; i < this.matchedImages.length; i ++) {
			jpgName = this.matchedImages[i][0];
			jpgName = this.getImageName(jpgName);
			newTifName = jpgName.substring(0, jpgName.length() - 4) + ".tif";
			System.out.println("new tif name " + newTifName);

			try{
				File tifFile = new File(this.matchedImages[i][1]);
				oldTifName = tifFile.getName();
				File jpgFile = new File(this.matchedImages[i][0]);
				if(tifFile.renameTo(new File(ICModel.RENAMED_FOLDER + newTifName))){
					System.out.println("Successfully renamed " + newTifName);
					for(int j = 0; j < this.tifData.length; j++) {

						if (this.tifData[j] != null) {
							if (oldTifName.equals(this.getImageName(this.tifData[j].path))) {
								this.tifData[j] = null;
							}
						}
					}
				} else{
					System.out.println("Unsuccessful move " + newTifName );
					System.out.println();
				}
				if(jpgFile.renameTo(new File(ICModel.USED_JPG_FOLDER + jpgFile.getName()))){
					System.out.println("Successfully renamed " + jpgFile.getName());
					for(int j = 0; j < this.jpgData.length; j++) {
						if (this.jpgData[j] != null) {
							if (jpgName.equals(this.getImageName(this.jpgData[j].path))) {
								System.out.println("removing " + jpgData[j] + " at " + j);
								this.jpgData[j] = null;
								
							}
						}
					}
				} else{
					System.out.println("Unsuccessful move " + jpgFile.getName() );
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		int j = 0;
		for (int i = 0; i < tifData.length; i++) {
			if (tifData[i] != null) {
				newTifData[j] = tifData[i];
				j++;
			}
		}
		j = 0;
		for (int i = 0; i < jpgData.length; i++) {
			if (jpgData[i] != null) {
				newJPGData[j] = jpgData[i];
				j++;
			}
		}
		
		this.tifData = newTifData;
		this.jpgData = newJPGData;
		
		
		this.serializeImageSets();

		// Create tifData again, then serialize that again. (delete old file
		// then recreate, to be safe.
	}

	private String generateMasterFileString(){
		// in format:
		// timestamp	tifName1	match
		String outputString = "";
		String timeStamp = getTimeStamp();
		for (int i = 0; i < this.matchedImages.length; i++) {
			outputString += timeStamp + "\t" + this.matchedImages[i][1] + "\t" + this.matchedImages[i][0];
			outputString += "\n";
		}

		return outputString;
	}






	public void writeToMasterFile() {
		File file = new File(ICModel.MASTER_LOG_PATH);

		String outputString = generateMasterFileString();
		PrintWriter out = null;
		boolean created = false;
		// if file doesn't exists, then create it
		try {
			if (!file.exists()) {
				file.createNewFile();
				created = true;
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(ICModel.MASTER_LOG_PATH, true)));

			if (created)  {
				outputString = "timeStamp\ttif name\tjpg name\n" + outputString;
			}
			out.println(outputString);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (out != null) {
				out.close();
			}
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
		if(m.find()) {
			String num = m.group(1);		
			return Integer.parseInt(num);
		}
		else {
			return -1;
		}
	}

	public String getTimeStamp()  {
		return (new Date()).toString();
	}

}
