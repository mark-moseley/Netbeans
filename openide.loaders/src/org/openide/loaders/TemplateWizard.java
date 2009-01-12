/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.openide.loaders;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.io.IOException;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.*;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.*;

/** Wizard for creation of new objects from a template.
*
* @author Jaroslav Tulach, Jiri Rechtacek
*/
public class TemplateWizard extends WizardDescriptor {
    /** EA that defines the wizards description */
    private static final String EA_DESCRIPTION = "templateWizardURL"; // NOI18N
    /** EA that defines custom iterator*/
    private static final String EA_ITERATOR = "templateWizardIterator"; // NOI18N
    /** EA that defines resource string to the description instead of raw URL
     * @deprecated
     */
    @Deprecated
    private static final String EA_DESC_RESOURCE = "templateWizardDescResource"; // NOI18N
    
    /** Defines the wizards description */
    private static final String CUSTOM_DESCRIPTION = "instantiatingWizardURL"; // NOI18N
    /** Defines custom iterator */
    private static final String CUSTOM_ITERATOR = "instantiatingIterator"; // NOI18N
    
    /** prefered dimmension of the panels */
    static java.awt.Dimension PREF_DIM = new java.awt.Dimension (560, 350);

    /** panel */
    private Panel<WizardDescriptor> templateChooser;
    /** panel */
    private Panel<WizardDescriptor> targetChooser;
    /** whether to show target chooser */
    private boolean showTargetChooser = true;
    
    /** Iterator for the targetChooser */
    private Iterator targetIterator;
    /** whole iterator */
    private TemplateWizardIteratorWrapper iterator;

    /** values for wizards */
    private DataObject template;
    
    /** root of all templates */
    private DataFolder templatesFolder;

    /** class name of object to create */
    private String targetName = null;
    /** target folder*/
    private DataFolder targetDataFolder;
    /** This is true if we have already set a value to the title format */
    private boolean titleFormatSet = false;

    /** listens on property (steps, index) changes and updates steps pane */
    private PropertyChangeListener pcl;
    
    /** Component which we are listening on for changes of steps */
    private Component lastComp;
    
    private Set<DataObject> newObjects = null;

    private ProgressHandle progressHandle;
    
    /** Creates new TemplateWizard */
    public TemplateWizard () {
        this (new TemplateWizardIteratorWrapper.InstantiatingIterator (new TemplateWizardIterImpl ()));
    }
     
    /** Constructor to be called from public default one.
    */
    private TemplateWizard (TemplateWizardIteratorWrapper it) {
        super (it);
        
        this.iterator = it;
        
        // pass this to iterator
        iterator.initialize(this);

        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        setTitle(NbBundle.getMessage(TemplateWizard.class,"CTL_TemplateTitle")); //NOI18N
        setTitleFormat(new MessageFormat("{0}")); // NOI18N
    }

    /** Constructor
     *  for wizards that require the target chooser or template
     *  chooser panel.
     *  @param it panel iterator instance
     */
    protected TemplateWizard (TemplateWizard.Iterator it) {
        this();
        iterator.setIterator(it, false);
    }
    
    /** Initializes important settings.
     */
    @Override
    protected void initialize () {
        if (iterator != null) {
            iterator.initialize(this);
            newObjects = null;
        }
        super.initialize ();
    }

    /** This is method used by TemplateWizardPanel1 to change the template
    */
    final void setTemplateImpl (DataObject obj, boolean notify) {
        DataObject old = template;
        if (template != obj) {
            template = obj;
        }
        setTitle (getTitleFormat().format(new Object[] { obj.getNodeDelegate().getDisplayName() }));
        if (old != template) {
            Iterator it;
            if (
                obj == null ||
                (it = getIterator (obj)) == null
            ) {
                it = defaultIterator ();
            }
            
            // change type of TemplateWizard's iterator to follow type of iterator corresponding to active template
            if (it instanceof InstantiatingIteratorBridge) {
                WizardDescriptor.InstantiatingIterator newIt = ((InstantiatingIteratorBridge) it).getOriginalIterator ();
                if (newIt instanceof WizardDescriptor.ProgressInstantiatingIterator) {
                    TemplateWizardIteratorWrapper newIterImplWrapper = new TemplateWizardIteratorWrapper.ProgressInstantiatingIterator (this.iterator.getOriginalIterImpl ());
                    this.iterator = newIterImplWrapper;
                    this.setPanelsAndSettings(newIterImplWrapper, this);
                } else if (newIt instanceof WizardDescriptor.AsynchronousInstantiatingIterator) {
                    TemplateWizardIteratorWrapper newIterImplWrapper = new TemplateWizardIteratorWrapper.AsynchronousInstantiatingIterator (this.iterator.getOriginalIterImpl ());
                    this.iterator = newIterImplWrapper;
                    this.setPanelsAndSettings(newIterImplWrapper, this);
                }
            }
            this.iterator.setIterator (it, notify);
        }
    }

    /** Getter for template to create object from.
     * @return template
     */
    public DataObject getTemplate () {
        return template;
    }

    /** Sets the template. If under the Templates/
    * directory it will be selected by the dialog.
    *
    * @param obj the template to start with
    */
    public void setTemplate (DataObject obj) {
        if (obj != null) {
            setTemplateImpl (obj, true);
        }
    }
    
    /** Setter for the folder with templates. If not specified the 
     * Templates/ folder is used.
     *
     * @param folder the root folder for all templates if null the
     *    default folder is used
     */
    public void setTemplatesFolder (DataFolder folder) {
        templatesFolder = folder;
    }
    
    /** Getter for the folder with templates.
     * @return the folder with templates that should be used as root
     */
    public DataFolder getTemplatesFolder () {
        DataFolder df = templatesFolder;
        if (df == null) {
            FileObject fo = FileUtil.getConfigFile("Templates"); // NOI18N
            if (fo != null && fo.isFolder ()) {
                return DataFolder.findFolder (fo);
            }
        }
        return df;
    }
        
        

    /**
     * Getter for target folder.
     * @return the target folder
     * @throws IOException if the target folder has not been set
     */
    public DataFolder getTargetFolder () throws IOException {
        if (targetDataFolder == null) {
            throw new IOException(NbBundle.getMessage(TemplateWizard.class, "ERR_NoFilesystem"));
        }
        return targetDataFolder;
    }
    
    // Note: called by reflection from projects/projectui/Hacks
    private void reload (DataObject obj) {
        Iterator it;
        if (
            obj == null ||
            (it = getIterator (obj)) == null
        ) {
            it = defaultIterator ();
        }
        this.iterator.setIterator (it, true);
    }
    
    /** Sets the target folder.
    *
    * @param f the folder
    */
    public void setTargetFolder (DataFolder f) {
        targetDataFolder = f;
    }

    /** Getter for the name of the target template.
    * @return the name or <code>null</code> if not yet set
    */
    public String getTargetName () {
        return targetName;
    }

    /** Setter for the name of the template.
    * @param name name for the new object, or <code>null</code>
    */
    public void setTargetName (String name) {
        targetName = name;
    }

    /** Returns wizard panel that is used to choose a template.
     * @return wizard panel
     */
    public Panel<WizardDescriptor> templateChooser () {
        synchronized (this) {
            if (templateChooser == null) {
                templateChooser = createTemplateChooser ();
            }
        }
        return templateChooser;
    }

    /** Returns wizard panel that that is used to choose target folder and
     * name of the template.
     * @return wizard panel
     */
    public Panel<WizardDescriptor> targetChooser () {
        synchronized (this) {
            if (targetChooser == null) {
                targetChooser = createTargetChooser ();
            }
        }
        return targetChooser;
    }
    
    /** Access method to the default iterator.
    */
    final synchronized Iterator defaultIterator () {
        if (targetIterator == null) {
            targetIterator = createDefaultIterator ();
        }
        return targetIterator;
    }
    
    /** Method that allows subclasses to provide their own panel
     * for choosing the template (the first panel).
     * 
     * @return the panel
     */
    protected Panel<WizardDescriptor> createTemplateChooser () {
        return new TemplateWizardPanel1 ();
    }

    /** Method that allows subclasses to second (default) panel.
     * 
     * @return the panel
     */
    protected Panel<WizardDescriptor> createTargetChooser () {
        if (showTargetChooser) {
            return new TemplateWizardPanel2 ();
        } else {
            return new NewObjectWizardPanel ();
        }
    }
    
    /** Allows subclasses to provide their own default iterator
     * the one that will be used if not special iterator is associated
     * with selected template.
     * <P>
     * This implementation creates iterator that just shows the targetChooser
     * panel.
     *
     * @return the iterator to use as default one
     */
    protected Iterator createDefaultIterator () {
        return new DefaultIterator ();
    }
    
    /** Chooses the template and instantiates it.
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public Set<DataObject> instantiate() throws IOException {
        showTargetChooser = true;
        return instantiateImpl (null, null);
    }

    /** Chooses the template and instantiates it.
    *
    * @param template predefined template that should be instantiated
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public Set<DataObject> instantiate(DataObject template) throws IOException {
        showTargetChooser = true;
        return instantiateImpl (template, null);
    }

    /** Chooses the template and instantiates it.
    *
    * @param template predefined template that should be instantiated
    * @param targetFolder the target folder
    *
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public Set<DataObject> instantiate(
        DataObject template, DataFolder targetFolder
    ) throws IOException {
        showTargetChooser = false;
        return instantiateImpl (template, targetFolder);
    }
    
    private ProgressHandle getProgressHandle () {
        return progressHandle;
    }
    
    Set<DataObject> instantiateNewObjects (ProgressHandle handle) throws IOException {
        progressHandle = handle;
        try {
            // #17341. The problem is handling ESC -> value is not
            // set to CANCEL_OPTION for such cases.
            Object option = getValue();
            if(option == FINISH_OPTION || option == YES_OPTION
                || option == OK_OPTION) {

                // show wait cursor when handling instantiate
                showWaitCursor (); 

                newObjects = handleInstantiate ();
                if (lastComp != null) {
                    lastComp.removePropertyChangeListener(propL());
                    lastComp = null;
                }
            } else {
                if (lastComp != null) {
                    lastComp.removePropertyChangeListener(propL());
                    lastComp = null;
                }
                newObjects = null;
            }

        } finally {
            
            // set normal cursor back
            showNormalCursor ();
        }
            
        return newObjects;
    }

    /** Chooses the template and instantiates it.
    * @param template predefined template or nothing
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    private Set<DataObject> instantiateImpl(
        DataObject template, DataFolder targetFolder
    ) throws IOException {

        showTargetChooser |= targetFolder == null;
        targetChooser = null;

        // Bugfix #16161
        // Message which cancelled the previous attempt to instantiate the 
        // template
        Throwable   thrownMessage = null;
        
        // Bugfix #15458: Target folder should be set before readSettings of 
        // template wizard first panel is called.
        if (targetFolder != null) {
            setTargetFolder (targetFolder);
        }

        if (template != null) {
            // force new template selection 
            this.template = null;
            setTemplate (template);

            // make sure that iterator is initialized
            if (iterator != null) {
                iterator.initialize(this);
            }
        } else if (iterator != null) {
            iterator.initialize(this);
            iterator.first();
        }

        try {
            updateState();
            // bugfix #40876, set null as initial value before show wizard
            setValue (null);

            final java.awt.Dialog d = DialogDisplayer.getDefault().createDialog(this);
            // Bugfix #16161: if there was a message to the user, notify it
            // after the dialog has been shown on screen:
            if (thrownMessage != null) {
                final Throwable t = thrownMessage;
                thrownMessage = null;
                d.addComponentListener(new java.awt.event.ComponentAdapter() {
                                       @Override
                                           public void componentShown(java.awt.event.ComponentEvent e) {
                                               if (t.getMessage() != null) {
                                                   // this is only for backward compatitility (plus bugfix #15618, using errMan to log stack trace)
                                                   DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(t));
                                               } else {
                                                   // this should be used (it checks for exception
                                                   // annotations and severity)
                                                   Exceptions.printStackTrace(t);
                                               }
                                               d.removeComponentListener(this);
                                           }
                                       });
            }
            d.setVisible(true);
        } catch (IllegalStateException ise) {
            thrownMessage = ise;
        }
        
        // here can return newObjects because instantiateNewObjects() was called
        // from WizardDescriptor before close dialog (on Finish)
        return newObjects;
    }
    
    private void showWaitCursor () {
        //
        // waiting times
        //
        org.openide.util.Mutex.EVENT.writeAccess(new java.lang.Runnable() {

                                                     public void run() {
                                                         try {
                                                             java.awt.Frame f = org.openide.windows.WindowManager.getDefault().getMainWindow();

                                                             if (f instanceof javax.swing.JFrame) {
                                                                 java.awt.Component c = ((javax.swing.JFrame) f).getGlassPane();

                                                                 c.setVisible(true);
                                                                 c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                                                             }
                                                         }
                                                         catch (java.lang.NullPointerException npe) {
                                                             Logger.getLogger(TemplateWizard.class.getName()).log(Level.WARNING,
                                                                               null,
                                                                               npe);
                                                         }
                                                     }
                                                 });
    }
    
    private void showNormalCursor () {
        //
        // normal times
        //
        org.openide.util.Mutex.EVENT.writeAccess(new java.lang.Runnable() {

                                                     public void run() {
                                                         try {
                                                             java.awt.Frame f = org.openide.windows.WindowManager.getDefault().getMainWindow();

                                                             if (f instanceof javax.swing.JFrame) {
                                                                 java.awt.Component c = ((javax.swing.JFrame) f).getGlassPane();

                                                                 c.setCursor(null);
                                                                 c.setVisible(false);
                                                             }
                                                         }
                                                         catch (java.lang.NullPointerException npe) {
                                                             Logger.getLogger(TemplateWizard.class.getName()).log(Level.WARNING,
                                                                               null,
                                                                               npe);
                                                         }
                                                     }
                                                 });
    }
    

    /** Overridden to be able to set own default value for the title format.
     * @param format message format
     */
    @Override
    public void setTitleFormat(MessageFormat format) {
        titleFormatSet = true; // someone have set the title format
        super.setTitleFormat(format);
    }

    /** Overridden to be able to set a default value for the title format.
     * @return message format in title
     */
    @Override
    public MessageFormat getTitleFormat () {
        if (!titleFormatSet) {
            // we want to call this just for the first time getTitleFormat was called
            // and not to call it when someone else called setTitleFormat
            setTitleFormat (new MessageFormat (NbBundle.getMessage(TemplateWizard.class, "CTL_TemplateTitle")));
        }
        return super.getTitleFormat();
    }

    
    /** Calls iterator's instantiate. It is called when user selects
     * a option which is not CANCEL_OPTION or CLOSED_OPTION.
     * @throws IOException if the instantiation fails
     * @return set of data objects that have been created (should contain
     * at least one)
     */
    protected Set<DataObject> handleInstantiate() throws IOException {
        return iterator.getIterator ().instantiate (this);
    }
    
    /** Method to attach a description to a data object.
    * It is suggested that the URL use the <code>nbresloc</code> protocol.
    * @param obj data object to attach description to
    * @param url the url with description or null if there should be
    *   no description
    * @exception IOException if I/O fails
    */
    public static void setDescription (DataObject obj, URL url) throws IOException {
        obj.getPrimaryFile().setAttribute(EA_DESCRIPTION, url);
        obj.getPrimaryFile().setAttribute(CUSTOM_DESCRIPTION, url);
    }

    /** Method to get a description for a data object.
    * @param obj data object to attach description to
    * @return the url with description or null
    */
    public static URL getDescription (DataObject obj) {
        URL desc = (URL)obj.getPrimaryFile().getAttribute(CUSTOM_DESCRIPTION);
        if (desc != null) return desc;
        desc = (URL)obj.getPrimaryFile().getAttribute(EA_DESCRIPTION);
        if (desc != null) return desc;
        // Backwards compatibility:
        String rsrc = (String) obj.getPrimaryFile ().getAttribute (EA_DESC_RESOURCE);
        if (rsrc != null) {
            try {
                URL better = new URL ("nbresloc:/" + rsrc); // NOI18N
                try {
                    setDescription (obj, better);
                } catch (IOException ioe) {
                    // Oh well, just ignore.
                }
                return better;
            } catch (MalformedURLException mfue) {
                Exceptions.printStackTrace(mfue);
            }
        }
        return null;
    }

    /** Set a description for a data object by resource path rather than raw URL.
     * @deprecated Use {@link #setDescription} instead.
     * @param obj data object to set description for
     * @param rsrc a resource string, e.g. "com/foo/MyPage.html", or <code>null</code> to clear
     * @throws IOException if the attribute cannot be set
     */
    @Deprecated
    public static void setDescriptionAsResource (DataObject obj, String rsrc) throws IOException {
        if (rsrc != null && rsrc.startsWith ("/")) { // NOI18N
            Logger.getLogger(TemplateWizard.class.getName()).warning("auto-stripping leading slash from resource path in TemplateWizard.setDescriptionAsResource: " + rsrc);
            rsrc = rsrc.substring (1);
        }
        obj.getPrimaryFile ().setAttribute (EA_DESC_RESOURCE, rsrc);
    }

    /** Get a description as a resource.
    * @param obj the data object
    * @return the resource path, or <code>null</code> if unset (incl. if only set as a raw URL)
    * @deprecated Use {@link #getDescription} instead.
    */
    @Deprecated
    public static String getDescriptionAsResource (DataObject obj) {
        return (String) obj.getPrimaryFile ().getAttribute (EA_DESC_RESOURCE);
    }

    /** Allows to attach a special Iterator to a template. This allows
    * templates to completelly control the way they are instantiated.
    * <P>
    * Better way for providing an Iterator is to return it from the
    * <code>dataobject.getCookie (TemplateWizard.Iterator.class)</code>
    * call.
    *
    * @param obj data object
    * @param iter TemplateWizard.Iterator to use for instantiation of this
    *    data object, or <code>null</code> to clear
    * @exception IOException if I/O fails
     *
    * @deprecated since 2.13 you should provide the iterator from <code>getCookie</code> method
    */
    @Deprecated
    public static void setIterator (DataObject obj, Iterator iter)
    throws IOException {
        obj.getPrimaryFile().setAttribute(CUSTOM_ITERATOR, iter);
        obj.getPrimaryFile().setAttribute(EA_ITERATOR, iter);
    }

    /** Finds a custom iterator attached to a template that should
    * be used to instantiate the object. First of all it checks
    * whether there is an iterator attached by <code>setIterator</code> method, if not it asks the
    * data object for the Iterator as cookie.
    * 
    * @param obj the data object
    * @return custom iterator or null
    */
    @SuppressWarnings("unchecked")
    public static Iterator getIterator (DataObject obj) {
        Object unknownIterator = obj.getPrimaryFile ().getAttribute(CUSTOM_ITERATOR);
        if (unknownIterator == null) {
            unknownIterator = obj.getPrimaryFile ().getAttribute(EA_ITERATOR);
        }
        Iterator it = null;
        if (unknownIterator instanceof Iterator) {
            // old style iterator
            it = (Iterator)unknownIterator;
        // own brigde for each one iterator type
        } if (unknownIterator instanceof WizardDescriptor.InstantiatingIterator) {
            it = new InstantiatingIteratorBridge((WizardDescriptor.InstantiatingIterator<WizardDescriptor>) unknownIterator);
        }
        if (it != null) {
            return it;
        }
        
        return obj.getCookie (Iterator.class);
    }
    
    // helper check for windows, its filesystem is case insensitive (workaround the bug #33612)
    /** Check existence of file on case insensitive filesystem.
     * Returns true if folder contains file with given name and extension.
     * @param folder folder for search
     * @param name name of file
     * @param extension extension of file
     * @return true if file with name and extension exists, false otherwise.
     */    
    static boolean checkCaseInsensitiveName (FileObject folder, String name, String extension) {
        // bugfix #41277, check only direct children
        Enumeration children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = (FileObject) children.nextElement ();
            if (extension.equalsIgnoreCase (fo.getExt ()) && name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }
    
    /** Overriden to add/remove listener to/from displayed component. Also make recreation
     * of steps and content.
     */
    @Override
    protected void updateState() {
        super.updateState();
        
        if (lastComp != null) {
            lastComp.removePropertyChangeListener(propL());
        }

        // listener
        lastComp = iterator.current().getComponent();
        lastComp.addPropertyChangeListener(propL());
        
        // compoun steps pane info
        putProperty(PROP_CONTENT_SELECTED_INDEX,
            new Integer(getContentSelectedIndex()));
        if (getContentData() != null) {
            putProperty(PROP_CONTENT_DATA, getContentData());
        }
    }

    /**
     * @return String[] content taken from first panel and delegated iterator or null if
     * delegated iterator doesn't supplied content steps name
     */
    private String[] getContentData() {
        Component first = templateChooser().getComponent();
        if (iterator.current() == templateChooser()) {
            // return first panel steps
            return (String[])((JComponent)first).getClientProperty(PROP_CONTENT_DATA);
        }
        String[] cd = null;
        Component c = iterator.current().getComponent();
        if (c instanceof JComponent) {
            // merge first panel name with delegated iterator steps
            Object property = ((JComponent)c).getClientProperty(PROP_CONTENT_DATA);
            if (property instanceof String[]) {
                String[] cont = (String[])property;
                Object value = ((JComponent)first).getClientProperty(PROP_CONTENT_DATA);
                if (value instanceof String[]) {
                    cd = new String[cont.length + 1];
                    cd[0] = ((String[])value)[0];
                    System.arraycopy(cont, 0, cd, 1, cont.length);
                } else {
                    cd = new String[cont.length];
                    System.arraycopy(cont, 0, cd, 0, cont.length);
                }
            }
        }
        return cd;
    }
    
    /** Returns selected item in content
     * @return int selected index of content
     */
    private int getContentSelectedIndex() {
        if (iterator.current() == templateChooser()) {
            return 0;
        }
        Component c = iterator.current().getComponent();
        if (c instanceof JComponent) {
            // increase supplied selected index by one (template chooser)
            Object property = ((JComponent)c).getClientProperty(PROP_CONTENT_SELECTED_INDEX);
            if ((property instanceof Integer)) {
                return ((Integer)property).intValue() + 1;
            }
        }
        return 1;
    }
    
    /** Listens on content property changes in delegated iterator. Updates Wizard
     * descriptor properties.
     */
    private PropertyChangeListener propL() {
        if (pcl == null) {
            pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (PROP_CONTENT_SELECTED_INDEX.equals(ev.getPropertyName())) {
                        putProperty(PROP_CONTENT_SELECTED_INDEX,
                            new Integer(getContentSelectedIndex()));
                    } else { 
                        if ((PROP_CONTENT_DATA.equals(ev.getPropertyName())) && (getContentData() != null)) {
                            putProperty(PROP_CONTENT_DATA, getContentData());
                        }
                    }
                }
            };
        }
        return pcl;
    }

    // needs for unit test only
    final TemplateWizardIterImpl getIterImpl () {
        return iterator.getOriginalIterImpl ();
    }

    /** The interface for custom iterator. Enhances to WizardDescriptor.Iterator
    * by serialization and ability to instantiate the object.
    * <P>
    * All Panels provided by this iterator will receive a TemplateWizard
    * as the settings object and they are encourage to store its data by the 
    * use of <CODE>putProperty</CODE> method and read it using <code>getProperty</code>.
    * <P>
    * Implements <code>Node.Cookie</code> since version 2.13
    */
    public interface Iterator extends WizardDescriptor.Iterator<WizardDescriptor>,
    java.io.Serializable, org.openide.nodes.Node.Cookie {
        /** Instantiates the template using information provided by
         * the wizard. If instantiation fails then wizard remains open to enable correct values.
         *
         * @return set of data objects that have been created (should contain
         *   at least one)
         * @param wiz the wizard
         * @exception IOException if the instantiation fails
         */
        public Set<DataObject> instantiate(TemplateWizard wiz)
            throws IOException;
        
        /** Initializes the iterator after it is constructed.
         * The iterator can for example obtain the {@link #targetChooser target chooser}
         * from the wizard if it does not wish to provide its own.
         * @param wiz template wizard that wishes to use the iterator
         */
        public void initialize(TemplateWizard wiz);
        
        /** Informs the Iterator that the TemplateWizard finished using the Iterator.
         * The main purpose of this method is to perform cleanup tasks that should
         * not be left on the garbage collector / default cleanup mechanisms.
         * @param wiz wizard which is no longer being used
         */
        public void uninitialize(TemplateWizard wiz);
    } // end of Iterator

    /** Implementation of default iterator.
    */
    private final class DefaultIterator implements Iterator {
        DefaultIterator() {}
        
        /** Name */
        public String name () {
            return ""; // NOI18N
        }

        /** Instantiates the template using informations provided by
        * the wizard.
        *
        * @param wiz the wizard
        * @return set of data objects that has been created (should contain
        *   at least one) 
        * @exception IOException if the instantiation fails
        */
        public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
            String n = wiz.getTargetName ();
            DataFolder folder = wiz.getTargetFolder ();
            DataObject template = wiz.getTemplate ();
            Map<String,Object> wizardProps = new HashMap<String, Object>();
            for (Map.Entry<String, ? extends Object> entry : wiz.getProperties().entrySet()) {
                wizardProps.put("wizard." + entry.getKey(), entry.getValue()); // NOI18N
            }
            
            DataObject obj = template.createFromTemplate (folder, n, wizardProps);

            // run default action (hopefully should be here)
            final Node node = obj.getNodeDelegate ();
            final Action a = node.getPreferredAction();
            if (a != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    }
                });
            }

            return Collections.singleton(obj);
        }
        
        /** No-op implementation.
         */
        public void initialize(TemplateWizard wiz) {
        }
        
        /** No-op implementation.
         */
        public void uninitialize(TemplateWizard wiz) {
        }
        
        /** Get the current panel.
        * @return the panel
        */
        public Panel<WizardDescriptor> current() {
            return targetChooser ();
        }
        
        /** Test whether there is a next panel.
        * @return <code>true</code> if so
        */
        public boolean hasNext() {
            return false;
        }
        
        /** Test whether there is a previous panel.
        * @return <code>true</code> if so
        */
        public boolean hasPrevious() {
            return false;
        }
        
        /** Move to the next panel.
        * I.e. increment its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void nextPanel() {
            throw new java.util.NoSuchElementException ();
        }
        
        /** Move to the previous panel.
        * I.e. decrement its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void previousPanel() {
            throw new java.util.NoSuchElementException ();
        }
        
        /** Add a listener to changes of the current panel.
        * The listener is notified when the possibility to move forward/backward changes.
        * @param l the listener to add
        */
        public void addChangeListener(javax.swing.event.ChangeListener l) {
        }
        
        /** Remove a listener to changes of the current panel.
        * @param l the listener to remove
        */
        public void removeChangeListener(javax.swing.event.ChangeListener l) {
        }
    }
    
    private static class InstantiatingIteratorBridge implements TemplateWizard.Iterator {
        private WizardDescriptor.InstantiatingIterator<WizardDescriptor> instantiatingIterator;
        public InstantiatingIteratorBridge (WizardDescriptor.InstantiatingIterator<WizardDescriptor> it) {
            instantiatingIterator = it;
        }
        
        private WizardDescriptor.InstantiatingIterator getOriginalIterator () {
            return instantiatingIterator;
        }
        
        public void addChangeListener (javax.swing.event.ChangeListener l) {
            instantiatingIterator.addChangeListener (l);
        }
        
        public org.openide.WizardDescriptor.Panel<WizardDescriptor> current () {
            return instantiatingIterator.current ();
        }
        
        public boolean hasNext () {
            return instantiatingIterator.hasNext ();
        }
        
        public boolean hasPrevious () {
            return instantiatingIterator.hasPrevious ();
        }
        
        public String name () {
            return instantiatingIterator.name ();
        }
        
        public void nextPanel () {
            instantiatingIterator.nextPanel ();
        }
        
        public void previousPanel () {
            instantiatingIterator.previousPanel ();
        }
        
        public void removeChangeListener (javax.swing.event.ChangeListener l) {
            instantiatingIterator.removeChangeListener (l);
        }
        
        public void initialize (TemplateWizard wiz) {
            instantiatingIterator.initialize (wiz);
        }
        
        public Set<DataObject> instantiate (TemplateWizard wiz) throws IOException {
            // iterate Set and replace unexpected object with dataobjects
            Set workSet;
            if (instantiatingIterator instanceof WizardDescriptor.ProgressInstantiatingIterator) {
                assert wiz.getProgressHandle () != null : "ProgressHandle cannot be null.";
                workSet = ((ProgressInstantiatingIterator)instantiatingIterator).instantiate (wiz.getProgressHandle ());
            } else {
                workSet = instantiatingIterator.instantiate ();
            }
            if (workSet == null) {
                Logger.getLogger(TemplateWizard.class.getName()).warning(
                        "Wizard iterator of type " + instantiatingIterator.getClass().getName() +
                        " illegally returned null from the instantiate method");
                return Collections.emptySet ();
            }
            java.util.Iterator it = workSet.iterator ();
            Object obj;
            DataObject dobj;
            Set<DataObject> resultSet = new LinkedHashSet<DataObject>(workSet.size());
            while (it.hasNext ()) {
                obj = it.next ();
                assert obj != null : "Null DataObject provided by " + instantiatingIterator;
                if (obj instanceof DataObject) {
                    resultSet.add ((DataObject) obj);
                } else if (obj instanceof FileObject) {
                    try {
                        dobj = DataObject.find ((FileObject)obj);
                        resultSet.add (dobj);
                    } catch (DataObjectNotFoundException ex) {
                        assert false : obj;
                    }
                } else if (obj instanceof Node) {
                    dobj = ((Node) obj).getCookie(DataObject.class);
                    if (dobj != null) {
                        resultSet.add (dobj);
                    }
                }
            }
            return resultSet;
        }
        
        public void uninitialize (TemplateWizard wiz) {
            instantiatingIterator.uninitialize (wiz);
        }
        
    }

}
