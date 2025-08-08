package com.stylemycloset.cloth.adapter;

import com.stylemycloset.cloth.dto.RawSiteData;
import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;

public interface SiteDataAdapter {
    
    /**
     * 이 어댑터가 해당 URL을 처리할 수 있는지 확인
     */
    boolean supports(String url);
    
    /**
     * 원시 데이터를 통일된 형식으로 변환
     */
    ClothExtractionResponseDto convert(RawSiteData rawData);
    
    /**
     * 사이트명 반환
     */
    String getSiteName();
    
    /**
     * 우선순위 반환 (낮을수록 우선)
     */
    default int getPriority() {
        return 100;
    }
}