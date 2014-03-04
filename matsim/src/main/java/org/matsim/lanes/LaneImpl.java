/* *********************************************************************** *
 * project: org.matsim.*
 * Lane
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.lanes;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.lanes.data.v20.LaneData20;


/**
 * Serves as meta structure between the data classes and the mobility simulation. 
 * The lane data classes do not have any lane length specified. To calculate the 
 * lane length the link is required that is not available if data is separated in 
 * different containers. 
 * Further, there is a List of downstream LaneImpl instances. 
 *  
 * @author dgrether
 */
public final class LaneImpl {

	private final LaneData20 laneData;
	private double length;
	private double endsAtMetersFromLinkEnd;
	private final List<Id> destinationLinkIds = new ArrayList<Id>();
	private final List<LaneImpl> toLanes = new ArrayList<LaneImpl>();

	public LaneImpl(LaneData20 data) {
		this.laneData = data;
	}
	
	public LaneData20 getLaneData(){
		return this.laneData;
	}
	
	public List<LaneImpl> getToLanes(){
		return toLanes;
	}

	public void addAToLane(LaneImpl toLane) {
		this.toLanes.add(toLane);
	}

	public void changeLength(double lengthMeter) {
		this.length = lengthMeter;
	}

	public double getLength() {
		return this.length;
	}

	
	public double getEndsAtMeterFromLinkEnd(){
		return this.endsAtMetersFromLinkEnd;
	}

	public void setEndsAtMetersFromLinkEnd(double endsAtMetersFromLinkEnd) {
		this.endsAtMetersFromLinkEnd = endsAtMetersFromLinkEnd;
	}

	public void addDestinationLink(Id toLinkId) {
		this.destinationLinkIds.add(toLinkId);
	}

	public List<Id> getDestinationLinkIds() {
		return this.destinationLinkIds;
	}

	
	
}
