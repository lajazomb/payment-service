package com.payment.service.data;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class ChargeRequest {
    private List<ChargeEntry> chargeEntries;
}
