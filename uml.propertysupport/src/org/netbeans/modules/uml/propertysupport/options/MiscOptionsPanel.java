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

package org.netbeans.modules.uml.propertysupport.options;

import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class MiscOptionsPanel {
    
  
    private final boolean debug = true ;
    private UMLMiscOptionsPanelForm form = null ;
    
    /** Creates a new instance of OptionsPanel */
    public MiscOptionsPanel() {
        log("MiscOptionsPanel");
    }
    
    
    public UMLMiscOptionsPanelForm create() {
        if (form != null) 
            return form;
        else
            form = new UMLMiscOptionsPanelForm() ;
        
        return form ;
    }
    
    public UMLMiscOptionsPanelForm getPanel() {
        return create();
    }
    
    
    public String getDisplayName() {
        return loc("MISC_OPTIONS") ;
    }
    
    private String loc(String key) {
        return NbBundle.getMessage(MiscOptionsPanel.class, key) ;
    }
    
    private void log (String s) {
        if (debug) System.out.println (this.getClass().toString()+"::"+s);
    }
}
