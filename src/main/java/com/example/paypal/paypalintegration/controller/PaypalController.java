package com.example.paypal.paypalintegration.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.paypal.paypalintegration.dto.MessageResponse;
import com.example.paypal.paypalintegration.services.PaypalService;

import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api")
public class PaypalController{
	
	PaypalService paypalService;
	Logger log = LoggerFactory.getLogger(getClass());

	public PaypalController(PaypalService paypalService) {
		this.paypalService = paypalService;
	}
	
	@GetMapping("/token")
	public String getToken(){
		log.error("(0)-ALT-getToken()");
		return "token: " + paypalService.getAccessToken();
	}
	
	/*Step 1 - To create a pyment this is the first step*/
	@PostMapping("/create")
	public ResponseEntity<?> createOrder(){
		log.error("(1)-CREATE Order");		
		Map<String, Object> result = paypalService.createOrder();
		log.error("RESULT: " + result.toString());
		
		return ResponseEntity.ok(result);
	}
	
	/*Step 3 - If User approves payment(one step more)*/
	@GetMapping("/accept")
	public ResponseEntity<MessageResponse> captureOrder(@RequestParam String token, HttpServletResponse response) throws IOException{
		log.error("(3)- CAPTURE Order");
		Map<String, Object> orderCaptured = paypalService.capturePayment(token);
		
		log.error("RESPONSE: " + orderCaptured.get("status").toString());
		
		//response.sendRedirect("http://localhost:4200/payments/result?value=" + orderCaptured.get("status").toString());
		
		return ResponseEntity.ok(new MessageResponse(orderCaptured.get("status").toString()));
	}
	
	/*Step 3 - Finish if User rejects payment*/
	@GetMapping("/cancel")
	public ResponseEntity<?> cancelOrder(@RequestParam String token) {
		Map<String, Object> response = new HashMap<>();
		response.put("order", token);
		response.put("status", "CANCELED");
		
		return ResponseEntity.ok(response);
	}
}

/*
 * Flujo completo: primero se consume create, 
 * posteriormente redirigira de forma automatica a paypalweb, 
 * luego retornara una respuesta de aceptar o rechazar.
 * 
 */