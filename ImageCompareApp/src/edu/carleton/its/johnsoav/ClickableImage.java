package edu.carleton.its.johnsoav;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import processing.core.*;
import processing.opengl.Texture;

public class ClickableImage {
	PApplet parent;
	public int x;
	public int y;
	public int width;
	public int height;
	
	boolean clickable;
	String path;
	
	private PImage image;
	
	public ClickableImage(PApplet p, String imgPath, int x, int y, boolean quality) {
		this.init(p, imgPath, x, y, 0, 0, quality);
	}
	
	public ClickableImage(PApplet p, String imgPath, int x, int y, int w, int h, boolean quality) {
		this.init(p, imgPath, x, y, w, h, quality);
	}
	
	public ClickableImage(PApplet p, BufferedImage image, int x, int y, int w, int h, boolean quality) {
		this.init(p, image, x, y, w, h, quality);
	}
	
	public ClickableImage(PApplet p, BufferedImage image, int x, int y, boolean quality) {
		this.init(p, image, x, y, 0, 0, quality);
	}
	
	
	private void init(PApplet p, String imgPath, int x, int y, int w, int h, boolean quality) {
		this.clickable = true;
		this.parent = p;
		this.image = getAsImage(imgPath, w, h, quality);
		this.x = x;
		this.y = y;
		this.width = this.image.width;
		this.height = this.image.height;
		this.path = imgPath;
	}
	
	private void init(PApplet p, BufferedImage img, int x, int y, int w, int h, boolean quality) {
		this.clickable = true;
		this.parent = p;
		this.image = getAsImage(img, w, h, quality);
		this.x = x;
		this.y = y;
		this.width = this.image.width;
		this.height = this.image.height;
	}
	
	
	public void done() {
		Object cache = parent.getCache(image);
		if (cache instanceof Texture)
			((Texture) cache).disposeSourceBuffer();
		parent.removeCache(image);
	}
	
	
	public void drawImage(){
		try {
			parent.image(this.image, x, y );
		} catch (java.lang.OutOfMemoryError e) {
			System.err.println("Unable to load image");
		}
	}
	
	public boolean clicked() {
		if ( ! this.clickable ){
			return false;
		}
		boolean inX = (parent.mouseX >= this.x && parent.mouseX <= this.x + this.width);
		boolean inY = (parent.mouseY >= this.y && parent.mouseY <= this.y + this.height);

		return inX && inY;
	}
	
		
	public BufferedImage resize(String fileName, int maxW, int maxH, boolean quality) {
		BufferedImage bImg;
		File f;
		try {
			f = new File(fileName);
		} catch (Exception e) {
			System.err.println("error opening file " + fileName );
			return null;
		}
		try {
			bImg = ImageIO.read(f);
			return resize(bImg, maxW, maxH, quality);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public BufferedImage resize(BufferedImage bImg, int maxW, int maxH, boolean quality) {
		int newW = maxW, newH = maxH;
		
		int oldW = bImg.getWidth();
		int oldH = bImg.getHeight();
		
		float ratio = (float)(oldW)/oldH;
		if (ratio > (float)maxW/(float)maxH) {
			// width is the limiting factor: scale down so newW = maxW
			newH = (int) ((float)maxW / ratio);
		} else {
			// height is the limiting factor
			newW = (int) ((float)maxH * ratio);
		}
		try {
			BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resized.createGraphics();
			if (quality)
				g.drawImage(bImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, newW, newH, null);
			else
				g.drawImage(bImg.getScaledInstance(newW, newH, Image.SCALE_FAST), 0, 0, newW, newH, null);
			g.dispose();
			return resized;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public PImage getAsImage(String fileName, int maxWidth, int maxHeight, boolean quality) {
		try {
			BufferedImage bImg = resize(fileName, maxWidth, maxHeight, quality);
			PImage img=new PImage(bImg.getWidth(), bImg.getHeight(),PConstants.ARGB);
			bImg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
			img.updatePixels();
			return img;
		}
		catch(Exception e) {
			System.err.println("Can't create image from buffer");
			e.printStackTrace();
		}
		return null;
	}
	
	public PImage getAsImage(BufferedImage bImg, int maxWidth, int maxHeight, boolean quality) {
		try {
			bImg = resize(bImg, maxWidth, maxHeight, quality);
			PImage img=new PImage(bImg.getWidth(), bImg.getHeight(),PConstants.ARGB);
			bImg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
			img.updatePixels();
			return img;
		}
		catch(Exception e) {
			System.err.println("Can't create image from buffer");
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public int[] getNewDimensions(BufferedImage bImg, int maxW, int maxH) {
		int maxWidth = maxW, maxHeight = maxH;
		if (maxW == 0)  {
			maxWidth = bImg.getWidth();
		}
		if (maxH == 0) {
			maxHeight = bImg.getHeight();
		}
		
		maxWidth = Math.min(Math.min(maxWidth, bImg.getWidth()), parent.getWidth());
		maxHeight = Math.min(Math.min(maxHeight, bImg.getHeight()), parent.getHeight());
		
		
		float sizeRatio = (float)(maxHeight)/(float)(bImg.getHeight());
		int newHeight = maxHeight;
		int newWidth = maxWidth;
		if (((float)maxWidth)/bImg.getWidth() > ((float)maxHeight)/bImg.getHeight()) {
			sizeRatio = (float)(maxHeight)/(float)(bImg.getHeight());
			newHeight = (int) (bImg.getHeight() * sizeRatio);
		} else {
			sizeRatio = (float)(maxWidth)/(float)(bImg.getWidth());
			newWidth = (int) (bImg.getWidth() * sizeRatio);
		}
		assert(newWidth > 0 && newHeight > 0);
		assert(newWidth <= maxW && newHeight <= maxH);
		
		return new int[] {newWidth, newHeight};
	}
	
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
	
	
	
}
