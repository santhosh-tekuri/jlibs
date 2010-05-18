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
@Table(value="employees", extend= EmployeeDAO.class)
public class Employee{
    private long id;
    @Column(value="id", primary=true, auto=true)
    public long getID(){
        return id;
    }
    public void setID(long id){
        this.id = id;
    }

    @Column("first_name")
    public String firstName;
//    public String getFirstName(){
//        return firstName;
//    }
//    public void setFirstName(String firstName){
//        this.firstName = firstName;
//    }

    private String lastName;
    public String getLastName(){
        return lastName;
    }
    @Column("last_name")
    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    private int age;
    @Column("age")
    public int getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }
}
