package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetMealDTO;
import com.sky.dto.SetMealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetMeal;
import com.sky.entity.SetMealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetMealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;

import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetMealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<SetMeal> list(SetMeal setmeal) {
        List<SetMeal> list = setMealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setMealDTO
     * @return
     */
    @Transactional
    public void saveWithDish(SetMealDTO setMealDTO) {
        log.info("开始保存套餐，套餐信息：{}", setMealDTO);
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setMealDTO, setMeal);
    
        //向套餐表插入数据
        setMealMapper.insert(setMeal);
        log.info("套餐表插入成功，生成的套餐 ID: {}", setMeal.getId());
    
        //获取生成的套餐 id
        Long setMealId = setMeal.getId();
    
        List<SetMealDish> setMealDishes = setMealDTO.getSetMealDishes();
        log.info("获取到套餐菜品关联数据，数量：{}", setMealDishes == null ? "null" : setMealDishes.size());
            
        //只有当套餐菜品不为空时才保存
        if (setMealDishes != null && !setMealDishes.isEmpty()) {
            log.info("开始保存套餐菜品关联关系...");
            setMealDishes.forEach(setMealDish -> {
                setMealDish.setSetmealId(setMealId);
                log.info("设置套餐菜品关联：套餐 ID={}, 菜品 ID={}, 名称={}, 价格={}, 份数={}", 
                    setMealDish.getSetmealId(), setMealDish.getDishId(), 
                    setMealDish.getName(), setMealDish.getPrice(), setMealDish.getCopies());
            });
    
            //保存套餐和菜品的关联关系
            setMealDishMapper.insertBatch(setMealDishes);
            log.info("套餐菜品关联关系保存成功，共{}条", setMealDishes.size());
        } else {
            log.warn("套餐菜品关联数据为空，跳过保存");
        }
    }

    @Override
    public PageResult pageQuery(SetMealPageQueryDTO setMealPageQueryDTO) {
        int pageNum = setMealPageQueryDTO.getPage();
        int pageSize = setMealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<SetMealVO> page = setMealMapper.pageQuery(setMealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            SetMeal setMeal = setMealMapper.getById(id);
            if(StatusConstant.ENABLE == setMeal.getStatus()){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(setMealId -> {
            //删除套餐表中的数据
            setMealMapper.deleteById(setMealId);
            //删除套餐菜品关系表中的数据
            setMealDishMapper.deleteBySetMealId(setMealId);
        });
    }

    @Override
    public SetMealVO getByIdWithDish(Long id) {
        SetMeal setMeal = setMealMapper.getById(id);
        List<SetMealDish> setMealDishes = setMealDishMapper.getBySetMealId(id);

        SetMealVO setMealVO = new SetMealVO();
        BeanUtils.copyProperties(setMeal, setMealVO);
        setMealVO.setSetMealDishes(setMealDishes);

        return setMealVO;
    }

    @Transactional
    @Override
    public void update(SetMealDTO setMealDTO) {
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setMealDTO, setMeal);

        //1、修改套餐表，执行update
        setMealMapper.update(setMeal);

        //套餐id
        Long setMealId = setMealDTO.getId();

        //2、删除套餐和菜品的关联关系，操作 setmeal_dish 表，执行 delete
        setMealDishMapper.deleteBySetMealId(setMealId);
    
        List<SetMealDish> setMealDishes = setMealDTO.getSetMealDishes();
        //只有当套餐菜品不为空时才保存
        if (setMealDishes != null && !setMealDishes.isEmpty()) {
            setMealDishes.forEach(setMealDish -> {
                setMealDish.setSetmealId(setMealId);
            });
            //3、重新插入套餐和菜品的关联关系，操作 setmeal_dish 表，执行 insert
            setMealDishMapper.insertBatch(setMealDishes);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetMealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetMealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        SetMeal setMeal = SetMeal.builder()
                .id(id)
                .status(status)
                .build();
        setMealMapper.update(setMeal);
    }
}

