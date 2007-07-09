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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import java.util.List;
import org.apache.tools.ant.BuildException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;

/**
 *
 * @author Vitaly Bychkov
 * @author Sreenivasan Genipudi
 */
public class JBIGenerator extends AbstractJBIGenerator {
    
    private Logger logger = Logger.getLogger(JBIGenerator.class.getName());    
    private List<File> myDependentProjectDirs;
    private List<File> mySourceDirs;
    
    public JBIGenerator(List<File> depedentProjectDirs , List<File> sourceDirs, String srcDir, String buildDir) {
        super(srcDir, buildDir);
        myDependentProjectDirs = depedentProjectDirs;
        System.out.println("myDependentProjectDirs: "+myDependentProjectDirs);
        if (myDependentProjectDirs != null) {
            int i =0;
            for (File file : myDependentProjectDirs) {
                i++;
                if (file == null) {
                    System.out.println(i+"] file is null");
                } else {
                    System.out.println(i+"] file "+file.getPath());
                }
            }

        }

        mySourceDirs = sourceDirs;
        if (mySourceDirs != null) {
            int i =0;
            for (File file : mySourceDirs) {
                i++;
                if (file == null) {
                    System.out.println(i+"] source  file is null");
                } else {
                    System.out.println(i+"] source file "+file.getPath());
                }
            }

        }
    }
    
    public List<File> getDepedentProjectDirs() {
        return myDependentProjectDirs;
    }
    
    public List<File> getSourceDirs() {
        return mySourceDirs;
    }

    protected TMapModel getTMapModel() {
        File transformmapFile = getTransformmapFile();
        if (transformmapFile == null) {
            logger.log(Level.SEVERE, "Error encountered while processing transformmap file - "+transformmapFile.getAbsolutePath());
            throw new RuntimeException("Can't find transformation descriptor");
        }
        TMapModel tMapModel = null;
        try {
            tMapModel = TransformmapCatalogModel.getDefault().getTMapModel(transformmapFile.toURI());
        }catch (Exception ex) {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Tramsformap Model ", ex);
            throw new RuntimeException("Error while creating Transformmap Model ",ex);
        }
        
        if (tMapModel == null 
                || !TMapModel.State.VALID.equals(tMapModel.getState())) 
        {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Transformmap Model - "+(tMapModel == null ? " is null" : " is not valid"));
            throw new RuntimeException("Error while creating Transformmap Model ");
        }
        return tMapModel;
    }
    
    protected <T extends ReferenceableWSDLComponent> T resolveReference(NamedComponentReference<T> ref) {
        if (ref == null) {
            return null;
        }
        T target = null;
        
        
        return target;
    }
}
