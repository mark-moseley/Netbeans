/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.beans.beaninfo;

import java.util.ResourceBundle;
import java.lang.reflect.InvocationTargetException;

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
* This class represents BeanInfo root node.
*
* @author   Petr Hrebejk
*/
public final class BiNode extends AbstractNode {


  // static variables ...........................................................................

  // Resource bundle
  private static ResourceBundle bundle = NbBundle.getBundle (BiFeatureNode.class);


  /** generated Serialized Version UID */
  //static final long                      serialVersionUID = -6346315017458451778L;

  private static String ICON_BASE = "/com/netbeans/developer/modules/beans/resources/beanInfo";
  private static String ICON_BASE_PATTERNS = "/com/netbeans/developer/modules/beans/resources/patternGroup";

  private static String PROP_NULL_PROPERTIES = "nullProperties";
  private static String PROP_NULL_EVENTS = "nullEvents";
  private static String PROP_BI_ICON_C16 = "iconColor16x16";
  private static String PROP_BI_ICON_M16 = "iconMono16x16";
  private static String PROP_BI_ICON_C32 = "iconColor32x32";
  private static String PROP_BI_ICON_M32 = "iconMono32x32";
  private static String PROP_BI_DEFAULT_PROPERTY = "defaultPropertyIndex";
  private static String PROP_BI_DEFAULT_EVENT = "defaultEventIndex";
  
  // variables ....................................................................................

  private BiAnalyser biAnalyser;

  private PropertySupport[] propSubnodeProperties =  new PropertySupport[] {
    new PropertySupport.ReadWrite (
         PROP_NULL_PROPERTIES,
         Boolean.TYPE,
         bundle.getString ("PROP_Bi_" + PROP_NULL_PROPERTIES ),
         bundle.getString ("HINT_Bi_" + PROP_NULL_PROPERTIES )
       ) {
         public Object getValue () {
           return new Boolean( biAnalyser.isNullProperties () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setNullProperties ( ((Boolean)val).booleanValue() );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
  };

  private PropertySupport[] eventSubnodeProperties =  new PropertySupport[] { 
    new PropertySupport.ReadWrite (
         PROP_NULL_EVENTS,
         Boolean.TYPE,
         bundle.getString ("PROP_Bi_" + PROP_NULL_EVENTS ),
         bundle.getString ("HINT_Bi_" + PROP_NULL_EVENTS )
       ) {
         public Object getValue () {
           return new Boolean( biAnalyser.isNullEventSets () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setNullEventSets ( ((Boolean)val).booleanValue() );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
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
    setIconBase (ICON_BASE);

    // Add children nodes

    Node[] subnodes = new Node[] {
      new SubNode( biAnalyser, 
               new Class[] { BiFeature.Property.class, BiFeature.IdxProperty.class },
               "CTL_NODE_Properties",
               ICON_BASE_PATTERNS,
               propSubnodeProperties ),
               
      new SubNode( biAnalyser, 
               new Class[] { BiFeature.EventSet.class },
               "CTL_NODE_EventSets",
               ICON_BASE_PATTERNS,
               eventSubnodeProperties )

      /*
      new SubNode( biAnalyser, 
               new Class[] { BiFeature.Method.class },
               "CTL_NODE_Methods",
               ICON_BASE_PATTERNS )
      */
    };

    Sheet sheet = Sheet.createDefault();
    Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
      
    ps.put( new PropertySupport.ReadWrite (
         PROP_BI_ICON_C16,
         String.class,
         bundle.getString ("PROP_Bi_" + PROP_BI_ICON_C16 ),
         bundle.getString ("HINT_Bi_" + PROP_BI_ICON_C16 )
       ) {
         public Object getValue () {
            return biAnalyser.getIconC16();
           //return new Boolean( biAnalyser.isNullProperties () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setIconC16 ( ((String)val) );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
    );
    ps.put( new PropertySupport.ReadWrite (
         PROP_BI_ICON_M16,
         String.class,
         bundle.getString ("PROP_Bi_" + PROP_BI_ICON_M16 ),
         bundle.getString ("HINT_Bi_" + PROP_BI_ICON_M16 )
       ) {
         public Object getValue () {
            return biAnalyser.getIconM16();
           //return new Boolean( biAnalyser.isNullProperties () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setIconM16 ( ((String)val) );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
    );
    ps.put( new PropertySupport.ReadWrite (
         PROP_BI_ICON_C32,
         String.class,
         bundle.getString ("PROP_Bi_" + PROP_BI_ICON_C32 ),
         bundle.getString ("HINT_Bi_" + PROP_BI_ICON_C32 )
       ) {
         public Object getValue () {
            return biAnalyser.getIconC32();
           //return new Boolean( biAnalyser.isNullProperties () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setIconC32 ( ((String)val) );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
    );
    ps.put( new PropertySupport.ReadWrite (
         PROP_BI_ICON_M32,
         String.class,
         bundle.getString ("PROP_Bi_" + PROP_BI_ICON_M32 ),
         bundle.getString ("HINT_Bi_" + PROP_BI_ICON_M32 )
       ) {
         public Object getValue () {
            return biAnalyser.getIconM32();
           //return new Boolean( biAnalyser.isNullProperties () );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setIconM32 ( ((String)val) );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
    );
    ps.put( new PropertySupport.ReadWrite (
         PROP_BI_DEFAULT_PROPERTY,
         Integer.TYPE,
         bundle.getString ("PROP_Bi_" + PROP_BI_DEFAULT_PROPERTY ),
         bundle.getString ("HINT_Bi_" + PROP_BI_DEFAULT_PROPERTY )
       ) {
         public Object getValue () {
            return new Integer( biAnalyser.getDefaultPropertyIndex() );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setDefaultPropertyIndex ( ((Integer)val).intValue() );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
    );
     ps.put( new PropertySupport.ReadWrite (
         PROP_BI_DEFAULT_EVENT,
         Integer.TYPE,
         bundle.getString ("PROP_Bi_" + PROP_BI_DEFAULT_EVENT ),
         bundle.getString ("HINT_Bi_" + PROP_BI_DEFAULT_EVENT )
       ) {
         public Object getValue () {
            return new Integer( biAnalyser.getDefaultEventIndex() );
         }
         public void setValue (Object val) throws
             IllegalAccessException, IllegalArgumentException, InvocationTargetException {
           try {
             biAnalyser.setDefaultEventIndex ( ((Integer)val).intValue() );
           } catch (ClassCastException e) {
             throw new IllegalArgumentException ();
           }
         }
       }
     );  
    setSheet(sheet);
    
    ((Children.Array)getChildren()).add( subnodes );
    
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (BiNode.class);
  }
 
  static class SubNode extends AbstractNode implements Node.Cookie {

    private static SystemAction[] staticActions;

    SubNode ( BiAnalyser biAnalyser, Class[] keys, String titleKey, String iconBase, 
              PropertySupport[] properties ) {
      super ( new BiChildren (  biAnalyser, keys ) );
      setDisplayName (NbBundle.getBundle(BiNode.class).
                     getString (titleKey));
      setIconBase ( iconBase );


      Sheet sheet = Sheet.createDefault();
      Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

      for ( int i = 0; i < properties.length; i++ ) { 
        ps.put( properties[i] );
      }

      setSheet(sheet);

      getCookieSet().add ( this );
    }

    public HelpCtx getHelpCtx () {
      return new HelpCtx (SubNode.class);
    }
  
    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
      if (staticActions == null) {
        staticActions = new SystemAction[] {
          SystemAction.get (BiIncludeAllAction.class),
          SystemAction.get (BiExcludeAllAction.class),
          null
        };
      }
      return staticActions;
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

  }
}


/*
 * Log
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */
