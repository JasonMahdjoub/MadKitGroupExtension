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

import java.io.Serializable;

import madkit.kernel.KernelAddress;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the agent/group/role principle
 * by giving the possibility to the user to work with a hierarchy of groups. So one group can have one or more subgroups. 
 * These last groups can have also subgroups, etc. Such groups are represented by the class {@link Group}.
 * But the user can also work with several groups taken arbitrarily on the hierarchy. Such grouping of groups is encapsulated 
 * by the class {@link MultiGroup}.  
 * 
 * The class AbstractGroup is the super class of these two classes.
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see Group
 * @see MultiGroup
 */
public abstract class AbstractGroup implements Serializable, Cloneable
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 2233207859620713364L;

    /**
     * This function returns the represented groups by the current instance.
     * These groups can be a list of subgroups of one group, 
     * a list of arbitrary defined groups or list of only one group. Only groups
     * that are used on MadKit, i.e. by agents, are returned. Groups that are only 
     * instantiated on the program are not returned.   
     * 
     * @param ka the used kernel address. 
     * @return the represented groups
     * @since MadKitGroupExtension 1.0
     * @see Group
     * @see MultiGroup
     */
    public abstract Group[] getRepresentedGroups(KernelAddress ka);
    
    @Override public abstract AbstractGroup clone();
    
}
