/* *********************************************************************** *
 * project: org.matsim.*
 * PadangSurvey2Biogeme.java
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

package playground.marcel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.matsim.gbl.MatsimRandom;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.utils.StringUtils;
import org.matsim.utils.geometry.CoordImpl;
import org.matsim.utils.io.IOUtils;
import org.matsim.utils.misc.Counter;

public class PadangSurvey2Biogeme {

	private static class Zone {
		public final String zoneId;
		public final Coord coord;
		public final int numOpportunities;

		public Zone(final String zoneId, final Coord coord, final int numOpportunities) {
			this.zoneId = zoneId;
			this.coord = coord;
			this.numOpportunities = numOpportunities;
		}
	}

	public static void main(String[] args) {
		final String surveyFilename = "../mystudies/benno/Survey.csv";
		final String zonesFilename = "../mystudies/benno/Zones.csv";
		final String biogemeFilename = "../mystudies/benno/biogeme.dat";

		ArrayList<Zone> zones = new ArrayList<Zone>(20);
		int sumOpportunities = 0;

		// read zones
		try {
			final BufferedReader zonesReader = IOUtils.getBufferedReader(zonesFilename);
			String header = zonesReader.readLine();
			String line = zonesReader.readLine();
			while (line != null) {
				String[] parts = StringUtils.explode(line, ';');
				int numOpportunities = Integer.parseInt(parts[3]);
				Zone zone = new Zone(parts[0], new CoordImpl(parts[1], parts[2]), numOpportunities);
				zones.add(zone);
				sumOpportunities += numOpportunities;
				// --------
				line = zonesReader.readLine();
			}
			zonesReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// process survey, line by line
		try {
			final BufferedReader surveyReader = IOUtils.getBufferedReader(surveyFilename);
			final BufferedWriter biogemeWriter = IOUtils.getBufferedWriter(biogemeFilename);
			String header = surveyReader.readLine();
			biogemeWriter.write("Id\tChoice\tdChosen\td1\td2\td3\td4\td5\td6\td7\td8\td9\n");
			String line = surveyReader.readLine();

			Counter counter = new Counter("entry ");
			while (line != null) {
				counter.incCounter();

				String[] parts = StringUtils.explode(line, ';');
				String id = parts[0];
				Coord homeCoord = new CoordImpl(parts[1], parts[2]);
				Coord primActCoord = new CoordImpl(parts[5], parts[6]);
				double distance = homeCoord.calcDistance(primActCoord);

				Zone[] alternatives = new Zone[9];

				int numShorterTrips = 0;
				int numEqualTrips = 0;
				int numLongerTrips = 0;
				int numMissed = 0;
				int missed = 0; // security counter to prevent endless loops, in case the actual distance is e.g. shorter than distance to nearest zone


				while ( (numShorterTrips + numEqualTrips + numLongerTrips + numMissed) < 9) {

					// draw random zone based on numOpportunities
					int r = MatsimRandom.random.nextInt(sumOpportunities);
					int tmpSum = 0;
					Zone tmpZone = null;
					for (Zone zone : zones) {
						tmpSum += zone.numOpportunities;
						if (r <= tmpSum) {
							tmpZone = zone;
							break;
						}
					}
					double tmpDistance = homeCoord.calcDistance(tmpZone.coord);
					if ((tmpDistance < (0.7 * distance)) && (numShorterTrips < 3)) {
						alternatives[numShorterTrips + numEqualTrips + numLongerTrips + numMissed] = tmpZone;
						numShorterTrips++;
						missed = 0;
					} else if ((tmpDistance > (1.3 * distance)) && (numLongerTrips < 3)) {
						alternatives[numShorterTrips + numEqualTrips + numLongerTrips + numMissed] = tmpZone;
						numLongerTrips++;
						missed = 0;
					} else if (numEqualTrips < 3) {
						alternatives[numShorterTrips + numEqualTrips + numLongerTrips + numMissed] = tmpZone;
						numEqualTrips++;
						missed = 0;
					} else if (missed >= 100) {
						System.out.println("WARN: couldn't find appropriate alternative for survey: " + id
								+ ". #shorter=" + numShorterTrips + " #equal=" + numEqualTrips
								+ " #longer=" + numLongerTrips + " d=" + distance);
						alternatives[numShorterTrips + numEqualTrips + numLongerTrips + numMissed] = tmpZone;
						numMissed++;
					} else {
						missed++;
					}
				}

				biogemeWriter.write(id + "\t1\t" + distance);
				for (Zone alternative : alternatives) {
					biogemeWriter.write("\t" + homeCoord.calcDistance(alternative.coord));
				}
				biogemeWriter.write('\n');

				// -------
				line = surveyReader.readLine();
			}
			counter.printCounter();
			surveyReader.close();
			biogemeWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

}
