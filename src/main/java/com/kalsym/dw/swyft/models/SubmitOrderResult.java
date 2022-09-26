/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.kalsym.dw.swyft.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kalsym.dw.swyft.models.daos.DeliveryOrder;

/**
 *
 * @author user
 */
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitOrderResult {
    public int deliveryProviderId;
    public DeliveryOrder orderCreated;
    public boolean isSuccess;
    public String message;
    public String status;
    public String systemTransactionId;
    public String orderId;
    @JsonIgnore
    public int resultCode;
    @JsonIgnore
    public String customerTrackingUrl;
    @JsonIgnore
    public String spTransactionId;

}
