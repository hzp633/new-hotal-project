package com.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * C 端发表评论请求
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "订单不能为空")
    private Long reservationId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为 1")
    @Max(value = 5, message = "评分最高为 5")
    private Integer rating;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String images;
}
