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

package vehicleincidentreportapplication;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.PasswordField;
import com.sun.webui.jsf.component.TextField;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.ValidatorException;
import javax.sql.rowset.CachedRowSet;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class NewUser extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        passwordLengthValidator.setMaximum(10);
        passwordLengthValidator.setMinimum(6);
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
    private HtmlPanelGrid content = new HtmlPanelGrid();

    public HtmlPanelGrid getContent() {
        return content;
    }

    public void setContent(HtmlPanelGrid hpg) {
        this.content = hpg;
    }
    private HtmlPanelGrid dataGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getDataGrid() {
        return dataGrid;
    }

    public void setDataGrid(HtmlPanelGrid hpg) {
        this.dataGrid = hpg;
    }
    private HtmlPanelGrid paddingPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getPaddingPanel() {
        return paddingPanel;
    }

    public void setPaddingPanel(HtmlPanelGrid hpg) {
        this.paddingPanel = hpg;
    }
    private HtmlPanelGrid messageGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getMessageGrid() {
        return messageGrid;
    }

    public void setMessageGrid(HtmlPanelGrid hpg) {
        this.messageGrid = hpg;
    }
    private HtmlPanelGrid contentGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getContentGrid() {
        return contentGrid;
    }

    public void setContentGrid(HtmlPanelGrid hpg) {
        this.contentGrid = hpg;
    }
    private MessageGroup messageGroup1 = new MessageGroup();

    public MessageGroup getMessageGroup1() {
        return messageGroup1;
    }

    public void setMessageGroup1(MessageGroup mg) {
        this.messageGroup1 = mg;
    }
    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }
    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }
    private Label label3 = new Label();

    public Label getLabel3() {
        return label3;
    }

    public void setLabel3(Label l) {
        this.label3 = l;
    }
    private Label label4 = new Label();

    public Label getLabel4() {
        return label4;
    }

    public void setLabel4(Label l) {
        this.label4 = l;
    }
    private Label label5 = new Label();

    public Label getLabel5() {
        return label5;
    }

    public void setLabel5(Label l) {
        this.label5 = l;
    }
    private Label label6 = new Label();

    public Label getLabel6() {
        return label6;
    }

    public void setLabel6(Label l) {
        this.label6 = l;
    }
    private Message message1 = new Message();

    public Message getMessage1() {
        return message1;
    }

    public void setMessage1(Message m) {
        this.message1 = m;
    }
    private Message message2 = new Message();

    public Message getMessage2() {
        return message2;
    }

    public void setMessage2(Message m) {
        this.message2 = m;
    }
    private Message message3 = new Message();

    public Message getMessage3() {
        return message3;
    }

    public void setMessage3(Message m) {
        this.message3 = m;
    }
    private Message message4 = new Message();

    public Message getMessage4() {
        return message4;
    }

    public void setMessage4(Message m) {
        this.message4 = m;
    }
    private Message message5 = new Message();

    public Message getMessage5() {
        return message5;
    }

    public void setMessage5(Message m) {
        this.message5 = m;
    }
    private Message message6 = new Message();

    public Message getMessage6() {
        return message6;
    }

    public void setMessage6(Message m) {
        this.message6 = m;
    }
    private TextField userId = new TextField();

    public TextField getUserId() {
        return userId;
    }

    public void setUserId(TextField tf) {
        this.userId = tf;
    }
    private TextField firstName = new TextField();

    public TextField getFirstName() {
        return firstName;
    }

    public void setFirstName(TextField tf) {
        this.firstName = tf;
    }
    private TextField lastName = new TextField();

    public TextField getLastName() {
        return lastName;
    }

    public void setLastName(TextField tf) {
        this.lastName = tf;
    }
    private TextField emailAddress = new TextField();

    public TextField getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(TextField tf) {
        this.emailAddress = tf;
    }
    private PasswordField password = new PasswordField();

    public PasswordField getPassword() {
        return password;
    }

    public void setPassword(PasswordField pf) {
        this.password = pf;
    }
    private PasswordField retypePassword = new PasswordField();

    public PasswordField getRetypePassword() {
        return retypePassword;
    }

    public void setRetypePassword(PasswordField pf) {
        this.retypePassword = pf;
    }
    private Button register = new Button();

    public Button getRegister() {
        return register;
    }

    public void setRegister(Button b) {
        this.register = b;
    }
    private LengthValidator passwordLengthValidator = new LengthValidator();

    public LengthValidator getPasswordLengthValidator() {
        return passwordLengthValidator;
    }

    public void setPasswordLengthValidator(LengthValidator lv) {
        this.passwordLengthValidator = lv;
    }
    private IntegerConverter integerConverter1 = new IntegerConverter();

    public IntegerConverter getIntegerConverter1() {
        return integerConverter1;
    }

    public void setIntegerConverter1(IntegerConverter ic) {
        this.integerConverter1 = ic;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public NewUser() {
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
            log("NewUser Initialization Failure", e);
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
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
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
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    public String register_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        //String userid = (String) getUseridfield().getText();
        Integer theUserId = (Integer) this.userId.getValue();
        
        // check if the user with same id exists
        CachedRowSet passwordRowSet = new CachedRowSetXImpl();
        try {
            passwordRowSet.setDataSourceName("java:comp/env/jdbc/VIR_ApacheDerby");
            passwordRowSet.setTableName("PASSWORD");
            passwordRowSet.setCommand("SELECT * FROM VIR.PASSWORD WHERE VIR.PASSWORD.ID=" + theUserId.intValue());
            passwordRowSet.execute();
            if (passwordRowSet.next()) {
                error("Choose a different user id. That used id is already in use.");
                return null;
            }
        } catch (Exception ex) {
            log(ex.getMessage(), ex);
            error(ex.getMessage());
            return null;
        }
        
        String theFirstName = (String) this.firstName.getText();
        String theLastName = (String) this.lastName.getText();
        String theEmailAddress = (String) this.emailAddress.getText();
        String thePassword = (String) this.password.getText();
        String theRetypePassword = (String) this.retypePassword.getText();
        if ( ! thePassword.equals(theRetypePassword) ) {
            error("Passwords must match one another.");
            return null;
        }
        // add the user
        try {
            CachedRowSet employeeRowSet = new CachedRowSetXImpl();
            employeeRowSet.setDataSourceName("java:comp/env/jdbc/VIR_ApacheDerby");
            employeeRowSet.setTableName("EMPLOYEE");
            employeeRowSet.setCommand("SELECT * FROM VIR.EMPLOYEE");            
            CachedRowSetDataProvider employeeDataProvider = new CachedRowSetDataProvider();
            employeeDataProvider.setCachedRowSet(employeeRowSet);
            RowKey rowKey = employeeDataProvider.appendRow();
            employeeDataProvider.setValue("employee.id", rowKey, theUserId);
            employeeDataProvider.setValue("employee.firstname", rowKey, theFirstName);
            employeeDataProvider.setValue("employee.lastname", rowKey, theLastName);
            employeeDataProvider.setValue("employee.email", rowKey, theEmailAddress);
            
            employeeDataProvider.commitChanges();            
            employeeDataProvider.getCachedRowSet().release();
            employeeDataProvider.refresh();

            passwordRowSet.setDataSourceName("java:comp/env/jdbc/VIR_ApacheDerby");
            passwordRowSet.setTableName("PASSWORD");
            CachedRowSetDataProvider passwordDataProvider = new CachedRowSetDataProvider();
            passwordDataProvider.setCachedRowSet(passwordRowSet);
            rowKey = passwordDataProvider.appendRow();
            passwordDataProvider.setValue("password.id", rowKey, theUserId);
            passwordDataProvider.setValue("password.password", rowKey, thePassword);
            passwordDataProvider.commitChanges();
            passwordDataProvider.getCachedRowSet().release();
            passwordDataProvider.refresh();            
        } catch (Exception ex) {
            log(ex.getMessage(), ex);
            error(ex.getMessage());
            return null;
        }
        return "login";
    }

    public void emailAddress_validate(FacesContext context, UIComponent component, Object value) {
        String email = (String) value;
        if ( email.indexOf("@") == -1 || email.startsWith("@") || email.endsWith("@") ) {
            throw new ValidatorException(new FacesMessage("Enter a valid email address."));
        }
    }
}

