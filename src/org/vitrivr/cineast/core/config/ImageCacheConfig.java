package org.vitrivr.cineast.core.config;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonObject;


public final class ImageCacheConfig {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final Policy DEFAULT_POLICY = Policy.AUTOMATIC;
	public static final int DEFAULT_SOFT_LIMIT = 3096;
	public static final int DEFAULT_HARD_LIMIT = 2048;
	public static final File DEFAULT_CACHE_LOCATION = new File(".");
	
	public static enum Policy{
		FORCE_DISK_CACHE, //cache to disk even if newInMemoryMultiImage is requested
		DISK_CACHE, //cache to disk unless newInMemoryMultiImage is requested
		AUTOMATIC, //keep in memory as long as threshold is not exceeded, makes exceptions for images not generated by the video decoder
		AVOID_CACHE //avoids cache until hard limit is reached
	}
	
	private final Policy cachingPolicy;
	private final long softMinMemory;
	private final long hardMinMemory;
	private final File cacheLocation;
	
	/**
	 * 
	 * @param softMemoryLimit Soft memory limit for in-memory frame cache in MB
	 * @param hardMemoryLimit Hard memory limit for in-memory frame cache in MB
	 * @param cachePolicy Caching Policy
	 * @param cacheLocation the file system location of the disk cache
	 * @throws IllegalArgumentException in case any of the memory limits is negative
	 * @throws NullPointerException in case the cachePolicy or cacheLocation is null
	 * @throws SecurityException in case access to cacheLocation is not permitted
	 */
	public ImageCacheConfig(int softMemoryLimit, int hardMemoryLimit, Policy cachePolicy, File cacheLocation){
		if(softMemoryLimit < 0){
			throw new IllegalArgumentException("Memorylimit must me positive");
		}
		if(hardMemoryLimit < 0){
			throw new IllegalArgumentException("Memorylimit must me positive");
		}
		if(cachePolicy == null){
			throw new NullPointerException("CachePolicy cannot be null");
		}
		if(cacheLocation == null){
			throw new NullPointerException("CacheLocation cannot be null");
		}
		if(!(cacheLocation.exists() && cacheLocation.isDirectory()) || cacheLocation.mkdirs()){
			this.cacheLocation = new File(".");
			LOGGER.warn("Specified cache location ({}) is invalid, using default location: {}", cacheLocation.getAbsolutePath(), this.cacheLocation.getAbsolutePath());
		}else{
			this.cacheLocation = cacheLocation;
		}
		this.softMinMemory = 1024L * 1024L * softMemoryLimit;
		this.hardMinMemory = 1024L * 1024L * hardMemoryLimit;
		this.cachingPolicy = cachePolicy;
	}
	
	/**
	 * Creates an ImageMemoryConfig object with the default configuration. This is equivalent to ImageMemoryConfig(3096, 2048, Policy.AUTOMATIC);
	 */
	public ImageCacheConfig(){
		this(DEFAULT_SOFT_LIMIT, DEFAULT_HARD_LIMIT, DEFAULT_POLICY, DEFAULT_CACHE_LOCATION);
	}
	
	/**
	 * @return the soft memory limit in bytes
	 */
	public final long getSoftMinMemory(){
		return this.softMinMemory;
	}
	
	/**
	 * @return the hard memory limit in bytes
	 */
	public final long getHardMinMemory(){
		return this.hardMinMemory;
	}
	
	/**
	 * @return the caching policy
	 */
	public final Policy getCachingPolicy(){
		return this.cachingPolicy;
	}
	
	/**
	 * @return the file system location of the cache
	 */
	public final File getCacheLocation(){
		return this.cacheLocation;
	}
	
	/**
 	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"cachePolicy" : "AUTOMATIC",
	 * 	"softMemoryLimit" : 3096,
	 * 	"hardMemoryLimit" : 2048,
	 * 	"cacheLocation" : "."
	 * }
	 * </pre>
	 * 
	 * @throws NullPointerException in case the provided JsonObject is null
	 * @throws IllegalArgumentException in case one the specified parameters has the wrong format or the specified path is invalid
	 * 
	 */
	public static final ImageCacheConfig parse(JsonObject obj){		
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		Policy cachePolicy = DEFAULT_POLICY;
		if(obj.get("cachePolicy") != null){
			String policy = "";
			try{
				policy = obj.get("cachePolicy").asString();
				cachePolicy = Policy.valueOf(policy);
			}catch(UnsupportedOperationException notAString){
				throw new IllegalArgumentException("'cachePolicy' was not a string in image memory config");
			}catch(IllegalArgumentException notAPolicy){
				throw new IllegalArgumentException(policy + " is not a valid 'cachePolicy' in image memory config");
			}
		}
		
		int softMemoryLimit = DEFAULT_SOFT_LIMIT;
		if(obj.get("softMemoryLimit") != null){
			try{
				softMemoryLimit = obj.get("softMemoryLimit").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'softMemoryLimit' was not an integer in image memory configuration");
			}
			
			if(softMemoryLimit <= 0){
				throw new IllegalArgumentException("'softMemoryLimit' must be > 0");
			}
		}

		
		int hardMemoryLimit = DEFAULT_HARD_LIMIT;
		if(obj.get("hardMemoryLimit") != null){
			try{
				hardMemoryLimit = obj.get("hardMemoryLimit").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'hardMemoryLimit' was not an integer in image memory configuration");
			}
			
			if(hardMemoryLimit <= 0){
				throw new IllegalArgumentException("'hardMemoryLimit' must be > 0");
			}
			
			if(hardMemoryLimit > softMemoryLimit){
				throw new IllegalArgumentException("'hardMemoryLimit' cannot be larger than 'softMemoryLimit' in image memory configuration");
			}
		}
		
		File cacheLocation = DEFAULT_CACHE_LOCATION;
		if(obj.get("cacheLocation") != null){
			String location = "";
			try{
				location = obj.get("cacheLocation").asString();
				cacheLocation = new File(location);
				if(!cacheLocation.exists()){
					throw new IllegalArgumentException("'cacheLocation' does not exist in image memory config: " + cacheLocation.getAbsolutePath());
				}
				if(!cacheLocation.isDirectory()){
					throw new IllegalArgumentException("'cacheLocation' is not a directory in image memory config: " + cacheLocation.getAbsolutePath());
				}
				if(!cacheLocation.canRead()){
					throw new IllegalArgumentException("'cacheLocation' is not readable in image memory config: " + cacheLocation.getAbsolutePath());
				}
			}catch(UnsupportedOperationException nameNotAString){
				throw new IllegalArgumentException("'cacheLocation' was not a string in image memory config");
			}catch(SecurityException canNotRead){
				throw new IllegalArgumentException("security settings do not permitt access to 'cacheLocation' specified in image memory config");
			}
			
		}
		
		return new ImageCacheConfig(softMemoryLimit, hardMemoryLimit, cachePolicy, cacheLocation);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"cache\" : { \"cachePolicy\" : \"");
		builder.append(this.cachingPolicy.toString());
		builder.append("\", \"softMemoryLimit\" : ");
		builder.append(this.softMinMemory / 1024L / 1024L);
		builder.append("\", \"hardMemoryLimit\" : ");
		builder.append(this.hardMinMemory / 1024L / 1024L);
		builder.append(", \"cacheLocation\" : \"");
		builder.append(this.cacheLocation.getAbsolutePath());
		builder.append("\" }");
		return builder.toString();
	}
	
}
