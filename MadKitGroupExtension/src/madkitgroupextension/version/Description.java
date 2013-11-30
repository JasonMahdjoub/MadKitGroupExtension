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
import java.util.Vector;

import madkitgroupextension.version.Version.Type;

public class Description
{
    
    private Vector<String> m_items=new Vector<String>();
    private int m_major=0;
    private int m_minor=0;
    private int m_revision=0;
    private Version.Type m_type=null;
    private int m_alpha_beta_version=0;
    private Date m_date;
    
    public Description(int _major, int _minor, int _revision, Version.Type _type, int _alpha_beta_version, Date _date)
    {
	m_major=_major;
	m_minor=_minor;
	m_revision=_revision;
	m_type=_type;
	m_alpha_beta_version=_alpha_beta_version;
	m_date=_date;
    }
    public int getMajor()
    {
	return m_major;
    }
    public int getMinor()
    {
	return m_minor;
    }
    public int getRevision()
    {
	return m_revision;
    }
    public Type getType()
    {
	return m_type;
    }
    
    public int getAlphaBetaVersion()
    {
	return m_alpha_beta_version;
    }
    public Date getDate()
    {
	return m_date;
    }
    public void addItem(String d)
    {
	m_items.add(d);
    }
    public Vector<String> getItems()
    {
	return m_items;
    }
    public String getHTML()
    {
	StringBuffer s=new StringBuffer();
	s.append("<BR><H2>"+m_major+"."+m_minor+"."+m_revision+" "+m_type+((m_type.equals(Type.Alpha) || m_type.equals(Type.Beta))?" "+Integer.toString(m_alpha_beta_version):"")+" ("+DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE).format(m_date)+")</H2>");
	s.append("<ul>");
	for (String d : m_items)
	{
	    s.append("<li>");
	    s.append(d);
	    s.append("</li>");
	}
	s.append("</ul>");
	return s.toString();
    }
}
