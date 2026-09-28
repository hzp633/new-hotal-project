package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.RoomTypeSearchDTO;
import com.hotel.entity.Announcement;
import com.hotel.entity.Hotel;
import com.hotel.entity.Review;
import com.hotel.entity.RoomType;
import com.hotel.service.AnnouncementService;
import com.hotel.service.HotelService;
import com.hotel.service.ReviewService;
import com.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开接口（无需登录）：酒店信息、启用户型、已发布公告。
 * <p>供 C 端首页与房型浏览使用。
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final HotelService hotelService;
    private final RoomTypeService roomTypeService;
    private final AnnouncementService announcementService;
    private final ReviewService reviewService;

    /** 酒店基本信息（单酒店系统，ID 固定 1） */
    @GetMapping("/hotel")
    public Result<Hotel> hotel() {
        return Result.ok(hotelService.getById(1L));
    }

    /** 启用房型列表（按价格升序） */
    @GetMapping("/room-types")
    public Result<List<RoomType>> roomTypes() {
        List<RoomType> list = roomTypeService.lambdaQuery()
                .eq(RoomType::getStatus, 1)
                .orderByAsc(RoomType::getPrice)
                .list();
        return Result.ok(list);
    }

    /** 房型筛选查询：按价格区间、床型过滤，按价格升降序排序 */
    @GetMapping("/room-types/search")
    public Result<List<RoomType>> searchRoomTypes(@ModelAttribute RoomTypeSearchDTO dto) {
        var chain = roomTypeService.lambdaQuery()
                .eq(RoomType::getStatus, 1);
        if (dto.getMinPrice() != null) {
            chain.ge(RoomType::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            chain.le(RoomType::getPrice, dto.getMaxPrice());
        }
        if (StringUtils.hasText(dto.getBedType())) {
            chain.eq(RoomType::getBedType, dto.getBedType());
        }
        String orderBy = dto.getOrderBy();
        if ("price_desc".equals(orderBy)) {
            chain.orderByDesc(RoomType::getPrice);
        } else {
            // 默认 price_asc
            chain.orderByAsc(RoomType::getPrice);
        }
        return Result.ok(chain.list());
    }

    /** 房型详情 */
    @GetMapping("/room-types/{id}")
    public Result<RoomType> roomTypeDetail(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.ROOM_TYPE_NOT_FOUND);
        }
        return Result.ok(roomType);
    }

    /** 房型可见评论列表（status=1，按 createTime 倒序） */
    @GetMapping("/room-types/{id}/reviews")
    public Result<List<Review>> roomTypeReviews(@PathVariable Long id) {
        List<Review> list = reviewService.lambdaQuery()
                .eq(Review::getRoomTypeId, id)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getCreateTime)
                .list();
        return Result.ok(list);
    }

    /** 已发布公告列表（置顶优先，再按时间倒序） */
    @GetMapping("/announcements")
    public Result<List<Announcement>> announcements() {
        List<Announcement> list = announcementService.lambdaQuery()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime)
                .list();
        return Result.ok(list);
    }
}
