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

/*
 * Page1.java
 *
 * Created on Aug 3, 2007, 11:02:03 AM
 * by jb144761
 *
 */
package singlepagecrudtable;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.Calendar;
import com.sun.webui.jsf.component.Checkbox;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.model.SingleSelectOptionsList;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class Page1 extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        tripDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.tripRowSet}"));
        personDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.personRowSet}"));
        triptypeDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.triptypeRowSet}"));
    }

    private Page page1 = new Page();
    
    public Page getPage1() {
        return page1;
    }
    
    public void setPage1(Page p) {
        this.page1 = p;
    }
    
    private Html html1 = new Html();
    
    public Html getHtml1() {
        return html1;
    }
    
    public void setHtml1(Html h) {
        this.html1 = h;
    }
    
    private Head head1 = new Head();
    
    public Head getHead1() {
        return head1;
    }
    
    public void setHead1(Head h) {
        this.head1 = h;
    }
    
    private Link link1 = new Link();
    
    public Link getLink1() {
        return link1;
    }
    
    public void setLink1(Link l) {
        this.link1 = l;
    }
    
    private Body body1 = new Body();
    
    public Body getBody1() {
        return body1;
    }
    
    public void setBody1(Body b) {
        this.body1 = b;
    }
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }
    private HtmlPanelGrid mainPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(HtmlPanelGrid hpg) {
        this.mainPanel = hpg;
    }
    private HtmlPanelGrid personInfoPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getPersonInfoPanel() {
        return personInfoPanel;
    }

    public void setPersonInfoPanel(HtmlPanelGrid hpg) {
        this.personInfoPanel = hpg;
    }
    private HtmlPanelGrid tripsPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getTripsPanel() {
        return tripsPanel;
    }

    public void setTripsPanel(HtmlPanelGrid hpg) {
        this.tripsPanel = hpg;
    }
    private HtmlPanelGrid addTripParentPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getAddTripParentPanel() {
        return addTripParentPanel;
    }

    public void setAddTripParentPanel(HtmlPanelGrid hpg) {
        this.addTripParentPanel = hpg;
    }
    private HtmlPanelGrid headerPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getHeaderPanel() {
        return headerPanel;
    }

    public void setHeaderPanel(HtmlPanelGrid hpg) {
        this.headerPanel = hpg;
    }
    private HtmlPanelGrid newTripInfoPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getNewTripInfoPanel() {
        return newTripInfoPanel;
    }

    public void setNewTripInfoPanel(HtmlPanelGrid hpg) {
        this.newTripInfoPanel = hpg;
    }
    private HtmlPanelGrid footerPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getFooterPanel() {
        return footerPanel;
    }

    public void setFooterPanel(HtmlPanelGrid hpg) {
        this.footerPanel = hpg;
    }
    private Table trips = new Table();

    public Table getTrips() {
        return trips;
    }

    public void setTrips(Table t) {
        this.trips = t;
    }
    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }
    private HtmlPanelGrid buttonsPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getButtonsPanel() {
        return buttonsPanel;
    }

    public void setButtonsPanel(HtmlPanelGrid hpg) {
        this.buttonsPanel = hpg;
    }
    private Button updateButton = new Button();

    public Button getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(Button b) {
        this.updateButton = b;
    }
    private Button deleteButton = new Button();

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button b) {
        this.deleteButton = b;
    }
    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }
    private DropDown personId = new DropDown();

    public DropDown getPersonId() {
        return personId;
    }

    public void setPersonId(DropDown dd) {
        this.personId = dd;
    }
    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }
    private StaticText jobTitle = new StaticText();

    public StaticText getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(StaticText st) {
        this.jobTitle = st;
    }
    private Label label3 = new Label();

    public Label getLabel3() {
        return label3;
    }

    public void setLabel3(Label l) {
        this.label3 = l;
    }
    private Message message1 = new Message();

    public Message getMessage1() {
        return message1;
    }

    public void setMessage1(Message m) {
        this.message1 = m;
    }
    private StaticText staticText4 = new StaticText();

    public StaticText getStaticText4() {
        return staticText4;
    }

    public void setStaticText4(StaticText st) {
        this.staticText4 = st;
    }
    private Label label4 = new Label();

    public Label getLabel4() {
        return label4;
    }

    public void setLabel4(Label l) {
        this.label4 = l;
    }
    private Calendar departureDate = new Calendar();

    public Calendar getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Calendar c) {
        this.departureDate = c;
    }
    private Message message2 = new Message();

    public Message getMessage2() {
        return message2;
    }

    public void setMessage2(Message m) {
        this.message2 = m;
    }
    private Label label5 = new Label();

    public Label getLabel5() {
        return label5;
    }

    public void setLabel5(Label l) {
        this.label5 = l;
    }
    private DropDown tripType = new DropDown();

    public DropDown getTripType() {
        return tripType;
    }

    public void setTripType(DropDown dd) {
        this.tripType = dd;
    }
    private Message message3 = new Message();

    public Message getMessage3() {
        return message3;
    }

    public void setMessage3(Message m) {
        this.message3 = m;
    }
    private Label label6 = new Label();

    public Label getLabel6() {
        return label6;
    }

    public void setLabel6(Label l) {
        this.label6 = l;
    }
    private TextField departureCity = new TextField();

    public TextField getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(TextField tf) {
        this.departureCity = tf;
    }
    private Label label7 = new Label();

    public Label getLabel7() {
        return label7;
    }

    public void setLabel7(Label l) {
        this.label7 = l;
    }
    private TextField destinationCity = new TextField();

    public TextField getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(TextField tf) {
        this.destinationCity = tf;
    }
    private Message message4 = new Message();

    public Message getMessage4() {
        return message4;
    }

    public void setMessage4(Message m) {
        this.message4 = m;
    }
    private StaticText staticText5 = new StaticText();

    public StaticText getStaticText5() {
        return staticText5;
    }

    public void setStaticText5(StaticText st) {
        this.staticText5 = st;
    }
    private Message message5 = new Message();

    public Message getMessage5() {
        return message5;
    }

    public void setMessage5(Message m) {
        this.message5 = m;
    }
    private Button addButton = new Button();

    public Button getAddButton() {
        return addButton;
    }

    public void setAddButton(Button b) {
        this.addButton = b;
    }
    private CachedRowSetDataProvider tripDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTripDataProvider() {
        return tripDataProvider;
    }

    public void setTripDataProvider(CachedRowSetDataProvider crsdp) {
        this.tripDataProvider = crsdp;
    }
    private TableColumn tableColumn3 = new TableColumn();

    public TableColumn getTableColumn3() {
        return tableColumn3;
    }

    public void setTableColumn3(TableColumn tc) {
        this.tableColumn3 = tc;
    }
    private TableColumn tableColumn4 = new TableColumn();

    public TableColumn getTableColumn4() {
        return tableColumn4;
    }

    public void setTableColumn4(TableColumn tc) {
        this.tableColumn4 = tc;
    }
    private TableColumn tableColumn5 = new TableColumn();

    public TableColumn getTableColumn5() {
        return tableColumn5;
    }

    public void setTableColumn5(TableColumn tc) {
        this.tableColumn5 = tc;
    }
    private TableColumn tableColumn6 = new TableColumn();

    public TableColumn getTableColumn6() {
        return tableColumn6;
    }

    public void setTableColumn6(TableColumn tc) {
        this.tableColumn6 = tc;
    }
    private TableColumn tableColumn8 = new TableColumn();

    public TableColumn getTableColumn8() {
        return tableColumn8;
    }

    public void setTableColumn8(TableColumn tc) {
        this.tableColumn8 = tc;
    }
    private Checkbox selectedTripCheckbox = new Checkbox();

    public Checkbox getSelectedTripCheckbox() {
        return selectedTripCheckbox;
    }

    public void setSelectedTripCheckbox(Checkbox c) {
        this.selectedTripCheckbox = c;
    }
    private TextField tripsDepartureDate = new TextField();

    public TextField getTripsDepartureDate() {
        return tripsDepartureDate;
    }

    public void setTripsDepartureDate(TextField tf) {
        this.tripsDepartureDate = tf;
    }
    private TextField tripsDestinationCity = new TextField();

    public TextField getTripsDestinationCity() {
        return tripsDestinationCity;
    }

    public void setTripsDestinationCity(TextField tf) {
        this.tripsDestinationCity = tf;
    }
    private DropDown tripsTripType = new DropDown();

    public DropDown getTripsTripType() {
        return tripsTripType;
    }

    public void setTripsTripType(DropDown dd) {
        this.tripsTripType = dd;
    }
    private TextField tripsDepartureCity = new TextField();

    public TextField getTripsDepartureCity() {
        return tripsDepartureCity;
    }

    public void setTripsDepartureCity(TextField tf) {
        this.tripsDepartureCity = tf;
    }
    private Message message6 = new Message();

    public Message getMessage6() {
        return message6;
    }

    public void setMessage6(Message m) {
        this.message6 = m;
    }
    private Message message7 = new Message();

    public Message getMessage7() {
        return message7;
    }

    public void setMessage7(Message m) {
        this.message7 = m;
    }
    private Message message8 = new Message();

    public Message getMessage8() {
        return message8;
    }

    public void setMessage8(Message m) {
        this.message8 = m;
    }
    private Message message9 = new Message();

    public Message getMessage9() {
        return message9;
    }

    public void setMessage9(Message m) {
        this.message9 = m;
    }
    private CachedRowSetDataProvider personDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getPersonDataProvider() {
        return personDataProvider;
    }

    public void setPersonDataProvider(CachedRowSetDataProvider crsdp) {
        this.personDataProvider = crsdp;
    }
    private IntegerConverter personIdConverter = new IntegerConverter();

    public IntegerConverter getPersonIdConverter() {
        return personIdConverter;
    }

    public void setPersonIdConverter(IntegerConverter ic) {
        this.personIdConverter = ic;
    }
    private CachedRowSetDataProvider triptypeDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTriptypeDataProvider() {
        return triptypeDataProvider;
    }

    public void setTriptypeDataProvider(CachedRowSetDataProvider crsdp) {
        this.triptypeDataProvider = crsdp;
    }
    private IntegerConverter tripsTripTypeConverter = new IntegerConverter();

    public IntegerConverter getTripsTripTypeConverter() {
        return tripsTripTypeConverter;
    }

    public void setTripsTripTypeConverter(IntegerConverter ic) {
        this.tripsTripTypeConverter = ic;
    }
    private IntegerConverter tripTypeConverter = new IntegerConverter();

    public IntegerConverter getTripTypeConverter() {
        return tripTypeConverter;
    }

    public void setTripTypeConverter(IntegerConverter ic) {
        this.tripTypeConverter = ic;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public Page1() {
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
            log("Page1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
        // Restore the current person
        RowKey personRowKey = getSessionBean1().getCurrentPersonRowKey();
        Integer pid = (Integer) personDataProvider.getValue("PERSON.PERSONID");
        if (personRowKey == null) {
            personDataProvider.cursorFirst();
            personRowKey = personDataProvider.getCursorRow();
            try {
                getSessionBean1().getTripRowSet().setObject(1, pid);
                getSessionBean1().getTripRowSet().execute();
                tripDataProvider.cursorFirst();
            } catch (Exception ex) {
                log("Error Description", ex);
            }
        } else {
            personDataProvider.setCursorRow(personRowKey);
        }
        personId.setSelected(pid);
    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
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
    public void destroy() {
        getSessionBean1().setCurrentPersonRowKey(personDataProvider.getCursorRow());
        tripDataProvider.close();
        personDataProvider.close();
        triptypeDataProvider.close();
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

    public void personId_processValueChange(ValueChangeEvent event) {
        Integer newPersonId = (Integer) personId.getSelected();
        // Find the datatable's row that matches the dropdown's
        // selected person
        RowKey rowKey = personDataProvider.findFirst("PERSON.PERSONID", newPersonId);
        // just in case someone deleted row after dropdown was populated
        if (rowKey != null) {
            try {
                // change cursor position
                personDataProvider.setCursorRow(rowKey);
                getSessionBean1().getTripRowSet().setObject(1, newPersonId);
                getSessionBean1().getTripRowSet().execute();
                tripDataProvider.cursorFirst();
            } catch (TableCursorVetoException tcve) {
                error("Cannot change to personId " + newPersonId);
            } catch (SQLException sqle) {
                error("Problem with trip table");
            }
        } else {
            // exceptional event we might want to know about
            log("Missing person for personId " + newPersonId);
            error("Missing person for personId " + newPersonId);
        }
        form1.discardSubmittedValues("saveChanges");
    }

    public String updateButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        // case name where null will return to the same page.
        try {
            tripDataProvider.commitChanges();         
        } catch (Exception e) {
            error("Cannot commit updates: " + e);
        }  
        return null;
    }

   /* The set of RowKeys for rows that have been selected.
    * This set is manipulated by calls to setSelectedTrip(),
    * which will occur when the checkbox of each row is decoded.
    */
    private Set<RowKey> selectedTrips = new HashSet<RowKey>();
    
    public String deleteButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        Iterator rowKeys = selectedTrips.iterator();        
        while (rowKeys.hasNext()) {         
            RowKey rowKey = (RowKey) rowKeys.next();
            System.out.println("rowKey: " + rowKey);
            try {
                tripDataProvider.removeRow(rowKey);
            } catch (Exception e) {
                error("Cannot delete trip with row key " + rowKey);
            }
        } 
        // do outside loop because commitChanges() invalidates rowkeys
        try {
            tripDataProvider.commitChanges();
        } catch (Exception ex) {
            error("Cannot commit deletions.");
        }
        return null;
    }
    
  /**
   * Returns true if the trip for the current row is selected.
   */ 
   public boolean isSelectedTrip() {
       TableRowDataProvider trdp = (TableRowDataProvider) getBean("currentRow");
       if (trdp == null) {
           return false;
       }
       RowKey rowKey = trdp.getTableRow();       
       return selectedTrips.contains(rowKey);
    }
    
   /**
    * Records whether or not the current trip should be marked as selected,
    * based on the state of the checkbox.
    */
   public void setSelectedTrip(boolean b) {
       TableRowDataProvider trdp = (TableRowDataProvider) getBean("currentRow");
       RowKey rowKey = trdp.getTableRow();        
       if (selectedTripCheckbox.isChecked()) {
           selectedTrips.add(rowKey);
       } else {
           selectedTrips.remove(rowKey);
       }
    }

    public String addButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        if ( tripDataProvider.canAppendRow() ) {
            try {
                RowKey rowKey = tripDataProvider.appendRow();
                tripDataProvider.setCursorRow(rowKey);
                tripDataProvider.setValue("TRIP.TRIPID", rowKey,  nextPK());
                tripDataProvider.setValue("TRIP.PERSONID", rowKey, personId.getSelected()); 
                java.util.Date depDate = (java.util.Date) departureDate.getValue();
                if ( depDate != null ) {
                    java.sql.Date date = new java.sql.Date( depDate.getTime() );
                    tripDataProvider.setValue("TRIP.DEPDATE", rowKey, date);
                } else {
                    tripDataProvider.setValue("TRIP.DEPDATE", rowKey, null);
                }
                tripDataProvider.setValue("TRIP.DEPCITY", rowKey, departureCity.getValue());
                tripDataProvider.setValue("TRIP.DESTCITY", rowKey, destinationCity.getValue());
                tripDataProvider.setValue("TRIP.TRIPTYPEID", rowKey, tripType.getSelected());
                tripDataProvider.commitChanges();
            } catch (Exception e) {
                error("Cannot append new trip: " + e);
            }                
        } else {
                error("Cannot append a new row");
        }    
        //clear fields
        departureDate.setValue(null);
        departureCity.setText(null);
        destinationCity.setText(null);
        tripType.setForgetValue(true);
        form1.discardSubmittedValues("saveChanges");
        return null;
    }
    // Generate an integer Primary Key
    // Return next primary key for TRAVEL.TRIP table
    private Integer nextPK() throws SQLException {
        // create a new rowset
	CachedRowSetXImpl pkRowSet = new CachedRowSetXImpl();
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
            error("Error fetching Max(TRAVEL.TRIP.TRIPID) + 1 : " + e.getMessage());
        } finally {
            pkRowSet.close();
	}
        return null;
    }
    
    private void containsNoDigits(FacesContext fc, UIComponent uic, Object value, String errorMessage) {
        String name = (String) value;
        for (int i=0; i<name.length(); i++){
            if (Character.isDigit(name.charAt(i)) ) {
                ((UIInput) uic).setValid(false);
                throw new ValidatorException(new FacesMessage(errorMessage));            
            }
        }
    }

    public void departureCity_validate(FacesContext context, UIComponent component, Object value) {
        containsNoDigits(context, component, value, "City names cannot contain digits.");
    }
}

