/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.examples.jdbc;

import jlibs.jdbc.DAO;
import jlibs.jdbc.JDBC;
import jlibs.jdbc.Transaction;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class DB{
    public static final JDBC JDBC;
    public static final EmployeeDAO EMPLOYEES;

    static{
        BasicDataSource ds = new BasicDataSource();
 	    ds.setUrl("jdbc:hsqldb:file:examples/db/demo");

        JDBC = new JDBC(ds, null);
        EMPLOYEES = (EmployeeDAO) DAO.create(Employee.class, JDBC);
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
        emp.id = 1;
        emp.setFirstName("santhosh");
        emp.setLastName("kumar");
        emp.setAge(25);
        EMPLOYEES.insert(emp);

        assert EMPLOYEES.all().size()==1;
        emp.setAge(20);
        EMPLOYEES.update(emp);
        assert EMPLOYEES.all().get(0).getAge()==20;

        try{
            JDBC.run(new Transaction<Object>(){
                @Override
                public Object run(Connection con) throws SQLException{
                    Employee emp = new Employee();
                    emp.id = 2;
                    emp.setFirstName("santhosh");
                    emp.setLastName("kumar");
                    emp.setAge(25);
                    EMPLOYEES.insert(emp);

                    emp.id = 3;
                    EMPLOYEES.insert(emp);

                    assert EMPLOYEES.all().size() == 3;
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
        assert EMPLOYEES.first("where id=?", emp.id).getAge()==10;
        emp.id = -1;
        emp.setLastName("KUMAR");
        EMPLOYEES.upsert(emp);
        assert EMPLOYEES.first("where last_name=?", "KUMAR")!=null;
        assert EMPLOYEES.all().size()==2;


        List<Employee> list = EMPLOYEES.all();
        list.get(0).setAge(29);
        EMPLOYEES.update(list.get(0));
        EMPLOYEES.delete(list.get(0));
//        List<Employee> list = table.findYoungEmployees(50);
//        Employee emp1 = EMPLOYEES.get(1);
    }
}
