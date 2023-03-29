package com.payment.service.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.*;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.payment.service.data.ChargeEntry;
import com.payment.service.data.ChargeRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@WebMvcTest(ChargeController.class)
public class ChargeControllerTests {

        private final ChargeController underTest = new ChargeController();

        @Autowired
        private MockMvc mockMvc;

        @Before
        public void setup() {
            mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
        }

        @Test
        public void testNormalCharge() throws Exception {
            // Create a charge request with one charge entry
            ChargeEntry chargeEntry = new ChargeEntry();

            // Test product
            chargeEntry.setProductId("prod_NcM23zdrtQ78YP");
            chargeEntry.setQuantity(1);
            List<ChargeEntry> chargeEntries = Collections.singletonList(chargeEntry);
            ChargeRequest chargeRequest = new ChargeRequest();
            chargeRequest.setChargeEntries(chargeEntries);


            // Make request to create checkout session
            MvcResult result = mockMvc.perform(post("/create-checkout-session")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(chargeRequest)))
                    .andExpect(status().isOk())
                    .andReturn();



            // Verify that the response body contains the session URL
            String responseBody = result.getResponse().getContentAsString();
            assertTrue(responseBody.contains("https://checkout.stripe.com/c/pay/cs_test"));
            assertEquals(200, result.getResponse().getStatus());
        }

    @Test
    public void testRetrieveNonExistingProduct() throws Exception {
            Assertions.assertThrows(InvalidRequestException.class, () -> {
                Product.retrieve("non-existing-id");
            });
    }
    @Test
    public void testCreateCheckoutSessionWithoutChargeRequest() throws StripeException {
        ResponseEntity<Object> responseEntity = underTest.createCheckoutSession(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}

