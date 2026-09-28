package com.hotel.common;

import lombok.Getter;

/**
 * 系统统一响应码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    SERVER_ERROR(500, "服务器异常"),

    // 业务码（6xxx）
    USER_EXISTS(6001, "手机号已注册"),
    USER_NOT_FOUND(6002, "用户不存在"),
    ACCOUNT_DISABLED(6003, "账号已被禁用"),
    PASSWORD_ERROR(6004, "密码错误"),
    ADMIN_NOT_FOUND(6005, "管理员不存在"),
    TOKEN_INVALID(6006, "Token 无效"),
    TOKEN_EXPIRED(6007, "Token 已过期，请刷新"),
    REFRESH_TOKEN_INVALID(6008, "刷新令牌无效，请重新登录"),

    // 房型 / 房间（61xx）
    ROOM_TYPE_NOT_FOUND(6101, "房型不存在"),
    ROOM_TYPE_DISABLED(6102, "房型已停用"),
    ROOM_NOT_FOUND(6103, "房间不存在"),
    ROOM_NOT_AVAILABLE(6104, "房间不可用"),
    NO_AVAILABLE_ROOM(6105, "该时段无可用房间"),

    // 入住人（62xx）
    GUEST_NOT_FOUND(6201, "入住人不存在"),
    GUEST_LIMIT_EXCEEDED(6202, "入住人数量超出限制"),

    // 收藏（63xx）
    FAVORITE_EXISTS(6301, "已收藏该房型"),
    FAVORITE_NOT_FOUND(6302, "收藏记录不存在"),

    // 订单（64xx）
    RESERVATION_NOT_FOUND(6401, "订单不存在"),
    RESERVATION_STATUS_ERROR(6402, "订单状态不允许此操作"),
    DATE_RANGE_INVALID(6403, "入住日期需早于离店日期"),
    ROOM_COUNT_INVALID(6404, "房间数量不合法"),
    RESERVATION_NOT_OWNED(6405, "无权操作他人订单"),
    RESERVATION_ALREADY_EXISTS(6406, "该时段已有重复订单"),

    // 支付 / 退款（65xx）
    PAYMENT_NOT_FOUND(6501, "支付记录不存在"),
    PAYMENT_ALREADY_PAID(6502, "订单已支付，无需重复支付"),
    PAYMENT_REFUNDED(6503, "订单已退款"),
    REFUND_NOT_ALLOWED(6504, "当前状态不允许退款"),

    // 评论（66xx）
    REVIEW_NOT_FOUND(6601, "评论不存在"),
    REVIEW_NOT_OWNED(6602, "无权操作他人评论"),
    REVIEW_ALREADY_EXISTS(6603, "该订单已评价过"),
    REVIEW_NOT_CHECKED_OUT(6604, "订单未完成，暂不能评价"),

    // 入住退住（67xx）
    CHECK_IN_RECORD_NOT_FOUND(6701, "入住记录不存在"),
    ROOM_OCCUPIED(6702, "房间已被占用"),
    NOT_CHECKED_IN(6703, "订单未入住，无法退住"),

    // 公告（68xx）
    ANNOUNCEMENT_NOT_FOUND(6801, "公告不存在"),

    // 通用业务（69xx）
    OLD_PASSWORD_ERROR(6901, "原密码错误"),
    OPERATION_NOT_ALLOWED(6902, "当前状态不允许此操作"),
    ;

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
