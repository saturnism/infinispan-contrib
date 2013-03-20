package org.infinispan.contrib.example.rest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.configuration.cache.CacheLoaderConfiguration;
import org.infinispan.configuration.cache.LoaderConfiguration;
import org.infinispan.interceptors.CacheMgmtInterceptor;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.loaders.remote.RemoteCacheStore;
import org.infinispan.loaders.remote.RemoteCacheStoreConfig;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;

@Named("cacheManagerService")
@Path("/caches")
@RequestScoped
public class CacheManagerRESTService {
	@Inject
	private EmbeddedCacheManager cacheManager;
	
	private String key;
	private String type;
	private boolean entryFound;
	private String action;
		
	@GET
	@Produces("application/json")
	@Path("/")
	public Set<String> getAllCacheNames() {
		return new LinkedHashSet<String>(cacheManager.getCacheNames());
	}
	
	@GET
	@Produces("application/json")
	@Path("/{cacheName}")
	public CacheInfo getCache(@PathParam("cacheName") String cacheName) {
		if (!cacheManager.cacheExists(cacheName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		Cache<?, ?> cache = cacheManager.getCache(cacheName);
		RemoteCacheStoreConfig rcsConfig = null;
		String remoteCacheName = null;
		
		List<CacheLoaderConfiguration> configs = cache.getAdvancedCache().getCacheConfiguration().loaders().cacheLoaders();
		for (CacheLoaderConfiguration config : configs) {
			if (config instanceof LoaderConfiguration) {
				LoaderConfiguration loaderConfig = (LoaderConfiguration) config;
				if (loaderConfig.cacheStore() instanceof RemoteCacheStore) {
					RemoteCacheStore rcs = (RemoteCacheStore) loaderConfig.cacheStore();
					remoteCacheName = loaderConfig.properties().getProperty("remoteCacheName");
				}
			}
		}
		
		RemoteCache<Object, Object> remoteCache = null;
		/*
		if (remoteCacheName != null) {
			remoteCache = remoteCacheManager.getCache(remoteCacheName);
		}
		*/
		
		return new CacheInfo(cacheManager.getCache(cacheName), remoteCache);
	}
	
	@DELETE
	@Produces("application/json")
	@Path("/{cacheName}")
	public void clearCache(@PathParam("cacheName") String cacheName, @DefaultValue("global") @QueryParam("scope") String scope) {
		if (!cacheManager.cacheExists(cacheName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		if (scope.equalsIgnoreCase("global")) {
			cacheManager.getCache(cacheName).clear();
		} else if (scope.equalsIgnoreCase("local")) {
			// USE WITH CARE! READ JAVADOC
			cacheManager.getCache(cacheName).getAdvancedCache().getDataContainer().clear();
		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
	}
	
	public void clearStats(String cacheName) {
		if (!cacheManager.cacheExists(cacheName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		for (CommandInterceptor interceptor : cacheManager.getCache(cacheName).getAdvancedCache().getInterceptorChain()) {
			if (interceptor instanceof CacheMgmtInterceptor) {
				CacheMgmtInterceptor mgmtInterceptor = (CacheMgmtInterceptor) interceptor;
				mgmtInterceptor.resetStatistics();
			}
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/{cacheName}/{key}")
	public Object getEntry(@PathParam("cacheName") String cacheName, @PathParam("key") String key, @DefaultValue("string") @QueryParam("type") String type) {
		Object realKey = convertToKey(key, type);
		
		return getEntry(cacheName, realKey);
	}
	
	public Object getEntry(String cacheName, Object key) {
		if (!cacheManager.cacheExists(cacheName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		if (!cacheManager.getCache(cacheName).containsKey(key)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			return cacheManager.getCache(cacheName).get(key);
		}
	}
	
	public void getEntryAction(String cacheName) {
		if (!cacheManager.cacheExists(cacheName)) {
			entryFound = false;
		}
		
		Object realKey = convertToKey(key, type);
		
		if (!cacheManager.getCache(cacheName).containsKey(realKey)) {
			entryFound = false;
		} else {
			cacheManager.getCache(cacheName).get(realKey);
			entryFound = true;
		}
	}
	
	@DELETE
	@Produces("application/json")
	@Path("/{cacheName}/{key}")
	public void removeEntry(@PathParam("cacheName") String cacheName, @PathParam("key") String key, @DefaultValue("string") @QueryParam("type") String type, @DefaultValue("global") @QueryParam("scope") String scope) {
		Object realKey = convertToKey(key, type);
		
		removeEntry(cacheName, realKey, scope);
	}
	
	public void removeEntry(String cacheName, Object key, String scope) {
		if (!cacheManager.cacheExists(cacheName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		if (!cacheManager.getCache(cacheName).containsKey(key)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			if (scope.equalsIgnoreCase("global")) {
				cacheManager.getCache(cacheName).remove(key);
			} else if (scope.equalsIgnoreCase("local")) {
				//USE WITH CARE! READ JAVADOC
				cacheManager.getCache(cacheName).getAdvancedCache().getDataContainer().remove(key);
			} else {
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
		}
	}
	
	protected Object convertToKey(String key, String type) {
		if (type == null || type.isEmpty() || type.equalsIgnoreCase("string")) {
			return key;
		} else if (type.equalsIgnoreCase("long")) {
			return Long.valueOf(key);
		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
	}
	
	public class CacheInfo {
		private Cache<?, ?> cache;
		private RemoteCache<?, ?> remoteCache;
		public CacheInfo(Cache<?, ?> cache, RemoteCache<?, ?> remoteCache) {
			this.cache = cache;
			this.remoteCache = remoteCache;
		}
		
		public Set<?> getLocalKeys() {
			return new LinkedHashSet<Object>(cache.keySet());
		}
		
		public Set<?> getRemoteKeys() {
			if (remoteCache == null) {
				return Collections.emptySet();
			} else {
				Map<?, ?> bulk = remoteCache.getBulk();				
				return new LinkedHashSet<Object>(bulk.keySet());
			}
		}
		
		public Stats getStats() {
			return cache.getAdvancedCache().getStats();
		}
		
		public boolean hasRemoteCache() {
			return remoteCache != null;
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isEntryFound() {
		return entryFound;
	}

	public void setEntryFound(boolean entryFound) {
		this.entryFound = entryFound;
	}
}
