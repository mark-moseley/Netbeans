/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * FileNameGetter.java
 *
 * Created on March 13, 2002, 4:21 PM
 */

package org.netbeans.xtest.driver;

import java.io.BufferedReader;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author lm97939
 */
public class LookForTimestamp extends Task {

    private File file;
    private String prop,name;

    public void setInstallPath(File f) {
        file = f;
    }

    public void setTimestampProperty(String p) {
        prop = p;
    }
    
    public void setTimestampFileName(String p) {
        name = p;
    }
    
    public void execute() throws BuildException {
        if (file == null) throw new BuildException("Attribute 'installPath' is empty.");
        if (prop == null) throw new BuildException("Attribute 'timestampProperty' is empty.");
        if (name == null) throw new BuildException("Attribute 'timestampFileName' is empty.");
        
        String dir = file.getParent();
        File timestampfile = new File(file.getParentFile(),name);
        if (timestampfile.exists()) {
          try {
            BufferedReader reader = new BufferedReader(new FileReader(timestampfile));
            String timestamp = reader.readLine();
            reader.close();
            getProject().setUserProperty(prop,timestamp);
          }
          catch (IOException e) {
              throw new BuildException(e);
          }
        }
    }

}
