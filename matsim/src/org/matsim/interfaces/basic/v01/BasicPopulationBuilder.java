/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.interfaces.basic.v01;

import java.util.List;

import org.matsim.basic.v01.BasicKnowledge;
import org.matsim.interfaces.basic.v01.BasicLeg.Mode;

/**
 * @author dgrether
 */
public interface BasicPopulationBuilder {

	BasicPerson createPerson(Id id) throws Exception;

	BasicPlan createPlan(BasicPerson currentPerson);

//	@Deprecated // to be clarified
//	BasicActivity createAct(BasicPlan basicPlan, String currentActType, BasicLocation currentlocation);

	/* we need something like createAct
	 * Kai wants something like:
	 * - takes either a coord, or a facility, or a link
	 * when working with basic*, the methods should be something like
	 * createActWithCoord(), createActWithFacility(), ... as Id is always the same.
	 */
	
	BasicLeg createLeg(BasicPlan basicPlan, Mode legMode);

	/**
	 * Creates a new Route object
	 * @param currentRouteLinkIds List of Ids including the start and the end Link Id of the route's links
	 * @return a BasicRoute Object with the links set accordingly
	 */
	BasicRoute createRoute(Id startLinkId, Id endLinkId, final List<Id> currentRouteLinkIds);

	BasicPlan createPlan(BasicPerson person, boolean selected);

//	@Deprecated // to be clarified: this generates an activity opportunity, not an "act"!!!
//	BasicActivityOption createActivity(String type, BasicLocation currentlocation);

	@Deprecated // to be clarified
	BasicKnowledge createKnowledge(List<BasicActivityOption> currentActivities);

//	BasicAct createAct(BasicPlan plan, String string, Coord coord);

}
