/*
 * MadKitGroupExtension (created by Jason MAHDJOUB (jason.mahdjoub@free.fr)) Copyright (c)
 * 2012. Individual contributors are indicated by the @authors tag.
 * 
 * This file is part of MadKitGroupExtension.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package madkitgroupextension.kernel;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import madkit.kernel.Scheduler.SimulationState;

/**
 * This is the common interface for the next MadKitGroupExtension agents : Scheduler.
 * 
 * This documentation is copied/pasted and adapted from the MadKit documentation.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.2.1
 */
public interface MKGEScheduler extends MKGEAgent
{
	/**
	 * Returns the delay between two simulation steps
	 * 
	 * @return the delay between two simulation steps.
	 */
	public int getDelay();
	
	
	/**
	 * Sets the delay between two simulation steps. That is the pause time
	 * between to call to {@link #doSimulationStep()}
	 * 
	 * @param delay
	 *           the pause between two steps in ms, an integer between 0 and 400: O is max speed.
	 *           speed
	 */
	public void setDelay(final int delay);
	
	
	/**
	 * Returns the simulation global virtual time.
	 * 
	 * @return the gVT
	 */
	public double getGVT();
	
	/**
	 * Sets the simulation global virtual time.
	 * 
	 * @param GVT
	 *           the actual simulation time
	 */
	public void setGVT(final double GVT);
	
	/**
	 * Executes all the activators in the order they have been added, using the
	 * and increment the global virtual time of this scheduler by one unit. This
	 * method should be overridden to define customized scheduling policy. So
	 * default implementation is :
	 * 
	 * <pre>
	 * <tt>@Override</tt>
	 * public void doSimulationStep() {
	 * 	if (logger != null) {
	 * 		logger.finer("Doing simulation step "+GVT);
	 * 	}
	 * 	for (final Activator&#139;?&#155; activator : m_activators) {
	 * 		triggerActivator(activator);
	 * 	}
	 * 	setGVT(getGVT() + 1);
	 * }
	 * </pre>
	 */
	public void doSimulationStep();	
	
	/**
	 * The state of the simualtion.
	 * 
	 * @return the state in which the simulation is.
	 * @see SimulationState
	 */
	public SimulationState getSimulationState();
	
	/**
	 * Changes the state of the scheduler
	 * 
	 * @param newState
	 *           the new state
	 */
	public void setSimulationState(final SimulationState newState);
	
	
	/**
	 * @return the simulationDuration
	 */
	public double getSimulationDuration();	
	
	/**
	 * Returns a toolbar which could be used in any GUI.
	 * 
	 * @return a toolBar controlling the scheduler's actions
	 */
	public JToolBar getSchedulerToolBar();
	
	/**
	 * Returns a menu which could be used in any GUI.
	 * 
	 * @return a menu controlling the scheduler's actions
	 */
	public JMenu getSchedulerMenu();
	
	/**
	 * Returns a label giving some information on the simulation process
	 * 
	 * @return a label giving some information on the simulation process
	 */
	public JLabel getSchedulerStatusLabel();
	
	/**
	 * Adds an activator to the kernel engine. This has to be done to make an
	 * activator work properly
	 * 
	 * @param activator
	 *           an activator.
	 */
	public void addActivator(madkitgroupextension.kernel.Activator<?> activator);
	
	/**
	 * Removes an activator from the kernel engine.
	 * 
	 * @param activator
	 *           an activator.
	 */
	public void removeActivator(madkitgroupextension.kernel.Activator<?> activator);
	
	/**
	 * Removes all activators contained on this kernel engine.
	 * 
	 */
	public void removeAllActivators();
	
	public ArrayList<madkitgroupextension.kernel.Activator<?>> getActivators();
}
