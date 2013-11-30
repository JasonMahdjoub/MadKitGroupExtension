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

import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
 * This is the common interface for the next MadKitGroupExtension agents : Agent, Scheduler.
 * 
 * This documentation is copied/pasted and adapted from the MadKit documentation.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.2.1
 */
public interface MKGEAgent extends MKGEAbstractAgent
{
    /**
     * Changes the priority of the agent's thread. 
     * This should be used only starting from the {@link Agent#activate()}
     * to have a concrete effect.
     * Default priority is set to {@link Thread#NORM_PRIORITY} - 1 
     * to ensure swing responsiveness.
     * 
     * @param newPriority priority to set this thread to
     * @exception  IllegalArgumentException  If the priority is not in the
     *               range <code>Thread.MIN_PRIORITY</code> to
     *               <code>Thread.MAX_PRIORITY</code>.
     * @exception  SecurityException  if the current thread cannot modify this thread.
     * @see        Thread
     * @since MadKit 5.0.1
     * @since MadKitGroupExtension 1.2.1
     */
    public void setThreadPriority(int newPriority);

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's priority for this agent.
     * @see     Thread
     * @since MadKit 5.0.1
     * @since MadKitGroupExtension 1.2.1
     */
    public int getThreadPriority();
    
	/**
	 * Tells if the agent is a daemon.
	 * 
	 * @return <code>true</code> if the agent is a Daemon
	 * @since MaDKit 5.0.0.9
	 * @since MadKitGroupExtension 1.2.1
	 */
    public boolean isDaemon();
    
	/**
	 * Sends a message and waits indefinitely for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, null)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @return the reply received as soon as available, or <code>null</code>
	 * if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
    public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend);    
    
	/**
	 * Sends a message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, null, timeOutMilliSeconds)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendMessageAndWaitForReply(final AgentAddress receiver, Message messageToSend,final int timeOutMilliSeconds);
	
	
	/**
	 * Sends a message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendMessageWithRoleAndWaitForReply(receiver, messageToSend, senderRole, null)</code>
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message.
	 * @see #sendMessageWithRoleAndWaitForReply(AgentAddress, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend, String senderRole);
	
	/**
	 * Sends a message and waits for an answer to it.
	 * Additionally, the sending is done using a specific role for the sender.
	 * 
	 * @param receiver the targeted agent by the send.
	 * @param messageToSend the message to send.
	 * @param senderRole the role with which the sending is done.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the message, that is any {@link madkit.kernel.AbstractAgent.ReturnCode}
	 * different from {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} 
	 * (see {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)}). 
	 * 
	 * @see #sendMessageWithRole(AgentAddress, Message, String)
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendMessageWithRoleAndWaitForReply(final AgentAddress receiver, Message messageToSend, String senderRole, Integer timeOutMilliSeconds );	
	
	/**
	 * Sends a reply message and waits indefinitely for an answer to it.
	 * This has the same effect as <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, null)</code>.
	 * 
	 * @param messageToReplyTo the original message previously received.
	 * @param reply the new message.
	 * @return the reply received as soon as available.
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply);	
	
	/**
	 * Sends a reply message and waits for an answer to it.
	 * This has the same effect as 
	 * <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, null, timeOutMilliSeconds)</code>.
	 * 
	 * @param messageToReplyTo the original message previously received
	 * @param reply the new message
	 * @return the reply received as soon as available
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendReplyAndWaitForReply(final Message messageToReplyTo, final Message reply,int timeOutMilliSeconds);
	
	/**
	 * Sends a reply message and waits indefinitely for an answer to it.
	 * This has the same effect as <code>sendReplyWithRoleAndWaitForReply(messageToReplyTo, reply, senderRole, null)</code>.
	 * @param messageToReplyTo the original message previously received
	 * @param reply the new message
	 * @return the reply received as soon as available
	 * @see #sendReplyWithRoleAndWaitForReply(Message, Message, String, Integer)
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply,String senderRole);
	
	/**
	 * Sends a reply message and waits for an answer to it.
	 * Additionally, the reply is done using a specific role for the sender.
	 * 
	 * @param messageToReplyTo the original message previously received
	 * @param reply the reply message.
	 * @param senderRole the role with which the reply is sent.
	 * @param timeOutMilliSeconds the maximum time to wait. 
	 * If <code>null</code> the agent will wait indefinitely.
	 * @return the reply received as soon as available, or <code>null</code> if the time out has elapsed 
	 * or if there was an error when sending the reply, that is any {@link madkit.kernel.AbstractAgent.ReturnCode}
	 * different from {@link madkit.kernel.AbstractAgent.ReturnCode#SUCCESS} 
	 * (see {@link MKGEAbstractAgent#sendReplyWithRole(Message, Message, String)}). 
	 * 
	 * @see #sendReplyWithRole(Message, Message, String)
	 * @see madkit.kernel.AbstractAgent.ReturnCode
	 * @since MaDKit 5
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message sendReplyWithRoleAndWaitForReply(final Message messageToReplyTo, final Message reply, String senderRole, Integer timeOutMilliSeconds);
	
	/**
	 * This method is the blocking version of nextMessage(). If there is no
	 * message in the mailbox, it suspends the agent life until a message is received
	 *
	 * @see #waitNextMessage(long)
	 * @return the first message of received in the mailbox
	 */
	public Message waitNextMessage();
	
	/**
	 * This method gets the next message of the mailbox or waits 
	 * for a new incoming message considering a certain delay.
	 * 
	 * @param timeOutMilliseconds the maximum time to wait, in milliseconds.
	 * 
	 * @return  the first message in the mailbox, or <code>null</code> if no message
	 * has been received before the time out delay is elapsed
	 * @since MadKitGroupExtension 1.2.1
	 */
	public Message waitNextMessage(final long timeOutMilliseconds);
	
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
	 * @see Group
	 * @since MadKitGroupExtension 1.2.1
	 */
	public List<Message> broadcastMessageWithRoleAndWaitForReplies(Group _group, String _roleName, Message _message, String _senderRole, Integer _timeOutMilliSeconds);
	
	
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
	 * @since MadKitGroupExtension 1.2.1
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @see Group
	 */
	public Message sendMessageAndWaitForReply(Group _group, String _role, Message _messageToSend);
	
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
	 * @since MadKitGroupExtension 1.2.1
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @see Group
	 */
	public Message sendMessageAndWaitForReply(Group _group, String _role, Message _messageToSend, int _timeOutMilliSeconds);
	
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
	 * @since MadKitGroupExtension 1.2.1
	 * @throws IllegalArgumentException when the given group represents also its subgroups
	 * @see Group
	 */
	public Message sendMessageWithRoleAndWaitForReply(Group _group, String _role, Message _messageToSend, String _senderRole);
	
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
	 * @since MadKitGroupExtension 1.2.1
	 * @see Group
	 */
	public Message sendMessageWithRoleAndWaitForReply(Group _group, String _role, Message _messageToSend, String _senderRole, Integer _timeOutMilliSeconds);
	
	/**
	 * Stops the agent's process for a while.
	 * @param milliSeconds the number of milliseconds for which the agent should pause.
	 */
	public void pause(final int milliSeconds);
	
}
