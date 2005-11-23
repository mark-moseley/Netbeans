/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Modifier;
import java.util.*;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 * This class collects various static methods for examining the layout.
 * For modifying methods see LayoutOperations class.
 *
 * @author Martin Grebac
 */

public class LayoutTestUtils implements LayoutConstants {
    
    static void writeString(List codeList, String name, String value) {
        if (value != null) {
            codeList.add("String " + name + "= \"" + value + "\";"); //NOI18N
        } else {
            codeList.add("String " + name + " = null;"); //NOI18N
        }
    }

    static void writeLayoutComponentArray(List codeList, String arrayName, String lcName) {
        codeList.add("LayoutComponent[] " + arrayName + " = new LayoutComponent[] { " + lcName + " };"); //NOI18N
    }
    
    static void writeCollection(List codeList, String name, Collection c) {
        codeList.add("Collection " + name + " = new ArrayList();"); //NOI18N
	Iterator i = c.iterator();
        while (i.hasNext()) {
            codeList.add(name + ".add(\"" + (String)i.next() + "\");"); // NOI18N
        }
    }

    static void writeStringArray(List codeList, String name, String[] compIds) {
        codeList.add("String[] " + name + " = new String[] {"); //NOI18N
        for (int i=0; i < compIds.length; i++) {
            codeList.add("\"" + compIds[i] + "\"" + (i+1 < compIds.length ? "," : "")); // NOI18N
        }
        codeList.add("};"); //NOI18N
    }

    static void writeIntArray(List codeList, String name, int[] values) {
        codeList.add("int[] " + name + " = new int[] {"); //NOI18N
        for (int i=0; i < values.length; i++) {
            codeList.add(Integer.toString(values[i]) + (i+1 < values.length ? "," : "")); // NOI18N
        }
        codeList.add("};"); //NOI18N
    }
    
    static void writeRectangleArray(List codeList, String name, Rectangle[] bounds) {
        codeList.add("Rectangle[] " + name + " = new Rectangle[] {"); //NOI18N
        for (int i=0; i < bounds.length; i++) {
            codeList.add("new Rectangle(" + bounds[i].x + ", " // NOI18N
                                                        + bounds[i].y + ", " // NOI18N
                                                        + bounds[i].width + ", " // NOI18N
                                                        + bounds[i].height + (i+1 < bounds.length ? "), " : ")")); // NOI18N
        }
        codeList.add("};"); // NOI18N
    }
    
    static void dumpTestcode(List codeList, DataObject form, int modelCounter) {

        FileWriter fw = null;
        String template = ""; //NOI18N
        
        if (form == null) return;
        try {

            FileObject primaryFile = form.getPrimaryFile();

            //Read the template for test class
            InputStream in = LayoutTestUtils.class.getResourceAsStream("/org/netbeans/modules/form/resources/LayoutModelAutoTest_template"); //NOI18N
            LineNumberReader lReader = new LineNumberReader(new InputStreamReader(in));
            while (lReader.ready()) {
                template += lReader.readLine() + "\n"; //NOI18N
            }
            lReader.close();

            //Get the code into one string
            String code = ""; //NOI18N
            Iterator i = codeList.iterator();
            while (i.hasNext()) {
                String line = (String)i.next();
                code += line + "\n"; //NOI18N
            }
	    
            //Find a name for the test file
            Resource r = JavaModel.getResource(primaryFile);
            Type type = JavaModel.getDefaultExtent().getType().resolve(r.getPackageName() + "." + primaryFile.getName()); //NOI18N
            if (type instanceof UnresolvedClass) return;
            
            String testClassName = primaryFile.getName() + "Test"; //NOI18N
            
            FileObject testFO = primaryFile.getParent().getFileObject(testClassName, "java");//NOI18N
            if (testFO == null) {
                testFO = primaryFile.getParent().createData(testClassName, "java"); //NOI18N
                
                //Rename the class in template to correct class name
                String output = Utilities.replaceString(template, "${CLASS_NAME}", testFO.getName()); //NOI18N

                //Write the file to disc
                fw = new FileWriter(FileUtil.toFile(testFO));
                fw.write(output);
                fw.close();
            }

            //8. Add the method to test class
            boolean rollback = true;
            JavaModel.getJavaRepository().beginTrans(true);
            
            try {
                JavaClass testClass = (JavaClass) resolveType("org.netbeans.modules.form.layoutdesign." + testFO.getName()); //NOI18N
                if (!(testClass instanceof UnresolvedClass)) {
                    org.netbeans.jmi.javamodel.Method m = ((JavaModelPackage)testClass.refImmediatePackage()).getMethod().createMethod();
                    m.setName("doChanges" + modelCounter);
                    m.setBodyText(code);
                    m.setType(resolveType("void"));
                    m.setModifiers(Modifier.PUBLIC);
                                        
                    testClass.getContents().add(m);
                    rollback = false;
                }
            } finally {
                JavaModel.getJavaRepository().endTrans(rollback);
            }
                        
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
    }
    
    private static Type resolveType(String typeName) {
        Type type = JavaModel.getDefaultExtent().getType().resolve(typeName);
        if (type instanceof UnresolvedClass) {
            Type basicType = JavaModel.getDefaultExtent().getType().resolve("java.lang." + typeName);  // NOI18N;
            if (!(basicType instanceof UnresolvedClass)) {
                return basicType;
            }
        }
        return type;
    }
    
    public static FileObject getTargetFolder(FileObject file) {
	FileObject targetFolder = file.getParent();
	try {
	    FileObject folder = file.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getFileObject("data/goldenfiles"); //NOI18N
	    if (folder != null) {
		targetFolder = folder;
	    }
	} catch (NullPointerException npe) {
	    // just ignore, it means the path doesn't exist
	}
	return targetFolder;
    }
    
    public static void writeTest(FormDesigner fd, FormDataObject formDO, Map idToNameMap, LayoutModel lm) {
	FileObject formFO = formDO.getFormFile();

	fd.getLayoutDesigner().dumpTestcode(formDO);

	FileWriter fw = null;
	try {
	    FileObject targetFolder = getTargetFolder(formFO);
	    FileObject fo = targetFolder.createData(formFO.getName() + "Test-ExpectedEndModel" + Integer.toString(fd.getLayoutDesigner().getModelCounter()), "txt"); //NOI18N
	    fw = new FileWriter(FileUtil.toFile(fo));
	    fw.write(lm.dump(idToNameMap));
	    StatusDisplayer.getDefault().setStatusText("The test was successfully written: " + fo.getPath()); // NOI18N
	} catch (IOException ex) {
	    ex.printStackTrace();
	    return;
	} finally {
	    try {
		if (fw != null) fw.close();
	    } catch (IOException io) {
		//TODO
	    }
	}
    }
    
}
