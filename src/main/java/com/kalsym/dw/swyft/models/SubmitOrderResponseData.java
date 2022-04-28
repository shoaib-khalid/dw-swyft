/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author imran
 */
public class SubmitOrderResponseData {

    @JsonProperty("consigneeName")
    String consigneeName;
    @JsonProperty("consigneeEmail")
    String consigneeEmail;
    @JsonProperty("CONSIGNEE_PHONE")
    String consigneePhone;
    @JsonProperty("CONSIGNEE_ADDRESS")
    String consigneeAddress;
    @JsonProperty("consigneeCity")
    String consigneeCity;
    @JsonProperty("parcelId")
    String parcelId;
    @JsonProperty("qty")
    Integer quantity;
    @JsonProperty("weight")
    Double weight;
    @JsonProperty("specialHandlings")
    String specialHandlings;
    @JsonProperty("description")
    String description;
    @JsonProperty("orderId")
    String orderId;
    @JsonProperty("shipperName")
    String shipperName;
    @JsonProperty("shipperAddress")
    String shipperAddress;
    @JsonProperty("shipperCity")
    String shipperCity;
    @JsonProperty("paymentMode")
    String paymentMode;
    @JsonProperty("amount")
    Integer amount;
}
