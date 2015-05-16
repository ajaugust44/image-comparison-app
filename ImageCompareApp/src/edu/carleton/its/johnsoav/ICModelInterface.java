package edu.carleton.its.johnsoav;

import java.util.ArrayList;

public interface ICModelInterface {

	public ArrayList<String> getJPGImages();
	public ArrayList<String> getTIFImages();
	
	public boolean saveSession(String[][] imagesMatched);
	public String[] restoreSession(String sessionName);
	public boolean writeToLog(String imagesMatched);
	public void setController(ICController controller);
	
	
	
	
}
