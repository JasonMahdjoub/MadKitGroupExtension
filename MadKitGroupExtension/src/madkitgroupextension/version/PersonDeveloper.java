/*
 * CIPORG (created by Jason MAHDJOUB (jason.mahdjoub@free.fr)) Copyright (c)
 * 2011, JBoss Inc., and individual contributors as indicated by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
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

package madkitgroupextension.version;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonDeveloper extends Person
{
    private Date m_date_begin_development;
    
    public PersonDeveloper(String _name, String _first_name, Date _date_begin_development)
    {
	super(_name, _first_name);
	m_date_begin_development=_date_begin_development;
    }
    
    public Date getDateBeginDevelopment()
    {
	return m_date_begin_development;
    }
    
    @Override public String toString()
    {
	
	return m_first_name+" "+m_name+" (Entred in the team at "+DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE).format(m_date_begin_development)+")";
    }
    
}
