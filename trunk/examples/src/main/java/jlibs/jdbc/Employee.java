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

/**
 * @author Santhosh Kumar T
 */
@Table(value=Employee.TABLE_NAME, extend= EmployeeDAO.class)
public class Employee{
    public static final String TABLE_NAME =    "employees";
    public static final String COL_ID = "id";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_AGE = "age";

    private int id;
    @Column(value=COL_ID, primary=true)
    public int getID(){
        return id;
    }
    public void setID(int id){
        this.id = id;
    }

    private String firstName;
    @Column(COL_FIRST_NAME)
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    private String lastName;
    @Column(COL_LAST_NAME)
    public String getLastName(){
        return lastName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    private int age;
    @Column(COL_AGE)
    public int getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }
}
