package com.mrokga.carrot_server.aws.controller;

import com.mrokga.carrot_server.aws.service.AwsS3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "S3 Files", description = "S3 이미지 업로드/삭제 API(연습용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    @Operation(
            summary = "이미지 여러 개 업로드",
            description = "폼 키 이름은 **multipartFiles** 로 보내세요.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "업로드 성공 (S3 URL 리스트 반환)",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadFiles(
            @Parameter(description = "업로드할 이미지 배열 (폼 키: multipartFiles)")
            @RequestPart("multipartFiles") List<MultipartFile> multipartFiles
    ) {
        return ResponseEntity.ok(awsS3Service.uploadFile(multipartFiles));
    }

    @Operation(
            summary = "이미지 1개 업로드",
            description = "폼 키 이름은 **multipartFile** 로 보내세요.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "업로드 성공 (S3 URL 1개 반환)",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping(value = "/upload-one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadOne(
            @Parameter(description = "업로드할 이미지 (폼 키: multipartFile)")
            @RequestPart("multipartFile") MultipartFile multipartFile
    ) {
        return ResponseEntity.ok(awsS3Service.uploadFile(List.of(multipartFile)));
    }

    @Operation(
            summary = "객체 삭제",
            description = "S3 객체 key로 삭제합니다. (예: `products/uuid.jpg`)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공")
            }
    )
    @DeleteMapping
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "S3 객체 key (버킷 내 경로/파일명)")
            @RequestParam String key
    ) {
        awsS3Service.deleteFileByKey(key);
        return ResponseEntity.ok(key);
    }
}
