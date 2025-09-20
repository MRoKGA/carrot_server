package com.mrokga.carrot_server.Aws.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 s3;

    public String uploadOne(MultipartFile file) {
        return uploadFile(List.of(file)).get(0);
    }

    public List<String> uploadFile(List<MultipartFile> multipartFiles) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            String key = createFileName(file.getOriginalFilename());

            ObjectMetadata om = new ObjectMetadata();
            om.setContentLength(file.getSize());
            om.setContentType(file.getContentType());

            try (InputStream in = file.getInputStream()) {
                s3.putObject(new PutObjectRequest(bucket, key, in, om)
                        .withCannedAcl(CannedAccessControlList.PublicRead)); // 퍼블릭 버킷이 아니면 제거
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패", e);
            }

            String url = s3.getUrl(bucket, key).toString();
            urls.add(url);
        }
        return urls;
    }

    public void deleteFileByKey(String key) {
        s3.deleteObject(bucket, key);
    }

    public void deleteFileByUrl(String url) {
        String key = extractKeyFromUrl(url);
        if (key != null && !key.isBlank()) {
            deleteFileByKey(key);
        }
    }

    /** 예: https://{bucket}.s3.{region}.amazonaws.com/products/abc.jpg → products/abc.jpg */
    public String extractKeyFromUrl(String url) {
        try {
            URI u = URI.create(url);
            // 경로 맨 앞의 '/' 제거
            String path = u.getPath();
            if (path != null && path.startsWith("/")) path = path.substring(1);
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    private String createFileName(String original) {
        return UUID.randomUUID() + getFileExtension(original);
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        if (idx < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일 형식(" + fileName + ")");
        }
        return fileName.substring(idx);
    }
}
