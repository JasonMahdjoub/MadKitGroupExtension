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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import javax.swing.JFrame;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import madkit.gui.OutputPanel;
import madkit.i18n.ErrorMessages;
import madkit.i18n.Words;
import madkit.kernel.AgentAddress;
import madkit.kernel.Gatekeeper;
import madkit.kernel.KernelAddress;
import madkit.kernel.MadkitClassLoader;
import madkit.kernel.Message;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.util.XMLUtilities;




/**
 * The class Scheduler of MadKitGroupExtension inherits from the class Scheduler of MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.     
 * 
 * This class defines a generic threaded scheduler agent. It holds a collection
 * of activators. The default state of a scheduler is {@link madkit.kernel.Scheduler.SimulationState#PAUSED}. The
 * default delay between two steps is 0 ms (max speed).
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @author Jason Mahdjoub
 * @since MadKit 2.0
 * @since MadKitGroupExtension 1.0
 * @version 1.0
 * @see Activator
 */
public class Scheduler extends madkit.kernel.Scheduler implements MKGEScheduler
{

    private static final long serialVersionUID = 5120873930866757927L;

	/**
	 * This constructor is equivalent to <code>Scheduler(Double.MAX_VALUE)</code>
	 */
    public Scheduler()
    {
	super();
    }
	/**
	 * Constructor specifying the time at which the simulation ends.
	 * 
	 * @param endTime
	 *           the GVT at which the simulation will automatically stop
	 */
    public Scheduler(double endTime)
    {
	super(endTime);
    }
    
    /*
     * AbstractAgent code
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     */

    private final ArrayList<GroupRole> m_group_roles=new ArrayList<GroupRole>();
    
    
    private static class AbstractGroupRole
    {
	public final AbstractGroup group;
	public final String role;
	public final Object passKey;
	public AbstractGroupRole(AbstractGroup _group, String _role, Object _passkey)
	{
	    group=_group.clone();
	    role=_role;
	    passKey=_passkey;
	}
	
	public boolean equals(AbstractGroupRole _agr)
	{
	    return group.equals(_agr.group) && role.equals(_agr.role);
	}
	
	@Override public boolean equals (Object o)
	{
	    if (o instanceof AbstractGroupRole)
		return this.equals((AbstractGroupRole)o);
	    return false;
	}
    }
    private static class GR
    {
	public final Group group;
	public final String role;
	public GR(Group _group, String _role)
	{
	    group=_group.clone();
	    role=_role;
	}
	@Override public boolean equals(Object o)
	{
	    if (o instanceof GR)
		return equals((GR)o);
	    else
		return false;
	}
	public boolean equals(GR g)
	{
	    return g.group.equals(group) && role.equals(g.role);
	}
    }
    
    private ArrayList<AbstractGroupRole> groups_to_auto_request=null;
    private ArrayList<GR> auto_requested_groups=null;
    
    
    
    
    /**
     * Do not use this function !
     */
    
    @Override public void potentialChangementInGroups()
    {
	synchronized(m_group_roles)
	{
	    if (groups_to_auto_request!=null)
	    {
		@SuppressWarnings("unchecked")
		ArrayList<GR> arg=(ArrayList<GR>)auto_requested_groups.clone();
		for (AbstractGroupRole agr : groups_to_auto_request)
		{
		    Group[] represented_groups=agr.group.getRepresentedGroups(this.getKernelAddress());
		    if (represented_groups!=null)
		    {
			for (Group g : represented_groups)
			{
			    GR gr=new GR(g, agr.role);
			    if (!super.isRole(g.getCommunity(), g.getPath(), agr.role))
			    {
				if (super.bucketModeRequestRole(g.getCommunity(), g.getPath(), agr.role, agr.passKey).equals(ReturnCode.SUCCESS))
				{
				    if (!auto_requested_groups.contains(gr))
					auto_requested_groups.add(gr);
				}
			    }
			    arg.remove(gr);
			}
		    }
		}
		for (GR g : arg)
		{
	    	    auto_requested_groups.remove(g);
	    	    super.leaveRole(g.group.getCommunity(), g.group.getPath(), g.role);
		}
	    }
    	}
    }
    
    
    /**
     * Automatically request the given role into the given represented groups, only for groups that have been requested with other agents. Do nothing else.
     * When other agents leave roles, those that correspond to the current auto-requested role are automatically leaved from this agent.
     * 
     * This function is equivalent to <code>autoRequestRole(AbstractGroup, String, null)</code>.
     * 
     * @param _group the abstract group and these represented groups.
     * @param role the role to request
     * @see #autoRequestRole(AbstractGroup, String, Object)
     * @see #removeAutoRequestedGroup(AbstractGroup)
     * @see #removeAutoRequestedRole(String)
     */
    public void autoRequestRole(AbstractGroup _group, String role)
    {
	this.autoRequestRole(_group, role, null);
    }
    
    /**
     * Automatically request the given role into the given represented groups, only for groups that have been requested with other agents. Do nothing else.
     * When other agents leave roles, those that correspond to the current auto-requested role are automatically leaved from this agent.
     * @param _group the abstract group and these represented groups.
     * @param _role the role to request
     * @param _passKey
     * 		 the <code>passKey</code> to enter a secured group. It is
     *           generally delivered by the group's <i>group manager</i>. It	
     *           could be <code>null</code>, which is sufficient to enter an
     *           unsecured group. Especially,
     *           {@link #autoRequestRole(AbstractGroup, String)} uses a null
     *           <code>passKey</code>.
     * @see #removeAutoRequestedGroup(AbstractGroup)
     * @see #removeAutoRequestedRole(String)
     */
    public void autoRequestRole(AbstractGroup _group, String _role, Object _passKey)
    {
	if (_group==null || _role==null)
	    return;
	boolean toadd=false;
	synchronized(m_group_roles)
	{
	    if (groups_to_auto_request==null)
	    {
		groups_to_auto_request=new ArrayList<AbstractGroupRole>();
		auto_requested_groups=new ArrayList<GR>();
		toadd=true;
	    }
	    groups_to_auto_request.add(new AbstractGroupRole(_group, _role, _passKey));
	}
	if (toadd)
	    Group.addGroupChangementNotifier(this);
	potentialChangementInGroups();
    }
    
    /**
     * Remove role from automatically requested roles.
     * @param role
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public void removeAutoRequestedRole(String role)
    {
	if (role==null)
	    return;
	boolean toremove=false;
	synchronized(m_group_roles)
	{
	    if (groups_to_auto_request!=null)
	    {
		boolean oneremoved=false;
		Iterator<AbstractGroupRole> it=groups_to_auto_request.iterator();
		while (it.hasNext())
		{
		    AbstractGroupRole agr=it.next();
		    if (agr.role.equals(role))
		    {
			it.remove();
			oneremoved=true;
		    }
		}
		if (oneremoved)
		{
		    potentialChangementInGroups();
	    
		    if (groups_to_auto_request.size()==0)
		    {
			toremove=true;
			groups_to_auto_request=null;
			auto_requested_groups=null;
		    }
		}
	    }
	}
	if (toremove)
	    Group.removeGroupChangementNotifier(this);
    }
    
    /**
     * Replace automatically requested groups by other groups. To replace the old abstract group, the references are used, and not the equals function. So the replacement is done only if <code>_old_group==_new_group</code>. 
     * @param _old_group the old group to compare and replace according its reference
     * @param _new_group the new group to replace
     * @return true if the operation have succeeded.
     * 
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public boolean replaceAutoRequestedGroup(AbstractGroup _old_group, AbstractGroup _new_group)
    {
	if (_old_group==null)
	    return false;
	if (_new_group==null)
	    removeAutoRequestedGroup(_old_group);

	synchronized(m_group_roles)
	{
	    if (groups_to_auto_request!=null)
	    {
		AbstractGroupRole found=null;
		Iterator<AbstractGroupRole> it=groups_to_auto_request.iterator();
		while (it.hasNext())
		{
		    AbstractGroupRole agr=it.next();
		    
		    if (agr.group==_old_group)
		    {
			found=agr;
			it.remove();
			break;
		    }
		}
		if (found!=null)
		{
		    groups_to_auto_request.add(new AbstractGroupRole(_new_group, found.role, found.passKey));
		    potentialChangementInGroups();
		    return true;
		}
		else 
		    return false;
	    }
	}
	return false;
    }
    
    
    /**
     * Remove group from automatically requested groups. If this group is contained into a MultiGroup, or if it is contained into the subdirectories of a Group that represent them, then all the concerned AbstractGroup is removed. 
     * @param _group the given group
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public void removeAutoRequestedGroup(AbstractGroup _group)
    {
	if (_group==null)
	    return;
	Group[] groups=_group.getRepresentedGroups(this.getKernelAddress());
	if (groups==null)
	    return;
	boolean toremove=false;
	synchronized(m_group_roles)
	{
	    if (groups_to_auto_request!=null)
	    {
		boolean oneremoved=false;
		Iterator<AbstractGroupRole> it=groups_to_auto_request.iterator();
		while (it.hasNext())
		{
		    AbstractGroupRole agr=it.next();
		    
		    if (agr.equals(_group))
		    {
			it.remove();
			oneremoved=true;
		    }
		    else
		    {
			Group groups2[]=agr.group.getRepresentedGroups(this.getKernelAddress());
			if (groups2!=null)
			{
			    boolean breaked=false;
			    for (Group g : groups)
			    {
				for (Group g2 : groups2)
				{
				    if (g.equals(g2))
				    {
					it.remove();
					breaked=true;
					oneremoved=true;
					break;
				    }
				}
				if (breaked)
				    break;
			    }
			}
		    }
		}
		if (oneremoved)
		{
		    potentialChangementInGroups();

		    if (groups_to_auto_request.size()==0)
		    {
			toremove=true;
		    	groups_to_auto_request=null;
		    	auto_requested_groups=null;
		    }
		}
	    }
	}
	if (toremove)
	    Group.removeGroupChangementNotifier(this);
    }
    
	/**
	 * replaced by {@link #hasRole(Group, String)}
	 */
	@Override @Deprecated public boolean hasRole(final String community, final String group, final String role) {
	    return this.hasRole(new Group(community, group), role);
	}
    
	/**
	 * 
	 * Tells if the agent is currently playing a specific role.
	 * 
	 * @param _group
	 * @param role
	 * @return <code>true</code> if the agent is playing this role
	 * 
	 * @since MaDKit 5.0.3
	 * @since MadKitGroupExtension 1.3.2
	 */
	@Override public boolean hasRole(final Group _group, final String role) {
	    return super.hasRole(_group.getCommunity(), _group.getPath(), role);
	}
	
	/**
	 * replaced by {@link #getMyMKGEGroups(String)}
	 */
	@Override @Deprecated public TreeSet<String> getMyGroups(final String community){
	    return super.getMyGroups(community);
	}
	
	/**
	 * Gets the names of the groups the agent is in
	 * according to a community
	 * 
	 * @param community
	 * @return a set containing the groups the agent is in, or <code>null</code> if this
	 * community does not exist. This set could be empty.
	 */
	@Override public TreeSet<Group> getMyMKGEGroups(final String community)
	{
	    TreeSet<String> tmp=super.getMyGroups(community);
	    if (tmp==null)
		return null;
	    TreeSet<Group> res=new TreeSet<>();
	    for (String s : tmp)
	    {
		res.add(Group.getGroupFromPath(community, s));
	    }
	    return res;
	}
	
	/**
	 * replaced by {@link #getMyMKGERoles(Group)}
	 */
	@Deprecated @Override public final TreeSet<String> getMyRoles(final String community, final String group){
	    return this.getMyMKGERoles(new Group(community, group));
	}
	
	/**
	 * Gets the names of the roles that the agent has in
	 * a specific group
	 * 
	 * @param _group
	 * @return a sorted set containing the names of the roles
	 * the agent has in a group, or <code>null</code> if the
	 * community or the group does not exist. This set could be empty.
	 */
	@Override public TreeSet<String> getMyMKGERoles(Group _group){
		return super.getMyRoles(_group.getCommunity(), _group.getPath());
	}

    @SuppressWarnings("unused")
    private ArrayList<GroupRole> getGroupRoles()
    {
	return m_group_roles;
    }
    
    @Deprecated @Override public TreeSet<String> getExistingGroups(String community)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
	/**
	 * returns the names of the groups that exist in this community.
	 * 
	 * @param community the community's name
	 * 
	 * @return a set containing the groups which exist in this community, or <code>null</code> if it does not exist. 
	 * 
	 * @since MaDKit 5.0.0.20
	 * @since MadKitGroupExtension 1.1
	 */
    public TreeSet<Group> getExistingGroupsInKernel(String community)
    {
	TreeSet<Group> res=new TreeSet<Group>();
	TreeSet<String> e=super.getExistingGroups(community);
	if (e==null)
	    return null;
	for (String s : e)
	    res.add(Group.getGroupFromPath(community, s));
	return res;
    }
    
    @Deprecated @Override public TreeSet<String> getExistingRoles(String community, String group)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }

	/**
	 * returns the names of the roles that exist in this abstract group.
	 * 
	 * @param _group the group's name
	 * 
	 * @return a set containing the names of the roles 
	 * which exist in this group, or <code>null</code> if it does not exist. 
	 * 
	 * @since MaDKit 5.0.0.20
	 * @since MadKitGroupExtension 1.1
	 */
    public TreeSet<String> getExistingRoles(AbstractGroup _group)
    {
	TreeSet<String> res=new TreeSet<String>();
	for (Group g : _group.getRepresentedGroups(getKernelAddress()))
	{
	    TreeSet<String> ts=super.getExistingRoles(g.getCommunity(), g.getPath());
	    if (ts!=null)
		res.addAll(ts);
	}
	if (res.size()==0)
	    return null;
	return res;
    }
    
	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization on a particular kernel. The caller is excluded from the search.
	 * 
	 * @param community
	 *           the community name
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param from
	 *           the kernel address on which the agent is running
	 * @return an {@link AgentAddress} corresponding to an agent handling this
	 *         role on the targeted kernel or <code>null</code> if such an agent does not exist.
	 */
    @Deprecated @Override public AgentAddress getDistantAgentWithRole(String community, String group, String role, KernelAddress from)
    {
	return this.getDistantAgentWithRole(new Group(community, group), role, from);
    }
    
	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization on a particular kernel. The caller is excluded from the search.
	 * 
	 * @param group
	 *           the group name
	 * @param role
	 *           the role name
	 * @param from
	 *           the kernel address on which the agent is running
	 * @return an {@link AgentAddress} corresponding to an agent handling this
	 *         role on the targeted kernel or <code>null</code> if such an agent does not exist.
	 */
    public AgentAddress getDistantAgentWithRole(AbstractGroup group, String role, KernelAddress from)
    {
	Group[] groups=group.getRepresentedGroups(from);
	for (Group g : groups)
	{
	    AgentAddress aa=super.getDistantAgentWithRole(g.getCommunity(), g.getPath(), role, from);
	    if (aa!=null)
		return aa;
	}
	return null;
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.broadcastMessage(new Group(community, group), role, message)</code>. 
     * 
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode broadcastMessage(String community, String group, String role, Message message)
    {
	return this.broadcastMessage(new Group(community, group), role, message);
    }
    
	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community, but not to the sender.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _roleName
	 *           the role name
	 * @param _m
	 * 	     the message
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode broadcastMessage(AbstractGroup _groups, String _roleName, Message _m)
    {
	return this.broadcastMessageWithRole(_groups, _roleName, _m, null);
    }

    /**
     * This function is deprecated and has the same effect that <code>this.broadcastMessageWithRole(new Group(_community, _group), _roleName, _m, _senderRole)</code>.
     * @since MadKitGroupExtension 1.0 
     * 
     */
    @Deprecated @Override public final ReturnCode broadcastMessageWithRole(String _community, String _group, String _roleName, Message _m, String _senderRole)
    {
	return this.broadcastMessageWithRole(new Group(_community, _group), _roleName, _m, _senderRole);
    }
    
        
	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community using a specific role for the sender. The sender is excluded
	 * from the search.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _roleName
	 *           the role name
	 * @param _m
	 * 	     the message
	 * @param _senderRole
	 *           the role name of the sender
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode broadcastMessageWithRole(AbstractGroup _groups, String _roleName, Message _m, String _senderRole)
    {
	boolean role_ok=false;
	boolean not_recipient=true;
	boolean not_in_group=true;
	
	Group[] groups=_groups.getRepresentedGroups(this.getKernelAddress());
	if (groups.length==0)
	    return ReturnCode.NOT_GROUP;
	for (Group g : groups)
	{
	    ReturnCode r=super.broadcastMessageWithRole(g.getCommunity(), g.getPath(), _roleName, _m, _senderRole);
	    if (r.equals(ReturnCode.NOT_COMMUNITY) || r.equals(ReturnCode.NOT_GROUP))
		throw new IllegalAccessError("Problem of data integrity ! The group "+_groups+" should be created on MadKit ! These bug is located into MadKitGroupExtension !");
	    if (!r.equals(ReturnCode.NO_RECIPIENT_FOUND))
	    {
		not_recipient=false;
	    }
	    if (!r.equals(ReturnCode.NOT_ROLE))
		role_ok=true;
	    if (r.equals(ReturnCode.SUCCESS))
		not_in_group=false;
	}
	
	if (role_ok)
	{
	    if (not_recipient)
		return ReturnCode.NO_RECIPIENT_FOUND;
	    else if (not_in_group)
		return ReturnCode.NOT_IN_GROUP;
	    else
		return ReturnCode.SUCCESS;
	}
	else
	    return ReturnCode.NOT_ROLE;
    }





    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode createGroup(String _community, String _group, boolean _isDistributed, Gatekeeper _theIdentifier)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    


    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode createGroup(String _community, String _group, boolean _isDistributed)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }


    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode createGroup(String _community, String _group)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }



    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final boolean createGroupIfAbsent(String _community, String _group, boolean _isDistributed, Gatekeeper _theIdentifier)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final boolean createGroupIfAbsent(String _community, String _group, boolean _isDistributed)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final boolean createGroupIfAbsent(String _community, String _group)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final void destroyCommunity(String _community)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    
    
    /*@Override public final void destroyCommunity(String _community)
    {
	synchronized(m_groups)
	{
	    super.destroyCommunity(_community);
	    Iterator<Group> it=m_groups.iterator();
	
	    while (it.hasNext())
	    {
		Group g=it.next();
		if (g.getCommunity().equals(_community))
		{
		    g.setMadKitCreatedToFalse();
		    it.remove();
		}
	    }
	}
    }*/

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final void destroyGroup(String community, String group)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    /*public void destroyGroup(AbstractGroup _g)
    {
	synchronized(m_groups)
	{
	    for (Group g : _g.getRepresentedGroups())
	    {
		m_groups.remove(g);
		g.setMadKitCreatedToFalse();
		super.destroyGroup(g.getCommunity(), g.getPath());
	    }
	}
    }*/
    
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final void destroyRole(String community, String group, String _role)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    /**
     * Initially designed to be called when the agent die, the code written by the user by inheriting this function 
     * must be now placed on the function <code>{@link #deactivate()}</code>.
     * @since MadKitGroupExtension 1.0
     * 
     */
    @Override protected final void end()
    {
	deactivate();
	synchronized(m_group_roles)
	{
	    for (GroupRole gr : m_group_roles)
	    {
		gr.resetMadKitReferencesByRemovingRoles(this.getKernelAddress());
	    }
	    m_group_roles.clear();
	}
    }

	/**
	 * This method corresponds to the last behavior which is called by the MadKit
	 * kernel. This call occurs when a threaded agent normally exits its live
	 * method or when the agent is killed. Usually a good place to release taken
	 * resources or log what has to be logged.
	 * 
	 * It has to be noted that the kernel automatically takes care of removing
	 * the agent from the organizations it is in. However, this cleaning is not
	 * logged by the agent. Therefore it could be of interest for the agent to do
	 * that itself.
	 * <p>
	 * Here is a typical example:
	 * <p>
	 * 
	 * <pre>
	 * <tt>@Override</tt>
	 * protected void deactivate()
	 * {
	 * 	AbstractAgent.ReturnCode returnCode = leaveRole(new Group("a community", "a group"), "my role");
	 * 	if (returnCode == AbstractAgent.ReturnCode.SUCCESS){
	 * 		if(logger != null)
	 * 			logger.info("I am leaving the artificial society");
	 * 	}
	 * 	else{
	 * 		if(logger != null)
	 * 			logger.warning("something wrong when ending, return code is "+returnCode);
	 * 	}
	 * 	if(logger != null)
	 * 		logger.info("I am done");
	 * 	}
	 * }
	 * </pre>
	 * @since MadKitGroupExtension 1.0
	 */
    protected void deactivate()
    {
	
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.getAgentAddressIn(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public AgentAddress getAgentAddressIn(String _community, String _group, String _role)
    {
	return this.getAgentAddressIn(new Group(_community, _group), _role);
    }

    	/**
	 * Returns the agent address of this agent at this CGR location.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the role name
	 * @return the agent's address in this location or <code>null</code> if this
	 *         agent does not handle this role.
	 * @since MadKit 5.0.0.15
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public AgentAddress getAgentAddressIn(Group _group, String _role)
    {
	return super.getAgentAddressIn(_group.getCommunity(), _group.getName(), _role);
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.getAgentsWithRole(new Group(_community, _group), _role, callerIncluded)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final List<AgentAddress> getAgentsWithRole(String _community, String _group, String _role, boolean callerIncluded)
    {
	return this.getAgentsWithRole(new Group(_community, _group), _role, callerIncluded);
    }
    /**
     * This function is deprecated and has the same effect that <code>this.getAgentsWithRole(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final List<AgentAddress> getAgentsWithRole(String _community, String _group, String _role)
    {
	return this.getAgentsWithRole(new Group(_community, _group), _role);
    }

	/**
	 * Returns an {@link java.util.List} containing agents that handle this role
	 * in the organization. The caller is excluded from this list.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @return a {@link java.util.List} containing agents that handle this role
	 *         or <code>null</code> if no agent has been found.
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public List<AgentAddress> getAgentsWithRole(AbstractGroup _groups, String _role)
    {
	return this.getAgentsWithRole(_groups, _role, false);
    }
    
	/**
	 * Returns an {@link java.util.List} containing all the agents that handle
	 * this role in the organization.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the _role name
	 * @param callerIncluded
	 *           if <code>false</code>, the caller is removed from the list if it
	 *           is in.
	 * @return a {@link java.util.List} containing agents that handle this role
	 *         or <code>null</code> if no agent has been found.
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public List<AgentAddress> getAgentsWithRole(AbstractGroup _groups, String _role, boolean callerIncluded)
    {
	Group[] groups=_groups.getRepresentedGroups(this.getKernelAddress());
	ArrayList<List<AgentAddress>> all_lists=new ArrayList<List<AgentAddress>>(groups.length); 
	
	int total=0;
	for (Group g : groups)
	{
	    List<AgentAddress> l=super.getAgentsWithRole(g.getCommunity(), g.getPath(), _role, callerIncluded);
	    if (l!=null)
	    {
		all_lists.add(l);
		total+=l.size();
	    }
	}
	
	HashSet<AgentAddress> res=new HashSet<AgentAddress>(total);
	
	for (List<AgentAddress> l : all_lists)
	{
	    res.addAll(l);
	}
	if (res.size()==0)
	    return null;
	ArrayList<AgentAddress> res2=new ArrayList<AgentAddress>();
	res2.addAll(res);
	
	return res2;
    }


    /**
     * This function is deprecated and has the same effect that <code>this.getAgentWithRole(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final AgentAddress getAgentWithRole(String _community, String _group, String _role)
    {
	return this.getAgentWithRole(new Group(_community, _group), _role);
    }


	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @return an {@link AgentAddress} corresponding to an agent handling this
	 *         role or <code>null</code> if such an agent does not exist.
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public AgentAddress getAgentWithRole(AbstractGroup _groups, String _role)
    {
	Group[] groups=_groups.getRepresentedGroups(this.getKernelAddress());
	
	ArrayList<AgentAddress> res=new ArrayList<AgentAddress>(groups.length);
	
	for (Group g : groups)
	{
	    AgentAddress aa=super.getAgentWithRole(g.getCommunity(), g.getPath(), _role);
	    if (aa!=null)
		res.add(aa);
	}
	if (res.size()==0)
	    return null;
	else
	    return res.get((int)(Math.random()*res.size())); 
    }



    /**
     * This function is deprecated and has the same effect that <code>this.isGroup(new Group(_community, _group))</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final boolean isGroup(String _community, String _group)
    {
	return this.isGroup(new Group(_community, _group));
    }

	/**
	 * Tells if a group exists in the artificial society.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @return <code>true</code> If a group with this name exists in this
	 *         community, <code>false</code> otherwise.
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public boolean isGroup(Group _group)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);
	return super.isGroup(_group.getCommunity(), _group.getPath());
    }



    /**
     * This function is deprecated and has the same effect that <code>this.isRole(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final boolean isRole(String _community, String _group, String _role)
    {
	return this.isRole(new Group(_community, _group), _role);
    }
    

	/**
	 * Tells if a role exists in the artificial society.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the name of the role
	 * @return <code>true</code> If a role with this name exists in this
	 *         <community;group> couple, <code>false</code> otherwise.
	 * @throws IllegalArgumentException when the given group represents also its subgroups        
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public boolean isRole(Group _group, String _role)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);
	
	return super.isRole(_group.getCommunity(), _group.getPath(), _role);
    }





    /**
     * This function is deprecated and has the same effect that <code>this.leaveGroup(new Group(_community, _group))</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode leaveGroup(String _community, String _group)
    {
	return this.leaveGroup(new Group(_community, _group));
    }


	/**
	 * Makes this agent leaves the group of a particular community.
	 * 
	 * @param _group
	 *           the group and the community name
	 * 
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of this group.</li>
	 *         </ul>
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKit 5.0
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode leaveGroup(Group _group)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);

	GroupRole founded_group=null;
	ReturnCode r=null;
	synchronized(m_group_roles)
	{
	    r=super.leaveGroup(_group.getCommunity(), _group.getPath());
	
	    if (r.equals(ReturnCode.SUCCESS))
	    {
		Iterator<GroupRole> it=m_group_roles.iterator();
	    
		while (it.hasNext() && founded_group==null)
		{
		    GroupRole gr=it.next();
		    if (gr.getGroup().equals(_group))
		    {
			gr.resetMadKitReferencesByRemovingRoles(this.getKernelAddress());
			it.remove();
			founded_group=gr;
		    }
		}
		if (founded_group==null)
		{
		    throw new IllegalAccessError("Problem of data integrity ! The group "+_group+" should be present on this agent ! These bug is located into MadKitGroupExtension !");
		}
	    }
	}
	if (founded_group!=null)
	    founded_group.resetMadKitReferencesByRemovingRoles(this.getKernelAddress());
	
	return r;
    }

    /**
     * This function is deprecated and has the same effect that <code>this.leaveRole(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode leaveRole(String _community, String _group, String _role)
    {
	return this.leaveRole(new Group(_community, _group), _role);
    }

	/**
	 * Abandons an handled role within a group of a particular community.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the role name
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ROLE_NOT_HANDLED}</code>: If this role
	 *         is not handled by this agent.</li>
	 *         </ul>
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKit 5.0
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode leaveRole(Group _group, String _role)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);
	
	GroupRole founded_group=null;
	ReturnCode r=null;
	synchronized(m_group_roles)
	{
	    r=super.leaveRole(_group.getCommunity(), _group.getPath(), _role);
	    
	    if (r.equals(ReturnCode.SUCCESS))
	    {
		synchronized(m_group_roles)
		{
		    Iterator<GroupRole> it=m_group_roles.iterator();
	    
		    while (it.hasNext() && founded_group==null)
		    {
			GroupRole gr=it.next();
			if (gr.getGroup().equals(_group))
			{
			    if (gr.getRolesNumber()<=1)
				it.remove();
			    founded_group=gr;
			}
		    }
		    if (founded_group==null)
		    {
			throw new IllegalAccessError("Problem of data integrity ! The group "+_group+" should be present on this agent ! These bug is located into MadKitGroupExtension !");
		    }
		}
	    }
	    
	}
	if (founded_group!=null)
	    founded_group.decrementMadKitReferences(this.getKernelAddress());
	
	return r;
    }

    /**
     * This function is deprecated and has the same effect that <code>this.requestRole(new Group(_community, _group), _role, _passKey)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode requestRole(String _community, String _group, String _role, Object _passKey)
    {
	return this.requestRole(new Group(_community, _group), _role, _passKey);
    }

	/**
	 * Requests a role within a group of a particular community using a passKey.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the desired role.
	 * @param _passKey
	 *           the <code>passKey</code> to enter a secured group. It is
	 *           generally delivered by the group's <i>group manager</i>. It
	 *           could be <code>null</code>, which is sufficient to enter an
	 *           unsecured group. Especially,
	 *           {@link #requestRole(Group, String)} uses a null
	 *           <code>passKey</code>.
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#IGNORED}</code>: If this method is
	 *         used in activate and this agent has been launched using
	 *         {@link AbstractAgent#launchAgentBucket(String, int, Role...)}
	 *         </li>
	 *         </ul>
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKit 5.0
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see madkit.kernel.Gatekeeper
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode requestRole(Group _group, String _role, Object _passKey)
    {
	return this.requestRole(_group, _role, _passKey, false);
    }
    
    private ReturnCode requestRole(Group _group, String _role, Object _passKey, boolean bucket_mode)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);

	boolean madkitcreated=false;
	try
	{
	    madkitcreated=((Boolean)m_is_madkit_created_method.invoke(_group, this.getKernelAddress())).booleanValue();
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    e1.printStackTrace();
	    System.exit(-1);
	}

	if (!madkitcreated)
	{
	    ReturnCode r=null;
	    if (bucket_mode)
	    	r=super.bucketModeCreateGroup(_group.getCommunity(), _group.getPath(), _group.isMadKitDistributed(), _group.getMadKitIdentifier());
	    else
		r=super.createGroup(_group.getCommunity(), _group.getPath(), _group.isMadKitDistributed(), _group.getMadKitIdentifier());
	    if (r.equals(ReturnCode.ALREADY_GROUP))
		throw new IllegalAccessError("Problem of data integrity ! The group "+_group+" has already be already created on MadKit. These bug is located into MadKitGroupExtension !");
	}

	GroupRole concerned_gr=null;
	ReturnCode r=null;
	synchronized(m_group_roles)
	{
	    String c=_group.getCommunity();
	    String p=_group.getPath();
	    
	    
	    boolean toleave=false;
	    if (auto_requested_groups!=null)
		toleave=auto_requested_groups.remove(new GR(_group, _role));

	    if (toleave)
	    {
		super.leaveRole(c, p, _role);
	    }
	    if (bucket_mode)
		r=super.requestRole(c, p, _role, _passKey);
	    else
		r=super.bucketModeRequestRole(c, p, _role, _passKey);

	    if (r.equals(ReturnCode.NOT_COMMUNITY) || r.equals(ReturnCode.NOT_GROUP))
		throw new IllegalAccessError("Problem of data integrity ! The group and/or the community "+_group+" should be already created. However, Madkit returns that there have not been created. These bug is located into MadKitGroupExtension !");
	
	    if (r.equals(ReturnCode.SUCCESS))
	    {
		if (!madkitcreated)
		{
		    concerned_gr=new GroupRole(_group);
		    m_group_roles.add(concerned_gr);
		}
		else
		{
		    for (GroupRole gr : m_group_roles)	
		    {
			if (gr.getGroup().equals(_group))
			{
			    concerned_gr=gr;
			    break;
			}
		    }
		    if (concerned_gr==null)
		    {
			concerned_gr=new GroupRole(_group);
			m_group_roles.add(concerned_gr);
		    }
		}
	    }
	}
	if (concerned_gr!=null)
    	    concerned_gr.incrementMadKitReferences(this.getKernelAddress());
    	return r;
	
    }


    /**
     * This function is deprecated and has the same effect that <code>this.requestRole(new Group(_community, _group), _role)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode requestRole(String _community, String _group, String _role)
    {
	return this.requestRole(new Group(_community, _group), _role);
    }

	/**
	 * Requests a role within a group of a particular community. This has the
	 * same effect as <code>requestRole(group, role, null)</code>.
	 * So the passKey is <code>null</code> and the group must
	 * not be secured for this to succeed.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the desired role.
	 * @see #requestRole(String, String, String, Object)
	 * @since MadKit 5.0
	 * 
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode requestRole(Group _group, String _role)
    {
	return this.requestRole(_group,_role, null);
    }



    /**
     * This function is deprecated and has the same effect that <code>this.sendMessage(new Group(_community, _group), _role, _message)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode sendMessage(String _community, String _group, String _role, Message _message)
    {
	return this.sendMessage(new Group(_community, _group), _role, _message);
    }


	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect
	 * as <code>sendMessageWithRole(group, role, messageToSend,null)</code>. If
	 * several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @param _messageToSend
	 *           the message to send
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode sendMessage(AbstractGroup _groups, String _role, Message _messageToSend)
    {
	return this.sendMessageWithRole(_groups, _role, _messageToSend, null);
    }


    /**
     * This function is deprecated and has the same effect that <code>this.sendMessageWithRole(new Group(_community, _group), _role, _messageToSend, _senderRole)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public final ReturnCode sendMessageWithRole(String _community, String _group, String _role, Message _messageToSend, String _senderRole)
    {
	return this.sendMessageWithRole(new Group(_community, _group), _role, _messageToSend, _senderRole);
    }


	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(groups, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param _groups
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @param _messageToSend
	 *           the message to send
	 * @param _senderRole
	 *           the agent's role with which the message has to be sent
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode sendMessageWithRole(AbstractGroup _groups, String _role, Message _messageToSend, String _senderRole)
    {
	AgentAddress aa=getAgentWithRole(_groups, _role);
	
	if (aa!=null)
	    return super.sendMessageWithRole(aa, _messageToSend, _senderRole);
	else
	    return ReturnCode.NO_RECIPIENT_FOUND;
    }

	/**
	 * Compares this agent with the specified agent for order with respect to
	 * instantiation time.
	 * 
	 * @param _other
	 *           the agent to be compared.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @return a negative integer, a positive integer or zero as this agent has
	 *         been instantiated before, after or is the same agent than the
	 *         specified agent.
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public int compareTo(madkit.kernel.AbstractAgent _other)
    {
	if (!(_other instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The given agent as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.compareTo(_other);
    }
	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,Integer.MAX_VALUE)</code> so that the targeted
	 * agent has a lot of time to complete its {@link #end()} method.
	 * 
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_KILLED}</code>: If the target
	 *         has been already killed.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_YET_LAUNCHED}</code>: If the
	 *         target has not been launched.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MadKit 5.0
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see #killAgent(AbstractAgent, int)
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode killAgent(madkit.kernel.AbstractAgent _target)
    {
	if (!(_target instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.killAgent(_target);
    }
	/**
	 * Kills the targeted agent. The kill process stops the agent's life cycle
	 * but allows it to process its {@link #end()} method until the time out
	 * elapsed.
	 * <p>
	 * If the target is in the activate or live method (Agent subclasses), it
	 * will be brutally stop and then proceed its end method.
	 * 
	 * <p>
	 * 
	 * The method returns only when the targeted agent actually ends its life. So
	 * if the target contains a infinite loop, the caller can be blocked. Using a
	 * timeout thus ensures that the caller will be blocked only a certain amount
	 * of time. Using 0 as timeout will stop the target as soon as possible,
	 * eventually brutally stop the its life cycle. In such a case, if its end
	 * method has not been started, it will never run.
	 * 
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li> <li><code>
	 *         {@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_KILLED}</code>: If the target has been
	 *         already killed.</li> <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_YET_LAUNCHED}
	 *         </code>: If the target has not been launched.</li> <li><code>
	 *         {@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT}</code>: If the target's end method took
	 *         too much time and has been brutally stopped.</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode killAgent(madkit.kernel.AbstractAgent _target, int _timeOutSeconds)
    {
	if (!(_target instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.killAgent(_target, _timeOutSeconds);
    }
	/**
	 * Launches a new agent in the MadKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,false)</code>
	 * 
	 * @param _agent
	 *           the agent to launch.
	 * @return <ul>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent)
    {
	if (!(_agent instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.launchAgent(_agent);
    }
	/**
	 * Launches a new agent in the MadKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,withGUIManagedByTheBooter)</code>
	 * 
	 * @param _agent
	 *           the agent to launch.
	 * @param _createFrame
	 *           if <code>true</code>, the kernel will launch a JFrame for this
	 *           agent.
	 * @return <ul>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, boolean _createFrame)
    {
	if (!(_agent instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.launchAgent(_agent, _createFrame);
    }
	/**
	 * Launches a new agent in the MadKit platform. This has the same effect as
	 * <code>launchAgent(agent,timeOutSeconds,false)</code>
	 * 
	 * @param _agent
	 *           the agent to launch.
	 * @param _timeOutSeconds
	 *           time to wait the end of the agent's activation until returning a
	 *           TIMEOUT.
	 * @return <ul>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#AGENT_CRASH}</code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 *  @see madkit.kernel.AbstractAgent.ReturnCode
	 *  @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 *  @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, int _timeOutSeconds)
    {
	if (!(_agent instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.launchAgent(_agent, _timeOutSeconds);
    }
	/**
	 * Launches a new agent and returns when the agent has completed its
	 * {@link AbstractAgent#activate()} method or when
	 * <code>timeOutSeconds</code> seconds elapsed. That is, the launched agent
	 * has not finished its {@link AbstractAgent#activate()} before the time out
	 * time elapsed. Additionally, if <code>createFrame</code> is
	 * <code>true</code>, it tells to MadKit that an agent GUI should be managed
	 * by the Kernel. In such a case, the kernel takes the responsibility to
	 * assign a JFrame to the agent and to manage its life cycle (e.g. if the
	 * agent ends or is killed then the JFrame is closed) Using this feature
	 * there are two possibilities:
	 * <ul>
	 * <li>1. the agent overrides the method
	 * {@link AbstractAgent#setupFrame(JFrame)} and so setup the default JFrame
	 * as will</li>
	 * <li>2. the agent does not override it so that MadKit will setup the JFrame
	 * with the default Graphical component delivered by the MadKit platform:
	 * {@link OutputPanel}
	 * </ul>
	 * 
	 * @param _agent
	 *           the agent to launch.
	 * @param _timeOutSeconds
	 *           time to wait for the end of the agent's activation until
	 *           returning a TIMEOUT.
	 * @param _createFrame
	 *           if <code>true</code>, the kernel will launch a JFrame for this
	 *           agent.
	 * @return <ul>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code> {@link madkit.kernel.AbstractAgent.ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, int _timeOutSeconds, boolean _createFrame)
    {
	if (!(_agent instanceof MKGEAbstractAgent))
	{
	    throw new IllegalArgumentException("The agent given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	}
	return super.launchAgent(_agent, _timeOutSeconds, _createFrame);
    }
	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, Integer.MAX_VALUE, false)</code>.
	 * 
	 * @param _agentClass
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 * @throws IllegalArgumentException When the given class as parameter don't implement the interface MadKitGroupExtensionAgent.
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public madkit.kernel.AbstractAgent launchAgent(String _agentClass)
    {
	Class<?> c;
	try
	{
	    c = Class.forName(_agentClass);
	    if (!MKGEAbstractAgent.class.isAssignableFrom(c))
	    {
		throw new IllegalArgumentException("The agent class given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	    }
	}
	catch (ClassNotFoundException e)
	{
	}
	
	return super.launchAgent(_agentClass);
    }
	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, Integer.MAX_VALUE, defaultGUI)</code>.
	 * 
	 * @param _agentClass
	 *           the full class name of the agent to launch
	 * @param _createFrame
	 *           if <code>true</code> a default GUI will be associated with the
	 *           launched agent
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 * @throws IllegalArgumentException When the given class as parameter don't implement the interface MadKitGroupExtensionAgent.
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public madkit.kernel.AbstractAgent launchAgent(String _agentClass, boolean _createFrame)
    {
	Class<?> c;
	try
	{
	    c = Class.forName(_agentClass);
	    if (!MKGEAbstractAgent.class.isAssignableFrom(c))
	    {
		throw new IllegalArgumentException("The agent class given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	    }
	}
	catch (ClassNotFoundException e)
	{
	}
	
	return super.launchAgent(_agentClass, _createFrame);
    }
	/**
	 * Launches a new agent using its full class name. <br>
	 * This has the same effect as
	 * <code>launchAgent(agentClass, timeOutSeconds, false)</code>.
	 * 
	 * @param _agentClass
	 *           the full class name of the agent to launch
	 * @param _timeOutSeconds
	 *           time to wait the end of the agent's activation until returning
	 *           <code>null</code>
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 * @throws IllegalArgumentException When the given class as parameter don't implement the interface MadKitGroupExtensionAgent.
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public madkit.kernel.AbstractAgent launchAgent(String _agentClass, int _timeOutSeconds)
    {
	Class<?> c;
	try
	{
	    c = Class.forName(_agentClass);
	    if (!MKGEAbstractAgent.class.isAssignableFrom(c))
	    {
		throw new IllegalArgumentException("The agent class given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	    }
	}
	catch (ClassNotFoundException e)
	{
	}
	
	return super.launchAgent(_agentClass, _timeOutSeconds);
    }
	/**
	 * Launches a new agent using its full class name and returns when the
	 * launched agent has completed its {@link AbstractAgent#activate()} method
	 * or when the time out is elapsed. This has the same effect as
	 * {@link #launchAgent(AbstractAgent, int, boolean)} but allows to launch
	 * agent using a class name found reflexively for instance. Additionally,
	 * this method will launch the last compiled byte code of the corresponding
	 * class if it has been reloaded using
	 * {@link MadkitClassLoader#reloadClass(String)}. Finally, if the launch
	 * timely succeeded, this method returns the instance of the created agent.
	 * 
	 * @param _agentClass
	 * @param _timeOutSeconds
	 *           time to wait the end of the agent's activation until returning
	 *           <code>null</code>
	 * @param _createFrame
	 *           if <code>true</code> a default GUI will be associated with the
	 *           launched agent
	 * 
	 *           the full class name of the agent to launch
	 * @return the instance of the launched agent or <code>null</code> if the
	 *         operation times out or failed.
	 * @throws IllegalArgumentException When the given class as parameter don't implement the interface MadKitGroupExtensionAgent.
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public madkit.kernel.AbstractAgent launchAgent(String _agentClass, int _timeOutSeconds, boolean _createFrame)
    {
	Class<?> c;
	try
	{
	    c = Class.forName(_agentClass);
	    if (!MKGEAbstractAgent.class.isAssignableFrom(c))
	    {
		throw new IllegalArgumentException("The agent class given as parameter must inherit a MadKitGroupExtension agent which implements the interface MadKitGroupExtensionAgent.");
	    }
	}
	catch (ClassNotFoundException e)
	{
	}
	
	return super.launchAgent(_agentClass, _timeOutSeconds, _createFrame);
    }
    
    
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, String ..._rolesName)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, int cpuCodeNb, String ..._rolesName)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
	/**
	 * Optimizes mass agent launching. Launches <i><code>bucketSize</code></i>
	 * instances of <i><code>agentClassName</code></i> (an agent class) and put them in
	 * the artificial society at the locations defined by
	 * <code>cgrLocations</code>. Each string of the <code>cgrLocations</code>
	 * array defines a complete CGR location. So for example,
	 * <code>cgrLocations</code> could be defined and used with code such as :
	 * 
	 * <p>
	 * 
	 * <pre>
	 * launchAgentBucketWithRoles("madkitgroupextension.OneAgent", 1000000, new Role(new Group("community", "group"), "role"),new Role(new Group("anotherC", "anotherG"), "anotherR"))
	 * </pre>
	 * 
	 * In this example all the agents created by this process will have these two
	 * roles in the artificial society, even if they do not request them in their
	 * {@link #activate()} method.
	 * <p>
	 * Additionally, in order to avoid to change the code of the agent
	 * considering how they will be launched (using the bucket mode or not).
	 * One should use the following alternative of the usual request method :
	 * {@link #bucketModeRequestRole(Group, String, Object)}:
	 * If used in {@link #activate()}, these requests will be ignored when the
	 * bucket mode is used or normally proceeded otherwise.
	 * <p>
	 * 
	 * If some of the corresponding groups do not exist before this call, the
	 * caller agent will automatically become the manager of these groups.
	 * 
	 * @param _agentClassName
	 *           the name of the class from which the agents should be built.
	 * @param _bucketSize
	 *           the desired number of instances.
	 * @param cpuCoreNb the number of parallel tasks to use. 
	 * Beware that if cpuCoreNb is greater than 1, the agents' constructors and {@link #activate()} methods
	 * will be called simultaneously so that one has to be careful if shared resources are
	 * accessed by the agents
	 * @param _rolesName
	 *           default locations in the artificial society for the launched
	 *           agents. Each string of the <code>cgrLocations</code> array
	 *           defines a complete CGR location by separating C, G and R with
	 *           commas as follows: <code>"community,group,role"</code>. It can be <code>null</code>.
	 * @return a list containing all the agents which have been launched, or
	 *         <code>null</code> if the operation has failed
	 * @since MaDKit 5.0.0.6
	 * @since MadKitGroupExtension 1.3.1
	 */
    @Override public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, int cpuCoreNb, Role ... _rolesName)
    {
	try
	{
	    ArrayList<MKGEAbstractAgent> lst_mkge_agents=new ArrayList<MKGEAbstractAgent>();
	    ArrayList<madkit.kernel.AbstractAgent> lst_mk_agents=new ArrayList<madkit.kernel.AbstractAgent>();
	    createBucket(_agentClassName, _bucketSize, cpuCoreNb, lst_mkge_agents, lst_mk_agents);
	    launchAgentBucket(lst_mkge_agents, lst_mk_agents, cpuCoreNb, _rolesName);
	    return lst_mk_agents;
	}
	catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
	{
	    getLogger().severeLog(ErrorMessages.CANT_LAUNCH + " " + _agentClassName, e);
	    return null;
	}
	
    }
    
	/**
	 * Optimizes mass agent launching. Launches <i><code>bucketSize</code></i>
	 * instances of <i><code>agentClassName</code></i> (an agent class) and put them in
	 * the artificial society at the locations defined by
	 * <code>cgrLocations</code>. Each string of the <code>cgrLocations</code>
	 * array defines a complete CGR location. So for example,
	 * <code>cgrLocations</code> could be defined and used with code such as :
	 * 
	 * <p>
	 * 
	 * <pre>
	 * launchAgentBucketWithRoles("madkitgroupextension.OneAgent", 1000000, new Role(new Group("community", "group"), "role"),new Role(new Group("anotherC", "anotherG"), "anotherR"))
	 * </pre>
	 * 
	 * In this example all the agents created by this process will have these two
	 * roles in the artificial society, even if they do not request them in their
	 * {@link #activate()} method.
	 * <p>
	 * Additionally, in order to avoid to change the code of the agent
	 * considering how they will be launched (using the bucket mode or not).
	 * One should use the following alternative of the usual request method :
	 * {@link #bucketModeRequestRole(Group, String, Object)}:
	 * If used in {@link #activate()}, these requests will be ignored when the
	 * bucket mode is used or normally proceeded otherwise.
	 * <p>
	 * 
	 * If some of the corresponding groups do not exist before this call, the
	 * caller agent will automatically become the manager of these groups.
	 * 
	 * @param _agentClassName
	 *           the name of the class from which the agents should be built.
	 * @param _bucketSize
	 *           the desired number of instances.
	 * @param _rolesName
	 *           default locations in the artificial society for the launched
	 *           agents. Each string of the <code>cgrLocations</code> array
	 *           defines a complete CGR location by separating C, G and R with
	 *           commas as follows: <code>"community,group,role"</code>. It can be <code>null</code>.
	 * @return a list containing all the agents which have been launched, or
	 *         <code>null</code> if the operation has failed
	 * @since MaDKit 5.0.0.6
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, Role ... _rolesName)
    {
	return this.launchAgentBucket(_agentClassName, _bucketSize, 1, _rolesName);
    }

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public void launchAgentBucket(List<? extends madkit.kernel.AbstractAgent> bucket, int cpuCodeNb, String ...cgrLocations)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public void launchAgentBucket(List<? extends madkit.kernel.AbstractAgent> bucket, String ...cgrLocations)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
	/**
	 * Similar to <code>launchAgentBucket(String, int, Role...)</code>
	 * except that the list of agents to launch is given. Especially, this could
	 * be used when the agents have no default constructor.
	 * 
	 * @param _bucket the list of agents to launch
	 * @param _rolesName the list of the roles to give for each agent to launch
	 * @throws IllegalArgumentException One (or more) of the given group throw the class Role, represent also its subgroups.
	 * @since MadKitGroupExtension 1.0
	 */
    @Override public void launchAgentBucket(List<MKGEAbstractAgent> _bucket, Role... _rolesName)
    {
	this.launchAgentBucket(_bucket, 1, _rolesName);
    }
    
    
	/**
	 * Similar to <code>launchAgentBucket(String, int, int, Role...)</code>
	 * except that the list of agents to launch is given. Especially, this could
	 * be used when the agents have no default constructor.
	 * 
	 * @param _bucket the list of agents to launch
	 * @param cpuCoreNb the number of parallel tasks to use. 
	 * Beware that if cpuCoreNb is greater than 1, the agents' constructors and {@link #activate()} methods
	 * will be called simultaneously so that one has to be careful if shared resources are
	 * accessed by the agents
	 * @param _rolesName the list of the roles to give for each agent to launch
	 * @throws IllegalArgumentException One (or more) of the given group throw the class Role, represent also its subgroups.
	 * @since MadKitGroupExtension 1.3.1
	 */
    @Override public void launchAgentBucket(List<MKGEAbstractAgent> _bucket, int cpuCoreNb, Role... _rolesName)
    {
	ArrayList<madkit.kernel.AbstractAgent> lst_mk_agents=new ArrayList<>(_bucket.size());
	for (MKGEAbstractAgent aa : _bucket)
	{
	    lst_mk_agents.add((madkit.kernel.AbstractAgent)aa);
	}
	launchAgentBucket(_bucket, lst_mk_agents, cpuCoreNb, _rolesName);
    }
    
    private void launchAgentBucket(List<MKGEAbstractAgent> lst_mkge_agents, List<madkit.kernel.AbstractAgent> lst_mk_agents, int cpuCoreNb, Role... _rolesName)
    {
	ArrayList<String> roles=new ArrayList<String>(_rolesName.length);
	ArrayList<Role> rolesName=new ArrayList<Role>(_rolesName.length);
	HashMap<Group, Integer> groups=new HashMap<>();
	
	boolean role_ok=_rolesName!=null && _rolesName.length>0;
	
	if (role_ok)
	{
	    for (int i=0;i<_rolesName.length;i++)
	    {
		Role r=_rolesName[i];
		if (r.getGroup().isUsedSubGroups())
		    throw new IllegalArgumentException("Unable to use sub groups of the group : "+r.getGroup());
 
		boolean removed=false;
		for (String r2 : roles)
		{
		    if (r2.equals(r.toString()))
		    {
			removed=true;
			break;
		    }
		}
		if (!removed)
		{
		    if (r.getGroup().isUsedSubGroups())
			throw new IllegalArgumentException("Unable to use sub groups of the group : "+r.getGroup());

		    boolean madkitcreated=false;
		    try
		    {
			madkitcreated=((Boolean)m_is_madkit_created_method.invoke(r.getGroup(), this.getKernelAddress())).booleanValue();
		    }
		    catch (IllegalArgumentException e1)
		    {
			System.err.println("Impossible to call the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			e1.printStackTrace();
			System.exit(-1);
		    }
		    catch (IllegalAccessException e1)
		    {
			System.err.println("Impossible to call the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			e1.printStackTrace();
			System.exit(-1);
		    }
		    catch (InvocationTargetException e1)
		    {
			e1.printStackTrace();
			System.exit(-1);
		    }

		    if (!madkitcreated)
		    {
			ReturnCode rc=super.createGroup(r.getGroup().getCommunity(), r.getGroup().getPath(), r.getGroup().isMadKitDistributed(), r.getGroup().getMadKitIdentifier());
			if (rc.equals(ReturnCode.ALREADY_GROUP))
			    throw new IllegalAccessError("Problem of data integrity ! The group "+r.getGroup()+" has already be already created on MadKit. These bug is located into MadKitGroupExtension !");
		    }
		
		    roles.add(r.toString());
		    rolesName.add(r);
		    Integer val=groups.get(r.getGroup());
		    if (val==null)
			val=new Integer(0);
		    groups.put(r.getGroup(), new Integer(val.intValue()+1));
		
		}
	    }
	}
	role_ok=role_ok && roles.size()>0;
	
	String rolestab[]=null;
	if (role_ok)
	{
	    rolestab=new String[roles.size()];
	    for (int i=0;i<roles.size();i++)
		rolestab[i]=roles.get(i);
	}
	super.launchAgentBucket(lst_mk_agents, cpuCoreNb, rolestab);
	    
	if (role_ok)
	{
	    for (MKGEAbstractAgent a : lst_mkge_agents)
	    {
		ArrayList<GroupRole> gra=GroupRole.getGroupRoles(a);
		synchronized(gra)
		{
		    for (Entry<Group, Integer> e : groups.entrySet())
		    {
			GroupRole gr=new GroupRole(e.getKey());
			gr.incrementMadKitReferences(e.getValue().intValue(), this.getKernelAddress());
			gra.add(gr);
		    }
		}
		a.potentialChangementInGroups();
	    }
	}
    }
    
	/**
	 * Launch agents by parsing an XML node. The method
	 * immediately returns without waiting the end of the agents' activation, 
	 * 
	 * @param agentXmlNode the XML node
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * 
	 * @see XMLUtilities
	 */
	@Override public ReturnCode launchNode(Node agentXmlNode){
		if(logger != null)
			logger.finest("launchNode "+XMLUtilities.nodeToString(agentXmlNode));
		final NamedNodeMap namesMap = agentXmlNode.getAttributes();
		try {
			ArrayList<MKGEAbstractAgent> list_mkge_aa = new ArrayList<>();
			ArrayList<madkit.kernel.AbstractAgent> list_mk_aa = new ArrayList<>();
			int nbOfInstances = 1;
			try {
				nbOfInstances = Integer.parseInt(namesMap.getNamedItem(XMLUtilities.NB_OF_INSTANCES).getNodeValue());
			} catch (NullPointerException e) {
			}
			
			this.createBucket(namesMap.getNamedItem(XMLUtilities.CLASS).getNodeValue(), nbOfInstances, 1, list_mkge_aa, list_mk_aa);
			
			
			//required for bucket mode with no roles
			boolean bucketMode = false;
			try {
				bucketMode = Boolean.parseBoolean(namesMap.getNamedItem(XMLUtilities.BUCKET_MODE).getNodeValue());
			} catch (NullPointerException e) {
			}
			
			NodeList attributes = agentXmlNode.getChildNodes();
			List<Role> roles= new ArrayList<>();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				switch (node.getNodeName()) {
				case XMLUtilities.ATTRIBUTES:
					NamedNodeMap att = node.getAttributes();
					final Class<? extends madkit.kernel.AbstractAgent> MKagentClass = list_mk_aa.get(0).getClass();
					for (int j = 0; j < att.getLength(); j++) {
						Node item = att.item(j);
						setAgentValues(madkit.kernel.Probe.findFieldOn(MKagentClass, item.getNodeName()),item.getNodeValue(),list_mkge_aa);
					}
					break;
				case XMLUtilities.BUCKET_MODE_ROLE:
					bucketMode = true;
					NamedNodeMap roleAttributes = node.getAttributes();
					roles.add(new Role(Group.getGroupFromPath(roleAttributes.item(0).getNodeValue(), roleAttributes.item(1).getNodeValue()), roleAttributes.item(2).getNodeValue()));
					break;
				default:
					break;
				}
			}
			
			if (bucketMode) {
			    launchAgentBucket(list_mkge_aa, list_mk_aa, 1, roles.toArray(new Role[roles.size()]));
			}
			else{
				try {
					Level logLevel = Level.parse(namesMap.getNamedItem(XMLUtilities.LOG_LEVEL).getNodeValue());
					for (MKGEAbstractAgent abstractAgent : list_mkge_aa) {
						abstractAgent.setLogLevel(logLevel);
					}
				} catch (NullPointerException e) {
				}
				
				boolean guiMode = false;
				try {
					guiMode = Boolean.parseBoolean(namesMap.getNamedItem(XMLUtilities.GUI).getNodeValue());
				} catch (NullPointerException e) {
				}
				for (final madkit.kernel.AbstractAgent abstractAgent : list_mk_aa) {
					launchAgent(abstractAgent, 0, guiMode);
				}
			}
		} catch (NullPointerException | ClassNotFoundException | NoSuchFieldException | NumberFormatException | InstantiationException | IllegalAccessException e) {
			getLogger().severeLog("launchNode "+ Words.FAILED+" : "+XMLUtilities.nodeToString(agentXmlNode),e);
			return ReturnCode.SEVERE;
		}
		return ReturnCode.SUCCESS;
	}

	/**
	 * @param stringValue
	 * @param type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void setAgentValues(final Field f, final String stringValue, List<MKGEAbstractAgent> l) throws IllegalAccessException {
		final Class<?> type = f.getType();
		if(type.isPrimitive()){
			if (type == int.class){
				int value = Integer.parseInt(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setInt(a, value);
				}
			}
			else if(type == boolean.class){
				boolean value = Boolean.parseBoolean(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setBoolean(a, value);
				}
			}
			else if (type == float.class){
				float value = Float.parseFloat(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setFloat(a, value);
				}
			}
			else if (type == double.class){
				double value = Double.parseDouble(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setDouble(a, value);
				}
			}
			else if (type == byte.class){
				byte value = Byte.parseByte(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setByte(a, value);
				}
			}
			else if (type == short.class){
				short value = Short.parseShort(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setShort(a, value);
				}
			}
			else if (type == long.class){
				long value = Long.parseLong(stringValue);
				for (MKGEAbstractAgent a : l) {
					f.setLong(a, value);
				}
			}
		}
		else if (type == Integer.class){
			int value = Integer.parseInt(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Integer(value));
			}
		}
		else if(type == Boolean.class){
			boolean value = Boolean.parseBoolean(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Boolean(value));
			}
		}
		else if (type == Float.class){
			float value = Float.parseFloat(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Float(value));
			}
		}
		else if (type == Double.class){
			double value = Double.parseDouble(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Double(value));
			}
		}
		else if (type == String.class){
			for (MKGEAbstractAgent a : l) {
				f.set(a, stringValue);
			}
		}
		else if (type == Byte.class){
			byte value = Byte.parseByte(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Byte(value));
			}
		}
		else if (type == Short.class){
			short value = Short.parseShort(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Short(value));
			}
		}
		else if (type == Long.class){
			long value = Long.parseLong(stringValue);
			for (MKGEAbstractAgent a : l) {
				f.set(a, new Long(value));
			}
		}
		else{
			if(logger != null)
				logger.severe("Do not know how to change attrib "+stringValue);
		}
	}
    
    
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.3.1 
     */
    @Override @Deprecated public ReturnCode bucketModeCreateGroup(final String community, final String group, boolean isDistributed, final Gatekeeper keyMaster) {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.bucketModeRequestRole(new Group(_community, _group), _role, passKey)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Override @Deprecated public ReturnCode bucketModeRequestRole(final String community, final String group, final String role, final Object passKey) {
	return this.bucketModeRequestRole(new Group(community, group), role, passKey);
    }

    
	/**
	 * Requests a role even if the agent has been launched 
	 * using one of the <code>launchAgentBucket</code> methods with non <code>null</code>
	 * roles. 
	 * 
	 * For instance, this is useful if you launch one million of agents and when only some of them 
	 * have to take a specific role which cannot be defined in the parameters of {@link #launchAgentBucket(List, int, Role...)}
	 * because they are priorly unknown and build at runtime.
	 * 
	 * @param _group
	 *           the targeted group.
	 * @param role
	 *           the desired role.
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         </li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * 
	 * @since MaDKit 5.0
	 * @since MadKitGroupExtension 1.3.1
	 */
@Override public ReturnCode bucketModeRequestRole(Group _group, final String role) {
	return bucketModeRequestRole(_group, role, null);
}
	/**
	 * Requests a role even if the agent has been launched 
	 * using one of the <code>launchAgentBucket</code> methods with non <code>null</code>
	 * roles. 
	 * 
	 * For instance, this is useful if you launch one million of agents and when only some of them 
	 * have to take a specific role which cannot be defined in the parameters of {@link #launchAgentBucket(List, int, Role...)}
	 * because they are priorly unknown and build at runtime.
	 * 
	 * @param _group
	 *           the targeted group.
	 * @param role
	 *           the desired role.
	 * @param passKey
	 *           the <code>passKey</code> to enter a secured group. It is
	 *           generally delivered by the group's <i>group manager</i>. It
	 *           could be <code>null</code>, which is sufficient to enter an
	 *           unsecured group. Especially,
	 *           {@link #requestRole(Group, String)} uses a <code>null</code>
	 *           <code>passKey</code>.
	 * @return <ul>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ROLE_ALREADY_HANDLED}</code>: If this
	 *         role is already handled by this agent.</li>
	 *         <li><code>{@link madkit.kernel.AbstractAgent.ReturnCode#ACCESS_DENIED}</code>: If the access
	 *         denied by the manager of that secured group.</li>
	 *         </li>
	 *         </ul>
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Gatekeeper
	 * 
	 * @since MaDKit 5.0
	 * @since MadKitGroupExtension 1.3.1
	 */
    @Override public ReturnCode bucketModeRequestRole(Group _group, final String role, final Object passKey) {
	return this.requestRole(_group, role, passKey, true);
    }

    private ThreadPoolExecutor getServiceExecutor()
    {
	    
	try
	{
	    Object kernel=m_get_kernel_method.invoke(this);
	    if (kernel==null)
		return null;
		
	    if (kernel.getClass().getName().equals("madkit.kernel.LoggedKernel"))
	    {
		Method m=kernel.getClass().getDeclaredMethod("getMadkitKernel");
		m.setAccessible(true);
		kernel=m.invoke(kernel);
		if (kernel==null)
		    return null;
	    }
	    Field f=kernel.getClass().getDeclaredField("serviceExecutor");
	    f.setAccessible(true);
	    return (ThreadPoolExecutor)f.get(kernel);
	}
	catch (Exception e)
	{
	    System.err.println("Impossible to access to the field ThreadPoolExecutor of the class madkit.kernel.AbstractAgent. This is an inner bug of MadKitLanExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	    return null;
	}
    }
    
    private void createBucket(final String agentClass, int bucketSize, int cpuCoreNb, ArrayList<MKGEAbstractAgent> lst_mkge_agents, ArrayList<madkit.kernel.AbstractAgent> lst_mk_agents) throws InstantiationException, IllegalAccessException, ClassNotFoundException 
    {
	@SuppressWarnings("unchecked")
	final Class<? extends MKGEAbstractAgent> constructor = (Class<? extends MKGEAbstractAgent>) MadkitClassLoader.getLoader().loadClass(agentClass);
	cpuCoreNb = cpuCoreNb > 0 ? cpuCoreNb : 1;
	lst_mk_agents.ensureCapacity(bucketSize);
	lst_mkge_agents.ensureCapacity(bucketSize);
	final int nbOfAgentsPerTask = bucketSize / (cpuCoreNb);
	
	class DoubleList
	{
	    public final List<MKGEAbstractAgent> lst_mkge_agents;
	    public final List<madkit.kernel.AbstractAgent> lst_mk_agents;
	    
	    public DoubleList(int capacity)
	    {
		lst_mkge_agents=new ArrayList<>(capacity);
		lst_mk_agents=new ArrayList<>(capacity);
	    }
	}
	
	final CompletionService<DoubleList> ecs = new ExecutorCompletionService<>(getServiceExecutor());
	for (int i = 0; i < cpuCoreNb; i++) {
	    ecs.submit(new Callable<DoubleList>() {
		public DoubleList call() throws InstantiationException, IllegalAccessException {
		    final DoubleList list = new DoubleList(nbOfAgentsPerTask);
		    for (int j = nbOfAgentsPerTask; j > 0; j--) {
			MKGEAbstractAgent aa=constructor.newInstance();
			list.lst_mkge_agents.add(aa);
			list.lst_mk_agents.add((madkit.kernel.AbstractAgent)aa);
		    }
		    return list;
		}
	    });
	}
	// adding the missing ones when the division results as a real number
	for (int i = bucketSize - nbOfAgentsPerTask * cpuCoreNb; i > 0; i--) {
	    MKGEAbstractAgent aa=constructor.newInstance();
	    lst_mk_agents.add((madkit.kernel.AbstractAgent)aa);
	    lst_mkge_agents.add(aa);
	}
	for (int i = 0; i < cpuCoreNb; ++i) {
	    try {
		DoubleList lst=ecs.take().get();
		lst_mk_agents.addAll(lst.lst_mk_agents);
		lst_mkge_agents.addAll(lst.lst_mkge_agents);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (ExecutionException e) {
		e.printStackTrace();
	    }
	}
    }
    
    protected static Method m_is_madkit_created_method=null;
    protected static Method m_get_kernel_method=null;
    static
    {
	try
	{
	    m_is_madkit_created_method=Group.class.getDeclaredMethod("isMadKitCreated", KernelAddress.class);
	    m_is_madkit_created_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function isMadKitCreated of the class Group. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_get_kernel_method=madkit.kernel.AbstractAgent.class.getDeclaredMethod("getKernel");
	    m_get_kernel_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function getKernel of the class madkit.kernel.AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function getKernel of the class madkit.kernel.AbstractAgent. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	
    }    

    /*
     * 
     * 
     * Agent code
     * 
     * 
     */

    /**
     * This function is deprecated and has the same effect that <code>this.broadcastMessageWithRoleAndWaitForReplies(new Group(community, group), role, message, senderRole, timeOutMilliSeconds)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public List<Message> broadcastMessageWithRoleAndWaitForReplies(String community, String group, String role, Message message, String senderRole, Integer timeOutMilliSeconds)
    {
	return this.broadcastMessageWithRoleAndWaitForReplies(new Group(community, group), role, message, senderRole, timeOutMilliSeconds);
    }
	/**
	 * Broadcasts a message and wait for answers considering a timeout duration.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _roleName
	 *           the role name
	 * @param _message
	 * 	     the message
	 * @param _senderRole
	 * @param _timeOutMilliSeconds
	 * @return a list of messages which are answers to the <code>message</code> which has been broadcasted.
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 */
    @Override public List<Message> broadcastMessageWithRoleAndWaitForReplies(Group _group, String _roleName, Message _message, String _senderRole, Integer _timeOutMilliSeconds)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);
	return super.broadcastMessageWithRoleAndWaitForReplies(_group.getCommunity(), _group.getPath(), _roleName,_message, _senderRole, _timeOutMilliSeconds);
    }

    /**
     * This function is deprecated and has the same effect that <code>this.sendMessageAndWaitForReply(new Group(community, group), role, messageToSend)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public Message sendMessageAndWaitForReply(String community, String group, String role, Message messageToSend)
    {
	return this.sendMessageAndWaitForReply(new Group(community, group), role, messageToSend);
    }
    
	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, null)</code>
	 * @param _group
	 *           the group and the community name
	 * @param _role the role name
	 * @param _messageToSend the message to send.
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> 
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(Group, String, Message, String, Integer)
	 * @since MadKit 5
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 */
    @Override public Message sendMessageAndWaitForReply(Group _group, String _role, Message _messageToSend)
    {
	return this.sendMessageWithRoleAndWaitForReply(_group, _role, _messageToSend, null, null);
    }
    
    
    /**
     * This function is deprecated and has the same effect that <code>this.sendMessageAndWaitForReply(new Group(community, group), role, messageToSend, timeOutMilliSeconds)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public Message sendMessageAndWaitForReply(String community, String group, String role, Message messageToSend, int timeOutMilliSeconds)
    {
	return this.sendMessageAndWaitForReply(new Group(community, group), role, messageToSend, timeOutMilliSeconds);
    }
	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, null, timeOutMilliSeconds)</code>
	 * @param _group
	 *           the group and the community name
	 * @param _role the role name
	 * @param _messageToSend the message to send.
	 * @param _timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(Group, String, Message, String, Integer)
	 * @since MadKit 5
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 */
    @Override public Message sendMessageAndWaitForReply(Group _group, String _role, Message _messageToSend, int _timeOutMilliSeconds)
    {
	return this.sendMessageWithRoleAndWaitForReply(_group, _role, _messageToSend, null, new Integer(_timeOutMilliSeconds));
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.sendMessageWithRoleAndWaitForReply(new Group(community, group), role, messageToSend, senderRole)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public Message sendMessageWithRoleAndWaitForReply(String community, String group, String role, Message messageToSend, String senderRole)
    {
	return this.sendMessageWithRoleAndWaitForReply(new Group(community, group), role, messageToSend, senderRole);
    }
	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(group, role, messageToSend, senderRole, null)</code>
	 * @param _group the group and the community name
	 * @param _role the role name
	 * @param _messageToSend the message to send.
	 * @param _senderRole the role with which the sending is done.
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code>
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(Group, String, Message, String, Integer)
	 * @since MadKit 5
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 */
    @Override public Message sendMessageWithRoleAndWaitForReply(Group _group, String _role, Message _messageToSend, String _senderRole)
    {
	return this.sendMessageWithRoleAndWaitForReply(_group, _role, _messageToSend, _senderRole, null);
    }
    
    /**
     * This function is deprecated and has the same effect that <code>this.sendMessageWithRoleAndWaitForReply(new Group(community, group), role, messageToSend, senderRole, timeOutMilliSeconds)</code>.
     * @since MadKitGroupExtension 1.0 
     */
    @Deprecated @Override public Message sendMessageWithRoleAndWaitForReply(String community, String group, String role, Message messageToSend, String senderRole, Integer timeOutMilliSeconds)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
	/**
	 * Sends a message to an agent having this position in the organization
	 * and waits for an answer to it.
	 * The targeted agent is selected randomly among matched agents.
	 * The sender is excluded from this search.
	 * @param _group the group and the community name
	 * @param _role the role name
	 * @param _messageToSend the message to send.
	 * @param _senderRole the role with which the sending is done.
	 * @param _timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @since MadKit 5
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKitGroupExtension 1.0
	 * @see Group
	 */
    @Override public Message sendMessageWithRoleAndWaitForReply(Group _group, String _role, Message _messageToSend, String _senderRole, Integer _timeOutMilliSeconds)
    {
	if (_group.isUsedSubGroups())
	    throw new IllegalArgumentException("Unable to use sub groups of the group : "+_group);
	return super.sendMessageWithRoleAndWaitForReply(_group.getCommunity(), _group.getPath(), _role, _messageToSend, _senderRole, _timeOutMilliSeconds);
    }
    
	/**
	 * Stops the agent's process for a while.
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	@Override public void pause(final int milliSeconds)
	{
	    super.pause(milliSeconds);
	}
    
    
    /*
     * 
     * Scheduler Code
     * 
     * 
     */
    /**
     * List of activators
     */
    protected ArrayList<madkitgroupextension.kernel.Activator<?>> m_activators=new ArrayList<madkitgroupextension.kernel.Activator<?>>();
    
	/**
	 * Executes all the activators in the order they have been added, using the
	 * and increment the global virtual time of this scheduler by one unit. This
	 * method should be overridden to define customized scheduling policy. So
	 * default implementation is :
	 * 
	 * <pre>
	 * <tt>@Override</tt>
	 * public void doSimulationStep() {
	 * 	if (logger != null) {
	 * 		logger.finer("Doing simulation step "+GVT);
	 * 	}
	 * 	for (final Activator<?> activator : m_activators) {
	 * 		triggerActivator(activator);
	 * 	}
	 * 	setGVT(getGVT() + 1);
	 * }
	 * </pre>
	 */
    @Override public void doSimulationStep()
    {
	synchronized(m_activators)
	{
		for (madkitgroupextension.kernel.Activator<?> a : this.m_activators)
		{
		    triggerActivator(a);
		}
	}
	super.doSimulationStep();
    }
    
    private void triggerActivator(final madkitgroupextension.kernel.Activator<?> activator) {
	    try
	    {
		m_update_changement_in_groups_method.invoke(activator);
	    }
	    catch (IllegalArgumentException e1)
	    {
		System.err.println("Impossible to call the function updateChangementInGroups of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e1.printStackTrace();
		System.exit(-1);
	    }
	    catch (IllegalAccessException e1)
	    {
		System.err.println("Impossible to call the function updateChangementInGroups of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
		e1.printStackTrace();
		System.exit(-1);
	    }
	    catch (InvocationTargetException e1)
	    {
		e1.printStackTrace();
		System.exit(-1);
	    }	
    }
    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0
     */
    @Override @Deprecated public final void addActivator(madkit.kernel.Activator<? extends madkit.kernel.AbstractAgent> activator)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    @SuppressWarnings("unused")
    private final void addPrivateActivator(madkit.kernel.Activator<? extends  madkit.kernel.AbstractAgent> activator)
    {
	super.addActivator(activator);
    }

	/**
	 * Adds an activator to the kernel engine. This has to be done to make an
	 * activator work properly
	 * 
	 * @param activator
	 *           an activator.
	 */
    public void addActivator(madkitgroupextension.kernel.Activator<?> activator)
    {
	synchronized(m_activators)
	{
	    m_activators.add(activator);
	}
	try
	{
	    m_associate_scheduler_method.invoke(activator, this);
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function associateScheduler of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function associateScheduler of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    e1.printStackTrace();
	    System.exit(-1);
	}
	activator.initialize();
    }

    /**
     * @throws IllegalAccessError when the function is called.
     * @since MadKitGroupExtension 1.0
     */
    @Override @Deprecated public final void removeActivator(madkit.kernel.Activator<? extends madkit.kernel.AbstractAgent> activator)
    {
	throw new IllegalAccessError("This method is deprecated, and connot be called");
    }
    
    @SuppressWarnings("unused")
    private final void removePrivateActivator(madkit.kernel.Activator<? extends madkit.kernel.AbstractAgent> activator)
    {
	super.removeActivator(activator);
    }
    
	/**
	 * Removes an activator from the kernel engine.
	 * 
	 * @param activator
	 *           an activator.
	 */
    public void removeActivator(madkitgroupextension.kernel.Activator<?> activator)
    {
	synchronized(m_activators)
	{
	    m_activators.remove(activator);
	}
	try
	{
	    m_remove_activators_method.invoke(activator);
	}
	catch (IllegalArgumentException e1)
	{
	    System.err.println("Impossible to call the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (IllegalAccessException e1)
	{
	    System.err.println("Impossible to call the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
	    e1.printStackTrace();
	    System.exit(-1);
	}
	catch (InvocationTargetException e1)
	{
	    e1.printStackTrace();
	    System.exit(-1);
	}
	
    }
    
    
	/**
	 * Removes all activators contained on this kernel engine.
	 * 
	 */
    @Override public void removeAllActivators()
    {
	synchronized(m_activators)
	{
		for (madkitgroupextension.kernel.Activator<?> a : m_activators)
		{
			try
			{
			    m_remove_activators_method.invoke(a);
			}
			catch (IllegalArgumentException e1)
			{
			    System.err.println("Impossible to call the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			    e1.printStackTrace();
			    System.exit(-1);
			}
			catch (IllegalAccessException e1)
			{
			    System.err.println("Impossible to call the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next exception :");
			    e1.printStackTrace();
			    System.exit(-1);
			}
			catch (InvocationTargetException e1)
			{
			    e1.printStackTrace();
			    System.exit(-1);
			}
		}
		m_activators.clear();
	}
    }
    
	/**
	 * Changes the state of the scheduler
	 * 
	 * @param newState
	 *           the new state
	 */
	@Override public void setSimulationState(final SimulationState newState)
	{
	    super.setSimulationState(newState);
	}
    
	@Override public ArrayList<madkitgroupextension.kernel.Activator<?>> getActivators()
	{
	    return m_activators;
	}
    
    private static Method m_associate_scheduler_method=null;
    private static Method m_remove_activators_method=null;
    private static Method m_update_changement_in_groups_method=null;
    static
    {
	try
	{
	    m_associate_scheduler_method=Activator.class.getDeclaredMethod("associateScheduler", Scheduler.class);
	    m_associate_scheduler_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function associateScheduler of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function associateScheduler of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_remove_activators_method=Activator.class.getDeclaredMethod("removeActivators");
	    m_remove_activators_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function removeActivators of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	try
	{
	    m_update_changement_in_groups_method=Activator.class.getDeclaredMethod("updateChangementInGroups");
	    m_update_changement_in_groups_method.setAccessible(true);
	}
	catch (SecurityException e)
	{
	    System.err.println("Impossible to access to the function updateChangementInGroups of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	catch (NoSuchMethodException e)
	{
	    System.err.println("Impossible to found to the function updateChangementInGroups of the class Activator. This is an inner bug of MadKitGroupExtension. Please contact the developers. Impossible to continue. See the next error :");
	    e.printStackTrace();
	    System.exit(-1);
	}
	
    }
}
