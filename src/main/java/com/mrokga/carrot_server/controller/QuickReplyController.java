package com.mrokga.carrot_server.controller;

import com.mrokga.carrot_server.dto.response.QuickReplyAddResponseDto;
import com.mrokga.carrot_server.dto.response.QuickReplyItemResponseDto;
import com.mrokga.carrot_server.service.QuickReplyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quickReply")
public class QuickReplyController {
    private final QuickReplyService quickReplyService;

    @Operation(summary = "자주 쓰는 문구 추가", description = "사용자가 특정 메세지를 자주 쓰는 문구로 저장합니다.")
    @PostMapping("/add/{messageId}")
    public ResponseEntity<?> addFromMessage(@PathVariable Integer messageId){
        QuickReplyAddResponseDto res = quickReplyService.addQuikReply(messageId);
        if("CREATED".equals(res.getStatus())){
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        //이미 존재
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @Operation(summary = "자주 쓰는 문구 목록 조회", description = "사용자가 저장한 자주 쓰는 문구 리스트를 조회합니다.")
    @GetMapping
    public List<QuickReplyItemResponseDto> listMine(){
        return quickReplyService.listMine();
    }

    @Operation(summary = "자주 쓰는 문구 삭제", description = "사용자가 자주 쓰는 문구에서 특정 메세지를 삭제합니다.")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id){
        quickReplyService.deleteMine(id);
    }
}
