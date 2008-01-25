/*
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */
package org.netbeans.modules.mashup.db.ui.model;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;


/**
 * Exposes getters for flatfile column table properties. TODO Extend this class to expose
 * setters for read-write property sheets (MutabledFlatfileColumnBeanInfo?)
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileColumnBeanInfo extends SimpleBeanInfo {

    private static BeanDescriptor beanDescriptor = null;

    private static PropertyDescriptor[] properties = null;

    private static EventSetDescriptor[] eventSets = null;

    private static MethodDescriptor[] methods = null;
    
    private static transient final Logger mLogger = LogUtil.getLogger(FlatfileColumnBeanInfo.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable properties of this bean. May return
     *         null if the information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(FlatfileColumn.class);
        }

        return beanDescriptor;
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable properties
     *         supported by this bean. May return null if the information should be
     *         obtained by automatic analysis.
     *         <p>
     *         If a property is indexed, then its entry in the result array will belong to
     *         the IndexedPropertyDescriptor subclass of PropertyDescriptor. A client of
     *         getPropertyDescriptors can use "instanceof" to check if a given
     *         PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            List myProps = new ArrayList();
            String nbBundle1 = mLoc.t("PRSR001: Column name");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("name", FlatfileColumn.class, "getName", null); // NOI18N
                String label = Localizer.parse(nbBundle1); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            String nbBundle2 = mLoc.t("PRSR001: Ordinal position");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("ordinalPosition", FlatfileColumn.class, "getOrdinalPosition", null); // NOI18N
                String label = Localizer.parse(nbBundle2);// NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            String nbBundle3 = mLoc.t("PRSR001: Precision / length");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("precision", FlatfileColumn.class, "getPrecision", null); // NOI18N
                String label = Localizer.parse(nbBundle3); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            String nbBundle4 = mLoc.t("PRSR001: Scale");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("scale", FlatfileColumn.class, "getScale", null); // NOI18N
                String label = Localizer.parse(nbBundle4); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            
            String nbBundle5 = mLoc.t("PRSR001: Data type");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("sqlType", FlatfileColumn.class, "getJdbcTypeString", null); // NOI18N
                String label = Localizer.parse(nbBundle5); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            String nbBundle6 = mLoc.t("PRSR001: Nullable");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("nullable", FlatfileColumn.class, "isNullable", null); // NOI18N
                String label = Localizer.parse(nbBundle6); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            
            String nbBundle7 = mLoc.t("PRSR001: Indexed");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("indexed", FlatfileColumn.class, "isIndexed", null); // NOI18N
                String label = Localizer.parse(nbBundle7); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            String nbBundle8 = mLoc.t("PRSR001: Primary key");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("primaryKey", FlatfileColumn.class, "isPrimaryKey", null); // NOI18N
                String label = Localizer.parse(nbBundle8); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            String nbBundle9 = mLoc.t("PRSR001: Foreign key");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("foreignKey", FlatfileColumn.class, "isForeignKey", null); // NOI18N
                String label = Localizer.parse(nbBundle9); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }
            String nbBundle10 = mLoc.t("PRSR001: Default Value");
            try {
                PropertyDescriptor pd = new PropertyDescriptor("defaultValue", FlatfileColumn.class, "getDefaultValue", null); // NOI18N
                String label = Localizer.parse(nbBundle10); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            properties = (PropertyDescriptor[]) myProps.toArray(new PropertyDescriptor[myProps.size()]);
        }

        return properties;
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return An array of EventSetDescriptors describing the kinds of events fired by
     *         this bean. May return null if the information should be obtained by
     *         automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSets == null) {
            eventSets = new EventSetDescriptor[0];
        }

        return eventSets;
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return An array of MethodDescriptors describing the methods implemented by this
     *         bean. May return null if the information should be obtained by automatic
     *         analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        if (methods == null) {
            methods = new MethodDescriptor[0];
        }

        return methods;
    }
}