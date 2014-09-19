/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.emissions.example;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;

/**
 * 
 * Creates a config file 
 * with necessary emission input files for the {@link org.matsim.contrib.emissions.utils.EmissionsConfigGroup EmissionsConfigGroup}.
 * 
 * This config file is used by the {@link org.matsim.contrib.emissions.example.RunEmissionToolOfflineExample OfflineExample} and 
 * the {@link org.matsim.contrib.emissions.example.RunEmissionToolOnlineExample OnlineExample} 
 * 
 * @author benjamin, julia
 *
 */
public class CreateEmissionConfig {

		static String inputPath = "./test/input/org/matsim/contrib/emissions/";
		static String networkFile = inputPath + "sample_network.xml";
		static String plansFile = inputPath + "sample_population.xml";
		
		static String roadTypeMappingFile = inputPath + "sample_roadTypeMapping.txt";
		static String emissionVehicleFile = inputPath + "sample_emissionVehicles.xml";
		
		static String averageFleetWarmEmissionFactorsFile = inputPath + "sample_EFA_HOT_vehcat_2005average.txt";
		static String averageFleetColdEmissionFactorsFile = inputPath + "sample_EFA_ColdStart_vehcat_2005average.txt";
		
		static boolean isUsingDetailedEmissionCalculation = true;
		static String detailedWarmEmissionFactorsFile = inputPath + "sample_EFA_HOT_SubSegm_2005detailed.txt";
		static String detailedColdEmissionFactorsFile = inputPath + "sample_EFA_ColdStart_SubSegm_2005detailed.txt";
		
		static String outputPath = "./test/output/";
		static String configFilePath = inputPath + "config.xml";
		
		static int numberOfIterations = 6;
		
		
		public static void main(String[] args) {
			
			Config config = new Config();
			config.addCoreModules();
			Controler controler = new Controler(config);
			
		// controlerConfigGroup
			ControlerConfigGroup ccg = controler.getConfig().controler();
			ccg.setOutputDirectory(outputPath);
			ccg.setFirstIteration(0);
			ccg.setLastIteration(numberOfIterations-1);
			
		// planCalcScoreConfigGroup
			PlanCalcScoreConfigGroup pcs = controler.getConfig().planCalcScore();
			ActivityParams homeP = new ActivityParams("home");
			homeP.setTypicalDuration(12 * 3600);
			pcs.addActivityParams(homeP);
			ActivityParams workP = new ActivityParams("work");
			workP.setTypicalDuration(8 * 3600);
			pcs.addActivityParams(workP);

		// strategy
			StrategyConfigGroup scg = controler.getConfig().strategy();
			StrategySettings strategySettings = new StrategySettings(Id.create("1", StrategySettings.class));
			strategySettings.setModuleName("ChangeExpBeta");
			strategySettings.setProbability(1.0);
			scg.addStrategySettings(strategySettings);
			
		// network
			NetworkConfigGroup ncg = controler.getConfig().network();
			ncg.setInputFile(networkFile);
			
		// plans
			PlansConfigGroup pcg = controler.getConfig().plans();
			pcg.setInputFile(plansFile);
			
		// define emission tool input files	
	        EmissionsConfigGroup ecg = new EmissionsConfigGroup() ;
	        controler.getConfig().addModule(ecg);
	        ecg.setEmissionRoadTypeMappingFile(roadTypeMappingFile);
	        ecg.setEmissionVehicleFile(emissionVehicleFile);
	        
	        ecg.setAverageWarmEmissionFactorsFile(averageFleetWarmEmissionFactorsFile);
	        ecg.setAverageColdEmissionFactorsFile(averageFleetColdEmissionFactorsFile);
	        
	        ecg.setUsingDetailedEmissionCalculation(isUsingDetailedEmissionCalculation);
	        ecg.setDetailedWarmEmissionFactorsFile(detailedWarmEmissionFactorsFile);
	        ecg.setDetailedColdEmissionFactorsFile(detailedColdEmissionFactorsFile);
			
	   // write config     
	        ConfigWriter cw = new ConfigWriter(config);
			cw.write(configFilePath);
			


	}

}
