package com.payment.service.data;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.Data;

import java.util.List;

@Data
public class ChargeRequest {
    private List<Pair<String, Integer>> idAndQuantity;
    private String stripeToken;
}
