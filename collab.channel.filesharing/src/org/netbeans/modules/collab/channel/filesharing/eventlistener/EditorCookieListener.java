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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import com.sun.collablet.*;

import org.openide.cookies.*;

import java.beans.*;

import javax.swing.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  ayub khan
 */
public class EditorCookieListener extends Object implements PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* holds cookie */
    private EditorCookie cookie = null;
    private String fileName = null;
    private Collablet collablet = null;

    /**
     * constructor
     *
     */
    public EditorCookieListener(EditorCookie cookie, String fileName, Collablet collablet)
    throws CollabException {
        super();
        this.cookie = cookie;
        this.fileName = fileName;
        this.collablet = collablet;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * propertyChange
     *
     * @param        event
     */
    public void propertyChange(PropertyChangeEvent event) {
        Debug.log("CollabFileHandlerSupport", "EditorCookieListener, propertyChange: " + event.getPropertyName()); //NoI18n		

        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(event.getPropertyName())) {
            Debug.log(
                "CollabFileHandlerSupport",
                "EditorCookieListener, received: " + //NoI18n
                EditorCookie.Observable.PROP_OPENED_PANES + " for file: " + //NoI18n
                fileName
            ); //NoI18n	

            if (cookie != null) {
                JEditorPane[] editorPanes = cookie.getOpenedPanes();

                if (editorPanes != null) {
                    Debug.log(
                        "CollabFileHandlerSupport", "EditorCookieListener, " + "pane size: " + editorPanes.length
                    ); //NoI18n

                    for (int i = 0; i < editorPanes.length; i++) {
                        if (editorPanes[i] != null) {
                            editorPanes[i].addFocusListener(new EditorComponentFocusListener(fileName, collablet));
                        }
                    }
                } else {
                    Debug.log("CollabFileHandlerSupport", "EditorCookieListener, " + "no panes opened"); //NoI18n
                }
            }
        }
    }
}
