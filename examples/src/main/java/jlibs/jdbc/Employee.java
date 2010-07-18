/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.jdbc;

import jlibs.jdbc.annotations.Column;
import jlibs.jdbc.annotations.Table;
import jlibs.jdbc.annotations.TypeMapper;

/**
 * @author Santhosh Kumar T
 */
@Table(name="employees", extend= EmployeeDAO.class)
public class Employee{
    @Column(name="id", primary=true, auto=true)
    public long id;

    private String firstName;
    @Column(name="first_name")
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    private String lastName;
    public String getLastName(){
        return lastName;
    }
    @Column(name="last_name")
    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    private int age;
    @Column(name="age")
    public int getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }

    private Integer experience;
    @Column
    public Integer getExperience(){
        return experience;
    }
    public void setExperience(Integer experience){
        this.experience = experience;
    }

    @Column
    @TypeMapper(mapper=GradeTypeMapper.class, mapsTo=Integer.class)
    public Grade grade;

//    @Column
//    public java.util.Date creationDate;
}
