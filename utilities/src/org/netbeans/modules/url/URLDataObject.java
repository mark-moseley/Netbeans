/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.url;


import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.DialogDisplayer;

import org.openide.awt.Actions;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListener;
import org.openide.util.actions.Presenter;


/** Data object that represents one bookmark, one .url file containing url.
 *
 * @author Ian Formanek
 * @see org.openide.Places.Folders#bookmarks
 */
public class URLDataObject extends MultiDataObject implements EditCookie, OpenCookie, InstanceCookie {

    /** Name for url property. */
    private static final String PROP_URL = "url"; // NOI18N
    
    /** Generated serial version UID. */
    static final long serialVersionUID =6829522922370124627L;

    
    /** Constructor.
     * @param pf primary file object for this data object */
    public URLDataObject(final FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);

        getCookieSet().add(this);
    }
    

    // PENDING: it would be neat to have get/setURL methods 
    // but, there is a problem(at least at jdk1.3 for linux) with URL.equals (too much time consuming
    // in underlying native method).
    /** Gets <code>URL</code> string from uderlying .url file. Notifies user
     * if error occures.
     * @return <code>URL</code> string stored in the file or empty string if file is empty
     * or <code>null</code> if error occured. Even there are multiple lines of text in the
     *  file, only the first one is returned */
    String getURLString() {
        FileObject urlFile = getPrimaryFile();
        if(!urlFile.isValid())
            return null;
        
        String urlString = ""; // NOI18N
        InputStream is = null;
        
        try {
            urlString = new BufferedReader (new InputStreamReader(is = urlFile.getInputStream ())).readLine ();
        } catch (FileNotFoundException fne) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, fne);
            return null;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            return null;
        } finally {
            if(is != null)
                try {
                    is.close ();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
        }
        
        if (urlString == null)
            // if the file is empty, return empty string, as null is reserved for notifying failure 
            urlString = ""; // NOI18N

        return urlString;
    }

    /** Stores specified String into the URL file.
     * @param newUrlString the URL String to be stored in the file. */
    void setURLString(String newUrlString) {
        FileObject urlFile = getPrimaryFile();
        if(!urlFile.isValid())
            return;
        
        FileLock lock = null;
        try {
            lock = urlFile.lock ();
            OutputStream os = urlFile.getOutputStream (lock);
            os.write (newUrlString.getBytes ());
            os.close ();
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        } finally {
            if (lock != null)
                lock.releaseLock ();
        }
    }

    /** Help context for this object. Overrides superclass method.
     * @return help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (URLDataObject.class);
    }

    /** Creates node delagte for this data object.
     * @return <code>URLNode</code> representating this data object instance
     */
    protected Node createNodeDelegate () {
        return new URLNode(this);
    }


    /** Invokes the open action. Implements <code>OpenCookie</code> interface. */
    public void open() {
        String urlString = getURLString ();
        if(urlString == null) 
            return;

        URL url = getURLFromString(urlString);
        
        if(url == null)
            return;

        org.openide.awt.HtmlBrowser.URLDisplayer.getDefault ().showURL (url);
    }

    /** Gets URL from string. Notifies user about error if it's not possible. Utility method.
     * @param urlString string from to construct <code>URL</code>
     * @return <code>URL</code> or null if it's not possible to construct from <code>urlString</code> */
    private static URL getURLFromString(String urlString) {
        URL url = null;
        
        try {
            url = new URL(urlString);
        } catch (MalformedURLException mue1) {
            try {
                // Try to prepend http protocol.
                url = new URL ("http://" + urlString); // NOI18N
            } catch (MalformedURLException mue2) {
                if (urlString.length () > 50) { // too long URL
                    DialogDisplayer.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            NbBundle.getBundle (URLDataObject.class).getString("MSG_MalformedURLError"),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                } else {
                    DialogDisplayer.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            MessageFormat.format (
                                NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_MalformedURLError"),
                                new Object[] { urlString }
                            ),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                }
            }
        }
        
        return url;
    }

    /** Implements <code>EditCookie</code> interface.
     * Instructs an editor to be opened. The operation can
     * return immediately and the editor be opened later.
     * There can be more than one editor open, so one of them is
     * arbitrarily chosen and opened. */
    public void edit() {
        String urlString = getURLString ();
        if (urlString == null) return;
        NotifyDescriptor.InputLine urlLine = new NotifyDescriptor.InputLine (
            NbBundle.getBundle (URLDataObject.class).getString("CTL_URL") ,
            NbBundle.getBundle (URLDataObject.class).getString("CTL_EditURL"));
        
        urlLine.setInputText(urlString);
        
        DialogDisplayer.getDefault ().notify (urlLine);
        if(urlLine.getValue () == NotifyDescriptor.OK_OPTION)
            setURLString (urlLine.getInputText ());
    }

    /** Gets name of instance. Implements <code>InstanceCookie</code> interface method. */
    public String instanceName () {
        return getName();
    }

    /** Gets class of instance. Implements <code>InstanceCookie</code> interface method. 
     * @return <code>URLPresenter</code> class
     * @see URLPresenter */
    public Class instanceClass () throws IOException, ClassNotFoundException {
        return URLPresenter.class;
    }

    /** Creates new instance. Implements <code>InstanceCookie</code> interface method. 
     * @return <code>URLPresenter</code> instance 
     * @see URLPresenter */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return createURLPresenter();
    }
    
    /** Creates <code>URLPresenter</code> for this object. */
    URLPresenter createURLPresenter() {
        return new URLPresenter(this);
    }
    

    /** Presenter which creates actual components on demand. */
    private static class URLPresenter extends Object implements Presenter.Menu, Presenter.Toolbar, Presenter.Popup {
        
        /** Data object to present. */
        private DataObject dataObject;
        
        /** Constructor. */        
        public URLPresenter(DataObject dataObject) {
            this.dataObject = dataObject;
        }

        /** Implements <code>Presenter.Menu</code> interface. */
        public JMenuItem getMenuPresenter() {
            return new URLMenuItem(dataObject);
        }
        
        /** Implements <code>Presenter.Popup</code> interface. */
        public JMenuItem getPopupPresenter() {
            return new URLMenuItem(dataObject);
        }
        
        /** Implements <code>Presenter.Toolbar</code> interface. */
        public Component getToolbarPresenter() {
            return new URLToolbarButton(dataObject);
        }
    } // End of URLPresenter nested class.

    
    /** Menu item representing the bookmark.
     * Takes display name and icon from associated data object;
     * when invoked, opens the URL in the browser. */
    private static class URLMenuItem extends JMenuItem {
        public URLMenuItem(DataObject dataObject) {
            new SimpleNodeButtonBridge(dataObject, this);
        }
    } // End of URLMenuItem nested class.

    
    /** Toolbar button representing the bookmark. */
    private static class URLToolbarButton extends JButton {
        public URLToolbarButton(DataObject dataObject) {
            new SimpleNodeButtonBridge(dataObject, this);
        }
    } // End of URLToolbarButton nested class.

    
    /** Bridge which binds a URLNode to a menu item or toolbar button. */
    private static class SimpleNodeButtonBridge extends Object implements ActionListener, PropertyChangeListener {

        /** Node to bind with. */
        private final DataObject dataObject;
        
        /** Abstract button to bind with. */
        private final AbstractButton button;

        
        /** Constructor. */
        public SimpleNodeButtonBridge(DataObject dataObject, AbstractButton button) {
            this.dataObject = dataObject;
            this.button = button;
            
            updateText();

            Image defaultImage = Utilities.loadImage("org/netbeans/modules/url/urlObject.gif"); // NOI18N

            try { 
                this.button.setIcon(new ImageIcon(this.dataObject.getPrimaryFile().getFileSystem().getStatus().annotateIcon(
                    defaultImage,
                    BeanInfo.ICON_COLOR_16x16, 
                    dataObject.files()))
                );
            } catch(FileStateInvalidException fsie) {
                // No filesystem, set defaultIcon.
                this.button.setIcon(new ImageIcon(defaultImage));
            }
            
            HelpCtx.setHelpIDString(button, dataObject.getHelpCtx().getHelpID());
            
            button.addActionListener(this);
            this.dataObject.addPropertyChangeListener(WeakListener.propertyChange(this, this.dataObject));
        }
        
        /** Implements <code>ActionListener</code> interface. Gets node's <code>OpenCookie</code>
         * and perfoms it if data object have it. */
        public void actionPerformed(ActionEvent evt) {
            OpenCookie open = (OpenCookie)dataObject.getCookie (OpenCookie.class);
            if(open != null)
                open.open();
        }
        
        /** Implements <code>PropertyChangeListener</code> interface method.
         * Listens on node's name, display name and icon changes and updates
         * associated button accordingly. */
        public void propertyChange(PropertyChangeEvent evt) {
            if(DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                updateText();
            }
        }
        
        /** Updates display text and tooltip of associated button. Node's
         * name or display name has changed. Helper method. */
        private void updateText() {
            String text = dataObject.getName();
            
            try {
                text = dataObject.getPrimaryFile().getFileSystem().getStatus().annotateName(text, dataObject.files());
            } catch(FileStateInvalidException fsie) {
                // No filesystem, do nothing.
            }
            
            Actions.setMenuText(button, text, true);
        }
        
    } // End of SimpleNodeButtonBridge nested class.


    /** <code>URL</code> node representing <code>URLDataObject</code>.
     * Leaf node, default action opens editor or instantiates template.
     * Icons redefined.
     */
    public static final class URLNode extends DataNode {

        /** Default constructor, constructs node */
        public URLNode (final DataObject dataObject) {
            super(dataObject, Children.LEAF);
            setIconBase("org/netbeans/modules/url/urlObject"); // NOI18N

            // Trick: To have a node name localized but remain the object name 
            // the same until renamed explictly. -> due to be able show
            // node names without ampersands for shortcuts and be able to show
            // shortcuts in bookmarks menu.
            // Note: see getDisplayName method bellow.
            setName(super.getDisplayName(), false);
        }


        /** Gets display name. Overrides superclass method. 
         * Cuts ampersand from the original display name. */
        public String getDisplayName() {
            return Actions.cutAmpersand(super.getDisplayName());
        }
        
        /** Gets sheet of properties. Overrides superclass method. */
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set sheetSet = sheet.get(Sheet.PROPERTIES);
            
            // Name property is replaced by so we could show the localized name
            // of node instead of the non-localized name of file on the disk 
            // which could differ at the start.
            sheetSet.remove(DataObject.PROP_NAME);
            sheetSet.put(createNameProperty());
                        
            sheetSet.put(createURLStringProperty());
            
            return sheet;
        }

        /** Creates a name property. */
        private Node.Property createNameProperty() {
            return new PropertySupport.ReadWrite (
                DataObject.PROP_NAME,
                String.class,
                NbBundle.getBundle(URLDataObject.class).getString("PROP_Name"),
                NbBundle.getBundle(URLDataObject.class).getString("PROP_NameShortDescription")) {

                public Object getValue () {
                  return URLNode.this.getName();
                }

                public void setValue (Object val) throws IllegalAccessException,
                  IllegalArgumentException, InvocationTargetException {
                  if (!canWrite())
                      throw new IllegalAccessException();
                  if (!(val instanceof String))
                      throw new IllegalArgumentException();

                  try {
                      getDataObject().rename ((String)val);
                  } catch (IOException ex) {
                      throw new InvocationTargetException (ex);
                  }
                }

                public boolean canWrite () {
                  return getDataObject().isRenameAllowed();
                }
            };
        }
        
        /** Creates property for editing.
         * @return property for URL String or null */
        private Node.Property createURLStringProperty() {
            Node.Property urlStringProperty = new PropertySupport.ReadWrite (
                URLDataObject.PROP_URL,
                String.class,
                NbBundle.getBundle(URLDataObject.class).getString("PROP_URLDisplayName"),
                NbBundle.getBundle(URLDataObject.class).getString("PROP_URLShortDescription")) {

                public Object getValue() {
                    return ((URLDataObject)getDataObject()).getURLString();
                }

                public void setValue(Object val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
                    if(!canWrite())
                        throw new IllegalAccessException();
                    if(!(val instanceof String))
                        throw new IllegalArgumentException();

                    ((URLDataObject)getDataObject()).setURLString((String)val);
                }
            };
            
            urlStringProperty.setPreferred(true);

            return urlStringProperty;
        }
        
    } // End of URLNode nested class.
    
}
