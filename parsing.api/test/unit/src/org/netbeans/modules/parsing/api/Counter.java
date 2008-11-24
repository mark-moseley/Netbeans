/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.api;

class Counter {

    private int count = 1;
    private int maxCount;
    private String errorMessage = null;

    public Counter (int maxCount) {
        this.maxCount = maxCount;
    }
    
    public void check (int c) {
        check (c, false);
    }

    public void wait (int c) {
        check (c, true);
    }
    
    public synchronized void check (final int c, final boolean wait) {
        while (true) {
            if (errorMessage != null) {
                return;
            }
            if (c == maxCount) {
                notifyAll ();
                return;
            }
            if (wait && count < c)
                try {
                    wait ();
                    continue;
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            if (c != count) {
                errorMessage = "expected " + c + ", but was " + count;
                notifyAll ();
                return;
            }
            count ++;
            notifyAll ();
            return;
        }
    }

    public synchronized void check (String expected, String current) {
        if (errorMessage != null)
            return;
        if (!expected.equals (current)) {
            errorMessage = "expected " + expected + ", but was " + current;
            notify ();
            return;
        }
    }

    public synchronized String errorMessage (boolean wait) throws InterruptedException {
        while (true) {
            if (errorMessage != null) 
                return errorMessage;
            if (count == maxCount) return null;
            if (wait)
                wait ();
            else
                return errorMessage;
        }
    }

    public int count () {
        return count;
    }
}



