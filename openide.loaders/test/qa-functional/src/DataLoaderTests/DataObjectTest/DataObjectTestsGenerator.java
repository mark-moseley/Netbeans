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
 * DataObjectTestsGenerator.java
 *
 * Created on June 26, 2001, 11:25 AM
 */

package DataLoaderTests.DataObjectTest;

import org.openide.filesystems.Repository;


/**
 *
 * @author  jzajicek
 * @version
 */
public class DataObjectTestsGenerator {

    /** Creates new DataObjectTestsGenerator */
    public DataObjectTestsGenerator() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        org.openide.filesystems.FileObject fo = Repository.getDefault()
                .findResource(PACKAGE + "/" + DATAOBJECTS + "/" + "Backup");
        
        org.openide.loaders.DataFolder df = null;
        try{
            if ( fo.isFolder() ) {
                df = (org.openide.loaders.DataFolder) org.openide.loaders.DataFolder.find(fo);
                //System.out.println(df);
                org.openide.loaders.DataObject[] dobs = df.getChildren();
                
                //java.io.File testlist = new java.io.File(ROOT + "/DataObjectsTests.ttl");
                //testlist.createNewFile();
                //java.io.PrintWriter toTestlist = new java.io.PrintWriter(new java.io.FileOutputStream(testlist));
                
                for (int j = 0 ; j < CAT.length; j++) {
                    for (int i = 0 ; i < dobs.length ; i++) {
                        //System.out.println(dobs[i]);
                        String name = dobs[i].getPrimaryFile().getName();
                        String ext = dobs[i].getPrimaryFile().getExt();
                        
                        java.io.File d = new java.io.File(ROOT + "/" + CAT[j] + "/" + name + "/");
                        d.mkdirs();
                        
                        java.io.File f = new java.io.File(ROOT + "/" + CAT[j] + "/" + name + "/" + name + "_" + CAT[j] + ".java");
                        f.createNewFile();
                        
                        //java.io.File cfg = new java.io.File(ROOT + "/" + CAT[j] + "/" + name + "/" + name /*+ "_" + CAT[j]*/ + ".cfg");
                        //cfg.createNewFile();
                        
                        String clazz =
                        s1 + CAT[j] + "." +name + ";\n" +
                        s2 + name + "_" + CAT[j] +
                        s3 + CAT[j] + "{\n" +
                        s4 + name + "_" + CAT[j] + s4_1 +
                        s5 + name + "." + ext +
                        s6 + name + "_" + CAT[j] +
                        s7;
                        
                        java.io.PrintWriter pv = new java.io.PrintWriter(new java.io.FileOutputStream(f));
                        pv.print(clazz);
                        pv.flush();
                        pv.close();
                        
                        //java.io.PrintWriter toCfg = new java.io.PrintWriter(new java.io.FileOutputStream(cfg));
                        //toCfg.print("EXECUTE_CLASS=" + PACKAGE.replace('/','.') + "." + CAT[j] + "." + name + "." + name + "_" + CAT[j]);
                        //toCfg.flush();
                        //toCfg.close();
                        
                        //toTestlist.print(PACKAGE + "/" + CAT[j] + "/" + name + " execute_positive\n");
                    }
                }
                //toTestlist.flush();
                //toTestlist.close();
            } else {
                System.out.println("Expecting 'DataObjects' folder in this package!");
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    final static String PACKAGE = new DataObjectTestsGenerator().getClass().getPackage().getName().replace('.','/');
    
    final static String ROOT = org.openide.filesystems.FileUtil.toFile(
        Repository.getDefault().findResource(PACKAGE)).getAbsolutePath();
    
    final static String DATAOBJECTS = "data";
    final static String MANIPULATION = "manipulation";
    final static String MODIFY = "modify";
    final static String VALIDITY = "validity";
    final static String DELEGATE = "delegate";
    final static String OTHERS = "others";
    
    final static String[] CAT = new String[]{MANIPULATION,MODIFY,VALIDITY,DELEGATE,OTHERS};
    
    final static String s1 = "package DataLoaderTests.DataObjectTest.";
    final static String s2 = "import junit.framework.*;\nimport org.netbeans.junit.*;\npublic class ";
    final static String s3 = " extends DataLoaderTests.DataObjectTest.DataObjectTest_";
    final static String s4 = " public ";
    final static String s4_1 = "(java.lang.String testName){\n   super(testName);\n";
    final static String s5 = "   NAME = \"/" + DATAOBJECTS + "/";
    final static String s6 = "\";\n }\npublic static Test suite() {\n   NbTestSuite suite = new NbTestSuite(";
    final static String s7 = ".class);\n   return suite;\n }\n}";
    
}
