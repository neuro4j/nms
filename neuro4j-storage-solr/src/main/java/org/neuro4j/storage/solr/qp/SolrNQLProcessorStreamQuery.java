package org.neuro4j.storage.solr.qp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.neuro4j.core.Connected;
import org.neuro4j.core.Path;
import org.neuro4j.storage.qp.ERType;
import org.neuro4j.storage.qp.Filter;
import org.neuro4j.storage.qp.NQLProcessorStream;
import org.neuro4j.storage.solr.SearchIndexHandler;
import org.neuro4j.storage.solr.SolrIndexMgr;

public class SolrNQLProcessorStreamQuery extends SolrNQLProcessorStreamBase {

	private int inChunkCounter = 0;

	private String solrQuery;
	
//	private ERType queryType;
	
	private ERType previousQueryType;
	
	private SolrDocument nextDoc = null;
	
	/**
	 * for serving filter clause e.g. filter(r[name='coin-belong-to-coin-definition'] 5, r[name='user-contributed-to-coin-definition'] 3)
	 * 
	 * <filter, match-count>
	 */
	protected Map<Filter, Integer> filterMap = new HashMap<Filter, Integer>();

	/**
	 * output network size limit
	 * allows to stop processing if the limit is reached
	 * 
	 */
	private long outputNetworkLimit = -1;

	/**
	 * 
	 * @param solrQuery
	 * @param siMgr
	 * @param filterSet
	 * @param queryType
	 * @param currentMatchedPaths
	 * @param inputStream
	 * @param optional
	 * @param outputNetworkLimit
	 */
	public SolrNQLProcessorStreamQuery(String solrQuery, SolrIndexMgr siMgr, Set<Filter> filterSet,
			Set<Path> currentMatchedPaths, 
			NQLProcessorStream inputStream, boolean optional, long outputNetworkLimit)  
	{
		super(siMgr, currentMatchedPaths, inputStream, optional);
		
		this.outputNetworkLimit = outputNetworkLimit;
		
		this.solrQuery = solrQuery;
		
		for (Filter filter : filterSet)
			filterMap.put(filter, 0);
		
/*		if (null == inputStream)
		{
			// very first decorator
			this.queryType = queryType;
			
			this.solrQuery = siMgr.addERFilterToQuery(solrQuery, queryType);
			
		} else {
			// not first decorator
			previousQueryType = inputStream.getERQueryType();
			switch (previousQueryType)
			{
			case entity:
				this.queryType = ERType.relation;
				break;
			case relation:
				this.queryType = ERType.entity;
				break;
			}
		}
*/		
	}
	
	/**
	 * 
	 * @param solrQuery
	 * @param siMgr
	 * @param filterSet
	 * @param inputStream
	 * @param optional
	 * @param outputNetworkLimit
	 */
	public SolrNQLProcessorStreamQuery(String solrQuery, SolrIndexMgr siMgr, Set<Filter> filterSet,
			NQLProcessorStream inputStream, boolean optional, long outputNetworkLimit) 
	{
		this(solrQuery, siMgr, filterSet, new HashSet<Path>(), inputStream, optional, outputNetworkLimit);
	}
	
	/**
	 * 
	 */
	public boolean hasNext() 
	{
		if (null != nextDoc) // next() was not called. probably hasNext() called > 1 time 
			return true;
		
		if (-1 < outputNetworkLimit // not ALL 
				&& outputNetworkLimit + 1 < getCurrentOutputNetSize()) // more then limit
			return false;
			
		boolean hasNext = false;
		if (null == inputStream)
		{
			// very first decorator - no parent ids
			if (null == iter)
		    	iter = siMgr.query(solrQuery); 

			hasNext = iter.hasNext();
			if (hasNext) 
				nextDoc = iter.next(); 
			return hasNext;
		}
		
		// current decorator is not first
		
		//  check if iterator created
		if (null == iter)
		{
			// parent input is empty - no query required
			if (!inputStream.hasNext())
			{
				return false;
			}

			updateIterator();

		} else {
			// iter exist - check if end is reached
			if (!iter.hasNext() && inputStream.hasNext())
			{
				// end is reached - check if parent decorator has more ids
				updateIterator();
			} // if (!iter.hasNext())
		} // if - else (null == iter) 
		
		// if current iterator is empty and upstream has more previous ids - do recursion
		if (!iter.hasNext() && inputStream.hasNext())
			return hasNext();


		// validate er stream with query filters. (skip er's which over filter amount)
		hasNext = checkQueryFilters();
		
		return hasNext;
	}
	
	
	/**
	 * validate er stream with query filters. (skip er's which over filter amount)
	 *  
	 * @return
	 */
	private boolean checkQueryFilters()
	{
		boolean hasNext = iter.hasNext();
		
		if (!hasNext) // nothing found in current iterator
		{
			// check if parent stream has more elements
			if (inputStream.hasNext())
			{
				// recreate current iterator with new data from parent stream
				updateIterator();
				// recursive check
	        	return checkQueryFilters();
	        	
			} else {
				// end of parent iterator
	    		nextDoc = null;         	
				return hasNext; // return false;
			}
		}
		
		boolean goThroughIterator = false;
		do
		{
    		goThroughIterator = false;
			nextDoc = iter.next(); 
			
	        Connected er = SearchIndexHandler.doc2erbase(nextDoc);
			for (Filter f : filterMap.keySet())
			{
		        if (f.propertyValue.equals(er.getProperty(f.propertyName)) )
		        {
		        	// er match this filter
		        	int matchCount = filterMap.get(f);
		        	matchCount++;
		        	filterMap.put(f, matchCount);
		        	
		        	if (matchCount > f.filterAmount)
		        	{
		    			// skip current doc 
		            	nextDoc = null;
		            	
		            	if (iter.hasNext())
			        		goThroughIterator = true;
			        	else
			            	hasNext = checkQueryFilters(); // do recursive check - may need updateIterator() call

		        		break;
		        	}
		        }
			}
		} while (goThroughIterator);

        
        return hasNext;
	}
	
	/**
	 * 
	 */
	private void updateIterator()
	{
		Set<String> previousIds = new LinkedHashSet<String>();
		// create new query with further ids from parent decorator
		while (inputStream.hasNext())
		{
			previousIds.add(inputStream.next());
			inChunkCounter ++;
			if (inChunkCounter >= INPUT_CHUNK_SIZE_LIMIT)
			{
				inChunkCounter = 0;
				break;
			}
		}
					
		if(previousIds.size() > 0)
		{
			solrQuery = previousIds2query(solrQuery, previousIds);
			
			// if optional -> copy matched paths from parent 
			if (optional)
				for (Path p : inputStream.getCurrentMatchedPaths())
					currentMatchedPaths.add(p);
			
			
	    	iter = siMgr.query(solrQuery);								
		}
		
		return;
	}
	
	public String next() {
		
		if (null == nextDoc)
			return null;
		
/*		Don't check it - in next() call outputNetworkLimit is much more then in hasNext() 
		because it updated in updateIterator() 
		
		if (outputNetworkLimit < getCurrentOutputNetSize())
			return null;
*/		
        Connected er = SearchIndexHandler.doc2erbase(nextDoc);
		
        nextDoc = null;
        
		if (null == inputStream)
		{
			// very first decorator
			// create Paths
			Path p = new Path(er.getUuid());
			currentMatchedPaths.add(p);
		} else {
			// not first decorator		
			updateMatchedPaths(er);
		}
		
		return er.getUuid();
	}	
	
	private void updateMatchedPaths(Connected newERBase)
	{
		Set<Path> newMatchedPaths = new HashSet<Path>();
		
		for (Path p : inputStream.getCurrentMatchedPaths())
		{
			
			// if current level is optional -> move through current paths
			if (optional)
				newMatchedPaths.add(p);
			
			String lastId = p.getLast();
//			ERBase lastER = outputNet.getById(lastId); // last er in the path can be virtual 
			if (newERBase.isConnectedTo(lastId))
//					|| (lastER.isVirtual() && lastER.isConnectedTo(newSubnetERBase.getUuid()))) // 
			{
				if (!ALLOW_REENTRANCE)
				{   // check for re-entrance
					if (p.contains(newERBase.getUuid()))
						continue;
				}

				p = p.clone();
				p.add(newERBase.getUuid());
				newMatchedPaths.add(p);
			}
		}
		
		for (Path p : newMatchedPaths)
			currentMatchedPaths.add(p);
			
		return;
	}

	public int getDepthLevel()
	{
		if (null == inputStream)
			return 0; // top level
		else
			return inputStream.getDepthLevel() + 1;
	}


	
	/**
	 * 
	 * @param solrQueryStr
	 * @param previousIds
	 * @return
	 */
	private String previousIds2query(String solrQueryStr, Set<String> previousIds)
	{
    	StringBuffer sqSB = new StringBuffer();
		if (null != previousIds && previousIds.size() > 0)
    	{
    		sqSB.append("connected:("); 
        	
			for (String id : previousIds)
				sqSB.append(id).append(" ");

			sqSB.append(")");
    		
			sqSB.append("  AND ( ");
    	}
    	
		sqSB.append(solrQueryStr);

    	if (null != previousIds && previousIds.size() > 0)
			sqSB.append(" ) ");
    	
    	return sqSB.toString();
	}	
}
