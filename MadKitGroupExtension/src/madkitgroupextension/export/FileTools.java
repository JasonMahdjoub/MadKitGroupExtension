/*
 * source : http://romain.novalan.fr/wiki/JAVA_copier_un_dossier,_manipulation_arborescence
 */
package madkitgroupextension.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
 
/**
 * FileTool is a class which provides some methods used to work on Files and Folders.
 */
public final class FileTools {
 
	/**
	 * FileTools Constructor.
	 */
	public FileTools() {
	}
 
	/**
	 * Check if a specified file path is a folder and create a folder if it does
	 * not exist.
	 * 
	 * @param folderPath A folder path.
	 */
	public static void checkFolder(String folderPath) {
		File file = new File(folderPath);
		if (!(file.isDirectory())) {
			file.mkdir();
		}
	}
 
	/**
	 * Delete a directory.
	 * 
	 * @param path A folder path.
	 */
	public static void deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		path.delete();
	}
 
	/**
	 * Move a file from a source to a destination. If the moving by using the
	 * renameTo method does not work, it used the copy method.
	 * 
	 * @param src Source file path.
	 * @param dst Destination file path.
	 * @throws IOException when an IO exception occurs
	 */
	public static void move(String src, String dst) throws IOException {
		File source = new File(src);
		File destination = new File(dst);
		// Try to use renameTo
		boolean result = source.renameTo(destination);
		if (!result) {
			// Copy
			copy(src, dst);
		} 
	}
 
	/**
	 * Copy a file from a source to a destination.
	 * 
	 * @param src Source file path.
	 * @param dst Destination file path.
	 * @throws IOException  when an IO exception occurs
	 */
	public static void copy(String src, String dst) throws IOException {
		File source = new File(src);
		File destination = new File(dst);
		FileInputStream sourceFile = null;
		FileOutputStream destinationFile = null;
		destination.createNewFile();
		sourceFile = new FileInputStream(source);
		destinationFile = new java.io.FileOutputStream(destination);
		// Read by 0.5MB segment.
		byte buffer[] = new byte[512 * 1024];
		int nbRead;
		while ((nbRead = sourceFile.read(buffer)) != -1) {
			destinationFile.write(buffer, 0, nbRead);
		}
		sourceFile.close();
		destinationFile.close();
	}
 
	/**
	 * Copy all files and directories from a Folder to a destination Folder.
	 * Must be called like: listAllFilesInFolder(srcFolderPath, "", srcFolderPath,
	 * destFolderPath)
	 * 
	 * @param currentFolder Used for the recursive called.
	 * @param relatedPath Used for the recursive called.
	 * @param sourceFolder Source directory.
	 * @param destinationFolder Destination directory.
	 * @throws Exception when an exception occurs 
	 */
	public static void copyFolderToFolder(String currentFolder,
			String relatedPath, String sourceFolder, String destinationFolder) throws Exception {
		// Current Directory.
		File current = new File(currentFolder);
		if (current.isDirectory()) {
			// List all files and folder in the current directory.
			File[] list = current.listFiles();
			if (list != null) {
				// Read the files list.
				for (int i = 0; i < list.length; i++) {
					// Create current source File
					File tf = new File(sourceFolder + relatedPath + "/"
							+ list[i].getName());
					// Create current destination File
					File pf = new File(destinationFolder + relatedPath + "/"
							+ list[i].getName());
					if (tf.isDirectory() && !pf.exists()) {
						// If the file is a directory and does not exit in the
						// destination Folder.
						// Create the directory.
						pf.mkdir();
						copyFolderToFolder(tf.getAbsolutePath(), relatedPath
								+ "/" + tf.getName(), sourceFolder,
								destinationFolder);
					} else if (tf.isDirectory() && pf.exists()) {
						// If the file is a directory and exits in the
						// destination Folder.
						copyFolderToFolder(tf.getAbsolutePath(), relatedPath
								+ "/" + tf.getName(), sourceFolder,
								destinationFolder);
					} else if (tf.isFile()) {
						// If it is a file.
						copy(sourceFolder + relatedPath + "/"
								+ list[i].getName(), destinationFolder
								+ relatedPath + "/" + list[i].getName());
					} else {
					    throw new Exception("Messages.file_problem + tf.getAbsolutePath()");
					}
				}
			}
		}
	}
 
	/**
	 * Remove a Vector of files on the local machine.
	 * 
	 * @param files A vector of file paths.
	 * @param projectDirectory The project Directory.
	 * @throws Exception when an exception occurs
	 */
	public static void removeFiles(Vector<String> files,
			String projectDirectory) throws Exception {
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			removeFile(it.next(), projectDirectory);
		}
	}
 
	/**
	 * Remove a file in a specified root directory.
	 * 
	 * @param file A file path.
	 * @param rootDirectory A root directory.
	 * @throws Exception when an exception occurs 
	 */
	public static void removeFile(String file, String rootDirectory) throws Exception {
		// Remove a file on the local machine
		if (file.equalsIgnoreCase("") || file == null) {
		}
		File dir = new File(rootDirectory);
		if (!dir.isDirectory()) {
			throw new Exception(rootDirectory);
		} else {
			String filename;
			if (rootDirectory.charAt(rootDirectory.length() - 1) == '/') {
				filename = rootDirectory + file;
			} else {
				filename = rootDirectory + "/" + file;
			}
			File f = new File(filename);
			if (f.exists()) {
				f.delete();
			} 
		}
	}
	
	public static ArrayList<File> getTree(String directory)
	{
	    ArrayList<File> res=new ArrayList<File>();
	    File f=new File(directory);
	    res.add(f);
	    for (File f2 : f.listFiles())
	    {
		if (f2.isDirectory())
		{
		    res.addAll(getTree(f2.getPath()));
		}
	    }
	    return res;
	}
	private static final int BUFFER = 2048;
	
	public static void unzipFile(File _zip_file, File _directory_dst) throws IOException
	{
	    if (!_directory_dst.exists())
		throw new IllegalAccessError("The directory of destination does not exists !");
	    if (!_directory_dst.isDirectory())
		throw new IllegalAccessError("The directory of destination is not a directory !");
	    
	    BufferedOutputStream dest = null;
	    FileInputStream fis = new FileInputStream(_zip_file);
	    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	    ZipEntry entry;
	    while((entry = zis.getNextEntry()) != null) {
		if (entry.isDirectory())
		{
		    new File(_directory_dst, entry.getName()).mkdir();
		}
		else
		{
	             //System.out.println("Extracting: " +entry);
	             int count;
	             byte data[] = new byte[BUFFER];
	             // write the files to the disk
	             //System.out.println("Extracting: " +new File(_directory_dst, entry.getName()));
	             FileOutputStream fos = new FileOutputStream(new File(_directory_dst, entry.getName()));
	             dest = new BufferedOutputStream(fos, BUFFER);
	             while ((count = zis.read(data, 0, BUFFER)) 
	               != -1) {
	                dest.write(data, 0, count);
	             }
	             dest.flush();
	             dest.close();
	          }
	    }
	    zis.close();
	}
	
	private static String getRelativePath(String base, String path)
	{
	    if (path.startsWith(base))
		return path.substring(base.length());
	    else
		return null;
	}
	private static String transformToDirectory(String _dir)
	{
	    if (_dir.endsWith("/"))
		return _dir;
	    else 
		return _dir+"/"; 
	}
	private static void zipDirectory(ZipOutputStream out, File _directory, String base_directory) throws IOException
	{
	    byte data[] = new byte[BUFFER];
	    for (File f : _directory.listFiles())
	    {
	            //System.out.println("Adding: "+files[i]);
	            if (f.isDirectory())
	            {
	        	ZipEntry entry = new ZipEntry(transformToDirectory(getRelativePath(base_directory, f.getAbsolutePath())));
	        	out.putNextEntry(entry);
	        	zipDirectory(out, f, base_directory);
	            }
	            else
	            {
	        	FileInputStream fi = new FileInputStream(f);
		        BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
	        	ZipEntry entry = new ZipEntry(getRelativePath(base_directory, f.getAbsolutePath()));
	        	out.putNextEntry(entry);
	        	int count;
		        while((count = origin.read(data, 0, BUFFER)) != -1) {
		               out.write(data, 0, count);
		            }
		        origin.close();
	            }
	    }
	}
	public static void zipDirectory(File _directory, boolean _include_directory, File _zipfile) throws IOException
	{
	    if (!_directory.exists())
		throw new IllegalAccessError("The directory "+_directory+" does not exists !");
	    if (!_directory.isDirectory())
		throw new IllegalAccessError("The directory "+_directory+" is not a directory !");
	    FileOutputStream dest = new FileOutputStream(_zipfile);
	    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	    
	    if (_include_directory)
	    {
		String dir=_directory.getAbsolutePath();
		int l=dir.lastIndexOf(_directory.getName());
		String base=dir.substring(0, l);
		
        	ZipEntry entry = new ZipEntry(transformToDirectory(getRelativePath(base, _directory.getAbsolutePath())));
        	out.putNextEntry(entry);
        	
        	zipDirectory(out, _directory, base);
	    }
	    else
	    {
		String base=_directory.getAbsolutePath();
		if (!base.endsWith("/"))
		    base=base+"/";
		zipDirectory(out, _directory, base);
	    }
	    
	    out.close();
	}
	public static void removeDirectory(FTPClient ftpClient, String _directory) throws IOException
	{
	    if (!_directory.endsWith("/"))
		_directory+="/";
	    FTPListParseEngine ftplpe=ftpClient.initiateListParsing(_directory);
	    FTPFile files[]=ftplpe.getFiles();
	    for (FTPFile f : files)
	    {
		if (!f.getName().equals(".") && !f.getName().equals(".."))
		{
		    if (f.isDirectory())
			removeDirectory(ftpClient, _directory+f.getName()+"/");
		    else
			ftpClient.deleteFile(_directory+f.getName());
		}
	    }
	    ftpClient.removeDirectory(_directory);
	    
	}
}