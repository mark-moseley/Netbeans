/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.web.taglib;

/** 
 *
 * @author  Milan Kuchtiak 
 * @version 1.0
 */

import java.io.IOException;

import org.openide.loaders.UniFileLoader;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes .tld files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
*/

public final class TLDLoader extends UniFileLoader {
    
    public static final String tldExt = "tld"; //NOI18N
    private static final String REQUIRED_MIME_PREFIX = "text/x-tld"; //NOI18N
    
    private static final long serialVersionUID = -7367746798495347598L;
    
    /** Constructor */
    public TLDLoader() {
	super("org.netbeans.modules.web.taglibed.TLDDataObject"); // NOI18N
    }
    
     /** Does initialization. Initializes display name,
     * extension list and the actions. */
    
    protected void initialize () {
    	super.initialize();
	ExtensionList ext = new ExtensionList();
	ext.addExtension(tldExt);
	setExtensions(ext);
    }
    
    protected FileObject findPrimaryFile(FileObject fo) {
      String mimeType = fo.getMIMEType();
      if (mimeType==null) return null;
      else 
        return (mimeType.startsWith(REQUIRED_MIME_PREFIX)?fo:null);
    }
    
    protected MultiDataObject createMultiObject(final FileObject fo)
	throws IOException {
	MultiDataObject obj = new TLDDataObject(fo, this);
	return obj;
    }

    protected String defaultDisplayName () {
	return NbBundle.getMessage (TLDLoader.class, "TLD_loaderName");
    }
    
    protected org.openide.util.actions.SystemAction[] defaultActions () {
        return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    SystemAction.get (FileSystemAction.class),
	    null,
	    SystemAction.get(CutAction.class),
	    SystemAction.get(CopyAction.class),
	    SystemAction.get(PasteAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	    SystemAction.get(RenameAction.class),
	    null,
	    SystemAction.get(ToolsAction.class),
	    SystemAction.get(PropertiesAction.class),
	};
    }

}
