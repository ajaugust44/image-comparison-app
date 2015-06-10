package edu.carleton.its.johnsoav;

import java.io.Serializable;

class ImageData implements Serializable {

	private static final long serialVersionUID = 1L;


	String path;
	float[] data;

	//create ImageInfo constructor that tuns the ii into an id

	ImageData(ImageInfo imageInfo) {
		this.path = imageInfo.path;
		this.data = imageInfo.getAllInfo();
	}

	ImageData(String path){
		this.path = path;
		this.data = (new ImageInfo(path)).getAllInfo();
	}

	@Override
	public String toString() {
		return new StringBuffer(" Path : ")
		.append(this.path)
		.append(" Data : ")
		.append(this.data).toString();
	}

	public ImageInfo toImageInfo() {
		return new ImageInfo(this.path, this.data);
	}
	
}