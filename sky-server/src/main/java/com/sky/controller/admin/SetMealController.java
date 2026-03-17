package com.sky.controller.admin;

import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetMealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody SetMealDTO setmealDTO) {
        log.info("===== 新增套餐 - 开始 =====");
        log.info("套餐基本信息：categoryId={}, name={}, price={}, status={}, description={}, image={}", 
            setmealDTO.getCategoryId(), setmealDTO.getName(), setmealDTO.getPrice(), 
            setmealDTO.getStatus(), setmealDTO.getDescription(), setmealDTO.getImage());
        
        if (setmealDTO.getSetMealDishes() != null) {
            log.info("接收到套餐菜品关联数据，数量：{}", setmealDTO.getSetMealDishes().size());
            for (int i = 0; i < setmealDTO.getSetMealDishes().size(); i++) {
                var dish = setmealDTO.getSetMealDishes().get(i);
                log.info("菜品 [{}] - dishId: {}, name: {}, price: {}, copies: {}", 
                    i, dish.getDishId(), dish.getName(), dish.getPrice(), dish.getCopies());
            }
        } else {
            log.warn("setMealDishes 为 null");
        }
        log.info("===== 新增套餐 - 结束 =====");

        setMealService.saveWithDish(setmealDTO);
        return Result.success();
    }
    /**
     * 分页查询
     * @param setMealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetMealPageQueryDTO setMealPageQueryDTO) {
        PageResult pageResult = setMealService.pageQuery(setMealPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        setMealService.deleteBatch(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetMealVO> getById(@PathVariable Long id) {
        SetMealVO setMealVO = setMealService.getByIdWithDish(id);
        return Result.success(setMealVO);
    }

    /**
     * 修改套餐
     *
     * @param setMealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetMealDTO setMealDTO) {
        setMealService.update(setMealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        setMealService.startOrStop(status, id);
        return Result.success();
    }
}
