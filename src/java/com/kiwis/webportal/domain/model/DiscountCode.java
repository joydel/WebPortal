package com.kiwis.webportal.domain.model;
// Generated Jun 18, 2014 9:46:47 PM by Hibernate Tools 3.6.0


import java.math.BigDecimal;

/**
 * DiscountCode generated by hbm2java
 */
public class DiscountCode  implements java.io.Serializable {


     private char discountCode;
     private BigDecimal rate;

    public DiscountCode() {
    }

	
    public DiscountCode(char discountCode) {
        this.discountCode = discountCode;
    }
    public DiscountCode(char discountCode, BigDecimal rate) {
       this.discountCode = discountCode;
       this.rate = rate;
    }
   
    public char getDiscountCode() {
        return this.discountCode;
    }
    
    public void setDiscountCode(char discountCode) {
        this.discountCode = discountCode;
    }
    public BigDecimal getRate() {
        return this.rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }




}

