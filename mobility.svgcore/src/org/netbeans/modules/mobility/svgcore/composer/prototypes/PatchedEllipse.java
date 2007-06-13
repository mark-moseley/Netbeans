/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import com.sun.perseus.model.*;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.Ellipse;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedEllipse extends Ellipse implements PatchedElement {
    private String    m_idBackup  = null;
    private SVGObject m_svgObject = null; 

    public PatchedEllipse(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }
    
    public PatchedEllipse(final DocumentNode ownerDocument, boolean isCircle) {
        super(ownerDocument, isCircle);
    }
    
    public void attachSVGObject(SVGObject obj) {
        m_svgObject = obj;
    }

    public SVGObject getSVGObject() {
        return m_svgObject;
    }
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id       = null;
        } else {
            id = m_idBackup;
        }
    }
    
    private int [] m_path;
    
    public void setPath(int [] path) {
        m_path = path;
    }
    
    public int [] getPath() {
        return m_path;
    }
        
    public ElementNode newInstance(final DocumentNode doc) {
        return new PatchedEllipse(doc, isCircle);
    }    
}
