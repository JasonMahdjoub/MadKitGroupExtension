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

/**
 * This class enables to encapsulate a role and a group (and its community).
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitGroupExtension 1.0
 * @see Group
 * @see AbstractGroup
 */
public class Role
{
    /**
     * The represented group
     */
    private final Group m_group;
    
    /**
     * The represented role
     */
    private final String m_role;
    
    /**
     * The concatenation of the cummunity, the group and the group, separated by semicolons.
     */
    private final String m_group_role;
    
    /**
     * Constructing a Role assiciated with a group and a community.
     *  
     * @param _group the group 
     * @param _role the role
     * @see Group
     * @see AbstractGroup
     */
    public Role (Group _group, String _role)
    {
	if (_role.contains(";"))
	    throw new IllegalArgumentException("The role given as parameter ("+_role+") cannot contains a ';' character !");
	m_group=_group;
	m_role=_role;
	m_group_role=m_group.getCommunity()+","+m_group.getPath()+","+m_role;
    }
    
    
    /**
     * Return the represented group.
     * 
     * @return the represented group
     */
    public Group getGroup()
    {
	return m_group; 
    }
    
    
    /**
     * Return the represented role.
     * 
     * @return the represented role
     */
    public String getRole()
    {
	return m_role;
    }
    
    @Override public String toString()
    {
	return m_group_role;
    }
    
    public boolean equals(Role _r)
    {
	return m_group_role.equals(m_group);
    }

    @Override public boolean equals(Object _r)
    {
	return _r==null?false:((_r instanceof Role)?m_group_role.equals(m_group):false);
    }
}
