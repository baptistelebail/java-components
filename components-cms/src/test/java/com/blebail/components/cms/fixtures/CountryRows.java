package com.blebail.components.cms.fixtures;

import com.blebail.components.cms.sql.BCountry;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class CountryRows {

    public static BCountry france() {
        return new BCountry(
                "FR",
                LocalDateTime.of(2017, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC),
                "France");
    }

    public static BCountry belgium() {
        return new BCountry(
                "BE",
                LocalDateTime.of(2017, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC),
                "Belgium");
    }
}
