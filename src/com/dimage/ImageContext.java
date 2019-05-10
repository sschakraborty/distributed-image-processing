package com.dimage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class ImageContext implements Serializable {
	private final int startX, startY;
	private final BufferedImage image;
	
	public ImageContext(int startX, int startY, BufferedImage image) {
		this.startX = startX;
		this.startY = startY;
		this.image = image;
	}
	
	public int getStartX() {
		return startX;
	}
	
	public int getStartY() {
		return startY;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeInt(startX);
		objStream.writeInt(startY);
		ImageIO.write(image, "png", objStream);
		return byteStream.toByteArray();
	}
	
	public static ImageContext readBytes(byte[] array) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(array);
		ObjectInputStream objStream = new ObjectInputStream(inputStream);
		int startX = objStream.readInt();
		int startY = objStream.readInt();
		BufferedImage image = ImageIO.read(objStream);
		return new ImageContext(startX, startY, image);
	}
}