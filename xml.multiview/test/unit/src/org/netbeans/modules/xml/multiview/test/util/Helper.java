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
package org.netbeans.modules.xml.multiview.test.util;

import java.io.File;
//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.test.BookDataObject;
import org.netbeans.modules.xml.multiview.test.bookmodel.Chapter;

public class Helper {

    public static File getBookFile(File dataDir) {
        String result = dataDir.getAbsolutePath() + "/projects/webapp/web/WEB-INF/sample.book";
        return new File(result);
    }
    
    public static javax.swing.JTextField getChapterTitleTF(BookDataObject dObj, Chapter chapter) 
        throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        
        java.lang.reflect.Method meth  = XmlMultiViewDataObject.class.getDeclaredMethod("getActiveMultiViewElement", new Class[]{});
        meth.setAccessible(true);
        ToolBarMultiViewElement mvEl = (ToolBarMultiViewElement)meth.invoke(dObj, new Object[]{});
        meth.setAccessible(false);
        if (mvEl==null) return null;
        javax.swing.JPanel sectionPanel = mvEl.getSectionView().findSectionPanel(chapter).getInnerPanel();
        if (sectionPanel==null) return null;
        java.awt.Component[] children = sectionPanel.getComponents();
        for (int i=0;i<children.length;i++) {
            if (children[i] instanceof javax.swing.JTextField) {
                return (javax.swing.JTextField)children[i];
            }
        }
        return  null;
    }
    
}
