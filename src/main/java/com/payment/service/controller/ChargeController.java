package com.payment.service.controller;

import com.payment.service.data.ChargeRequest;
import com.payment.service.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChargeController {

    @Autowired
    private StripeService paymentsService;

    @PostMapping("/charge")
    public ResponseEntity<Object> charge(
            @RequestBody ChargeRequest chargeRequest
    ) throws StripeException {
        chargeRequest.setCurrency(ChargeRequest.Currency.EUR);
        paymentsService.charge(chargeRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Object> handleError(StripeException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}
