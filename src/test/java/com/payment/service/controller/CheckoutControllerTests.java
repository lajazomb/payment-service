package com.payment.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.stripe.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
public class CheckoutControllerTests {
    private final ChargeController underTest = new ChargeController();

    @Test
    public void testCreateCheckoutSessionWithoutChargeRequest() throws StripeException {
        ResponseEntity<Object> responseEntity = underTest.createCheckoutSession(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}

