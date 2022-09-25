package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.commom.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);
        if(emp==null){
            return  R.error("失败");
        }
    if (!emp.getPassword().equals(password)){
        return  R.error("密码不对登录失败");
    }
    if(emp.getStatus()==0){
        return R.error("账号禁用");

    }
    request.getSession().setAttribute("employee",emp.getId());
        return  R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("成功退出");

    }
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工：{}",employee.toString());
      employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        /** employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
       Long empId=(Long) request.getSession().getAttribute("employee");
       employee.setCreateUser(empId);
       employee.setUpdateUser(empId);*/
       employeeService.save(employee);
       return  R.success("新增员工成功");

    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pagesize={}，name={}",page,pageSize,name);
        Page pageInfo=new Page(page,pageSize);
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,queryWrapper);
        return  R.success(pageInfo);
    }
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        Long empID=(Long)  request.getSession().getAttribute("employee");

        employeeService.updateById(employee);
        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    public R<Employee> getByID(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee employee=employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);
        }
        return null;

    }


}
