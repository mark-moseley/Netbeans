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

package org.apache.jasper.compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.JarFile;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.Compiler;
import org.openide.ErrorManager;

/**
 *
 * @author Petr Jiricka
 */
public class ParserControllerProxy {

    private JspCompilationContext ctxt;
    private Compiler compiler;
    private ParserController pc;
    
    boolean isXml;
    String sourceEnc;
    
    private static Method getJarFileM;
    private static Method resolveFileNameM;
    private static Method getJspConfigPageEncodingM;
    private static Method determineSyntaxAndEncodingM;
    private static Field isXmlF;
    private static Field sourceEncF;
    
    static {
        initMethodsAndFields();
    }
    
    /** Creates a new instance of ParserControllerProxy */
    public ParserControllerProxy(JspCompilationContext ctxt, Compiler compiler) {
        this.ctxt = ctxt; 
	this.compiler = compiler;
        pc = new ParserController(ctxt, compiler);
    }
    
    public static void initMethodsAndFields() {
        try {
            // getJarFile method
            getJarFileM = ParserController.class.getDeclaredMethod("getJarFile", new Class[] {URL.class});
            getJarFileM.setAccessible(true);
            // resolveFileName method
            resolveFileNameM = ParserController.class.getDeclaredMethod("resolveFileName", new Class[] {String.class});
            resolveFileNameM.setAccessible(true);
            // getJspConfigPageEncoding method
            getJspConfigPageEncodingM = ParserController.class.getDeclaredMethod("getJspConfigPageEncoding", new Class[] {String.class});
            getJspConfigPageEncodingM.setAccessible(true);
            // determineSyntaxAndEncoding method
            determineSyntaxAndEncodingM = ParserController.class.getDeclaredMethod("determineSyntaxAndEncoding", new Class[] 
                {String.class, JarFile.class, String.class});
            determineSyntaxAndEncodingM.setAccessible(true);
            // isXML field
            isXmlF = ParserController.class.getDeclaredField("isXml");
            isXmlF.setAccessible(true);
            // sourceEnc field
            sourceEncF = ParserController.class.getDeclaredField("sourceEnc");
            sourceEncF.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (NoSuchFieldException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public void extractSyntaxAndEncoding(String inFileName)
	throws FileNotFoundException, JasperException, IOException {
	// If we're parsing a packaged tag file or a resource included by it
	// (using an include directive), ctxt.getTagFileJar() returns the 
	// JAR file from which to read the tag file or included resource,
	// respectively.
	extractSyntaxAndEncoding(inFileName, ctxt.getTagFileJarUrl());
    }
    
    private void extractSyntaxAndEncoding(String inFileName, URL jarFileUrl) 
        throws FileNotFoundException, JasperException, IOException {
        try {
            JarFile jarFile = (JarFile)getJarFileM.invoke(pc, new Object[] {jarFileUrl});
            String absFileName = (String)resolveFileNameM.invoke(pc, new Object[] {inFileName});
            String jspConfigPageEnc = (String)getJspConfigPageEncodingM.invoke(pc, new Object[] {absFileName});

            // Figure out what type of JSP document and encoding type we are
            // dealing with
            determineSyntaxAndEncodingM.invoke(pc, new Object[] {absFileName, jarFile, jspConfigPageEnc});
            
            // now the isXml and sourceEnc variables of ParserController have values
            isXml = ((Boolean)isXmlF.get(pc)).booleanValue();
            sourceEnc = (String)sourceEncF.get(pc);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new JasperException(e);
        }
        catch (InvocationTargetException e) {
            Throwable r = e.getTargetException();
            if (r instanceof RuntimeException) {
                throw (RuntimeException)r;
            }
            if (r instanceof FileNotFoundException) {
                throw (FileNotFoundException)r;
            }
            if (r instanceof JasperException) {
                throw (JasperException)r;
            }
            if (r instanceof IOException) {
                throw (IOException)r;
            }
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new JasperException(e);
        }
    }
    
}
