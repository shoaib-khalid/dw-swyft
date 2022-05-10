/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import lombok.Data;

/**
 *
 * @author imran
 */
@Data
public class Pickup {

    String parcelReadyTime;
    String pickupDate;
    String pickupTime;
    String endPickupDate;
    String endPickupTime;
    String pickupOption;
    VehicleType vehicleType;

    String pickupAddress;
    String pickupPostcode;
    String pickupState;
    String pickupCountry;
    String pickupCity;
    Integer pickupLocationId;

    String pickupContactName;
    String pickupContactPhone;
    String pickupContactEmail;
    boolean isTrolleyRequired;
    String remarks;
    String pickupZone;
    String costCenterCode;
}
