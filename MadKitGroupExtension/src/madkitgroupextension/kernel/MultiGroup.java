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
import java.util.concurrent.atomic.AtomicReference;

import madkit.kernel.KernelAddress;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the agent/group/role principle
 * by giving the possibility to the user to work with a hierarchy of groups. So one group can have one or more subgroups. 
 * These last groups can have also subgroups, etc. Such groups are represented by the class {@link Group}.
 * But the user can also work with several groups taken arbitrarily on the hierarchy. Such grouping of groups is encapsulated 
 * by the class {@link MultiGroup}.  
 *
 * The class MultiGroup combines several type of groups chosen arbitrarily. These groups can be {@link Group} class, or {@link MultiGroup} class. 
 * So a MultiGroup can be composed for example of a Group which represent its subgroups, and by another MutliGroup.
 * 
 * It is also possible to combine forbidden groups. For example, we want to get subgroups of the Group "My group":
 * <pre>
 * MultiGroup mg=new MultiGroup(new Group(true, "My community", "My group"));
 * </pre>
 * 
 * But we want also to exclude the subgroup "One subgroup". It is possible to do it like this:
 * </pre>
 * mg.addForbidenGroup(new Group(true, "My community", "My group", "One subgroup"));
 * </pre>
 * If we use {@link #getRepresentedGroups(KernelAddress)} to get the represented groups by 'mg', 
 * all subgroups of "My Group" will be returned, excepted "One subgroup". 
 *    
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see AbstractGroup
 * @see MultiGroup
 */
public class MultiGroup extends AbstractGroup
{
    
    /**
     * 
     */
    private static final long serialVersionUID = -1107997740777891372L;
    
    private ArrayList<AssociatedGroup> m_groups=new ArrayList<AssociatedGroup>();
    
    
    private static final class RepresentedGroupsDuplicated
    {
	public final AtomicReference<Group[]> m_represented_groups_duplicated=new AtomicReference<Group[]>(null);
	public KernelAddress m_kernel=null;
	
	public RepresentedGroupsDuplicated(KernelAddress ka)
	{
	    m_kernel=ka;
	}
    }
    
    private transient ArrayList<RepresentedGroupsDuplicated> m_represented_groups_by_kernel_duplicated=new ArrayList<RepresentedGroupsDuplicated>();
    
    //private Group[] m_represented_groups_duplicated=null;
    /**
     * Construct a MultiGroup which combine the different groups (Group and MultiGroup) given as parameter
     * @param _groups the different groups to combine.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    public MultiGroup(AbstractGroup..._groups)
    {
	if (_groups.length==0)
	    throw new IllegalArgumentException("There is no group given as parameter !");
	
	for (AbstractGroup g : _groups)
	{
	    addGroup(g);
	}
    }
    private MultiGroup()
    {
	
    }
    
    @Override synchronized public MultiGroup clone()
    {
	MultiGroup res=new MultiGroup();
	res.m_groups=new ArrayList<AssociatedGroup>(m_groups.size());
	for (int i=0;i<m_groups.size();i++)
	{
	    AssociatedGroup ag = m_groups.get(i);
	    if (ag.m_group instanceof MultiGroup)
	    {
		res.m_groups.add(new AssociatedGroup((AbstractGroup)(((MultiGroup)ag.m_group).clone()), ag.m_forbiden));
	    }
	    else
		res.m_groups.add(new AssociatedGroup(ag.m_group, ag.m_forbiden));
	}
	return res;
    }
    @Override public synchronized String toString()
    {
	StringBuffer sb=new StringBuffer();
	sb.append("MultiGroup[");
	int s1=m_groups.size()-1;
	for (int i=0;i<s1;i++)
	{
	    AssociatedGroup ag=m_groups.get(i);
	    if (ag.m_forbiden)
		sb.append("Forbiden");
	    sb.append(ag.m_group);
	    sb.append(", ");
	}
	AssociatedGroup ag=m_groups.get(s1);
	if (ag.m_forbiden)
	    sb.append("Forbiden");
	sb.append(ag.m_group);
	sb.append("]");
	return sb.toString();
    }
    
    /**
     * Add a new group ({@link Group} or {@link MultiGroup}) which will be combined with the current MultiGroup.
     * @param _g the abstract group to add.
     * @return false if the group has been already added.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    synchronized public boolean addGroup(AbstractGroup _g)
    {
	if (!m_groups.contains(_g))
	{
	    m_groups.add(new AssociatedGroup(_g, false));
	    for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
		rgd.m_represented_groups_duplicated.set(null);
	    return true;
	}
	return false;
    }
    /**
     * Add a new forbidden group ({@link Group} or {@link MultiGroup}) which will forbid the representation of itself onto the current MultiGroup (see the class description).
     * @param _g the group to forbid.
     * @return false if the group has been already added.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    synchronized public boolean addForbidenGroup(AbstractGroup _g)
    {
	if (!m_groups.contains(_g))
	{
	    m_groups.add(new AssociatedGroup(_g, true));
	    for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
		rgd.m_represented_groups_duplicated.set(null);
	    return true;
	}
	return false;
    }
    /**
     * Remove the group given as parameter from the current MultiGroup. This group can be forbidden or not.
     * @param _g the group to remove.
     * @return false if group was not found.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    synchronized public boolean removeGroup(AbstractGroup _g)
    {
	if (m_groups.remove(_g))
	{
	    for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
		rgd.m_represented_groups_duplicated.set(null);
	    return true;
	}
	return false;	    
    }
    
    @Override public boolean equals(Object o)
    {
	if (o instanceof AbstractGroup)
	{
	    return this.equals((MultiGroup)o);
	}
	else
	    return false;
    }

    public boolean equals(AbstractGroup _ag)
    {
	if (_ag==this)
	    return true;
	
	Group[] g1=this.getRepresentedGroups(Group.getFirstUsedMadKitKernel());
	Group[] g2=_ag.getRepresentedGroups(Group.getFirstUsedMadKitKernel());
	if (g1.length!=g2.length)
	    return false;
	for (Group gg1 : g1)
	{
	    boolean ok=false;
	    for (Group gg2 : g2)
	    {
		if (gg1.equals(gg2))
		{
		    ok=true;
		    break;
		}
	    }
	    if (!ok)
		return false;
	}
	return true;
    }
    
    /**
     * Returns true if the group given as parameter is represented (see {@link #getRepresentedGroups(KernelAddress)}) by the current MultiGroup considering a MadKit kernel.
     * @param ka the considered Madkit kernel.
     * @param _g the group to research.
     * @return true if the given group is represented by the current MultiGroup.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    public boolean contains(KernelAddress ka, Group _g)
    {
	Group[] m_groups=getRepresentedGroups(ka);
	for (Group g : m_groups)
	{
	    if (g.equals(_g))
		return true;
	}
	return false;
    }
    

    /**
     * This function returns the represented groups by the current instance.
     * The returned list is a combination of the represented groups of several AbstractGroups (which can be MultiGroup), 
     * excepted forbidden groups added onto the current instance.   
     * 
     * @param ka the used the kernel address. 
     * @return the represented groups
     * @since MadKitGroupExtension 1.0
     * @see Group
     * @see MultiGroup
     */
    @Override
    synchronized public Group[] getRepresentedGroups(KernelAddress ka)
    {
	RepresentedGroupsDuplicated rdg=null;
	for (RepresentedGroupsDuplicated r : m_represented_groups_by_kernel_duplicated)
	{
	    if (r.m_kernel.equals(ka))
	    {
		rdg=r;
		break;
	    }
	}
	if (rdg==null)
	{
	    rdg=new RepresentedGroupsDuplicated(ka);
	    m_represented_groups_by_kernel_duplicated.add(rdg);
	}
	
	if (rdg.m_represented_groups_duplicated.get()!=null)
	{
	    for (AssociatedGroup ag : m_groups)
	    {
		if (ag.hasRepresentedGroupsChanged(ka))
		{
		    rdg.m_represented_groups_duplicated.set(null);
		    break;
		}
	    }
	}
	if (rdg.m_represented_groups_duplicated.get()==null)
	{
	    ArrayList<Group> l=new ArrayList<Group>(100);
	    
	    while (m_groups.size()>0 && m_groups.get(0).m_forbiden)
	    {
		m_groups.remove(0);
	    }
	    if (m_groups.size()>0)
	    {
		for (Group g : m_groups.get(0).getRepresentedGroups(ka))
		{
		    l.add(g);
		}
		for (int i=1;i<m_groups.size();i++)
		{
		    if (m_groups.get(i).m_forbiden)
		    {
			for (Group g : m_groups.get(i).getRepresentedGroups(ka))
			{
			    l.remove(g);
			}
		    }
		    else
		    {
			for (Group g : m_groups.get(i).getRepresentedGroups(ka))
			{
			    if (!l.contains(g))
				l.add(g);
			}
		    }
		}
		
	    }
	    rdg.m_represented_groups_duplicated.set((Group[])l.toArray());
	}
	
	return rdg.m_represented_groups_duplicated.get();
    }


    private class AssociatedGroup
    {
	public final AbstractGroup m_group;
	private Group[] m_represented_groups=null;
	public final boolean m_forbiden;
	
	public AssociatedGroup(AbstractGroup _a, boolean _forbiden)
	{
	    m_group=_a;
	    m_forbiden=_forbiden;
	}
	
	public boolean hasRepresentedGroupsChanged(KernelAddress ka)
	{
	    return m_group.getRepresentedGroups(ka)!=m_represented_groups;
	}
	public Group[] getRepresentedGroups(KernelAddress ka)
	{
	    return m_represented_groups=m_group.getRepresentedGroups(ka);
	}
	public boolean equals(Object o)
	{
	    if (o==this)
		return true;
	    if (o instanceof AssociatedGroup)
	    {
		return m_group.equals(((AssociatedGroup)o).m_group);
	    }
	    else if (o instanceof AbstractGroup)
	    {
		return ((AbstractGroup)o).equals(m_group);
	    }
	    else return true;
	}
    }


}
