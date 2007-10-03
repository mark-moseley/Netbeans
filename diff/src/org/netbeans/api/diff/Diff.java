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

package org.netbeans.api.diff;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.openide.util.Lookup;

/**
 * This class represents a visual diff presenter, that knows how to compute the
 * differences between files and show them to the user.
 *
 * @author  Martin Entlicher
 */
public abstract class Diff extends Object {

    /**
     * Get the default visual diff presenter.
     */
    public static Diff getDefault() {
        return Lookup.getDefault().lookup(Diff.class);
    }
    
    /**
     * Get all visual diff presenters registered in the system.
     */
    public static Collection<? extends Diff> getAll() {
        return Lookup.getDefault().lookup(new Lookup.Template<Diff>(Diff.class)).allInstances();
    }
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The Component representing the diff visual representation
     *         or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Component createDiff(String name1, String title1,
                                         Reader r1, String name2, String title2,
                                         Reader r2, String MIMEType) throws IOException ;
    
    /**
     * Creates single-window diff component that does not include any navigation controls and
     * is controlled programatically via the returned DiffView interface.
     * <p>
     * The StreamSource can be used to save the source content if it's modified
     * in the view. The view should not allow source modification if StreamSource.createWriter()
     * returns <code>null</code>.
     * 
     * @param s1 the first source
     * @param s2 the second source
     * @return DiffView controller interface
     */ 
    public DiffView createDiff(StreamSource s1, StreamSource s2) throws IOException {
        final Component c = createDiff(s1.getName(), s1.getTitle(), s1.createReader(),
                                       s2.getName(), s2.getTitle(), s2.createReader(),
                                       s1.getMIMEType());
        return new DiffView() {
            
            public Component getComponent() {
                return c;
            }
    
            public int getDifferenceCount() {
                return 0;
            }
    
            public boolean canSetCurrentDifference() {
                return false;
            }

            public void setCurrentDifference(int diffNo) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
    
            public int getCurrentDifference() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
            
            public javax.swing.JToolBar getToolBar() {
                return null;
            }
    
            public void addPropertyChangeListener(PropertyChangeListener l) {}
    
            public void removePropertyChangeListener(PropertyChangeListener l) {}
    
        };
    }
}
