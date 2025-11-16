package com.mrokga.carrot_server.product.enums;

public enum TradeStatus {
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료");

    private final String label;
    TradeStatus(String label) { this.label = label; }
    public String label() { return label; }

    /** 상태 전이 허용 규칙 */
    public boolean canTransitionTo(TradeStatus target) {
        return switch (this) {
            case ON_SALE -> (target == RESERVED || target == SOLD);
            case RESERVED -> (target == ON_SALE || target == SOLD);
            case SOLD -> false; // 완료 후 변경 불가
        };
    }
}
