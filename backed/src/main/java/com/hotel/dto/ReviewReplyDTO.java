package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论回复请求
 */
@Data
public class ReviewReplyDTO implements Serializable {

    private String reply;
}
