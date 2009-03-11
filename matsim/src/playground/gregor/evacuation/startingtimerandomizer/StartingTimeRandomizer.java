/* *********************************************************************** *
 * project: org.matsim.*
 * StartingTimeRanomizer.java
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

package playground.gregor.evacuation.startingtimerandomizer;

import java.util.Random;

import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationWriter;
import org.matsim.world.World;

public class StartingTimeRandomizer {

	private final Population pop;
	private final double MEAN = 5;
	private final double SIGMA = 5;
	Random rand = new Random();
	public StartingTimeRandomizer(final Population pop) {
		this.pop = pop;
	}

	public void run(){
		for (Person pers : this.pop.getPersons().values()) {
			Plan plan = pers.getSelectedPlan();
			Activity act = plan.getFirstActivity();
			double endTime = getRandomTime(act.getEndTime());
			act.setEndTime(Math.max(3*3600,endTime));
		}
	}

	private double getRandomTime(final double time) {
		return time + 60* (this.MEAN + this.rand.nextGaussian() * this.SIGMA);
	}

	public static void main(final String [] args) {
		String planIn = "../inputs/networks/padang_plans_v20080618_reduced_10p.xml.gz";
		String planOut = "../inputs/networks/padang_plans_v20080618_reduced_10p_rndStartTime.xml.gz";
		String network = "../inputs/networks/padang_net_v20080618.xml";

		World world = Gbl.createWorld();
		Gbl.createConfig(null);
		NetworkLayer net = new NetworkLayer();
		new MatsimNetworkReader(net).readFile(network);
		world.setNetworkLayer(net);
		world.complete();
		Population pop = new PopulationImpl();
		new MatsimPopulationReader(pop, net).readFile(planIn);

		new StartingTimeRandomizer(pop).run();
		new PopulationWriter(pop,planOut,"v4").write();
	}

}
