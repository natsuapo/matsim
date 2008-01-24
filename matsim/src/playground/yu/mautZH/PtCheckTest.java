/* *********************************************************************** *
 * project: org.matsim.*
 * PtCheckTest.java
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
package playground.yu.mautZH;

import java.io.IOException;

import org.matsim.controler.Controler;

/**
 * @author ychen
 * 
 */
public class PtCheckTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Controler c = new Controler(args);
		c.addControlerListener(new PtCheckListener(c));
		c.run();
		System.exit(0);
	}
}
