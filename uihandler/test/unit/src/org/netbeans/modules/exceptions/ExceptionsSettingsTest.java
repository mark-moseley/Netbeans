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

package org.netbeans.modules.exceptions;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettingsTest extends NbTestCase {
    
    public ExceptionsSettingsTest(String testName) {
        super(testName);
    }
    
    public void testUserName() {
        String str = "Moje_Jmeno";
        String previous;
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        previous = settings.getUserName();
        settings.setUserName(str);
        assertEquals(str, settings.getUserName());
        settings.setUserName(previous);
        assertEquals(previous, settings.getUserName());
    }

    public void testPasswd() {
        String str = "MY_PASSWD";
        String previous;
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        previous = settings.getPasswd();
        settings.setPasswd(str);
        assertEquals(str, settings.getPasswd());
        settings.setPasswd(previous);
        assertEquals(previous, settings.getPasswd());
    }

    public void testIsGuest() {
        ExceptionsSettings settings = new ExceptionsSettings();
        assertNotNull(settings);
        boolean previous = settings.isGuest();
        settings.setGuest(true);
        assertTrue(settings.isGuest());
        settings.setGuest(false);
        assertFalse(settings.isGuest());
        settings.setGuest(previous);
        assertEquals(previous, settings.isGuest());
    }

}
