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

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.LazyTreeLoader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.java.source.usages.SymbolDumper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class TreeLoader extends LazyTreeLoader {

    public static void preRegister(final Context context, final ClasspathInfo cpInfo) {
        context.put(lazyTreeLoaderKey, new TreeLoader(context, cpInfo));
    }
    
    private Context context;
    private ClasspathInfo cpInfo;

    private TreeLoader(Context context, ClasspathInfo cpInfo) {
        this.context = context;
        this.cpInfo = cpInfo;
    }
    
    @Override
    public boolean loadTreeFor(final ClassSymbol clazz) {
        if (clazz != null) {
            try {
                FileObject fo = SourceUtils.getFile(clazz, cpInfo);                
                JavacTaskImpl jti = context.get(JavacTaskImpl.class);
                if (fo != null && jti != null) {
                    Log.instance(context).nerrors = 0;
                    JavaFileObject jfo = FileObjects.nbFileObject(fo, null);
                    try {
                        jti.analyze(jti.enter(jti.parse(jfo)));
                        dumpSymFile(clazz);
                        return true;                        
                    } catch (CouplingAbort ca) {
                        RepositoryUpdater.couplingAbort(ca, jfo);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    private void dumpSymFile(ClassSymbol clazz) throws IOException {
        PrintWriter writer = null;
        try {
            JavaFileManager fm = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);
            String binaryName = null;
            if (clazz.classfile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.PLATFORM_CLASS_PATH, clazz.classfile);
                if (binaryName == null)
                    binaryName = fm.inferBinaryName(StandardLocation.CLASS_PATH, clazz.classfile);                
            }
            else if (clazz.sourcefile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.SOURCE_PATH, clazz.sourcefile);
            }
            if (binaryName == null) {
                return;
            }
            String surl = clazz.classfile.toUri().toURL().toExternalForm();
            int index = surl.lastIndexOf(FileObjects.convertPackage2Folder(binaryName));
            assert index > 0;
            File classes = Index.getClassFolder(new URL(surl.substring(0, index)));
            String pkg, name;
            index = binaryName.lastIndexOf('.');
            if (index < 0) {
                pkg = null;
                name = binaryName;
            } else {
                pkg = binaryName.substring(0, index);
                assert binaryName.length() > index;
                name = binaryName.substring(index + 1);
            }
            if (pkg != null) {
                classes = new File(classes, pkg.replace('.', File.separatorChar));
                if (!classes.exists())
                    classes.mkdirs();
            }
            File outputFile = new File(classes, name + '.' + FileObjects.SIG);
            if (outputFile.exists())
                return ;//no point in dumping again already existing sig file
            writer = new PrintWriter(outputFile, "UTF-8");
            Symbol owner;
            if (clazz.owner.kind == Kinds.PCK) {
                owner = null;
            }
            else if (clazz.owner.kind == Kinds.VAR) {
                owner = clazz.owner.owner;
            }
            else {
                owner = clazz.owner;
            }
            SymbolDumper.dump(writer, Types.instance(context), clazz, owner);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
