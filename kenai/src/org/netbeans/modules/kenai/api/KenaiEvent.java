/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.util.EventObject;

/**
 * Kenai instance fires events of following types:
 * <pre>
 * LOGIN
 * PROJECT_OPEN
 * PROJECT_CLOSE
 * </pre>
 * @see #getType()
 * @see Kenai#addKenaiListener(org.netbeans.modules.kenai.api.KenaiListener)
 * @see Kenai#removeKenaiListener(org.netbeans.modules.kenai.api.KenaiListener)
 * @see Kenai#login(java.lang.String, char[])
 * @see Kenai#logout()
 * @see KenaiProject#open()
 * @see KenaiProject#clone()
 * @see Kenai#getOpenProjects()
 * @author Jan Becicka
 */
 public final class KenaiEvent extends EventObject {

     /**
      * PasswordAuthentication getSource() logged in
      */
     public static final int LOGIN = 0;
     /**
      * Project getSource() was open
      */
     public static final int PROJECT_OPEN = 1;

     /**
      * Project getSource() was closed
      */
     public static final int PROJECT_CLOSE = 3;

     /**
      * Project getSource() was refreshed from server
      */
     public static final int PROJECT_CHANGED = 4;
     private int type;

     /**
      * if type is LOGIN, than getSource returns instance of new
      * PasswordAuthentication or null, if user logged out.
      * If type is PROJCT_OPEN/PROJECT_CLOSE/PROJECT_CHANGED than getResource return instance of
      * KenaiProject being closed/open
      * @return type of event
      */
     public int getType() {
         return type;
     }

     KenaiEvent(Object source, int type) {
         super(source);
         this.type = type;
     }
}
