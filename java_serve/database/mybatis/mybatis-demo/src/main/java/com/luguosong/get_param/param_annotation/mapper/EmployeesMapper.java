package com.luguosong.get_param.param_annotation.mapper;

import com.luguosong.get_param.pojo.Employees;
import org.apache.ibatis.annotations.Param;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    Employees getEmployeesByName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}
