package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.GuestDTO;
import com.hotel.entity.Guest;
import com.hotel.security.BaseContext;
import com.hotel.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端：常用入住人 CRUD。
 */
@RestController
@RequestMapping("/guest")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /** 当前用户的入住人列表（默认优先，再按创建时间倒序） */
    @GetMapping("/list")
    public Result<List<Guest>> list() {
        Long userId = BaseContext.getUserId();
        List<Guest> list = guestService.lambdaQuery()
                .eq(Guest::getUserId, userId)
                .orderByDesc(Guest::getIsDefault)
                .orderByDesc(Guest::getCreateTime)
                .list();
        return Result.ok(list);
    }

    /** 新增入住人 */
    @PostMapping
    @Transactional
    public Result<Void> add(@RequestBody @Valid GuestDTO dto) {
        Long userId = BaseContext.getUserId();
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearOtherDefaults(userId);
        }
        Guest guest = new Guest();
        guest.setUserId(userId);
        guest.setName(dto.getName());
        guest.setPhone(dto.getPhone());
        guest.setIdCard(dto.getIdCard());
        guest.setGender(dto.getGender());
        guest.setIsDefault(dto.getIsDefault());
        guestService.save(guest);
        return Result.ok();
    }

    /** 更新入住人 */
    @PutMapping
    @Transactional
    public Result<Void> update(@RequestBody @Valid GuestDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "入住人 ID 不能为空");
        }
        Guest existing = guestService.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.GUEST_NOT_FOUND);
        }
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearOtherDefaults(existing.getUserId());
        }
        Guest guest = new Guest();
        guest.setId(dto.getId());
        guest.setName(dto.getName());
        guest.setPhone(dto.getPhone());
        guest.setIdCard(dto.getIdCard());
        guest.setGender(dto.getGender());
        guest.setIsDefault(dto.getIsDefault());
        guestService.updateById(guest);
        return Result.ok();
    }

    /** 删除入住人 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        guestService.removeById(id);
        return Result.ok();
    }

    /** 设为默认入住人 */
    @PutMapping("/{id}/default")
    @Transactional
    public Result<Void> setDefault(@PathVariable Long id) {
        Guest guest = guestService.getById(id);
        if (guest == null) {
            throw new BusinessException(ResultCode.GUEST_NOT_FOUND);
        }
        clearOtherDefaults(guest.getUserId());
        Guest update = new Guest();
        update.setId(id);
        update.setIsDefault(1);
        guestService.updateById(update);
        return Result.ok();
    }

    /** 清除当前用户其他入住人的默认标记 */
    private void clearOtherDefaults(Long userId) {
        Guest update = new Guest();
        update.setIsDefault(0);
        guestService.lambdaUpdate()
                .eq(Guest::getUserId, userId)
                .eq(Guest::getIsDefault, 1)
                .update(update);
    }
}
