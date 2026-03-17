package com.sky.mapper;

import com.sky.entity.SetMealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetMealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setMealDishes
     */
    void insertBatch(List<SetMealDish> setMealDishes);


    @Delete("delete from setmeal_dish where setmeal_id = #{setMealId}")
    void deleteBySetMealId(Long setMealId);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetMealDish> getBySetMealId(Long id);
}
