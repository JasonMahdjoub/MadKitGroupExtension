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


/**
 * This is the common interface for the next MadKitGroupExtension agents : Watcher, SwingViewer.
 * 
 * This documentation is copied/pasted and adapted from the MadKit documentation.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.2.1
 */
public interface MKGEWatcher extends MKGEAbstractAgent
{
	/**
	 * Remove all probes at once.
	 */
    public void removeAllProbes();
    
    public void addProbe(Probe<? extends madkit.kernel.AbstractAgent> _probe);
    
    public void removeProbe(Probe<? extends madkit.kernel.AbstractAgent> _probe);
    
    public Probe<?>[] probes();
}
