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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;

import javax.swing.text.*;

import org.netbeans.modules.collab.core.Debug;


/**
 * Support for line regions
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabLineRegion extends CollabRegionSupport implements CollabRegion {
    private Annotation lineAnnotation = null;
    private CollabRegion parent = null;
    private boolean assigned = false;

    /**
     *
     * @param regionName
     * @param regionBegin
     * @param regionEnd
     * @throws CollabException
     */
    public CollabLineRegion(StyledDocument document, String regionName, int regionBegin, int regionEnd)
    throws CollabException {
        super(document, regionName, regionBegin, regionEnd, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                        

    /**
     * getter for region content
     *
     * @throws CollabException
     * @return region content
     */
    public String getContent() throws CollabException {
        return super.getContent();
    }

    /**
     * getLineIndex
     *
     * @return lineIndex
     */
    public int getLineIndex() {
        return getDocument().getDefaultRootElement().getElementIndex(getBeginOffset());
    }

    /**
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(DataObject dataObject, CollabFileHandler fileHandler,
            int style, String annotationMessage) throws CollabException {
        Debug.log(this, "CollabRegionSupport, adding Annotation for region: " + //NoI18n
            regionName
        );

        LineCookie cookie = (LineCookie) dataObject.getCookie(LineCookie.class);
        Line.Set lineSet = cookie.getLineSet();
        Line currLine = lineSet.getCurrent(getLineIndex());
        removeAnnotation();
        if(Debug.isEnabled())
            annotationMessage = String.valueOf(getLineIndex()) + " " + getID(); 
        lineAnnotation = createRegionAnnotation(style, annotationMessage);
        lineAnnotation.attach(currLine);
    }

    /**
     * removeAnnotation
     *
     */
    public void removeAnnotation() {
        if (lineAnnotation != null) {
            lineAnnotation.detach();
        }
    }

    /**
     * setAssigned
     *
     */
    public void setAssigned(CollabRegion parent, boolean assigned) {
        Debug.log("CollabRegionSupport","CRS, setAssigned: "+assigned);
        this.parent=parent;
        this.assigned=assigned;
    }

    /**
     * resetAssigned
     *
     */
    public void resetAssigned() {
        this.parent=null;
        this.assigned=false;
    }

    /**
     * isAssigned
     *
     */
    public boolean isAssigned() {
        Debug.log("CollabRegionSupport","CRS, isAssigned: "+this.assigned);
        return this.assigned;
    }
    
    /**
     * getAssignedRegion
     *
     */
    public CollabRegion getAssignedRegion() {
        Debug.log("CollabRegionSupport","CRS, getAssignedRegion: "+this.parent);
        return this.parent;        
    }
}
