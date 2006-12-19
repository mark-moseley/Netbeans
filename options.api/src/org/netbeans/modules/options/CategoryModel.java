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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Radek Matous
 */
public final class CategoryModel implements LookupListener {
    private static Reference<CategoryModel> INSTANCE = new WeakReference<CategoryModel>(new CategoryModel());
    private final RequestProcessor RP = new RequestProcessor();    
    private static String currentCategoryID = null;
    private String highlitedCategoryID = null;    
    private boolean categoriesValid = true;
    private final Map<String, CategoryModel.Category> id2Category = 
            Collections.synchronizedMap(new LinkedHashMap<String, CategoryModel.Category>());
    private MasterLookup masterLookup;
    private final RequestProcessor.Task masterLookupTask = RP.create(new Runnable() {
        public void run() {
            String[] categoryIDs = getCategoryIDs();
            List<Lookup> all = new ArrayList<Lookup>();
            for (int i = 0; i < categoryIDs.length; i++) {
                Category item = getCategory(categoryIDs[i]);
                Lookup lkp = item.getLookup();
                assert lkp != null;
                if (lkp != Lookup.EMPTY) {
                    all.add(lkp);
                }
            }
            getMasterLookup().setLookups(all);
        }
    },true);
    private final RequestProcessor.Task categoryTask = RP.create(new Runnable() {
        public void run() {
            Map<String, OptionsCategory> all = loadOptionsCategories();       
            Map<String, CategoryModel.Category> temp = new LinkedHashMap<String, CategoryModel.Category>();
            for (Iterator<Map.Entry<String, OptionsCategory>> it = all.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, OptionsCategory> entry = it.next();
                OptionsCategory oc = entry.getValue();
                String id = entry.getKey();
                Category cat = new Category(id, oc);
                temp.put(cat.getID(), cat);
            }
            id2Category.clear();
            id2Category.putAll(temp);
            masterLookupTask.schedule(0);
        }
    },true);
    
    private CategoryModel() {
        categoryTask.schedule(0);
    }
        
    public static CategoryModel getInstance() {
        CategoryModel retval = (CategoryModel)INSTANCE.get();
        if (retval == null) {
            retval = new CategoryModel();
            INSTANCE = new WeakReference<CategoryModel>(retval);
        }
        return retval;
    }
    
    boolean needsReinit() {
        synchronized(CategoryModel.class) {
            return !categoriesValid;
        }                
    }
    
    boolean isInitialized() {
        return categoryTask.isFinished();
    }
    
    boolean isLookupInitialized() {
        return masterLookupTask.isFinished();
    }
    
    
    void waitForInitialization() {
        categoryTask.waitFinished();
    }
        
    public String getCurrentCategoryID() {
        return verifyCategoryID(currentCategoryID);
    }
    
    public void setCurrentCategoryID(String categoryID) {
        currentCategoryID = verifyCategoryID(categoryID);
    }
    

    String getHighlitedCategoryID() {
        return verifyCategoryID(highlitedCategoryID);
    }
        
    private String verifyCategoryID(String categoryID) {
        String retval = findCurrentCategoryID(categoryID) != -1 ? categoryID : null;
        if (retval == null) {
            String[] categoryIDs = getCategoryIDs();
            if (categoryIDs.length > 0) {
                retval = categoryID = categoryIDs[0];
            }
        }
        return retval;
    }
    
    private int findCurrentCategoryID(String categoryID) {
        return categoryID == null ? -1 : Arrays.asList(getCategoryIDs()).indexOf(categoryID);
    }
    
    public String[] getCategoryIDs() {
        categoryTask.waitFinished();
        Set<String> keys = id2Category.keySet();        
        return keys.toArray(new String[keys.size()]);
    }
    
    Category getCurrent() {
        String categoryID =  getCurrentCategoryID();
        return (categoryID == null) ? null : getCategory(categoryID);
    }
    
    void setCurrent(Category item) {
        item.setCurrent();
    }

    void setHighlited(Category item) {
        item.setHighlited();
    }
            
    HelpCtx getHelpCtx() {
        final CategoryModel.Category category = getCurrent();
        return (category == null) ? null : category.getHelpCtx();
    }
    
    void update(PropertyChangeListener l, boolean force) {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.update(l, force);
        }
    }
    
    void save() {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.applyChanges();
        }
    }
    
    void cancel() {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.cancel();
        }
    }
    
    boolean dataValid() {
        boolean retval = true;
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; retval && i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            retval = item.isValid();
        }
        return retval;
    }
    
    boolean isChanged() {
        boolean retval = false;
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; !retval && i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            retval = item.isChanged();
        }
        return retval;
    }
    
    
    void setNextCategoryAsCurrent() {
        int idx =  findCurrentCategoryID(getCurrentCategoryID());
        String[] categoryIDs = getCategoryIDs();
        if (idx >= 0 && idx+1 < categoryIDs.length) {
            currentCategoryID = categoryIDs[idx+1];
        }  else {
            currentCategoryID = null;
        }
    }
    
    void setPreviousCategoryAsCurrent() {
        int idx =  findCurrentCategoryID(getCurrentCategoryID());
        String[] categoryIDs = getCategoryIDs();
        if (idx >= 0 && idx < categoryIDs.length && categoryIDs.length > 0) {
            if (idx-1 >= 0) {
                currentCategoryID = categoryIDs[idx-1];
            }  else {
                currentCategoryID = categoryIDs[categoryIDs.length-1];
            }
        } else {
            currentCategoryID = null;
        }
    }
    
    
    Category getCategory(String categoryID) {
        categoryTask.waitFinished();
        return (Category)id2Category.get(categoryID);
    }
        
    private MasterLookup getMasterLookup() {
        if (masterLookup == null) {
            masterLookup = new MasterLookup();
        }
        return masterLookup;
    }
    
    private Map<String, OptionsCategory> loadOptionsCategories() {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("OptionsDialog");// NOI18N
        if (fo != null) {
            Lookup lookup = new FolderLookup(DataFolder.findFolder(fo),null).getLookup();//NOI18N
            Lookup.Result<OptionsCategory> result = lookup.lookup(new Lookup.Template<OptionsCategory>(OptionsCategory.class));
            result.addLookupListener(this);
            Map<String, OptionsCategory> m = new LinkedHashMap<String, OptionsCategory>();
            for (Iterator<? extends Lookup.Item<OptionsCategory>> it = result.allItems().iterator(); it.hasNext();) {
                Lookup.Item<OptionsCategory> item = it.next();
                m.put(item.getId(), item.getInstance());                
            }
            return Collections.unmodifiableMap(m);
        }
        return Collections.<String, OptionsCategory>emptyMap();
    }

    public void resultChanged(LookupEvent ev) {
        synchronized(CategoryModel.class) {
            categoriesValid = false;
            INSTANCE = new WeakReference<CategoryModel>(new CategoryModel());
        }
    }
    
    final class Category  {
        private OptionsCategory category;
        private OptionsPanelController controller;
        private boolean isUpdated;
        private HelpCtx helpCtx;
        private JComponent component;
        private Lookup lookup;
        private final String id;
        
        private Category(final String id, final OptionsCategory category) {
            this.category = category;
            this.id = id;
        }
        
        boolean isCurrent() {
            return getID().equals(getCurrentCategoryID());
        }
        
        boolean isHighlited() {
            return getID().equals(getHighlitedCategoryID());
        }
                
        private void setCurrent() {
            setCurrentCategoryID(getID());
        }

        private void setHighlited() {
            highlitedCategoryID = getID();
        }
                        
        public Icon getIcon() {
            return category.getIcon();
        }

        //whatever ID representing category (dataObject name,category name, just mnemonic, ...)
        //for impl. #74855: Add an API for opening the Options dialog
        public  String getID() {
            return id;
        }
        
        public String getCategoryName() {
            return category.getCategoryName();
        }
        
        public String getTitle() {
            return category.getTitle();
        }
        
        private synchronized OptionsPanelController create() {
            if (controller == null) {
                controller = category.create();
            }
            return controller;
        }
        
        final void update(PropertyChangeListener l, boolean forceUpdate) {
            if ((!isUpdated && !forceUpdate) || (isUpdated && forceUpdate)) {
                isUpdated = true;
                getComponent();
                create().update();
                if (l != null) {
                    create().addPropertyChangeListener(l);
                }
            }
        }
        
        private void applyChanges() {
            if (isUpdated) {
                create().applyChanges();
            }
        }
        
        private void cancel() {
            if (isUpdated) {
                create().cancel();
            }
        }
        
        private boolean isValid() {
            boolean retval = true;
            if (isUpdated) {
                retval = create().isValid();
            }
            return retval;
        }
        
        private boolean isChanged() {
            boolean retval = false;
            if (isUpdated) {
                retval = create().isChanged();
            }
            return retval;
        }
        
        public JComponent getComponent() {
            if (component == null) {
                component = create().getComponent(getMasterLookup());
            }
            return component;
        }
        
        private HelpCtx getHelpCtx() {
            if (helpCtx == null && isUpdated) {
                helpCtx = create().getHelpCtx();
            }
            return helpCtx;
        }
        
        
        private Lookup getLookup() {
            if (lookup == null) {
                lookup = create().getLookup();
            }
            return lookup;
        }
    }
    
    private class MasterLookup extends ProxyLookup {
        private void setLookups(List<Lookup> lookups) {
            setLookups(lookups.toArray(new Lookup[lookups.size()]));
        }
        protected void beforeLookup(Lookup.Template template) {
            super.beforeLookup(template);
            masterLookupTask.waitFinished();
        }
    }
}
