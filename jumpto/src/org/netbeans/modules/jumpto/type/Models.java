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

package org.netbeans.modules.jumpto.type;

import java.util.List;
import javax.swing.ListModel;

/**
 * (copied from org.netbeans.modules.java.source.util.Models
 * @author Petr Hrebejk
 */
public final class Models {

    private  Models() {
    }


    public static <T> ListModel fromList( List<? extends T> list ) {
        return new ListListModel<T>( list );
    }

    /** Creates list model which translates the objects using a factory.
     */
    public static <T,P> ListModel translating( ListModel model, Factory<T,P> factory ) {
        return new TranslatingListModel<T,P>( model, factory );
    }
 
    // Private innerclasses ----------------------------------------------------        
    
    private static class ListListModel<T> implements ListModel {
    
        private List<? extends T> list;

        /** Creates a new instance of IteratorList */
        public ListListModel( List<? extends T> list ) {
            this.list = list;
        }

        // List implementataion ------------------------------------------------

        public T getElementAt(int index) {
            // System.out.println("GE " + index );
            return list.get( index );
        }

        public int getSize() {
            return list.size();
        }

        public void removeListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }

        public void addListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }

    }
    
    private static class TranslatingListModel<T,P> implements ListModel {
    
        private Factory<T,P> factory;
        private ListModel listModel;


        /** Creates a new instance of IteratorList */
        public TranslatingListModel( ListModel model, Factory<T,P> factory ) {
            this.listModel = model;
            this.factory = factory;
        }

        // List implementataion ----------------------------------------------------

        //@SuppressWarnings("xlint")
        public T getElementAt(int index) {        
            @SuppressWarnings("unchecked")
            P original = (P)listModel.getElementAt( index );
            return factory.create( original );
        }

        public int getSize() {
            return listModel.getSize();
        }

        public void removeListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }

        public void addListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }


    }
    
}
