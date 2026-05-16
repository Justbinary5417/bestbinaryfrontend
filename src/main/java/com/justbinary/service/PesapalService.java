package com.justbinary.service;

import com.justbinary.config.PesapalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PesapalService {

    @Autowired
    private PesapalConfig pesapalConfig;

    @Autowired
    private RestTemplate restTemplate;

    // ── STEP 1: Get Access Token ──────────────────────
    public String getAccessToken() {
        String url = pesapalConfig.getBaseUrl() + "/api/Auth/RequestToken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("consumer_key", pesapalConfig.getConsumerKey());
        body.put("consumer_secret", pesapalConfig.getConsumerSecret());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("token")) {
            return (String) responseBody.get("token");
        }
        throw new RuntimeException("Failed to get Pesapal access token");
    }

    // ── STEP 2: Register IPN ──────────────────────────
    public String registerIPN(String token) {
        String url = pesapalConfig.getBaseUrl() + "/api/URLSetup/RegisterIPN";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, String> body = new HashMap<>();
        body.put("url", pesapalConfig.getIpnUrl());
        body.put("ipn_notification_type", "GET");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("ipn_id")) {
            return (String) responseBody.get("ipn_id");
        }
        throw new RuntimeException("Failed to register Pesapal IPN");
    }

    // ── STEP 3: Submit Order (STK Push) ───────────────
    public Map<String, Object> submitOrder(
            String phone, double amount, String currency, String description) {

        String token = getAccessToken();
        String ipnId = registerIPN(token);

        String url = pesapalConfig.getBaseUrl() + "/api/Transactions/SubmitOrderRequest";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String orderId = "JB-" + System.currentTimeMillis();

        Map<String, Object> billingAddress = new HashMap<>();
        billingAddress.put("phone_number", phone);

        Map<String, Object> body = new HashMap<>();
        body.put("id", orderId);
        body.put("currency", currency);
        body.put("amount", amount);
        body.put("description", description);
        body.put("callback_url", pesapalConfig.getCallbackUrl());
        body.put("notification_id", ipnId);
        body.put("billing_address", billingAddress);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            responseBody.put("orderId", orderId);
            return responseBody;
        }
        throw new RuntimeException("Failed to submit Pesapal order");
    }

    // ── STEP 4: Check Transaction Status ─────────────
    public Map<String, Object> checkStatus(String orderTrackingId) {
        String token = getAccessToken();
        String url = pesapalConfig.getBaseUrl()
                + "/api/Transactions/GetTransactionStatus?orderTrackingId="
                + orderTrackingId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class);

        return response.getBody();
    }
}