/* *********************************************************************** *
 * project: org.matsim.*
 * AmberTime
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.signalsystems.data.ambertimes.v10;

import java.util.Map;

import org.matsim.api.core.v01.Id;


/**
 * @author dgrether
 *
 */
public interface AmberTimeData {
	
	public Id getSignalSystemId();
	
	public Integer getDefaultRedAmber();
	
	public Integer getDefaultAmber();
	
	public Integer getRedAmberOfSignal(Id signalId);
	
	public Integer getAmberOfSignal(Id signalId);
	
	public void setAmberTimeOfSignal(Id signalId, Integer seconds);
	
	public void setRedAmberTimeOfSignal(Id signalId, Integer seconds);
	
	/**
	 * @return A map with signal ids as keys and amber times for that signal in seconds as values
	 */
	public Map<Id, Integer> getSignalAmberMap();
	/**
	 * @return A map with signal ids as keys and red-amber times for that signal in seconds as values
	 */
	public Map<Id, Integer> getSignalRedAmberMap();
	
}
