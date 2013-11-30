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

import madkit.kernel.KernelAddress;

public class GroupRole
{
	private Group m_group;
	private int m_number_roles=0;
	//private ArrayList<String> m_roles;
	
	public GroupRole(Group _group)
	{
	    m_group=_group;
	}
	
	public void incrementMadKitReferences(KernelAddress ka)
	{
	    m_group.incrementMadKitReferences(ka);
	    m_number_roles++;
	}
	public void incrementMadKitReferences(int number_of_increment, KernelAddress ka)
	{
	    m_group.incrementMadKitReferences(number_of_increment, ka);
	    m_number_roles+=number_of_increment;
	}
	
	public void decrementMadKitReferences(KernelAddress ka)
	{
	    m_group.decrementMadKitReferences(1, ka);
	    m_number_roles--;
	}

	public void resetMadKitReferencesByRemovingRoles(KernelAddress ka)
	{
	    m_group.decrementMadKitReferences(m_number_roles, ka);
	    m_number_roles=0;
	}
	
	public boolean isContainingRoles()
	{
	    return m_number_roles>0;
	}
	
	public int getRolesNumber()
	{
	    return m_number_roles;
	}
	
	public Group getGroup()
	{
	    return m_group;
	}
	
	public static ArrayList<GroupRole> getGroupRoles(MKGEAbstractAgent agent)
	{
	    if (agent instanceof AbstractAgent)
	    {
		try
		{
		    @SuppressWarnings("unchecked")
		    ArrayList<GroupRole> list = (ArrayList<GroupRole>) (m_get_group_roles_abstract_agent_method.invoke(agent));
		    return list;
		}
		catch (IllegalArgumentException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (IllegalAccessException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (InvocationTargetException e1)
		{
		    e1.printStackTrace();
		    System.exit(-1);
		}
	    }
	    else if (agent instanceof Agent)
	    {
		try
		{
		    @SuppressWarnings("unchecked")
		    ArrayList<GroupRole> list = (ArrayList<GroupRole>) (m_get_group_roles_agent_method.invoke(agent));
		    return list;
		}
		catch (IllegalArgumentException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Agent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (IllegalAccessException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Agent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (InvocationTargetException e1)
		{
		    e1.printStackTrace();
		    System.exit(-1);
		}
	    }
	    else if (agent instanceof Scheduler)
	    {
		try
		{
		    @SuppressWarnings("unchecked")
		    ArrayList<GroupRole> list = (ArrayList<GroupRole>) (m_get_group_roles_scheduler_method.invoke(agent));
		    return list;
		}
		catch (IllegalArgumentException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (IllegalAccessException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (InvocationTargetException e1)
		{
		    e1.printStackTrace();
		    System.exit(-1);
		}
	    }
	    else if (agent instanceof Watcher)
	    {
		try
		{
		    @SuppressWarnings("unchecked")
		    ArrayList<GroupRole> list = (ArrayList<GroupRole>) (m_get_group_roles_watcher_method.invoke(agent));
		    return list;
		}
		catch (IllegalArgumentException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (IllegalAccessException e1)
		{
		    System.err.println("Impossible to call the function getGroupRoles of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		    e1.printStackTrace();
		    System.exit(-1);
		}
		catch (InvocationTargetException e1)
		{
		    e1.printStackTrace();
		    System.exit(-1);
		}
	    }
	    else
		throw new IllegalAccessError("Unknow object : "+agent.getClass());
	    return null;
	    
	}
	
	
	
	protected static Method m_get_group_roles_abstract_agent_method=null;
	protected static Method m_get_group_roles_agent_method=null;
	protected static Method m_get_group_roles_scheduler_method=null;
	protected static Method m_get_group_roles_watcher_method=null;
	
	static
	{
	    try
	    {
		m_get_group_roles_abstract_agent_method=AbstractAgent.class.getDeclaredMethod("getGroupRoles");
		m_get_group_roles_abstract_agent_method.setAccessible(true);
	    }
	    catch (SecurityException e)
	    {
		System.err.println("Impossible to access to the function getGroupRoles of the class AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    catch (NoSuchMethodException e)
	    {
		System.err.println("Impossible to found to the function getGroupRoles of the class AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    try
	    {
		m_get_group_roles_agent_method=Agent.class.getDeclaredMethod("getGroupRoles");
		m_get_group_roles_agent_method.setAccessible(true);
	    }
	    catch (SecurityException e)
	    {
		System.err.println("Impossible to access to the function getGroupRoles of the class Agent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    catch (NoSuchMethodException e)
	    {
		System.err.println("Impossible to found to the function getGroupRoles of the class Agent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    try
	    {
		m_get_group_roles_scheduler_method=Scheduler.class.getDeclaredMethod("getGroupRoles");
		m_get_group_roles_scheduler_method.setAccessible(true);
	    }
	    catch (SecurityException e)
	    {
		System.err.println("Impossible to access to the function getGroupRoles of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    catch (NoSuchMethodException e)
	    {
		System.err.println("Impossible to found to the function getGroupRoles of the class Scheduler. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    try
	    {
		m_get_group_roles_watcher_method=Watcher.class.getDeclaredMethod("getGroupRoles");
		m_get_group_roles_watcher_method.setAccessible(true);
	    }
	    catch (SecurityException e)
	    {
		System.err.println("Impossible to access to the function getGroupRoles of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
	    catch (NoSuchMethodException e)
	    {
		System.err.println("Impossible to found to the function getGroupRoles of the class Watcher. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
		e.printStackTrace();
		System.exit(-1);
	    }
		
	}
	
}
