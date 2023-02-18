package com.payment.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CheckoutController {

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @RequestMapping("/checkout")
    public ResponseEntity<Object> checkout() {
        return ResponseEntity.status(HttpStatus.OK).body(stripePublicKey);
    }
}
