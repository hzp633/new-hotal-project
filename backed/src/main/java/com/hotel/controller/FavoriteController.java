package com.hotel.controller;

import com.hotel.common.Result;
import com.hotel.dto.FavoriteVO;
import com.hotel.entity.Favorite;
import com.hotel.entity.RoomType;
import com.hotel.security.BaseContext;
import com.hotel.service.FavoriteService;
import com.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C 端：房型收藏。
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final RoomTypeService roomTypeService;

    /** 当前用户的收藏列表（含房型信息） */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> list() {
        Long userId = BaseContext.getUserId();
        List<Favorite> favorites = favoriteService.lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime)
                .list();
        if (favorites.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> roomTypeIds = favorites.stream()
                .map(Favorite::getRoomTypeId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, RoomType> roomTypeMap = roomTypeService.listByIds(roomTypeIds).stream()
                .collect(Collectors.toMap(RoomType::getId, rt -> rt));
        List<FavoriteVO> vos = favorites.stream()
                .map(fav -> {
                    RoomType rt = roomTypeMap.get(fav.getRoomTypeId());
                    FavoriteVO.FavoriteVOBuilder builder = FavoriteVO.builder()
                            .id(fav.getId())
                            .roomTypeId(fav.getRoomTypeId())
                            .createTime(fav.getCreateTime());
                    if (rt != null) {
                        builder.roomTypeName(rt.getName())
                                .price(rt.getPrice())
                                .images(rt.getImages())
                                .bedType(rt.getBedType())
                                .area(rt.getArea());
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());
        return Result.ok(vos);
    }

    /** 新增收藏（不校验是否重复） */
    @PostMapping("/{roomTypeId}")
    public Result<Void> add(@PathVariable Long roomTypeId) {
        Long userId = BaseContext.getUserId();
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRoomTypeId(roomTypeId);
        favoriteService.save(favorite);
        return Result.ok();
    }

    /** 取消收藏 */
    @DeleteMapping("/{roomTypeId}")
    public Result<Void> delete(@PathVariable Long roomTypeId) {
        Long userId = BaseContext.getUserId();
        favoriteService.lambdaUpdate()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getRoomTypeId, roomTypeId)
                .remove();
        return Result.ok();
    }

    /** 检查是否已收藏 */
    @GetMapping("/check/{roomTypeId}")
    public Result<Boolean> check(@PathVariable Long roomTypeId) {
        Long userId = BaseContext.getUserId();
        long count = favoriteService.lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getRoomTypeId, roomTypeId)
                .count();
        return Result.ok(count > 0);
    }
}
