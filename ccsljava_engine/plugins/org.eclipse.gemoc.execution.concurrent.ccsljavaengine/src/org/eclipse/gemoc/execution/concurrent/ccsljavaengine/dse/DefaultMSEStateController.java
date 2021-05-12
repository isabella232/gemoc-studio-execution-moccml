/*******************************************************************************
 * Copyright (c) 2017 INRIA and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     INRIA - initial API and implementation
 *     I3S Laboratory - API update and bug fix
 *******************************************************************************/
package org.eclipse.gemoc.execution.concurrent.ccsljavaengine.dse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.dse.IMoccmlMSEStateController;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ICCSLSolver;
import org.eclipse.gemoc.execution.concurrent.ccsljavaxdsml.api.moc.ISolver;
import org.eclipse.gemoc.moccml.mapping.feedback.feedback.ModelSpecificEvent;

import fr.inria.aoste.trace.EventOccurrence;
import fr.inria.aoste.trace.ModelElementReference;
import fr.inria.aoste.trace.TraceFactory;

public class DefaultMSEStateController implements IMoccmlMSEStateController
{

	private void applyForcePresence(ICCSLSolver solver, EventOccurrence eventOccurrence) 
	{
		solver.forceEventOccurrence(eventOccurrence);
	}
	
	private void applyForceAbsence(ICCSLSolver solver, EventOccurrence eventOccurrence) 
	{
		solver.forbidEventOccurrence(eventOccurrence);
	}
	
	private EventOccurrence createEventoccurence(ModelSpecificEvent mse) 
	{
		EventOccurrence eo = TraceFactory.eINSTANCE.createEventOccurrence();
		ModelElementReference mer = TraceFactory.eINSTANCE.createModelElementReference();
		mer.getElementRef().add(mse.getSolverEvent().eContainer().eContainer());
		mer.getElementRef().add(mse.getSolverEvent().eContainer());
		mer.getElementRef().add(mse.getSolverEvent());
		eo.setReferedElement(mer);
		return eo;
	}

	public void applyMSEFutureStates(ICCSLSolver solver) 
	{
		for(Entry<ModelSpecificEvent, Boolean> entry : _mseNextStates.entrySet())
		{
			EventOccurrence eo = createEventoccurence(entry.getKey());
			if (entry.getValue())
			{
				applyForcePresence(solver, eo);
			}
			else
			{
				applyForceAbsence(solver, eo);				
			}
		}
		//_mseNextStates.clear(); --> done by the 'futurActions'
		
	}

	/**
	 * participates in the state in the exhaustive exploration
	 */
	public Map<ModelSpecificEvent, Boolean> _mseNextStates = new HashMap<ModelSpecificEvent, Boolean>();
	
	@Override
	public void forcePresenceInTheFuture(ModelSpecificEvent mse)
	{
		setFutureClockState(mse, true);
	}

	@Override
	public void forceAbsenceTickInTheFuture(ModelSpecificEvent mse)
	{
		setFutureClockState(mse, false);		
	}

	private void setFutureClockState(ModelSpecificEvent mse, Boolean willTick)
	{
		_mseNextStates.put(mse, willTick);		
	}
	
	@Override
	public void freeInTheFuture(ModelSpecificEvent mse)
	{
		Set<Entry<ModelSpecificEvent, Boolean>> allEntrySet = new HashSet<Entry<ModelSpecificEvent, Boolean>>(_mseNextStates.entrySet());
		for(Entry<ModelSpecificEvent, Boolean> entry : allEntrySet) {
			if (entry.getKey().getName().compareTo(mse.getName()) == 0) {
				_mseNextStates.remove(entry.getKey());
			}
		}
		
	}

	@Override
	public void applyMSEFutureStates(ISolver solver) {
		if(solver instanceof ICCSLSolver) {
			applyMSEFutureStates((ICCSLSolver)solver);
		}
	}
	
}
