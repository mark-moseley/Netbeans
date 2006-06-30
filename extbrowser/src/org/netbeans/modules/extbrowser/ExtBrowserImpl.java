/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.openide.awt.HtmlBrowser;

/**
 * The ExtBrowserImpl is generalized external browser.
 *
 * @author Radim Kubacki
 */
public abstract class ExtBrowserImpl extends HtmlBrowser.Impl {

    /** standart helper variable */
    protected PropertyChangeSupport pcs;

    /** requested URL */
    protected URL url;
    protected String title = "";      // NOI18N

    /** reference to a factory to get settings */
    protected ExtWebBrowser extBrowserFactory;

    /** Default constructor. 
      * <p>Builds PropertyChangeSupport. 
      */
    public ExtBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
    }
    
    /** Dummy implementations */
    public boolean isBackward() { return false; }
    public boolean isForward() { return false; }
    public void backward() { }
    public void forward() { }
    public boolean isHistory() { return false; }
    public void showHistory() {}
    public void stopLoading() { }
    
    protected void setTitle (String title) {
        return;
    }
    
    public String getTitle() {
        return "";
    }

    
    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    public String getStatusMessage() {
        return "";
    }
        
    /** Call setURL again to force reloading.
     * Browser must be set to reload document and do not cache them.
     */
    public void reloadDocument() {
        if (url != null) {
            setURL(url);
        }
    }
        
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    public URL getURL() {
        return url;
    }
    
    /** 
     *  Sets current URL. Descendants of this class will implement it and they can call this
     *  to display internal resources.
     *
     * @param url URL to show in the browser.
     */
    public abstract void setURL(URL url);
    
    /** Returns visual component of html browser.
     *
     * @return visual component of html browser.
     */
    public final java.awt.Component getComponent() {
        return null;
    }

    /** Adds PropertyChangeListener to this browser.
     *
     * @param l Listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /** Removes PropertyChangeListener from this browser.
     *
     * @param l Listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

}
