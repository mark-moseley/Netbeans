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
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestAction extends TestNode {

    /** Creates new TestAction */
    public TestAction(String name) {
        super(name);
    }

    public TestAction(Element node) {
        super(node);
    }

    public Element toXML(Element node) {
        return super.toXML(node);
    }

    public boolean isParent() {
        return false;
    }
    
    protected void registerCookies() {
        getCookieSet().put(PerformCookie.class,new PerformCookie() {
            public void perform() {
                TestAction.this.perform();
            }
            
            public boolean isPerforming() {
                return TestAction.this.isPerforming;
            }
        });
    }
}