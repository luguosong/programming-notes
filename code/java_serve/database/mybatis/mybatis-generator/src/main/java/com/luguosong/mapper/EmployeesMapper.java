package com.luguosong.mapper;

import com.luguosong.pojo.Employees;
import java.util.List;

public interface EmployeesMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table employees
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table employees
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    int insert(Employees row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table employees
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    Employees selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table employees
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    List<Employees> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table employees
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    int updateByPrimaryKey(Employees row);
}