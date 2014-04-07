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
import java.util.HashSet;
import java.util.Iterator;
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
    
    final ArrayList<AssociatedGroup> m_groups;
    
    
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
	m_groups=new ArrayList<AssociatedGroup>();
	
	for (AbstractGroup g : _groups)
	{
	    addGroup(g);
	}
    }
    private MultiGroup(ArrayList<AssociatedGroup> _groups)
    {
	m_groups=_groups;
    }
    
    @Override synchronized public MultiGroup clone()
    {
	synchronized(this)
	{
	    ArrayList<AssociatedGroup> groups=new ArrayList<AssociatedGroup>(m_groups.size());
	    for (int i=0;i<m_groups.size();i++)
	    {
		AssociatedGroup ag = m_groups.get(i);
		if (ag.m_group instanceof MultiGroup)
		{
		    groups.add(new AssociatedGroup((AbstractGroup)(((MultiGroup)ag.m_group).clone()), ag.m_forbiden));
		}
		else
		    groups.add(new AssociatedGroup(ag.m_group, ag.m_forbiden));
	    }
	    return new MultiGroup(groups);
	}
    }
    @Override public String toString()
    {
	synchronized(this)
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
    }
    
    enum CONTAINS
    {
	CONTAINS_ON_FORBIDEN,
	CONTAINS_ON_AUTHORIZED,
	NOT_CONTAINS
    }
    
    CONTAINS contains(AbstractGroup _group)
    {
	for (AssociatedGroup ag : m_groups)
	{
	    if (ag.equals(_group))
	    {
		if (ag.m_forbiden)
		    return CONTAINS.CONTAINS_ON_FORBIDEN;
		else 
		    return CONTAINS.CONTAINS_ON_AUTHORIZED;
	    }
	}
	return CONTAINS.NOT_CONTAINS;
    }
    
    /*int containsReference(AbstractGroup _group)
    {
	for (AssociatedGroup ag : m_groups)
	{
	    if (ag.m_group==_group)
		return 1;
	    else if (ag.m_group instanceof MultiGroup)
	    {
		int v=((MultiGroup)ag.m_group).containsReference(_group);
		if (v>0)
		    return v+1;
	    }
	}
	return 0;
	
    }*/
    
    /**
     * Add a new group ({@link Group} or {@link MultiGroup}) which will be combined with the current MultiGroup.
     * @param _g the abstract group to add.
     * @return false if the group has been already added. 
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    public boolean addGroup(AbstractGroup _g)
    {
	synchronized(this)
	{
	    if (_g.equals(AbstractGroup.getUniverse()))
	    {
		Iterator<AssociatedGroup> it=m_groups.iterator();
		while (it.hasNext())
		{
		    AssociatedGroup ag=it.next();
		    if (!ag.m_forbiden)
		    {
			if (ag.m_group.equals(AbstractGroup.getUniverse()))
			{
			    return false;
			}
			else
			{
			    it.remove();
			}
		    }
		}
		m_groups.add(new AssociatedGroup(AbstractGroup.getUniverse(), false));
		for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
		    rgd.m_represented_groups_duplicated.set(null);
		return true;
	    }
	    else
	    {
		CONTAINS c=contains(_g);
		if (c.equals(CONTAINS.NOT_CONTAINS))
		{
		    m_groups.add(new AssociatedGroup(_g.clone(), false));
		    for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
			rgd.m_represented_groups_duplicated.set(null);
		    return true;
		}
		return false;
	    }
	}
    }
    /**
     * Add a new forbidden group ({@link Group} or {@link MultiGroup}) which will forbid the representation of itself onto the current MultiGroup (see the class description).
     * @param _g the group to forbid.
     * @return false if the group has been already added.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    public boolean addForbidenGroup(AbstractGroup _g)
    {
	synchronized(this)
	{
        	if (_g.equals(AbstractGroup.getUniverse()))
        	{
        	    for (AssociatedGroup ag : m_groups)
        	    {
        		if (ag.m_forbiden)
        		{
        		    if (ag.m_group.equals(AbstractGroup.getUniverse()))
        		    {
        			return false;
        		    }
        		}
        	    }
        	    m_groups.clear();
        	    m_groups.add(new AssociatedGroup(AbstractGroup.getUniverse(), true));
        	    for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
        		rgd.m_represented_groups_duplicated.set(null);
        	    return true;
        	}
        	else
        	{
        	    CONTAINS c=contains(_g);
        	    if (c.equals(CONTAINS.CONTAINS_ON_AUTHORIZED))
        	    {
        		removeGroup(_g);
        	    }
        	    if (!c.equals(CONTAINS.CONTAINS_ON_FORBIDEN))
        	    {
        		m_groups.add(new AssociatedGroup(_g.clone(), true));
        		for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
        		    rgd.m_represented_groups_duplicated.set(null);
        		return true;
        	    }
        	    return false;
        	}
	}
    }
    /**
     * Remove the group given as parameter from the current MultiGroup. This group can be forbidden or not.
     * @param _g the group to remove.
     * @return false if group was not found.
     * @see Group
     * @since MadKitGroupExtension 1.0
     */
    boolean removeGroup(AbstractGroup _g)
    {
	Iterator<AssociatedGroup> it=m_groups.iterator();
	while (it.hasNext())
	{
	    AssociatedGroup ag=it.next();
	    if (ag.equals(_g))
	    {
		it.remove();
		for (RepresentedGroupsDuplicated rgd : m_represented_groups_by_kernel_duplicated)
		    rgd.m_represented_groups_duplicated.set(null);
		return true;
	    }
	}
	return false;	    
    }
    
    /**
     * This method is equivalent to this.include(_ag) && _ag.include(this)
     * @see #getRepresentedGroups(KernelAddress)
     * @since MadKitGroupExtension 1.5.1 
     */
    @Override public boolean equals(Object o)
    {
	if (o==null)
	    return false;
	if (o instanceof AbstractGroup)
	{
	    return this.equals((AbstractGroup)o);
	}
	else
	    return false;
    }

    /**
     * This method is equivalent to this.include(_ag) && _ag.include(this)
     * @see #getRepresentedGroups(KernelAddress)
     * @since MadKitGroupExtension 1.5.1 
     */
    public boolean equals(AbstractGroup _ag)
    {
	if (_ag==this)
	    return true;
	if (_ag instanceof Group.Universe)
	{
	    synchronized(this)
	    {
		boolean universe=false; 
		for (AssociatedGroup ag : m_groups)
		{
		    if (ag.m_forbiden)
		    {
			if (!ag.m_group.isEmpty())
			    return false;
		    }
		    else if (ag.m_group.equals(_ag))
			universe=true;
		    
		}
		return universe;
	    }
	}
	else
	    return this.includes(_ag) && _ag.includes(this);
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
	if (ka==null)
	    throw new NullPointerException("ka");
	
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
	    synchronized(this)
	    {
		HashSet<Group> l=new HashSet<>();
		ArrayList<Group> f=new ArrayList<Group>(10);
		ArrayList<Group> l2=null;
	    
		if (m_groups.size()>0)
		{
		    for (AssociatedGroup ag : m_groups)
		    {
			if (ag.m_forbiden)
			{
			    for (Group g : ag.m_group.getRepresentedGroups(ka))
				f.add(g);
			}
			else
			{
			    for (Group g : ag.m_group.getRepresentedGroups(ka))
				l.add(g);
			}
		    }
		
		    l2=new ArrayList<>(l.size());
		    for (Group g : l)
		    {
			boolean add=true;
			for (Group gf : f)
			{
			    if (g.equals(gf))
			    {
				add=false;
				break;
			    }
			}
			if (add)
			    l2.add(g);
		    }
		}
		else
		    l2=new ArrayList<>(0);
		Group res[]=new Group[l2.size()];
		l2.toArray(res);
		rdg.m_represented_groups_duplicated.set(res);
	    }
	}
	
	return rdg.m_represented_groups_duplicated.get();
    }


    class AssociatedGroup
    {
	final AbstractGroup m_group;
	private Group[] m_represented_groups=null;
	final boolean m_forbiden;
	
	AssociatedGroup(AbstractGroup _a, boolean _forbiden)
	{
	    m_group=_a;
	    m_forbiden=_forbiden;
	}
	
	boolean hasRepresentedGroupsChanged(KernelAddress ka)
	{
	    return m_group.getRepresentedGroups(ka)!=m_represented_groups;
	}
	Group[] getRepresentedGroups(KernelAddress ka)
	{
	    return m_represented_groups=m_group.getRepresentedGroups(ka);
	}
	@Override public boolean equals(Object o)
	{
	    if (o==this)
		return true;
	    if (o instanceof AssociatedGroup)
	    {
		return m_group.equals(((AssociatedGroup)o).m_group);
	    }
	    else if (o instanceof AbstractGroup)
	    {
		if (o instanceof Group)
		    return ((Group)o).equals(m_group);
		else if (m_group instanceof Group)
		    return false;
		else
		    return o==m_group;
	    }
	    else return true;
	}
    }
}
