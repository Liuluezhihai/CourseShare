package com.courseshare.log.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class LogParamsSerializer {

    private static final Set<String> SENSITIVE_PARTS = Set.of(
            "password", "token", "secret", "session", "authorization"
    );
    private static final int MAX_LENGTH = 4000;

    private final ObjectMapper objectMapper;

    public LogParamsSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object[] args) {
        List<Object> safe = new ArrayList<>();
        for (Object argument : args) {
            if (argument instanceof ServletRequest || argument instanceof ServletResponse) {
                continue;
            }
            if (argument instanceof MultipartFile file) {
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("fileName", file.getOriginalFilename());
                metadata.put("contentType", file.getContentType());
                metadata.put("size", file.getSize());
                safe.add(metadata);
                continue;
            }
            safe.add(argument);
        }
        try {
            JsonNode node = objectMapper.valueToTree(safe);
            mask(node);
            String json = objectMapper.writeValueAsString(node);
            return json.length() <= MAX_LENGTH ? json : json.substring(0, MAX_LENGTH);
        } catch (Exception exception) {
            return "[参数序列化失败]";
        }
    }

    private void mask(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey().toLowerCase(Locale.ROOT);
                boolean sensitive = SENSITIVE_PARTS.stream().anyMatch(key::contains);
                if (sensitive) {
                    objectNode.put(field.getKey(), "***");
                } else {
                    mask(field.getValue());
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::mask);
        }
    }
}

