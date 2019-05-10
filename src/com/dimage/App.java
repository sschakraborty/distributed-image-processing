package com.dimage;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class App {
	private final static PrintWriter OUT, ERR;
	private final static BufferedReader IN;
	private final static String IMAGE_PATH;
	private final static ExecutorService POOL;
	private static BufferedImage IMAGE;
	private static int IMAGE_SEGMENT_BASE_NUM;
	private static byte[][][] BYTES;
	
	static {
		OUT = new PrintWriter(new OutputStreamWriter(System.out));
		ERR = new PrintWriter(new OutputStreamWriter(System.err));
		IN = new BufferedReader(new InputStreamReader(System.in));
		IMAGE_SEGMENT_BASE_NUM = -1;
		
		POOL = Executors.newFixedThreadPool(16);
		
		String tempPath = null;
		try {
			App.pr("Enter segments / axis (N - default 1): ");
			App.flush();
			String p = IN.readLine().trim();
			
			IMAGE_SEGMENT_BASE_NUM = Math.abs(Integer.parseInt((p.length() == 0) ? "1" : p));
			IMAGE_SEGMENT_BASE_NUM = Math.min(64, IMAGE_SEGMENT_BASE_NUM);
			App.pr("Using segments / axis (N): ");
			App.prln("" + IMAGE_SEGMENT_BASE_NUM);
			
			BYTES = new byte[IMAGE_SEGMENT_BASE_NUM][IMAGE_SEGMENT_BASE_NUM][];
			
			App.pr("Enter image path (absolute): ");
			App.flush();
			tempPath = IN.readLine();
		} catch(IOException e) {
			App.errln("Error while reading master image path!");
			App.flushErr();
		} finally {
			IMAGE_PATH = tempPath;
			BufferedImage tempImage = null;
			try {
				File imageFile = new File(IMAGE_PATH);
				if(imageFile.exists() && imageFile.canRead()) {
					App.prln("Reading image...");
					App.flush();
					tempImage = ImageIO.read(imageFile);
					App.prln("Image read...");
					App.flush();
				} else {
					App.err("File corresponding to the entered path does not exist ");
					App.errln("or does not have read permissions.");
					App.flushErr();
					System.exit(0);
				}
			} catch(IOException e) {
				App.err("Error while reading image: ");
				App.errln(e.getMessage());
				App.flushErr();
				System.exit(0);
			} finally {
				App.prln("Pre-serializing and caching sub-images!");
				App.flush();
				
				try {
					IMAGE = tempImage;
					int width = IMAGE.getWidth() / App.getDivisionNum();
					int height = IMAGE.getHeight() / App.getDivisionNum();
					for(int i = 0; i < App.getDivisionNum(); i++) {
						for(int j = 0; j < App.getDivisionNum(); j++) {
							BufferedImage im = IMAGE.getSubimage(i * width, j * height, width, height);
							BYTES[i][j] = new ImageContext(i * width, j * height, im).getBytes();
						}
					}
				} catch(IOException e) {
					App.errln("Error in IO during caching");
					App.errln(e.getMessage());
					App.flushErr();
					System.exit(0);
				}
				App.prln("Breaking process completed!");
				App.prln("Pre-serializing and caching sub-images completed!");
				App.flush();
			}
		}
	}
	
	public static void pr(String s) {
		OUT.print(s);
	}
	
	public static void prln(String s) {
		OUT.println(s);
	}
	
	public static void flush() {
		OUT.flush();
	}
	
	public static void err(String s) {
		ERR.print(s);
	}
	
	public static void errln(String s) {
		ERR.println(s);
	}
	
	public static void flushErr() {
		ERR.flush();
	}
	
	public static String readLine() {
		try {
			return IN.readLine();
		} catch(IOException e) {
			return null;
		}
	}
	
	public static int getSegmentNum() {
		return IMAGE_SEGMENT_BASE_NUM * IMAGE_SEGMENT_BASE_NUM;
	}
	
	public static int getDivisionNum() {
		return IMAGE_SEGMENT_BASE_NUM;
	}
	
	public static String getDirPath() {
		return IMAGE_PATH;
	}
	
	public static BufferedImage getImage() {
		return IMAGE;
	}
	
	public static byte[][][] getContexts() {
		return BYTES;
	}
	
	public static ExecutorService getThreadPool() {
		return POOL;
	}
}