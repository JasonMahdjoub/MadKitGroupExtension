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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import madkitgroupextension.simulation.probe.PropertyProbe;

/**
 * The class Probe of MadKitGroupExtension seems like the class Probe of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.     
 * 
 * This class defines a watcher's generic probe. 
 * A probe is configured according to a community, one or more groups and a role.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @author Jason Mahdjoub
 * @since MadKit 2.0
 * @since MadKitGroupExtension 1.0
 * @version 1.0
 * @see Watcher
 * @see PropertyProbe
 * @see AbstractGroup
 * @see Group
 * @see MultiGroup
 */
public class Probe<A extends madkit.kernel.AbstractAgent & MKGEAbstractAgent> implements GroupChangementNotifier
{
    protected final AbstractGroup m_group;
    protected final AtomicReference<Group[]> m_represented_groups=new AtomicReference<Group[]>();
    private final ArrayList<PersonalProbe> m_madkit_probes=new ArrayList<PersonalProbe>();
    protected ArrayList<A> m_agents=null;
    protected Watcher m_watcher=null;
    protected final String m_role;
    protected boolean m_is_changed=true;
    
	/**
	 * Builds a new Probe on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Watcher} 
	 * agent using the {@link Watcher#addProbe(Probe)} method.
	 * @param _group Activator.java
	 * @param _role the role name
	 * @see Watcher
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 */
    public Probe(AbstractGroup _group, String _role)
    {
	if (_group==null)
	    throw new NullPointerException("The _group argument is a null pointer.");
	if (_role==null)
	    throw new NullPointerException("The _role argument is a null pointer.");
	if (_group instanceof MultiGroup)
	    m_group=(AbstractGroup)(((MultiGroup)_group).clone());
	else
	    m_group=_group;
	m_role=_role;
    }
    
	/**
	 * Called when an agent has joined the corresponding group(s) and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization when an agent enters the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent joined several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param theAgent which has been added to this group/role
	 */
    protected void adding(A theAgent)
    {
	
    }
    
	/**
	 * Called when a list of agents have leaved the corresponding group(s) and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent joined several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param agents the list of agents which have been removed from this group/role
	 */
    protected void adding(List<A> agents)
    {
	for (A a : agents)
	    adding(a);
    }
    
    public synchronized void allAgentsLeaveRole()
    {
	for (PersonalProbe pp : m_madkit_probes)
	{
	    pp.allAgentsLeaveRole();
	}
    }

	/** 
	 * Returns a snapshot at moment t of the agents handling one of the groups represented by the AbstractGroup given in parameter in the constructor of this class. On each of these groups, the agent must have the given role into the same constructor.
	 * 
	 * Returned agents are not duplicated.
	 * 
	 * @return a list view (a snapshot at moment t) of the agents that handle the group/role couple (in proper sequence)
	 * @since MadKit 3.0
	 * @since MadKitGroupExtension 1.0
	 */
    public synchronized List<A> getCurrentAgentsList()
    {
	if (m_watcher==null)
	    return new ArrayList<A>();
	    
	if (m_is_changed)
	{
	    ArrayList<List<A>> l=new ArrayList<List<A>>(m_madkit_probes.size());
	    int size=0;
	    for (PersonalProbe pp : m_madkit_probes)
	    {
		List<A> l2=pp.getCurrentAgentsList();
		l.add(l2);
		size+=l2.size();
	    }
	    m_agents=new ArrayList<A>(size);
	    if (l.size()>0)
	    {
		m_agents.addAll(l.get(0));
		for (int i=1;i<l.size();i++)
		{
		    List<A> l2=l.get(i);
		    for (A a : l2)
		    {
			boolean found=false;
			for (A a2 : m_agents)
			{
			    if (a2==a)
			    {
				found=true;
				break;
			    }
			}
			if (!found)
			    m_agents.add(a);
		    }
		}
	    }
	    m_is_changed=false;
	}
	return m_agents;
	
    }
    
    /**
     * 
     * @return the group(s) handled by this probe
     * @see AbstractGroup
     * @see Group
     * @see MultiGroup
     */
    public final AbstractGroup getGroup()
    {
	return m_group;
    }
    /**
     * 
     * @return the role handled by this probe
     */
    public final String getRole()
    {
	return m_role;
    }
    
	/** 
	 * Returns a List over the agents which is shuffled
	 * @return a List which has been previously shuffled
	 * @since MadKit 3.0
	 * @since MadKitGroupExtension 1.0
	 */
    public List<A> getShuffledList()
    {
	@SuppressWarnings("unchecked")
	ArrayList<A> l=(ArrayList<A>)((ArrayList<A>)getCurrentAgentsList()).clone();
	
	Collections.shuffle(l);
	
	return l;
    }
    
	/**
	 * Called by the MadKit kernel when the Activator or Probe is
	 * first added. Default behavior is: <code>adding(getCurrentAgentsList());</code>
	 */
    public void initialize()
    {
	adding(getCurrentAgentsList());
    }
    
    
    public synchronized void killAgents() 
    {
	for (PersonalProbe pp : m_madkit_probes)
	    pp.killAgents();
    }
    
    @SuppressWarnings("unused")
    private final synchronized void associateWatcher(Watcher _w)
    {
	if (m_watcher!=null)
	{
	    throw new IllegalAccessError("The Probe "+this+" has been already added on a watcher. Impossible to add it on the watcher "+_w);
	}
	if (m_watcher==_w)
	{
	    throw new IllegalAccessError("The Probe "+this+" has been already added on the watcher "+_w+". Impossible to add it anther time");
	}

	m_watcher=_w;
	try
	{
	    m_add_group_changement_notifier_method.invoke(null, this);
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function addGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function addGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    e1.printStackTrace();
	    System.exit(-1);
	}
	potentialChangementInGroups();
    }
    
    protected synchronized void removeProbes()
    {
	for (PersonalProbe pp : m_madkit_probes)
	{
	    try
	    {
		m_remove_madkit_probe_method.invoke(m_watcher, pp);
	    }
	    catch (IllegalArgumentException e1)
	    {
		System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e1.printStackTrace();
		System.exit(-1);
	    }
	    catch (IllegalAccessException e1)
	    {
		System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e1.printStackTrace();
		System.exit(-1);
	    }
	    catch (InvocationTargetException e1)
	    {
		System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e1.printStackTrace();
		System.exit(-1);
	    }
	}
	m_represented_groups.set(null);
	m_madkit_probes.clear();
	m_agents=null;
	m_watcher=null;
	setChanged(true);
	try
	{
	    m_remove_group_changement_notifier_method.invoke(null, this);
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    System.err.println("Impossible to call the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
    }
    
	/**
	 * Called when an agent has leaved the corresponding group(s) and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some work when an agent leaves the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent leaved several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param theAgent which has been removed from this group/role
	 */
    protected void removing(A theAgent)
    {
	
    }
    
	/**
	 * Called when a list of agents have leaved the corresponding group(s) and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent leaved several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param agents the list of agents which have been removed from this group/role
	 */
    protected void removing(List<A> agents) 
    {
	for (A a : agents)
	    removing(a);
    }
    
	/** 
	 * Returns the number of the agents handling the group(s)/role couple
	 * @return the number of the agents that handle the group(s)/role couple
	 */
    public int size()
    {
	return getCurrentAgentsList().size();
    }
    
    @Override public String toString()
    {
	return "Probe with "+m_group+" and role "+m_role;
    }

    protected synchronized void setChanged(boolean _is_changed)
    {
	m_is_changed=_is_changed;
    }
    
    /**
     * This function is reserved to the MadKitGroupExtension kernel. The user do not use this function.
     */
    @Override
    public synchronized void potentialChangementInGroups()
    {
	if (m_watcher==null)
	    return;
	Group[] groups=m_group.getRepresentedGroups(m_watcher.getKernelAddress());
	if (m_represented_groups.get()!=groups)
	{
		if (m_represented_groups.get()==null)
		{
		    for (Group g : groups)
		    {
			PersonalProbe pp=new PersonalProbe(g, m_role);
			m_madkit_probes.add(pp);
			try
			{
			    m_add_madkit_probe_method.invoke(m_watcher, pp);
			}
			catch (IllegalArgumentException e1)
			{
			    System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			    e1.printStackTrace();
			    System.exit(-1);
			}
			catch (IllegalAccessException e1)
			{
			    System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			    e1.printStackTrace();
			    System.exit(-1);
			}
			catch (InvocationTargetException e1)
			{
			    System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			    e1.printStackTrace();
			    System.exit(-1);
			}
		    }
		}
		else
		{
		    //detecting new groups
		    for (Group g : groups)
		    {
			boolean found=false;
			for (PersonalProbe pp : m_madkit_probes)
			{
			    if (pp.equals(g))
			    {
				found=true;
				break;
			    }
			}
			if (!found)
			{
			    PersonalProbe pp=new PersonalProbe(g, m_role);
			    m_madkit_probes.add(pp);
			    try
			    {
				m_add_madkit_probe_method.invoke(m_watcher, pp);
			    }
			    catch (IllegalArgumentException e1)
			    {
				System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			    catch (IllegalAccessException e1)
			    {
				System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			    catch (InvocationTargetException e1)
			    {
				System.err.println("Impossible to call the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			 }
		    }
		    
		    //detecting removed groups
		    Iterator<PersonalProbe> it=m_madkit_probes.iterator();
		    while (it.hasNext())
		    {
			PersonalProbe pp=it.next();
			boolean found=false;
			for (Group g : groups)
			{
			    if (pp.equals(g))
			    {
				found=true;
				break;
			    }
			}
			if (!found)
			{
			    it.remove();
			    try
			    {
				m_remove_madkit_probe_method.invoke(m_watcher, pp);
			    }
			    catch (IllegalArgumentException e1)
			    {
				System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			    catch (IllegalAccessException e1)
			    {
				System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			    catch (InvocationTargetException e1)
			    {
				System.err.println("Impossible to call the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
				e1.printStackTrace();
				System.exit(-1);
			    }
			}
		    }
		}
		m_represented_groups.set(groups);
		setChanged(true);
	}

	
    }
    
    private class PersonalProbe extends madkit.kernel.Probe<A>
    {
	private Group m_group;
	
	public PersonalProbe(Group _group, String _role)
	{
	    super(_group.getCommunity(), _group.getPath(), _role);
	    m_group=_group;
	}
	
	@Override public void adding(A theAgent)
	{
	    Probe.this.setChanged(true);
	    Probe.this.adding(theAgent);
	}
	@Override public void adding(List<A> agents)
	{
	    Probe.this.setChanged(true);
	    Probe.this.adding(agents);
	}
	
	@Override public void removing(A theAgent)
	{
	    Probe.this.setChanged(true);
	    Probe.this.removing(theAgent);
	}
	
	@Override public void removing(List<A> agents)
	{
	    Probe.this.setChanged(true);
	    Probe.this.removing(agents);
	}
	
	@Override public void initialize()
	{
	    Probe.this.setChanged(true);
	}
	
	public boolean equals(Group _group)
	{
	    return m_group.equals(_group);
	}
	
    }

    private static Method m_add_madkit_probe_method=null;
    private static Method m_remove_madkit_probe_method=null;
    private static Method m_add_group_changement_notifier_method=null;
    private static Method m_remove_group_changement_notifier_method=null;
    static
    {
	try
	{
	    m_add_madkit_probe_method=Watcher.class.getDeclaredMethod("addPrivateProbe", madkit.kernel.Probe.class);
	    m_add_madkit_probe_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function addPrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_remove_madkit_probe_method=Watcher.class.getDeclaredMethod("removePrivateProbe", madkit.kernel.Probe.class);
	    m_remove_madkit_probe_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function removePrivateProbe of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_add_group_changement_notifier_method=Group.class.getDeclaredMethod("addGroupChangementNotifier", GroupChangementNotifier.class);
	    m_add_group_changement_notifier_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function addGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function addGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_remove_group_changement_notifier_method=Group.class.getDeclaredMethod("removeGroupChangementNotifier", GroupChangementNotifier.class);
	    m_remove_group_changement_notifier_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	
    }
}
