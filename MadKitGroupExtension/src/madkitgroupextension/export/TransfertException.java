package madkitgroupextension.export;

import java.io.File;
import java.io.IOException;

public class TransfertException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -3664877054570889927L;

    public final File current_file_transfert;
    public File current_directory_transfert;
    public final IOException original_exception;
    
    public TransfertException(File _current_file_transfert, File _current_directory_transfert, IOException _original_exception)
    {
	current_file_transfert=_current_file_transfert;
	current_directory_transfert=_current_directory_transfert;
	original_exception=_original_exception;
    }
}
