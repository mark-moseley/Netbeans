/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.http.ui.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.tools.common.dbgp.HttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpProgress;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpRequest;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpResponse;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class HttpActivitiesModel implements TreeModel, TableModel, NodeModel, NodeActionsProvider {
    
    private final List<ModelListener> listeners;
    public final static String METHOD_COLUMN = "METHOD_COLUMN";
    public final static String SENT_COLUMN = "SENT_COLUMN";
    public final static String RESPONSE_COLUMN = "RESPONSE_COLUMN";
    

    private static final String HTTP_RESPONSE=
            "org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/GreenArrow"; // NOI18N
//    private final static String ROOT = "Root";
    private NbJSDebugger debugger;

    public HttpActivitiesModel(NbJSDebugger debugger) {
        listeners = new CopyOnWriteArrayList<ModelListener>();
        this.debugger = debugger;
        debugger.addJSHttpMessageEventListener(new JSHttpMesageEventListenerImpl());
    }


    private class JSHttpMesageEventListenerImpl implements JSHttpMessageEventListener {
        Map<String, HttpActivity> id2ActivityMap = new HashMap<String,HttpActivity>();
        public void onHttpMessageEvent(JSHttpMessageEvent jsHttpMessageEvent) {
            JSHttpMessage message = jsHttpMessageEvent.getHttpMessage();
            if( message instanceof JSHttpRequest ){
                HttpActivity activity = new HttpActivity((JSHttpRequest)message);
                id2ActivityMap.put(message.getId(), activity);
                activityList.add(activity);
            } else {
                HttpActivity activity = id2ActivityMap.get(message.getId());
                if (activity != null ){
                    if( message instanceof JSHttpResponse) {
                        activity.setResponse((JSHttpResponse) message);
                    } else if ( message instanceof JSHttpProgress ){
                        activity.updateProgress((JSHttpProgress)message);
                    }
                } else {
                    //Why is the activity null.. maybe we started listening to late a missed a request.
                    return;
                }
            }
            fireModelChange();
        }

    }

    final List<HttpActivity> activityList = new LinkedList<HttpActivity>();
    public List<HttpActivity> getHttpActivities() {
        return activityList;
    }
    


    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if ( ROOT.equals(node)){
            return getHttpActivities();
        }
        if( node instanceof HttpActivity ){
            HttpActivity activity = (HttpActivity)node;
            
            if ( METHOD_COLUMN.equals(columnID)){
                return activity.getRequest().getMethod();
            } else if ( SENT_COLUMN.equals(columnID) ) {
                return activity.getRequest().getTimeStamp();
            } else if ( RESPONSE_COLUMN.equals(columnID) ){
                JSHttpMessage response = activity.getResponse();
                if( response != null ){
                    return response.getTimeStamp();
                } 
                return "";
            }
            throw new UnknownTypeException("Column type not recognized: " + columnID);
                
        }
        throw new UnknownTypeException("Type not recognized:" + node);
    }

    public Object[] getChildren(Object parent, int from, int to) {
        if( ROOT.equals(parent) ){
            return getHttpActivities().toArray();
        }
        return new Object[0];
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return getHttpActivities().size();
        }
        return 0;
    }

    public Object getRoot() {
        return ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return false;
        }
        return true;
    }
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    private void fireModelChange( ){
        for( ModelListener l : listeners ){
            l.modelChanged(new TreeChanged(this));
        }
    }
    

    public String getDisplayName(Object node) throws UnknownTypeException {
        if ( ROOT.equals(node)){
            return NbBundle.getMessage(HttpActivitiesModel.class, "URL_COLUMN");
        }
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            String displayName = activity.getRequest().toString();  //url
            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return null;
        } else {
            return HTTP_RESPONSE;
        }
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            String displayName = activity.getRequest().toString();
            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        return new Action[]{};
    }
    
    private static final MethodColumn methodColumn = new MethodColumn();
    private static final SentColumn sentColumn = new SentColumn();
    private static final ResponseColumn resColumn = new ResponseColumn();
    
    public static ColumnModel getColumnModel(String columnID){
        if( METHOD_COLUMN.equals(columnID)){
            return methodColumn;
        } else if ( SENT_COLUMN.equals(columnID)){
            return sentColumn;
        } else if ( RESPONSE_COLUMN.equals(columnID)){
            return resColumn;
        }
        return null;
    }
    
    private static final class MethodColumn extends ColumnModel {

        @Override
        public String getID() {
            return METHOD_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, METHOD_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }
    
    private static final class SentColumn extends ColumnModel {

        @Override
        public String getID() {
            return SENT_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, SENT_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }
    
    private static final class ResponseColumn extends ColumnModel {

        @Override
        public String getID() {
            return RESPONSE_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, RESPONSE_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }




}
