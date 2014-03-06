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
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import madkitgroupextension.simulation.activator.GenericBehaviorActivator;

/**
 * The class Activator of MadKitGroupExtension has the same role of the class Activator of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.     
 * 
 * This class defines a tool for scheduling mechanism.
 * An activator is configured according to a community, a group and a role.
 * It could be used to activate a group of agents on a particular behavior (a method of the agent's class)
 * Subclasses should override {@link #execute(List, Object...)} for defining how 
 * a sequential execution of the agents take place. By default these methods
 * do nothing. To set the mode that will be used by the scheduler, 
 * to use multicore execution on activators having internal concurrent mechanism.
 * The multicore is set to <code>false</code> by default.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub 
 * @since MadKit 2.0
 * @since MadKitGroupExtension 1.0
 * @see Scheduler
 * @see GenericBehaviorActivator
 * @see AbstractGroup
 * @see Group
 * @see MultiGroup 
 * @version 1.1
 * 
 */
public abstract class Activator<A extends madkit.kernel.AbstractAgent & MKGEAbstractAgent> implements GroupChangementNotifier
{
    private AbstractGroup m_group;
    private String m_role;
    private final AtomicReference<Group[]> m_represented_groups=new AtomicReference<>();
    private final AtomicReference<Group[]> m_represented_groups_temp=new AtomicReference<>();
    private Scheduler m_scheduler;
    protected ArrayList<madkit.kernel.Activator<A>> m_activators=new ArrayList<madkit.kernel.Activator<A>>();
    protected ArrayList<madkit.kernel.Activator<A>> m_activators_to_add=new ArrayList<madkit.kernel.Activator<A>>();
    protected ArrayList<madkit.kernel.Activator<A>> m_activators_to_remove=new ArrayList<madkit.kernel.Activator<A>>();
    private final madkit.kernel.Activator<A> m_executor;
    private boolean m_list_changed=true;
    private ArrayList<A> m_agents=null;
    //private AtomicInteger m_nb_used_cores=new AtomicInteger(1); 
    /*protected Method m_personal_execute_method=null;
    protected Method m_personal_multicoreExecute_method=null;*/
    
	/**
	 * Builds a new Activator on the given CGR location of the
	 * artificial society with multicore mode set to <code>false</code>.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @see Scheduler
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    public Activator(AbstractGroup _groups, String _role)
    {
	this(_groups, _role, new Object[0]);
    }
    
    protected Activator(AbstractGroup _groups, String _role, Object ...args)
    {
	if (_groups==null)
	    throw new NullPointerException("The _group argument is a null pointer.");
	if (_role==null)
	    throw new NullPointerException("The _role argument is a null pointer.");
	if (_groups instanceof MultiGroup)
	    m_group=(AbstractGroup)(((MultiGroup)_groups).clone());
	else
	    m_group=_groups;

	m_role=_role;
	m_executor=getPersonalActivatorInstance(args);
	//updateMethods();
    }

    
    
    /**
     * this method is reserved to internal processes of MadKitGroupExtension.
     */
    protected madkit.kernel.Activator<A> getPersonalActivatorInstance(Object ... args)
    {
	return new PersonalActivatorExecutor();
    }
    
    /*protected void updateMethods()
    {
	try
	{
	    m_personal_execute_method=PersonalActivator.class.getDeclaredMethod("personalExecute");
	    m_personal_execute_method.setAccessible(true);
	}
	catch (Exception e)
	{
	    System.err.println("Impossible to access to the function personalExecute of the class Activator<A>.PersonalActivator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_personal_multicoreExecute_method=PersonalActivator.class.getDeclaredMethod("personalMulticoreExecute");
	    m_personal_multicoreExecute_method.setAccessible(true);
	}
	catch (Exception e)
	{
	    System.err.println("Impossible to access to the function personalMulticoreExecute of the class Activator<A>.PersonalActivator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e.printStackTrace();
	    System.exit(-1);
	}
    }*/
	/**
	 * Called when an agent has joined the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization when an agent enters the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent joined several groups, each represented by the given AbstractGroup into the constructor of this class 
	 * 
	 * @param _agent which has been added to this group/role
	 * @since MadKitGroupExtension 1.0
	 */
    protected void adding(A _agent)
    {
	
    }
	/**
	 * Called when a list of agents have joined the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent joined several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param _agents the list of agents which have been added to this group/role at once.
	 * @since MadKitGroupExtension 1.0
	 */
    protected void adding(List<A> _agents)
    {
	for (A a : _agents)
	    adding(a);
    }
	/**
	 * Called when an agent has leaved the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some work when an agent leaves the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent leaved several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param _agent which has been removed from this group/role
	 * @since MadKitGroupExtension 1.0
	 */
    protected void removing(A _agent)
    {
	
    }
	/**
	 * Called when a list of agents have leaved the corresponding group and role.
	 * This method is protected because it is automatically called
	 * by the MadKit kernel. Override this method when you want
	 * to do some initialization on the agents that enter the group/role.
	 * 
	 * This function can be called several times for the same agent if the agent leaved several groups, each represented by the given AbstractGroup into the constructor of this class
	 * 
	 * @param _agents the list of agents which have been removed from this group/role
	 * @since MadKitGroupExtension 1.0
	 */
    protected void removing(List<A> _agents)
    {
	for (A a : _agents)
	    removing(a);
    }
    public synchronized void allAgentsLeaveRole() 
    {
	for (madkit.kernel.Activator<A> pa : m_activators)
	    pa.allAgentsLeaveRole();
    }
    
    
	/**
	 * This should define what has to be done on the agents
	 * for a simulation step. By default, this calls is automatically made
	 * using a list containing all the agents for this CGR, 
	 * i.e. {@link #getCurrentAgentsList()} is used by default.
	 * 
	 * When the multicore mode is on, the list is only a portion and
	 * this method will automatically be distributed over several threads.
	 * So, one has to take care about how the activator's fields are used
	 * here to avoid a {@link ConcurrentModificationException} for instance.
	 * 
	 * @param _agentsList
	 * @param args arguments that could be used by the scheduler 
	 * to pass information to this activator for an activation
	 * @since 1.1
	 */
    public abstract void execute(List<A> _agentsList, Object ...args);

    /**
	 * @return <code>true</code> if the multi core mode is on. I.e. 
	 * {@link #nbOfParallelTasks()} > 1.
	 * This method could be used by the default behavior of scheduler agents as 
	 * they test in which mode each activator has to be used.
	 * @since MadKitGroupExtension 1.0
	 */
    public final boolean isMulticoreModeOn() 
    {
	return m_executor.isMulticoreModeOn();
    }
    
	/**
	 * Returns the number tasks that will
	 * be created by this activator.
	 * @return the number of tasks that will be created.
	 * @since MadKitGroupExtension 1.0
	 */
    public final int nbOfParallelTasks()
    {
	return m_executor.nbOfParallelTasks();
    }
    
	/**
	 * Sets the number of core which will be used. If set to a number greater
	 * than 1, the scheduler will automatically separate agent calls 
	 * in different threads.
	 * @param nbOfsimultaneousTasks the number of simultaneous tasks
	 * that this activator will use to make a step. Default is 1 upon
	 * creation, so that 
	 * {@link #isMulticoreModeOn()} returns <code>false</code>.
	 * @since MadKitGroupExtension 1.0
	 */
    public final synchronized void useMulticore(int nbOfsimultaneousTasks)
    {
	/*for (madkit.kernel.Activator<A> pa : m_activators)
	{
	    pa.useMulticore(nbOfsimultaneousTasks);
	}
	for (madkit.kernel.Activator<A> pa : m_activators_to_add)
	{
	    pa.useMulticore(nbOfsimultaneousTasks);
	}*/
	//m_nb_used_cores.set(nbOfsimultaneousTasks);
	m_executor.useMulticore(nbOfsimultaneousTasks);
	
    } 
    
    /**
     * 
     * return the abstract group representing one or more groups. 
     * 		These groups are used by the activator to get a set of agents
     * 		which are part of one (or more) of these groups.
     * @return the group(s)
     * @see AbstractGroup
     * @see Group
     * @see MultiGroup
     * @since MadKitGroupExtension 1.0
     * 
     */
    public AbstractGroup getGroup()
    {
	return m_group;
    }
    
    /**
     * return the role used by the activator to select agents which use this role.
     * @return the used role
     * @see Role
     * @since MadKitGroupExtension 1.0
     */
    public String getRole()
    {
	return m_role;
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
	if (m_scheduler==null)
	    return new ArrayList<A>();
	
	if (m_list_changed)
	{
	    ArrayList<List<A>> l=new ArrayList<List<A>>(m_activators.size());
	    int size=0;
	    for (madkit.kernel.Activator<A> pp : m_activators)
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
	    m_list_changed=false;
	}
	return m_agents;
    }
	/** 
	 * Returns a ListIterator over the agents which is shuffled
	 * @return a ListIterator which has been previously shuffled
	 * @since MadKit 3.0
	 * @since MadKitGroupExtension 1.0
	 */
    public List<A> getShuffledList()
    {
	@SuppressWarnings("unchecked")
	List<A> l=(ArrayList<A>)((ArrayList<A>)getCurrentAgentsList()).clone();
	Collections.shuffle(l);
	return l;
    }
    
	/**
	 * Called by the MadKit kernel when the Activator or Probe is
	 * first added. Default behavior is: <code>adding(getCurrentAgentsList());</code>
	 * @since MadKitGroupExtension 1.0
	 */
    public void initialize()
    {
	
    }
    
    public void killAgents()
    {
	for (madkit.kernel.Activator<A> pa : m_activators)
	    pa.killAgents();
    }
	/** 
	 * Returns the number of the agents handling the groups/role couples
	 * @return the number of the agents that handle the groups/role couples
	 * @since MadKitGroupExtension 1.0
	 */
    public int size()
    {
	return getCurrentAgentsList().size();
    }
    
    
    @Override public String toString()
    {
	return "Activator with "+m_group+" and role "+m_role;
    }

    @SuppressWarnings("unused")
    private final synchronized void associateScheduler(Scheduler _s)
    {
	if (m_scheduler!=null)
	{
	    throw new IllegalAccessError("The Activator "+this+" has been already added on a Scheduler. Impossible to add it on the Scheduler "+_s);
	}
	if (m_scheduler==_s)
	{
	    throw new IllegalAccessError("The Activator "+this+" has been already added on the Scheduler "+_s+". Impossible to add it anther time");
	}
	
	m_scheduler=_s;
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
	    System.err.println("Impossible to call the function addGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_add_madkit_activator_method.invoke(m_scheduler, m_executor);
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function addPersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function addPersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    System.err.println("Impossible to call the function addPersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	potentialChangementInGroups();
    }
    protected synchronized void removeActivators()
    {
	try
	{
	    m_remove_madkit_activator_method.invoke(m_scheduler, m_executor);
	}
	catch (Exception e)
	{
	    System.err.println("Impossible to call the function removePersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	for (madkit.kernel.Activator<A> a : m_activators)
	{
	    try
	    {
		m_remove_madkit_activator_method.invoke(m_scheduler, a);
	    }
	    catch (Exception e)
	    {
		System.err.println("Impossible to call the function removePersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	m_represented_groups.set(null);
	m_represented_groups_temp.set(null);
	m_activators.clear();
	m_activators_to_add.clear();
	m_activators_to_remove.clear();
	m_agents=null;
	m_scheduler=null;
	setChanged();
	try
	{
	    m_remove_group_changement_notifier_method.invoke(null, this);
	}
	catch (Exception e)
	{
	    System.err.println("Impossible to call the function removeGroupChangementNotifier of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
    
    /*protected madkit.kernel.Activator<A> getActivatorInstance(Group _group, String _role)
    {
	return new PersonalActivator(_group, _role); 
    }*/
    
    @Override
    public synchronized void potentialChangementInGroups()
    {
	if (m_scheduler==null)
	    return;
	m_activators_to_add.clear();
	m_activators_to_remove.clear();
	
	Group[] groups=m_group.getRepresentedGroups(m_scheduler.getKernelAddress());
	if (m_represented_groups.get()!=groups)
	{
	    if (m_represented_groups.get()==null)
	    {
		for (Group g : groups)
		{
		    madkit.kernel.Activator<A> a=new PersonalActivator(g, m_role);
		    //a.useMulticore(m_nb_used_cores.get());
		    m_activators_to_add.add(a);
		}
	    }
	    else
	    {
		//detecting new groups
		for (Group g : groups)
		{
		    boolean found=false;
		    for (Group pg : m_represented_groups.get())
		    {
			if (g.equals(pg))
			{
			    found=true;
			    break;
			}
		    }
		    if (!found)
		    {
			madkit.kernel.Activator<A> a=new PersonalActivator(g, m_role);
			//a.useMulticore(m_nb_used_cores.get());
			m_activators_to_add.add(a);
		    }
		}
		
		//detecting removed groups
		for (madkit.kernel.Activator<A> a : m_activators)
		{
		    String g=a.getGroup();
		    boolean found=false;
		    for (Group ng : groups)
		    {
			if (g.equals(ng.getPath()))
			{
			    found=true;
			    break;
			}
		    }
		    if (!found)
		    {
			m_activators_to_remove.add(a);
		    }
		}
	    }
	    m_represented_groups_temp.set(groups);
	}
	
    }
    @SuppressWarnings("unused")
    private synchronized void updateChangementInGroups()
    {
	if (m_represented_groups_temp.get()!=null && m_represented_groups_temp.get()!=m_represented_groups.get())
	{
	    for (madkit.kernel.Activator<A> a : m_activators_to_remove)
	    {
		m_activators.remove(a);
		try
		{
		    m_remove_madkit_activator_method.invoke(m_scheduler, a);
		}
		catch (Exception e)
		{
		    System.err.println("Impossible to call the function removePersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e.printStackTrace();
		    System.exit(-1);
		}
	    }
	    m_activators_to_remove.clear();
	    for (madkit.kernel.Activator<A> a : m_activators_to_add)
	    {
		m_activators.add(a);
		try
		{
		    m_add_madkit_activator_method.invoke(m_scheduler, a);
		}
		catch (Exception e)
		{
		    System.err.println("Impossible to call the function addPersonalActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e.printStackTrace();
		    System.exit(-1);
		}
	    }
	    m_activators_to_add.clear();
	    m_represented_groups.set(m_represented_groups_temp.get());
	    setChanged();
	}
    }
    
    protected void setChanged()
    {
	m_list_changed=true;
    }
    
    private class PersonalActivator extends madkit.kernel.Activator<A>
    {
	public PersonalActivator(Group _group, String _role)
	{
	    super(_group.getCommunity(), _group.getPath(), _role);
	}
	@Override protected void adding(A _agent)
	{
	    Activator.this.setChanged();
	    Activator.this.adding(_agent);
	}
	@Override protected void adding(List<A> _agents)
	{
	    Activator.this.setChanged();
	    Activator.this.adding(_agents);
	}
	@Override protected void removing(A _agent)
	{
	    Activator.this.setChanged();
	    Activator.this.removing(_agent);
	}
	@Override protected void removing(List<A> _agents)
	{
	    Activator.this.setChanged();
	    Activator.this.removing(_agents);
	}
	@Override public void initialize() 
	{
	    Activator.this.setChanged();
	}
	@Override public void execute(List<A> _agentsList, Object ...args)
	{
	    
	}
	@Override public void multicoreExecute(Object ...args)
	{
	    
	}
    }
    private class PersonalActivatorExecutor extends madkit.kernel.Activator<A>
    {

	public PersonalActivatorExecutor()
	{
	    super("", "", "");
	}
	@Override public void execute(List<A> _agentsList, Object ...args)
	{
	    Activator.this.execute(_agentsList, args);
	}
	
	@Override public List<A> getCurrentAgentsList()
	{
	    return Activator.this.getCurrentAgentsList();
	}
	
    }
    
    private static Method m_add_madkit_activator_method=null;
    private static Method m_remove_madkit_activator_method=null;
    private static Method m_add_group_changement_notifier_method=null;
    private static Method m_remove_group_changement_notifier_method=null;
    static
    {
	try
	{
	    m_add_madkit_activator_method=Scheduler.class.getDeclaredMethod("addPrivateActivator", madkit.kernel.Activator.class);
	    m_add_madkit_activator_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function addPrivateActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function addPrivateActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_remove_madkit_activator_method=Scheduler.class.getDeclaredMethod("removePrivateActivator", madkit.kernel.Activator.class);
	    m_remove_madkit_activator_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function removePrivateActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function removePrivateActivator of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
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
