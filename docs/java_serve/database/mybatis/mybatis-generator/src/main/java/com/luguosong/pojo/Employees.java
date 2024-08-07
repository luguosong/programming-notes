package com.luguosong.pojo;

import java.util.Date;

public class Employees {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.first_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private String firstName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.last_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private String lastName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.position
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private String position;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.hire_date
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private Date hireDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column employees.department_id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private Integer departmentId;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.id
     *
     * @return the value of employees.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.id
     *
     * @param id the value for employees.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.first_name
     *
     * @return the value of employees.first_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.first_name
     *
     * @param firstName the value for employees.first_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.last_name
     *
     * @return the value of employees.last_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.last_name
     *
     * @param lastName the value for employees.last_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.position
     *
     * @return the value of employees.position
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public String getPosition() {
        return position;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.position
     *
     * @param position the value for employees.position
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setPosition(String position) {
        this.position = position == null ? null : position.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.hire_date
     *
     * @return the value of employees.hire_date
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public Date getHireDate() {
        return hireDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.hire_date
     *
     * @param hireDate the value for employees.hire_date
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column employees.department_id
     *
     * @return the value of employees.department_id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public Integer getDepartmentId() {
        return departmentId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column employees.department_id
     *
     * @param departmentId the value for employees.department_id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }
}