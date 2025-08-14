package com.stylemycloset.location.util;

public class LamcConst {

    public static final double RE = 6371.00877;   // 지구 반경 [km]
    public static final double GRID = 5.0;       // 격자 간격 [km]
    public static final double SLAT1 = 30.0;     // 표준 위도1
    public static final double SLAT2 = 60.0;     // 표준 위도2
    public static final double OLON = 126.0;     // 기준점 경도
    public static final double OLAT = 38.0;      // 기준점 위도
    public static final double XO = 210 / GRID;  // 기준점 X좌표
    public static final double YO = 675 / GRID;  // 기준점 Y좌표

    // 내부 계산용 상수 (Lambert 계산 후 한 번 초기화)
    public static final double PI = Math.asin(1.0) * 2.0;
    public static final double DEGRAD = PI / 180.0;
    public static final double RADDEG = 180.0 / PI;

    // 계산 시 사용할 변수 (한 번만 계산)
    public static final double SN;
    public static final double SF;
    public static final double RO;

    static {
        double slat1Rad = SLAT1 * DEGRAD;
        double slat2Rad = SLAT2 * DEGRAD;
        double olatRad = OLAT * DEGRAD;
        double re = RE / GRID;

        SN = Math.log(Math.cos(slat1Rad)/Math.cos(slat2Rad)) /
            Math.log(Math.tan(PI*0.25 + slat2Rad*0.5)/Math.tan(PI*0.25 + slat1Rad*0.5));

        SF = Math.pow(Math.tan(PI*0.25 + slat1Rad*0.5), SN) * Math.cos(slat1Rad)/SN;

        RO = re * SF / Math.pow(Math.tan(PI*0.25 + olatRad*0.5), SN);
    }
}