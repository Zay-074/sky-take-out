package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("添加菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //删除缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        deleteCacheData( key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    /**
     * 进行分页查询
     */
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询:{}",dishPageQueryDTO);
        PageResult pageResult= dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);

    }


    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids){ //加上@RequestParam，使用SpringMVC将前端传递的字符串参数转为List<Long>
        log.info("批量删除：{}",ids);
        dishService.deleteBatch(ids);
        //删除缓存数据
        deleteCacheData("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //删除缓存数据
        deleteCacheData("dish_*");

        return Result.success();
    }
    /**
         * 商品起售停售
         * @param status
         * @param id
         * @return
         */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("商品起售停售：{}", status, id);
        dishService.startOrStop(status, id);
        //删除缓存数据
        deleteCacheData( "dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    //删除缓存数据
    private void deleteCacheData(String key){
        Set keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);
    }

}
