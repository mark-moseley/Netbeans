/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries;

import java.net.*;
import org.xml.sax.*;

/**
 * Decodes plain text values to typed objects.
 *
 * @author Petr Kuzel
 */
public class LibraryDeclarationConvertorImpl implements LibraryDeclarationConvertor {
   
    
    public URL parseResource(final String data) throws SAXException {
        try {
            if (data == null) {
                return null;
            }
            else {
                return new URL (data);
            }
        } catch (MalformedURLException ex) {
            throw new SAXException("Invalid resource URI: " + data.trim() + ")", ex);  // NOI18N
        }
    }    
}

