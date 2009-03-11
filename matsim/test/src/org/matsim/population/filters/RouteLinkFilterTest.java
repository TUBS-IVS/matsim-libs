/* *********************************************************************** *
 * project: org.matsim.*
 * RouteLinkFilterTest.java.java
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

package org.matsim.population.filters;

import java.util.ArrayList;
import java.util.List;

import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.basic.v01.BasicLeg.Mode;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.CarRoute;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.PersonImpl;
import org.matsim.population.PopulationImpl;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.utils.StringUtils;

public class RouteLinkFilterTest extends MatsimTestCase {

	public void testRouteLinkFilter() {
		loadConfig(null); // used to set the default dtd-location
		Population population = getTestPopulation();

		TestAlgorithm tester = new TestAlgorithm();

		RouteLinkFilter linkFilter = new RouteLinkFilter(tester);
		linkFilter.addLink(new IdImpl(15));

		SelectedPlanFilter selectedPlanFilter = new SelectedPlanFilter(linkFilter);
		selectedPlanFilter.run(population);
		assertEquals(3, population.getPersons().size());
		assertEquals(2, linkFilter.getCount());
	}

	private Population getTestPopulation() {
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile("test/scenarios/equil/network.xml");

		Link link1 = network.getLink(new IdImpl(1));
		Link link20 = network.getLink(new IdImpl(20));

		Population population = new PopulationImpl(PopulationImpl.NO_STREAMING);

		Person person;
		Plan plan;
		Leg leg;
		CarRoute route;

		person = new PersonImpl(new IdImpl("1"));
		plan = person.createPlan(true);
		Activity a = plan.createAct("h", link1);
		a.setEndTime(7.0 * 3600);
		leg = plan.createLeg(Mode.car);
		route = (CarRoute) network.getFactory().createRoute(BasicLeg.Mode.car, link1, link20);
		route.setNodes(link1, getNodesFromString(network, "2 7 12"), link20);
		leg.setRoute(route);
		plan.createAct("w", link20);
		population.addPerson(person);

		person = new PersonImpl(new IdImpl("2"));
		plan = person.createPlan(true);
		Activity a2 = plan.createAct("h", link1);
		a2.setEndTime(7.0 * 3600 + 5.0 * 60);
		leg = plan.createLeg(Mode.car);
		route = (CarRoute) network.getFactory().createRoute(BasicLeg.Mode.car, link1, link20);
		route.setNodes(link1, getNodesFromString(network, "2 7 12"), link20);
		leg.setRoute(route);
		plan.createAct("w", link20);
		population.addPerson(person);

		person = new PersonImpl(new IdImpl("3"));
		plan = person.createPlan(true);
		Activity a3 = plan.createAct("h", link1);
		a3.setEndTime(7.0 * 3600 + 10.0 * 60);
		leg = plan.createLeg(Mode.car);
		route = (CarRoute) network.getFactory().createRoute(BasicLeg.Mode.car, link1, link20);
		route.setNodes(link1, getNodesFromString(network, "2 6 12"), link20);
		leg.setRoute(route);
		plan.createAct("w", link20);
		population.addPerson(person);

		return population;
	}

	/*package*/ static class TestAlgorithm implements PlanAlgorithm {

		public void run(final Plan plan) {
			assertTrue("1".equals(plan.getPerson().getId().toString())
					|| "2".equals(plan.getPerson().getId().toString()));
		}

	}

	private List<Node> getNodesFromString(final NetworkLayer network, final String nodes) {
		List<Node> nodesList = new ArrayList<Node>();
		for (String node : StringUtils.explode(nodes, ' ')) {
			nodesList.add(network.getNode(node));
		}
		return nodesList;
	}

}
