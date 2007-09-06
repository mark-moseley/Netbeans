/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
var customersObj;

//function to show all customers
function showCustomers() {
    var app = new CustomerDB();
    var resources = app.getResources();
    for(i=0;i<resources.length;i++) {
        var resource = resources[i];
        if(resource instanceof Customers) {
            customersObj = resource;
            var customers = customersObj.getItems();
            var headers = new Array();
            headers[0] = 'ID';
            headers[1] = 'Name';
            headers[2] = 'Email';
            headers[3] = 'Address';
            headers[4] = 'Action';
            var node = document.getElementById('vw_pl_content');
            node.innerHTML = createCustomersTable(headers, customers) ;
            doShowContent('vw_pl');
        }
    }   
}

//function to show a customer
function showCustomer(index, editable) {
    var customer = customersObj.getItems()[index]; 
    var str = createField('id', 'Id: ', customer.getCustomerId(), false);
    str += createField('name_', 'Name: ', customer.getName(), editable);
    str += createField('email', 'Email: ', customer.getEmail(), editable);
    str += createField('line1', 'Address Line1: ', customer.getAddressline1(), editable);
    str += createField('line2', 'Address Line2: ', customer.getAddressline2(), editable);
    str += createField('city', 'City: ', customer.getCity(), editable);
    str += createField('state', 'State: ', customer.getState(), editable);
    str += createField('zip', 'Zip: ', customer.getZip(), editable);
    str += createField('phone', 'Phone: ', customer.getPhone(), editable);
    str += createField('fax', 'Fax: ', customer.getFax(), editable);
    str += createField('discount', 'Discount Code: ', customer.getDiscountCode().getDiscountCode(), false);
    str += createField('rate', 'Discount Rate: ', customer.getDiscountCode().getRate(), false);
    str += createField('limit', 'Credit Limit: ', customer.getCreditLimit(), editable);    
    var node = document.getElementById('vw_pl_item_content');
    node.innerHTML = createForm(index, str, editable);;
    doShowContent('vw_pl_item');
}


//function to edit a customer
function updateCustomer(index) {
    var customer = customersObj.getItems()[index];
    var f = document.form1;
    var id = customer.getCustomerId();
    customer.setName(f.name_.value);
    customer.setEmail(f.email.value);
    customer.setAddressline1(f.line1.value);
    customer.setAddressline2(f.line2.value);
    customer.setCity(f.city.value);
    customer.setState(f.state.value);
    customer.setZip(f.zip.value);
    customer.setPhone(f.phone.value);
    customer.setFax(f.fax.value);
    //customer.setDiscountCode(f.discount);
    customer.setCreditLimit(f.limit.value);
    var status = customer.flush();
    if(status)
        alert('Update succeeded for customer with Id: \''+id+'\'.');
    else
        alert('Update failed for customer with Id: \''+id+'\'.');
}

//function to create a customer
function createCustomer() {
    var uri = customersObj.getUri();
    var id = 1000+customersObj.getItems().length;
    var customer = new Customer(uri+id+'/', true);
    var f = document.form1;
    customer.setCustomerId(id);
    customer.setName(f.name_.value);
    customer.setEmail(f.email.value);
    customer.setAddressline1(f.line1.value);
    customer.setAddressline2(f.line2.value);
    customer.setCity(f.city.value);
    customer.setState(f.state.value);
    customer.setZip(f.zip.value);
    customer.setPhone(f.phone.value);
    customer.setFax(f.fax.value);
    var codes = getDiscountCodes();
    var code = codes.getItems()[0];
    customer.setDiscountCode(code);
    customer.setCreditLimit(f.limit.value);
    customersObj.addItem(customer);
    var status = customersObj.flush(customer);
    if(status) {
        alert('Create succeeded for customer with Id: \''+id+'\'. Redirecting to "View Customers" page.');
        showCustomers();
    } else {
        alert('Create failed for customer with Id: \''+id+'\'.');
    }
}

//function to delete a customer
function deleteCustomer(index) {
    var customer = customersObj.getItems()[index];
    var id = customer.getCustomerId();
    var status = customer.delete_();
    if(status) {
        alert('Delete succeeded for customer with Id: \''+id+'\'. Redirecting to "View Customers" page.');
        showCustomers();
    } else {
        alert('Delete failed for customer with Id: \''+id+'\'.');
    }
}

//Helper functions

//function to get DiscountCodes
function getDiscountCodes() {
    var app = new CustomerDB();
    var resources = app.getResources();
    for(i=0;i<resources.length;i++) {
        var resource = resources[i];
        if(resource instanceof DiscountCodes) {
            return resource;
        }
    }  
    return null; 
}

//function to create new customer form
function createNewCustomerForm() {
    var editable = true;
    var str = createField('name_', 'Name: ', '', editable);
    str += createField('email', 'Email: ', '', editable);
    str += createField('line1', 'Address Line1: ', '', editable);
    str += createField('line2', 'Address Line2: ', '', editable);
    str += createField('city', 'City: ', '', editable);
    str += createField('state', 'State: ', '', editable);
    str += createField('zip', 'Zip: ', '', editable);
    str += createField('phone', 'Phone: ', '', editable);
    str += createField('fax', 'Fax: ', '', editable);
    str += createField('limit', 'Credit Limit: ', '', editable);
    var node = document.getElementById('vw_pl_item_content');
    node.innerHTML = createForm(-1, str, editable);
    doShowContent('vw_pl_item');
}

function createCustomersTable(headers, customers) {
    var style = 'otab';    
    var str = '<div><table class="result"><tr>';
    str += '<td class="tab '+style+'"><a class="links" href="javascript:createNewCustomerForm()"><span class="text2">Add</span></a></td>';
    str += '</tr></table></div>';    
    str += '<table class="result" border="1">';
    for(i=0;i<headers.length;i++) {
        str += '<th>'+headers[i]+'</th>';
    }
    for(i=0;i<customers.length;i++) {
        str += createCustomersRow(customers[i], i);
    }    
    str += '</table>';
    return str;
}

function createCustomersRow(customer, index) {
    var id = customer.getCustomerId();
    var name = customer.getName();
    var email = customer.getEmail();
    var address = customer.getAddressline1()+'<br/>'+
        customer.getAddressline2()+'<br/>'+customer.getCity()+', '+customer.getState()+'-'+customer.getZip();
    var str = '<tr><td><a href="javascript:showCustomer('+index+', false)" >'+id+'</a></td>';
    str += '<td>'+name+'</td>';
    str += '<td>'+email+'</td>';
    str += '<td>'+address+'</td>';
    str += '<td><input value="Delete" type="Submit" onclick="javascipt:deleteCustomer('+index+')"/></td></tr>';
    return str;
}

function createForm(index, content, editable) {
    var style = 'otab';
    var str = '<table class="result"><tr>';
    if(index == -1) {
        str += '<td class="tab '+style+'"><a class="links" href="javascript:createCustomer()"><span class="text2">Create</span></a></td>';
    } else {
        str += '<td class="tab '+style+'"><a class="links" href="javascript:deleteCustomer('+index+')"><span class="text2">Delete</span></a></td>';    
        if(editable)
            str += '<td class="tab '+style+'"><a class="links" href="javascript:updateCustomer('+index+')"><span class="text2">Update</span></a></td>';
        else
            str += '<td class="tab '+style+'"><a class="links" href="javascript:showCustomer('+index+', true)"><span class="text2">Edit</span></a></td>';
    }
    str += '</tr></table></div>';    
    str += '<form action="" method="" name="form1"><table class="result" border="1">';
    str += content;
    str += '</table></form>';
    str += '<a class="links" href="javascript:showCustomers();"><span class="text">&lt;Go Back</span></a>';    
    return str;
}

function createField(id, name, value, editable) {
    var str = '<tr><td>'+name+'</td>';
    if(editable)
        str += '<td><input id="'+id+'" type="text" value="'+value+'" size="40"/></td></tr>';
    else
        str += '<td>'+value+'&nbsp;</td></tr>';
    return str;
}

function doShowContent(id) {
    var nodes = new Array();
    nodes[0] = 'vw_pl';
    nodes[1] = 'cr_pl';
    nodes[2] = 'vw_pl_item';
    for(i=0;i<nodes.length;i++)
    {
        doHideContent(nodes[i]);
    }
    var node = document.getElementById(id).style;
    node.display="block";
}

function doHideContent(id) {
    document.getElementById(id).style.display="none";
}
