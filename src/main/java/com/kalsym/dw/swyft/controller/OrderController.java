package com.kalsym.dw.swyft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kalsym.dw.swyft.models.*;
import com.kalsym.dw.swyft.models.daos.DeliveryOrder;
import com.kalsym.dw.swyft.models.enums.DeliveryCompletionStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kalsym.dw.swyft.utils.LogUtil;
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
 * @author imran
 * @review Kumar
 */
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger("Swyft-Application");
    private static WebClient swyftClient;

    public Object getPrice(String providerConfig, String orderObject, String systemTransactionId, String fulfillment) {
        logger.warn("Request Get Price [:{}], Request Body : {}", systemTransactionId, orderObject);

        ProcessResult response = new ProcessResult();

        JsonObject order = new Gson().fromJson(orderObject, JsonObject.class);

        JsonObject fulfillments = new Gson().fromJson(fulfillment, JsonObject.class);

        String pickupCity = order.getAsJsonObject("pickup").get("pickupCity").getAsString();
        String deliveryCity = order.getAsJsonObject("delivery").get("deliveryCity").getAsString();
        String zonePickup = order.getAsJsonObject("pickup").get("pickupZone").getAsString();
        String zoneDelivery = order.getAsJsonObject("delivery").get("deliveryZone").getAsString();
        Double weight = order.get("totalWeightKg").getAsDouble();

        Double fuelChargeRate = 16.0;
        Double gstRate = 8.5;
        Double totalCharge = 0.00;

        ObjectMapper mapper = new ObjectMapper();
        try {
            ProviderConfig configObj = mapper.readValue(providerConfig, ProviderConfig.class);
            fuelChargeRate = configObj.getFuelCharges();
            gstRate = configObj.getGst();
        } catch (JsonProcessingException ex) {
            logger.error(
                    "Failed to process configuration string. Using default values for fuel charges and GST instead.",
                    ex.getLocalizedMessage());
        }

        if (!zonePickup.equals("null") && !zoneDelivery.equals("null") && !pickupCity.equals("null")
                && !deliveryCity.equals("null")) {
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
        PriceResult priceResult = new PriceResult();
        BigDecimal bd = new BigDecimal(totalCharge);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        priceResult.price = bd;
        priceResult.pickupCity = pickupCity;
        priceResult.deliveryCity = deliveryCity;
        priceResult.pickupZone = zonePickup;
        priceResult.deliveryZone = zoneDelivery;
        priceResult.fulfillment = fulfillments.get("fulfillment").getAsString();
        priceResult.interval = null;
        response.resultCode = 0;
        response.returnObject = priceResult;

        LogUtil.info(systemTransactionId, "Request Url :" + response.toString(), "");


        return new JSONObject(new Gson().toJson(response));
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
        // String endpoint = "/api-upload";
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig configObj = new ProviderConfig();
        ProcessResult res = new ProcessResult();
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
                    .uri(configObj.getVendorId() + configObj.getSubmitOrderEndpoint())
                    .body(
                            Mono.just(requestList),
                            new ParameterizedTypeReference<List<SubmitOrderRequest>>() {
                            })
                    .retrieve()
                    .bodyToMono(SubmitOrderResponse.class)
                    .block();

            DeliveryOrder orderCreated = new DeliveryOrder();
            for (SubmitOrderResponseData s : response.getData()) {
                orderCreated.setSpOrderId(s.getOrderId());
                orderCreated.setSpOrderName(s.getOrderId());
                orderCreated.setCreatedDate(new Date().toString());
                // orderCreated.setCustomerTrackingUrl(shareLink);
                orderCreated.setStatus("ASSIGNING_DRIVER");
                orderCreated.setStatusDescription("ASSIGNING_DRIVER");
                orderCreated.setSystemStatus("ASSIGNING_DRIVER");
            }
            SubmitOrderResult submitOrderResult = new SubmitOrderResult();

            submitOrderResult.orderCreated = orderCreated;
            res.returnObject = submitOrderResult;

        } catch (WebClientResponseException ex) {
            logger.error("Failed to submit order. Status Code: {}, {}, Error Body: {}",
                    ex.getStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());
            errorResponse = ex.getResponseBodyAsString();
        } catch (NullPointerException ex) {
            logger.error("Error while getting property from provider configuration", ex);
        }

        if (response == null) {
            SubmitOrderResult submitOrderResult = new SubmitOrderResult();
            submitOrderResult.message = errorResponse;
            res.resultCode = -1;
            res.resultString = errorResponse;
            res.returnObject = submitOrderResult;
            return new JSONObject(res);

        }

        return new JSONObject(res);
    }

    public Object cancelOrder(String providerConfig, String orderId) {
        // String endpoint = "/cancel-parcel/" + orderId;
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
        // String endpoint = "/get-parcel-history/" + orderId;
        ProcessResult res = new ProcessResult();

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
                    .uri(configObj.getVendorId() + configObj.getQueryOrderEndpoint() + orderId)
                    .retrieve()
                    .bodyToMono(ParcelHistory.class)
                    .block();
            QueryOrderResult queryOrderResult = new QueryOrderResult();

            DeliveryOrder orderFound = new DeliveryOrder();
            assert response != null;
            logger.error("Success response order. Status Code: {}, {}", response.getStatus(), new Date());
            switch (response.getStatus()) {
                case "RECEIVED":
                case "WAITING_FOR_TRANSPORT":
                    orderFound.setSystemStatus(DeliveryCompletionStatus.ASSIGNING_RIDER.name());
                    break;
                case "Awaiting Pickup":
                    orderFound.setSystemStatus(DeliveryCompletionStatus.AWAITING_PICKUP.name());
                    break;
                case "Picked Up":
                    orderFound.setSystemStatus(DeliveryCompletionStatus.BEING_DELIVERED.name());
                    break;
                case "Delivered":
                    orderFound.setSystemStatus(DeliveryCompletionStatus.COMPLETED.name());
                    break;
                case "Canceled":
                    orderFound.setSystemStatus(DeliveryCompletionStatus.CANCELED.name());
                    break;
            }
            orderFound.setSpOrderId(orderId);
            queryOrderResult.orderFound = orderFound;

            res.resultCode = 0;
            res.returnObject = queryOrderResult;
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
