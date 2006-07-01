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


package org.netbeans.core.windows.persistence;


/**
 * Class of reference of TopComponent in mode config properties for communication
 * with persistence management.
 * It keeps data which are read/written from/in .wstcref xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCRefConfig {

    /** Reference to TopComponent by its unique Id. */
    public String tc_id;

    /** Is TopComponent opened. */
    public boolean opened;

    public String previousMode;

    /** Creates a new instance of TCRefConfig */
    public TCRefConfig() {
        tc_id = ""; // NOI18N
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCRefConfig) {
            TCRefConfig tcRefCfg = (TCRefConfig) obj;
            return (tc_id.equals(tcRefCfg.tc_id) &&
                   (opened == tcRefCfg.opened));
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        return hash;
    }
    
    public String toString () {
        return "TCRefConfig: tc_id=" + tc_id + ", opened=" + opened;
    }
    
}
