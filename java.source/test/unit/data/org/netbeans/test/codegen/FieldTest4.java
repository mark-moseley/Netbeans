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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.codegen;

/**
 * The class is used as a source for field test generating.
 *
 * @author  Pavel Flaska
 */
public class FieldTest4 {

    private static final int modifiersField;

    public long typeField = 10;

    public String noveJmenoField;

    String initialValueTextTester = "This text will be replaced by another one.";

    long initialValueChanger = 5 * 12 - 22;

    /** This field contains JavaDoc */
    protected String initialValueReplacer = new String("NetBeers");

    Object removeInitialValueField = new Object();

}
