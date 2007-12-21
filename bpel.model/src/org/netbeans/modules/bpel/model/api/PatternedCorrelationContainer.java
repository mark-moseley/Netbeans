/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.0-06/22/2005 01:29 PM(ryans)-EA2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2005.09.05 at 07:05:33 PM MSD
//
package org.netbeans.modules.bpel.model.api;

/**
 * <p>
 * Java class for tCorrelationsWithPattern complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 *   &lt;complexType name=&quot;tCorrelationsWithPattern&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;correlation&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tCorrelationWithPattern&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface PatternedCorrelationContainer extends ExtensibleElements,
        BpelContainer
{

    /**
     * @return array of correlationWithPattern children.
     */
    PatternedCorrelation[] getPatternedCorrelations();

    /**
     * @param i
     *            index
     * @return ith correlationWithPattern entity.
     */
    PatternedCorrelation getPatternedCorrelation( int i );

    /**
     * Set new array of correlationWithPattern.
     * 
     * @param correlations
     *            array for set.
     */
    void setPatternedCorrelations( PatternedCorrelation[] correlations );

    /**
     * Set correlationWithPattern <code>correlation</code> on the ith place.
     * 
     * @param correlation
     *            object for set.
     * @param i
     *            index.
     */
    void setPatternedCorrelation( PatternedCorrelation correlation, int i );

    /**
     * Insert <code>correlation</code> to the ith place.
     * 
     * @param correlation
     *            object for set.
     * @param i
     *            index.
     */
    void insertPatternedCorrelation( PatternedCorrelation correlation, int i );

    /**
     * Add <code>correlation</code>.
     * 
     * @param correlation
     *            object for add.
     */
    void addPatternedCorrelation( PatternedCorrelation correlation );

    /**
     * Removes ith correlationWithPattern.
     * 
     * @param i
     *            index.
     */
    void removePatternedCorrelation( int i );

    /**
     * @return size of correlationWithPattern children.
     */
    int sizeOfPatternedCorrelation();

}
