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
	public final int cs;
	public final int miso;
	public final int mosi;
	public final int clk;
	
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
	 */
	public SoftSPI(int cs, int miso, int mosi, int clk) {
		this.cs = cs;
		this.miso = miso;
		this.mosi = mosi;
		this.clk = clk;
	}
	
	/**
	 * Read a register from the SPI device
	 * @param address Register address to read
	 * @return Value in the register
	 */
	public byte read(byte address) {
		return readRegister(this, (byte)((address << 1) | 0x80));
	}
	
	/**
	 * Write to a register on the SPI device
	 * @param address Register address to write
	 * @param value Value to write to the register
	 */
	public void write(byte address, byte value) {
		writeRegister(this, (byte)((address << 1) & 0x7F), value);
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
	 * Read a register from the SPI Device
	 * @param spi SPI Pins Definition
	 * @param reg Address of the register to read
	 * @return Value read from the register
	 */
	private static native byte readRegister(SoftSPI spi, byte reg);
	
	/**
	 * Write a register on the SPI Device
	 * @param spi SPI Pins Definition
	 * @param reg Address of the register to write
	 * @param value Value to write into the register
	 */
	private static native void writeRegister(SoftSPI spi, byte reg, byte value);
}
