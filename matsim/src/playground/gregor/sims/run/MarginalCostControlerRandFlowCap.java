/* *********************************************************************** *
 * project: org.matsim.*
 * MarginalCostControler.java
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

package playground.gregor.sims.run;

import org.matsim.core.controler.Controler;

import playground.gregor.sims.socialcost.LinkFlowCapRandomizer;
import playground.gregor.sims.socialcost.MarginalTravelCostCalculatorII;
import playground.gregor.sims.socialcost.SocialCostCalculator;
import playground.gregor.sims.socialcost.SocialCostCalculatorSingleLink;

public class MarginalCostControlerRandFlowCap extends Controler{



	private double c;

	public MarginalCostControlerRandFlowCap(final String[] args, double c) {
		super(args);
		this.c = c;
		
	}

	@Override
	protected void setUp() {
		super.setUp();
		
		
		LinkFlowCapRandomizer lr = new LinkFlowCapRandomizer(this.network, this.c, 0.);
		
//		TravelTimeAggregatorFactory factory = new TravelTimeAggregatorFactory();
//		factory.setTravelTimeDataPrototype(TravelTimeDataHashMap.class);
//		factory.setTravelTimeAggregatorPrototype(PessimisticTravelTimeAggregator.class);
		SocialCostCalculator sc = new SocialCostCalculatorSingleLink(this.network,this.config.travelTimeCalculator().getTraveltimeBinSize());
		
		this.events.addHandler(sc);
		this.travelCostCalculator = new MarginalTravelCostCalculatorII(this.travelTimeCalculator,sc);
		this.strategyManager = loadStrategyManager();
		this.addControlerListener(sc);
		this.addControlerListener(lr);
	}

	public static void main(final String[] args) {
		double c = Double.parseDouble(args[1]);
		final Controler controler = new MarginalCostControlerRandFlowCap(new String [] {args[0]}, c);
		controler.setOverwriteFiles(true);
		controler.run();
		System.exit(0);
	}
}