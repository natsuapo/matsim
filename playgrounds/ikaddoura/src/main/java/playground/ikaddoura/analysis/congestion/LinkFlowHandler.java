/* *********************************************************************** *
 * project: org.matsim.*
 * CarCongestionHandler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package playground.ikaddoura.analysis.congestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.AgentStuckEvent;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.TransitDriverStartsEvent;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentStuckEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;
import org.matsim.core.events.handler.TransitDriverStartsEventHandler;

/**
 * @author Ihab
 *
 */
public class LinkFlowHandler implements LinkLeaveEventHandler, AgentDepartureEventHandler, AgentArrivalEventHandler, AgentStuckEventHandler {

	private final static Logger log = Logger.getLogger(LinkFlowHandler.class);
	private final Network network;
	
	private Map<Id, Double> linkId2previousLinkLeaveTime = new HashMap<Id, Double>();
	private List<Id> weirdAgents = new ArrayList<Id>();
	private Map<Id, Id> agentId2departureLinkId = new HashMap<Id, Id>();
	private Map<Id, Id> agentId2arrivalLinkId = new HashMap<Id, Id>();

	private int counterNoCong = 0;
	private int counterCong = 0;

	private final List<Id> ptVehicleIDs = new ArrayList<Id>();
	
	public LinkFlowHandler(Network network) {
		this.network = network;
	}

	@Override
	public void reset(int iteration) {
		linkId2previousLinkLeaveTime.clear();
	}
	
	
	@Override
	public void handleEvent(LinkLeaveEvent event) {
		
		if (this.ptVehicleIDs.contains(event.getVehicleId())){
			log.warn("Not tested for pt.");
	
		} else {
			// car!
			if (linkId2previousLinkLeaveTime.get(event.getLinkId()) == null){
				// first leave event on that link
			} else {
				Link link = this.network.getLinks().get(event.getLinkId());
				double flowCapacity_vehPerHour = link.getCapacity();
				double flowDelay = Math.floor((1 / (flowCapacity_vehPerHour / 3600.)));
				
				double previousLinkLeaveTime = this.linkId2previousLinkLeaveTime.get(event.getLinkId());
				double gap = event.getTime() - previousLinkLeaveTime;
				
				if (gap > flowDelay){
					// expected if not congested
					counterNoCong++;
				} else if (gap == flowDelay){
					counterCong++;
					// expected if congested
				} else {
					
					this.weirdAgents.add(event.getPersonId());
					
					System.out.println("----------------------------");
					System.out.println("personId: " + event.getPersonId() + " // linkId: " + event.getLinkId() + " // flowDelay: " + flowDelay + " // gap: " + gap);
					System.out.println(event.toString());

				}
			}
			this.linkId2previousLinkLeaveTime.put(event.getLinkId(), event.getTime());

		}
	}

	public void printResults() {
		
		System.out.println("noCong: " + counterNoCong);
		System.out.println("cong: " +  counterCong);
	}

	@Override
	public void handleEvent(AgentDepartureEvent event) {
		this.agentId2departureLinkId.put(event.getPersonId(), event.getLinkId());
	}


	@Override
	public void handleEvent(AgentArrivalEvent event) {
		this.agentId2arrivalLinkId.put(event.getPersonId(), event.getLinkId());	
		
//		if (this.weirdAgents.contains(event.getPersonId())){
//			System.out.println("----------------------------");
//			Id id = event.getPersonId();
//			System.out.println("Agent Id: " +  id);
//			System.out.println("Departure link: " + this.agentId2departureLinkId.get(id));
//			System.out.println("Arrival link: " + this.agentId2arrivalLinkId.get(id));
//		}
	}

	@Override
	public void handleEvent(AgentStuckEvent event) {
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++ stuck event: " + event.toString());
	}
	
}
