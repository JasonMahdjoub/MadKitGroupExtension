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

package madkitgroupextension.simulation.probe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import madkitgroupextension.kernel.AbstractGroup;
import madkitgroupextension.kernel.Group;
import madkitgroupextension.kernel.GroupChangementNotifier;
import madkitgroupextension.kernel.MKGEAbstractAgent;
import madkitgroupextension.kernel.Probe;
import madkitgroupextension.kernel.Watcher;

/**
 * The class SingleAgentProbe of MadKitGroupExtension is similar to the class SingleAgentProbe of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.
 * 
 * This probe inspects fields of type T on only one agent of type A and its subclasses.
 * This is designed for probing one single agent, i.e. methods are designed  
 * and optimized in this respect.
 * 
 * @param <A> the most common class type expected in this group (e.g. AbstractAgent)
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int, i.e. a primitive type)
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.18
 * @since MadKitGroupExtension 1.0
 * @version 1.0
 * 
 */
public class SingleAgentProbe<A extends madkit.kernel.AbstractAgent & MKGEAbstractAgent, T> extends Probe<A>
{
    private final String m_field_name;
    private final ArrayList<PersonalSingleAgentProbe> m_madkit_field_probes=new ArrayList<PersonalSingleAgentProbe>();

    public SingleAgentProbe(AbstractGroup _group, String _role, String _field_name)
    {
	super(_group, _role);
	m_field_name=_field_name;
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
    	@Override protected void adding(A theAgent)
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
    	@Override protected void adding(List<A> agents)
    	{
    	    for (A a : agents)
    		adding(a);
    	}

    	@Override public synchronized void allAgentsLeaveRole()
    	{
    	    for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
    	    {
    		ppp.allAgentsLeaveRole();
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
@Override public synchronized List<A> getCurrentAgentsList()
{
	if (m_watcher==null)
	    return new ArrayList<A>();
	    
	if (m_is_changed)
	{
	    ArrayList<List<A>> l=new ArrayList<List<A>>(m_madkit_field_probes.size());
	    int size=0;
	    for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
	    {
		List<A> l2=ppp.getCurrentAgentsList();
		l.add(l2);
		size+=l2.size();
	    }
	
	    m_agents=new ArrayList<A>(size);
	    for (List<A> l2 : l )
	    {
		m_agents.addAll(l2);
	    }
	    m_is_changed=false;
	}
	return m_agents;
	
}
	/**
	 * Should be used to work with primitive types
	 * or fields which are initially <code>null</code>
	 * @param value
	 */
public void setPropertyValue(T value)
{
	for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
	    ppp.setPropertyValue(value);
}
	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @return the actual value of the agent's field 
	 */
public T getPropertyValue() 
{
	for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
	{
	    T p=ppp.getPropertyValue();
	    if (p!=null)
		return p;
	}
	return null;
}
@Override public synchronized void killAgents() 
{
	for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
	    ppp.killAgents();
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
			PersonalSingleAgentProbe ppp=new PersonalSingleAgentProbe(g, m_role, m_field_name);
			m_madkit_field_probes.add(ppp);
			try
			{
			    m_add_madkit_probe_method.invoke(m_watcher, ppp);
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
			for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
			{
			    if (ppp.equals(g))
			    {
				found=true;
				break;
			    }
			}
			if (!found)
			{
			    PersonalSingleAgentProbe ppp=new PersonalSingleAgentProbe(g, m_role, m_field_name);
			    m_madkit_field_probes.add(ppp);
			    try
			    {
				m_add_madkit_probe_method.invoke(m_watcher, ppp);
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
				e1.printStackTrace();
				System.exit(-1);
			    }
			}
		    }
		    
		    //detecting removed groups
		    Iterator<PersonalSingleAgentProbe> it=m_madkit_field_probes.iterator();
		    while (it.hasNext())
		    {
			PersonalSingleAgentProbe ppp=it.next();
			boolean found=false;
			for (Group g : groups)
			{
			    if (ppp.equals(g))
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
				m_remove_madkit_probe_method.invoke(m_watcher, ppp);
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
    
@Override protected synchronized void removeProbes()
{
	super.removeProbes();
	for (PersonalSingleAgentProbe ppp : m_madkit_field_probes)
	{
	    try
	    {
		m_remove_madkit_probe_method.invoke(m_watcher, ppp);
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
		e1.printStackTrace();
		System.exit(-1);
	    }
	}
	m_madkit_field_probes.clear();
	//m_properties=null;
	//m_agent_properties=null;
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
@Override protected void removing(A theAgent)
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
@Override protected void removing(List<A> agents) 
{
    for (A a : agents)
	removing(a);
	
}

@Override public String toString()
{
	return "SingleAgentProbe with "+getGroup()+" and role "+getRole()+" and field name"+m_field_name;
}

@Override protected synchronized void setChanged(boolean _is_changed)
{
	m_is_changed=_is_changed;
	//m_is_agent_properties_changed=_is_changed;
	//m_is_properties_changed=_is_changed;
}

    private class PersonalSingleAgentProbe extends madkit.simulation.probe.SingleAgentProbe<A, T>
    {
	private Group m_group;
	
	public PersonalSingleAgentProbe(Group _group, String _role, String _property)
	{
	    super(_group.getCommunity(), _group.getPath(), _role, _property);
	    m_group=_group;
	}
	
	@Override public void adding(A theAgent)
	{
	    SingleAgentProbe.this.setChanged(true);
	    SingleAgentProbe.this.adding(theAgent);
	}
	@Override public void adding(List<A> agents)
	{
	    SingleAgentProbe.this.setChanged(true);
	    SingleAgentProbe.this.adding(agents);
	}
	
	@Override public void removing(A theAgent)
	{
	    SingleAgentProbe.this.setChanged(true);
	    SingleAgentProbe.this.removing(theAgent);
	}
	
	@Override public void removing(List<A> agents)
	{
	    SingleAgentProbe.this.setChanged(true);
	    SingleAgentProbe.this.removing(agents);
	}
	
	@Override public void initialize()
	{
	    SingleAgentProbe.this.setChanged(true);
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
