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

package org.netbeans.modules.httpserver;

import java.beans.*;
import javax.swing.*;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.openide.util.NbBundle;

/** Property editor for host property of HttpServerSettings class
*
* @author Ales Novak, Petr Jiricka
*/
public class HostPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

    private PropertyEnv env;

    /** localized local (selected) host string*/
    private static String localhost() {
        return NbBundle.getMessage(HostPropertyEditor.class, "CTL_Local_host");
    }

    /** localized any host string*/
    private static String anyhost() {
        return NbBundle.getMessage(HostPropertyEditor.class, "CTL_Any_host");
    }

    /** @return text for the current value */
    public String getAsText () {
        HttpServerSettings.HostProperty hp = (HttpServerSettings.HostProperty) getValue();
        if (hp == null) {
            return "";
        }
        String host = hp.getHost();
        if (host.equals(HttpServerSettings.LOCALHOST)) {
            return localhost () + hp.getGrantedAddresses ();
        }
        else {
            return anyhost ();
        }
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        if (anyhost ().equals (text)) {
            setValue (new HttpServerSettings.HostProperty ("", HttpServerSettings.ANYHOST));    // NOI18N
            return;
        } else if (text != null && text.startsWith(localhost())) {
            setValue (new HttpServerSettings.HostProperty (text.substring (localhost ().length ()), HttpServerSettings.LOCALHOST));
            return;
        } else if (text != null) {
            setValue (new HttpServerSettings.HostProperty (text, HttpServerSettings.LOCALHOST));
            return;
        }
        throw new IllegalArgumentException (text);
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new HostPropertyCustomEditor (this, env);
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

}
