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

import jlibs.jdbc.annotations.*;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class EmployeeDAO extends DAO<Employee>{
    public EmployeeDAO(DataSource dataSource, TableMetaData table){
        super(dataSource, table);
    }

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

    @Select(column="experience", assertMinmumCount=1)
    public abstract Integer findExperience2(long id);

    @Select(column="experience")
    public abstract List<Integer> findExperiences(long id);

    @Select(column="experience", assertMinmumCount=5)
    public abstract List<Integer> findExperiences1(long id);

    @Select
    public abstract Employee findByID(long id);

    @Select
    public abstract List<Employee> findByAge(int age);

    @Select
    public abstract List<Employee> find(String firstName, String lastName);

    @Insert
    public abstract void insert1(long id, String firstName, String lastName);

    @Insert
    public abstract Employee insert2(long id, String firstName, String lastName);

    @Insert
    public abstract void insert(String firstName, int age);
    
    @Update
    public abstract int update1(long id, int age, String where_firstName, String where_lastName);

    @Update
    public abstract int update2(int age, String where_firstName, long id, String where_lastName);

    @Upsert
    public abstract void upsert1(long id, int age, String where_firstName, String where_lastName);

    @Upsert
    public abstract void upsert2(long id, int age, String where_firstName, String where_lastName);

    @Delete
    public abstract int delete(String firstName, String lastName);

    @Delete
    public abstract int delete(String firstName, int age);

    @Delete("WHERE #{age} BETWEEN ${fromAge} AND ${toAge} OR #{lastName}=${lastN}")
    public abstract int delete(int fromAge, int toAge, String lastN);
}
