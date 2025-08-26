package com.stylemycloset.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class FlexibleStringListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return List.of();
        }

        JsonNode node = p.readValueAsTree();
        if (node == null || node instanceof NullNode) {
            return List.of();
        }

        Set<String> set = new LinkedHashSet<>();

        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode n : arrayNode) {
                if (n == null || n.isNull()) continue;
                String v = n.asText(null);
                if (v != null && !v.isBlank()) set.add(v.strip());
            }
        } else if (node instanceof TextNode textNode) {
            String raw = textNode.asText("");
            if (!raw.isBlank()) {

                String[] parts = raw.split(",");
                for (String part : parts) {
                    if (part != null && !part.isBlank()) set.add(part.strip());
                }
            }
        } else {

            String raw = node.asText("");
            if (!raw.isBlank()) {
                String[] parts = raw.split(",");
                for (String part : parts) {
                    if (part != null && !part.isBlank()) set.add(part.strip());
                }
            }
        }

        return new ArrayList<>(set);
    }
}


