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

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * This is the common interface for the next MadKitGroupExtension agents : SwingViewer.
 * 
 * This documentation is copied/pasted and adapted from the MadKit documentation.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.2.1
 */
public interface MKGESwingViewer extends MKGEWatcher
{
	/**
	 * @return <code>true</code> if the rendering 
	 * activity is activated.
	 */
    public boolean isRendering();
    
	/**
	 * Enable or disable the rendering activity
	 * @param activate rendering activation
	 */
	public void setRendering(boolean activate);
	
	/**
	 * @return the current panel which is used for display
	 */
	public JComponent getDisplayPane();
	
	/**
	 * Could be used to define a customized panel 
	 * instead of the default pane
	 * 
	 * 
	 * @param displayPane the displayPane to set
	 */
	public void setDisplayPane(JComponent displayPane);
	
	
	/**
	 * Tells if the rendering should be done synchronously 
	 * or asynchronously with respect to simulation steps.
	 * 
	 * @return the synchronousPainting
	 */
	public boolean isSynchronousPainting();
	
	/**
	 * Set the rendering mode to synchronous or asynchronous. 
	 * Synchronous painting is done
	 * for each time step and the simulation does not advance until
	 * all the rendering is done for a step: The simulation is slower but 
	 * more smoothly rendered, making the visualization 
	 * of the simulation dynamics more precise. In asynchronous 
	 * mode, the rendering is done in parallel with the simulation
	 * steps and thus only display snapshot of the simulation's state:
	 * 
	 * @param synchronousPainting the synchronousPainting mode to set
	 */
	public void setSynchronousPainting(boolean synchronousPainting);
	
	/** Provides a default implementation that 
	 * assigns the default panel to the default frame
	 * 
	 * @see madkit.kernel.AbstractAgent#setupFrame(javax.swing.JFrame)
	 */
	public void setupFrame(javax.swing.JFrame frame);
	
	/**
	 * By default, get the default frame provided by MaDKit in 
	 * {@link #setupFrame(JFrame)} and
	 * set using {@link #setupFrame(JFrame)}.
	 * It can be anything else if {@link #setupFrame(JFrame)} is overridden.
	 * 
	 * @return the working frame
	 */
	public JFrame getFrame();
	
	/**
	 * Set the frame which is used so that
	 * subclasses can have access to it
	 * 
	 * @param frame the working frame
	 */
	public void setFrame(JFrame frame);	
}
