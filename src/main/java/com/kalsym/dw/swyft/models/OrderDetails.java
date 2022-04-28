/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author imran
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails {

    String orderId;
    String customerId;
    Integer merchantId;
    String paymentType;
    Double orderAmount;
    String signature;
    Double shipmentValue;
    String itemType;
    Integer pieces;
    Double totalWeightKg;
    String shipmentContent;

    DeliveryDetails delivery;
    PickupDetails pickup;
}
