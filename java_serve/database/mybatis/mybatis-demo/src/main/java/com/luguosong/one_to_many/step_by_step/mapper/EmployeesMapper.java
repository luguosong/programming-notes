package com.luguosong.one_to_many.step_by_step.mapper;


import com.luguosong.one_to_many.pojo.Employees;

import java.util.List;

/**
 * @author luguosong
 */
public interface EmployeesMapper {
    List<Employees> getEmployeesByDeptId(Integer id);
}
