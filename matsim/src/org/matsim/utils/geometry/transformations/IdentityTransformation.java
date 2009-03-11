/* *********************************************************************** *
 * project: org.matsim.*
 * IdentityTransformation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.utils.geometry.transformations;

import org.matsim.interfaces.core.v01.Coord;
import org.matsim.utils.geometry.CoordinateTransformation;

/**
 * A very simple coordinate transformation which always returns the same coordinate
 * as it was given.
 *
 * @author mrieser
 */
public class IdentityTransformation implements CoordinateTransformation {

	public Coord transform(Coord coord) {
		return coord;
	}

}
