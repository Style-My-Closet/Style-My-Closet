package com.stylemycloset.location.util;

public class LamcConst {

    // 지도 정보 (C 코드 lamc_parameter 기준)
    public static final double RE = 6371.00877;      // 지도 반경 [km]
    public static final double GRID = 5.0;           // 격자 간격 (km)
    public static final double SLAT1 = 30.0;         // 표준 위도 1
    public static final double SLAT2 = 60.0;         // 표준 위도 2
    public static final double OLON = 126.0;         // 기준점 경도
    public static final double OLAT = 38.0;          // 기준점 위도
    public static final double XO = 210 / GRID;      // 기준점 X좌표
    public static final double YO = 675 / GRID;      // 기준점 Y좌표

    // 계산용 상수
    public static final double PI;
    public static final double DEGRAD;
    public static final double RADDEG;
    public static final double RE_GRID;
    public static final double OLON_RAD;
    public static final double OLAT_RAD;
    public static final double SN;
    public static final double SF;
    public static final double RO;

    static {
        PI = Math.asin(1.0) * 2.0;
        DEGRAD = PI / 180.0;
        RADDEG = 180.0 / PI;

        // 격자 단위 보정
        RE_GRID = RE / GRID;

        // 라디안 단위 변환
        double slat1Rad = SLAT1 * DEGRAD;
        double slat2Rad = SLAT2 * DEGRAD;
        OLON_RAD = OLON * DEGRAD;
        OLAT_RAD = OLAT * DEGRAD;

        // Lambert Conformal Conic 계산
        SN = Math.log(Math.cos(slat1Rad) / Math.cos(slat2Rad)) /
            Math.log(Math.tan(PI * 0.25 + slat2Rad * 0.5) / Math.tan(PI * 0.25 + slat1Rad * 0.5));

        SF = Math.pow(Math.tan(PI * 0.25 + slat1Rad * 0.5), SN) * Math.cos(slat1Rad) / SN;
        RO = RE_GRID * SF / Math.pow(Math.tan(PI * 0.25 + OLAT_RAD * 0.5), SN);
    }
}
