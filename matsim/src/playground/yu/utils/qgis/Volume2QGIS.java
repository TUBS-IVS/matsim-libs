/* *********************************************************************** *
 * project: org.matsim.*
 * Volume2QGIS.java
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

/**
 * 
 */
package playground.yu.utils.qgis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.analysis.VolumesAnalyzer;
import org.matsim.basic.v01.Id;
import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author yu
 * 
 */
public class Volume2QGIS extends MATSimNet2QGIS implements X2QGIS {

	public static List<Map<Id, Integer>> createVolumes(final NetworkLayer net,
			final VolumesAnalyzer va) {
		List<Map<Id, Integer>> volumes = new ArrayList<Map<Id, Integer>>(24);
		for (int i = 0; i < 24; i++)
			volumes.add(i, null);
		for (Link link : net.getLinks().values()) {
			Id linkId = link.getId();
			int[] v = va.getVolumesForLink(linkId.toString());
			for (int i = 0; i < 24; i++) {
				Map<Id, Integer> m = volumes.get(i);
				if (m == null) {
					m = new HashMap<Id, Integer>();
					volumes.add(i, m);
				}
				m.put(linkId, (int) ((v != null ? v[i] : 0) / flowCapFactor));
			}
		}
		return volumes;
	}

	public void setCrs(final String wkt, final NetworkLayer network,
			final CoordinateReferenceSystem crs, Set<Id> linkIds) {
		super.setCrs(wkt);
		setN2g(new Volume2PolygonGraph(network, crs, linkIds));
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		MATSimNet2QGIS mn2q = new MATSimNet2QGIS();
		// String netFilename = "../schweiz-ivtch/network/ivtch-osm.xml";
		// String netFilename = "test/yu/test/equil_net.xml";
		// String netFilename =
		// "../swiss-advest/ch.cut.640000.200000.740000.310000.xml";
		String netFilename = "../schweiz-ivtch-SVN/baseCase/network/ivtch-osm.xml";
		String eventsFilenameA = "../runs/r153_10/it.1000/1000.events.txt.gz";
		String eventsFilenameB = "../runs/run628/it.1000/1000.events.txt.gz";
		String shapeFilepath = "../runs/r153_10/vsRun628/";
		// ////////////////////////////////////////
		// MATSimNet2QGIS.setFlowCapFactor(0.1);
		// //////////////////////////////////////
		/*
		 * ///////////////////////////////////////////////////////////////
		 * Traffic Volumes and MATSim-network to Shp-file //
		 * ///////////////////////////////////////////////////////////////
		 */
		// mn2q.readNetwork(netFilename);
		// mn2q.setCrs(ch1903);
		// NetworkLayer net = mn2q.getNetwork();
		// VolumesAnalyzer va = new VolumesAnalyzer(3600, 24 * 3600 - 1, net);
		// mn2q.readEvents("../runs/run474/250.events.txt.gz", va);
		// List<Map<Id, Integer>> vols = createVolumes(net, va);
		// for (int i = 0; i < 24; i++) {
		// mn2q.addParameter("vol" + i + "-" + (i + 1) + "h", Integer.class,
		// vols.get(i));
		// }
		// mn2q.writeShapeFile("../runs/run474/250.volumes.shp");
		/*
		 * //////////////////////////////////////////////////////////////
		 * Differenz of Traffic Volumes and MATSim-network to Shp-file
		 * /////////////////////////////////////////////////////////////
		 */

		mn2q.readNetwork(netFilename);
		mn2q.setCrs(ch1903);
		NetworkLayer net = mn2q.getNetwork();
		VolumesAnalyzer vaA = new VolumesAnalyzer(3600, 24 * 3600 - 1, net);
		mn2q.readEvents(eventsFilenameA, vaA);
		List<Map<Id, Integer>> volsA = createVolumes(net, vaA);
		VolumesAnalyzer vaB = new VolumesAnalyzer(3600, 24 * 3600 - 1, net);
		mn2q.readEvents(eventsFilenameB, vaB);
		List<Map<Id, Integer>> volsB = createVolumes(net, vaB);

		for (int i = 0; i < 24; i++) {
			Volume2QGIS v2q = new Volume2QGIS();

			String index = i + "-" + (i + 1) + "h";
			Map<Id, Integer> diff = new TreeMap<Id, Integer>();
			Map<Id, Integer> sign = new TreeMap<Id, Integer>();

			v2q.setCrs(ch1903, mn2q.network, mn2q.crs, diff.keySet());

			for (Id linkId : volsB.get(i).keySet()) {
				int volDiff = volsA.get(i).get(linkId).intValue()
						- volsB.get(i).get(linkId).intValue();
				if (volDiff < 0) {
					diff.put(linkId, -volDiff);
					sign.put(linkId, -1);
				} else if (volDiff > 0) {
					diff.put(linkId, volDiff);
					sign.put(linkId, 1);
				}
			}
			v2q.addParameter("vol" + index, Integer.class, diff);
			v2q.addParameter("sign" + index, Integer.class, sign);
			v2q.writeShapeFile(shapeFilepath + index + ".shp");
		}
		/*
		 * /////////////////////////////////////////////////////////////////////
		 * Shp-file with 25 Layers
		 * //////////////////////////////////////////////
		 * //////////////////////////
		 */
		// mn2q.readNetwork(netFilename);
		// mn2q.setCrs(ch1903);
		// mn2q.writeShapeFile(shapeFilepath + "net.shp");
		// VolumesAnalyzer va = new VolumesAnalyzer(3600, 24 * 3600 - 1,
		// mn2q.network);
		// mn2q.readEvents(eventsFilename, va);
		// List<Map<Id, Integer>> vols = createVolumes(mn2q.network, va);
		// for (int i = 0; i < 24; i++) {
		// Volume2QGIS v2q = new Volume2QGIS();
		// v2q.setCrs(ch1903, mn2q.network, mn2q.crs);
		// String index = "vol" + i + "-" + (i + 1) + "h";
		// v2q.addParameter(index, Integer.class, vols.get(i));
		// v2q.writeShapeFile(shapeFilepath + "760." + index + ".shp");
		// }
		////////////////////////////////////////////////////////////////////////
		// //
	}
}
