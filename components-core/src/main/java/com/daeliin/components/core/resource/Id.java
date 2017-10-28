package com.daeliin.components.core.resource;

import org.apache.commons.lang3.RandomStringUtils;

public final class Id {

    public final String value;

    public Id() {
        value = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    }
}