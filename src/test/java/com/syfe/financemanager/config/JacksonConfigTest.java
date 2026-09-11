package com.syfe.financemanager.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JacksonConfigTest {

    private final JacksonConfig serializer = new JacksonConfig();

    @Test
    void handledTypeIsDouble() {
        assertEquals(Double.class, serializer.handledType());
    }

    @Test
    void writesNull() throws IOException {
        JsonGenerator gen = mock(JsonGenerator.class);
        serializer.serialize(null, gen, mock(SerializerProvider.class));
        verify(gen).writeNull();
    }

    @Test
    void writesProgressPercentageUnscaled() throws IOException {
        JsonGenerator gen = generatorWithField("progressPercentage");
        serializer.serialize(16.67, gen, mock(SerializerProvider.class));
        verify(gen).writeNumber(16.67);
    }

    @Test
    void writesZeroNetSavingsAsRawZero() throws IOException {
        JsonGenerator gen = generatorWithField("netSavings");
        serializer.serialize(0.0, gen, mock(SerializerProvider.class));
        verify(gen).writeRawValue("0");
    }

    @Test
    void writesZeroCurrentProgressAsRawZero() throws IOException {
        JsonGenerator gen = generatorWithField("currentProgress");
        serializer.serialize(0.0, gen, mock(SerializerProvider.class));
        verify(gen).writeRawValue("0");
    }

    @Test
    void writesScaledAmount() throws IOException {
        JsonGenerator gen = generatorWithField("amount");
        serializer.serialize(50.1, gen, mock(SerializerProvider.class));
        verify(gen).writeNumber(java.math.BigDecimal.valueOf(50.1).setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private JsonGenerator generatorWithField(String field) {
        JsonGenerator gen = mock(JsonGenerator.class);
        JsonStreamContext context = mock(JsonStreamContext.class);
        when(gen.getOutputContext()).thenReturn(context);
        when(context.getCurrentName()).thenReturn(field);
        return gen;
    }
}
