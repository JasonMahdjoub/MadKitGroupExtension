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

package madkitgroupextension.simulation.activator;

import java.lang.reflect.Field;
import java.util.ConcurrentModificationException;
import java.util.List;

import madkitgroupextension.kernel.AbstractAgent;
import madkitgroupextension.kernel.AbstractGroup;
import madkitgroupextension.kernel.Activator;
import madkitgroupextension.kernel.MKGEAbstractAgent;
import madkitgroupextension.kernel.Scheduler;

/**
 * The class GenericBehaviorActivator of MadKitGroupExtension is similar to the class GenericBehaviorActivator of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.     
 * 
 * An activator that invokes a single method with no parameters on a group of agents.
 * This class encapsulates behavior invocation on MadKit agents for scheduler agents.
 * This activator allows to call a particular Java method on agents 
 * regardless of their actual class type as long
 * as they extend {@link AbstractAgent}. 
 * This has to be used by {@link Scheduler} subclasses to 
 * create simulation applications.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadKit 5.0.0.1
 * @since MadKitGroupExtension 1.0
 * @version 1.1
 * @see Activator
 */

public class GenericBehaviorActivator<A extends madkit.kernel.AbstractAgent & MKGEAbstractAgent> extends Activator<A>
{
    protected String m_the_behavior_to_activate;
    private PersonalActivatorExecutor m_executor=null;
	/**
	 * Builds a new GenericBehaviorActivator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler} 
	 * agent using the {@link Scheduler#addActivator(Activator)} method.
	 * Once added, it could be used to trigger the behavior on all the agents which are at this CGR location, regardless
	 * of their class type as long as they extend {@link AbstractAgent}
	 * @param _group the group(s) and the community(ies) name
	 * @param _role the role name
	 * @param theBehaviorToActivate name of the Java method which will be invoked
	 */
    public GenericBehaviorActivator(AbstractGroup _group, String _role, String theBehaviorToActivate)
    {
	super(_group, _role, theBehaviorToActivate);
	m_the_behavior_to_activate=theBehaviorToActivate;
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
	 * @param _agentsList the agent list to execute
	 * @param args arguments that could be used by the scheduler 
	 * to pass information to this activator for an activation
	 * @since 1.1
	 */
    @SuppressWarnings("unchecked")
    @Override public void execute(List<A> _agentsList, Object ...args)
    {
	
	if (m_executor==null)
	{
	    try
	    {
		m_executor = (PersonalActivatorExecutor)executor_field.get(this);
	    }
	    catch (IllegalArgumentException | IllegalAccessException e)
	    {
		System.err.println("Impossible to access the field m_executor of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	m_executor.personalExecute(_agentsList, args);
	
    }
    
    
    static final Field executor_field;
    
    static
    {
	Field m=null;
	try
	{
	    m=Activator.class.getDeclaredField("m_executor");
	    m.setAccessible(true);
	}
	catch (NoSuchFieldException | SecurityException e)
	{
	    System.err.println("Impossible to access the field m_executor of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	executor_field=m;
    }
    
    /**
     * this method is reserved to internal processes of MadKitGroupExtension.
     */
    @Override protected madkit.kernel.Activator<A> getPersonalActivatorInstance(Object ... args)
    {
	return new PersonalActivatorExecutor((String)args[0]);
    }
    
    public String getBehaviorName()
    {
	return m_the_behavior_to_activate;
    }
    
    private class PersonalActivatorExecutor extends madkit.simulation.activator.GenericBehaviorActivator<A>
    {
	
	public PersonalActivatorExecutor(String theBehavior)
	{
	    super("", "", "", theBehavior);
	}
	@Override public List<A> getCurrentAgentsList()
	{
	    return GenericBehaviorActivator.this.getCurrentAgentsList();
	    
	}
	@Override public void execute(List<A> agents, Object ...args)
	{
	    GenericBehaviorActivator.this.execute(agents, args);
	}
	
	public void personalExecute(List<A> agents, Object ...args)
	{
	    super.execute(agents, args);
	}
	
    }

}
