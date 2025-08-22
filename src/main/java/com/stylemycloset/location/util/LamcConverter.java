package com.stylemycloset.location.util;

public class LamcConverter {

    /**
     * code = 0 : 위경도 -> 격자
     * code = 1 : 격자 -> 위경도
     */
    public static double[] mapConv(double lon,double lat, int code) {
        double[] lonLat = {lon,lat};
        double[] xy = new double[2];
        double x1, y1;

        if (code == 0) { // 위경도 -> 격자
            lon = lonLat[0];
            lat = lonLat[1];
            lamcProj(lon, lat, code, xy);
            xy[0] = Math.floor(xy[0] + 1.5);
            xy[1] = Math.floor(xy[1] + 1.5);
        } else { // 격자 -> 위경도
            x1 = xy[0] - 1.0;
            y1 = xy[1] - 1.0;
            double[] tmp = new double[]{x1, y1};
            lamcProj(0, 0, code, tmp);
            lonLat[0] = tmp[0];
            lonLat[1] = tmp[1];
        }

        return xy;
    }

    public static void lamcProj(double lon, double lat, int code, double[] xy) {
        double ra, theta, xn, yn, alat, alon;

        if (code == 0) { // 위경도 -> 격자
            ra = Math.tan(LamcConst.PI * 0.25 + lat * LamcConst.DEGRAD * 0.5);
            ra = LamcConst.RE_GRID * LamcConst.SF / Math.pow(ra, LamcConst.SN);

            theta = (lon * LamcConst.DEGRAD - LamcConst.OLON_RAD);
            if (theta > LamcConst.PI) theta -= 2.0 * LamcConst.PI;
            if (theta < -LamcConst.PI) theta += 2.0 * LamcConst.PI;
            theta *= LamcConst.SN;

            xy[0] = ra * Math.sin(theta) + LamcConst.XO;
            xy[1] = LamcConst.RO - ra * Math.cos(theta) + LamcConst.YO;

        } else { // 격자 -> 위경도
            xn = xy[0] - LamcConst.XO;
            yn = LamcConst.RO - xy[1] + LamcConst.YO;
            ra = Math.sqrt(xn*xn + yn*yn);
            if (LamcConst.SN < 0.0) ra = -ra;

            alat = Math.pow(LamcConst.RE_GRID * LamcConst.SF / ra, 1.0 / LamcConst.SN);
            alat = 2.0 * Math.atan(alat) - LamcConst.PI * 0.5;

            if (Math.abs(xn) <= 1e-10) theta = 0.0;
            else if (Math.abs(yn) <= 1e-10) {
                theta = LamcConst.PI * 0.5;
                if (xn < 0.0) theta = -theta;
            } else theta = Math.atan2(xn, yn);

            alon = theta / LamcConst.SN + LamcConst.OLON_RAD;

            xy[0] = alon * LamcConst.RADDEG;
            xy[1] = alat * LamcConst.RADDEG;
        }
    }
}
