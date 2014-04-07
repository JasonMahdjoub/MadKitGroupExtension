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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import madkit.kernel.AgentAddress;
import madkit.kernel.Gatekeeper;
import madkit.kernel.KernelAddress;

/**
 * MadKitGroupExtension aims to encapsulate MadKit in order to extends the agent/group/role principle
 * by giving the possibility to the user to work with a hierarchy of groups. So one group can have one or more subgroups. 
 * These last groups can have also subgroups, etc. Such groups are represented by the class {@link Group}.
 * One group can represent itself, but also its subgroups already handled by MadKit.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see AbstractGroup
 * @see MultiGroup
 */
public final class Group extends AbstractGroup
{
    /**
     * 
     */
    private static final long serialVersionUID = 2926497540140504602L;
    
    private transient GroupTree m_group;
    private transient boolean m_use_sub_groups;
    private transient GroupTree[] m_sub_groups_tree=null;
    private transient GroupTree[] m_parent_groups_tree=null;
    private transient Group[] m_sub_groups=null;
    private transient Group[] m_parent_groups=null;
    
    private transient Group[] m_represented_groups=null;
    
    
    /**
     * Construct a group within a community and a path of groups.
     * This constructor has the same effect than <code>Group(false, false, null, false, _community, _groups)</code>.
     * 
     * Here a typical example :
     * <p>
     * <pre>
     * Group g=new Group("My community", "My group", "My subgroup 1", "My subgroup 2");
     * </pre>
     * The created group "My subgroup 2" is contained into the group "My subgroup 1" 
     * which is contained on the group "My group", which is contained into the community "My community".
     * 
     * Through the function {@link #getPath()}, you can observe the used String path (containing '/' characters) into MadKit for this group. 
     * To convert a String path to a Group class, use the function {@link #getGroupFromPath(String, String)} or this constructor as follows :
     * <p>
     * <pre>
     * String MyPath="/My group/My subgroup 1/My subgroup 2"
     * Group g=new Group("My community", MyPath);
     * </pre>
     * 
     * This is the only way to introduce a '/' character into this constructor.
     *  
     * @param _community the community
     * @param _groups the path of groups 
     * @throws IllegalArgumentException if a group name is empty, or if a group name contains a ';' character.
     *  @since MadKitGroupExtension 1.0
     *  @see #Group(boolean, boolean , Gatekeeper, String, String...)
     */
    public Group(String _community, String ..._groups){
	this(false, false, null, _community, _groups);
    }
    
    /**
     * Construct a group within a community and a path of groups.
     * This constructor has the same effect than <code>Group(_useSubGroups, false, null, false, _community, _groups)</code>.
     * 
     * Here a typical example :
     * <p>
     * <pre>
     * Group g=new Group(false, "My community", "My group", "My subgroup 1", "My subgroup 2");
     * </pre>
     * The created group "My subgroup 2" is contained into the group "My subgroup 1" 
     * which is contained on the group "My group", which is contained into the community "My community". 
     * The obtained group does not represent its subgroups.  
     * 
     * Through the function {@link #getPath()}, you can observe the used String path (containing '/' characters) into MadKit for this group. 
     * To convert a String path to a Group class, use the function {@link #getGroupFromPath(String, String)} or this constructor as follows :
     * <p>
     * <pre>
     * String MyPath="/My group/My subgroup 1/My subgroup 2"
     * Group g=new Group(false, "My community", MyPath);
     * </pre>
     * 
     * This is the only way to introduce a '/' character into this constructor.
     *  
     * @param _useSubGroups is set to true, the current group will represent itself, but also its subgroups.
     * 		When using subgroups, an activator (for example) can handle a set of agents which are part of this group 
     * 		and its subgroups.	
     * 		see {@link #getRepresentedGroups(KernelAddress)} for more information.
     * @param _community the community
     * @param _groups the path of groups 
     * @throws IllegalArgumentException if a group name is empty, or if a group name contains a ';' character.
     *  @since MadKitGroupExtension 1.0
     *  @see #Group(boolean, boolean , Gatekeeper, String, String...)
     */
    public Group(boolean _useSubGroups, String _community, String ..._groups)
    {
	this(_useSubGroups, false, null, _community, _groups);
    }
    /**
     * Construct a group within a community and a path of groups.
     * This constructor has the same effect than <code>Group(_useSubGroups, isDistributed, null, false, _community, _groups)</code>.
     * 
     * Here a typical example :
     * <p>
     * <pre>
     * Group g=new Group(false, false, "My community", "My group", "My subgroup 1", "My subgroup 2");
     * </pre>
     * The created group "My subgroup 2" is contained into the group "My subgroup 1" 
     * which is contained on the group "My group", which is contained into the community "My community". 
     * The obtained group does not represent its subgroups. He is not distributed through several instances 
     * of MadKit into a network.
     * 
     * Through the function {@link #getPath()}, you can observe the used String path (containing '/' characters) into MadKit for this group. 
     * To convert a String path to a Group class, use the function {@link #getGroupFromPath(String, String)} or this constructor as follows :
     * <p>
     * <pre>
     * String MyPath="/My group/My subgroup 1/My subgroup 2"
     * Group g=new Group(false, false, "My community", MyPath);
     * </pre>
     * 
     * This is the only way to introduce a '/' character into this constructor.
     *  
     * @param _useSubGroups is set to true, the current group will represent itself, but also its subgroups.
     * 		When using subgroups, an activator (for example) can handle a set of agents which are part of this group 
     * 		and its subgroups.	
     * 		see {@link #getRepresentedGroups(KernelAddress)} for more information.
     * @param _isDistributed tell if the group is distributed through several instances of MadKit into a network.
     * @param _community the community
     * @param _groups the path of groups 
     * @throws IllegalArgumentException if a group name is empty, or if a group name contains a ';' character.
     *  @since MadKitGroupExtension 1.0
     *  @see #Group(boolean, boolean, madkit.kernel.Gatekeeper, String, String...)
     */
    public Group(boolean _useSubGroups, boolean _isDistributed, String _community, String ..._groups)
    {
	this(_useSubGroups, _isDistributed, null, _community, _groups);
    }
    
    /**
     * Construct a group within a community and a path of groups.
     * This constructor has the same effect than <code>Group(_useSubGroups, isDistributed, _theIdentifier, false, _community, _groups)</code>.
     * 
     * Here a typical example :
     * <p>
     * <pre>
     * Group g=new Group(false, false, null, "My community", "My group", "My subgroup 1", "My subgroup 2");
     * </pre>
     * The created group "My subgroup 2" is contained into the group "My subgroup 1" 
     * which is contained on the group "My group", which is contained into the community "My community". 
     * The obtained group does not represent its subgroups. He is not distributed through several instances 
     * of MadKit into a network. No Gatekeeper is given.
     * 
     * Through the function {@link #getPath()}, you can observe the used String path (containing '/' characters) into MadKit for this group. 
     * To convert a String path to a Group class, use the function {@link #getGroupFromPath(String, String)} or this constructor as follows :
     * <p>
     * <pre>
     * String MyPath="/My group/My subgroup 1/My subgroup 2"
     * Group g=new Group(false, false, null, "My community", MyPath);
     * </pre>
     * 
     * This is the only way to introduce a '/' character into this constructor. 
     * 
     * @param _useSubGroups is set to true, the current group will represent itself, but also its subgroups.
     * 		When using subgroups, an activator (for example) can handle a set of agents which are part of this group 
     * 		and its subgroups.	
     * 		see {@link #getRepresentedGroups(KernelAddress)} for more information.
     * @param _isDistributed tell if the group is distributed through several instances of MadKit into a network.
     * @param _theIdentifier
     *           any object that implements the {@link Gatekeeper} interface. If
     *           not <code>null</code>, this object will be used to check if an
     *           agent can be admitted in the group. When this object is null,
     *           there is no group access control.
     * @param _community the community
     * @param _groups the path of groups 
     * @throws IllegalArgumentException if a group name is empty, or if a group name contains a ';' character. 
     *  @since MadKitGroupExtension 1.0
     *  @see madkit.kernel.Gatekeeper
     */
    
    public Group(boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier, String _community, String ..._groups)
    {
	this(_useSubGroups, _isDistributed, _theIdentifier, false, _community, _groups);
    }
    
    /**
     * Construct a group within a community and a path of groups.
     *      * 
     * Here a typical example :
     * <p>
     * <pre>
     * Group g=new Group(false, null, false, "My community", "My group", "My subgroup 1", "My subgroup 2");
     * </pre>
     * The created group "My subgroup 2" is contained into the group "My subgroup 1" 
     * which is contained on the group "My group", which is contained into the community "My community". 
     * The obtained group does not represent its subgroups. He is not distributed through several instances 
     * of MadKit into a network. No Gatekeeper is given.
     * 
     * Through the function {@link #getPath()}, you can observe the used String path (containing '/' characters) into MadKit for this group. 
     * To convert a String path to a Group class, use the function {@link #getGroupFromPath(String, String)} or this constructor as follows :
     * <p>
     * <pre>
     * String MyPath="/My group/My subgroup 1/My subgroup 2"
     * Group g=new Group(false, false, null, "My community", MyPath);
     * </pre>
     * 
     * This is the only way to introduce a '/' character into this constructor. 
     * 
     * @param _isDistributed tell if the group is distributed through several instances of MadKit into a network.
     * @param _isReserved tell if the group does not authorize other instances with the same group
     * @param _theIdentifier
     *           any object that implements the {@link Gatekeeper} interface. If
     *           not <code>null</code>, this object will be used to check if an
     *           agent can be admitted in the group. When this object is null,
     *           there is no group access control.
     * @param _community the community
     * @param _groups the path of groups 
     * @throws IllegalArgumentException if a group name is empty, or if a group name contains a ';' character. 
     *  @since MadKitGroupExtension 1.0
     *  @see madkit.kernel.Gatekeeper
     */
    public Group(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community, String ..._groups)
    {
	this(false, _isDistributed, _theIdentifier, _isReserved, _community, _groups);
    }
    
    private Group(boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community, String ..._groups)
    {
	if (_groups.length==1 && _groups[0].contains("/"))
	    _groups=getGroupsStringFromPath(_groups[0]);
	m_group=getRoot(_community).getGroup(_isDistributed, _theIdentifier, _isReserved, _groups);
	    
	m_use_sub_groups=_useSubGroups;
	if (!m_use_sub_groups)
	{
	    m_represented_groups=new Group[0];
	    //m_represented_groups[0]=this;
	}
	if (m_group.isMadKitDistributed() && !_isDistributed)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared as not distributed, whereas previous declarations of the same group were distributed. So the current created group is distributed !");
	if (!m_group.isMadKitDistributed() && _isDistributed)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared as distributed, whereas previous declarations of the same group were not distributed. So the current created group is not distributed !");
	if (m_group.getMadKitIdentifier()!=_theIdentifier)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared with an identifier ("+_theIdentifier+") which is not the same than the one declared with the previous same declared groups. So the current created group has the previous declared MadKit identifier ("+m_group.getMadKitIdentifier()+") !");
    }
    Group(GroupTree _g)
    {
	this(_g, false, true);
    }
    Group(GroupTree _g, boolean _use_sub_groups, boolean increase)
    {
	m_group=_g;
	m_use_sub_groups=_use_sub_groups;
	if (increase)
	    m_group.incrementReferences();
	if (!m_use_sub_groups)
	{
	    m_represented_groups=new Group[1];
	    m_represented_groups[0]=this;
	}
	
    }
    @Override public final void finalize()
    {
	m_group.decrementReferences();
    }
    
    @Override public int hashCode()
    {
	return getPath().hashCode();
    }

    @Override public Group clone()
    {
	return this;
    }
    
    
    @SuppressWarnings("unused")
    private  void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException {

	String com=ois.readUTF();
	String path=ois.readUTF();
	this.m_use_sub_groups=ois.readBoolean();
	boolean dist=ois.readBoolean();
	boolean isReserved=ois.readBoolean();
	this.m_parent_groups=null;
	this.m_parent_groups_tree=null;
	this.m_represented_groups=null;
	this.m_sub_groups=null;
	this.m_sub_groups_tree=null;
	
	m_group=getRoot(com).getGroup(dist, null, isReserved, getGroupsStringFromPath(path));
	    
	if (!m_use_sub_groups)
	{
	    m_represented_groups=new Group[0];
	}
	if (m_group.isMadKitDistributed() && !dist)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared as not distributed, whereas previous declarations of the same group were distributed. So the current created group is distributed !");
	if (!m_group.isMadKitDistributed() && dist)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared as distributed, whereas previous declarations of the same group were not distributed. So the current created group is not distributed !");
	if (m_group.getMadKitIdentifier()!=null)
	    System.err.println("[GROUP WARNING] The current created group ("+this+") have be declared with an identifier ("+null+") which is not the same than the one declared with the previous same declared groups. So the current created group has the previous declared MadKit identifier ("+m_group.getMadKitIdentifier()+") !");
   }

    private  void writeObject(ObjectOutputStream oos)
    throws IOException {

	oos.writeUTF(this.getCommunity());
	oos.writeUTF(this.getPath());
	oos.writeBoolean(this.m_use_sub_groups);
	oos.writeBoolean(this.isMadKitDistributed());
	oos.writeBoolean(isReserved());
   }    
    
    public boolean isReserved()
    {
	return this.m_group.isReserved();
    }
    
    /**
     * Returns the parent group of this group.
     * @return the parent group or null if the parent group is reserved.
     * @since MadKitGroupExtension 1.0
     */
    public Group getParent()
    {
	GroupTree p=m_group.getParent();
	if (p==null)
	    return null;
	else if (p.isReserved())
	    return null;
	else
	    return new Group(m_group.getParent());
    }
    
    /**
     * Returns the parent group of this group. This parent group will represent also all its subgroups.
     * @return the parent group or null if the parent group is reserved.
     * @since MadKitGroupExtension 1.0
     */
    public Group getParentWithItsSubGroups()
    {
	GroupTree p=m_group.getParent();
	if (p==null)
	    return null;
	else if (p.isReserved())
	    return null;
	else
	    return new Group(m_group.getParent(), true, true);
    }
    
    /**
     * Return the community of this group.
     * @return the community of this group.
     * @since MadKitGroupExtension 1.0
     */
    public String getCommunity()
    {
	return m_group.getCommunity();
    }
    
    /**
     * Return the name of this group.
     * @return the name of this group.
     * @since MadKitGroupExtension 1.0
     */
    public String getName()
    {
	return m_group.getGroupName();
    }
    
    /**
     * The path of a group is used transparently by MadKitGroupExtension as being the real group name used in MadKit.
     * If a group A is in a group B, which have no parent group, than the path of A will be '/B/A/' and the path of B
     * will be '/B/'.
     * 
     * @return the path of this group.
     * @since MadKitGroupExtension 1.0
     */
    public String getPath()
    {
	return m_group.getGroupPath();
    }

    /**
     * Returns a subgroup contained on this group, or on one of its subgroups.
     * This function is the same call than <code>getSubGroup(false, _groups)</code>
     * 
     * Here a simple example :
     * <pre>
     * 	Group a=new Group("My community", "A");
     *  Group aa=new Group("My community", "A", "A");
     *  Group ab=new Group("My community", "A", "B");
     *  Group aba=new Group("My community", "A", "B", "A");
     *  
     *  Group subgroup=ab.getSubGroup("A");
     * </pre>
     * On this example, the group <code>subgroup</code> is the same group than <code>aba</code>
     * 
     * 
     * @param _groups the path of the desired subgroup
     * @return the subgroup
     * @since MadKitGroupExtension 1.0
     */
    public Group getSubGroup(String ..._groups)
    {
	return this.getSubGroup(false, _groups);
    }
    /**
     * Returns a subgroup contained on this group, or on one of its subgroups.
     * 
     * Here a simple example :
     * <pre>
     * 	Group a=new Group("My community", "A");
     *  Group aa=new Group("My community", "A", "A");
     *  Group ab=new Group("My community", "A", "B");
     *  Group aba=new Group("My community", "A", "B", "A");
     *  
     *  Group subgroup=ab.getSubGroup("A");
     * </pre>
     * On this example, the group <code>subgroup</code> is the same group than <code>aba</code>
     * 
     * 
     * @param _isReserved tell if the group does not authorize other instances with the same group
     * @param _groups the path of the desired subgroup
     * @return the subgroup
     * @since MadKitGroupExtension 1.0
     */
    public Group getSubGroup(boolean _isReserved, String ..._groups)
    {
	return new Group(m_group.getGroup(m_group.isMadKitDistributed(), m_group.getMadKitIdentifier(), _isReserved, _groups), false, false);
    }
    /**
     * This function works in the same way that the function {@link #getSubGroup(String...)}. 
     * But the return subgroup will also represent its subgroups.  
     * @param _group the path of the desired subgroup
     * @return the subgroup
     * @since MadKitGroupExtension 1.0
     * @see #getRepresentedGroups(KernelAddress)
     */
    public Group getSubGroupWithItsSubGroups(String ..._group)
    {
	return new Group(m_group.getGroup(m_group.isMadKitDistributed(), m_group.getMadKitIdentifier(), false, _group), true, false);
    }
    
    /**
     * Return the same group, which does not represent its subgroups
     * 
     * @return the same group, which does not represent its subgroups
     * @since MadKitGroupExtension 1.0
     * @see #getRepresentedGroups(KernelAddress)
     */
    public Group getThisGroupWithoutItsSubGroups()
    {
	if (m_use_sub_groups)
	{
	    return new Group(m_group, false, true);
	}
	else 
	    return this;
    }
    
    /**
     * Return the same group, which represents its subgroups
     * 
     * @return the same group, which represents its subgroups
     * @since MadKitGroupExtension 1.0
     * @see #getRepresentedGroups(KernelAddress)
     */
    public Group getThisGroupWithItsSubGroups()
    {
	if (!m_use_sub_groups)
	{
	    return new Group(m_group, true, true);
	}
	else 
	    return this;
    }
    
    /**
     * This function enable to return all the subgroups of this group, i.e. all the subgroups that are handled by one or more agents. 
     * @param ka the used kernel address
     * @return the subgroups that are handled by one or more agents, excepted those that are reserved
     * @since MadKitGroupExtension 1.0
     */
    public Group[] getSubGroups(KernelAddress ka)
    {
	GroupTree[] sub_groups=m_group.getSubGroups(ka);
	if (m_sub_groups_tree!=sub_groups)
	{
	    ArrayList<Group> res=new ArrayList<Group>();
	    
	    for (int i=0;i<sub_groups.length;i++)
	    {
		if (!sub_groups[i].isReserved())
		    res.add(new Group(sub_groups[i], false, true));
	    }
	    
	    synchronized(this)
	    {
		m_represented_groups=null;
		m_sub_groups=new Group[res.size()];
		res.toArray(m_sub_groups);
		m_sub_groups_tree=sub_groups;
	    }
	}
	return m_sub_groups;
    }
    
    /**
     * Return all the parent groups of this group. Note that these parent groups are not necessarily handled by one or more agents.
     * 
     * @return all the parent groups of this group excepted those that are reserved. Note that these parent groups are not necessarily handled by one or more agents.
     * @since MadKitGroupExtension 1.0 
     */
    public Group[] getParentGroups()
    {
	GroupTree[] parent_groups=m_group.getParentGroups();
	if (m_parent_groups_tree!=parent_groups)
	{
	    ArrayList<Group> res=new ArrayList<>(parent_groups.length);
	    
	    for (int i=0;i<parent_groups.length;i++)
	    {
		if (!parent_groups[i].isReserved())
		    res.add(new Group(parent_groups[i], false, true));
	    }
	    
	    synchronized(this)
	    {
		m_parent_groups=new Group[res.size()];
		res.toArray(m_parent_groups);
		if (m_use_sub_groups) 
		    m_represented_groups=null;
		m_parent_groups_tree=parent_groups;
	    }
	}
	return m_parent_groups;
    }
    @Override public boolean equals(Object o)
    {
	if (o instanceof Group)
	{
	    return this.equals((Group)o);
	}
	else return false;
    }
    public boolean equals(Group _g)
    {
	return _g==this || (this.m_group==_g.m_group && this.m_use_sub_groups==_g.m_use_sub_groups);
    }

    /**
     * Return true if this group is distributed into a network of several MadKit kernels.
     * @return true if this group is distributed into a network of several MadKit kernels.
     * @see #Group(boolean, boolean, Gatekeeper, String, String...)
     */
    public boolean isMadKitDistributed()
    {
	return m_group.isMadKitDistributed();
    }
    
    /**
     * Return the gatekeeper used to control the agents handling of this group.
     * @return the gatekeeper used to control the agents handling of this group. 
     * @see #Group(boolean, boolean, Gatekeeper, String, String...) 
     */
    public Gatekeeper getMadKitIdentifier()
    {
	return m_group.getMadKitIdentifier();
    }

    private boolean isMadKitCreated(KernelAddress ka)
    {
	return m_group.isMadKitCreated(ka);
    }

    protected void incrementMadKitReferences(KernelAddress ka)
    {
	m_group.incrementMadKitReferences(ka);
    }

    protected void incrementMadKitReferences(int number, KernelAddress ka)
    {
	if (number<1)
	    throw new IllegalAccessError();
	m_group.incrementMadKitReferences(number, ka);
    }

    protected boolean decrementMadKitReferences(KernelAddress ka)
    {
	m_group.decrementMadKitReferences(ka);
	return m_group.isMadKitCreated(ka);
    }

    protected boolean decrementMadKitReferences(int number, KernelAddress ka)
    {
	m_group.decrementMadKitReferences(number, ka);
	return m_group.isMadKitCreated(ka);
    }

    /**
     * This function returns the represented groups by the current instance.
     * These groups are for the class Group a list of subgroups corresponding to this instance. 
     * Only groups that are used on MadKit, i.e. by agents, are returned. Groups that are only 
     * instantiated on the program are not returned. If the current instance does not represent 
     * its subgroups (see {@link #Group(boolean, String, String...)}), only one group can be returned at maximum. This group correspond to the current 
     * instance. Moreover, if the previous group have not been already handled by one agent, an empty list is returned.       
     * 
     * @param ka the used kernel address. 
     * @return the represented groups excepted those that are reserved
     * @since MadKitGroupExtension 1.0
     * @see AbstractGroup
     */
    @Override
    public Group[] getRepresentedGroups(KernelAddress ka)
    {
	if (m_use_sub_groups)
	{
	    synchronized(this)
	    {
		
		if(m_represented_groups==null || (this.isMadKitCreated(ka) && ((m_represented_groups.length!=0 && m_represented_groups[0]!=this) || m_represented_groups.length==0)) || (!this.isMadKitCreated(ka) && m_represented_groups.length!=0 && m_represented_groups[0]==this))
		{
		    Group[] sg=getSubGroups(ka);
		    if (this.isMadKitCreated(ka))
		    {
			m_represented_groups=new Group[sg.length+1];
			if (this.isUsedSubGroups())
			    m_represented_groups[0]=this.getThisGroupWithoutItsSubGroups();
			else
			    m_represented_groups[0]=this;
			System.arraycopy(sg, 0, m_represented_groups, 1, sg.length);
		    }
		    else
		    {
			m_represented_groups=sg;
		    }
		}
		return m_represented_groups;
	    }
	}
	else 
	{
	    synchronized(this)
	    {
        	    if (m_represented_groups.length==0)
        	    {
        		if (this.isMadKitCreated(ka))
        		{
        		    m_represented_groups=new Group[1];
        		    m_represented_groups[0]=this;
        		}
        	    }
        	    else
        	    {
        		if (!this.isMadKitCreated(ka))
        		    m_represented_groups=new Group[0];
        	    }
	    }
		
	    return m_represented_groups;
	}
    }
    
    /**
     * Return true if this group represents also its subgroups.
     * 
     * @return true if this group represents also its subgroups.
     * @since MadKitGroupExtension 1.0
     * @see #Group(boolean, String, String...)
     */
    public boolean isUsedSubGroups()
    {
	return m_use_sub_groups;
    }

    /*private GroupTree getGroupTree()
    {
	return m_group;
    }*/
    
    @Override public String toString()
    {
	if (m_use_sub_groups)
	    return "GroupAndSubGroups("+getCommunity()+":"+getPath()+")";
	else
	    return "Group("+getCommunity()+":"+getPath()+")";
    }
    
    private static String [] getGroupsStringFromPath(String _path)
    {
	String []r=_path.split("/");
	if (r==null || r.length==0)
	    return null;
	int size=0;
	for (int i=0;i<r.length;i++)
	{
	    if (r[i].length()==0)
		r[i]=null;
	    else
		size++;
	}
	String r2[];
	if (size==r.length)
	    r2=r;
	else
	{
	    r2=new String[size];
	    int j=0; 
	    for (int i=0;i<r.length;i++)
	    {
		if (r[i]!=null)
		{
		    r2[j]=r[i];
		    j++;
		}
	    }
	}
	return r2;
    }
    
    
    /**
     * Return a group from a path.
     * 
     * @param _community the community of desired group.
     * @param _path the path (see {@link #getPath()} to get more information) corresponding to the desired group.
     * @return the desired group.
     * @since MadKitGroupExtension 1.0
     */
    public static Group getGroupFromPath(String _community, String _path)
    {
	return new Group(_community, Group.getGroupsStringFromPath(_path));
    }
    
    /**
     * Return a group from an agent address.
     * 
     * @param _address the agent address
     * @return the corresponding group.
     * @since MadKitGroupExtension 1.3.7
     */
    public static Group getGroupFromAgentAddress(AgentAddress _address)
    {
	if (_address==null)
	    return null;
	return getGroupFromPath(_address.getCommunity(), _address.getGroup());
    }
    
    static private final ArrayList<GroupChangementNotifier> m_objects_to_notify=new ArrayList<GroupChangementNotifier>(100);
    
    protected static void addGroupChangementNotifier(GroupChangementNotifier _gcn)
    {
	synchronized(m_objects_to_notify)
	{
	    m_objects_to_notify.add(_gcn);
	}
    }

    protected static void removeGroupChangementNotifier(GroupChangementNotifier _gcn)
    {
	synchronized(m_objects_to_notify)
	{
	    m_objects_to_notify.remove(_gcn);
	}
    }
    
    static void notifyChangements()
    {
	synchronized(m_objects_to_notify)
	{
	    for (GroupChangementNotifier gcn : m_objects_to_notify)
	    {
		gcn.potentialChangementInGroups();
	    }
	}
    }
    
    final static class Universe extends AbstractGroup
    {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4697381152906077266L;
	
	@Override
	public Group[] getRepresentedGroups(KernelAddress _ka)
	{
	    if (_ka==null)
		return new Group[0];
	    AtomicReference<Group[]> rp=null;
	    synchronized(represented_groups_universe)
	    {
		rp=represented_groups_universe.get(_ka);
		if (rp==null)
		{
		    rp=new AtomicReference<Group[]>(null);
		    represented_groups_universe.put(_ka, rp);
		}
	    }
	    Group res[]=rp.get();
	    if (res==null)
	    {
		ArrayList<Group> lst=new ArrayList<>(50);
		synchronized(m_groups_root)
		{
		    for (GroupTree gt : m_groups_root)
		    {
			for (Group g : gt.getRepresentedGroups(_ka))
			    lst.add(g);
		    }
		}
		res=new Group[lst.size()];
		lst.toArray(res);
		rp.set(res);
	    }
	    return res;
	}

	@Override
	public AbstractGroup clone()
	{
	    return this;
	}
	
	@Override public boolean equals(Object o)
	{
	    if (o==this)
		return true;
	    if (o==null)
		return false;
	    if (o instanceof Universe)
		return true;
	    else if (o instanceof MultiGroup)
		return o.equals(this);
	    else
		return false;
	}
	
	@Override public String toString()
	{
	    return "UniverseOfGroups";
	}
	@Override public int hashCode()
	{
	    return 0;
	}
    }
    
    static final Universe universe=new Universe();
    static final Map<KernelAddress, AtomicReference<Group[]>> represented_groups_universe=new HashMap<KernelAddress, AtomicReference<Group[]>>();
    
    static void resetRepresentedGroupsOfUniverse(KernelAddress ka)
    {
	synchronized(represented_groups_universe)
	{
	    AtomicReference<Group[]> af=represented_groups_universe.get(ka);
	    if (af!=null)
		af.set(null);
	}
    }
    
    static protected final ArrayList<GroupTree> m_groups_root=new ArrayList<GroupTree>();
    
    static protected GroupTree getRoot(String _community)
    {
	synchronized(m_groups_root)
	{
        	for (GroupTree g: m_groups_root)
        	{
        	    if (g.m_community.equals(_community))
        	    {
        		return g;
        	    }
        	}
        	GroupTree res=new GroupTree(_community);
        	m_groups_root.add(res);        	
        	return res;
	}
    }
    
    protected static KernelAddress m_first_kernel=null;
    
    public static KernelAddress getFirstUsedMadKitKernel()
    {
	return m_first_kernel;
    }
    
    private final static class GroupTree
    {
	private static final class KernelReferences
	{
	    public int m_madkit_references=0;
	    public KernelAddress m_kernel=null;
	    public LinkedList<GroupTree> m_all_sub_groups=new LinkedList<GroupTree>();
	    public final AtomicReference<GroupTree[]> m_all_sub_groups_duplicated=new AtomicReference<GroupTree[]>(new GroupTree[0]);
	    
	    public KernelReferences(KernelAddress ka)
	    {
		m_kernel=ka;
	    }
	    
	    public boolean equals(KernelReferences kr)
	    {
		return m_kernel.equals(kr.m_kernel);
	    }
	    @Override public boolean equals(Object o)
	    {
		if (o instanceof KernelReferences)
		{
		    return equals((KernelReferences)o);
		}
		return false;
	    }
	}
	
	public boolean isReserved()
	{
	    return isReserved;
	}
	
	private final ArrayList<GroupTree> m_sub_groups=new ArrayList<GroupTree>();
	protected final String m_community;
	private final String m_group;
	private final String m_path;
	private final GroupTree m_parent;
	private final boolean m_is_distributed;
	private final Gatekeeper m_identifier;
	private int m_references=0;
	private HashMap<KernelAddress, KernelReferences> m_kernel_references=new HashMap<KernelAddress, KernelReferences>();
	private boolean isReserved;
	
	//private final LinkedList<GroupTree> m_all_sub_groups=new LinkedList<GroupTree>();
	//private GroupTree[] m_all_sub_groups_duplicated=new GroupTree[0];
	
	private final LinkedList<GroupTree> m_parent_groups=new LinkedList<GroupTree>();
	private final AtomicReference<GroupTree[]> m_parent_groups_duplicated=new AtomicReference<GroupTree[]>(new GroupTree[0]);
	
	/*private final LinkedList<String> m_sub_group_paths=new LinkedList<String>();
	private String[] m_sub_group_paths_duplicated=new String[0];
	
	private final LinkedList<String> m_parent_group_paths=new LinkedList<String>();
	private String[] m_parent_group_paths_duplicated=new String[0];*/

	
	
	public GroupTree(String _community)
	{
	    m_community=_community;
	    m_group="";
	    m_path="/";
	    m_parent=null;
	    m_is_distributed=true;
	    m_identifier=null;
	    isReserved=false;
	}
	

	private GroupTree(String group, GroupTree _parent, boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved)
	{
	    if (group.length()==0)
		throw new IllegalArgumentException("There is a group whose name is empty");
	    if (group.contains("/"))
		throw new IllegalArgumentException("The group named '"+group+"' cannot contains a '/' character !");
	    if (group.contains(";"))
		throw new IllegalArgumentException("The group named '"+group+"' cannot contains a ';' character !");

	    m_community=_parent.m_community;
	    m_group=group;
	    m_path=_parent.m_path+group+"/";
	    m_parent=_parent;
	    m_is_distributed=_isDistributed;
	    m_identifier=_theIdentifier;
	    isReserved=_isReserved;
	}
	public GroupTree getGroup(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String ..._group)
	{
	    if (_group.length==0)
		return this;

	    return getGroup(_isDistributed, _theIdentifier, 0, _isReserved, _group);
	}
	
	
	private synchronized GroupTree getGroup(boolean _isDistributed, Gatekeeper _theIdentifier, int i, boolean _isReserved, String ..._group)
	{
	    String g=_group[i];
	    
	    for (GroupTree gt : m_sub_groups)
	    {
		if (gt.m_group.equals(g))
		{
		    if (i==_group.length-1)
		    {
			if ((_isReserved && gt.getNbReferences()>0) || gt.isReserved)
			{
			    String err="";
			    for (String s : _group)
				err+=s+"/";
			    if (gt.isReserved)
				throw new IllegalArgumentException("The group "+err+" is reserved !");
			    else
				throw new IllegalArgumentException("The group "+err+" cannot be reserved, because it have already been reserved !");
			}
			if (_isReserved)
			    gt.isReserved=true;
			gt.incrementReferences();
			return gt;
		    }
		    else
			return gt.getGroup(_isDistributed, _theIdentifier, i+1, _isReserved, _group);
        	}
	    }

	    GroupTree gt=new GroupTree(g, this, _isDistributed, _theIdentifier, (i==_group.length-1)?_isReserved:false);
	    
	    GroupTree res;
	    if (i==_group.length-1)
		res=gt;
	    else
		res=gt.getGroup(_isDistributed, _theIdentifier, i+1, _isReserved, _group);
	    
	    addSubGroup(gt);
	    return res;
	}
	
	public GroupTree getParent()
	{
	    if (m_parent!=null && m_parent.m_parent==null)
		return null;
	    return m_parent;
	}
	public String getCommunity()
	{
	    return m_community;
	}
	public String getGroupName()
	{
	    return m_group;
	}
	public String getGroupPath()
	{
	    return m_path;
	}
	public boolean isMadKitDistributed()
	{
	    return m_is_distributed;
	}
	public Gatekeeper getMadKitIdentifier()
	{
	    return m_identifier;
	}
	public synchronized GroupTree[] getSubGroups(KernelAddress ka)
	{
	    KernelReferences kr=m_kernel_references.get(ka);
	    if (kr==null)
		return new GroupTree[0];
	    
	    return kr.m_all_sub_groups_duplicated.get();
	}
	public GroupTree[] getParentGroups()
	{
	    return m_parent_groups_duplicated.get();
	}
	public synchronized void incrementMadKitReferences(KernelAddress ka)
	{
	    if (Group.m_first_kernel==null)
		Group.m_first_kernel=ka;
	    
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
	    {
		kr=new KernelReferences(ka);
		m_kernel_references.put(ka, kr);
	    }
		
	    
	    ++kr.m_madkit_references;
	    if (kr.m_madkit_references==1)
		activateGroup(ka);
	}
	public synchronized void incrementMadKitReferences(int number, KernelAddress ka)
	{
	    if (Group.m_first_kernel==null)
		Group.m_first_kernel=ka;
	    
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
	    {
		kr=new KernelReferences(ka);
		m_kernel_references.put(ka, kr);
	    }
		
	    
	    kr.m_madkit_references+=number;
	    if (kr.m_madkit_references==number)
		activateGroup(ka);
	}
	public synchronized void decrementMadKitReferences(KernelAddress ka)
	{
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
		throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");
	    
	    --kr.m_madkit_references;
	    if (kr.m_madkit_references<0)
		throw new IllegalAccessError("Problem of data integrity ! The madkit reference for this group shouldn't be lower than 0. This is a MaKitGroupExtension bug !");
	    if (kr.m_madkit_references==0)
	    {
		m_kernel_references.remove(kr);
		deactivateGroup(ka);
	    }
	}
	public synchronized void decrementMadKitReferences(int number, KernelAddress ka)
	{
	    if (number<=0)
		throw new IllegalArgumentException("the argument number ("+number+") should be greater than zero. This is a MaKitGroupExtension bug !");

	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
		throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");
	    
	    
	    kr.m_madkit_references-=number;
	    if (kr.m_madkit_references<0)
		throw new IllegalAccessError("Problem of data integrity ! The madkit reference for this group shouldn't be lower than 0. This is a MaKitGroupExtension bug !");
	    if (kr.m_madkit_references==0)
	    {
		m_kernel_references.remove(kr);
		deactivateGroup(ka);
	    }
	}
	public synchronized boolean isMadKitCreated(KernelAddress ka)
	{
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
		return false;

	    return kr.m_madkit_references>0;
	}
	
	/*public void setMadKitCreatedToFalse(KernelAddress ka)
	{
	    KernelReferences kr=null;
	    for (KernelReferences k : m_kernel_references)
	    {
		if (k.m_kernel.equals(ka)){
		    kr=k;
		    break;
		}
	    }
	    
	    if (kr==null)
		throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");
	    
	    kr.m_madkit_references=0;
	}*/
	
	
	private synchronized void updateDuplicatedParentList()
	{
	    GroupTree res[]=new GroupTree[m_parent_groups.size()];
	    int i=0;
	    for (GroupTree gt : m_parent_groups)
	    {
		res[i++]=gt;
	    }
	    m_parent_groups_duplicated.set(res);
	}
	private synchronized void updateDuplicatedSubGroupList(KernelAddress ka)
	{
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
		throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");
	    
	    GroupTree res[]=new GroupTree[kr.m_all_sub_groups.size()];
	    int i=0;
	    for (GroupTree gt : kr.m_all_sub_groups)
	    {
		res[i++]=gt;
	    }
	    kr.m_all_sub_groups_duplicated.set(res);
	}
	private synchronized void addSubGroup(GroupTree _g)
	{
	    m_sub_groups.add(_g);
	    _g.m_parent_groups.clear();
	    if (m_parent!=null)
	    {
		_g.m_parent_groups.add(this);
	    }
	    GroupTree p=m_parent;
	    while (p!=null)
	    {
		_g.m_parent_groups.add(p);
		p=p.m_parent;
	    }
	    _g.updateDuplicatedParentList();
	}
	/*private KernelReferences getKernelReferences(KernelAddress ka)
	{
	    for (KernelReferences k : m_kernel_references)
	    {
		if (k.m_kernel.equals(ka))
		{
		    return k;
		}
	    }
	    return null;
	}*/
	private synchronized void activateGroup(KernelAddress ka)
	{
	    KernelReferences kr=m_kernel_references.get(ka);
	    
	    if (kr==null)
		throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");

	    GroupTree p=m_parent;
	    while (p!=null)
	    {
		KernelReferences krp=p.m_kernel_references.get(ka);
		if (krp==null)
		{
		    krp=new KernelReferences(ka);
		    p.m_kernel_references.put(ka, krp);
		}
		krp.m_all_sub_groups.add(this);
		p.updateDuplicatedSubGroupList(ka);
		p=p.m_parent;
	    }
	    resetRepresentedGroupsOfUniverse(ka);
	    Group.notifyChangements();
	}
	private synchronized void deactivateGroup(KernelAddress ka)
	{
	    GroupTree p=m_parent;
	    while (p!=null)
	    {
		KernelReferences krp=p.m_kernel_references.get(ka);
		if (krp==null)
		    throw new IllegalAccessError("Problem of data integrity ! The KernelAddress should be stored on the GroupTree class. This is a MaKitGroupExtension bug !");
		
		krp.m_all_sub_groups.remove(this);
		if (krp.m_madkit_references==0)
		    p.m_kernel_references.remove(krp);
		p.updateDuplicatedSubGroupList(ka);
		p=p.m_parent;
	    }
	    resetRepresentedGroupsOfUniverse(ka);
	    Group.notifyChangements();
	}
	
	private synchronized void removeSubGroup(GroupTree _g)
	{
	    if (!m_sub_groups.remove(_g))
		throw new IllegalAccessError("The previous test (after this line code) should return true");
	    /*m_all_sub_groups.remove(_g);
	    updateDuplicatedSubGroupList();
	    GroupTree p=m_parent;
	    while (p!=null && p.m_parent!=null)
	    {
		p.m_all_sub_groups.remove(_g);
		p.updateDuplicatedSubGroupList();
	    }*/

	    if (m_parent!=null)
	    {
		synchronized(m_parent)
		{
		    if (m_references==0 && m_sub_groups.size()==0)
			m_parent.removeSubGroup(this);
		}
	    }
	    else 
	    {
		synchronized(m_groups_root)
		{
		    if (m_sub_groups.size()==0)
			m_groups_root.remove(this);
		}
	    }
		
	}
	public synchronized void incrementReferences()
	{
	    ++m_references;
	}
	public synchronized int getNbReferences()
	{
	    return m_references;
	}
	public synchronized void decrementReferences()
	{
	    if (m_parent!=null)
	    {
		synchronized(m_parent)
		{
		    --m_references;
		    if (m_references==0)
		    {
			if (m_kernel_references.size()>0)
			    throw new IllegalAccessError("The program shouldn't arrive on this line code. This is a MaKitGroupExtension bug !");
			if (m_sub_groups.size()==0)
				m_parent.removeSubGroup(this);
		    }
		}
	    }
	    else
		--m_references;
	}
	
	private final AtomicReference<Group> root_group=new AtomicReference<>(null);
	
	Group[] getRepresentedGroups(KernelAddress ka)
	{
	    Group g=root_group.get();
	    if (g==null)
	    {
		root_group.set(g=new Group(this, true, false));
	    }
	    return g.getRepresentedGroups(ka);
	}
	
    }

    
}
