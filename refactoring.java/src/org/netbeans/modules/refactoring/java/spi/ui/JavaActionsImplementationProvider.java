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

package org.netbeans.modules.refactoring.java.spi.ui;

import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class JavaActionsImplementationProvider {

    /**
     * @return true if provider can handle rename
     */
    public boolean canEncapsulateFields(Lookup node) {
        return false;
    }

    /**
     * @return implementation of Rename Action
     */
    public void doEncapsulateFields(Lookup selectedNodes) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @return true if provider can handle find usages
     */
    public boolean canChangeParameters(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Find Usages Action
     */
    public void doChangeParameters(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @return true if provider can handle delete
     */
    public boolean canPullUp(Lookup lookup) {
        return false;
    }
    
    /**
     * @return implementation of Delete Action
     */
    public void doPullUp(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @return true if provider can handle move
     */
    public boolean canPushDown(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Move Action
     */
    public void doPushDown(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }
    
    /**
     * @return true if provider can handle copy
     */
    public boolean canInnerToOuter(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Copy Action
     */
    public void doInnerToOuter(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    /**
     * @return true if provider can handle copy
     */
    public boolean canUseSuperType(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Copy Action
     */
    public void doUseSuperType(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    /**
     * @return true if provider can handle copy
     */
    public boolean canExtractSuperclass(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Copy Action
     */
    public void doExtractSuperclass(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    /**
     * @return true if provider can handle copy
     */
    public boolean canExtractInterface(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Copy Action
     */
    public void doExtractInterface(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    
}
