package edu.carleton.its.johnsoav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ICModel implements ICModelInterface {

	public static final String IMAGE_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/";
	public static final String JPEG_FOLDER = "jpgSmallTest/";
	public static final String TIFF_FOLDER = "tiffSmallTest/";
	public static final String OUTPUT_PATH = System.getProperty("user.home") + "/work2/esternayFileCompare/output/";
	
	
	private ArrayList<String> tifSet;
	private ArrayList<String> jpgSet;
	public ICController controller;
	
	public String[][] matchedImages;
	
	@Override
	public ArrayList<String> getJPGImages() {
		if(jpgSet == null) {
			jpgSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
		}
		
		return jpgSet;
	}
	
	@Override
	public ArrayList<String> getTIFImages() {
		if(tifSet == null) {
			tifSet = getAllImagesInFolder(new File(ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		}
		
		return tifSet;
	}
	
	
	@Override
	public boolean saveSession(String[][] imagesMatched) {
		this.matchedImages = imagesMatched; 
		return this.writeToFile();
	}

	@Override
	public String[] restoreSession(String sessionName) {
		// TODO Auto-generated method stub
		return new String[] {"SESSION"};
	}

	@Override
	public boolean writeToLog(String imagesMatched) {
		// TODO Auto-generated method stub
		return false;
	}

	
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
	
	public void getImageSets() {
		tifSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.TIFF_FOLDER));
		jpgSet = this.getAllImagesInFolder(new File(
				ICModel.IMAGE_PATH + ICModel.JPEG_FOLDER));
	}
	
	public String getImageName(String filePath) {
		String[] splitPath = filePath.split("/");
    	return splitPath[splitPath.length - 1];
	}

	@Override
	public void setController(ICController controller) {
		this.controller = controller;
	}
	
	
	private String generateFileString(){
		// in format:
		// decimalAccuracy (if known)	size of tifSet	size of jpgSet
		// tifName1	neighbor1	neighbor2	etc.
		String outputString = "tif name\tjpg name\n";
		
		for (int i = 0; i < this.matchedImages.length; i++) {
			outputString += this.matchedImages[i][0] + "\t" + this.matchedImages[i][1] + "\n";
		}
		
		return outputString;
	}
	
	private int getSessionNumber() {
		return 0;
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
	
}
