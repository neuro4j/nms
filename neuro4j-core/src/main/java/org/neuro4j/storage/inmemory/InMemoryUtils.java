package org.neuro4j.storage.inmemory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.neuro4j.core.ERBase;
import org.neuro4j.core.Network;

public class InMemoryUtils {

	/**
	 * for AND operation
	 * 
	 * @param net1
	 * @param net2
	 * @return
	 */
	public static Network andNetworks(Network net1, Network net2)
	{
		List<ERBase> e4Removal = new ArrayList<ERBase>();
		for (String eid : net1.getIds())
		{
			if (null == net2.getById(eid))
				e4Removal.add(net1.getById(eid));
		}
		
//		net1.remove(e4Removal.toArray(new Entity[]{}));
		for (ERBase entity : e4Removal)
			net1.remove(entity, true);


//		// relations
//		List<Relation> r4Removal = new ArrayList<Relation>();
//		for (String rid : net1.getRelations())
//		{
//			if (null == net2.getRelationByUUID(rid))
//				r4Removal.add(net1.getRelationByUUID(rid));
//		}
//		
//		net1.remove(r4Removal.toArray(new Relation[]{}));
		
		return net1;
	}	

	/**
	 * useful for AND operation 
	 * 
	 * @param net
	 * @param key
	 * @param value
	 */
	public static void filterEntities(Network net, String key, String value) {
		for (String eid : net.getIds())
		{
			ERBase e = net.getById(eid);
			if (!value.equals(e.getProperty(key)))
			{
				net.remove(e, true);
			}
		}
		return;
	}
	
/*	*//**
	 * useful for AND operation
	 * 
	 * @param net
	 * @param key
	 * @param value
	 *//*
	public static void filterRelations(Network net, String key, String value) {
		for (String rid : net.getRelations())
		{
			Relation r = net.getRelationByUUID(rid);
			if (!value.equals(r.getProperty(key)))
			{
				net.remove(r);
			}
		}
		return;
	}*/
	
/*	public static void saveOrUpdate(Network net, ERBase newRelation) {
		Relation currentRelation = net.getRelationByUUID(newRelation.getUuid());
		if (null == currentRelation)
		{
			// TODO do not make deep copy - it can lead to object duplications (through entities in relations)
//			currentRelation = (Relation) ClassUtils.deepCloneBySerialization(newRelation);
			// create new relation - all entities should be in network already  
			currentRelation = createNewRelationUsingExistingEntitiesIfPossible(net, newRelation);
			net.add(currentRelation);
		} else {
			// update existing relation - all entities should be in network already  
			updateCurrentRelationUsingExistingEntitiesIfPossible(net, currentRelation, newRelation);
		}
		
		return;
	}*/

	/**
	 * 
	 * 
	 * @param net
	 * @param newEntity
	 */
	public static void saveOrUpdate(Network net, ERBase newEntity) {
		
		ERBase currentEntity = net.getById(newEntity.getUuid());
		if (null == currentEntity)
		{
			// TODO do not make deep copy - it can lead to object duplications (through entities in relations)
//			currentEntity = (Entity) ClassUtils.deepCloneBySerialization(newEntity);

//			currentEntity = newEntity.cloneBase();
			currentEntity = newEntity.cloneWithConnectedKeys();
			net.add(currentEntity);

		} else {
			// update existing entity 
			currentEntity.setLastModifiedDate(new Date());
			
			// properties
			currentEntity.removeProperties();
			for (String key : newEntity.getPropertyKeysWithRepresentations())
				currentEntity.setProperty(key, newEntity.getProperty(key));
				
		}

		return;
	}

	
/*	private static Relation createNewRelationUsingExistingEntitiesIfPossible(Network net, Relation outsideRelation)
	{
		Relation r = outsideRelation.cloneBase(); 
		
		for (Entity outrpe : outsideRelation.getAllParticipants())
		{
			Entity e = net.getEntityByUUID(outrpe.getUuid());
			if (null == e)
			{
				// create new entity without relations
				e = outrpe.cloneBase();
			}
//			RelationPart rp = new RelationPart(outrp.getType(), e);
			r.addParticipant(e);
		}
		return r;
	}*/

/*	private static void updateCurrentRelationUsingExistingEntitiesIfPossible(Network net, Relation existingRelation, Relation outsideRelation)
	{
		existingRelation.setName(outsideRelation.getName());
		existingRelation.setLastModifiedDate(outsideRelation.getLastModifiedDate());

		existingRelation.removeProperties();
		for (String key : outsideRelation.getPropertyKeysWithRepresentations())
			existingRelation.setProperty(key, outsideRelation.getProperty(key));
		
		
		for (Entity outrpe : outsideRelation.getAllParticipants())
		{
//			Entity outrpe = outrp.getEntity();
			Entity rp = existingRelation.getParticipant(outrpe.getUuid());
			if (null != rp)
				continue; // this relation part already exist
			
			Entity e = net.getEntityByUUID(outrpe.getUuid());
			if (null == e)
			{
				// create new entity without relations
				e = new Entity(outrpe.getName());
				e.setUuid(outrpe.getUuid());
				e.setLastModifiedDate(outrpe.getLastModifiedDate());
				
				for (String key : outrpe.getPropertyKeys())
					e.setProperty(key, outrpe.getProperty(key));
			}
			existingRelation.addParticipant(e);
		}
		
		return;
	}
	
*/}
