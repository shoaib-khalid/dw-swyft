/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kalsym.dw.swyft.models;

import java.util.List;
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
public class SubmitOrderResponse {
    List<SubmitOrderResponseData> data;
}
