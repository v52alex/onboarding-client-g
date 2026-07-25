package com.v52alex.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class WorkflowCatalogTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loadsAValidWorkflow() throws Exception {
        WorkflowCatalog catalog = catalog(validWorkflow("sample"));

        catalog.load();

        assertThat(catalog.require("sample").initialStep()).isEqualTo("start");
    }

    @Test
    void rejectsTransitionsToUnknownSteps() {
        String invalid = validWorkflow("sample")
            .replace("\"transitions\": {\"success\": \"done\"}",
                "\"transitions\": {\"success\": \"missing\"}");
        WorkflowCatalog catalog = catalog(invalid);

        assertThatThrownBy(catalog::load)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unknown workflow step");
    }

    @Test
    void rejectsDuplicatedWorkflowKeys() {
        ByteArrayResource first = resource(validWorkflow("sample"));
        ByteArrayResource second = resource(validWorkflow("sample"));
        WorkflowCatalog catalog = new WorkflowCatalog(
            objectMapper,
            new ByteArrayResource[]{first, second}
        );

        assertThatThrownBy(catalog::load)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicated workflow key");
    }

    @Test
    void reportsUnknownWorkflows() throws Exception {
        WorkflowCatalog catalog = catalog(validWorkflow("sample"));
        catalog.load();

        assertThatThrownBy(() -> catalog.require("unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown workflow");
    }

    private WorkflowCatalog catalog(String json) {
        return new WorkflowCatalog(objectMapper, new ByteArrayResource[]{resource(json)});
    }

    private ByteArrayResource resource(String json) {
        return new ByteArrayResource(json.getBytes());
    }

    private String validWorkflow(String key) {
        return """
            {
              "key": "%s",
              "version": 1,
              "initialStep": "start",
              "steps": [
                {
                  "name": "start",
                  "action": "submit",
                  "terminal": false,
                  "transitions": {"success": "done"}
                },
                {
                  "name": "done",
                  "action": null,
                  "terminal": true,
                  "transitions": {}
                }
              ]
            }
            """.formatted(key);
    }
}
