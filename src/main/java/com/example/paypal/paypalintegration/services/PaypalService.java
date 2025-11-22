package com.example.paypal.paypalintegration.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaypalService {
	
	@Value("${paypal.client-id}")
	String cliendId;
	@Value("${paypal.client-secret}")
	String clientSecret;
	@Value("${paypal.base-url}")
	String baseUrl;
	@Value("${app.frontend-url}")
	String appUrl;
	@Value("${app.subscription}")
	String subscriptionAmount;
	RestTemplate restTemplate;
	
	Logger log = LoggerFactory.getLogger(getClass());

	
	public PaypalService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public String getAccessToken(){
		String url = baseUrl + "/v1/oauth2/token";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(cliendId, clientSecret);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
		
		return (String) response.getBody().get("access_token");
	}
	
	//Step 2
	public Map<String, Object> createOrder(){
		log.error("(2)-CREATE order");
		
		String accessToken = getAccessToken();
		String url = baseUrl + "/v2/checkout/orders";		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		Map<String, Object> paymentRequest = Map.of(
				"intent", "CAPTURE",
				"purchase_units", List.of(
						Map.of("amount", Map.of("currency_code", "USD", 
								                "value", subscriptionAmount))
				),
				"application_context", Map.of(
						"return_url", appUrl + "/accept",
						"cancel_url", appUrl + "/cancel"
				)
		);
		
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
		
		return response.getBody();
	}
	
	/*Step 4 - Finish*/
	public Map<String, Object> capturePayment(String orderId){
		log.error("(4)-CAPTURED Order");
		String accessToken = getAccessToken();
		String url = baseUrl + "/v2/checkout/orders/" + orderId + "/capture";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<>(null, headers);
		ResponseEntity<Map> response = restTemplate.exchange(
				url, 
				HttpMethod.POST, 
				entity, 
				Map.class
		);

		return response.getBody();
	}	
}
