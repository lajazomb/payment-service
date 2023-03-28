package com.payment.service.controller;

import com.payment.service.ServiceApplication;
import com.payment.service.data.ChargeEntry;
import com.payment.service.data.ChargeRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.*;

@Controller
public class ChargeController {

    @Value("${stripe.api.key}")
    private String secretKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    Logger logger = LoggerFactory.getLogger(ServiceApplication.class);

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Object> createCheckoutSession(
            @RequestBody @Valid ChargeRequest chargeRequest
    ) throws StripeException {
        if (chargeRequest == null) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("url", frontendUrl);
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } else {
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            for (ChargeEntry entry : chargeRequest.getChargeEntries()) {
                Product product = Product.retrieve(entry.getProductId());
                lineItems.add(SessionCreateParams.LineItem.builder()
                        .setQuantity(entry.getQuantity().longValue())
                        .setPrice(product.getDefaultPrice())
                        .build());
            }


            Session session = Session.create(createSessionParams(lineItems));
            logger.info(session.getStatus());
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("url", session.getUrl());
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        }
    }

    private SessionCreateParams createSessionParams(List<SessionCreateParams.LineItem> items) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setShippingAddressCollection(
                        SessionCreateParams.ShippingAddressCollection
                                .builder()
                                .addAllowedCountry(
                                        SessionCreateParams.ShippingAddressCollection.AllowedCountry.DE
                                )
                                .addAllowedCountry(
                                        SessionCreateParams.ShippingAddressCollection.AllowedCountry.US
                                )
                                .build()
                )
                .addShippingOption(SessionCreateParams.ShippingOption.builder()
                        .setShippingRateData(SessionCreateParams.ShippingOption.ShippingRateData
                                .builder()
                                .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                                .setFixedAmount(SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount
                                        .builder()
                                        .setAmount(0L)
                                        .setCurrency("eur")
                                        .build()
                                )
                                .setDisplayName("Free shipping")
                                .setDeliveryEstimate(
                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate
                                                .builder()
                                                .setMinimum(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum
                                                                .builder()
                                                                .setUnit(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY
                                                                )
                                                                .setValue(10L)
                                                                .build()
                                                )
                                                .setMaximum(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum
                                                                .builder()
                                                                .setUnit(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.Unit.BUSINESS_DAY
                                                                )
                                                                .setValue(15L)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                        )
                        .build()
                )
                .addShippingOption(SessionCreateParams.ShippingOption.builder()
                        .setShippingRateData(SessionCreateParams.ShippingOption.ShippingRateData
                                .builder()
                                .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                                .setFixedAmount(SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount
                                        .builder()
                                        .setAmount(500L)
                                        .setCurrency("eur")
                                        .build()
                                )
                                .setDisplayName("Fast shipping")
                                .setDeliveryEstimate(
                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate
                                                .builder()
                                                .setMinimum(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum
                                                                .builder()
                                                                .setUnit(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY
                                                                )
                                                                .setValue(2L)
                                                                .build()
                                                )
                                                .setMaximum(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum
                                                                .builder()
                                                                .setUnit(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.Unit.BUSINESS_DAY
                                                                )
                                                                .setValue(4L)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                        )
                        .build()
                )
                .setSuccessUrl(frontendUrl + "completion")
                .setCancelUrl(frontendUrl + "payment").addAllLineItem(items).build();
    }

    /*
    // A basic example of how you might implement the Stripe webhook
    // Can be used for handling shipping after the order is successfully fulfilled and many others
    // Requires a publicly accessible endpoint and is therefore left out for the purposes of this project

    // You can find your endpoint's secret in your webhook settings
    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String stripeWebhookSecret;

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (JsonSyntaxException | StripeException e) {
            // Invalid payload or signature
            return ResponseEntity.badRequest().build();
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                // Then define and call a method to handle the successful payment intent.
                // handlePaymentIntentSucceeded(paymentIntent);
                break;
            case "payment_method.attached":
                PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                // Then define and call a method to handle the successful attachment of a PaymentMethod.
                // handlePaymentMethodAttached(paymentMethod);
                break;
            case "checkout.session.completed": {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();

                // Save an order in your database, marked as 'awaiting payment'
                createOrder(session);

                // Check if the order is paid (for example, from a card payment)
                //
                // A delayed notification payment will have an `unpaid` status, as
                // you're still waiting for funds to be transferred from the customer's
                // account.
                if ("paid".equals(session.getPaymentStatus())) {
                    // Fulfill the purchase
                    fulfillOrder(session);
                }

            }
            case "checkout.session.async_payment_succeeded": {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();

                // Fulfill the purchase
                fulfillOrder(session);

            }
            case "checkout.session.async_payment_failed": {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();

                // Email the customer asking them to retry their order
                emailCustomerAboutFailedPayment(session);
            }
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok().build();
    }*/

    private void createOrder(Session session) {
        // here you would create an order to be placed
        logger.info("Creating order...");
    }

    private void fulfillOrder(Session session) {
        // here you would place the order to your shipping provider
        logger.info("Fulfilling order...");
    }

    private void emailCustomerAboutFailedPayment(Session session) {
        String to = session.getCustomerEmail();
        String from = "ourEmail@gmail.com";
        String host = "localhost"; //or IP address

        //Get the session object
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        jakarta.mail.Session emailSession = jakarta.mail.Session.getDefaultInstance(properties);

        //compose the message
        try{
            MimeMessage message = new MimeMessage(emailSession);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject("Failed Payment");
            message.setText("Dear Sir/Madam, we are sorry to inform you that your payment has failed. Please retry your order or contact our customer support."
            + " Please include your email in your inquiry.");

            // Send message
            Transport.send(message);
            logger.info("Emailed customer successfully...");

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Object> handleError(StripeException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}
