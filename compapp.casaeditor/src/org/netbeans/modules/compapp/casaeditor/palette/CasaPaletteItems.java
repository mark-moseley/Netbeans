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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.util.*;

import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.TemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplateGroup;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class CasaPaletteItems extends Index.ArrayChildren {
   
    private CasaPaletteCategory category;
    private Lookup mLookup;
    
   
    public CasaPaletteItems(CasaPaletteCategory Category, Lookup lookup) {
        this.category = Category;
        mLookup = lookup;
    }

    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.WSDL_BINDINGS)) {
            addExternalWsdlPoints(childrenNodes);
        }
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS)) {
            addServiceUnits(childrenNodes); 
        }
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS)) {
            addInternalEndPoints(childrenNodes);
        }
        return childrenNodes;
    }

    private HashMap getWsdlTemplates() {
        ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        HashMap temps = new HashMap();
        for (TemplateGroup group : groups) {
            ltg = factory.getLocalizedTemplateGroup(group);
            protocols.add(ltg);
            temps.put(ltg.getName(), ltg);
        }

        return temps;
    }
    
    private void addExternalWsdlPoints(ArrayList childrenNodes) {
        HashMap bcTemplates = getWsdlTemplates();
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String biName = bi.getBindingName().toUpperCase();
                if (bcTemplates.get(biName) != null) {
                    CasaPaletteItem item = new CasaPaletteItem();
                    item.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.WSDL_BINDINGS);
                    item.setTitle(bi.getBindingName());
                    item.setComponentName(bi.getBcName());
                    childrenNodes.add( new CasaPaletteItemNode(
                            item,
                            bi.getIcon().getFile(),
                            mLookup,
                            true) );
                }
            }
        }
    }
    
    private void addInternalEndPoints(ArrayList childrenNodes) {
        CasaPaletteItem consumeItem = new CasaPaletteItem();
        consumeItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS);
        consumeItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.CONSUME);
        consumeItem.setTitle(getMessage("Palette_Consume_Title"));      // NOI18N
        childrenNodes.add( new CasaPaletteItemNode( 
                consumeItem, 
                "org/netbeans/modules/compapp/casaeditor/palette/resources/consumesPalette.png", // NOI18N
                mLookup) );
        
        CasaPaletteItem provideItem = new CasaPaletteItem();
        provideItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS);
        provideItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.PROVIDE);
        provideItem.setTitle(getMessage("Palette_Provide_Title"));  // NOI18N
        childrenNodes.add( new CasaPaletteItemNode( 
                provideItem, 
                "org/netbeans/modules/compapp/casaeditor/palette/resources/providesPalette.png", // NOI18N
                mLookup) ); 
    }

    private void addServiceUnits(ArrayList childrenNodes) {
//Don't add Int. SU / Jbi Module. Add it when its supported.
        /*
        CasaPaletteItem intsuItem = new CasaPaletteItem();
        intsuItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS);
        intsuItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.INT_SU);
        intsuItem.setTitle(getMessage("Palette_IntSU_Title"));      // NOI18N

        childrenNodes.add( new CasaPaletteItemNode( 
                intsuItem, 
                "org/netbeans/modules/compapp/casaeditor/palette/resources/intsu.png",  // NOI18N
                mLookup) );
 */

        CasaPaletteItem extsuItem = new CasaPaletteItem();
        extsuItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS);
        extsuItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.EXT_SU);
        extsuItem.setTitle(getMessage("Palette_ExtSU_Title"));  // NOI18N
        childrenNodes.add( new CasaPaletteItemNode( 
                extsuItem, 
                "org/netbeans/modules/compapp/casaeditor/palette/resources/extsu.png", // NOI18N
                mLookup) );   
    }

    private String getMessage(String key) {
        return NbBundle.getBundle(CasaPaletteItems.class).getString(key); 
    }
}
