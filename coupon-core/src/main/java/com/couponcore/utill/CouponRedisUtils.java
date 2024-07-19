package com.couponcore.utill;

public class CouponRedisUtils {

    //키 관리
    public static String getIssueRequestKey(long couponId) {
        return "issue.request.couponId=%s".formatted(couponId);
    }

    public static String getIssueRequestQueueKey() {
        return "issue.request";
    }
}
