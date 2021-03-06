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
 * Abstract BeanInfo implementation to expose read-only access to selected table
 * properties.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class FlatfileTableBeanInfo extends SimpleBeanInfo {

    private static PropertyDescriptor[] properties = null;
    private static EventSetDescriptor[] eventSets = null;
    private static MethodDescriptor[] methods = null;
    private static transient final Logger mLogger = LogUtil.getLogger(FlatfileTableBeanInfo.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable properties of this bean. May return
     *         null if the information should be obtained by automatic analysis.
     */
    public abstract BeanDescriptor getBeanDescriptor();

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

            try {
                PropertyDescriptor pd = new PropertyDescriptor("tableName", FlatfileTable.class, "getTableName", null); // NOI18N
                String nbBundle1 = mLoc.t("PRSR001: Table name");
                String label = Localizer.parse(nbBundle1); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileType", FlatfileTable.class, "getFileType", null); // NOI18N
                String nbBundle2 = mLoc.t("PRSR001: File type");
                String label = Localizer.parse(nbBundle2); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("description", FlatfileTable.class, "getDescription", null); // NOI18N
                String nbBundle3 = mLoc.t("PRSR001: Description");
                String label = Localizer.parse(nbBundle3); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileName", FlatfileTable.class, "getFileName", null); // NOI18N
                String nbBundle4 = mLoc.t("PRSR001: File name");
                String label = Localizer.parse(nbBundle4);// NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("encodingScheme", FlatfileTable.class, "getEncodingScheme", null); // NOI18N
                String nbBundle5 = mLoc.t("PRSR001: Encoding scheme");
                String label = Localizer.parse(nbBundle5); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("rowsToSkip", FlatfileTable.class, "getRowsToSkip", null); // NOI18N
                String nbBundle6 = mLoc.t("PRSR001: Initial rows to skip");
                String label = Localizer.parse(nbBundle6); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("maxFaults", FlatfileTable.class, "getMaxFaults", null); // NOI18N
                String nbBundle7 = mLoc.t("PRSR001: Maximum # faults to tolerate");
                String label = Localizer.parse(nbBundle7); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("isFirstLineHeader", FlatfileTable.class, "isFirstLineHeader", null); // NOI18N
                String nbBundle8 = mLoc.t("PRSR001: First line is header");
                String label = Localizer.parse(nbBundle8); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("recordDelimiter", FlatfileTable.class, "getRecordDelimiter", null); // NOI18N
                String nbBundle9 = mLoc.t("PRSR001: Record delimiter");
                String label = Localizer.parse(nbBundle9); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("trimWhiteSpace",
                        FlatfileTable.class, "enableWhiteSpaceTrimming", null); // NOI18N
                String nbBundle10 = mLoc.t("PRSR001: Trim white space for columns");
                String label = Localizer.parse(nbBundle10); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
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
