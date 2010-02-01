package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Flow;

/**
 * Interface for {@link Flow} Data Access Objects.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface FlowDao extends GenericDao<Flow,Long>{

    /**
     * Return the {@link Flow}s that the specified owner has created. None of
     * the returned flows will be templates.
     * 
     * @param userId The unique ID of the user for whom flows will be fetched.
     * @return A list of flows.
     */
    List<Flow> getFlowsByCreatorId(Long userId);
    
    /**
     * Return the {@link Flow}s that are templates.
     * 
     * @return A list of flows
     */
    public List<Flow> getFlowTemplates();

}