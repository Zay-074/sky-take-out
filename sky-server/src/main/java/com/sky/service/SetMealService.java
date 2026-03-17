package com.sky.service;

import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.entity.SetMeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetMealVO;

import java.util.List;

public interface SetMealService {

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<SetMeal> list(SetMeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetMealDTO setmealDTO);

    PageResult pageQuery(SetMealPageQueryDTO setMealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    SetMealVO getByIdWithDish(Long id);

    void update(SetMealDTO setMealDTO);

    void startOrStop(Integer status, Long id);
}
