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

function Customer(uri_) {
    this.Customer(uri_, false);
}

function Customer(uri_, initialized_) {
    this.uri = uri_;
    this.customerId = '';
    this.zip = '';
    this.name = '';
    this.addressline1 = '';
    this.addressline2 = '';
    this.city = '';
    this.state = '';
    this.phone = '';
    this.fax = '';
    this.email = '';
    this.creditLimit = '';
    this.discountCodeRef = '';

    this.initialized = initialized_;
}

Customer.prototype = {

   getUri : function() {
      return this.uri;
   },

   getCustomerId : function() {
      if(!this.initialized)
         this.init();
      return this.customerId;
   },

   setCustomerId : function(customerId_) {
      this.customerId = customerId_;
   },

   getZip : function() {
      if(!this.initialized)
         this.init();
      return this.zip;
   },

   setZip : function(zip_) {
      this.zip = zip_;
   },

   getName : function() {
      if(!this.initialized)
         this.init();
      return this.name;
   },

   setName : function(name_) {
      this.name = name_;
   },

   getAddressline1 : function() {
      if(!this.initialized)
         this.init();
      return this.addressline1;
   },

   setAddressline1 : function(addressline1_) {
      this.addressline1 = addressline1_;
   },

   getAddressline2 : function() {
      if(!this.initialized)
         this.init();
      return this.addressline2;
   },

   setAddressline2 : function(addressline2_) {
      this.addressline2 = addressline2_;
   },

   getCity : function() {
      if(!this.initialized)
         this.init();
      return this.city;
   },

   setCity : function(city_) {
      this.city = city_;
   },

   getState : function() {
      if(!this.initialized)
         this.init();
      return this.state;
   },

   setState : function(state_) {
      this.state = state_;
   },

   getPhone : function() {
      if(!this.initialized)
         this.init();
      return this.phone;
   },

   setPhone : function(phone_) {
      this.phone = phone_;
   },

   getFax : function() {
      if(!this.initialized)
         this.init();
      return this.fax;
   },

   setFax : function(fax_) {
      this.fax = fax_;
   },

   getEmail : function() {
      if(!this.initialized)
         this.init();
      return this.email;
   },

   setEmail : function(email_) {
      this.email = email_;
   },

   getCreditLimit : function() {
      if(!this.initialized)
         this.init();
      return this.creditLimit;
   },

   setCreditLimit : function(creditLimit_) {
      this.creditLimit = creditLimit_;
   },

   getDiscountCode : function() {
      if(!this.initialized)
         this.init();
      return this.discountCodeRef;
   },

   setDiscountCode : function(discountCodeRef_) {
      this.discountCodeRef = discountCodeRef_;
   },



   init : function() {
      var remote = new CustomerRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('(' +c+')');
         var customer = myObj.customer;
         this.uri = customer['@uri'];
         this.customerId = customer['customerId']['$'];
         this.zip = customer['zip']['$'];
         this.name = customer['name']['$'];
         this.addressline1 = customer['addressline1']['$'];
         this.addressline2 = customer['addressline2']['$'];
         this.city = customer['city']['$'];
         this.state = customer['state']['$'];
         this.phone = customer['phone']['$'];
         this.fax = customer['fax']['$'];
         this.email = customer['email']['$'];
         this.creditLimit = customer['creditLimit']['$'];
         this.discountCodeRef = new DiscountCode(customer['discountCodeRef']['@uri']);

         this.initialized = true;
      }
   },

   flush : function() {
      var remote = new CustomerRemote(this.uri);
      return remote.putJson(this.toString());
   },
   
   delete_ : function() {
      var remote = new CustomerRemote(this.uri);
      return remote.delete_();
   },   

   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '{"customer":'+
         '{'+
         '"@uri":"'+this.uri+'",'+
                  '"customerId":{"$":"'+this.customerId+'"},'+
         '"zip":{"$":"'+this.zip+'"},'+
         '"name":{"$":"'+this.name+'"},'+
         '"addressline1":{"$":"'+this.addressline1+'"},'+
         '"addressline2":{"$":"'+this.addressline2+'"},'+
         '"city":{"$":"'+this.city+'"},'+
         '"state":{"$":"'+this.state+'"},'+
         '"phone":{"$":"'+this.phone+'"},'+
         '"fax":{"$":"'+this.fax+'"},'+
         '"email":{"$":"'+this.email+'"},'+
         '"creditLimit":{"$":"'+this.creditLimit+'"},'+
         '"discountCodeRef":{"@uri":"'+this.discountCodeRef.getUri()+'", "discountCode":{"$":"'+this.discountCodeRef.getDiscountCode()+'"}}'+

         '}'+
      '}';
      return myObj;
   }

}

function CustomerRemote(uri_) {
    this.uri = uri_;
}

CustomerRemote.prototype = {

   getXml : function() {
      return get_(this.uri, 'application/xml');
   },

   getJson : function() {
      return get_(this.uri, 'application/json');
   },

   putXml : function(content) {
      return put_(this.uri, 'application/xml', content);
   },

   putJson : function(content) {
      return put_(this.uri, 'application/json', content);
   },

   delete_ : function() {
      return delete__(this.uri);
   },

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
