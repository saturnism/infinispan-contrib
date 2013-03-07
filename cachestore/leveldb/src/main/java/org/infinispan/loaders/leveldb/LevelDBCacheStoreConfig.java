package org.infinispan.loaders.leveldb;

import java.util.Properties;

import org.infinispan.loaders.LockSupportCacheStoreConfig;
import org.infinispan.util.TypedProperties;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public class LevelDBCacheStoreConfig extends LockSupportCacheStoreConfig {
	private static final long serialVersionUID = -6138954929695571573L;
	
	protected String location = "leveldb/data";
	protected String expiraryLocation = "leveldb/expired";
	protected String compressionType = "NONE";
	protected Integer blockSize;
	protected Long cacheSize;
	
	/**
     * @configRef desc="Whenever a new entry is stored, an expiry entry is created and added
     * to the a queue that is later consumed by the eviction thread. This parameter sets the size
     * of this queue."
     */
	protected int expiryQueueSize = 10000;
	
	/**
	 * @configRef desc="There are two methods to clear all entries in LevelDB.  One method is to iterate through
	 * all entries and remove each entry individually.  The other method is to delete the database and re-init.
	 * For smaller databases, deleting individual entries is faster than the latter method.  This configuration
	 * sets the max number of entries allowed before using the latter method."
	 */
	protected int clearThreshold = 10000;
	
	public LevelDBCacheStoreConfig() {
		setCacheLoaderClassName(LevelDBCacheStore.class.getName());
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		testImmutability("location");
		setProperty(location, "location", properties);
		this.location = location;
	}

	public int getExpiryQueueSize() {
		return expiryQueueSize;
	}

	public void setExpiryQueueSize(int expiryQueueSize) {
		testImmutability("expiryQueueSize");
		setProperty(location, "location", properties);
		this.expiryQueueSize = expiryQueueSize;
	}

	public String getExpiraryLocation() {
		return expiraryLocation;
	}

	public void setExpiraryLocation(String expiraryLocation) {
		testImmutability("expiraryLocation");
		setProperty(expiraryLocation, "expiraryLocation", properties);
		this.expiraryLocation = expiraryLocation;
	}

	public int getClearThreshold() {
		return clearThreshold;
	}

	public void setClearThreshold(int clearThreshhold) {
		testImmutability("clearThreshhold");
		setProperty(String.valueOf(clearThreshhold), "clearThreshold", properties);
		this.clearThreshold = clearThreshhold; 
	}

	public String getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionType) {
		testImmutability("compressionType");
		setProperty(compressionType, "compressionType", properties);
		this.compressionType = compressionType;
	}

	public Integer getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(Integer blockSize) {
		testImmutability("blockSize");
		setProperty(String.valueOf(blockSize), "blockSize", properties);
		this.blockSize = blockSize;
	}
	
	public Long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Long cacheSize) {
		setProperty(String.valueOf(cacheSize), "cacheSize", properties);
		this.cacheSize = cacheSize;
	}
	
	protected Options getDataDbOptions() {
		Options options = new Options().createIfMissing(true);
		
		options.compressionType(CompressionType.valueOf(compressionType));
		
		if (blockSize != null) {
			options.blockSize(blockSize);
		}
		
		if (cacheSize != null) {
			options.cacheSize(cacheSize);
		}
		
		return options;
	}
	
	protected Options getExpiryDbOptions() {
		return new Options()
			.createIfMissing(true);
	}

	protected void setProperty(String properyValue, String propertyName,
			Properties p) {
		if (properyValue != null) {
			try {
				p.setProperty(propertyName, properyValue);
			} catch (UnsupportedOperationException e) {
				// Most likely immutable, so let's work around that
				TypedProperties writableProperties = new TypedProperties(p);
				writableProperties.setProperty(propertyName, properyValue);
				setProperties(writableProperties);
			}
		}
	}
}
