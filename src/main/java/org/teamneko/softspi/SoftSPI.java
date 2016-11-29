package org.teamneko.softspi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Software Implementation of an SPI port
 * @author Tommy « LeChat » Savaria
 */
public class SoftSPI {
	/**
	 * Clock Select pin number (wiringPi numbering scheme)
	 */
	public final int cs;
	
	/**
	 * Master In Slave Out pin number (wiringPi numbering scheme)
	 */
	public final int miso;
	
	/**
	 * Master Out Slave In pin number (wiringPi numbering scheme)
	 */
	public final int mosi;
	
	/**
	 * SPI Clock pin number (wiringPi numbering scheme)
	 */
	public final int clk;
	
	/**
	 * Bus speed in bits per second
	 */
	public final long speed;
	
	static {
		boolean loaded = false;
		try {
			loadLibraryFromJar("native/libSoftSpi.so");
			loaded = true;
		} catch (Throwable t) {	}
		
		if(!loaded) {
			try {
				System.loadLibrary("SoftSpi");
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Build a new SoftSPI object by specifying the pins to use (wiringPi scheme)
	 * @param cs Chip Select Pin
	 * @param miso Master In Slave Out Pin
	 * @param mosi Master Out Slave In Pin
	 * @param clk Clock Pin
	 * @param speed Speed of the SPI port in bits per second (b/s)
	 */
	public SoftSPI(int cs, int miso, int mosi, int clk, long speed) {
		this.cs = cs;
		this.miso = miso;
		this.mosi = mosi;
		this.clk = clk;
		this.speed = speed;
		
		initialize(this);
	}
	
	/**
     * Loads library from current JAR archive
     * 
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
     * Method uses String as filename because the pathname is "abstract", not system-dependent.
     * 
     * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @throws IOException If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If source file (param path) does not exist
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
     */
    private static void loadLibraryFromJar(String path) throws IOException {
 
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }
 
        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
 
        // Split filename to prexif and suffix (extension)
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; // Thanks, davs! :-)
        }
 
        // Check if the filename is okay
        if (filename == null || prefix.length() < 3) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }
 
        // Prepare temporary file
        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();
 
        if (!temp.exists()) {
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
        }
 
        // Prepare buffer for data copying
        byte[] buffer = new byte[1024];
        int readBytes;
 
        // Open and check input stream
        InputStream is = SoftSPI.class.getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }
 
        // Open output stream and copy data between source file in JAR and the temporary file
        OutputStream os = new FileOutputStream(temp);
        try {
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            // If read/write fails, close streams safely before throwing an exception
            os.close();
            is.close();
        }
 
        // Finally, load the library
        System.load(temp.getAbsolutePath());
    }
    
    /**
     * Initializes WiringPi and the GPIO for the SPI Device
     * @param spi SPI Pins Definition
     */
    public static native void initialize(SoftSPI spi);
    
    /**
     * Initiate a read/write communication on the SPI port
     * @param spi SPI Pins Definition
     * @param data Data to write on the SPI port
     * @return Data read from the SPI port
     */
	public static native byte[] readWrite(SoftSPI spi, byte[] data);
}
