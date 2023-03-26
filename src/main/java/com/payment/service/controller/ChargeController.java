package com.payment.service.controller;

import ch.qos.logback.core.joran.sanity.Pair;
import com.payment.service.ServiceApplication;
import com.payment.service.data.ChargeEntry;
import com.payment.service.data.ChargeRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChargeController {

    @Value("${stripe.api.key}")
    private String secretKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Object> createCheckoutSession(
            @RequestBody @Valid ChargeRequest chargeRequest
    ) throws StripeException {
        // We create a  stripe session parameters
        if (chargeRequest == null) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("url", frontendUrl);
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } else {
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            for (ChargeEntry entry : chargeRequest.getChargeEntries()) {
                Product product = Product.retrieve(entry.getProductId());
                lineItems.add(SessionCreateParams.LineItem.builder().setQuantity(entry.getQuantity().longValue())
                        .setName(product.getName()).setPrice(product.getDefaultPrice())
                        .setCurrency("eur").setDescription(product.getDescription())
                        .build());
            }

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(frontendUrl + "completion")
                            .setCancelUrl(frontendUrl + "payment").addAllLineItem(lineItems).build();
            Session session = Session.create(params);
            Logger logger = LoggerFactory.getLogger(ServiceApplication.class);
            logger.info(session.getStatus());
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("url", session.getUrl());
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        }
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Object> handleError(StripeException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}
