package org.netbeans.modules.web.jsf.navigation.graph;
import java.awt.Image;
import java.io.IOException;
import org.openide.nodes.Node;

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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import org.openide.util.HelpCtx;
/**
 *
 * @author joelle
 */
public abstract class PageFlowSceneElement {
    private String name;
    
    
    public PageFlowSceneElement(){
    }
    
    public boolean equals(Object obj) {
        return (this == obj);
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    private boolean modifiable = true;
    public boolean isModifiable() {
        return modifiable;
    }
    public void setModifiable(boolean modifiable ){
        this.modifiable = modifiable;
    }
    
    public abstract Node getNode();
    public abstract HelpCtx getHelpCtx();
    public abstract void destroy() throws IOException;
    public abstract boolean canDestroy();
    public abstract boolean canRename();
    public abstract Image getIcon( int type );
}
