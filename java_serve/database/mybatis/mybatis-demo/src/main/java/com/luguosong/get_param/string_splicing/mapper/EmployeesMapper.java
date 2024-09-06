package com.luguosong.get_param.string_splicing.mapper;

import com.luguosong.get_param.pojo.Employees;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    Employees getEmployeesById(Integer id);

    Employees getEmployeesByName(String firstName);
}
