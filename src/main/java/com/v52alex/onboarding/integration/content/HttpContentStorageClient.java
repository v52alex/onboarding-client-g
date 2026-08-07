package com.v52alex.onboarding.integration.content;

import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
class HttpContentStorageClient implements ContentStorageClient {

    private final RestClient client;
    private final String serviceToken;

    HttpContentStorageClient(
        @Value("${onboarding.integrations.content.base-url}") String baseUrl,
        @Value("${onboarding.integrations.content.internal-token}") String serviceToken
    ) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.serviceToken = serviceToken;
    }

    @Override
    public StoredContent upload(UUID caseId, MultipartFile file, String actorId) {
        try {
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
            HttpEntity<NamedByteArrayResource> filePart = new HttpEntity<>(
                new NamedByteArrayResource(file.getBytes(), file.getOriginalFilename()), fileHeaders);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", filePart);
            return client.post()
                .uri(uri -> uri.path("/api/content/v1/documents")
                    .queryParam("applicationId", caseId).build())
                .header("X-Content-Service-Token", serviceToken)
                .header("X-Actor-Id", actorId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(StoredContent.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Document cannot be read for upload", exception);
        }
    }

    @Override
    public byte[] download(UUID caseId, UUID contentId) {
        return client.get()
            .uri(uri -> uri.path("/api/content/v1/documents/{id}/content")
                .queryParam("applicationId", caseId).build(contentId))
            .header("X-Content-Service-Token", serviceToken)
            .retrieve()
            .body(byte[].class);
    }

    @Override
    public void delete(UUID caseId, UUID contentId) {
        client.delete()
            .uri(uri -> uri.path("/api/content/v1/documents/{id}")
                .queryParam("applicationId", caseId).build(contentId))
            .header("X-Content-Service-Token", serviceToken)
            .retrieve()
            .toBodilessEntity();
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] content, String filename) {
            super(content);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
