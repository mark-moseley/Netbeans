/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.IOException;
import org.xml.sax.SAXException;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;


/**
 *
 * @author  vkraemer
 */
public interface ConfigParser {

    public Object parse(java.io.InputStream in) throws IOException, SAXException, DDException;
    
}
