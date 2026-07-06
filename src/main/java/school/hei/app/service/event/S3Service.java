package school.hei.app.service.event;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
public class S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName;
  private final Long presignedUrlDurationSeconds;

  public S3Service(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${aws.s3.bucket}") String bucketName,
      @Value("${aws.s3.presigned-url-duration:86400}") Long presignedUrlDurationSeconds) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
    this.presignedUrlDurationSeconds = presignedUrlDurationSeconds;
  }

  public String uploadPdfAndGenerateUrl(byte[] pdfContent, String fileName, String userId) {
    try {
      String key = generateObjectKey(userId, fileName);

      PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType("application/pdf")
              .metadata(
                  Map.of(
                      "userId", userId,
                      "fileName", fileName,
                      "uploadedAt", String.valueOf(System.currentTimeMillis())))
              .build();

      s3Client.putObject(
          putRequest,
          RequestBody.fromInputStream(new ByteArrayInputStream(pdfContent), pdfContent.length));

      log.info("PDF uploaded to S3: {}", key);

      Duration duration = Duration.ofSeconds(presignedUrlDurationSeconds);

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(duration)
              .getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key(key).build())
              .build();

      PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
      String presignedUrl = presignedRequest.url().toString();

      log.info(" Pre-signed URL generated, valid for {} seconds", presignedUrlDurationSeconds);

      return presignedUrl;

    } catch (S3Exception e) {
      log.error("S3 error: {}", e.getMessage());
      throw new RuntimeException("Failed to upload PDF to S3", e);
    } catch (Exception e) {
      log.error("Error: ", e);
      throw new RuntimeException("Failed to generate PDF link", e);
    }
  }

  private String generateObjectKey(String userId, String fileName) {
    String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    return String.format(
        "certificats/%s/%s_%s.pdf", userId, System.currentTimeMillis(), cleanFileName);
  }
}
