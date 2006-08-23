/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview;
import org.netbeans.modules.cnd.classview.resources.I18n;
import java.awt.*;

import org.openide.ErrorManager;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Vladimir Kvashin
 */
public class ClassViewTopComponent extends TopComponent  {

    static final long serialVersionUID = 420172427347975689L;

    private static final String PREFERRED_ID = "classview"; //NOI18N

    public static transient ClassViewTopComponent DEFAULT;

    private transient ClassView view;
    
    public ClassViewTopComponent() {
        //if( Diagnostic.DEBUG ) Diagnostic.traceStack("ClassViewTopComponent .ctor #" + (++cnt));
    }
    
    /** Return preferred ID */
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized ClassViewTopComponent findDefault() {
        if(DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID); // NOI18N
            //if( ! (tc instanceof ClassViewTopComponent) ) {
            if( DEFAULT == null ) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find project component for its ID. Returned " + tc)); // NOI18N
//                DEFAULT = new ClassViewTopComponent();
//                // XXX Look into getDefault method.
//                DEFAULT.scheduleValidation();
                getDefault();
            }
        }
        
        return DEFAULT;
    }
    
    /** Gets default instance. Don't use directly, it reserved for deserialization routines only,
     * e.g. '.settings' file in xml layer, otherwise you can get non-deserialized instance. */
    public static synchronized ClassViewTopComponent getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ClassViewTopComponent();
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            //$ DEFAULT.scheduleValidation();
        }
        
        return DEFAULT;
    }
    
    public Object readResolve() throws java.io.ObjectStreamException {
        //return getDefault();
        if( DEFAULT == null ) {
            DEFAULT = this;
            //$ DEFAULT.scheduleValidation();
        }
        return this;
    }
    
    /** Overriden to explicitely set persistence type of ProjectsTab
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /** Initialize visual content of component */
    protected void componentShowing() {
        super.componentShowing();
//            view.getAccessibleContext().setAccessibleName(
//                I18n.getMessage(
//                    "ACSN_ClassViewName")); // NOI18N
//            view.getAccessibleContext().setAccessibleDescription(
//                I18n.getMessage(
//                    "ACSD_ClassViewDescription`")); // NOI18N
//        addListenets();

//        if( Diagnostic.DEBUG ) {
//            new Thread(new Runnable() {
//                public void run() {
//                    new TestVk().test();
//                }
//            }).run();
//        }
    }
    
    protected void componentHidden() {
        super.componentHidden();
    }
    
    protected void componentOpened() {
        if( view == null ) {
            view = new ClassView();
            setLayout(new BorderLayout());
            add(view);
            setToolTipText(I18n.getMessage("ClassViewTitle"));	// NOI18N
            setName(I18n.getMessage("ClassViewTooltip"));	// NOI18N
        }
        view.startup();
    }
    
    protected void componentClosed() {
        if( view != null ) { // paranoia
            view.shutdown();
        }
    }
    
}
