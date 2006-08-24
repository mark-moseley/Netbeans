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
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.xml.sax.*;


/**
 *
 * @author dlm198383
 */
public abstract class WSMultiViewDataObject extends XmlMultiViewDataObject{
    protected WSDesignView designView;
    protected ModelSynchronizer modelSynchronizer;
    protected boolean changedFromUI;
    protected DDXmi ddBaseBean;
    private static final long serialVersionUID = 76675745399723L;
    public static final String DD_MULTIVIEW_POSTFIX = "_multiview_design";
    public static final String MULTIVIEW_WEBBND = "webbnd";
    public static final String MULTIVIEW_WEBEXT = "webext";
    public static final String MULTIVIEW_APPBND = "appbnd";
    public static final String MULTIVIEW_APPEXT = "appext";
    public static final String MULTIVIEW_EJBBND = "ejbbnd";
    public static final String MULTIVIEW_EJBEXT = "ejbext";
    /**
     * Creates a new instance of WSMultiViewDataObject
     */
    public WSMultiViewDataObject(FileObject pf, MultiFileLoader loader)  throws DataObjectExistsException, IOException {
        super(pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        try {
            parseDocument();
        } catch (IOException ex) {
            System.out.println("ex="+ex);
        }
    }
    protected String getPrefixMark() {
        return null;
    }
    
    /**
     *
     * @throws IOException
     */
    protected java.io.InputStream getInputStream() {
        return getDataCache().createInputStream();
    }
    
    
    
    
    protected void parseDocument() throws IOException {
        if(ddBaseBean==null) {
            ddBaseBean=getDD();
        } else {
            try {
                SAXParseException error = DDUtils.parse(new InputSource(getDataCache().createReader()));
                setSaxError(error);
                
                DDXmi bb = createDDXmiFromDataCache();
                
                if (bb!=null) {
                    ddBaseBean.merge(bb, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
                }
            } catch (SAXException ex) {
                setSaxError(ex);
            }
        }
    }
    
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();
    
    public WSDesignView getDesignView() {
        return designView;
    }
    
    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }
    public boolean isChangedFromUI() {
        return changedFromUI;
    }
    
    public void setChangedFromUI(boolean changedFromUI) {
        this.changedFromUI=changedFromUI;
    }
    protected abstract class WSDesignView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 71111745399723L;
        protected WSDesignView(WSMultiViewDataObject dObj) {
            super(dObj, "Design");
        }
        public abstract MultiViewElement createElement();
        public abstract java.awt.Image getIcon();
        public abstract String preferredID() ;
        
    }
    public abstract DDXmi getDD() throws java.io.IOException;
    
    protected abstract DDXmi createDDXmiFromDataCache() ;
    
    protected class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        
        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 500);
        }
        
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }
        public void updateData(org.openide.filesystems.FileLock dataLock, boolean modify) {
            super.updateData(dataLock, modify);
            try {
                parseDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
        
        
        protected void updateDataFromModel(Object model, org.openide.filesystems.FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((DDXmi) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (Schema2BeansException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        protected Object getModel() {
            try {
                return getDD();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }
        
        protected void reloadModelFromData() {
            try {
                parseDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        //protected void updateDataFromModel(Object object, org.openide.filesystems.FileLock fileLock, boolean b) {
        //}
    }
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return modelSynchronizer;
    }
    
    /** Enable to get active MultiViewElement object
     */
    
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }
    
    
}
