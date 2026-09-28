package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.ReviewCreateDTO;
import com.hotel.entity.Reservation;
import com.hotel.entity.Review;
import com.hotel.security.BaseContext;
import com.hotel.service.ReservationService;
import com.hotel.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端评论：发表评论 / 我的评论列表。
 */
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReservationService reservationService;

    /** 发表评论（订单需为已完成状态 status=3，且每个订单只能评价一次） */
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ReviewCreateDTO dto) {
        Reservation reservation = reservationService.getById(dto.getReservationId());
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == null || reservation.getStatus() != 3) {
            throw new BusinessException(ResultCode.REVIEW_NOT_CHECKED_OUT);
        }
        long exists = reviewService.lambdaQuery()
                .eq(Review::getReservationId, dto.getReservationId())
                .count();
        if (exists > 0) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review();
        review.setUserId(BaseContext.getUserId());
        review.setHotelId(reservation.getHotelId());
        review.setRoomTypeId(reservation.getRoomTypeId());
        review.setReservationId(dto.getReservationId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setStatus(1);
        reviewService.save(review);
        return Result.ok();
    }

    /** 当前用户评论列表（按 createTime 倒序） */
    @GetMapping("/list")
    public Result<List<Review>> list() {
        Long userId = BaseContext.getUserId();
        List<Review> list = reviewService.lambdaQuery()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime)
                .list();
        return Result.ok(list);
    }
}
