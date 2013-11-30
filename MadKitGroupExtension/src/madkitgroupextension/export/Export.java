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

package madkitgroupextension.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import madkitgroupextension.kernel.MadKitGroupExtension;
import madkitgroupextension.version.PersonDeveloper;
import madkitgroupextension.version.Version;

public class Export
{
    private final static String MadKitPath="../MadKit/";
    private final static String ExportPathTmp=".export/";
    private final static String ExportPathFinal="export/";
    
    private final static String FTPURL="ftpperso.free.fr";
    private final static int FTPPORT=21;
    private final static String FTPLOGIN="madkitgroupextension";
    
    final private boolean m_export_source;
    final private boolean m_include_madkit;
    final private boolean m_export_only_jar_file;
    
    private String m_madkit_version=null;
    private File m_madkit_jar_file=null;
    
    public Export() throws IllegalAccessException, NumberFormatException, IOException
    {
	this(true, true, false);
    }
    public Export(boolean _export_source, boolean _include_madkit, boolean _export_only_jar_file) throws IllegalAccessException, NumberFormatException, IOException
    {
	m_export_source=_export_source;
	m_include_madkit=_include_madkit;
	m_export_only_jar_file=_export_only_jar_file;
	
	File current_directory=new File(MadKitPath);
	if (!current_directory.exists())
	    throw new IllegalAccessException("The Madkit path is invalid: "+MadKitPath);
	Pattern pattern=Pattern.compile("^madkit-.*\\.jar$");
	for (File f : current_directory.listFiles())
	{
	    
	    if (f.isFile() && pattern.matcher(f.getName()).matches())
	    {
		m_madkit_jar_file=f;
		m_madkit_version=f.getName().substring(7, f.getName().lastIndexOf("."));
		break;
	    }
	}
	if (m_madkit_jar_file==null)
	    throw new IllegalAccessException("Impossible to found the MadKit jar file !");
	
	File f=new File("build.txt");
	if (f.exists())
	{
	    FileReader fr=new FileReader(f);
	    BufferedReader bf=new BufferedReader(fr);
	    MadKitGroupExtension.VERSION.setBuildNumber(Integer.parseInt(bf.readLine()));
	    bf.close();
	    fr.close();
	}
    }
    
    public String getManifest()
    {
	String res="Manifest-Version: 1.0\n" +
	"Description: "+MadKitGroupExtension.VERSION.getProgramName()+", an extension to MadKit "+m_madkit_version+" (the Multiagent Development Kit: An API for building Multiagent applications as artificial societies)\n"+
	"Version: "+MadKitGroupExtension.VERSION.toStringShort()+"\n"+
	"Author: ";
	boolean first=true;
	for (PersonDeveloper p : MadKitGroupExtension.VERSION.getDevelopers())
	{
	    if (first)
		first=false;
	    else
		res+=", ";
	    res+=p.getFirstName()+" "+p.getName();
	}
	res+="\nBuilt-By: Jason Mahdjoub\n"+
	"Main-Class: madkitgroupextension.kernel.MadKitGroupExtension\n";
	/*if (m_include_madkit)
	{
	    res+="Class-Path:"+this.getMadKitClassPath();	    
	}*/
	return res;
    }
    public void createManifestFile(File f) throws IOException
    {
	FileWriter fw=new FileWriter(f);
	BufferedWriter b=new BufferedWriter(fw);
	b.write(getManifest());
	b.flush();
	b.close();
	fw.close();
    }
    
    public static void saveAndIncrementBuild() throws IOException
    {
	MadKitGroupExtension.VERSION.setBuildNumber(MadKitGroupExtension.VERSION.getBuildNumber()+1);
	FileWriter fw=new FileWriter(new File("build.txt"));
	BufferedWriter b=new BufferedWriter(fw);
	b.write(Integer.toString(MadKitGroupExtension.VERSION.getBuildNumber()));
	b.flush();
	b.close();
	fw.close();
    }
    
    public void createBuildFile(File f) throws IOException
    {
	FileWriter fw=new FileWriter(f);
	BufferedWriter b=new BufferedWriter(fw);
	b.write(Integer.toString(MadKitGroupExtension.VERSION.getBuildNumber()));
	b.flush();
	b.close();
	fw.close();
    }
    
    public void createVersionFile(File f) throws IOException
    {
	FileWriter fw=new FileWriter(f);
	BufferedWriter b=new BufferedWriter(fw);
	b.write(MadKitGroupExtension.VERSION.getHTMLCode());
	b.flush();
	b.close();
	fw.close();
    }
    
    public void saveIndexList(File dst, File madkitindexfile) throws IOException
    {
	FileWriter fw=new FileWriter(dst);
	BufferedWriter bw=new BufferedWriter(fw);
	
	if (m_include_madkit)
	{
	    FileReader fr=new FileReader(madkitindexfile);
	    BufferedReader bf=new BufferedReader(fr);
	    String line=null;
	    while ((line=bf.readLine())!=null)
		bw.write(line+"\n");
	    bf.close();
	    fr.close();
	}
	else
	{
	    bw.write("JarIndex-Version: 1.0\n\n");
	}
	int l=(ExportPathTmp+"jardir/").length();
	for (File f : FileTools.getTree(ExportPathTmp+"jardir/madkitgroupextension/"))
	{
	    bw.write(f.getPath().substring(l)+"\n");
	}
	bw.flush();
	bw.close();
	fw.close();
    }
    public String getJarFileName()
    {
	return "mkge-"+
		Integer.toString(MadKitGroupExtension.VERSION.getMajor())+
		"."+
		Integer.toString(MadKitGroupExtension.VERSION.getMinor())+
		"."+
		Integer.toString(MadKitGroupExtension.VERSION.getRevision())+
		MadKitGroupExtension.VERSION.getType()+
		((MadKitGroupExtension.VERSION.getType().equals(Version.Type.Beta) || MadKitGroupExtension.VERSION.getType().equals(Version.Type.Alpha))?Integer.toString(MadKitGroupExtension.VERSION.getAlphaBetaVersion()):"")+
		(m_include_madkit?("+madkit-"+m_madkit_version):"")+
		(m_export_source?"_withsrc":"")+
		".jar";
    }
    
    public String getZipFileName()
    {
	String jarname=getJarFileName();
	return jarname.substring(0, jarname.length()-4)+"_withdoc.zip";
    }
    
    public static void execExternalProcess(String command, final boolean screen_output, final boolean screen_erroutput) throws IOException, InterruptedException
    {
	Runtime runtime = Runtime.getRuntime();
	final Process process = runtime.exec(command);

	// Consommation de la sortie standard de l'application externe dans un Thread separe
	new Thread() {
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = "";
				try {
					while((line = reader.readLine()) != null) {
						if (screen_output)
						{
						    System.out.println(line);
						}
					}
				} finally {
					reader.close();
				}
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}.start();

	// Consommation de la sortie d'erreur de l'application externe dans un Thread separe
	new Thread() {
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line = "";
				try {
					while((line = reader.readLine()) != null) {
						if (screen_erroutput)
						{
						    System.out.println(line);
						}
					}
				} finally {
					reader.close();
				}
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}.start();
	process.waitFor();
    }
    
    public void process() throws Exception
    {
	//Runtime runtime=Runtime.getRuntime();
	File export_directory_tmp=new File(ExportPathTmp);
	if (export_directory_tmp.exists())
	    FileTools.deleteDirectory(export_directory_tmp);
	//export_directory_tmp=new File(ExportPathTmp);
	export_directory_tmp.mkdir();
	File exportroot=new File(ExportPathFinal);
	if (!exportroot.exists())
	    exportroot.mkdir();
	File exportfinal=getExportFinal();
	if (!exportfinal.exists())
	{
	    File parent=exportfinal.getParentFile();
	    if (!parent.exists())
		parent.mkdir();
	    exportfinal.mkdir();
	}
	    
	
	/*
	 * creating the jar file
	 */
	File jardirectory=new File(ExportPathTmp+"jardir/");
	jardirectory.mkdir();
	File jardirectorymkge=new File(ExportPathTmp+"jardir/madkitgroupextension/");
	jardirectorymkge.mkdir();
	
	File mkgedir=new File("bin/madkitgroupextension/");
	if (!mkgedir.exists())
	    throw new IllegalAccessError("The MKGE directory does not exists !");
	if (!mkgedir.isDirectory())
	    throw new IllegalAccessError("The directory bin/madkitgroupextension sould not be a file !");
	
	FileTools.copyFolderToFolder(mkgedir.getAbsolutePath(), "", mkgedir.getAbsolutePath(), jardirectorymkge.getAbsolutePath());

	if (m_export_source)
	{
	    File mkgedirsrc=new File("src/madkitgroupextension/");
	    if (!mkgedirsrc.exists())
		throw new IllegalAccessError("The MKGE source directory does not exists !");
	    if (!mkgedirsrc.isDirectory())
		throw new IllegalAccessError("The directory src/madkitgroupextension sould not be a file !");
	
	    FileTools.copyFolderToFolder(mkgedirsrc.getAbsolutePath(), "", mkgedirsrc.getAbsolutePath(), jardirectorymkge.getAbsolutePath());
	}
	File exportdir=new File(ExportPathTmp+"jardir/madkitgroupextension/export/");
	if (!exportdir.exists())
	    throw new IllegalAccessError("The path "+exportdir.getAbsolutePath()+" should exist !");
	FileTools.deleteDirectory(exportdir);
	
	
	if (m_include_madkit)
	{
	    File madkitdir=new File(ExportPathTmp+"madkit/");
	    madkitdir.mkdir();
	    
	    //runtime.exec("unzip "+m_madkit_jar_file.getAbsoluteFile()+" -d "+madkitdir.getAbsolutePath());
	    FileTools.unzipFile(m_madkit_jar_file, madkitdir);

	    if (m_export_source)
	    {
		File madkitsrc=new File(MadKitPath+"docs/madkit-"+m_madkit_version+"-src.zip");
		if (!madkitsrc.exists())
		    throw new IllegalAccessError("The source of MadKit was not found !");
		
		//runtime.exec("unzip "+madkitsrc.getAbsoluteFile()+" -d "+madkitdir.getAbsolutePath());
		FileTools.unzipFile(madkitsrc, madkitdir);
	    }
	    
	    FileTools.copyFolderToFolder(madkitdir.getAbsolutePath(), "", madkitdir.getAbsolutePath(), jardirectory.getAbsolutePath());
	}
	
	

	File metainf=new File(ExportPathTmp+"jardir/META-INF/");
	if (!metainf.exists())
	    metainf.mkdir();
	//saveIndexList(new File(metainf, "INDEX.LIST"), new File(ExportPathTmp+"madkit/META-INF/INDEX.LIST"));
	
	createManifestFile(new File(ExportPathTmp+"jardir/META-INF/MANIFEST.MF"));
	createVersionFile(new File(jardirectory, "version.html"));
	createBuildFile(new File(jardirectory, "madkitgroupextension/kernel/build.txt"));
	
	
	File jarfile=new File(export_directory_tmp, getJarFileName());
	FileTools.zipDirectory(jardirectory, false, jarfile);
	//runtime.exec("zip "+jarfile.getAbsoluteFile()+" -r "+jardirectory.getAbsolutePath()+"/").waitFor();
	
	/*
	 * Constructing the ZIP file containing the jar file and the documentation
	 */
	
	if (!m_export_only_jar_file)
	{
	    //generating java doc
	    File srcforjavadoc=new File(ExportPathTmp+"srcforjavadoc/");
	    srcforjavadoc.mkdir();
	    File javadocdirectory=new File(ExportPathTmp+"javadocdir/");
	    javadocdirectory.mkdir();

	    File mkgedirsrc=new File("src/madkitgroupextension/");
	    if (!mkgedirsrc.exists())
		throw new IllegalAccessError("The MKGE source directory does not exists !");
	    if (!mkgedirsrc.isDirectory())
		throw new IllegalAccessError("The directory src/madkitgroupextension sould not be a file !");
		
	    File mkgedirdst=new File(srcforjavadoc, "madkitgroupextension");
	    mkgedirdst.mkdir();
	    FileTools.copyFolderToFolder(mkgedirsrc.getAbsolutePath(), "", mkgedirsrc.getAbsolutePath(), mkgedirdst.getAbsolutePath());
	    FileTools.deleteDirectory(new File(srcforjavadoc, "madkitgroupextension/export"));
	    
	    File madkitsrc=new File(MadKitPath+"docs/madkit-"+m_madkit_version+"-src.zip");
	    if (!madkitsrc.exists())
		throw new IllegalAccessError("The source of MadKit was not found !");
		
	    FileTools.unzipFile(madkitsrc, srcforjavadoc);
	    
	    
	    String command="javadoc -protected -link http://docs.oracle.com/javase/7/docs/api/ -sourcepath "+srcforjavadoc.getAbsolutePath()+" -d "+javadocdirectory.getAbsolutePath()+
		    " -version -author -subpackages madkitgroupextension "+(m_include_madkit?"-subpackages madkit":"");
	    System.out.println("\n*************************\n\n" +
	    		"Generating documentation\n" +
	    		"\n*************************\n\n");
	    execExternalProcess(command, true, true);
	    
	    File zipdir=new File(export_directory_tmp, "zipdir");
	    zipdir.mkdir();
	    
	    //File docsrcdir=new File("doc/");
	    File docdstdir=new File(zipdir, "doc");
	    docdstdir.mkdir();
	    
	    FileTools.copy(new File("COPYING").getAbsolutePath(), (new File(zipdir, "COPYING")).getAbsolutePath());
	    FileTools.copy(jarfile.getAbsolutePath(), new File(zipdir, getJarFileName()).getAbsolutePath());
	    
	    createVersionFile(new File(zipdir, "MKGE_version.html"));
	    
	    
	    FileTools.copyFolderToFolder(javadocdirectory.getAbsolutePath(), "", javadocdirectory.getAbsolutePath(), docdstdir.getAbsolutePath());
	    
	    if (m_include_madkit)
	    {
		File madkitdocsrc=new File(MadKitPath, "docs");
		File madkitdocdst=new File(zipdir, "docsMadKit");
		madkitdocdst.mkdir();
		FileTools.copyFolderToFolder(madkitdocsrc.getAbsolutePath(), "", madkitdocsrc.getAbsolutePath(), madkitdocdst.getAbsolutePath());
		
		FileTools.copy(new File(MadKitPath+"README.html").getAbsolutePath(), (new File(zipdir, "MadKitReadMe.html")).getAbsolutePath());
		FileTools.deleteDirectory(new File(madkitdocdst, "api"));
		(new File(madkitdocdst, "src.zip")).delete();
		
		if (this.m_export_source)
		{
		    File useddocdir=new File("./doc");
		    if(useddocdir.exists())
		    {
			if (useddocdir.isFile())
			    useddocdir.delete();
			else
			    FileTools.deleteDirectory(useddocdir);
		    }
		    useddocdir.mkdir();
		    FileTools.copyFolderToFolder(docdstdir.getAbsolutePath(), "", docdstdir.getAbsolutePath(), useddocdir.getAbsolutePath());
		    createVersionFile(new File(useddocdir, "MKGE_version.html"));
		}
		
	    }
	    //System.out.println("zip "+new File(exportfinal, getZipFileName()).getAbsoluteFile()+" -r "+zipdir.getAbsolutePath());
	    FileTools.zipDirectory(zipdir, false, new File(exportfinal, getZipFileName()));
	    //runtime.exec("zip "+new File(exportfinal, getZipFileName()).getAbsoluteFile()+" -r "+zipdir.getAbsolutePath());
	}
	else
	{
	    FileTools.move(jarfile.getAbsolutePath(), new File(exportfinal, jarfile.getName()).getAbsolutePath());
	}
	
	//saveAndIncrementBuild();
	FileTools.deleteDirectory(export_directory_tmp);
	
    }
    
    public File getExportFinal()
    {
	return new File(ExportPathFinal+"mkge-"+Integer.toString(MadKitGroupExtension.VERSION.getMajor())+"."+Integer.toString(MadKitGroupExtension.VERSION.getMinor())+"/"+(m_include_madkit?"WithMadKit/":"WithoutMadKit/"));
    }
    
    /*public String getMadKitClassPath()
    {
	return "lib/madkit-5.0.0.16.jar";
    }*/
    
    public static void updateFTP(FTPClient ftpClient, String _directory_dst, File _directory_src) throws IOException
    {
	ftpClient.changeWorkingDirectory("./");
	FTPListParseEngine ftplpe=ftpClient.initiateListParsing(_directory_dst);
	FTPFile files[]=ftplpe.getFiles();
	
	for (File f : _directory_src.listFiles())
	{
	    if (f.isDirectory())
	    {
		if (!f.getName().equals("./") && !f.getName().equals("../"))
		{
		    boolean found=false;
		    for (FTPFile ff : files)
		    {
			if (f.getName().equals(ff.getName()))
			{
			    if (ff.isFile())
			    { 
				ftpClient.deleteFile(_directory_dst+ff.getName());
			    }
			    else
				found=true;
			    break;
			}
		    }
		
		    if (!found)
		    {
			ftpClient.changeWorkingDirectory("./");
			if (!ftpClient.makeDirectory(_directory_dst+f.getName()+"/"))
			    System.err.println("Impossible to create directory "+_directory_dst+f.getName()+"/");
		    }
		    updateFTP(ftpClient, _directory_dst+f.getName()+"/", f);
		}
	    }
	    else
	    {
		FTPFile found=null;
		for (FTPFile ff : files)
		{
		    if (f.getName().equals(ff.getName()))
		    {
			    if (ff.isDirectory())
			    {
				FileTools.removeDirectory(ftpClient, _directory_dst+ff.getName());
			    }
			    else
				found=ff;
			    break;
		    }
		}
		if (found==null || (found.getTimestamp().getTimeInMillis()-f.lastModified())<0 || found.getSize()!=f.length())
		{
		    FileInputStream fis=new FileInputStream(f);
		    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		    if (!ftpClient.storeFile(_directory_dst+f.getName(), fis))
			System.err.println("Impossible to send file: "+_directory_dst+f.getName());
		    //ftpClient.setModificationTime(_directory_dst+f.getName(), new SimpleDateFormat("YYYYMMDDhhmmss").format(new Date(f.lastModified())));
		    fis.close();
		    for (FTPFile ff : ftplpe.getFiles())
		    {
			if (f.getName().equals(ff.getName()))
			{
			    f.setLastModified(ff.getTimestamp().getTimeInMillis());
			    break;
			}
		    }
		}
	    }
	    
	}
	for (FTPFile ff : files)
	{
	    if (!ff.getName().equals(".") && !ff.getName().equals(".."))
	    {
		boolean found=false;
		for (File f : _directory_src.listFiles())
		{
		    if (f.getName().equals(ff.getName()) && f.isDirectory()==ff.isDirectory())
		    {
			found=true;
			break;
		    }
		}
		if (!found)
		{
		    if (ff.isDirectory())
		    {
			FileTools.removeDirectory(ftpClient, _directory_dst+ff.getName());
		    }
		    else
		    {
			ftpClient.deleteFile(_directory_dst+ff.getName());
		    }
		}
	    }
	}
    }
    
    
    private static void sendToWebSite() throws IOException
    {
	System.out.println("Enter your password :");
	byte b[]=new byte[100];
	int l=System.in.read(b);
	String pwd=new String(b, 0, l);
	
	FTPClient ftpClient=new FTPClient();
	ftpClient.connect(FTPURL, 21);
	
	if (ftpClient.isConnected())
	{
	    System.out.println("Connected to server "+FTPURL+" (Port: "+FTPPORT+") !");
	    if(ftpClient.login(FTPLOGIN, pwd))
	    {
		System.out.println("Logged as "+FTPLOGIN+" !");
		System.out.print("Updating...");
		ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
		
		//FTPListParseEngine ftplpe=ftpClient.initiateListParsing("");
		FTPFile files[]=ftpClient.listFiles("");
		FTPFile downloadroot=null;
		FTPFile docroot=null;
		
		for (FTPFile f : files)
		{
		    if (f.getName().equals("downloads"))
		    {
			downloadroot=f;
			if (docroot!=null)
			    break;
		    }
		    if (f.getName().equals("doc"))
		    {
			docroot=f;
			if (downloadroot!=null)
			    break;
		    }
		}
		if (downloadroot==null)
		{
		    //ftpClient.changeWorkingDirectory("/");
		    if (!ftpClient.makeDirectory("downloads"))
		    {
			System.err.println("Impossible to create directory: downloads");
		    }
		}
		if (docroot==null)
		{
		    //ftpClient.changeWorkingDirectory("/");
		    if (!ftpClient.makeDirectory("doc"))
		    {
			System.err.println("Impossible to create directory: doc");
		    }
		}
		
		updateFTP(ftpClient, "downloads/", new File(ExportPathFinal));
		updateFTP(ftpClient, "doc/", new File("./doc"));

		System.out.println("[OK]");
		if (ftpClient.logout())
		{
		    System.out.println("Logged out from "+FTPLOGIN+" succesfull !");		    
		}
		else
		    System.err.println("Logged out from "+FTPLOGIN+" FAILED !");
		
	    }
	    else
		System.err.println("Impossible to log as "+FTPLOGIN+" !");
	    
	    
	    ftpClient.disconnect();
	    System.out.println("Disconnected from "+FTPURL+" !");
	}
	else
	{
	    System.err.println("Impossible to get a connection to the server "+FTPURL+" !");
	}
	
	/*URL url=new URL("ftp://"+FTPLOGIN+":"+pwd+"@"+FTPURL+":"+FTPPORT+"/");
	URLConnection connection=url.openConnection();
	System.out.println(connection.getContent());*/
    }
    
    public static void main(String args[]) throws NumberFormatException, IllegalAccessException, IOException, Exception
    {
	new Export(true, true, false).process();
	new Export(true, false, false).process();
	new Export(false, true, false).process();
	new Export(false, false, false).process();
	Export e=new Export(true, true, true);
	e.process();
	FileTools.copy(new File(e.getExportFinal(), e.getJarFileName()).getAbsolutePath(), new File("madkitgroupextension.jar").getAbsolutePath());
	new Export(false, true, true).process();
	new Export(true, false, true).process();
	new Export(false, false, true).process();
	saveAndIncrementBuild();
	System.out.println("\n**************************\n\nUpdating Web site ? (y[es]|n[o])");
	byte b[]=new byte[100];
	System.in.read(b);
	if (b[0]=='y' || b[0]=='Y')
	{
	    sendToWebSite();
	}
	
	
    }
    
}
