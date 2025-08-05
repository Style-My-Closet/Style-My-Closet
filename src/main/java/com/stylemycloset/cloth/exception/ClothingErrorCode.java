package com.stylemycloset.cloth.exception;

import lombok.Getter;

@Getter
public enum ClothingErrorCode {
    CLOTH_NOT_FOUND("CLOTH_001", "해당 의상을 찾을 수 없습니다."),
    CLOTH_LIST_FETCH_FAILED("CLOTH_002", "옷 목록 조회에 실패했습니다."),
    INVALID_ATTRIBUTE("ATTR_001", "유효하지 않은 속성입니다."),
    ATTRIBUTE_DUPLICATE("ATTR_002", "이미 존재하는 속성입니다."),
    ATTRIBUTE_NOT_FOUND("ATTR_003", "해당 속성을 찾을 수 없습니다."),
    INVALID_PARAMETER("PARAM_001", "잘못된 매개변수입니다."),
    CLOSET_NOT_FOUND("CLOSET_001", "해당 옷장을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND("CATEGORY_001", "해당 카테고리를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("AUTH_001", "권한이 없습니다."),
    INTERNAL_ERROR("SYS_001", "서버 내부 오류가 발생했습니다."),
    CLOTH_UPDATE_FAILED("CLOTH_003", "옷 업데이트에 실패했습니다.");
    private final String code;
    private final String message;


    ClothingErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
