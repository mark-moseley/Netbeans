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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package twopagecrudtable;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.Calendar;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.TextField;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.convert.IntegerConverter;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class CreatePage extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        triptypeDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.triptypeRowSet}"));
        tripDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.tripRowSet}"));
    }
    private Calendar dateCalendar = new Calendar();

    public Calendar getDateCalendar() {
        return dateCalendar;
    }

    public void setDateCalendar(Calendar c) {
        this.dateCalendar = c;
    }
    private TextField fromCity = new TextField();

    public TextField getFromCity() {
        return fromCity;
    }

    public void setFromCity(TextField tf) {
        this.fromCity = tf;
    }
    private TextField toCity = new TextField();

    public TextField getToCity() {
        return toCity;
    }

    public void setToCity(TextField tf) {
        this.toCity = tf;
    }
    private DropDown tripType = new DropDown();

    public DropDown getTripType() {
        return tripType;
    }

    public void setTripType(DropDown dd) {
        this.tripType = dd;
    }
    private CachedRowSetDataProvider triptypeDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTriptypeDataProvider() {
        return triptypeDataProvider;
    }

    public void setTriptypeDataProvider(CachedRowSetDataProvider crsdp) {
        this.triptypeDataProvider = crsdp;
    }
    private IntegerConverter tripTypeConverter = new IntegerConverter();

    public IntegerConverter getTripTypeConverter() {
        return tripTypeConverter;
    }

    public void setTripTypeConverter(IntegerConverter ic) {
        this.tripTypeConverter = ic;
    }
    private CachedRowSetDataProvider tripDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTripDataProvider() {
        return tripDataProvider;
    }

    public void setTripDataProvider(CachedRowSetDataProvider crsdp) {
        this.tripDataProvider = crsdp;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public CreatePage() {
    }

    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    @Override
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here
            
        // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("CreatePage Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    @Override
    public void preprocess() {
    }

    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    @Override
    public void prerender() {
    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    @Override
    public void destroy() {
        triptypeDataProvider.close();
        tripDataProvider.close();
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

    public String addButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        if ( tripDataProvider.canAppendRow() ) {
            try {              
                RowKey rowKey = tripDataProvider.appendRow();
                tripDataProvider.setCursorRow(rowKey);
                Integer tid = nextPK();
                tripDataProvider.setValue("TRIP.TRIPID", rowKey, tid);
                Integer pid = getSessionBean1().getCurrentPersonId();
                tripDataProvider.setValue("TRIP.PERSONID", rowKey, pid); 
                java.util.Date depDate = (java.util.Date) dateCalendar.getValue();
                if ( depDate != null ) {
                    java.sql.Date date = new java.sql.Date(depDate.getTime());
                    tripDataProvider.setValue("TRIP.DEPDATE", rowKey, date);
                }
                tripDataProvider.setValue("TRIP.DEPCITY", rowKey, fromCity.getValue());
                tripDataProvider.setValue("TRIP.DESTCITY", rowKey, toCity.getValue());
                tripDataProvider.setValue("TRIP.TRIPTYPEID", rowKey, tripType.getSelected());
                tripDataProvider.commitChanges();                      
            } catch (Exception e) {
                error("Cannot append new trip: " + e);
            }                
        } else {
                error("Cannot append a new row");
        }    
        return "created";
    }

    public String cancel_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        return "canceled";
    }

    // Generate an integer Primary Key
    // Return next primary key for PERSON table
    private Integer nextPK() throws java.sql.SQLException {
        // create a new rowset
	com.sun.sql.rowset.CachedRowSetXImpl pkRowSet = new com.sun.sql.rowset.CachedRowSetXImpl();
	try {
            // set the rowset to use the Travel database
            pkRowSet.setDataSourceName("java:comp/env/jdbc/TRAVEL_ApacheDerby");
            // find the highest person id and add one to it
            pkRowSet.setCommand("SELECT MAX(TRAVEL.TRIP.TRIPID) + 1 FROM TRAVEL.TRIP");
            pkRowSet.setTableName("TRAVEL.TRIP");
            // execute the rowset -- which will contain a single row and single column
            pkRowSet.execute();    
            pkRowSet.next();
            // get the key
            int counter = pkRowSet.getInt(1);
            return new Integer(counter);
	} catch (Exception e) {
            error("Error fetching Max(TRIPID)+1 : " + e.getMessage());
        } finally {
            pkRowSet.close();
	}
        return null;
}
}

