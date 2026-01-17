package com.k2so.watcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIIdentificationService {

    private static final Logger logger = LoggerFactory.getLogger(AIIdentificationService.class);

    @Value("${k2so.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${k2so.ai.provider:openai}")
    private String aiProvider;

    @Value("${k2so.ai.endpoint:https://api.openai.com/v1/chat/completions}")
    private String aiEndpoint;

    @Value("${k2so.ai.api-key:}")
    private String apiKey;

    @Value("${k2so.ai.model:gpt-4}")
    private String model;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public AIIdentificationService() {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public boolean isEnabled() {
        return aiEnabled && apiKey != null && !apiKey.isEmpty();
    }

    public String identifyDevice(Device device) {
        if (!isEnabled()) {
            logger.debug("AI identification is disabled or not configured");
            return null;
        }

        try {
            String prompt = buildPrompt(device);
            String response = callAI(prompt);

            if (response != null) {
                device.setAiIdentification(response);
                return response;
            }
        } catch (Exception e) {
            logger.error("Error calling AI for device identification", e);
        }

        return null;
    }

    private String buildPrompt(Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append("Identify this network device based on the following information. ");
        sb.append("Provide a brief description of what type of device this likely is, ");
        sb.append("its probable manufacturer and model if determinable, and any security considerations.\n\n");

        sb.append("Device Information:\n");
        sb.append("- MAC Address: ").append(device.getMacAddress()).append("\n");
        sb.append("- MAC Prefix: ").append(device.getMacAddress().substring(0, 8)).append("\n");

        if (device.getVendor() != null && !device.getVendor().equals("Unknown")) {
            sb.append("- Vendor: ").append(device.getVendor()).append("\n");
        }

        if (device.getHostname() != null && !device.getHostname().isEmpty()) {
            sb.append("- Hostname: ").append(device.getHostname()).append("\n");
        }

        if (device.getIpAddress() != null) {
            sb.append("- IP Address: ").append(device.getIpAddress()).append("\n");
        }

        if (device.getOpenPorts() != null && !device.getOpenPorts().isEmpty()) {
            sb.append("- Open Ports: ").append(device.getOpenPorts()).append("\n");
        }

        sb.append("\nProvide your response in a concise format (2-3 sentences max).");

        return sb.toString();
    }

    private String callAI(String prompt) {
        try {
            if ("claude".equalsIgnoreCase(aiProvider)) {
                return callClaude(prompt);
            } else {
                return callOpenAI(prompt);
            }
        } catch (Exception e) {
            logger.error("Error calling AI API", e);
            return null;
        }
    }

    private String callOpenAI(String prompt) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestBody.set("messages", messages);
        requestBody.put("max_tokens", 200);
        requestBody.put("temperature", 0.3);

        String response = webClient.post()
                .uri(aiEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response != null) {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                if (messageNode != null) {
                    return messageNode.get("content").asText();
                }
            }
        }

        return null;
    }

    private String callClaude(String prompt) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model.isEmpty() ? "claude-3-sonnet-20240229" : model);
        requestBody.put("max_tokens", 200);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestBody.set("messages", messages);

        String endpoint = aiEndpoint.isEmpty() ? "https://api.anthropic.com/v1/messages" : aiEndpoint;

        String response = webClient.post()
                .uri(endpoint)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response != null) {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                return content.get(0).get("text").asText();
            }
        }

        return null;
    }

    public DeviceType suggestDeviceType(String aiIdentification) {
        if (aiIdentification == null) {
            return DeviceType.UNKNOWN;
        }

        String lower = aiIdentification.toLowerCase();

        if (lower.contains("smartphone") || lower.contains("iphone") || lower.contains("android phone")) {
            return DeviceType.SMARTPHONE;
        }
        if (lower.contains("tablet") || lower.contains("ipad")) {
            return DeviceType.TABLET;
        }
        if (lower.contains("laptop") || lower.contains("macbook") || lower.contains("notebook")) {
            return DeviceType.LAPTOP;
        }
        if (lower.contains("desktop") || lower.contains("pc") || lower.contains("computer") || lower.contains("imac")) {
            return DeviceType.COMPUTER;
        }
        if (lower.contains("router") || lower.contains("gateway")) {
            return DeviceType.ROUTER;
        }
        if (lower.contains("switch") || lower.contains("network switch")) {
            return DeviceType.SWITCH;
        }
        if (lower.contains("access point") || lower.contains("wifi extender")) {
            return DeviceType.ACCESS_POINT;
        }
        if (lower.contains("smart tv") || lower.contains("television") || lower.contains("streaming")) {
            return DeviceType.SMART_TV;
        }
        if (lower.contains("gaming") || lower.contains("playstation") || lower.contains("xbox") || lower.contains("nintendo")) {
            return DeviceType.GAMING_CONSOLE;
        }
        if (lower.contains("printer") || lower.contains("scanner")) {
            return DeviceType.PRINTER;
        }
        if (lower.contains("camera") || lower.contains("security cam") || lower.contains("webcam")) {
            return DeviceType.CAMERA;
        }
        if (lower.contains("nas") || lower.contains("network storage")) {
            return DeviceType.NAS;
        }
        if (lower.contains("server") || lower.contains("raspberry pi")) {
            return DeviceType.SERVER;
        }
        if (lower.contains("smart home") || lower.contains("iot") || lower.contains("echo") || lower.contains("alexa") || lower.contains("google home")) {
            return DeviceType.SMART_HOME;
        }

        return DeviceType.UNKNOWN;
    }
}
