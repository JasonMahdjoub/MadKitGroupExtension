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

import madkit.simulation.SimulationException;
import madkitgroupextension.kernel.AbstractGroup;
import madkitgroupextension.kernel.Group;
import madkitgroupextension.kernel.GroupChangementNotifier;
import madkitgroupextension.kernel.MKGEAbstractAgent;
import madkitgroupextension.kernel.Probe;
import madkitgroupextension.kernel.Watcher;

/**
 * The class PropertyProbe of MadKitGroupExtension is similar to the class PropertyProbe of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.
 *      
 * This probe inspects fields of type T on agents of type A and its subclasses.
 * 
 * @param <A> the group's agent most common class type (i.e. AbstractAgent)
 * @param <P> the type of the property, i.e. Integer (this works if the field is an int, i.e. a primitive type)
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadKit 5.0.0.13
 * @since MadKitGroupExtension 1.0
 * @version 1.0
 * 
 */
public class PropertyProbe<A extends madkit.kernel.AbstractAgent & MKGEAbstractAgent, P>  extends Probe<A>
{
    private final String fieldName;
    private final ArrayList<PersonalPropertyProbe> m_madkit_property_probes=new ArrayList<PersonalPropertyProbe>();
    /*private boolean m_is_agent_properties_changed=true;
    private boolean m_is_properties_changed=true;
    
    private HashMap<A,P> m_agent_properties=null;
    private ArrayList<P> m_properties=null;*/
    
    public PropertyProbe(AbstractGroup _a, String _role, String _fieldName)
    {
	super(_a, _role);
	fieldName=_fieldName;
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
	for (PersonalPropertyProbe ppp : m_madkit_property_probes)
	{
	    ppp.allAgentsLeaveRole();
	}
    }
    /*public synchronized Map<A,P> getAgentToPropertyMap()
    {
	if (m_is_agent_properties_changed)
	{
	    ArrayList<Map<A,P>> l=new ArrayList<Map<A,P>>(m_madkit_property_probes.size());
	    int total=0;
	    for (PersonalPropertyProbe ppp : m_madkit_property_probes)
	    {
		Map<A,P> m=ppp.getAgentToPropertyMap();
		l.add(m);
		total+=m.size();
	    }
	
	    HashMap<A,P> res=new HashMap<A,P>(total);
	
	    for (Map<A,P> m : l)
	    {
		res.putAll(m);
	    }
	    m_agent_properties=res;
	    m_is_agent_properties_changed=false;
	}
	return m_agent_properties;
    }*/
    /*public synchronized Collection<P> getAllProperties()
    {
	if (m_is_properties_changed)
	{
	    ArrayList<Collection<P>> l=new ArrayList<Collection<P>>(m_madkit_property_probes.size());
	    int total=0;
	    for (PersonalPropertyProbe ppp : m_madkit_property_probes)
	    {
		Collection<P> c=ppp.getAllProperties();
		l.add(c);
		total+=c.size();
	    }
	    
	    m_properties=new ArrayList<P>(total);
	    for (Collection<P> c : l)
	    {
		m_properties.addAll(c);
	    }
	    m_is_properties_changed=false;
	}
	return m_properties;
    }*/
	/** 
	 * Returns a snapshot at moment t of the agents handling the group/role couple
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
	    ArrayList<List<A>> l=new ArrayList<List<A>>(m_madkit_property_probes.size());
	    int size=0;
	    for (PersonalPropertyProbe ppp : m_madkit_property_probes)
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
	 * @param theAgent the agent
	 * @param value the value to set
	 */
    public void setPropertyValue(A theAgent,  P value)
    {
	for (PersonalPropertyProbe ppp : m_madkit_property_probes)
	    ppp.setPropertyValue(theAgent, value);
    }
	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @param theAgent the agent to probe
	 * @return the actual value of the agent's field 
	 */
    public P getPropertyValue(A theAgent) 
    {
	for (PersonalPropertyProbe ppp : m_madkit_property_probes)
	{
	    P p=ppp.getPropertyValue(theAgent);
	    if (p!=null)
		return p;
	}
	return null;
    }
    @Override public synchronized void killAgents() 
    {
	for (PersonalPropertyProbe ppp : m_madkit_property_probes)
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
			PersonalPropertyProbe ppp=new PersonalPropertyProbe(g, m_role, fieldName);
			m_madkit_property_probes.add(ppp);
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
			for (PersonalPropertyProbe ppp : m_madkit_property_probes)
			{
			    if (ppp.equals(g))
			    {
				found=true;
				break;
			    }
			}
			if (!found)
			{
			    PersonalPropertyProbe ppp=new PersonalPropertyProbe(g, m_role, fieldName);
			    m_madkit_property_probes.add(ppp);
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
		    Iterator<PersonalPropertyProbe> it=m_madkit_property_probes.iterator();
		    while (it.hasNext())
		    {
			PersonalPropertyProbe ppp=it.next();
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
	for (PersonalPropertyProbe ppp : m_madkit_property_probes)
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
	m_madkit_property_probes.clear();
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
	return "PropertyProbe with "+getGroup()+" and role "+getRole()+" and property "+fieldName;
    }

    @Override protected synchronized void setChanged(boolean _is_changed)
    {
	m_is_changed=_is_changed;
	//m_is_agent_properties_changed=_is_changed;
	//m_is_properties_changed=_is_changed;
    }
    
	/**
	 * Returns the average value for the property over all the agents. The property
	 * must be numerical for this to work.
	 * 
	 * @return the average value for this property
	 */
	public double getAverageValue() {
		double total = 0;
		for (final A a : getCurrentAgentsList()) {
			try {
				total += ((Number) getPropertyValue(a)).doubleValue();
			} catch (ClassCastException e) {
				throw new SimulationException(toString() + " on " + a, e);
			}
		}
		return total / size();
	}
    

    private class PersonalPropertyProbe extends madkit.simulation.probe.PropertyProbe<A, P>
    {
	private Group m_group;
	
	public PersonalPropertyProbe(Group _group, String _role, String _property)
	{
	    super(_group.getCommunity(), _group.getPath(), _role, _property);
	    m_group=_group;
	}
	
	@Override public void adding(A theAgent)
	{
	    PropertyProbe.this.setChanged(true);
	    PropertyProbe.this.adding(theAgent);
	}
	@Override public void adding(List<A> agents)
	{
	    PropertyProbe.this.setChanged(true);
	    PropertyProbe.this.adding(agents);
	}
	
	@Override public void removing(A theAgent)
	{
	    PropertyProbe.this.setChanged(true);
	    PropertyProbe.this.removing(theAgent);
	}
	
	@Override public void removing(List<A> agents)
	{
	    PropertyProbe.this.setChanged(true);
	    PropertyProbe.this.removing(agents);
	}
	
	@Override public void initialize()
	{
	    PropertyProbe.this.setChanged(true);
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
