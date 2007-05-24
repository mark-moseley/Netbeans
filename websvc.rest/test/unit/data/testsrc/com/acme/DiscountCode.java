/*
 * DiscountCode.java
 * 
 * Created on May 17, 2007, 5:32:07 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author nam
 */
@Entity
@Table(name = "DISCOUNT_CODE")
@NamedQueries({@NamedQuery(name = "DiscountCode.findByDiscountCode", query = "SELECT d FROM DiscountCode d WHERE d.discountCode = :discountCode"), @NamedQuery(name = "DiscountCode.findByRate", query = "SELECT d FROM DiscountCode d WHERE d.rate = :rate")})
public class DiscountCode implements Serializable {
    @Id
    @Column(name = "DISCOUNT_CODE", nullable = false)
    private String discountCode;
    @Column(name = "RATE")
    private BigDecimal rate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discountCode")
    private Collection<Customer> customerCollection;

    public DiscountCode() {
    }

    public DiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Collection<Customer> getCustomerCollection() {
        return customerCollection;
    }

    public void setCustomerCollection(Collection<Customer> customerCollection) {
        this.customerCollection = customerCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (discountCode != null ? discountCode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DiscountCode)) {
            return false;
        }
        DiscountCode other = (DiscountCode) object;
        if (this.discountCode != other.discountCode && (this.discountCode == null || !this.discountCode.equals(other.discountCode))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "test.DiscountCode[discountCode=" + discountCode + "]";
    }

}
