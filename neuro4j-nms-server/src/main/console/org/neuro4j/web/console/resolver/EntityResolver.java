package org.neuro4j.web.console.resolver;

import org.neuro4j.NetworkUtils;
import org.neuro4j.core.Entity;
import org.neuro4j.core.Network;
import org.neuro4j.storage.NeuroStorage;
import org.neuro4j.storage.StorageException;
import org.neuro4j.web.console.vlh.EntryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityResolver implements EntryResolver {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private NeuroStorage neuroStorage = null;
	private Network network = null;
	private int connectedCountLimit = Integer.MAX_VALUE;
	
	public EntityResolver(NeuroStorage neuroStorage, Network network)
	{
		this.neuroStorage = neuroStorage;
		this.network = network;
	}
	
	public EntityResolver(NeuroStorage neuroStorage, Network network, int connectedCountLimit)
	{
		this.neuroStorage = neuroStorage;
		this.network = network;
		this.connectedCountLimit = connectedCountLimit;
	}
	
	public Object resolve(String id, String language) {
		
		try {
			Entity e = network.getEntityByUUID(id);
			if (null == e)
			{
				e = neuroStorage.getEntityByUUID(id);
				network.add(e);
			}
			NetworkUtils.loadConnected(e, network, neuroStorage, connectedCountLimit);
			return e;
		} catch (StorageException e) {
			logger.error("Can't resolve entity " + id, e);
		}
		
		return null;
	}

}