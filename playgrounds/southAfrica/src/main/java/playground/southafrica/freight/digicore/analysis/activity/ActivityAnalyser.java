/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,     *
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

package playground.southafrica.freight.digicore.analysis.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Counter;

import playground.southafrica.freight.digicore.containers.DigicoreVehicle;
import playground.southafrica.utilities.FileUtils;
import playground.southafrica.utilities.Header;

public class ActivityAnalyser {
	final private static Logger LOG = Logger.getLogger(ActivityAnalyser.class);
	private final ExecutorService threadExecutor;
	
	/**
	 * Simple class to perform a number of analysis on activity times and 
	 * durations. The objective is to prepare data for R graphics.
	 * @param args
	 */
	public static void main(String[] args) {
		Header.printHeader(ActivityAnalyser.class.toString(), args);
		String inputFolder = args[0];
		String outputFile = args[1];
		int analysis = Integer.parseInt(args[2]);
		int numberOfThreads = Integer.parseInt(args[3]);
		
		ActivityAnalyser aa = new ActivityAnalyser(numberOfThreads);
		aa.analyse(analysis, inputFolder, outputFile);
		
		Header.printFooter();
	}
	
	
	/**
	 * Constructor with multiple thread handling. If you only require a single 
	 * thread, you can also use {@link #ActivityAnalyser()}.
	 */
	public ActivityAnalyser(int numberOfThreads) {
		/* Set up the multithreaded analysis. */
		this.threadExecutor = Executors.newFixedThreadPool(numberOfThreads);
	}
	
	
	/**
	 * Constructor with only a single thread. If you require multithreaded 
	 * analysis, you have to use {@link #ActivityAnalyser(int)}.
	 */
	public ActivityAnalyser() {
		this(1);
	}
	
	
	/**
	 * Perform a variety of analysis on digicore vehicle files.
	 * @param analysis a value indicating the specific analysis to perform. The
	 * 		  following values apply:
	 * 		  <ol>
	 * 			<li> minor activity start time;
	 * 		  </ol>
	 * @param input the folder where {@link DigicoreVehicle} files can be found;
	 * @param output the file to which the relevant analysis will be written.
	 * 		  This will typically then be further analysed using R. 
	 */
	public void analyse(int analysis, String input, String output){
		/* Check that input folder exists, and get the vehicle files. */
		File folder = new File(input);
		if(!folder.exists() || !folder.canRead()){
			throw new RuntimeException("Cannot read vehicle files from " + input);
		}
		List<File> vehicleFiles = FileUtils.sampleFiles(folder, Integer.MAX_VALUE, FileUtils.getFileFilter(".xml.gz"));
		

		switch (analysis) {
		case 1:
			runMinorActivityStartTimeAnalysis(vehicleFiles, output);
			break;
		default:
			break;
		}
	}

	
	private void runMinorActivityStartTimeAnalysis(List<File> vehicles, String output){
		LOG.info("Performing minor activity start time analysis (" + vehicles.size() + " vehicle files)");
		Counter counter = new Counter("   vehicles completed # ");
		List<ActivityStartTimeRunable> listOfJobs = new ArrayList<ActivityStartTimeRunable>(vehicles.size());
		
		/* Execute the multi-threaded analysis. */
		for(File file : vehicles){
			ActivityStartTimeRunable job = new ActivityStartTimeRunable(file, counter, "minor");
			this.threadExecutor.execute(job);
			listOfJobs.add(job);
		}
		
		/* Consolidate the output */
		Map<String, Integer> map = new TreeMap<String, Integer>();
		for(ActivityStartTimeRunable job : listOfJobs){
			Map<String, Integer> thisMap = job.getStartTimeMap();
			for(String s : thisMap.keySet()){
				if(!map.containsKey(s)){
					map.put(s, thisMap.get(s));
				} else{
					int oldCount = map.get(s);
					map.put(s, oldCount + thisMap.get(s));
				}
			}
		}
		
		/* Write the output to file. */
		BufferedWriter bw = IOUtils.getBufferedWriter(output);
		try{
			for(String s : map.keySet()){
				bw.write(s);
				bw.write(",");
				bw.write(String.valueOf(map.get(s)));
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot write to " + output);
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close " + output);
			}
		}
		
		LOG.info("Done with minor activity start time analysis.");
	}
}
