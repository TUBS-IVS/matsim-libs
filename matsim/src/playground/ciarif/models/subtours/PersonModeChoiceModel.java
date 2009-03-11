/* *********************************************************************** *
 * project: org.matsim.*
 * PersonModeChoiceModel.java
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

package playground.ciarif.models.subtours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.matsim.basic.v01.BasicActImpl;
import org.matsim.gbl.Gbl;
import org.matsim.gbl.MatsimRandom;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.utils.geometry.CoordImpl;
import org.matsim.world.Layer;
import org.matsim.world.Location;

import playground.balmermi.census2000.data.Municipalities;
import playground.balmermi.census2000.data.Municipality;
import playground.balmermi.census2000.data.Persons;



public class PersonModeChoiceModel extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private static final String W = "w";
	private static final String H = "h";
	private static final CoordImpl ZERO = new CoordImpl(0.0,0.0);
	private final Persons persons;
	private final Municipalities municipalities; // Da cambiare per PersonStreaming
	private ModelModeChoice model;
	private List<PersonSubtour> personSubtours = new Vector<PersonSubtour>();
	 
		
	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////
	//public PersonModeChoiceModel(final Persons persons) {// Da mettere per PersonStreaming
	public PersonModeChoiceModel(final Persons persons, Municipalities municipalities) {// Da mettere per SubtourPersonStreaming
		System.out.println("    init " + this.getClass().getName() + " module...");
		this.persons = persons;
		this.municipalities = municipalities;// Da togliere per PersonStreaming
		System.out.println("    done.");
	}
	
	//////////////////////////////////////////////////////////////////////
	// get/set methods
	//////////////////////////////////////////////////////////////////////

	public List<PersonSubtour> getPersonSubtours() {
		return personSubtours;
	}


	public void setPersonSubtours(List<PersonSubtour> personSubtours) {
		this.personSubtours = personSubtours;
	}
	
	//////////////////////////////////////////////////////////////////////
	// private methods
	//////////////////////////////////////////////////////////////////////
	
	private final void setUpModeChoice(final Plan plan, final PersonSubtour personSubtour) {	
		// setting subtour parameters
		if (plan == null) { Gbl.errorMsg("Person id=" + plan.getPerson().getId() + "does not have a selected plan."); }
		Iterator<BasicActImpl> act_it = plan.getIteratorAct();
		Coord home_coord = null;
		Coord work_coord = null;
		act_it.hasNext();
		double dist_h_w = 0.0;
		while (act_it.hasNext()) {
			Activity act = (Activity)act_it.next();
			if (H.equals(act.getType().substring(0,1))) { home_coord = act.getCoord();}
			else if (W.equals(act.getType().substring(0,1))) { work_coord = act.getCoord(); }
		}
		if ((home_coord == null) || (home_coord.equals(ZERO))) { Gbl.errorMsg("No home coord defined!"); }
		if ((work_coord != null) && (work_coord.equals(ZERO))) { Gbl.errorMsg("Weird work coord defined!!!"); }
		if (work_coord != null) { 
			dist_h_w = work_coord.calcDistance(home_coord); 
			dist_h_w = dist_h_w/1000.0;
		}
		//System.out.println("Work coord " + work_coord);

		int subtours_nr = personSubtour.getSubtours().size()-1;
		for (int i=0; i<=subtours_nr; i=i+1){
			
			Subtour sub = personSubtour.getSubtours().get(i);
			int mainpurpose = sub.getPurpose();
			// choose mode choice model based on main purpose
			if (plan.getPerson().getAge() >=18) {
				if (mainpurpose == 0) {model = new ModelModeChoiceWork18Plus();}
				else if (mainpurpose == 1) {model = new ModelModeChoiceEducation18Plus();}
				else if (mainpurpose == 2) {model = new ModelModeChoiceShop18Plus();}
				else if (mainpurpose == 3) {model = new ModelModeChoiceLeisure18Plus();}
				else { Gbl.errorMsg("This should never happen!"); }
			}
			else {
				if (mainpurpose == 1) {model = new ModelModeChoiceEducation18Minus ();}
				else {model = new ModelModeChoiceOther18Minus ();}
			}
					
			// generating a random bike ownership (see STRC2007 paper Ciari for more details)
//			boolean has_bike = true;
			boolean has_bike = false;
			if (MatsimRandom.random.nextDouble() < 0.56) { has_bike = true; }			

			////////////////////////////////////////////////////////////////////////////////
			// TODO [balmermi]: Check for antoher probability
//			boolean ride_possible = true;
			boolean ride_possible = false;
			double rd2 = MatsimRandom.random.nextDouble ();
			if (plan.getPerson().hasLicense()) {}
			else {
				if (rd2 < 0.60) {ride_possible = true; System.out.println("random = " + rd2);} // should be substituted with car ownership 
				// at the houshold level or something similar 
			}
			////////////////////////////////////////////////////////////////////////////////
			
			////////////////////////////////////////////////////////////////////////////////
			// TODO [balmermi]: Check for antoher probability
			//boolean pt = true;			
			boolean pt = false; // Should be substituted with actual access to pt;
			double rd3 = MatsimRandom.random.nextDouble (); 
			if (plan.getPerson().getCarAvail().equals("always")) {
				if (rd3 < 0.45) {pt = true;}
				System.out.println("pt = " + pt );
			}
			else if (rd3 < 0.90) {pt =true;}
			//else {pt =true;}	
			////////////////////////////////////////////////////////////////////////////////
			
			if (sub.getPrev_subtour()>=0){
				if (i>=1) {
					model.setPrevMode (personSubtour.getSubtours().get(i-1).getMode());
					personSubtour.getSubtours().get(i).setPrev_mode(personSubtour.getSubtours().get(i-1).getMode());
				}
			}
			// -1 means that the subtour starts at home
			else {
				model.setPrevMode(-1);personSubtour.getSubtours().get(i).setPrev_mode(-1);
				}
			
			// setting person parameters
			model.setPt(pt);
			model.setRide(ride_possible);
			model.setDistanceHome2Work(dist_h_w);
			model.setAge(plan.getPerson().getAge());
			model.setLicenseOwnership(plan.getPerson().hasLicense());
			model.setCar(plan.getPerson().getCarAvail());
			model.setTickets(plan.getPerson().getTravelcards());
			model.setBike(has_bike);
			model.setMale (plan.getPerson().getSex());
			//model.setHHDimension(p.getHousehold().getPersonCount());
			
			// To be used when mun data is not available in order to have a reasonable distributions of starting points
//			int udeg = 1; // // Da cambiare per PersonStreaming
//			double rd4 = Gbl.random.nextDouble();
//			if (rd4<=0.81) {udeg=2;}
//			if (rd4<0.70) {udeg=3;}
//			if (rd4<0.63) {udeg=4;}
//			if (rd4<.30) {udeg=5;}
					
			
			Layer muni_layer = Gbl.getWorld().getLayer(Municipalities.MUNICIPALITY);
			ArrayList<Location> locs = muni_layer.getNearestLocations(sub.getStart_coord());
			Location loc = locs.get(MatsimRandom.random.nextInt(locs.size()));
			Municipality m = municipalities.getMunicipality(new Integer(loc.getId().toString()));
			int udeg = m.getRegType();
			//System.out.println ("udeg");
			//Iterator<Location> l_it = Gbl.getWorld().getLayer(Municipalities.MUNICIPALITY).getLocations().values().iterator(); //TODO controllare se serve!!!!!
			
			
			model.setUrbanDegree(udeg);
			model.setMainPurpose(mainpurpose);
			model.setDistanceTour(sub.getDistance()); 			 
			model.setHomeCoord(home_coord);
			
			// getting the chosen mode
			int modechoice = model.calcModeChoice();
			BasicLeg.Mode mode = null;
			if (modechoice == 0) { mode = BasicLeg.Mode.car; }
			else if (modechoice == 1) { mode = BasicLeg.Mode.pt; }
			else if (modechoice == 2) { mode = BasicLeg.Mode.ride; }
			else if (modechoice == 3) { mode = BasicLeg.Mode.bike; }
			else if (modechoice == 4) { mode = BasicLeg.Mode.walk; }
			else { Gbl.errorMsg("Mode choice returns undefined value!"); }
			System.out.println("modechoice = " + modechoice);
			System.out.println();
			personSubtour.getSubtours().get(i).setMode(modechoice);
			personSubtour.getSubtours().get(i).setStart_udeg(udeg);
			
			for (int k=1; k<sub.getNodes().size(); k=k+1){
				((Leg)plan.getPlanElements().get(sub.getNodes().get(k)-1)).setMode(mode);
				System.out.println("leg = " + ((Leg)plan.getPlanElements().get(sub.getNodes().get(k)-1)));
								
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {
		Plan plan = person.getSelectedPlan();
		int subtour_idx =0;
		TreeMap<Integer, ArrayList<Integer>> subtours = new TreeMap<Integer,ArrayList<Integer>>();
		PersonSubTourExtractor pste = new PersonSubTourExtractor(this.persons);
		pste.run(person);
		subtours = pste.getSubtours();
		subtour_idx = pste.getSubtourIdx();
		System.out.println("person_id = " + person.getId());
		PersonSubtourHandler psh = new PersonSubtourHandler();
		PersonSubtour personSubtour = new PersonSubtour(); //TODO Change the constructor and place it below the psh.run
		psh.run(plan,subtours,subtour_idx);
		personSubtour = psh.getPers_sub();
		this.setUpModeChoice(plan,personSubtour);
		personSubtour.setPerson_id(person.getId());	
		this.personSubtours.add(personSubtour);	
	}
	
	
	public void run(Plan plan){
	}
}

