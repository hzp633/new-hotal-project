package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.ReviewReplyDTO;
import com.hotel.entity.Review;
import com.hotel.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * B 端评论管理
 */
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    /** 评论分页查询 */
    @GetMapping("/page")
    public Result<PageResult<Review>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer status) {
        Page<Review> page = new Page<>(current, size);
        Page<Review> result = reviewService.lambdaQuery()
                .eq(rating != null, Review::getRating, rating)
                .eq(status != null, Review::getStatus, status)
                .orderByDesc(Review::getCreateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 显示 / 隐藏评论 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Review review = reviewService.getById(id);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        review.setStatus(status);
        reviewService.updateById(review);
        return Result.ok();
    }

    /** 回复评论 */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody ReviewReplyDTO dto) {
        Review review = reviewService.getById(id);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        review.setReply(dto.getReply());
        review.setReplyTime(LocalDateTime.now());
        reviewService.updateById(review);
        return Result.ok();
    }
}
