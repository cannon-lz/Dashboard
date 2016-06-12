package com.zly.dashboard.lib;

/**
 * @authour zhangluya on 2015/9/25.
 */
public class Indicator {

    private float viewHeight;

    private float x;
    private float y;

    private float pointAx;
    private float pointAy;
    private float pointBx;
    private float pointBy;
    private float pointCx;
    private float pointCy;

    private float center;
    private float bigRadius;
    private float smallRadius;
    private double angle;
    private float triangleRadius;

    public Indicator(int viewWidth, float viewHeight, float radius, float offset,
                     float range, float indicatorRange) {
        this.viewHeight = viewHeight;
        center = viewWidth / 2;
        bigRadius = center - offset;
        smallRadius = bigRadius - radius * range;
        double cos = (Math.pow(bigRadius, 2) + Math.pow(smallRadius, 2) - Math.pow(radius, 2)) / bigRadius / smallRadius / 2;
        angle = Math.acos(cos);
        triangleRadius = smallRadius * indicatorRange;
    }

    public void calculation(double indicatorAngle) {
        this.x = (float) (center + bigRadius * Math.cos(Math.PI - indicatorAngle));
        this.y = (float) (viewHeight - bigRadius * Math.sin(Math.PI - indicatorAngle));
        this.pointAx = (float) (center + smallRadius * Math.cos(Math.PI - indicatorAngle + angle));
        this.pointAy = (float) (viewHeight - smallRadius * Math.sin(Math.PI - indicatorAngle + angle));
        this.pointBx = (float) (center + smallRadius * Math.cos(Math.PI - indicatorAngle - angle));
        this.pointBy = (float) (viewHeight - smallRadius * Math.sin(Math.PI - indicatorAngle - angle));
        this.pointCx = (float) (center + triangleRadius * Math.cos(Math.PI - indicatorAngle));
        this.pointCy = (float) (viewHeight - triangleRadius * Math.sin(Math.PI - indicatorAngle));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPointAx() {
        return pointAx;
    }

    public float getPointAy() {
        return pointAy;
    }

    public float getPointBx() {
        return pointBx;
    }

    public float getPointBy() {
        return pointBy;
    }

    public float getPointCx() {
        return pointCx;
    }

    public float getPointCy() {
        return pointCy;
    }
}
