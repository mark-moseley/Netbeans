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
import org.openide.awt.Mnemonics;
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
public class URLDataObject extends MultiDataObject
                           implements EditCookie, OpenCookie, InstanceCookie {

    /** Name for url property. */
    static final String PROP_URL = "url";                       //NOI18N
    
    /** Generated serial version UID. */
    static final long serialVersionUID = 6829522922370124627L;

    
    /**
     * Constructs a new URL data object.
     *
     * @param  file  file to create an object from
     * @param  loader  <code>DataLoader</code> which recognized the file
     *                 and initiated calling this constructor
     */
    public URLDataObject(final FileObject file, MultiFileLoader loader)
            throws DataObjectExistsException {
        super(file, loader);
        getCookieSet().add(this);
    }
    
    /*
     * PENDING: it would be neat to have get/setURL methods 
     * but, there is a problem(at least at jdk1.3 for linux) with URL.equals
     * (too much time consuming in underlying native method).
     */
    
    /**
     * Gets a <code>URL</code> string from the underlying .url file.
     * The user is notified if an error occures during reading the file.
     * If there are multiple lines of text in the file, only the first one is
     * returned and no error is reported.
     *
     * @return  <code>URL</code> string stored in the file,
     *          an empty string if the file is empty,
     *          or <code>null</code> if an error occured while reading the file
     */
    String getURLString() {
        FileObject urlFile = getPrimaryFile();
        if (!urlFile.isValid()) {
            return null;
        }
        String urlString = null;
        
        InputStream is = null;
        try {
            is = urlFile.getInputStream();
            urlString = new BufferedReader(new InputStreamReader(is))
                        .readLine();
        } catch (FileNotFoundException fne) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, fne);
            return null;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close ();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        if (urlString == null) {
            /*
             * If the file is empty, return an empty string.
             * <null> is reserved for notifications of failures.
             */
            urlString = "";                                             //NOI18N
        }
        return urlString;
    }

    /**
     * Stores a specified URL into the file backing up this URL object.
     *
     * @param  newUrlString  URL to be stored in the file
     */
    void setURLString(String newUrlString) {
        FileObject urlFile = getPrimaryFile();
        if (!urlFile.isValid()) {
            return;
        }
        FileLock lock = null;
        try {
            lock = urlFile.lock();
            OutputStream os = urlFile.getOutputStream(lock);
            os.write(newUrlString.getBytes());
            os.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    /** */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(URLDataObject.class);
    }

    /**
     * Creates a node delagate for this <code>URLDataObject</code>.
     *
     * @return <code>URLNode</code> representing this <code>URLDataObject</code>
     */
    protected Node createNodeDelegate () {
        return new URLNode(this);
    }


    /* implements interface OpenCookie */
    public void open() {
        String urlString = getURLString();
        if (urlString == null) {
            return;
        }
        URL url = getURLFromString(urlString);
        if (url == null) {
            return;
        }
        org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }

    /**
     * Converts an URL string to an <code>URL</code> object.
     * Notifies the user in case of failure.
     *
     * @param  urlString  string to convert to <code>URL</code>
     * @return  <code>URL</code> object representing the specified URL;
     *          or <code>null</code> in case of failure
     */
    private static URL getURLFromString(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException mue1) {
        }
        

        /* failed - try to prepend 'http://' */
        try {
            return new URL("http://" + urlString);                      //NOI18N
        } catch (MalformedURLException mue1) {
        }
        
        /* failed again - notify about the failure and return null: */
        String msg;
        if (urlString.length() > 50) {          //too long URL
            msg = NbBundle.getMessage(URLDataObject.class,
                                      "MSG_MalformedURLError");         //NOI18N
        } else {
            msg = NbBundle.getMessage(URLDataObject.class,
                                      "MSG_FMT_MalformedURLError",      //NOI18N
                                      urlString);
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE));
        return null;
    }

    /* Implements interface EditCookie. */
    /**
     * Asks the user for a new URL and changes this object to the new URL.
     */
    public void edit() {
        String urlString = getURLString();
        if (urlString == null) {
            return;
        }

        String msgTitle = NbBundle.getMessage(URLDataObject.class,
                                              "CTL_EditURL");           //NOI18N
        String msgText  = NbBundle.getMessage(URLDataObject.class,
                                              "CTL_URL");               //NOI18N
        NotifyDescriptor.InputLine urlLine;
        urlLine = new NotifyDescriptor.InputLine(msgText, msgTitle);
        urlLine.setInputText(urlString);
        DialogDisplayer.getDefault().notify(urlLine);
        
        if (urlLine.getValue() == NotifyDescriptor.OK_OPTION) {
            setURLString(urlLine.getInputText());
        }
    }

    /* implements interface InstanceCookie */
    public String instanceName () {
        return getName();
    }

    /* implements interface InstanceCookie */
    /**
     * @return  class <code>URLPresenter</code>
     * @see  URLPresenter
     */
    public Class instanceClass () throws IOException, ClassNotFoundException {
        return URLPresenter.class;
    }

    /* implements interface InstanceCookie */
    /**
     * Creates an instance of <code>URLPresenter</code>.
     *
     * @return  instance of class <code>URLPresenter</code>
     * @see URLPresenter
     */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return new URLPresenter(this);
    }
    
}
