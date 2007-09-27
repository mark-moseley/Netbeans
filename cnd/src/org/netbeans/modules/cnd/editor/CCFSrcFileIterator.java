/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package  org.netbeans.modules.cnd.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public class CCFSrcFileIterator implements TemplateWizard.Iterator {
    /** Holds list of event listeners */
    private static Vector listenerList = null;

    private WizardDescriptor.Panel targetChooserDescriptorPanel;

    public WizardDescriptor.Panel current() {
	return targetChooserDescriptorPanel;
    }
    
    public boolean hasNext() {
	return false;
    }

    public boolean hasPrevious() {
	return false;
    }
    
    public synchronized void nextPanel() {
    }
    
    public synchronized void previousPanel() {
    }

    public void initialize (TemplateWizard wiz) {
	targetChooserDescriptorPanel = wiz.targetChooser();
    }
    
    public void uninitialize (TemplateWizard wiz) {
    }
    
    public Set instantiate (TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder ();
        DataObject template = wiz.getTemplate ();
	String ext = template.getPrimaryFile().getExt();

	String filename = wiz.getTargetName();
	if (filename != null && ext != null) {
	    if (filename.endsWith("." + ext)) { // NOI18N
		// strip extension, it will be added later ...
		filename = filename.substring(0, filename.length()-(ext.length()+1));
	    }
	}

	DataObject result = template.createFromTemplate(targetFolder, filename);


	if (result != null) {
	    fireWizardEvent(new EventObject(result));
	    OpenCookie open = (OpenCookie) result.getCookie (OpenCookie.class);
	    if (open != null) {
		open.open ();
	    }
	}

	return Collections.singleton(result);
    }

    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
       
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public String name() {
	return ""; // NOI18N ?????
    }

    /* ------------------------------------------*/

    protected static void fireWizardEvent(EventObject e)
    {
	Vector listeners = getListenerList();

	for (int i = listeners.size()-1; i >= 0; i--) {
	    ((SrcFileWizardListener)listeners.elementAt(i)).srcFileCreated(e);
	}
    }

    private static Vector getListenerList() {
	if (listenerList == null) {
	    listenerList = new Vector(0);
	}
	return listenerList;
    }

    public static void addSrcFileWizardListener(SrcFileWizardListener l) {
	getListenerList().add(l);
    }

    public static void removeSrcFileWizardListener(SrcFileWizardListener l) {
	getListenerList().remove(l);
    }

}

