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

/*
 * Midlet.java
 *
 * Created on 09 November 2005, 17:17
 */
package hello;

import javax.microedition.midlet.*;

/**
 *
 * @author  lukas
 * @version
 */
public class Midlet extends MIDlet {
    public void startApp() {
        throw new RuntimeException();
    }
    
    public void pauseApp() {
        //#if DefaultConfiguration
        
        
        //#endif
        
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
