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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import madkit.kernel.KernelAddress;
import madkitgroupextension.kernel.MultiGroup.AssociatedGroup;

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
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     */
    public abstract Group[] getRepresentedGroups(KernelAddress ka);
    
    @Override public abstract AbstractGroup clone();
    
    
    /**
     * This function returns the intersection between the represented groups by this group into the given KernelAddress, and those of the given abstract group in parameter. 
     * 
     * @param ka the used kernel address
     * @param _group the group to operate with
     * @return a list of groups which results from the request
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public ArrayList<Group> intersect(KernelAddress ka, AbstractGroup _group)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	if (_group==null)
	    return new ArrayList<>();
	Group gs1[]=this.getRepresentedGroups(ka);
	Group gs2[]=_group.getRepresentedGroups(ka);
	ArrayList<Group> groups=new ArrayList<>(Math.min(gs1.length, gs2.length));
	if (gs1.length<gs2.length)
	{
	    Group tmp[]=gs1;
	    gs1=gs2;
	    gs2=tmp;
	}
	for (Group g1 : gs1)
	{
	    for (Group g2 : gs2)
	    {
		if (g1.equals(g2))
		{
		    groups.add(g1);
		    break;
		}
	    }
	}
	groups.trimToSize();
	return groups;
    }

    /**
     * This function returns the union between the represented groups by this group into the given KernelAddress, and those of the given abstract group in parameter. 
     * 
     * @param ka the used kernel address
     * @param _group the group to operate with
     * @return a list of groups which results from the request
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public HashSet<Group> union(KernelAddress ka, AbstractGroup _group)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	if (_group==null)
	{
	    HashSet<Group> res=new HashSet<>();
	    Collections.addAll(res, this.getRepresentedGroups(ka));
	    return res;
	}
	Group gs1[]=this.getRepresentedGroups(ka);
	Group gs2[]=_group.getRepresentedGroups(ka);
	HashSet<Group> groups=new HashSet<>();

	for (Group g1 : gs1)
	{
	    groups.add(g1);
	}
	for (Group g2 : gs2)
	{
	    groups.add(g2);
	}
	return groups;
    }
    
    /**
     * This function returns the symmetric difference between the represented groups by this group into the given KernelAddress, and those of the given abstract group in parameter. 
     * 
     * @param ka the used kernel address
     * @param _group the group to operate with
     * @return a list of groups which results from the request
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public HashSet<Group> symmetricDifference(KernelAddress ka,  AbstractGroup _group)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	if (_group==null)
	{
	    HashSet<Group> res=new HashSet<>();
	    Collections.addAll(res, this.getRepresentedGroups(ka));
	    return res;
	}
	ArrayList<Group> gi=this.intersect(ka, _group);
	HashSet<Group> gu=this.union(ka, _group);
	gu.removeAll(gi);
	return gu;
    }
    
    /**
     * This function returns the represented groups by this group into the given KernelAddress, minus those of the given abstract group in parameter. 
     * 
     * @param ka the used kernel address
     * @param _group the group to operate with
     * @return a list of groups which results from the request
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public ArrayList<Group> minus(KernelAddress ka,  AbstractGroup _group)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	if (_group==null)
	{
	    ArrayList<Group> res=new ArrayList<>();
	    Collections.addAll(res, this.getRepresentedGroups(ka));
	    return res;
	}
	Group gs1[]=this.getRepresentedGroups(ka);
	Group gs2[]=_group.getRepresentedGroups(ka);
	ArrayList<Group> groups=new ArrayList<>(gs1.length);
	for (Group g1 : gs1)
	{
	    boolean found=false;
	    for (Group g2 : gs2)
	    {
		if (g1.equals(g2))
		{
		    found=true;
		    break;
		}
	    }
	    if (!found)
		groups.add(g1);
	}
	groups.trimToSize();
	return groups;
    }
    
    /**
     * This function tells if the represented groups by this group into the given KernelAddress, include those of the given abstract group in parameter. 
     * 
     * @param ka the used kernel address
     * @param _group the group to operate with
     * @return a list of groups which results from the request
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public boolean include(KernelAddress ka, AbstractGroup _group)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	if (_group==null)
	    return true;
	Group gs1[]=this.getRepresentedGroups(ka);
	Group gs2[]=_group.getRepresentedGroups(ka);
	if (gs1.length<gs2.length)
	    return false;
	for (Group g2 : gs2)
	{
	    boolean found=false;
	    for (Group g1 : gs1)
	    {
		if (g2.equals(g1))
		{
		    found=true;
		    break;
		}
	    }
	    if (!found)
		return false;
	}
	return true;
    }
    
    /**
     * This function tells there is no represented groups by this group into the given KernelAddress. 
     * 
     * @param ka the used kernel address
     * @return true if this group has no represented groups into the given kernel address.
     * @since MadKitGroupExtension 1.5
     * @see #getRepresentedGroups(KernelAddress)
     * @see KernelAddress
     * @see Group
     * @see MultiGroup
     * @throws NullPointerException if ka is null
     */
    public boolean isEmpty(KernelAddress ka)
    {
	if (ka==null)
	    throw new NullPointerException("ka");
	return this.getRepresentedGroups(ka).length>0;
    }

    /**
     * This function returns an AbstractGroup that intersects this group with the given abstract group in parameter.
     * @param _group the group to intersect with
     * @return the intersection result
     * @see Group
     * @see MultiGroup
     * @since MadKitGroupExtension 1.5
     */
    public AbstractGroup intersect(AbstractGroup _group)
    {
	if (_group==null)
	    return new MultiGroup();
	if (this==_group)
	    return this;
	if (this instanceof Group)
	{
	    Group This=(Group)this;
	    if (_group instanceof Group)
	    {
		Group group=(Group)_group;
		if (group.equals(This))
		{
		    if (group.isUsedSubGroups()==This.isUsedSubGroups())
			return This;
		    if (group.isUsedSubGroups())
			return This;
		    else
			return group;
		}
		else
		{
		    if (This.getCommunity().equals(group.getCommunity()))
		    {
			if (This.getPath().startsWith(group.getPath()))
			    return This;
			else if (group.getPath().startsWith(This.getPath()))
			    return group;
		    }
		    return new MultiGroup();
		}
	    }
	    else if (_group instanceof MultiGroup)
	    {
		return _group.intersect(this);
	    }
	}
	else if (this instanceof MultiGroup)
	{
	    MultiGroup This=(MultiGroup)this;
	    if (_group instanceof Group)
	    {
		MultiGroup res=new MultiGroup();
		ArrayList<AbstractGroup> forbiden=new ArrayList<>();
		synchronized(this)
		{
		    Group group=(Group)_group;
		    ArrayList<AbstractGroup> AThis=new ArrayList<>();
		    
		    for (AssociatedGroup ag : This.m_groups)
		    {
			if (ag.m_forbiden)
			{
			    forbiden.add(ag.m_group);
			}
			else
			    AThis.add(ag.m_group);
		    }
		    for (AbstractGroup ag : AThis)
		    {
			AbstractGroup tmp=ag.intersect(group);
			if (tmp!=null)
			{
			    res.addGroup(tmp);
			}
		    }
		    if (res.m_groups.size()==0)
			return new MultiGroup();
		    for (AbstractGroup ag : forbiden)
		    {
			res.addForbidenGroup(ag);
		    }
		    for (AssociatedGroup ag : res.m_groups)
		    {
			if (!ag.m_forbiden)
			    return res;
		    }
		    return new MultiGroup();
		}
	    }
	    else if (_group instanceof MultiGroup)
	    {
		MultiGroup res=new MultiGroup();
		ArrayList<AbstractGroup> forbiden=new ArrayList<>();
		synchronized(this)
		{
		    MultiGroup group=(MultiGroup)_group;
		    ArrayList<AbstractGroup> AThis=new ArrayList<AbstractGroup>();
		    MultiGroup AGroup=new MultiGroup();
		    
		    synchronized(group)
		    {
			for (AssociatedGroup ag : This.m_groups)
			{
			    if (ag.m_forbiden)
			    {
				forbiden.add(ag.m_group);
			    }
			    else
				AThis.add(ag.m_group);
			}
			for (AssociatedGroup ag : group.m_groups)
			{
			    if (ag.m_forbiden)
			    {
				forbiden.add(ag.m_group);
			    }
			    else
				AGroup.addGroup(ag.m_group);
			}
			if (AGroup.m_groups.size()==0)
			    return new MultiGroup();
			for (AbstractGroup ag : AThis)
			{
			    AbstractGroup tmp=ag.intersect(AGroup);
			    if (tmp!=null)
			    {
				res.addGroup(tmp);
			    }
			 }
			if (res.m_groups.size()==0)
			    return new MultiGroup();
			for (AbstractGroup ag : forbiden)
			{
			    res.addForbidenGroup(ag);
			}
			for (AssociatedGroup ag : res.m_groups)
			{
			    if (!ag.m_forbiden)
				return res;
			}
			return new MultiGroup();
		    }
		}
	    }
	}
	return new MultiGroup();
    }
    
    /**
     * This function returns an AbstractGroup that union this group with the given abstract group in parameter.
     * @param _group the group to union with
     * @return the union result
     * @see Group
     * @see MultiGroup
     * @since MadKitGroupExtension 1.5
     */
    public AbstractGroup union(AbstractGroup _group)
    {
	if (_group==null || _group==this)
	    return this;
	MultiGroup res=new MultiGroup();

	res.addGroup(_group);
	res.addGroup(this);
	return res;
    }
    
    /**
     * This function returns an AbstractGroup that is the symmetric difference between this group and the given abstract group in parameter.
     * @param _group the group to operate with
     * @return the symmetric difference result
     * @see Group
     * @see MultiGroup
     * @since MadKitGroupExtension 1.5
     */
    public AbstractGroup symmetricDifference( AbstractGroup _group)
    {
	if (_group==null)
	    return this;
	if (_group==this)
	    return new MultiGroup();
	
	AbstractGroup union=this.union(_group);
	AbstractGroup intersection=this.intersect(_group);
	return union.minus(intersection);
    }
    
    boolean isPerhapsEmpty()
    {
	if (this instanceof Group)
	    return false;
	else if (this instanceof MultiGroup)
	{
	    for (AssociatedGroup ag : ((MultiGroup)this).m_groups)
	    {
		if (!ag.m_forbiden)
		    return false;
	    }
	    return true;
	}
	return false;
    }
    
    /**
     * This function returns an AbstractGroup that is the result of this group minus the given abstract group in parameter.
     * @param _group the group to operate with
     * @return the subtraction result
     * @see Group
     * @see MultiGroup
     * @since MadKitGroupExtension 1.5
     */
    public AbstractGroup minus(AbstractGroup _group)
    {
	if (_group==null)
	    return this;
	if (this==_group)
	    return new MultiGroup();
	if (this instanceof MultiGroup)
	{
	    ((MultiGroup)this).addForbidenGroup(_group);
	    return this;
	}
	else
	{
	    MultiGroup res=new MultiGroup();
	    res.addGroup(this);
	    res.addForbidenGroup(_group);
	    return res;
	}
    }
    
    
}
