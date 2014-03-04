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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkitgroupextension.version.Description;
import madkitgroupextension.version.Person;
import madkitgroupextension.version.PersonDeveloper;
import madkitgroupextension.version.Version;


/**
 * The class MadKitGroupExtension inherits from the class MadKit. 
 * The provided documentation is copy/pasted from the original documentation of MadKit. 
 * Some modifications are done considering the specificities of MadKitGroupExtension.
 *      
 * MadKit 5 booter class. 
 * <p>
 * <h2>MadKit v.5 new features</h2>
 * <p>
 * <ul>
 * <li>One big change that comes with version 5 is how agents
 * are identified and localized within the artificial society.
 * An agent is no longer binded to a single agent address but 
 * has as many agent addresses as holden positions in the artificial society.
 * see {@link AgentAddress} for more information.</li>
 * <br>
 * <li>With respect to the previous change, a <code><i>withRole</i></code> version
 * of all the messaging methods has been added. 
 * See {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)} for an example
 * of such a method.</li>
 * <br><li>A replying mechanism has been introduced through 
 * <code><i>SendReply</i></code> methods. 
 * It enables the agent with the possibility of replying directly to a given message.
 * Also, it is now possible to get the reply to a message, or to wait for a reply 
 * ( for {@link Agent} subclasses only as they are threaded)
 * See {@link AbstractAgent#sendReply(Message, Message)}
 * for more details.</li>
 * <br><li>Agents now have a <i>formal</i> state during a MadKit session.
 * See the {@link AbstractAgent#getState()} method for detailed information.</li>
 * <br><li>One of the most convenient improvement of v.5 is the logging mechanism which is provided.
 * See the {@link AbstractAgent#logger} attribute for more details.</li>
 * <br><li>Internationalization is being made (fr_fr and en_us for now).</li>
 * <p>
 * @author Fabien Michel
 * @author Jacques Ferber
 * @author Jason Mahdjoub
 * @since MadKit 4.0
 * @since MadKitGroupExtension 1.0
 * @version 1.0
 */

public class MadKitGroupExtension
{
    
	/**
	 * This main could be used to
	 * launch a new kernel using predefined options.
	 * The new kernel automatically ends when all
	 * the agents living on this kernel are done.
	 * So the JVM automatically quits if there is no
	 * other remaining threads.
	 * 
	 * Basically this call just instantiates a new kernel like this:
	 * 
	 * <pre>
	 * public static void main(String[] options) {
	 * 	new Madkit(options);
	 * }
	 * </pre>
	 * 
	 * So, this main can be used as a MAS application entry point
	 * in two ways :
	 * <p>
	 * (1) From the command line:
	 * <p>
	 * For instance, assuming that your classpath is already set correctly:
	 * <p>
	 * <tt>>java madkit.kernel.Madkit agentLogLevel INFO --launchAgents
	 * madkit.marketorg.Client,20,true;madkit.marketorg.Broker,10,true;madkit.marketorg.Provider,20,true;</tt>
	 * <p>
	 * (2) It can be used programmatically anywhere, especially within main method of agent classes to ease their launch within an IDE.
	 * <p>
	 * Here is an example of how it can be used in this way:
	 * <p>
	 * 
	 * <pre>
	 * 
	 * public static void main(String[] args) {
	 * 	String[] argss = { LevelOption.agentLogLevel.toString(),
	 * 			&quot;FINE&quot;,
	 * 			Option.launchAgents.toString(),// gets the -- launchAgents string
	 * 			Client.class.getName() + &quot;,true,20;&quot; + Broker.class.getName()
	 * 					+ &quot;,true,10;&quot; + Provider.class.getName() + &quot;,false,20&quot; };
	 * 	Madkit.main(argss);// launching the application
	 * }
	 * </pre>
	 * 
	 * @param options the options which should be used to launch Madkit:
	 *           see {@link LevelOption}, {@link BooleanOption} and {@link Option}
	 * @throws IllegalArgumentException when one of the agent class given as parameter is not a MadKitGroupExtension agent, but only a MadKit agent.
	 * @since MadKitGroupExtension 1.0
	 */
    public static void main(String options[])
    {
	String v=VERSION.toStringShort();
	int decal=(39-v.length())/2;
	StringBuffer version=new StringBuffer();
	for (int i=0;i<decal;i++)
	    version.append(" ");
	version.append(v);
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy");
	System.out.println("\t---------------------------------------\n" +
			   "\t         MadKitGroupExtension" +
			   "\n\n" +
			   "\t"+version.toString()+
			   "\n\n" +
			   "\t  MadKitGroupExtension Team " +sdf.format(VERSION.getProjectStartDate())+"-"+sdf.format(VERSION.getProjectEndDate()) +"\n" +
			   "\n" +
			   "\t---------------------------------------\n");
	for (int i=0;i<options.length;i++)
	{
	    if (options[i].equals("--launchAgents"))
	    {
		for (++i;i<options.length;i++)
		{
		    if (options[i].startsWith("--"))
		    {
			--i;
			break;
		    }
		    else
		    {
			try
			{
			    Class<?> c=Class.forName(options[i]);
			    
			    if (!MKGEAbstractAgent.class.isAssignableFrom(c))
			    {
				throw new IllegalArgumentException("The class to load "+options[i]+" given as parameter must be an agent which inherits from a MadKitGroupExtension class agent (and not from a MadKit class agent) !");
			    }
			}
			catch (ClassNotFoundException e)
			{
			}
			
		    }
		}
	    }
	}
	Madkit.main(options);
    }
    
    public static final Version VERSION;
    
    static{
	Calendar c=Calendar.getInstance();
	c.set(2012, 5, 8);
	Calendar c2=Calendar.getInstance();
	c2.set(2014, 2, 4);
	VERSION=new Version("MadKitGroupExtension", 1,3,6, Version.Type.Beta, 1, c.getTime(), c2.getTime());
	
	InputStream is=MadKitGroupExtension.class.getResourceAsStream("build.txt");
	
	if (is!=null)
	{
	    try
	    {
		InputStreamReader isr=new InputStreamReader(is);
		BufferedReader b=new BufferedReader(isr);
		VERSION.setBuildNumber(Integer.parseInt(b.readLine()));
		b.close();
		isr.close();
		is.close();
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	
	VERSION.addCreator(new Person("mahdjoub", "jason"));
	c=Calendar.getInstance();
	c.set(2012, 5, 8);
	VERSION.addDeveloper(new PersonDeveloper("mahdjoub", "jason", c.getTime()));
	

	c=Calendar.getInstance();
	c.set(2014, 2, 4);
	Description d=new Description(1,3,6,Version.Type.Beta, 1, c.getTime());
	d.addItem("Adding function 'AbstractAgent.replaceAutoRequestedGroup(AbstractGroup _old_group, AbstractGroup _new_group)'");
	d.addItem("Removing constructor 'Group(boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community, String ..._groups)' ");
	d.addItem("Adding constructor 'Group(boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community, String ..._groups)' ");
	d.addItem("Altering function 'Group.getParent()'");
	d.addItem("Altering function 'Group.getParentGroups()'");
	d.addItem("Altering function 'Group.getParentWithItsSubGroups()'");
	d.addItem("Altering function 'Group.getRepresentedGroups(KernelAddress)'");
	d.addItem("Altering function 'Group.getSubGroups(KernelAddress)'");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2014, 2, 2);
	d=new Description(1,3,5,Version.Type.Beta, 1, c.getTime());
	d.addItem("Adding constructor public Group(boolean _useSubGroups, boolean _isDistributed, Gatekeeper _theIdentifier, boolean _isReserved, String _community, String ..._groups)");
	d.addItem("Adding function Group.getSubGroup(boolean, String...)");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2014, 1, 28);
	d=new Description(1,3,4,Version.Type.Beta, 1, c.getTime());
	d.addItem("Adaptation of the function 'launchNode(Node)' to MadKitGroupExtension");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2014, 1, 26);
	d=new Description(1,3,3,Version.Type.Beta, 1, c.getTime());
	d.addItem("Including MadKit 5.0.3.2");
	d.addItem("Adding function AbstractAgent.getMyMKGERoles(Group)");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2013, 10, 15);
	d=new Description(1,3,2,Version.Type.Beta, 1, c.getTime());
	d.addItem("Including MadKit 5.0.3.");
	d.addItem("function Watcher.getProbes() is replaced by function Watcher.probes().");
	d.addItem("Adding function AbstractAgent.hasRole(Group, String).");
	d.addItem("Adding function AbstractAgent.getMyMKGEGroups(String).");
	d.addItem("Altering function AbstractAgent.bucketModeRequestRole(Group, String), see documentation.");
	d.addItem("The license of MadKitGroupExtension has changed from 'LGPL' to 'GPL'. Indeed, because MadKit has a GPL License, the LGPL license has no effect.");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2013, 8, 11);
	d=new Description(1,3,1,Version.Type.Beta, 1, c.getTime());
	d.addItem("Including MadKit 5.0.2.");
	d.addItem("Updating functions launchAgentBucket and adding some others according MadKit 5.0.2.");
	d.addItem("Adding functions bucketModeRequestRole(...) according MadKit 5.0.2.");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2013, 8, 6);
	d=new Description(1,3,0,Version.Type.Beta, 1, c.getTime());
	d.addItem("Add the function 'public void autoRequestRole(AbstractGroup _group, String role)' in the class AbstractAgent.");
	d.addItem("Add the function 'public void autoRequestRole(AbstractGroup _group, String _role, Object _passKey)' in the class AbstractAgent.");
	d.addItem("Add the function 'public void removeAutoRequestedRole(String role)' in the class AbstractAgent.");
	d.addItem("Add the function 'public void removeAutoRequestedGroup(AbstractGroup _group)' in the class AbstractAgent.");
	d.addItem("Correcting a bug concerning the function Group.getSubGroups() when several MadKitKernel are used.");
	d.addItem("Correcting a bug of Group referencing when using AbstractAgent.launchAgentBucket(...).");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2013, 7, 27);
	d=new Description(1,2,2,Version.Type.Beta, 1, c.getTime());
	d.addItem("Add a function hashCode() into the class Group.");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2013, 7, 12);
	d=new Description(1,2,1,Version.Type.Beta, 1, c.getTime());
	d.addItem("Completing MadKitGroupExtensionAgent interface.");
	d.addItem("Renaming MadKitGroupExtensionAgent to MKGEAbstractAgent.");
	d.addItem("Adding interfaces MKGEAgent, MKGEScheduler, MKGEWatcher, MKGESwingViewer.");
	d.addItem("Deprecating the function allProbes() in the class Watcher.");
	d.addItem("Adding the function getProbes() in the class Watcher.");
	d.addItem("Updating the Group class : now its autodetect in the given parameters if this last are a hierarchy of groups or a MadKit path.");
	VERSION.addDescription(d);

	
	c=Calendar.getInstance();
	c.set(2013, 7, 4);
	d=new Description(1,2,0,Version.Type.Beta, 1, c.getTime());
	d.addItem("The group classes are now serializable.");
	VERSION.addDescription(d);
	
	
	c=Calendar.getInstance();
	c.set(2013, 2, 14);
	d=new Description(1,1,0,Version.Type.Beta, 1, c.getTime());
	d.addItem("Now MadkitGroupExtension is compiled with Java 7.");
	d.addItem("Updatating Activator and GenericBehaviorActivator classes considering the last version of MadKit");
	
	d.addItem("adding madkitgroupextension.kernel.AbstractAgent.getExistingGroupsInKernel(String) ");
	d.addItem("adding madkitgroupextension.kernel.AbstractAgent.getExistingRoles(AsbtractGroup)");
	d.addItem("adding madkitgroupextension.simulation.probe.SingleAgentProbe class");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2012, 9, 12);
	d=new Description(1,0,0,Version.Type.Alpha, 4, c.getTime());
	d.addItem("changing madkit.kernel.AbstractAgent.launchAgentBucketWithRoles(String, int, Role...) to madkit.kernel.AbstractAgent.launchAgentBucket(String, int, Role...)");
	d.addItem("changing madkit.kernel.AbstractAgent.launchAgentBucketWithRoles(List<MadKitGroupExtensionAgent>, Role...) to madkit.kernel.AbstractAgent.launchAgentBucket(List<MadKitGroupExtensionAgent>, Role...)");
	d.addItem("madkitgroupextension.simulation has been split, adding madkitgroupextension.simulation.activator and madkitgroupextension.simulation.probe. Use organize imports in your IDE to ease the refactoring.");
	d.addItem("adding madkitgroupextension.simulation.viewer.SwingViewer class");
	d.addItem("adding madkitgroupextension.simulation.probe.SingleAgentProbe class");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2012, 5, 14);
	d=new Description(1,0,0,Version.Type.Alpha, 3, c.getTime());
	d.addItem("Increasing the retro-compatibility with old MadKit functions");
	VERSION.addDescription(d);

	c=Calendar.getInstance();
	c.set(2012, 5, 10);
	d=new Description(1,0,0,Version.Type.Alpha, 2, c.getTime());
	d.addItem("Correcting a bug in the functions AbstractAgent.launchAgentBucketWithRoles");
	VERSION.addDescription(d);
    }

    
    
}
