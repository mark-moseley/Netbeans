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

import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.Action;
import org.netbeans.modules.beans.PatternAnalyser;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
* This class represents BeanInfo root node.
*
* @author   Petr Hrebejk
*/
public final class BiNode extends AbstractNode {


    // static variables ...........................................................................

    /** generated Serialized Version UID */
    //static final long                      serialVersionUID = -6346315017458451778L;

    private static String ICON_BASE = "org/netbeans/modules/beans/resources/beanInfo.gif"; // NOI18N
    private static String ICON_BASE_PATTERNS = "org/netbeans/modules/beans/resources/patternGroup"; // NOI18N
    private static String WAIT_ICON_BASE = "org/openide/src/resources/wait.gif"; // NOI18N

    private static String PROP_NULL_DESCRIPTOR = "nullDescriptor"; // NOI18N
    private static String PROP_NULL_PROPERTIES = "nullProperties"; // NOI18N
    private static String PROP_NULL_EVENTS = "nullEvents"; // NOI18N
    private static String PROP_NULL_METHODS = "nullMethods"; // NOI18N
    private static String PROP_LAZY_DESCRIPTOR = "lazyDescriptor"; // NOI18N
    private static String PROP_LAZY_PROPERTIES = "lazyProperties"; // NOI18N
    private static String PROP_LAZY_EVENTS = "lazyEvents"; // NOI18N
    private static String PROP_LAZY_METHODS = "lazyMethods"; // NOI18N
    private static String PROP_BI_ICON_C16 = "iconColor16x16"; // NOI18N
    private static String PROP_BI_ICON_M16 = "iconMono16x16"; // NOI18N
    private static String PROP_BI_ICON_C32 = "iconColor32x32"; // NOI18N
    private static String PROP_BI_ICON_M32 = "iconMono32x32"; // NOI18N
    private static String PROP_BI_DEFAULT_PROPERTY = "defaultPropertyIndex"; // NOI18N
    private static String PROP_BI_DEFAULT_EVENT = "defaultEventIndex"; // NOI18N
    private static String PROP_USE_SUPERCLASS   = "useSuperclass"; // NOI18N
    
    static javax.swing.GrayFilter grayFilter = null;
    
    static{
        grayFilter = new javax.swing.GrayFilter(true, 5);
    }

    // variables ....................................................................................

    private BiAnalyser biAnalyser;
    
    private PropertySupport[] descSubnodeDescriptor =  new PropertySupport[] {
                new PropertySupport.ReadWrite (
                    PROP_NULL_DESCRIPTOR,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_DESCRIPTOR ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_DESCRIPTOR )
                ) {
                    public Object getValue () {
                        return biAnalyser.isNullDescriptor () ? Boolean.TRUE : Boolean.FALSE;
                    }
                    public void setValue (Object val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {                            
                            biAnalyser.setNullDescriptor ( ((Boolean)val).booleanValue() );                            
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

            
    private PropertySupport[] propSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite (
                    PROP_NULL_PROPERTIES,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_PROPERTIES ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_PROPERTIES )
                ) {
                    public Object getValue () {
                        return biAnalyser.isNullProperties () ? Boolean.TRUE : Boolean.FALSE;
                    }
                    public void setValue (Object val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullProperties ( ((Boolean)val).booleanValue() );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    private PropertySupport[] eventSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite (
                    PROP_NULL_EVENTS,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_EVENTS ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_EVENTS )
                ) {
                    public Object getValue () {
                        return biAnalyser.isNullEventSets () ? Boolean.TRUE : Boolean.FALSE;
                    }
                    public void setValue (Object val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullEventSets ( ((Boolean)val).booleanValue() );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    private PropertySupport[] methodSubnodeProperties =  new PropertySupport[] {
                new PropertySupport.ReadWrite (
                    PROP_NULL_PROPERTIES,
                    Boolean.TYPE,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NULL_METHODS ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NULL_METHODS )
                ) {
                    public Object getValue () {
                        return biAnalyser.isNullMethods () ? Boolean.TRUE : Boolean.FALSE;
                    }
                    public void setValue (Object val) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            biAnalyser.setNullMethods ( ((Boolean)val).booleanValue() );
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        iconChange();
                    }
                }
            };

    // constructors ..................................................................................

    /**
    * Creates tree for BeanInfo Analyser.
    */
    BiNode ( final BiAnalyser biAnalyser ) {
        /*
        super ( new BiChildren ( biAnalyser, 
          new Class[] {
            BiFeature.Property.class, 
            BiFeature.IdxProperty.class,
            BiFeature.EventSet.class } ) );
        */
        super (new Children.Array() );
        this.biAnalyser = biAnalyser;
        setDisplayName (NbBundle.getBundle(BiNode.class).
                        getString ("CTL_NODE_BeanInfo"));
        setIconBaseWithExtension (ICON_BASE);

        Node[] subnodes = (biAnalyser.isOlderVersion() ? 
            new Node[] {
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Property.class, BiFeature.IdxProperty.class },
                               "CTL_NODE_Properties", // NOI18N
                               ICON_BASE_PATTERNS,
                               propSubnodeProperties, 
                               null ),

                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.EventSet.class },
                               "CTL_NODE_EventSets", // NOI18N
                               ICON_BASE_PATTERNS,
                               eventSubnodeProperties, 
                               null )
            } : new Node[] {
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Descriptor.class },
                               "CTL_NODE_Descriptor", // NOI18N
                               ICON_BASE_PATTERNS,
                               descSubnodeDescriptor ,
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_DESCRIPTOR, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_DESCRIPTOR ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_DESCRIPTOR ),
                                    "isLazyDescriptor", "setLazyDescriptor" )} // NOI18N
                               ),
                               
                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.Property.class, BiFeature.IdxProperty.class },
                               "CTL_NODE_Properties", // NOI18N
                               ICON_BASE_PATTERNS,
                               propSubnodeProperties,
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_DESCRIPTOR, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_PROPERTIES ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_PROPERTIES ),
                                    "isLazyProperties", "setLazyProperties" )} // NOI18N
                               ),

                    new SubNode( biAnalyser,
                               new Class[] { BiFeature.EventSet.class },
                               "CTL_NODE_EventSets", // NOI18N
                               ICON_BASE_PATTERNS,
                               eventSubnodeProperties, 
                               new Node.Property[] {
                                    createProperty (biAnalyser, Boolean.TYPE,
                                    PROP_LAZY_EVENTS, 
                                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_EVENTS ),
                                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_EVENTS ),
                                    "isLazyEventSets", "setLazyEventSets" )} // NOI18N
                               ),

                    new SubNode( biAnalyser, 
                           new Class[] { BiFeature.Method.class },
                           "CTL_NODE_Methods", // NOI18N
                           ICON_BASE_PATTERNS,
                           methodSubnodeProperties, 
                           new Node.Property[] {
                                createProperty (biAnalyser, Boolean.TYPE,
                                PROP_LAZY_METHODS, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_LAZY_METHODS ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_LAZY_METHODS ),
                                "isLazyMethods", "setLazyMethods" )} // NOI18N
                           )
            });
        
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_C16,
                    String.class,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_C16 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_C16 )
                ) {
                    public Object getValue () throws
                        IllegalAccessException, InvocationTargetException {
                        if( biAnalyser.getIconC16() != null ) 
                            ie.setAsText(biAnalyser.getIconC16());
                        else
                            ie.setAsText("null"); //NOI18N    
                            
                        return biAnalyser.getIconC16();                        
                    }
                    
                    public void setValue (Object value) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            if( value == null )
                                biAnalyser.setIconC16 ( null );
                            else {
                                if (value instanceof BiIconEditor.BiImageIcon) {
                                    biAnalyser.setIconC16 ( ie.getSourceName() );
                                }
                                else{
                                    biAnalyser.setIconC16( (String)value );
                                }
                            }
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                    }                                
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_M16,
                    String.class,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_M16 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_M16 )
                ) {
                    public Object getValue () throws
                        IllegalAccessException, InvocationTargetException {
                        if( biAnalyser.getIconM16() != null ) 
                            ie.setAsText(biAnalyser.getIconM16());
                        else
                            ie.setAsText("null"); //NOI18N    
                        return biAnalyser.getIconM16();                        
                    }
                    
                    public void setValue (Object value) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            if( value == null )
                                biAnalyser.setIconM16 ( null );
                            else {
                                if (value instanceof BiIconEditor.BiImageIcon) {
                                    biAnalyser.setIconM16 ( ie.getSourceName() );
                                }
                                else{
                                    biAnalyser.setIconM16( (String)value );
                                }
                            }
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                    }
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_C32,
                    String.class,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_C32 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_C32 )
                ) {
                    public Object getValue () throws
                        IllegalAccessException, InvocationTargetException {
                        if( biAnalyser.getIconC32() != null ) 
                            ie.setAsText(biAnalyser.getIconC32());
                        else
                            ie.setAsText("null"); //NOI18N    
                        
                        return biAnalyser.getIconC32();
                    }
                    
                    public void setValue (Object value) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            if( value == null )
                                biAnalyser.setIconC32 ( null );
                            else {
                                if (value instanceof BiIconEditor.BiImageIcon) {
                                    biAnalyser.setIconC32 ( ie.getSourceName() );
                                }
                                else{
                                    biAnalyser.setIconC32( (String)value );
                                }
                            }
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                    }                    
                }
              );
        ps.put( new ImagePropertySupportRW (
                    PROP_BI_ICON_M32,
                    String.class,
                    GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_ICON_M32 ),
                    GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_ICON_M32 )
                ) {
                    public Object getValue () throws
                        IllegalAccessException, InvocationTargetException {
                        if( biAnalyser.getIconM32() != null ) 
                            ie.setAsText(biAnalyser.getIconM32());
                        else
                            ie.setAsText("null"); //NOI18N    
                        
                        return biAnalyser.getIconM32();
                    }
                    
                    public void setValue (Object value) throws
                        IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        try {
                            if( value == null )
                                biAnalyser.setIconM32 ( null );
                            else {
                                if (value instanceof BiIconEditor.BiImageIcon) {
                                    biAnalyser.setIconM32 ( ie.getSourceName() );
                                }
                                else{
                                    biAnalyser.setIconM32( (String)value );
                                }
                            }
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                    }                    
                }
              );
        ps.put( createProperty (biAnalyser, Integer.TYPE,
                                PROP_BI_DEFAULT_PROPERTY, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_DEFAULT_PROPERTY ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_DEFAULT_PROPERTY ),
                                "getDefaultPropertyIndex", "setDefaultPropertyIndex" ) ); // NOI18N
        
        ps.put( createProperty (biAnalyser, Integer.TYPE,
                                PROP_BI_DEFAULT_EVENT, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BI_DEFAULT_EVENT ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BI_DEFAULT_EVENT ),
                                "getDefaultEventIndex", "setDefaultEventIndex" ) ); // NOI18N

        //only if it is super class version (since 3.3)      
        if(biAnalyser.isSuperclassVersion()){      
            ps.put( createProperty (biAnalyser, Boolean.TYPE,
                                PROP_USE_SUPERCLASS, 
                                GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_USE_SUPERCLASS ),
                                GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_USE_SUPERCLASS ),
                                "isUseSuperClass", "setUseSuperClass" ) ); // NOI18N
        }              
        setSheet(sheet);

        ((Children.Array)getChildren()).add( subnodes );

    }
   
    /** refresh icons after get from introspection change */
    public void iconChange(){
        Node[] nodes = ((Children.Array)getChildren()).getNodes();
        for( int i = 0; i < nodes.length; i++ ){
            ((SubNode)nodes[i]).iconChanged();
        }
    }
    
    static class SubNode extends AbstractNode implements Node.Cookie {

        //private static SystemAction[] staticActions;
        private BiAnalyser biAnalyser;
        private Class key; 
        
        SubNode ( BiAnalyser biAnalyser, Class[] keys, String titleKey, String iconBase,
                  Node.Property[] properties, Node.Property[] expert ) {
            super ( new BiChildren (  biAnalyser, keys ) );
            setDisplayName (NbBundle.getBundle(BiNode.class).
                            getString (titleKey));
            setIconBaseWithExtension ( iconBase + ".gif" );
                
            this.biAnalyser = biAnalyser;
            this.key = keys[0];
            
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

            for ( int i = 0; i < properties.length; i++ ) {
                ps.put( properties[i] );
            }
            
            if( expert != null ){                
                Sheet.Set eps = sheet.createExpertSet();

                for ( int i = 0; i < expert.length; i++ ) {
                    eps.put( expert[i] );
                }
                sheet.put(eps);
            }
            
            setSheet(sheet);

            getCookieSet().add ( this );
        }
        
        public java.awt.Image getIcon( int type ){
            if( key == BiFeature.Descriptor.class && biAnalyser.isNullDescriptor() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Property.class && biAnalyser.isNullProperties() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.EventSet.class && biAnalyser.isNullEventSets() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Method.class && biAnalyser.isNullMethods() )
                return grayFilter.createDisabledImage(super.getIcon(type));

            return super.getIcon(type);
        }

        public java.awt.Image getOpenedIcon( int type ){
            if( key == BiFeature.Descriptor.class && biAnalyser.isNullDescriptor() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Property.class && biAnalyser.isNullProperties() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.EventSet.class && biAnalyser.isNullEventSets() )
                return grayFilter.createDisabledImage(super.getIcon(type));
            if( key == BiFeature.Method.class && biAnalyser.isNullMethods() )
                return grayFilter.createDisabledImage(super.getIcon(type));

            return super.getOpenedIcon(type);
        }

        /** Getter for set of actions that should be present in the
        * popup menu of this node. This set is used in construction of
        * menu returned from getContextMenu and specially when a menu for
        * more nodes is constructed.
        *
        * @return array of system actions that should be in popup menu
        */
        public Action[] getActions ( boolean context ) {
            if ( context ) {
                return super.getActions( true );
            }
            else {
                Children ch = getChildren();
                Node[] nodes = ch.getNodes();
                if ( nodes == null )
                    return new SystemAction[0];

                if( nodes.length == 0 || ( nodes[0] != null && ((BiFeatureNode)nodes[0]).getBiFeature() instanceof BiFeature.Descriptor) )
                    return new SystemAction[0];

                return new SystemAction[] {
                                        SystemAction.get (BiIncludeAllAction.class),
                                        SystemAction.get (BiExcludeAllAction.class),
                                        null
                                    };
            }                          
        }

        void includeAll( boolean value) {
            Children ch = getChildren();

            Node[] nodes = ch.getNodes();

            if ( nodes == null )
                return;

            for( int i = 0; i < nodes.length; i++ ) {
                ((BiFeatureNode)nodes[i]).include( value );
            }

        }

        /** refresh icons after get from introspection change */
        public void iconChanged(){
            fireIconChange();
            fireOpenedIconChange();
            
            Children ch = getChildren();
            Node[] nodes = ch.getNodes();
            if ( nodes == null )
                return;

            for( int i = 0; i < nodes.length; i++ ) {
                ((BiFeatureNode)nodes[i]).iconChanged();
            }
        }
    }

    // Inner Class ---------------------------------------------------------------

    static class Wait extends AbstractNode {

        Wait () {

            super( Children.LEAF );
            setDisplayName( JavaMetamodel.getManager().isScanInProgress()? NbBundle.getBundle( BiNode.class ).getString( "CTL_NODE_WaitScan" ) : NbBundle.getBundle( BiNode.class ).getString( "CTL_NODE_Wait" ) );
            setIconBaseWithExtension( WAIT_ICON_BASE );

        }
    }
    
    abstract class ImagePropertySupportRW extends PropertySupport.ReadWrite
    {
        BiIconEditor ie = null;
        
        ImagePropertySupportRW(String name, Class type,
                              String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
            ie = new BiIconEditor( PatternAnalyser.fileObjectForElement( biAnalyser.classElement ) );            
        }

        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                public java.awt.Component getCustomEditor() {
                    return ie.getCustomEditor();
                }
                    
                public boolean supportsCustomEditor() {
                    return true;
                }
                
            public void setAsText(String text) throws java.lang.IllegalArgumentException {
                    ie.setAsText(text);
                    setValue(ie.getSourceName());
                }
            };
        }
    }    

    public static Node.Property createProperty (Object inst, Class type,
                                                String name, String dispName,
                                                String shortDesc,
                                                String getter, String setter ) {
        Node.Property prop;

        try {
            prop = new PropertySupport.Reflection (inst, type, getter, setter);
        } catch (NoSuchMethodException e) {            
            throw new IllegalStateException (e.getMessage() + " " + getter); // NOI18N
        }
        
        prop.setName (name);
        prop.setDisplayName (dispName);
        prop.setShortDescription (shortDesc);
        return prop;
    }
}
