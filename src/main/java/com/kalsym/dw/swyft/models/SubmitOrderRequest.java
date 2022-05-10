/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class SubmitOrderRequest {

    @JsonProperty("ORDER_ID")
    String orderId;
    @JsonProperty("ORDER_TYPE")
    String orderType;
    @JsonProperty("CONSIGNEE_FIRST_NAME")
    String consigneeFirstName;
    @JsonProperty("CONSIGNEE_LAST_NAME")
    String consigneeLastName;
    @JsonProperty("CONSIGNEE_EMAIL")
    String consigneeEmail;
    @JsonProperty("CONSIGNEE_PHONE")
    String consigneePhone;
    @JsonProperty("CONSIGNEE_CITY")
    String consigneeCity;
    @JsonProperty("CONSIGNEE_ADDRESS")
    String consigneeAddress;
    @JsonProperty("PACKAGING")
    String packaging;
    @JsonProperty("ORIGIN_CITY")
    String originCity;
    @JsonProperty("PIECES")
    Integer pieces;
    @JsonProperty("COD")
    Double cod;
    @JsonProperty("DESCRIPTION")
    String description;
    @JsonProperty("WEIGHT")
    Double weight;
    @JsonProperty("SHIPPER_ADDRESS_ID")
    String shipperAddressId;

    public SubmitOrderRequest(Order order) {
        this.orderType = order.getPaymentType();
        this.consigneeFirstName = order.getDelivery().getDeliveryContactName();
        this.consigneeLastName = order.getDelivery().getDeliveryContactName();
        this.consigneeEmail = order.getDelivery().getDeliveryContactEmail();
        this.consigneePhone = order.getDelivery().getDeliveryContactPhone();
        this.consigneeCity = order.getDelivery().getDeliveryCity();
        this.consigneeAddress = order.getDelivery().getDeliveryAddress();
        this.packaging = order.getItemType();
        this.originCity = order.getPickup().getPickupCity();
        this.pieces = order.getPieces();
        this.cod = order.getPaymentType().equals("COD") ? order.getShipmentValue() : null;
        this.cod = order.getShipmentValue();
        this.description = order.getShipmentContent();
        this.weight = order.getTotalWeightKg();
        this.shipperAddressId = null;
    }
}
