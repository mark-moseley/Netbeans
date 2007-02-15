/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 * @author Alexander Simon
 */
public abstract class BaseDwarfProvider implements DiscoveryProvider {
    
    public BaseDwarfProvider() {
    }
    
    protected List<ProjectProperties> divideByLanguage(List<SourceFileProperties> sources){
        DwarfProject cProp = null;
        DwarfProject cppProp = null;
        for (SourceFileProperties source : sources) {
            ItemProperties.LanguageKind lang = source.getLanguageKind();
            DwarfProject current = null;
            if (lang == ItemProperties.LanguageKind.C){
                if (cProp == null) {
                    cProp = new DwarfProject(lang);
                }
                current = cProp;
            } else {
                if (cppProp == null) {
                    cppProp = new DwarfProject(lang);
                }
                current = cppProp;
            }
            current.update(source);
        }
        List<ProjectProperties> languages = new ArrayList<ProjectProperties>();
        if (cProp != null) {
            languages.add(cProp);
        }
        if (cppProp != null) {
            languages.add(cppProp);
        }
        return languages;
    }
    
    protected List<SourceFileProperties> getSourceFileProperties(String[] objFileName){
        HashMap<String,SourceFileProperties> map = new HashMap<String,SourceFileProperties>();
        for (String file : objFileName) {
            for(SourceFileProperties f : getSourceFileProperties(file)){
                String name = f.getItemPath();
                if (new File(name).exists()){
                    map.put(name,f);
                }
            }
        }
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        list.addAll(map.values());
        return list;
    }
    
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName){
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        try{
            Dwarf dump = new Dwarf(objFileName);
            List <CompilationUnit> units = dump.getCompilationUnits();
            if (units != null && units.size() > 0) {
                for (CompilationUnit cu : units) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        System.err.println("Compilation unit has broken name in file "+objFileName);
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        System.err.println("Compilation unit has unresolved language in file "+objFileName);
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang) ||
                        LANG.DW_LANG_C89.toString().equals(lang) ||
                        LANG.DW_LANG_C99.toString().equals(lang)) {
                        list.add(new DwarfSource(cu,false));
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        list.add(new DwarfSource(cu,true));
                    } else {
                        // Ignore other languages
                    }
                }
            } else {
                System.err.println("There are no compilation units in file "+objFileName);
            }
        } catch (WrongFileFormatException ex) {
            System.err.println("Unsuported format of file "+objFileName);
            // no trace
        } catch (IOException ex) {
            System.err.println("Exception in file "+objFileName);
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Exception in file "+objFileName);
            ex.printStackTrace();
        }
        return list;
    }
}
