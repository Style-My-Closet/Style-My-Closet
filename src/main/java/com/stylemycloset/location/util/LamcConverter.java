package com.stylemycloset.location.util;

public class LamcConverter {

    // code = 0 : lon, lat -> x, y
    // code = 1 : x, y -> lon, lat
    public static double[] lamcProj(double p1, double p2, int code) {
        if (code == 0) { // lon, lat -> x, y
            double lon = p1;
            double lat = p2;
            double ra = Math.tan(LamcConst.PI*0.25 + lat*LamcConst.DEGRAD*0.5);
            ra = (LamcConst.RE / LamcConst.GRID) * LamcConst.SF / Math.pow(ra, LamcConst.SN);

            double theta = lon*LamcConst.DEGRAD - LamcConst.OLON*LamcConst.DEGRAD;
            if (theta > LamcConst.PI) theta -= 2.0*LamcConst.PI;
            if (theta < -LamcConst.PI) theta += 2.0*LamcConst.PI;
            theta *= LamcConst.SN;

            double x = ra * Math.sin(theta) + LamcConst.XO;
            double y = LamcConst.RO - ra * Math.cos(theta) + LamcConst.YO;
            return new double[]{x, y};
        } else { // x, y -> lon, lat
            double x = p1;
            double y = p2;

            double xn = x - LamcConst.XO;
            double yn = LamcConst.RO - y + LamcConst.YO;
            double ra = Math.sqrt(xn*xn + yn*yn);
            double alat = Math.pow((LamcConst.RE / LamcConst.GRID * LamcConst.SF / ra), (1.0/LamcConst.SN));
            alat = 2.0*Math.atan(alat) - LamcConst.PI*0.5;

            double theta;
            if (Math.abs(xn) <= 1e-10) {
                theta = 0.0;
            } else if (Math.abs(yn) <= 1e-10) {
                theta = Math.PI*0.5;
                if (xn < 0.0) theta = -theta;
            } else {
                theta = Math.atan2(xn, yn);
            }

            double alon = theta / LamcConst.SN + LamcConst.OLON*LamcConst.DEGRAD;
            return new double[]{alon * LamcConst.RADDEG, alat * LamcConst.RADDEG};
        }
    }


}
