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

package org.netbeans.modules.bpel.model.api;

/**
 * <p>
 * Java class for tActivityContainer complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tActivityContainer&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;group ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}activity&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 * 
 * @author ads
 */
public interface ActivityHolder extends ExtensibleElements, BpelContainer {

    /**
     * @return child activity in this holder.
     */
    ExtendableActivity getActivity();

    /**
     * Set <code>activity</code> as child in this holder.
     * 
     * @param activity
     *            object for set.
     */
    void setActivity( ExtendableActivity activity );

    /**
     * Removes child activity from this holder.
     */
    void removeActivity();

}
