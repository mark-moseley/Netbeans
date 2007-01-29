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
package org.netbeans.modules.visualweb.xhtml;

/**
 * JavaBean for the "f:loadBundle" tag
 *
 * @author Carl Quinn
 */
public class F_LoadBundle {

    /**
     * Creates a <code>F_LoadBundle</code>.
     */
    public F_LoadBundle() {
    }

    private String basename;

    /**
     * Gets the String property <b>basename</b>.
     *
     * @return String
     */
    public String getBasename() {
        return basename;
    }

    /**
     * Sets the String property <b>basename</b>.
     *
     * @param basename
     */
    public void setBasename(String basename) {
        this.basename = basename;
    }

    private String var;

    /**
     * Gets the String property <b>var</b>.
     *
     * @return String
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets the String property <b>var</b>.
     *
     * @param basename
     */
    public void setVar(String var) {
        this.var = var;
    }
}
