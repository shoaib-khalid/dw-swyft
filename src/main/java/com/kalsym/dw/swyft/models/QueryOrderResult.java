package com.kalsym.dw.swyft.models;

import com.kalsym.dw.swyft.models.daos.DeliveryOrder;

/**
 * @author user
 */
public class QueryOrderResult {
    public int providerId;
    public DeliveryOrder orderFound;
    public boolean isSuccess;
}
