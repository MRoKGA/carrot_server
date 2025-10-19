package com.mrokga.carrot_server.chat.service;

import com.mrokga.carrot_server.chat.dto.response.QuickReplyAddResponseDto;
import com.mrokga.carrot_server.chat.dto.response.QuickReplyItemResponseDto;
import com.mrokga.carrot_server.chat.entity.ChatMessage;
import com.mrokga.carrot_server.chat.entity.QuickReply;
import com.mrokga.carrot_server.chat.enums.MessageType;
import com.mrokga.carrot_server.chat.repository.ChatMessageRepository;
import com.mrokga.carrot_server.chat.repository.QuickReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QuickReplyService {
    private final ChatMessageRepository chatMessageRepository;
    private final QuickReplyRepository quickReplyRepository;

    @Transactional
    public QuickReplyAddResponseDto addQuikReply(Integer messageId){
        Integer userId = QuickReplyUtils.currentUserIdOrThrow();

        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메세지 없음"));

        if(!Objects.equals(msg.getUser().getId(), userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "내가 보낸 메세지만 추가 가능");
        }

        if(msg.getMessageType() != MessageType.TEXT){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지는 자주 쓰는 문구로 추가할 수 없습니다.");
        }

        String body = msg.getMessage();
        String norm = QuickReplyUtils.normalizeForDuplicate(body);

        if (body == null || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 문자열 메세지. 추가 불가능");
        }
        if (norm.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문구는 최대 1000자까지 가능합니다.");
        }

        // 선조회
        var exists = quickReplyRepository.findByUserIdAndBodyNorm(userId, norm);
        if(exists.isPresent()){
            return QuickReplyAddResponseDto.alreadyExists(exists.get().getId());
        }

        // 저장
        QuickReply q = new QuickReply();
        q.setUserId(userId);
        q.setBody(body);
        q.setBodyNorm(norm);

        try {
            QuickReply saved = quickReplyRepository.saveAndFlush(q);
            return QuickReplyAddResponseDto.created(saved.getId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 유니크 제약에 걸리면 이미 존재
            var existing = quickReplyRepository.findByUserIdAndBodyNorm(userId, norm);
            if (existing.isPresent()) {
                return QuickReplyAddResponseDto.alreadyExists(existing.get().getId());
            }
            // 혹시 모를 다른 제약 오류는 409보다는 400/500으로 넘겨도 됨
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 문구가 등록되어 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<QuickReplyItemResponseDto> listMine() {
        Integer userId = QuickReplyUtils.currentUserIdOrThrow();
        return quickReplyRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(QuickReplyItemResponseDto::from)
                .toList();
    }

    @Transactional
    public void deleteMine(Integer id) {
        Integer userId = QuickReplyUtils.currentUserIdOrThrow();
        long affected = quickReplyRepository.deleteByUserIdAndId(userId, id);
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 문구를 찾지 못함");
        }
    }
}
