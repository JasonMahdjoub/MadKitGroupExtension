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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import madkit.gui.OutputPanel;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.kernel.AgentAddress;
import madkit.kernel.AgentLogger;
import madkit.kernel.Gatekeeper;
import madkit.kernel.KernelAddress;
import madkit.kernel.Madkit;
import madkit.kernel.MadkitClassLoader;
import madkit.kernel.Message;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.Madkit.Option;
import madkit.message.EnumMessage;
import madkit.message.MessageFilter;
import madkit.util.XMLUtilities;


/**
 * This is the common interface of each MadKitGroupExtension agent (AbstractAgent, Agent, Scheduler, Watcher, SwingViewer).
 * 
 * This documentation is copied/pasted and adapted from the MadKit documentation.
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadKitGroupExtension 1.0
 */
public interface MKGEAbstractAgent extends GroupChangementNotifier
{
    
	/**
	 * Activates the MaDKit GUI initialization when launching the agent whatever
	 * the launching parameters. By default agents are launched without a GUI
	 * but some of them always need one: This ensures that the agent will have one.
	 * This method should be used only in the constructor of the 
	 * agent, otherwise it will be useless as it specifies a boot 
	 * property of the agent.
	 * 
	 */
	public void createGUIOnStartUp();

	/**
	 * Tells if this agent has a GUI automatically built by the kernel
	 * @return <code>true</code> if this agent has a GUI built by the kernel
	 */
	public boolean hasGUI();    
    
	
	/**
	 * Return a string representing a unique identifier for the agent
	 * over the network.
	 * 
	 * @return the agent's network identifier
	 */
	public String getNetworkID();	
	
	/**
	 * Return a string representing a shorter version of the 
	 * unique identifier of the agent over the network.
	 * As a simplified version, this string may not be unique.
	 * 
	 * @return a simplified version of the agent's network identifier
	 * @see #getNetworkID()
	 */
	public String getSimpleNetworkID();	
	
	
	/**
	 * Returns <code>true</code> if the agent has been launched and is not ended nor killed.
	 * 
	 */
    public boolean isAlive();
    
	/**
	 * Returns the agent's name.
	 * 
	 * @return the name to display in logger info, GUI title and so on. Default
	 *         is "<i>class name + internal ID</i>"
	 * 
	 */
    public String getName();

    	/**
	 * Changes the agent's name
	 * @param name
	 *           the name to display in logger info, GUI title and so on, default
	 *           is "class name + internal ID"
	 */
    public void setName(final String name);
    
    
	/**
	 * Sets the agent's log level. This should be
	 * used instead of directly {@link AgentLogger#setLevel(Level)} because
	 * this works when {@link madkit.kernel.AbstractAgent#logger} is <code>null</code> and allows
	 * to set it to <code>null</code> to save cpu time.
	 * 
	 * @param newLevel The log level under which log messages are displayed. 
	 * If {@link Level#OFF} is used
	 * then {@link madkit.kernel.AbstractAgent#logger} is set to <code>null</code>
	 * 
	 * @see madkit.kernel.AbstractAgent#logger
	 */
    public void setLogLevel(final Level newLevel);
    
	/**
	 * Returns the agent's logger.
	 * 
	 * @return the agent's logger. It cannot be <code>null</code> as it will be
	 *         created if necessary. But you can then still put {@link madkit.kernel.AbstractAgent#logger}
	 *         to <code>null</code> for optimizing your code by using
	 *         {@link #setLogLevel(Level)} with {@link Level#OFF}.
	 * 
	 * @since MadKit 5.0.0.6
	 */
    public AgentLogger getLogger();
    
	/**
	 * Retrieves and removes the oldest received message contained in the
	 * mailbox.
	 * 
	 * @return The next message or <code>null</code> if the message box is empty.
	 */
    public Message nextMessage();
    
	/**
	 * Purges the mailbox and returns the most
	 * recent received message at that time.
	 * 
	 * @return the most recent received message or <code>null</code> if the
	 *         mailbox is already empty.
	 */
    public Message purgeMailbox();

	/**
	 * Tells if there is a message in the mailbox
	 * @return <code>true</code> if there is no message in
	 *         the mailbox.
	 */
    public boolean isMessageBoxEmpty(); 
    
	/**
	 * Sends a message to an agent using an agent address. This has the same
	 * effect as <code>sendMessageWithRole(receiver, messageToSend, null)</code>.
	 * 
	 * @param receiver
	 * @param messageToSend
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
    public ReturnCode sendMessage(final AgentAddress receiver, final Message messageToSend);
	

	/**
	 * Sends a message, using an agent address, specifying explicitly the role
	 * used to send it.
	 * 
	 * @param receiver
	 *           the targeted agent
	 * @param message
	 *           the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the receiver's group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AgentAddress
	 */
    public ReturnCode sendMessageWithRole(final AgentAddress receiver, final Message message, final String senderRole);
    
	/**
	 * Sends a message by replying to a previously received message. The sender
	 * is excluded from this search.
	 * 
	 * @param messageToReplyTo
	 *           the previously received message.
	 * @param reply
	 *           the reply itself.
	 * @param senderRole the agent's role with which the message should be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         no longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see ReturnCode
	 * 
	 */
    public ReturnCode sendReplyWithRole(final Message messageToReplyTo, final Message reply, final String senderRole); 
    
	/**
	 * Sends a message by replying to a previously received message. This has the
	 * same effect as
	 * <code>sendReplyWithRole(messageToReplyTo, reply, null)</code>.
	 * 
	 * @param messageToReplyTo
	 *           the previously received message.
	 * @param reply
	 *           the reply itself.
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the reply has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         no longer a member of the corresponding group.</li>
	 *         <li><code>{@link ReturnCode#INVALID_AGENT_ADDRESS}</code>: If the
	 *         receiver address is no longer valid. This is the case when the
	 *         corresponding agent has leaved the role corresponding to the
	 *         receiver agent address.</li>
	 *         </ul>
	 * @see AbstractAgent#sendReplyWithRole(Message, Message, String)
	 * 
	 */
    public ReturnCode sendReply(final Message messageToReplyTo, final Message reply);
    
	/**
	 * Gets the next message which is a reply to the <i>originalMessage</i>.
	 * 
	 * @param originalMessage
	 *           the message to which a reply is searched.
	 * @return a reply to the <i>originalMessage</i> or <code>null</code> if no
	 *         reply to this message has been received.
	 */
    public Message getReplyTo(final Message originalMessage); 

	/**
	 * This method offers a convenient way for regular object to send messages to
	 * Agents, especially threaded agents. For instance when a GUI wants to
	 * discuss with its linked agent: This allows to enqueue work to do in their
	 * life cycle
	 * 
	 * @param m
	 */
    public void receiveMessage(final Message m);
    
	/**
	 * Gets the MadKit session property indicated by the specified key. This call
	 * is equivalent to <code>getMadkitConfig().getProperty(key)</code>
	 * 
	 * @param key
	 *           the name of the MadKit property
	 * @return the string value of the MadKit property, or <code>null</code> if
	 *         there is no property with that key.
	 * @see #setMadkitProperty(String, String)
	 * @see Madkit
	 */
    public String getMadkitProperty(String key);
    
	/**
	 * Sets the MadKit session property indicated by the specified key.
	 * 
	 * @param key
	 *           the name of the MadKit property
	 * @see #getMadkitProperty(String)
	 * @see Madkit
	 */
    public void setMadkitProperty(String key, String value); 

    
	/**
	 * Called when the default GUI mechanism is used upon agent creation. This
	 * provides an empty frame which will be used as GUI for the agent. The
	 * life cycle of the frame is automatically managed: the frame is disposed when the
	 * agent is terminated. Some menus are available by default. Default code
	 * is only one line: <code>frame.add(new IOPanel(this));</code>.
	 * 
	 * Default settings for the frame are:
	 * <ul>
	 * <li>width = 400</li>
	 * <li>height = 300</li>
	 * <li>location = center of the screen</li>
	 * <li>a JMenuBar with: {@link MadkitMenu}, {@link AgentMenu} and {@link AgentLogLevelMenu}</li> 
	 * </ul>
	 * 
	 * @param frame
	 *           the default frame which has been created by MadKit for this
	 *           agent.
	 * @since MadKit 5.0.0.8
	 * @see madkit.gui.OutputPanel
	 */
    public void setupFrame(final JFrame frame);    
    
    public Map<String, Map<String, Map<String, Set<AgentAddress>>>> getOrganizationSnapShot(boolean global);
    
    /**
	 * Tells if a community exists in the artificial society.
	 * 
	 * @param community
	 *           the name of the community
	 * @return <code>true</code> If a community with this name exists,
	 *         <code>false</code> otherwise.
	 */
    public boolean isCommunity(final String community);
    
	/**
	 * returns the names of the communities that exist.
	 * 
	 * @return an alphanumerically ordered set containing the names of the communities 
	 * which exist. 
	 * 
	 * @since MaDKit 5.0.0.20
	 */
	public TreeSet<String> getExistingCommunities();

	/**
	 * Returns the server's info, IP and port, if the kernel is online.
	 * 
	 * @return server's info: e.g. /192.168.1.14:4444
	 */
	public String getServerInfo();
	
	/**
	 * Returns the current state of the agent in the MaDKit platform.
	 * 
	 * This method provides a way of knowing what is the current state of the
	 * agent regarding its life cycle. This could be convenient when you design a
	 * method that could work differently depending on the actual state of the
	 * agent.
	 * 
	 * @return the current state of the agent:
	 *         <ul>
	 *         <li><code>{@link State#NOT_LAUNCHED}</code>: the agent has not
	 *         been launched yet. This especially means that most of the methods
	 *         of this API still do not work for this agent as it has not been
	 *         registered yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#INITIALIZING}</code>: the agent is being
	 *         registered by the kernel but has not started its
	 *         {@link AbstractAgent#activate()} method yet.</li>
	 *         <br/>
	 *         <li><code>{@link State#ACTIVATED}</code>: the agent is processing
	 *         its {@link AbstractAgent#activate()} method. This state is also the "running"
	 *         state of {@link AbstractAgent} subclasses (i.e. when they have
	 *         finished their activation) as they do not have a
	 *         {@link Agent#live()} managed by the kernel in their life cycle. On
	 *         the contrary to {@link Agent} subclasses which next state is
	 *         {@link State#LIVING}.</li>
	 *         <br/>
	 *         <li><code>{@link State#LIVING}</code>: returned when {@link Agent}
	 *         subclasses are processing their {@link Agent#live()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#ENDING}</code>: the agent is processing its
	 *         {@link AbstractAgent#end()} method.</li>
	 *         <br/>
	 *         <li><code>{@link State#TERMINATED}</code>: the agent has finished
	 *         its life in the MaDKit platform. Especially, most of the methods
	 *         of this API will no longer work for this agent.</li>
	 *         </ul>
	 * 
	 */
	public State getState();	
    
	/**
	 * Proceeds an {@link EnumMessage} so that if it is correctly built, the
	 * agent will trigger its corresponding behavior using the parameters of the
	 * message.
	 * 
	 * @param message
	 *           the message to proceed
	 * @since MaDKit 5.0.0.14
	 * @see EnumMessage
	 */
	public <E extends Enum<E>> void proceedEnumMessage(EnumMessage<E> message);
    
	
	/**
	 * Tells if the kernel on which this agent is running is online.
	 * 
	 * @return <code>true</code> if the kernel is online.
	 */
	public boolean isKernelOnline();
	
	
    /**
	 * Returns the Properties object of this MadKit session. That is by default
	 * the parameter which has been used to launch the kernel the agent
	 * is running on. If the agent has not been launched yet, the
	 * Properties returned is the default MadKit configuration.
	 * It can be programmatically modified to launch a
	 * new session with different parameters. It can also be used as a
	 * black board shared by all the agents of a kernel by adding
	 * new user defined properties at run time or via the command line. 
	 * The default set of MadKit properties includes
	 * values for the following keys:
	 * <table summary="Shows madkit keys and associated values">
	 * <tr>
	 * <th>Key</th>
	 * <th>Description of Associated Value</th>
	 * </tr>
	 * <tr>
	 * <td><code>madkit.version</code></td>
	 * <td>MadKit kernel version</td>
	 * </tr>
	 * <tr>
	 * <td><code>build.id</code></td>
	 * <td>MadKit kernel build ID</td></tr
	 * <tr>
	 * <td><code>madkit.repository.url</code></td>
	 * <td>the agent repository for this version, usually http://www.madkit.net/repository/MadKit-${madkit.version}/ </td>
	 * </tr>
	 * <tr>
	 * <td><code>desktop</code></td>
	 * <td><code>true</code> or <code>false</code>: Launch the desktop during boot phase</td>
	 * </tr>
	 * <tr>
	 * <td><code>launchAgents</code></td>
	 * <td>The agents launched during the boot phase</td>
	 * </tr>
	 * <tr>
	 * <td><code>createLogFiles</code></td>
	 * <td>true</code> or <code>false</code>: Create log files automatically for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>logDirectory</code></td>
	 * <td>The directory used for the log files (./logs by default)</td>
	 * </tr>
	 * <tr>
	 * <td><code>agentLogLevel</code></td>
	 * <td>the default log level for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>warningLogLevel</code></td>
	 * <td>the default warning log level for the new agents</td>
	 * </tr>
	 * <tr>
	 * <td><code>network</code></td>
	 * <td><code>true</code> or <code>false</code>: Launch the network during boot phase</td>
	 * </tr>
	 * </table>
	 * <p>
	 * 
	 * 
	 * @return the Properties object defining the values of each MadKit options
	 *         in the current session.
	 * @see Option LevelOption BooleanOption 
	 * @since MadKit 5.0.0.10
	 */
    public Properties getMadkitConfig();    
    
	/**
	 * Returns the kernel address on which the agent is running.
	 * 
	 * @return the kernel address representing the MadKit kernel on which the
	 *         agent is running
	 */
    public KernelAddress getKernelAddress();
	
	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community, but not to the sender.
	 * 
	 * @param _group
	 *           the group(s) and the community(ies) name
	 * @param _roleName
	 *           the role name
	 * @param _m
	 * 	     the message
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode broadcastMessage(AbstractGroup _group, String _roleName, Message _m);

    
	/**
	 * Broadcasts a message to every agent having a role in a group in a
	 * community using a specific role for the sender. The sender is excluded
	 * from the search.
	 * 
	 * @param _group
	 *           the group(s) and the community(ies) name
	 * @param _roleName
	 *           the role name
	 * @param _m
	 * 	     the message
	 * @param _senderRole
	 *           the role name of the sender
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of all the targeted groups.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode broadcastMessageWithRole(AbstractGroup _group, String _roleName, Message _m, String _senderRole);
    
    
	/**
	 * Returns the agent address of this agent at this CGR location.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _roleName
	 *           the role name
	 * @return the agent's address in this location or <code>null</code> if this
	 *         agent does not handle this role.
	 * @since MadKit 5.0.0.15
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    public AgentAddress getAgentAddressIn(Group _group, String _roleName);
    
    
	/**
	 * Returns an {@link java.util.List} containing agents that handle this role
	 * in the organization. The caller is excluded from this list.
	 * 
	 * @param _group
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
    public List<AgentAddress> getAgentsWithRole(AbstractGroup _group, String _role);

	/**
	 * Returns an {@link java.util.List} containing all the agents that handle
	 * this role in the organization.
	 * 
	 * @param _group
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
    public List<AgentAddress> getAgentsWithRole(AbstractGroup _group, String _role, boolean callerIncluded);

    
	/**
	 * Returns an {@link AgentAddress} corresponding to an agent having this
	 * position in the organization. The caller is excluded from the search.
	 * 
	 * @param _group
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
    public AgentAddress getAgentWithRole(AbstractGroup _group, String _role);

    
    
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
    public boolean isGroup(Group _group);

    
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
    public boolean isRole(Group _group, String _role);

    
	/**
	 * Makes this agent leaves the group of a particular community.
	 * 
	 * @param _group
	 *           the group and the community name
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of this group.</li>
	 *         </ul>
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKit 5.0
	 * @see ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode leaveGroup(Group _group);
    
    
	/**
	 * Abandons an handled role within a group of a particular community.
	 * 
	 * @param _group
	 *           the group and the community name
	 * @param _role
	 *           the role name
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the operation has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If this role
	 *         is not handled by this agent.</li>
	 *         </ul>
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @since MadKit 5.0
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode leaveRole(Group _group, String _role);

    
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
	 * @see Gatekeeper
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode requestRole(Group _group, String _role, Object _passKey);

    
    
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
	 * @see #requestRole(Group, String, Object)
	 * @since MadKit 5.0
	 * 
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @see Group
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode requestRole(Group _group, String _role);

    
    
	/**
	 * Sends a message to an agent having this position in the organization,
	 * specifying explicitly the role used to send it. This has the same effect
	 * as <code>sendMessageWithRole(group, role, messageToSend,null)</code>. If
	 * several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param _group
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @param _messageToSend
	 *           the message to send
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode sendMessage(AbstractGroup _group, String _role, Message _messageToSend);

    
	/**
	 * Sends a message to an agent having this position in the organization. This
	 * has the same effect as
	 * <code>sendMessageWithRole(groups, role, messageToSend,null)</code>
	 * . If several agents match, the target is chosen randomly. The sender is
	 * excluded from this search.
	 * 
	 * @param _group
	 *           the group(s) and the community(ies) name
	 * @param _role
	 *           the role name
	 * @param _messageToSend
	 *           the message to send
	 * @param _senderRole
	 *           the agent's role with which the message has to be sent
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the send has
	 *         succeeded.</li>
	 *         <li><code>{@link ReturnCode#NOT_COMMUNITY}</code>: If the
	 *         community does not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_GROUP}</code>: If the group does
	 *         not exist.</li>
	 *         <li><code>{@link ReturnCode#NOT_ROLE}</code>: If the role does not
	 *         exist.</li>
	 *         <li><code>{@link ReturnCode#ROLE_NOT_HANDLED}</code>: If
	 *         <code>senderRole</code> is not handled by this agent.</li>
	 *         <li><code>{@link ReturnCode#NOT_IN_GROUP}</code>: If this agent is
	 *         not a member of the targeted group.</li>
	 *         <li><code>{@link ReturnCode#NO_RECIPIENT_FOUND}</code>: If no
	 *         agent was found as recipient, i.e. the sender was the only agent
	 *         having this role.</li>
	 *         </ul>
	 * @see ReturnCode
	 * @see AbstractGroup
	 * @see Group
	 * @see MultiGroup
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode sendMessageWithRole(AbstractGroup _group, String _role, Message _messageToSend, String _senderRole);
    
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
	 * 
	 */
    public int compareTo(madkit.kernel.AbstractAgent _other);

    
    
	/**
	 * Kills the targeted agent. This has the same effect as
	 * <code>killAgent(target,Integer.MAX_VALUE)</code> so that the targeted
	 * agent has a lot of time to complete its {@link AbstractAgent#end()} method.
	 * 
	 * @return <ul>
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li>
	 *         <li><code>{@link ReturnCode#ALREADY_KILLED}</code>: If the target
	 *         has been already killed.</li>
	 *         <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}</code>: If the
	 *         target has not been launched.</li>
	 *         <li><code>{@link ReturnCode#TIMEOUT}</code>: If the target's end
	 *         method took more than 2e31 seconds and has been brutally stopped:
	 *         This unlikely happens ;).</li>
	 *         </ul>
	 * @since MadKit 5.0
	 * @see #killAgent(AbstractAgent, int)
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode killAgent(madkit.kernel.AbstractAgent _target);

	/**
	 * Kills the targeted agent. The kill process stops the agent's life cycle
	 * but allows it to process its {@link AbstractAgent#end()} method until the time out
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
	 *         <li><code>{@link ReturnCode#SUCCESS}</code>: If the target's end
	 *         method has completed normally.</li> <li><code>
	 *         {@link ReturnCode#ALREADY_KILLED}</code>: If the target has been
	 *         already killed.</li> <li><code>{@link ReturnCode#NOT_YET_LAUNCHED}
	 *         </code>: If the target has not been launched.</li> <li><code>
	 *         {@link ReturnCode#TIMEOUT}</code>: If the target's end method took
	 *         too much time and has been brutally stopped.</li>
	 *         </ul>
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode killAgent(madkit.kernel.AbstractAgent _target, int _timeOutSeconds);

    
	/**
	 * Launches a new agent in the MadKit platform. This has the same effect as
	 * <code>launchAgent(agent,Integer.MAX_VALUE,false)</code>
	 * 
	 * @param _agent
	 *           the agent to launch.
	 * @return <ul>
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent);

    
    
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
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If your agent is
	 *         activating for more than 68 years Oo !</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @see AbstractAgent#launchAgent(AbstractAgent)
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, boolean _createFrame);

    
    
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
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code>{@link ReturnCode#AGENT_CRASH}</code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 *  @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 *  @since MadKitGroupExtension 1.0
	 */
    public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, int _timeOutSeconds);

    
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
	 *         <li><code> {@link ReturnCode#SUCCESS} </code>: The launch has
	 *         succeeded. This also means that the agent has successfully
	 *         completed its <code>activate</code> method</li>
	 *         <li><code> {@link ReturnCode#ALREADY_LAUNCHED} </code>: If this
	 *         agent has been already launched</li>
	 *         <li><code> {@link ReturnCode#TIMEOUT} </code>: If the activation
	 *         time of the agent is greater than <code>timeOutSeconds</code>
	 *         seconds</li>
	 *         <li><code> {@link ReturnCode#AGENT_CRASH} </code>: If the agent
	 *         crashed during its <code>activate</code> method</li>
	 *         </ul>
	 * @since MadKit 5.0
	 * @throws IllegalArgumentException When the given agent as parameter don't implement the interface MadKitGroupExtensionAgent
	 * @since MadKitGroupExtension 1.0
	 */
    public ReturnCode launchAgent(madkit.kernel.AbstractAgent _agent, int _timeOutSeconds, boolean _createFrame);

    
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
    public madkit.kernel.AbstractAgent launchAgent(String _agentClass);


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
    public madkit.kernel.AbstractAgent launchAgent(String _agentClass, boolean _createFrame);

    
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
    public madkit.kernel.AbstractAgent launchAgent(String _agentClass, int _timeOutSeconds);

    
    
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
    public madkit.kernel.AbstractAgent launchAgent(String _agentClass, int _timeOutSeconds, boolean _createFrame);

    
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
	 * {@link AbstractAgent#activate()} method.
	 * <p>
	 * Additionally, in order to avoid to change the code of the agent
	 * considering how they will be launched (using the bucket mode or not).
	 * One should use the following alternative of the usual request method :
	 * {@link #bucketModeRequestRole(Group, String, Object)}:
	 * If used in {@link AbstractAgent#activate()}, these requests will be ignored when the
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
	 * Beware that if cpuCoreNb is greater than 1, the agents' constructors and {@link AbstractAgent#activate()} methods
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
    	public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, int cpuCoreNb, Role ... _rolesName);
    
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
	 * {@link AbstractAgent#activate()} method.
	 * <p>
	 * Additionally, in order to avoid to change the code of the agent
	 * considering how they will be launched (using the bucket mode or not).
	 * One should use the following alternative of the usual request method :
	 * {@link #bucketModeRequestRole(Group, String, Object)}:
	 * If used in {@link AbstractAgent#activate()}, these requests will be ignored when the
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
    public List<madkit.kernel.AbstractAgent> launchAgentBucket(String _agentClassName, int _bucketSize, Role ..._rolesName);

    
	/**
	 * Similar to <code>launchAgentBucket(String, int, Role...)</code>
	 * except that the list of agents to launch is given. Especially, this could
	 * be used when the agents have no default constructor.
	 * 
	 * @param _bucket the list of agents to launch
	 * @param _rolesName the list of the roles to give for each agent to launch
	 * 
	 * @throws IllegalArgumentException One (or more) of the given group throw the class Role, represent also its subgroups.
	 * @since MadKitGroupExtension 1.0
	 */
    public void launchAgentBucket(List<MKGEAbstractAgent> _bucket, Role ..._rolesName);

	/**
	 * Similar to <code>launchAgentBucket(String, int, int, Role...)</code>
	 * except that the list of agents to launch is given. Especially, this could
	 * be used when the agents have no default constructor.
	 * 
	 * @param _bucket the list of agents to launch
	 * 
	 * @param cpuCoreNb the number of parallel tasks to use. 
	 * Beware that if cpuCoreNb is greater than 1, the agents' constructors and {@link AbstractAgent#activate()} methods
	 * will be called simultaneously so that one has to be careful if shared resources are
	 * accessed by the agents
	 * @param _rolesName the list of the roles to give for each agent to launch
	 * @throws IllegalArgumentException One (or more) of the given group throw the class Role, represent also its subgroups.
	 * @since MadKitGroupExtension 1.3.1
	 */
    public void launchAgentBucket(List<MKGEAbstractAgent> _bucket, int cpuCoreNb, Role... _rolesName);
    

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
    public ReturnCode bucketModeRequestRole(Group _group, final String role);
    
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
    public ReturnCode bucketModeRequestRole(Group _group, final String role, final Object passKey);
    
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
    public TreeSet<Group> getExistingGroupsInKernel(String community);
    
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
    public TreeSet<String> getExistingRoles(AbstractGroup _group);
    
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
    public AgentAddress getDistantAgentWithRole(AbstractGroup group, String role, KernelAddress from);
    
    
    /**
     * Automatically request the given role into the given represented groups, only for groups that have been requested with other agents. Do nothing else.
     * When other agents leave roles, those that correspond to the current auto-requested role are automatically leaved from this agent.
     * 
     * This function is equivalent to <code>autoRequestRole(AbstractGroup, String, null)</code>.
     * 
     * @param _group the abstract group and these represented groups.
     * @param role the role to request
     * 
     * @see #autoRequestRole(AbstractGroup, String, Object)
     * @see #removeAutoRequestedGroup(AbstractGroup)
     * @see #removeAutoRequestedRole(String)
     */
    public void autoRequestRole(AbstractGroup _group, String role);
    
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
    public void autoRequestRole(AbstractGroup _group, String _role, Object _passKey);
    
    /**
     * Remove role from automatically requested roles.
     * @param role
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public void removeAutoRequestedRole(String role);

    
    /**
     * Replace automatically requested groups by other groups. To replace the old abstract group, the references are used, and not the equals function. So the replacement is done only if <code>_old_group==_new_group</code>. 
     * @param _old_group the old group to compare and replace according its reference
     * @param _new_group the new group to replace
     * @return true if the operation have succeeded.
     * 
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public boolean replaceAutoRequestedGroup(AbstractGroup _old_group, AbstractGroup _new_group);
    
    /**
     * Remove group from automatically requested groups. If this group is contained into a MultiGroup, or if it is contained into the subdirectories of a Group that represent them, then all the concerned AbstractGroup is removed. 
     * @param _group the given group
     * @see #autoRequestRole(AbstractGroup, String, Object)
     */
    public void removeAutoRequestedGroup(AbstractGroup _group);
    

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
    public boolean hasRole(final Group _group, final String role);    

	/**
	 * Gets the names of the groups the agent is in
	 * according to a community
	 * 
	 * @param community
	 * @return a set containing the groups the agent is in, or <code>null</code> if this
	 * community does not exist. This set could be empty.
	 */
	public TreeSet<Group> getMyMKGEGroups(final String community);

	
	/**
	 * Shortcut for <code>getMadkitProperty(option.name())</code>.
	 * Runtime options could be represented using enumeration constants,
	 * as it is the case for MaDKit's, so this is a convenient method 
	 * for retrieving the value of an option.
	 * 
	 * @param option the constant representing a MaDKit option
	 * 
	 * @return the corresponding value as a String, or <code>null</code> if
	 *         there is no property having the corresponding name.
	 * 
	 * @see Option LevelOption BooleanOption
	 * 
	 * @since MaDKit 5.0.3
	 * 
	 */
	public <E extends Enum<E>> String getMadkitProperty(E option);


	/**
	 * Set the MaDKit session property indicated by the specified 
	 * constant representing a MaDKit option.
	 * 
	 * @param option the constant representing a MaDKit option
	 * 
	 * @see #getMadkitProperty(String)
	 * @see Madkit
	 * @since MaDKit 5.0.3
	 */
	public <E extends Enum<E>> void setMadkitProperty(E option, String value);

	/**
	 * Shortcut for <code>Boolean.parseBoolean(getMadkitProperty(option))</code>
	 * 
	 * @param option the constant representing a runtime option
	 * 
	 * @return <code>true</code> if the option has been set to <code>true</code>
	 * 
	 * @since MadKit 5.0.3
	 */
	public <E extends Enum<E>> boolean isMadkitPropertyTrue(E option);

	/**
	 * Gets the names of the roles that the agent has in
	 * a specific group
	 * 
	 * @param _group
	 * @return a sorted set containing the names of the roles
	 * the agent has in a group, or <code>null</code> if the
	 * community or the group does not exist. This set could be empty.
	 */
	public TreeSet<String> getMyMKGERoles(Group _group);
	
	/**
	 * launch all the agents defined in an xml configuration file
	 * 
	 * @param xmlFile the XML file to parse
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public ReturnCode launchXmlAgents(String xmlFile) throws SAXException, IOException, ParserConfigurationException;
	
	/**
	 * Launch agents by parsing an XML node. The method
	 * immediately returns without waiting the end of the agents' activation, 
	 * 
	 * @param agentXmlNode the XML node
	 * @return {@link ReturnCode#SEVERE} if the launch failed
	 * 
	 * @see XMLUtilities
	 */
	public ReturnCode launchNode(Node agentXmlNode);

	/**
	 * Retrieves and removes the first message of the
	 * mailbox that matches the filter.
	 * 
	 * @return The next acceptable message or <code>null</code> if no such message has been found.
	 * @since MadKitGroupExtension 1.4.0
	 */
	public Message nextMessage(MessageFilter filter);
	
	/**
	 * Retrieves and removes all the messages of the
	 * mailbox that match the filter. 
	 * 
	 * @param filter if <code>null</code> all the messages are returned and removed from the mailbox.
	 * @return the ordered list of matching messages, or an empty list if none has been found.
	 * @since MadKitGroupExtension 1.4.0
	 */
	public List<Message> nextMessages(MessageFilter filter);
}
