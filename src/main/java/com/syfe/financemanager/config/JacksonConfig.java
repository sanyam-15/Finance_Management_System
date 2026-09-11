package com.syfe.financemanager.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Custom Jackson serializer that matches official e2e numeric formatting.
 */
@JsonComponent
public class JacksonConfig extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            String fieldName = gen.getOutputContext().getCurrentName();
            if ("progressPercentage".equals(fieldName)) {
                gen.writeNumber(value);
            } else if (value == 0.0 && ("netSavings".equals(fieldName) || "currentProgress".equals(fieldName))) {
                gen.writeRawValue("0");
            } else {
                gen.writeNumber(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
            }
        } else {
            gen.writeNull();
        }
    }
    
    @Override
    public Class<Double> handledType() {
        return Double.class;
    }
}
