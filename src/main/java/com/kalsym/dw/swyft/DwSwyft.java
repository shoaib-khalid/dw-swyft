/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.kalsym.dw.swyft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.dw.swyft.models.ProviderConfig;
import org.json.JSONObject;

/**
 *
 * @author imran
 */
public class DwSwyft {

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig config = new ProviderConfig();
        config.setBaseUrl("wiaujdsoia");
        config.setApiKey("aoiuejoijas");
        JSONObject jsonObj = new JSONObject(mapper.writeValueAsString(config));
        System.out.println(jsonObj.get("api_key"));
    }
}
