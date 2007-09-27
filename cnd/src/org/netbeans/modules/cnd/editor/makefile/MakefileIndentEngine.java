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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.makefile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.editor.FormatterIndentEngine;

import org.openide.util.HelpCtx;

/**
 * Makefile indentation engine
 */

public class MakefileIndentEngine extends FormatterIndentEngine {
    
    public static final String MAKEFILE_TYPE = "MakefileType"; // NOI18N
    
    // Makefile type isn't implemented yet
//    public static final String SOLARIS_MAKEFILE_TYPE = "SolarisMakefileType";
//    public static final String GNU_MAKEFILE_TYPE = "GNUMakefileType";
//    
//    private String type = GNU_MAKEFILE_TYPE;
    
    private final static long serialVersionUID = -5085934337015783530L;

    public MakefileIndentEngine() {
        setAcceptedMimeTypes(new String[] { MIMENames.MAKEFILE_MIME_TYPE });
	setExpandTabs(false); // This should be the default for Makefilesd
	setSpacesPerTab(8);
    }
    
    protected ExtFormatter createFormatter() {
        return new MakefileFormatter(MakefileKit.class);
    }
    
    // Makefile type isn't implemented yet
//    public String getMakefileType() {
//        return type;
//    }
//    
//    public void setMakefileType(String type) {
//        this.type = type;
//    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_indent_makefile"); // NOI18N // FIXUP
    }
    
    // Serialization
    
    private static final ObjectStreamField[] serialPersistenFields = {
        new ObjectStreamField(MAKEFILE_TYPE, String.class)
    };
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
//        ObjectInputStream.GetField fields = ois.readFields();
//        setMakefileType((String) fields.get(MAKEFILE_TYPE, (Object) getMakefileType()));
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
//        ObjectOutputStream.PutField fields = oos.putFields();
//        fields.put(MAKEFILE_TYPE, getMakefileType());
//        oos.writeFields();
    }
}

