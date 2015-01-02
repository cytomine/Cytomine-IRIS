package be.cytomine.apps.iris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This class manages properties stored in a specific .properties file.
 * 
 * @author Philipp Kainz
 * @since 1.5
 */
public class PropertyManager {

	protected File propFile;
	protected Properties props;

	/**
	 * Standard constructor.
	 */
	public PropertyManager() {
		this.props = new Properties();
	}

	/**
	 * Constructs a {@link PropertyManager} and stores the key-value pairs in
	 * the specified file.
	 * 
	 * @param f
	 */
	public PropertyManager(File f) {
		this();
		this.propFile = f;
	}

	/**
	 * Writes properties to a file.
	 * 
	 * @param props
	 * @param file
	 * @param comment
	 */
	public static synchronized void write(Properties props, File file,
			String comment) {
		try {
			OutputStream os = new FileOutputStream(file);
			props.store(os, comment);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads properties from a file.
	 * 
	 * @param file
	 * @param createIfNotExists
	 *            if <code>true</code>, the file will be created and the
	 *            properties associated with that file are returned, if
	 *            <code>false</code>, the method returns null
	 * 
	 * @return the {@link Properties} object
	 */
	public static synchronized Properties read(File file,
			boolean createIfNotExists) {
		try {
			Properties props = new Properties();
			if (!file.exists() && createIfNotExists) {
				write(props, file, null);
			}
			InputStream is = new FileInputStream(file);
			props.load(is);
			return props;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets a property manager for a {@link Class} name of a specific object.
	 * This method instantiates a property file for this class and loads it to
	 * the manager.
	 * 
	 * @param o
	 * @return the {@link PropertyManager} for a given class
	 */
	public static PropertyManager getManager(Object o) {
		String fileName = o.getClass().getName() + ".properties";

		File file = new File("tmp" + File.separator + fileName);

		PropertyManager pm = new PropertyManager(file);

		// fill the manager with the properties
		pm.read();

		// return the reference
		return pm;
	}

	/**
	 * Sets the file of this manager.
	 * 
	 * @param propFile
	 */
	public void setPropFile(File propFile) {
		this.propFile = propFile;
	}

	/**
	 * Gets the file of this manager.
	 * 
	 * @return the property file
	 */
	public File getPropFile() {
		return propFile;
	}

	/**
	 * Sets the property and writes it to the file.
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
		write(props, propFile, null);
	}

	/**
	 * Get a specific property by key.
	 * 
	 * @param key
	 * @return a {@link String}, the value associated with the key, or
	 *         <code>null</code> if the property is not found
	 */
	public String getProperty(String key) {
		props = read();
		return props.getProperty(key);
	}

	/**
	 * Reads the current property file.
	 * 
	 * @return the properties as {@link Properties} object
	 */
	public Properties read() {
		props = read(propFile, true);
		return props;
	}

	/**
	 * Writes the current property file.
	 */
	public void write() {
		// sort the properties
		write(props, propFile, null);
	}

	/**
	 * Gets the {@link Properties} object.
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return props;
	}

	/**
	 * Sets the properties.
	 * 
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	@Override
	public PropertyManager clone() {
		PropertyManager pm = new PropertyManager();
		
		pm.setPropFile(propFile);
		pm.getProperties().putAll(this.props);
		
		return pm;
	}
}
