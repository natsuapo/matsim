/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.ikaddoura.internalizationCar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;

/**
 * @author ikaddoura
 *
 */
public class LinkCongestionInfo {
	
	private Id linkId;
	private double freeTravelTime;
	private double marginalDelayPerLeavingVehicle_sec;
	private int storageCapacity_cars;
	
	private Map<Id, Double> personId2linkLeaveTime = new HashMap<Id, Double>();
	private List<Id> leavingAgents = new ArrayList<Id>();
	
	private List<Id> agentsOnLink = new ArrayList<Id>();
	private Map<Id, Double> personId2freeSpeedLeaveTime = new HashMap<Id, Double>();
	
	public Id getLinkId() {
		return linkId;
	}
	public void setLinkId(Id linkId) {
		this.linkId = linkId;
	}
	public void setMarginalDelayPerLeavingVehicle(double flowCapacity_hour) {
		this.marginalDelayPerLeavingVehicle_sec = Math.floor((1 / (flowCapacity_hour / 3600.) ) );
	}
	public int getStorageCapacity_cars() {
		return storageCapacity_cars;
	}
	public void setStorageCapacity_cars(int storageCapacity_cars) {
		this.storageCapacity_cars = storageCapacity_cars;
	}
	public double getMarginalDelayPerLeavingVehicle_sec() {
		return marginalDelayPerLeavingVehicle_sec;
	}
	public double getFreeTravelTime() {
		return freeTravelTime;
	}
	public void setFreeTravelTime(double freeTravelTime) {
		this.freeTravelTime = freeTravelTime;
	}
	public Map<Id, Double> getPersonId2linkLeaveTime() {
		return personId2linkLeaveTime;
	}
	public List<Id> getLeavingAgents() {
		return leavingAgents;
	}
	public List<Id> getAgentsOnLink() {
		return agentsOnLink;
	}
	public Map<Id, Double> getPersonId2freeSpeedLeaveTime() {
		return personId2freeSpeedLeaveTime;
	}
	public void setPersonId2freeSpeedLeaveTime(
			Map<Id, Double> personId2freeSpeedLeaveTime) {
		this.personId2freeSpeedLeaveTime = personId2freeSpeedLeaveTime;
	}
}
