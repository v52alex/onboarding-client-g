package com.v52alex.onboarding.integration.content;

import java.time.Instant;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ContentStorageClient {

    StoredContent upload(UUID caseId, MultipartFile file, String actorId);

    byte[] download(UUID caseId, UUID contentId);

    void delete(UUID caseId, UUID contentId);

    record StoredContent(
        UUID id, String originalName, String contentType, long sizeBytes,
        String checksumSha256, String status, Instant createdAt
    ) {
    }
}
