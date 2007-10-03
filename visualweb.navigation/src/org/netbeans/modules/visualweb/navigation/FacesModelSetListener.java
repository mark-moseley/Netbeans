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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.navigation;

import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSetListener;
import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 *
 * @author joelle
 */
public class FacesModelSetListener implements ModelSetListener {

    private static Logger LOGGER = Logger.getLogger(FacesModelSetListener.class.getName());


    public FacesModelSetListener(VWPContentModel vwpContentModel) {
        setVwpContentModel(vwpContentModel);
    }

    public void modelAdded(Model model) {
        LOGGER.finest("Model Added()");
        //DO NOTHING
    }

    public void modelChanged(final Model myModel) {
        FacesModel facesModel = getVwpContentModel().getFacesModel();
        LOGGER.finest("Model Changed()");
        if ((myModel == facesModel) || (myModel.getFile().getExt().equals("jspf") && getVwpContentModel().isKnownFragementModel(facesModel, facesModel.getRootBean(), myModel))) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    getVwpContentModel().updateModel();
                }
            });
        }
    }

    public void modelRemoved(Model model) {
        LOGGER.finest("Model Removed()");
        //DO NOTHING
    }

    public void modelProjectChanged() {
        LOGGER.finest("Model Project Changed()");
        //DO NOTHING
    }
    
    private WeakReference<VWPContentModel> refVWPContentModel;

    private VWPContentModel getVwpContentModel() {
        VWPContentModel vwpContentModel = null;
        if( refVWPContentModel != null ){
            vwpContentModel = refVWPContentModel.get();
        }
        return vwpContentModel;
    }

    private void setVwpContentModel(VWPContentModel vwpContentModel) {
        refVWPContentModel = new WeakReference<VWPContentModel>(vwpContentModel);
    }
}