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

package org.netbeans.modules.beans.beaninfo;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.openide.filesystems.Repository;

/**
 * Finds or creates BeanInfo source elemnet for the class.
 * It can regenerate the source if there are the guarded blocks.
 * @author  Petr Hrebejk
 */

public final class BeanInfoSource extends Object {

    private static final String BEANINFO_NAME_EXT = "BeanInfo"; // NOI18N
    
    private static final String DESCRIPTOR_SECTION = "BeanDescriptor"; // NOI18N
    private static final String PROPERTIES_SECTION = "Properties"; // NOI18N
    private static final String EVENTSETS_SECTION = "Events"; // NOI18N
    private static final String ICONS_SECTION = "Icons"; // NOI18N
    private static final String IDX_SECTION = "Idx"; // NOI18N
    private static final String METHODS_SECTION = "Methods"; // NOI18N
    private static final String SUPERCLASS_SECTION = "Superclass";  // NOI18N
    
    private JavaClass classElement;

    private DataObject   biDataObject = null;
    private JavaEditor   javaEditor =  null;
    //private PatternAnalyser pa = null;

    /** Creates new BeanInfoSource */
    public BeanInfoSource (JavaClass classElement ) {
        this.classElement = classElement;
        //this.pa = pa;

        findBeanInfo();
    }

    /** Returns wether the bean info exists or not */
    boolean exists() {
        return biDataObject != null;
    }

    /** Checks wether the bean info object has Guarded sections i.e.
     * was created from netbeans template.
     */
    boolean isNbBeanInfo() {

        if ( !exists() || javaEditor == null ) {
            return false;
        }

        //JavaEditor.InteriorSection dis = javaEditor.findInteriorSection( DESCRIPTOR_SECTION );
        JavaEditor.InteriorSection pis = javaEditor.findInteriorSection( PROPERTIES_SECTION );
        JavaEditor.InteriorSection eis = javaEditor.findInteriorSection( EVENTSETS_SECTION );
//        JavaEditor.InteriorSection mis = javaEditor.findInteriorSection( METHODS_SECTION );
        //JavaEditor.SimpleSection iss = javaEditor.findSimpleSection( ICONS_SECTION );
        JavaEditor.SimpleSection dss = javaEditor.findSimpleSection( IDX_SECTION );

        //return ( pis != null && eis != null && iss != null && dss != null);
        return ( pis != null && eis != null && dss != null);
    }

    boolean hasIconInfo(){
        JavaEditor.SimpleSection iss = javaEditor.findSimpleSection( ICONS_SECTION );
        return ( iss != null );
    }
    /** Checks wether the bean descriptor object has Guarded sections i.e.
     * was created from new netbeans template.
     */
    boolean isNbBeanInfoDescriptor() {

        if ( !exists() || javaEditor == null ) {
            return false;
        }
        JavaEditor.InteriorSection dis = javaEditor.findInteriorSection( DESCRIPTOR_SECTION );
        return ( dis != null );
    }

    /** Checks wether the bean info object has Guarded sections for superclass i.e.
     * was created from new netbeans template.
     */
    boolean isNbSuperclass() {

        if ( !exists() || javaEditor == null ) {
            return false;
        }
        JavaEditor.InteriorSection dis = javaEditor.findInteriorSection( SUPERCLASS_SECTION );
        return ( dis != null );
    }

    /** Finds the bean info for classElement asspciated with this
        object */
    void findBeanInfo() {

        javaEditor = null;
        
        Resource sc = classElement.getResource();
        if ( sc == null ) {
            return;
        }
        
        DataObject dataObject = JavaMetamodel.getManager().getDataObject(sc);
        if ( dataObject == null ) {
            return;
        }

        FileObject folder = dataObject.getFolder().getPrimaryFile();
        if ( folder == null ) {
            return;
        }
        
        FileObject biFile = folder.getFileObject( dataObject.getName() + BEANINFO_NAME_EXT, "java" ); // NOI18N
        if ( biFile == null ) {
            return;
        }
        
        try {
            biDataObject = DataObject.find( biFile );
            javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );
            //System.out.println("ClassElem : " + biDataObject ); // NOI18N
        }
        catch ( org.openide.loaders.DataObjectNotFoundException e ) {
            // Do nothing if no data object is found
        }

    }

    /** Deletes the BeanInfo */
    void delete() throws java.io.IOException {
        biDataObject.delete();
    }


    /** Creates beanInfo data object */
    void createFromTemplate( boolean iconBlock ) {        
        FileObject foTemplates = Repository.getDefault().getDefaultFileSystem().findResource("Templates"); //NOI18N ;
        if ( foTemplates == null ) {
            return;
        }

        FileObject foClassTemplates = foTemplates.getFileObject( "Beans" ); // NOI18N
        if ( foClassTemplates == null ) {
            return;
        }    
            
        FileObject foBiTemplate = null;
        
        if( iconBlock ){
            foBiTemplate = foClassTemplates.getFileObject( "BeanInfo", "java" ); // NOI18N
        }
        else {
            foBiTemplate = foClassTemplates.getFileObject( "BeanInfoNoIcon", "java" ); // NOI18N
        }

        if ( foBiTemplate == null ) {
            return;
        }

        try {
            DataObject doBiTemplate = DataObject.find ( foBiTemplate );
             
            Resource sc = classElement.getResource();
            if ( sc == null )
                return;

            DataObject dataObject = JavaMetamodel.getManager().getDataObject(sc);

            if ( dataObject == null ) {
                return;
            }
            
            DataFolder folder = dataObject.getFolder();
            biDataObject = doBiTemplate.createFromTemplate( folder, dataObject.getName() + BEANINFO_NAME_EXT );
            javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );
        }
        catch ( org.openide.loaders.DataObjectNotFoundException e ) {
            //System.out.println ( e );
            // Do nothing if no data object is found
        }
        catch ( java.io.IOException e ) {
            //System.out.println ( e );
            // Do nothing if no data object is found
        }
    }

    /** If the bean info is available returns the bean info data object */
    DataObject getDataObject() {
        return biDataObject;
    }

    /** opens the source */
    void open() {
        javaEditor.open();
    }

    /** Sets the header and bottom of properties section */
    void setDescriptorSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( DESCRIPTOR_SECTION );

        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getDescriptorSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( DESCRIPTOR_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else
            return null;

    }

    /** Sets the header and bottom of properties section */
    void setPropertiesSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( PROPERTIES_SECTION );

        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getPropertiesSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( PROPERTIES_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else
            return null;

    }

    /** Sets the header and bottom of methods section */
    void setMethodsSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( METHODS_SECTION );

        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getMethodsSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( METHODS_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else {
          return null;
        }

    }

    /** Sets the header and bottom of event sets section */
    void setEventSetsSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( EVENTSETS_SECTION );
        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getEventSetsSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( EVENTSETS_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else
            return null;
    }

    /** Gets the header of properties setion */
    String getIconsSection() {
        JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( ICONS_SECTION );

        if ( ss != null ) {
            return ss.getText();
        }
        else
            return null;
    }

    /** Sets the header of properties setion */
    void setIconsSection( String text ) {
        JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( ICONS_SECTION );
        if ( ss != null )
            ss.setText( text );
    }

    /** Gets the header of properties setion */
    String getDefaultIdxSection() {
        JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( IDX_SECTION );

        if ( ss != null ) {
            return ss.getText();
        }
        else
            return null;
    }

    /** Sets the header of properties setion */
    void setDefaultIdxSection( String text ) {
        JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( IDX_SECTION );
        if ( ss != null )
            ss.setText( text );
    }

    /** Sets the header and bottom of properties section */
    void setSuperclassSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( SUPERCLASS_SECTION );

        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getSuperclassSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( SUPERCLASS_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else
            return null;
    }

    /*
    void regenerateMethods() {
      JavaEditor.InteriorSection is = javaEditor.findInteriorSection( "Events" );
      
      if ( is != null ) {
        is.setHeader( BeanInfoGenerator.generateMethods( classElement.getName().getName(), methods ) );
        is.setBottom( BeanInfoGenerator.generateMethodsBottom( methods ) );
      }
}
    */

}
