package unrn.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import unrn.app.Application;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ResponseContractGuardrailTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("PageResponse_serialization_mandatoryFieldsAndTypes")
    void PageResponse_serialization_mandatoryFieldsAndTypes() throws Exception {
        PageResponse<String> empty = PageResponse.of(List.of(), 0L, 0, 12);
        JsonNode node = objectMapper.valueToTree(empty);

        assertTrue(node.has("items"), "items field must be present");
        assertTrue(node.get("items").isArray(), "items must be an array");

        assertTrue(node.has("total"), "total field must be present");
        assertTrue(node.get("total").isNumber(), "total must be numeric");

        assertTrue(node.has("totalPages"), "totalPages field must be present");
        assertTrue(node.get("totalPages").isNumber(), "totalPages must be numeric");

        assertTrue(node.has("page"), "page field must be present");
        assertTrue(node.get("page").isNumber(), "page must be numeric");

        assertTrue(node.has("size"), "size field must be present");
        assertTrue(node.get("size").isNumber(), "size must be numeric");

        // Empty semantics: total=0 => totalPages=0
        assertEquals(0L, node.get("total").asLong(), "empty page total must be 0");
        assertEquals(0, node.get("totalPages").asInt(), "empty page totalPages must be 0");
    }

    @Test
    @DisplayName("ApiError_serialization_shapeAndTypes")
    void ApiError_serialization_shapeAndTypes() throws Exception {
        ApiErrorHandler.ApiError sample = new ApiErrorHandler.ApiError("some message", 400, "requestDesc",
                Instant.parse("2026-01-01T00:00:00Z"));

        JsonNode node = objectMapper.valueToTree(sample);

        assertTrue(node.has("message"), "message field must be present");
        assertTrue(node.get("message").isTextual(), "message must be a string");

        assertTrue(node.has("status"), "status field must be present");
        assertTrue(node.get("status").isNumber(), "status must be numeric");

        assertTrue(node.has("path"), "path field must be present");
        assertTrue(node.get("path").isTextual(), "path must be a string");

        assertTrue(node.has("timestamp"), "timestamp field must be present");
        // timestamp serialized by Jackson for Instant is usually textual ISO string
        assertTrue(node.get("timestamp").isTextual() || node.get("timestamp").isNumber(),
                "timestamp must be textual or numeric (configured serialization)");
    }
}
