/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author imran
 */
@Data
public class DeliveryDetails {

    String deliveryContactPhone;
    String deliveryContactName;
    String deliveryContactEmail;
    String deliveryAddress;
    String deliveryPostcode;
    String deliveryState;
    String deliveryCity;
    String deliveryCountry;

    BigDecimal latitude;
    BigDecimal longitude;
}
