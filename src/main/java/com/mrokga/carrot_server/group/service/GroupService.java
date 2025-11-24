package com.mrokga.carrot_server.group.service;

import com.mrokga.carrot_server.group.dto.*;
import com.mrokga.carrot_server.group.entity.*;
import com.mrokga.carrot_server.group.repository.*;
import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.region.repository.RegionRepository;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupJoinRequestRepository joinRequestRepository;
    private final GroupPostRepository postRepository;
    private final GroupEventRepository eventRepository;
    private final GroupEventRsvpRepository rsvpRepository;

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    // ===== Group =====
    @Transactional
    public Group createGroup(GroupCreateRequest req, int userId) {
        User owner = userRepository.findById(userId).orElseThrow();
        Region region = regionRepository.findById(req.getRegionId()).orElseThrow();

        Group g = Group.builder()
                .owner(owner)
                .region(region)
                .name(req.getName())
                .description(req.getDescription())
                .coverImageUrl(req.getCoverImageUrl())
                .visibility(req.getVisibility())
                .joinPolicy(req.getJoinPolicy())
                .maxMembers(req.getMaxMembers())
                .createdAt(LocalDateTime.now())
                .build();
        groupRepository.save(g);

        membershipRepository.save(GroupMembership.builder()
                .group(g).user(owner)
                .role(GroupMembership.Role.OWNER)
                .isActive(true).build());

        return g;
    }

    @Transactional
    public GroupResponse createGroupAndReturn(Integer userId, GroupCreateRequest req) {
        User owner = userRepository.findById(userId).orElseThrow();
        Region region = regionRepository.findById(req.getRegionId()).orElseThrow();

        Group g = Group.builder()
                .owner(owner)
                .region(region)
                .name(req.getName())
                .description(req.getDescription())
                .coverImageUrl(req.getCoverImageUrl())
                .visibility(req.getVisibility())
                .joinPolicy(req.getJoinPolicy())
                .maxMembers(req.getMaxMembers())
                .createdAt(LocalDateTime.now())
                .build();
        groupRepository.save(g);

        membershipRepository.save(GroupMembership.builder()
                .group(g).user(owner)
                .role(GroupMembership.Role.OWNER)
                .isActive(true).build());

        // 생성 트랜잭션 안에서 안전하게 DTO 매핑
        return toResponse(g, userId);
    }

    // toResponse 그대로 사용 (fetch join/동일 트랜잭션에서만 호출)
    public GroupResponse toResponse(Group g, Integer meId) {
        long mc = membershipRepository.countByGroupIdAndIsActiveTrue(g.getId());
        var mem = (meId == null) ? null :
                membershipRepository.findByGroupIdAndUserId(g.getId(), meId).orElse(null);

        return GroupResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .regionId(g.getRegion().getId())
                .regionName(g.getRegion().getName())
                .coverImageUrl(g.getCoverImageUrl())
                .visibility(g.getVisibility().name())
                .joinPolicy(g.getJoinPolicy().name())
                .memberCount((int) mc)
                .maxMembers(g.getMaxMembers())
                .member(mem != null)
                .role(mem != null ? mem.getRole().name() : "NONE")
                .createdAt(g.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public GroupResponse getDetailResponse(Integer groupId, Integer meId) {
        Group g = groupRepository.findWithOwnerAndRegionById(groupId)
                .orElseThrow();
        return toResponse(g, meId); // region/owner 이미 초기화됨
    }

    // ===== Membership & Join =====
    @Transactional
    public void requestJoin(Integer groupId, Integer userId, String message) {
        Group g = groupRepository.findById(groupId).orElseThrow();
        User u = userRepository.findById(userId).orElseThrow();

        if (membershipRepository.existsByGroupIdAndUserId(groupId, userId)) return;

        if (g.getJoinPolicy() == Group.JoinPolicy.OPEN) {
            membershipRepository.save(GroupMembership.builder()
                    .group(g).user(u).role(GroupMembership.Role.MEMBER).isActive(true).build());
            return;
        }
        // APPROVAL or INVITE_ONLY -> 요청
        joinRequestRepository.save(
                GroupJoinRequest.builder()
                        .group(g).user(u).status(GroupJoinRequest.Status.PENDING)
                        .message(message).build()
        );
    }

    @Transactional
    public void leave(Integer groupId, Integer userId) {
        var mem = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow();
        // OWNER 탈퇴 제한(실서비스는 위임 필요)
        if (mem.getRole() == GroupMembership.Role.OWNER) {
            throw new IllegalStateException("그룹 소유자는 바로 탈퇴할 수 없습니다.");
        }
        membershipRepository.delete(mem);
    }

    @Transactional
    public void approveJoin(Integer groupId, Integer reqId, Integer actorId, boolean approve) {
        Group g = groupRepository.findById(groupId).orElseThrow();
        var actor = membershipRepository.findByGroupIdAndUserId(groupId, actorId)
                .orElseThrow();
        if (actor.getRole() == GroupMembership.Role.MEMBER) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        var req = joinRequestRepository.findById(reqId).orElseThrow();
        if (!req.getGroup().getId().equals(groupId)) throw new IllegalArgumentException();

        if (approve) {
            req.setStatus(GroupJoinRequest.Status.APPROVED);
            joinRequestRepository.save(req);
            membershipRepository.save(
                    GroupMembership.builder()
                            .group(g).user(req.getUser())
                            .role(GroupMembership.Role.MEMBER).isActive(true).build()
            );
        } else {
            req.setStatus(GroupJoinRequest.Status.REJECTED);
            joinRequestRepository.save(req);
        }
    }

    // ===== Post =====
    @Transactional
    public GroupPost createPost(Integer groupId, Integer authorId, GroupPostCreateRequest req) {
        var g = groupRepository.findById(groupId).orElseThrow();
        var mem = membershipRepository.findByGroupIdAndUserId(groupId, authorId)
                .orElseThrow();
        if (!mem.getIsActive()) throw new IllegalStateException("탈퇴 회원입니다.");

        var author = mem.getUser();
        GroupPost.Type type = GroupPost.Type.valueOf(req.getType());
        GroupPost post = GroupPost.builder()
                .group(g).author(author)
                .type(type).content(req.getContent()).imageUrl(req.getImageUrl())
                .build();
        return postRepository.save(post);
    }

    // ===== Event & RSVP =====
    @Transactional
    public GroupEvent createEvent(Integer groupId, Integer creatorId, GroupEventCreateRequest req) {
        var g = groupRepository.findById(groupId).orElseThrow();
        var mem = membershipRepository.findByGroupIdAndUserId(groupId, creatorId)
                .orElseThrow();

        var ev = GroupEvent.builder()
                .group(g).creator(mem.getUser())
                .title(req.getTitle()).description(req.getDescription())
                .location(new com.mrokga.carrot_server.region.entity.embeddable.Location(
                        req.getLocation().getLatitude(),
                        req.getLocation().getLongitude(),
                        req.getLocation().getName()
                ))
                .startAt(req.getStartAt()).endAt(req.getEndAt())
                .capacity(req.getCapacity()).fee(req.getFee())
                .build();
        return eventRepository.save(ev);
    }

    @Transactional
    public void rsvp(Integer eventId, Integer userId, GroupEventRsvp.Status status) {
        var ev = eventRepository.findById(eventId).orElseThrow();
        var mem = membershipRepository.findByGroupIdAndUserId(ev.getGroup().getId(), userId)
                .orElseThrow();

        var ex = rsvpRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (ex == null) {
            rsvpRepository.save(GroupEventRsvp.builder()
                    .event(ev).user(mem.getUser()).status(status).build());
        } else {
            ex.setStatus(status);
            rsvpRepository.save(ex);
        }
    }

    public GroupEventResponse toEventResponse(GroupEvent ev, Integer meId) {
        long going = rsvpRepository.countByEventIdAndStatus(ev.getId(), GroupEventRsvp.Status.GOING);
        long interested = rsvpRepository.countByEventIdAndStatus(ev.getId(), GroupEventRsvp.Status.INTERESTED);

        String my = "NONE";
        if (meId != null) {
            var mine = rsvpRepository.findByEventIdAndUserId(ev.getId(), meId).orElse(null);
            if (mine != null) my = mine.getStatus().name();
        }

        return GroupEventResponse.builder()
                .id(ev.getId()).groupId(ev.getGroup().getId())
                .title(ev.getTitle()).description(ev.getDescription())
                .location(new com.mrokga.carrot_server.region.dto.LocationDto() {{
                    setLatitude(ev.getLocation().getLatitude());
                    setLongitude(ev.getLocation().getLongitude());
                    setName(ev.getLocation().getName());
                }})
                .startAt(ev.getStartAt()).endAt(ev.getEndAt())
                .capacity(ev.getCapacity()).fee(ev.getFee())
                .goingCount(going).interestedCount(interested)
                .myRsvp(my)
                .build();
    }



}
