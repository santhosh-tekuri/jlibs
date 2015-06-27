/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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
import jlibs.jdbc.Order;
import jlibs.jdbc.TableMetaData;
import jlibs.jdbc.annotations.*;
import jlibs.jdbc.paging.Paging;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class EmployeeDAO extends DAO<Employee> {
    public EmployeeDAO(JDBC jdbc, TableMetaData table){
        super(jdbc, table);
    }

    @Select(column="grade")
    public abstract Grade findGrade(long id);

    @Select(column="grade", assertMinimumCount =1)
    public abstract Grade findGrade1(long id);

    @Select(column="grade")
    public abstract List<Grade> findGrades(int lt_age);

    @Select(expression="count(*)", sql="WHERE #{grade}=${(grade)grade} AND #{age}<${age}")
    public abstract int countByGradeAndAge(Grade grade, int age);

    @Update
    public abstract void updateGrade(long where_id, Grade grade);

    @Select(ignoreNullConditions=true, pageBy=@OrderBy(column="id", order= Order.DESCENDING))
    public abstract Paging<Employee> pageById(String firstName, String lastName, Integer age, int experience);

    @Select(ignoreNullConditions=true, orderBy=@OrderBy(column="id", order=Order.DESCENDING))
    public abstract List<Employee> searchOrderById(String firstName, String lastName, Integer age, int experience);

    @Select(ignoreNullConditions=true)
    public abstract List<Employee> search(String firstName, String lastName, Integer age, int experience);

    @Update(ignoreNullConditions=true)
    public abstract int updateAgeAndExperience(String where_firstName, String where_lastName, int age, Integer experience);

//    @Upsert(ignoreNullConditions=true)
//    public abstract void upsertAgeAndExperience(String where_firstName, String where_lastName, int age, Integer experience);

    @Select(expression="count(*)")
    public abstract int total();

    @Select(expression="sum(#{experience})")
    public abstract int experienceSum(int lt_age);

    @Select(expression="#{age}-#{experience}")
    public abstract List<Integer> ageMinusExperiences();

    @Select(column="firstName")
    public abstract String findFirstName(long id);

    @Select(column="firstName")
    public abstract List<String> findFirstNames(long id);

    @Select(column="experience")
    public abstract int findExperience(long id);

    @Select(column="experience")
    public abstract Integer findExperience1(long id);

    @Select(column="experience", assertMinimumCount =1)
    public abstract Integer findExperience2(long id);

    @Select(column="experience")
    public abstract List<Integer> findExperiences(long id);

    @Select(column="experience", assertMinimumCount =5)
    public abstract List<Integer> findExperiences1(long id);

    @Select
    public abstract Employee findByID(long id);

    @Select(orderBy=@OrderBy(column = "experience"))
    public abstract List<Employee> findByAgeOrderByExperience(int age);

    @Select
    public abstract List<Employee> findByAge(int age);

    @Select
    public abstract List<Employee> find(String firstName, String lastName);

    @Insert
    public abstract void insert1(long id, String firstName, String lastName);

    @Insert
    public abstract Employee insert2(long id, String firstName, String lastName, Grade grade);

    @Insert
    public abstract void insert(String firstName, int age);
    
    @Update
    public abstract int update1(long id, int age, String where_firstName, String where_lastName);

    @Update
    public abstract int update2(int age, String where_firstName, long id, String where_lastName);

    @Upsert
    public abstract void upsert1(long id, int age, String where_firstName, String where_lastName, Grade where_grade);

    @Upsert
    public abstract void upsert2(long id, int age, String where_firstName, String where_lastName);

    @Delete
    public abstract int delete(String firstName, String lastName);

    @Delete
    public abstract int delete(String firstName, int age);

    @Delete(sql="WHERE #{age} BETWEEN ${fromAge} AND ${toAge} OR #{lastName}=${lastN}")
    public abstract int delete(int fromAge, int toAge, String lastN);
}
