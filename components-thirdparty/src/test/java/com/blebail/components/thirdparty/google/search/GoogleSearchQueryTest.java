package com.blebail.components.thirdparty.google.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class GoogleSearchQueryTest {

    @Test
    public void maxResultsShouldBe10ByDefault() {
        assertThat(new GoogleSearchQuery("test").maxResults).isEqualTo(10);
    }
}