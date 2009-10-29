package org.imirsel.nema.dao;

import java.util.Collection;

import org.imirsel.nema.model.Flow;

public interface FlowDao {

	public abstract void remove(Long id);

	public abstract Flow get(long flowInstanceId);

	public abstract Collection<? extends Flow> getFlowTemplates();

	public abstract void save(Flow instance);

}