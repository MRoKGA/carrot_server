package com.mrokga.carrot_server.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.aws.service.AwsS3Service;
import com.mrokga.carrot_server.group.dto.*;
import com.mrokga.carrot_server.group.entity.*;
import com.mrokga.carrot_server.group.repository.*;
import com.mrokga.carrot_server.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "동네모임(Group)", description = "동네모임(그룹) 생성/조회/가입/게시글/이벤트 API")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupPostRepository postRepository;
    private final GroupEventRepository eventRepository;
    private final GroupJoinRequestRepository joinRequestRepository;
    private final AwsS3Service awsS3Service;

    // === 인증 유틸 ===
    private Integer meOrNull() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) return null;
        return Integer.valueOf(a.getName());
    }

    private Integer me() {
        Integer id = meOrNull();
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return id;
    }

    // === Group ===
    @Operation(
            summary = "동네모임 생성",
            description = "새로운 동네모임을 생성합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GroupCreateRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "name": "회기동 아침조깅 모임",
                              "description": "매주 토·일 오전 7시에 회기역 근처에서 함께 조깅해요!",
                              "regionId": 366,
                              "coverImageUrl": "https://cdn.example.com/groups/jogging.jpg",
                              "visibility": "PUBLIC",
                              "joinPolicy": "APPROVAL",
                              "maxMembers": 50
                            }
                            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = GroupResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "id": 123,
                              "name": "회기동 아침조깅 모임",
                              "description": "매주 토·일 오전 7시에 회기역 근처에서 함께 조깅해요!",
                              "regionId": 366,
                              "regionName": "회기동",
                              "coverImageUrl": "https://cdn.example.com/groups/jogging.jpg",
                              "visibility": "PUBLIC",
                              "joinPolicy": "APPROVAL",
                              "memberCount": 1,
                              "maxMembers": 50,
                              "member": true,
                              "role": "OWNER",
                              "createdAt": "2025-10-29T08:30:00"
                            }
                            """))
            )
    })
    @PostMapping
    public GroupResponse create(@RequestBody GroupCreateRequest req) {
        return groupService.createGroupAndReturn(me(), req);
    }

    @Operation(
            summary = "동네모임 목록",
            description = """
                동네모임 목록을 페이징으로 조회합니다.
                - regionId가 있으면 해당 지역 PUBLIC 그룹만 조회
                - q가 있으면 이름 부분검색
                - 기본은 전체 그룹 페이징
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GroupResponse.class)),
                            examples = @ExampleObject(value = """
                            {
                              "content": [
                                {
                                  "id": 1,
                                  "name": "회기동 러닝",
                                  "description": "...",
                                  "regionId": 366,
                                  "regionName": "회기동",
                                  "coverImageUrl": null,
                                  "visibility": "PUBLIC",
                                  "joinPolicy": "OPEN",
                                  "memberCount": 18,
                                  "maxMembers": 50,
                                  "member": false,
                                  "role": "NONE",
                                  "createdAt": "2025-10-29T08:00:00"
                                }
                              ],
                              "pageable": { "pageNumber": 0, "pageSize": 20 },
                              "totalElements": 1,
                              "totalPages": 1,
                              "last": true
                            }
                            """))
            )
    })
    @GetMapping
    public Page<GroupResponse> list(
            @Parameter(description = "지역 ID(옵션)", example = "366")
            @RequestParam(required = false) Integer regionId,
            @Parameter(description = "이름 검색어(옵션)", example = "조깅")
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(size = 20) Pageable pg
    ) {
        Page<Group> page;
        if (q != null && !q.isBlank()) page = groupRepository.findByNameContainingIgnoreCase(q, pg);
        else if (regionId != null) page = groupRepository.findByRegionIdAndVisibility(regionId, Group.Visibility.PUBLIC, pg);
        else page = groupRepository.findAll(pg);

        final Integer meId = meOrNull();
        return page.map(g -> groupService.toResponse(g, meId));
    }

    @Operation(summary = "동네모임 상세", description = "그룹 단건 상세를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "404", description = "없음")
    })
    @GetMapping("/{id}")
    public GroupResponse detail(@Parameter(description = "그룹 ID", example = "123") @PathVariable Integer id) {
        return groupService.getDetailResponse(id, meOrNull());
    }

    // === Join / Leave ===
    @Operation(
            summary = "그룹 가입 요청",
            description = "가입정책이 APPROVAL인 경우 승인 대기 요청을 생성합니다. OPEN이면 즉시 가입 처리할 수 있습니다.",
            requestBody = @RequestBody(content = @Content(
                    schema = @Schema(implementation = GroupJoinRequestCreateRequest.class)
            ))
    )
    @PostMapping("/{id}/join")
    public ResponseEntity<?> requestJoin(@PathVariable Integer id,
                                         @org.springframework.web.bind.annotation.RequestBody(required = false) GroupJoinRequestCreateRequest body) {
        groupService.requestJoin(id, me(), body != null ? body.getMessage() : null);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 탈퇴", description = "내 membership을 비활성화합니다.")
    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable Integer id) {
        groupService.leave(id, me());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가입 요청 목록", description = "OWNER/MANAGER만 조회 가능. status=PENDING/APPROVED/REJECTED")
    @GetMapping("/{id}/requests")
    public Page<GroupJoinRequestResponse> requests(@PathVariable Integer id,
                                                   @RequestParam(defaultValue = "PENDING") String status,
                                                   @ParameterObject @PageableDefault(size = 20) Pageable pg) {

        var actor = membershipRepository.findByGroupIdAndUserId(id, me())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (actor.getRole() == GroupMembership.Role.MEMBER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return groupService.listJoinRequests(id, GroupJoinRequest.Status.valueOf(status), pg);
    }

    @Operation(summary = "가입 승인", description = "OWNER/MANAGER 권한 필요")
    @PostMapping("/{id}/requests/{reqId}/approve")
    public ResponseEntity<?> approve(@PathVariable Integer id, @PathVariable Integer reqId) {
        groupService.approveJoin(id, reqId, me(), true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가입 거절", description = "OWNER/MANAGER 권한 필요")
    @PostMapping("/{id}/requests/{reqId}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id, @PathVariable Integer reqId) {
        groupService.approveJoin(id, reqId, me(), false);
        return ResponseEntity.ok().build();
    }

    // === Posts ===
    @Operation(summary = "그룹 게시글 목록", description = "그룹 게시글을 최신순으로 페이징 조회")
    @GetMapping("/{id}/posts")
    public Page<GroupPost> posts(@PathVariable Integer id, @ParameterObject @PageableDefault(size = 20) Pageable pg) {
        return postRepository.findByGroupIdOrderByIdDesc(id, pg);
    }

    @Operation(summary = "그룹 게시글 등록")
    @PostMapping("/{id}/posts")
    public GroupPost createPost(@PathVariable Integer id, @org.springframework.web.bind.annotation.RequestBody GroupPostCreateRequest req) {
        return groupService.createPost(id, me(), req);
    }

    // === Events ===
    @Operation(summary = "그룹 이벤트 목록", description = "현재 시각 이후 이벤트만 시작시간 오름차순으로 페이징")
    @GetMapping("/{id}/events")
    public Page<GroupEventResponse> events(@PathVariable Integer id, @ParameterObject @PageableDefault(size = 20) Pageable pg) {
        var now = LocalDateTime.now();
        Integer meId = meOrNull();
        return eventRepository.findByGroupIdAndStartAtAfterOrderByStartAtAsc(id, now, pg)
                .map(ev -> groupService.toEventResponse(ev, meId));
    }

    @Operation(summary = "그룹 이벤트 생성")
    @PostMapping("/{id}/events")
    public GroupEventResponse createEvent(@PathVariable Integer id, @org.springframework.web.bind.annotation.RequestBody GroupEventCreateRequest req) {
        var ev = groupService.createEvent(id, me(), req);
        return groupService.toEventResponse(ev, me());
    }

    @Operation(summary = "이벤트 RSVP", description = "RSVP 상태를 설정합니다. status=GOING/INTERESTED/CANCELLED")
    @PostMapping("/events/{eventId}/rsvp")
    public ResponseEntity<?> rsvp(@PathVariable Integer eventId, @RequestParam String status) {
        groupService.rsvp(eventId, me(), GroupEventRsvp.Status.valueOf(status));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "동네모임 생성(멀티파트: JSON + 표지 이미지 파일)",
            description = """
            폼 파트:
            - meta: application/json (GroupCreateRequest)
            - cover: file (선택, 이미지 1장)
            업로드된 이미지는 S3 URL로 변환되어 meta.coverImageUrl에 주입됩니다.
            예시(meta):
            {
              "name": "회기동 아침조깅 모임",
              "description": "매주 토·일 오전 7시에 회기역 근처에서 함께 조깅해요!",
              "regionId": 366,
              "visibility": "PUBLIC",
              "joinPolicy": "APPROVAL",
              "maxMembers": 50
            }
            """
    )
    public GroupResponse createMultipart(
            @org.springframework.web.bind.annotation.RequestPart("meta") String metaJson,
            @org.springframework.web.bind.annotation.RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws Exception {

        // 1) JSON 문자열 → DTO
        GroupCreateRequest meta = new ObjectMapper().readValue(metaJson, GroupCreateRequest.class);

        // 2) 파일 검증 + 업로드 → URL 주입
        if (cover != null && !cover.isEmpty()) {
            String ct = cover.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
            }

            // AwsS3Service에 단건 업로드가 없다면, 리스트로 랩핑 후 첫 URL 사용
            String url = awsS3Service.uploadFile(java.util.List.of(cover)).get(0);
            meta.setCoverImageUrl(url);
        }

        // 3) 서비스 위임 (기존 로직 재사용)
        return groupService.createGroupAndReturn(me(), meta);
    }

}
