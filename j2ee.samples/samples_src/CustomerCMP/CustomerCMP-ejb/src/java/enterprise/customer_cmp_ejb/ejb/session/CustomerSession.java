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


package enterprise.customer_cmp_ejb.ejb.session;

import javax.ejb.Stateless;
import javax.ejb.Stateful;
import javax.ejb.SessionContext;
import javax.persistence.*;
import javax.ejb.*;
import java.util.List;

import enterprise.customer_cmp_ejb.persistence.*;
import enterprise.customer_cmp_ejb.common.*;
/**
 *
 * @author Rahul Biswas
 *
 * Why a facade?  
 * 1. session beans are thread safe, and EMs are not necessarily; so injecting a EM into a SessionBean makes it safe. 
 * 2. Tx management is taken care of by container
 * 3. of course, because it's a facade [we can combine operations].
 * 
 */
@Stateless
@TransactionManagement(value=TransactionManagementType.CONTAINER)

public class CustomerSession implements CustomerSessionLocal, CustomerSessionRemote{
    
    @javax.persistence.PersistenceContext(unitName="persistence_sample")
    private EntityManager em ;
    
    
    public CustomerSession(){
        
    }

    public Customer searchForCustomer(String id){
        
        Customer cust = (Customer)em.find(Customer.class, id);
        return cust;
    }
    
    
    public Subscription searchForSubscription(String id){
        
        Subscription subscription = (Subscription)em.find(Subscription.class, id);
        return subscription;
    }
    
    public Address searchForAddress(String id){
        
        Address address = (Address)em.find(Address.class, id);
        return address;
        
    }
    
    //This is the default; here as an example of @TransactionAttribute
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void remove(Object obj){
        Object mergedObj = em.merge(obj);
        em.remove(mergedObj);
    }
    
    public void persist(Object obj){
        em.persist(obj);
    }
    
    public List findAllSubscriptions(){
    
        List subscriptions = em.createNamedQuery("findAllSubscriptions").getResultList();
        return subscriptions;
        
    }
    
    public List findCustomerByFirstName(String firstName){
        List customers = em.createNamedQuery("findCustomerByFirstName").setParameter("firstName", firstName).getResultList();
        return customers;
    }
    
    public List findCustomerByLastName(String lastName){
        List customers = em.createNamedQuery("findCustomerByLastName").setParameter("lastName", lastName).getResultList();
        return customers;
    }
    
    
    public Customer addCustomerAddress(Customer cust, Address address){
        
        Customer mergedCust = em.merge(cust);
        mergedCust.getAddresses().add(address);
        return mergedCust;
        
    }
    
    public Customer removeCustomerSubscription(String cust, String subs) throws SubscriptionNotFoundException{
        
        //System.out.println("called remove Customer Subscription.....");
        
        Customer customer = (Customer)em.find(Customer.class, cust);
        Subscription subscription = (Subscription)em.find(Subscription.class, subs);
        
        if(!customer.getSubscriptions().contains(subscription)){
            System.out.println("remove: did not find a subscription obj for :"+subscription.getTitle());
            throw new SubscriptionNotFoundException();
        }
        
        customer.getSubscriptions().remove(subscription);
        subscription.getCustomers().remove(customer);
        
        return customer;
    }
    
    public Customer addCustomerSubscription(String cust, String subs) throws DuplicateSubscriptionException{
        
        //System.out.println("called add Customer Subscription.....");
        Customer customer = (Customer)em.find(Customer.class, cust);
        Subscription subscription = (Subscription)em.find(Subscription.class, subs);
        
        if(customer.getSubscriptions().contains(subscription)){
            System.out.println("add: found an existing subscription obj for :"+subscription.getTitle());
            throw new DuplicateSubscriptionException();
        }
        
        customer.getSubscriptions().add(subscription);
        subscription.getCustomers().add(customer);
        
        return customer;
        
    }
    
}
