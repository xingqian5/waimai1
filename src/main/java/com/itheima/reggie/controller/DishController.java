package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.commom.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return  R.success("新增菜品成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records=pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId=item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
            String categoryName=category.getName();
            dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);

    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return  R.success("修改菜品成功");
    }
    //根据条件查看菜品数据
    @GetMapping("/list")
    public  R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtos=null;
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
       dishDtos= (List<DishDto>)redisTemplate.opsForValue().get(key);
       if (dishDtos!=null){
           return R.success(dishDtos);
       }
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list =dishService.list(queryWrapper);
        dishDtos = list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId=item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long dishId=item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors=dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key,dishDtos,60, TimeUnit.MINUTES);
        return R.success(dishDtos);
    }
}
