package com.kalsym.dw.swyft.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.dw.swyft.models.CancelParcelResponse;
import com.kalsym.dw.swyft.models.Order;
import com.kalsym.dw.swyft.models.ParcelHistory;
import com.kalsym.dw.swyft.models.ProviderConfig;
import com.kalsym.dw.swyft.models.SubmitOrderRequest;
import com.kalsym.dw.swyft.models.SubmitOrderResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 *
 * @author imran
 */
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger("Swyft-Application");
    private static WebClient swyftClient;

    public Object getPrice(String providerConfig, Order order) {

        String pickupCity = order.getPickup().getPickupCity();
        String deliveryCity = order.getDelivery().getDeliveryCity();
        String zonePickup = order.getPickup().getPickupZone();
        String zoneDelivery = order.getDelivery().getDeliveryZone();
        Double weight = order.getTotalWeightKg();

        Double fuelChargeRate = 16.0;
        Double gstRate = 8.5;
        Double totalCharge = 0.00;

        ObjectMapper mapper = new ObjectMapper();
        try {
            ProviderConfig configObj = mapper.readValue(providerConfig, ProviderConfig.class);
            fuelChargeRate = configObj.getFuelCharges();
            gstRate = configObj.getGst();
        } catch (JsonProcessingException ex) {
            logger.error("Failed to process configuration string. Using default values for fuel charges and GST instead.", 
                    ex.getLocalizedMessage());
        }

        if (!zonePickup.equals("null") && !zoneDelivery.equals("null") && !pickupCity.equals("null") && !deliveryCity.equals("null")) {
            if (pickupCity.equals(deliveryCity)) {
                totalCharge = calculatePrice(weight, 119.0, 125.0, 50.0);
            } else if (zonePickup.equals(zoneDelivery)) {
                totalCharge = calculatePrice(weight, 125.0, 138.0, 60.0);
            } else {
                totalCharge = calculatePrice(weight, 151.0, 163.0, 70.0);
            }
            Double fuelCharge = totalCharge * fuelChargeRate / 100.0;
            Double gst = totalCharge * gstRate / 100.0;
            totalCharge += fuelCharge + gst;
        }

        return totalCharge;
    }

    private Double calculatePrice(Double weight, Double lowerBasePrice, 
            Double higherBasePrice, Double additionalPrice) {
        if (weight <= 0.5) {
            return lowerBasePrice;
        }

        if (weight <= 1.0) {
            return higherBasePrice;
        }
        
        return higherBasePrice + (weight - 1) * additionalPrice;
    }

    public Object submitOrder(String providerConfig, String orderString, String systemTransactionId) {
//        String endpoint = "/api-upload";
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig configObj = new ProviderConfig();
        Order order = new Order();
        try {
            configObj = mapper.readValue(providerConfig, ProviderConfig.class);
            order = mapper.readValue(orderString, Order.class);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to process json string for providerConfig", ex);
            return null;
        }

        SubmitOrderResponse response = null;
        String errorResponse = null;
        try {
            initWebClient(configObj.getBaseUrl(), configObj.getApiKey());

            SubmitOrderRequest request = new SubmitOrderRequest(order);
            List<SubmitOrderRequest> requestList = new ArrayList();
            requestList.add(request);

            response = swyftClient.post()
                    .uri(configObj.getSubmitOrderEndpoint())
                    .body(
                            Mono.just(requestList),
                            new ParameterizedTypeReference<List<SubmitOrderRequest>>() {
                    })
                    .retrieve()
                    .bodyToMono(SubmitOrderResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            logger.error("Failed to submit order. Status Code: {}, {}, Error Body: {}",
                    ex.getStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());
            errorResponse = ex.getResponseBodyAsString();
        } catch (NullPointerException ex) {
            logger.error("Error while getting property from provider configuration", ex);
        }

        if (response == null) {
            return new JSONObject(errorResponse);
        }

        return new JSONObject(response);
    }

    public Object cancelOrder(String providerConfig, String orderId) {
//                String endpoint = "/cancel-parcel/" + orderId;
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig configObj = new ProviderConfig();
        try {
            configObj = mapper.readValue(providerConfig, ProviderConfig.class);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to process json string for providerConfig", ex);
            return null;
        }

        CancelParcelResponse response = new CancelParcelResponse();
        try {
            initWebClient(configObj.getBaseUrl(), configObj.getApiKey());

            response = swyftClient.post()
                    .uri(configObj.getCancelOrderEndpoint())
                    .retrieve()
                    .bodyToMono(CancelParcelResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            logger.error("Error while cancelling order. Status Code: {}, {}, Error Body: {}",
                    ex.getStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());

            try {
                response = mapper.readValue(ex.getResponseBodyAsString(), CancelParcelResponse.class);
            } catch (JsonProcessingException ex1) {
                logger.error("Error while parsing json string", ex1.getLocalizedMessage());
            }
        } catch (NullPointerException ex) {
            logger.error("Error while getting property from provider configuration", ex);
        }

        return new JSONObject(response);
    }

    public Object queryOrder(String providerConfig, String orderId) {
//        String endpoint = "/get-parcel-history/" + orderId;

        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig configObj = new ProviderConfig();
        try {
            configObj = mapper.readValue(providerConfig, ProviderConfig.class);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to process json string for providerConfig", ex);
            return null;
        }

        ParcelHistory response = null;
        String errorResponse = null;

        try {
            initWebClient(configObj.getBaseUrl(), configObj.getApiKey());

            response = swyftClient.get()
                    .uri(configObj.getQueryOrderEndpoint())
                    .retrieve()
                    .bodyToMono(ParcelHistory.class)
                    .block();
        } catch (WebClientResponseException ex) {
            logger.error("Error while cancelling order. Status Code: {}, {}, Error Body: {}",
                    ex.getStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());
            errorResponse = ex.getResponseBodyAsString();
        } catch (NullPointerException ex) {
            logger.error("Error while getting property from provider configuration", ex);
        }

        if (response == null) {
            return new JSONObject(errorResponse);
        }

        return new JSONObject(response);
    }

    public String getToken(String providerConfig) {
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig configObj = new ProviderConfig();
        try {
            configObj = mapper.readValue(providerConfig, ProviderConfig.class);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to process json string for providerConfig", ex);
            return null;
        }
        return configObj.getApiKey();
    }

    private void initWebClient(String apiUrl, String apiKey) throws NullPointerException {
        if (swyftClient == null) {
            swyftClient = WebClient
                    .builder()
                    .baseUrl(apiUrl)
                    .defaultHeaders(httpHeaders -> {
                        httpHeaders.set(HttpHeaders.AUTHORIZATION, apiKey);
                        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .filters(exchangeFilterFunctions -> {
                        exchangeFilterFunctions.add(logRequest());
                        exchangeFilterFunctions.add(logResponse());
                    })
                    .build();
        }
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder sb = new StringBuilder();
            sb.append(clientRequest.method())
                    .append(" ").append(clientRequest.url()).append("\n");
            logger.info("Request info: {}", sb.toString());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response statusCode: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private ProviderConfig parseProviderConfig(String configString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configString, ProviderConfig.class);
    }
}
