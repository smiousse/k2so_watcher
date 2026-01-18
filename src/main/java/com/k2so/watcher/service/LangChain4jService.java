package com.k2so.watcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k2so.watcher.dto.DeviceAnalysisResult;
import com.k2so.watcher.model.Device;
import com.k2so.watcher.model.DeviceType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class LangChain4jService {

    private static final Logger logger = LoggerFactory.getLogger(LangChain4jService.class);

    private final AppSettingsService settingsService;
    private final ObjectMapper objectMapper;

    public LangChain4jService(AppSettingsService settingsService) {
        this.settingsService = settingsService;
        this.objectMapper = new ObjectMapper();
    }

    public boolean isConfigured() {
        String provider = settingsService.getSettingValue("langchain4j.provider", "gemini");
        if ("groq".equals(provider)) {
            String apiKey = settingsService.getSettingValue("langchain4j.groq.api-key", "");
            return apiKey != null && !apiKey.isEmpty();
        } else {
            String apiKey = settingsService.getSettingValue("langchain4j.gemini.api-key", "");
            return apiKey != null && !apiKey.isEmpty();
        }
    }

    public DeviceAnalysisResult analyzeDeepScanLog(Device device) {
        if (device.getDeepScanLog() == null || device.getDeepScanLog().isEmpty()) {
            throw new IllegalArgumentException("Device has no deep scan log to analyze");
        }

        String provider = settingsService.getSettingValue("langchain4j.provider", "gemini");
        ChatLanguageModel model = buildModel(provider);

        String prompt = buildAnalysisPrompt(device);
        logger.info("Sending deep scan log analysis request to {} for device {}", provider, device.getMacAddress());

        String response = model.generate(prompt);
        logger.debug("AI response: {}", response);

        return parseAnalysisResult(response);
    }

    private ChatLanguageModel buildModel(String provider) {
        if ("groq".equals(provider)) {
            String apiKey = settingsService.getSettingValue("langchain4j.groq.api-key", "");
            String modelName = settingsService.getSettingValue("langchain4j.groq.model", "llama-3.3-70b-versatile");

            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Groq API key is not configured");
            }

            return OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
        } else {
            String apiKey = settingsService.getSettingValue("langchain4j.gemini.api-key", "");
            String modelName = settingsService.getSettingValue("langchain4j.gemini.model", "gemini-2.0-flash");

            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Gemini API key is not configured");
            }

            return GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
        }
    }

    private String buildAnalysisPrompt(Device device) {
        String deviceTypes = String.join(", ",
            Arrays.stream(DeviceType.values())
                .map(DeviceType::name)
                .toArray(String[]::new));

        return String.format("""
            Analyze this network device deep scan log and provide:
            1. Device type (must be one of: %s)
            2. Suggested name for this device
            3. Brief notes about the device (OS, services, purpose)

            Device Info:
            - MAC: %s
            - IP: %s
            - Vendor: %s
            - Current hostname: %s

            Deep Scan Log:
            %s

            Respond ONLY with a valid JSON object in this exact format (no markdown, no code blocks):
            {"deviceType": "...", "suggestedName": "...", "notes": "..."}
            """,
            deviceTypes,
            device.getMacAddress(),
            device.getIpAddress() != null ? device.getIpAddress() : "Unknown",
            device.getVendor() != null ? device.getVendor() : "Unknown",
            device.getHostname() != null ? device.getHostname() : "Unknown",
            device.getDeepScanLog()
        );
    }

    private DeviceAnalysisResult parseAnalysisResult(String response) {
        DeviceAnalysisResult result = new DeviceAnalysisResult();

        try {
            // Extract JSON from response (handle markdown code blocks if present)
            String jsonStr = response.trim();
            if (jsonStr.contains("```")) {
                int start = jsonStr.indexOf("{");
                int end = jsonStr.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    jsonStr = jsonStr.substring(start, end);
                }
            }

            JsonNode json = objectMapper.readTree(jsonStr);

            // Parse device type
            if (json.has("deviceType")) {
                String typeStr = json.get("deviceType").asText().toUpperCase();
                try {
                    result.setDeviceType(DeviceType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown device type from AI: {}, defaulting to UNKNOWN", typeStr);
                    result.setDeviceType(DeviceType.UNKNOWN);
                }
            }

            // Parse suggested name
            if (json.has("suggestedName")) {
                result.setSuggestedName(json.get("suggestedName").asText());
            }

            // Parse notes
            if (json.has("notes")) {
                result.setNotes(json.get("notes").asText());
            }

        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", response, e);
            // Return partial result with notes containing the raw response
            result.setDeviceType(DeviceType.UNKNOWN);
            result.setNotes("AI analysis (raw): " + response);
        }

        return result;
    }
}
