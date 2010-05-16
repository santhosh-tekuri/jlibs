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

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class DB{
    public static final BasicDataSource DATA_SOURCE;
    public static final EmployeeDAO EMPLOYEES;

    static{
        DATA_SOURCE = new BasicDataSource();
 	    DATA_SOURCE.setUrl("jdbc:mysql://localhost/test");
        EMPLOYEES = (EmployeeDAO)DAO.create(Employee.class, DATA_SOURCE);
    }

    public static void main(String[] args) throws Exception{
        try{
            assert false;
            throw new RuntimeException("assertions are not enabled"); 
        }catch(AssertionError ignore){
            // ignore
        }

        EMPLOYEES.delete();
        assert EMPLOYEES.all().size()==0;

        Employee emp = new Employee();
        emp.setID(1);
        emp.firstName = "santhosh";
        emp.setLastName("kumar");
        emp.setAge(25);
        EMPLOYEES.insert(emp);

        assert EMPLOYEES.all().size()==1;
        emp.setAge(20);
        EMPLOYEES.update(emp);
        assert EMPLOYEES.all().get(0).getAge()==20;

        try{
            TransactionManager.run(DATA_SOURCE, new Transaction<Object>(){
                @Override
                public Object run(Connection con) throws SQLException{
                    Employee emp = new Employee();
                    emp.setID(2);
                    emp.firstName = "santhosh";
                    emp.setLastName("kumar");
                    emp.setAge(25);
                    EMPLOYEES.insert(emp);

                    emp.setID(3);
                    EMPLOYEES.insert(emp);

                    assert EMPLOYEES.all().size()==3;
//                    assert table.findOlderEmployees(1).size()==2;
                    throw new RuntimeException();
                }
            });
        } catch(RuntimeException ignore){
            // ignore
        }
//        assert table.findOlderEmployees(1).size()==0;
        System.out.println(EMPLOYEES.all().size());
        assert EMPLOYEES.all().size()==1;

        emp.setAge(10);
        EMPLOYEES.upsert(emp);
        assert EMPLOYEES.first("where id=?", 1).getAge()==10;
        emp.setID(4);
        EMPLOYEES.upsert(emp);
        assert EMPLOYEES.first("where id=?", 4)!=null;
        assert EMPLOYEES.all().size()==2;


        List<Employee> list = EMPLOYEES.all();
        list.get(0).setAge(29);
        EMPLOYEES.update(list.get(0));
        EMPLOYEES.delete(list.get(0));
//        List<Employee> list = table.findYoungEmployees(50);
//        Employee emp1 = EMPLOYEES.get(1);
    }
}
