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

import jlibs.jdbc.annotations.Column;
import jlibs.jdbc.annotations.Table;
import jlibs.jdbc.annotations.TypeMapper;

/**
 * @author Santhosh Kumar T
 */
@Table(extend= EmployeeDAO.class)
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
