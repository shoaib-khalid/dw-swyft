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
public class ProviderConfig {

    @JsonProperty("base_url")
    String baseUrl;
    @JsonProperty("api_key")
    String apiKey;
    @JsonProperty("submit_order_endpoint")
    String submitOrderEndpoint;
    @JsonProperty("cancel_order_endpoint")
    String cancelOrderEndpoint;
    @JsonProperty("query_order_endpoint")
    String queryOrderEndpoint;

}
