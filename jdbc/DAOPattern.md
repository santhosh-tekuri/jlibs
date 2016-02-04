---
title: DAOPattern
layout: default
---

# J2EE DAO Pattern made easier

## Dependencies ###

~~~xml
<dependency>
    <groupId>in.jlibs</groupId>
    <artifactId>jlibs-jdbc</artifactId>
    <version>2.1</version>
</dependency> 

<dependency>
    <groupId>in.jlibs</groupId>
    <artifactId>jlibs-jdbc-apt</artifactId>
    <version>2.1</version>
    <optional>true</optional>
</dependency> 
~~~

`jlibs-jdbc-apt` contains annotation processor and is required only at *compile time*

## Annotating POJO ##

Create a POJO and map it to database using annotations

~~~java
package sample;

import jlibs.jdbc.annotations.*;

@Table
public class Employee{
    @Column(primary=true)
    public int id;
    
    @Column public String firstName;
    @Column public String lastName;
    @Column public int age;
    @Column public int experience;
    @Column public int salary;
}
~~~

## @Table Annotation ##

~~~java
@Table
public class Employee{
~~~

`@Table` annotation says that, the class `Employee` is mapped to `employees` table in database.

The name of table is derived from class name by joining words with underscore and converting to lowercase and plural.

You can also explicitly specify the table name as below:

~~~java
@Table(name="employees")
public class Employee{
~~~

## @Column Annotation ##

~~~java
@Column public String firstName;
~~~

here the property `firstName` is mapped to `first_name` in `employees` table.

We call `firstName` as column-property (i.e, property that is mapped to table column).

The name of the column is derived from property name by joining words with underscore and converting to lowercase

You can also explicitly specify the column name as below:

~~~java
@Column(name="first_name")
public String firstName;
~~~

The primary key can be specified by specifying `primary=true` as below:

~~~java
@Column(primary=true)
public int id;
~~~

If primary key is combination of multiple columns, specify `primary=true` on all those column properties;

If the primary key is auto-incremented value in database, then specify `auto=true` as below:

~~~java
@Column(primary=true, auto=true)
public int id;
~~~

`@Column` annotation can also be applied on `getXXX` or `setXXX` method rather than on field as below:

~~~java
private String firstName;
@Column(name="first_name")
public String getFirstName(){
    return firstName;
}
public void setFirstName(String firstName){
    this.firstName = firstName;
}
~~~

If you are using database specific sql type then set `nativeType=true`:

~~~java
import org.postgresql.util.PGmoney;

@Column(nativeType=true)
public PGmoney salary;
~~~

A class with `@Table` annotation must have atleast one member with `@Column` annotation.  
Otherwize you will get compilation error.

## Invalid Column-Property Type ##

Only the java types which have sql type mapping can be used for column-property. For example:

~~~java
@Column Properties defautls;
~~~

gives compilation error `java.util.Properties has no mapping SQL Type`.

Later will will know, how to use custom java types.

## Duplicate Column-Property Type ##

If two column-properties are mapped to same column as below:

~~~
@Column(name="first_name")
public String firstName;

@Column(name="first_name")
public String lastName;
~~~

gives compilation error. This can happen when you used copy/paste and forgot to change column name

## Validating POJOs with Database ##

Sometimes you might misspell table or column names in POJO definition.  
To avoid such mistakes, during compilation time you can ask to validate POJO definitions against database.

for this you specify database in `package-info.java` as follows:

~~~java
@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test")
package foo.model;

import jlibs.jdbc.annotations.Database;
~~~

`@Database` annotation gives all the information required to make database connection

If your database requires authentication, then use:

~~~java
@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test", user="santhosh", password="stillThinking")
~~~

here `@Database` annotation is defined on package `foo.model`. So this database is used to validate all POJOs in package `foo.model` and its sub-packages.

the following are validated in POJO definition at compile time:

- table name
- column names
- compatibility of column property tpe with the database column type
- check for new columns which are not defined in POJO

when new columns found in database which are not defined in POJO, it simply produces compilation warning.  
If you want to fail compilation in such scenarios, specify `failOnMissingColumns` attribute as follows:

~~~java
@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test", failOnMissingColumns=true)
~~~

## DataAccessObject (DAO) ##

Let us create DAO:

~~~java
package sample;

import jlibs.jdbc.*;
import org.apache.commons.dbcp.BasicDataSource;

public class DB{
    public static final BasicDataSource DATA_SOURCE;
    public static final DAO<Employee> EMPLOYEES;
    
    static{
        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setUrl("jdbc:mysql://localhost/test");
        JDBC jdbc = new JDBC(DATA_SOURCE);
        EMPLOYEES = DAO.create(Employee.class, jdbc);
    }
}
~~~

~~~java
JDBC jdbc = new JDBC(DATA_SOURCE);
~~~

`JDBC` is the class which executes the sql statements against the specified `DATA_SOURCE`.  
By default `JDBC` finds the `quoteString` from `DatabaseMetaData` and uses it to quote identifiers  
like table name, column name etc. To explicitly set `quoteString`:

~~~java
JDBC jdbc = new JDBC(DATA_SOURCE, "\"");
~~~

If the second argument is `null`, then identifiers are not quoted.

To print sql queries being executed, set system property `jlibs.jdbc.debug` to `true`.  
they are logged to `System.out`. This will be useful for debugging.

We created constants to all DAO's in class `DB`:

~~~java
EMPLOYEES = DAO.create(Employee.class, jdbc);
~~~

The above line creates DAO for `Employee` pojo on given `javax.sql.DataSource`.  
`jlibs.jdbc.DAO` is an abstract base class for all DAO's.  
It has common methods like `insert(...)`, `delete(...)` etc that are expected in all DAO's.

When you compile `Employee` class, `_EmployeeDAO.java` is generated and compiled.  
This class extends `jlibs.jdbc.DAO<Employee>` and implements the abstract methods.

## Inserting record ##

~~~java
import static sample.DB.*;

Employee emp = new Employee();
emp.id = 1;
emp.firstName = "santhosh";
emp.lastName = "kumar";
emp.age = 28;

EMPLOYEES.insert(emp);
~~~

let us say `id` of employee is defined as auto-increment as below:

~~~java
@Column(primary=true, auto=true)
public int id;
~~~

then after inserting employee, `emp.id` gives the actual `id` assigned by database:

~~~java
Employee emp = new Employee();
emp.firstName = "santhosh";
emp.lastName = "kumar";
emp.age = 28;

EMPLOYEES.insert(emp);
System.out.println("ID of new employee: "+emp.id);
~~~

## Updating record ##

~~~java
emp.age = 27;
EMPLOYEES.update(emp);
~~~

## Listing records ##

~~~java
List<Employee> employees = EMPLOYEES.all();
Employee emp = EMPLOYEES.first(); // get first employee from table
</code></pre>
Deleting records:
<pre><code>if(!EMPLOYEES.delete(emp))
    System.out.println("employee with id '"+emp.id+"' doesn't exist"); 
~~~

## Deleting all records ##

~~~java
int count = EMPLOYEES.delete();
System.out.println(count+" employees deleted"); 
~~~

## Upsert record ##

Upsert means: update if exists, otherwise insert

~~~java
import static sample.DB.*;

Employee emp = new Employee();
emp.id = 1;
emp.firstName = "Santhosh";
emp.lastName = "Kumar";
emp.age = 26;

EMPLOYEES.upsert(emp);
~~~

The above code updates the values of employee with id `1`.  
If employee with id `1` doesn't exist, it inserts new record.

## Custom Queries ##

Except `upsert(...)` all above methods in `DAO` support custom queries.


To find first 10 employees whose age is less than given age:

~~~java
int age = ...;
List<Employee> employees = employeeDAO.top(10, "where age<?", age);
~~~

if first argument to `top(...)` is zero, then it fetches all rows.

All `DAO` methods throw `jlibs.jdbc.DAOException`.  
This is runtime exception wrapping `SQLException`.

~~~java
int id = (Integer)EMPLOYEES.insert("(first_name, last_name) values(?, ?)", "james", "gosling");
int numberOfEmployeesUpdated = EMPLOYEES.update("set age=? where last_name=?", 25, "kumar");
int numberOfEmployeesDeleted = EMPLOYEES.delete("where age < ?", 20);
Employee emp = EMPLOYEES.first("where id=?", 5);
List<Employees> youngEmployees = EMPLOYEES.all("where age < ?", 21);
~~~

custom queries like above are good, there are are no compile time validations.  
let us say, if less aruments are specified than expected by the query, this mistake is not caught at compile time.

Let us see how to find such mistakes at compile time.

## @Select Annotation ##

Create a separate DAO for Employee, and add abstract methods for each custom query you want:

~~~java
package sample;

import jlibs.jdbc.*;
import javax.sql.*;

public abstract class EmployeeDAO extends DAO<Employee>{
    public EmployeeDAO(DataSource dataSource, TableMetaData table){
        super(dataSource, table);
    }
    
    @Select
    public abstract Employee findByID(int id);
}
~~~

**NOTE:**

- `EmployeeDAO` class is abstract and extends `DAO<Employee>`
- the constructor simply delegates to the constructor in superclass.

the abstract method `findById` is annotated with `@Select`.  
this says that this method does select query on database.

The method signature should follow some rules.  
When these rules are violated, you will get compilation error.

**Return Type:**

If you want to select single employee use return type as `Employee`.  
If you want to select list of employees use return type as `List<Employee>`.

**Parameters:**

The parameter names should match the column-proprty name in your POJO,  
and their type should match with the type of that column-property in POJO.

For example, here the method `findByID(...)` takes one parameter `int id`.  
Its name and type matches with the column-property `Employee.id` in POJO.

**Method Name:**

There is no restriction on method name.  
i.e you can name the method of your choice.

Exceptions:

The abstract query methods can only throw `jlibs.jdbc.DAOException` which is runtime exception.

We have seen that, when you compile `Employee.java`, the `_EmployeeDAO.java` generated extends `DAO<Employee>`.  
Now we have to say that `_EmployeeDAO.java` generated should extend `EmployeeDAO` that we have written.  
This is done by specifing `extend` attribute of `@Table` annotation:

~~~
@Table(name="employees", extend=EmployeeDAO.class)
public class Employee{
~~~

Now the generated `_EmployeeDAO` class extends our `EmployeeDAO` class and implements the abstract query method `findByID(...)`

If you open generated `_EmployeeDAO.java`, you can see the following implementation generated:

~~~java
@Override
public jlibs.jdbc.Employee findByID(long id){
    return first("where id=?", id);
}
~~~

To find employees by last name and age, you define abstract query method as below:

~~~java
@Select
public abstract List<Employee> findByLastNameAndAge(String lastName, int age);
~~~

and the generated implementation will be:

~~~java
@Override
public java.util.List<jlibs.jdbc.Employee> findByLastNameAndAge(java.lang.String lastName, int age){
    return all("where last_name=? and age=?", lastName, age);
}
~~~

If you want to find all employees with complex conditions:

~~~
@Select(sql="where #{age} between ${fromAge} and ${toAge} or #{lastName}=${lastName}")
public List<Employee> findByAgeOrLastName(int fromAge, int toAge, String lastName);
~~~

here we are specifying the sql query in @Select annotation.

`#{column-property}` in query will be replaced by its columnName  
i,e `#{age}` is replaced by `age` and `#{lastName}` is replaced by `last_name`

`${parameter-name}` in query will be replaced by its parameter value  
i,e `${fromAge}` is replaced by the value of fromAge passed to the method.

the generated implementation for the above method will be:

~~~java
@Override
public List<Employee> findByAgeOrLastName(int fromAge, int toAge, String lastName){
    return all("where age between ? and ? or last_name=?", fromAge, toAge, lastName);
}
~~~

Let us say we did a mistake as below:

~~~java
@Select(sql="where #{age} between ${fromAge} and ${toAge} or #{lastName}=${lastName}")
public List<Employee> findByAgeOrLastName(int fromAge, String toAge, String lastName);
~~~

here the second argument `toAge` is specified as `String`. But it is supposed to be `int`.  
But when you compile above method, it generates the following implementation without any compile time error:

~~~java
@Override
public List<Employee> findByAgeOrLastName(int fromAge, String toAge, String lastName){
    return all("where age between ? and ? or last_name=?", fromAge, toAge, lastName);
}
~~~

to add compile time validations to verify that the types are matching with their column types:

~~~java
@Select(sql="where #{age} between ${fromAge} and ${(age)toAge} or #{lastName}=${lastName}")
public List<Employee> findByAgeOrLastName(int fromAge, String toAge, int lastName);
~~~

Now on compiling the above method you get following compile time error:

~~~
toAge must be of type int/java.lang.Integer
~~~

Notice that, here rather than simply using `${toAge}` in query, we specified `${(age)toAge}`  
i.e, `${(column-property)parameter-name}` will verify that the type of parameter matches with  
the type of column property specified, during compile time.

## @Insert Annotation ##

~~~java
@Insert
public abstract void insert(int id, String firstName);
~~~

The parameter names should match the column-proprty name in your POJO,  
and their type should match with the type of that column-property in POJO.

the generated implementation will be:

~~~java
@Override
public void insert(int id, java.lang.String firstName){
    insert("(id, first_name) values(?, ?)", id, firstName);
}
~~~

The method can return either void or the POJO. for example:

~~~java
@Insert
public Employee insert(long id, String firstName, String lastName);
~~~

the generated implemenation will be:

~~~java
@Override
public jlibs.jdbc.Employee insert(long id, java.lang.String firstName, java.lang.String lastName){
    insert("(id, first_name, last_name) values(?, ?, ?)", id, firstName, lastName);
    return first("where id=?", id);
}
~~~

## @Delete Annotation ##

~~~java
@Delete
public abstract int delete(String firstName, String lastName);
~~~

The parameter names should match the column-proprty name in your POJO,  
and their type should match with the type of that column-property in POJO.

The return value will be the number of employees deleted;  
The method can return void, if you are not interested in that number.

the generated implementation will be:

~~~java
@Override
public int delete(java.lang.String firstName, java.lang.String lastName){
    return delete("where first_name=? and last_name=?", firstName, lastName);
}
~~~

If you want to use complex conditions:

~~~java
@Delete(sql="where #{age} between ${(age)fromAge} and ${(age)toAge} or #{lastName}=${(lastName)lastN}")
public abstract int delete(int fromAge, int toAge, String lastN);
~~~

the generated implementation will be:

~~~java
@Override
public int delete(int fromAge, int toAge, java.lang.String lastN){
    return delete("where age between ? and ? or last_name=?", fromAge, toAge, lastN);
}
~~~

## @Update Annotation ##

~~~java
@Update
public abstract int update(int age, String where_firstName, String where_lastName);
~~~

The parameter names should match the column-proprty name in your POJO,  
and their type should match with the type of that column-property in POJO.  
the parameters to be used in `WHERE` condition should be prefixed with `where_`.

The return value will be the number of employees updated;  
The method can return void, if you are not interested in that number.

the generated implementation will be:

~~~java
@Override
public int update(int age, java.lang.String where_firstName, java.lang.String where_lastName){
    return update("set age=? where first_name=? and last_name=?", id, age, where_firstName, where_lastName);
}
~~~

If you want to use complex conditions:

~~~java
@Update(sql="set #{lastName}=${(lastName)lastN} where #{age} between ${(age)fromAge} and ${(age)toAge}")
public abstract int updateLastName(int fromAge, int toAge, String lastN);
~~~

the generated implementation will be:

~~~java
@Override
public int updateLastName(int fromAge, int toAge, java.lang.String lastN){
    return update("set last_name=? where age between ? and ?", lastN, fromAge, toAge);
}
~~~

## @Upsert Annotation ##

This is similar to `@Update` except that the method should return `void`.

If the record is found in database it is updated, otherwise new record is inserted.

## WHERE Condition ##

various sql operators can be specified by using prefixes for method parameters.

for example to find all employees whose age is less than specified age:

~~~java
@Select
public abstract List<Employee> findYoungEmployees(int lt_age);
~~~

here the prefix is `lt_` and column property is `age`.  
the prefix `lt_` says to use the `<` sql operator on `age` column

to delete all employees between specified ages:

~~~java
@Delete
public abstract int deleteEmployeesByAge(int from_age, int to_age);
~~~

to update experience of employees between specified ages:

~~~java
@Update
public abstract int updateEmployeesByAge(int from_age, int to_age, int experience);
~~~

The supported operators and the prefixes to use are:

~~~
| Prefix     | Operator        |
|------------|----------------:|
| where_     | = ?             |
| eq_        | = ?             |
| ne_        | <> =            |
| lt_        | < ?             |
| le_        | <= ?            |
| gt_        | > ?             |
| ge_        | >= ?            |
| like_      | LIKE ?          |
| nlike_     | NOT LIKE ?      |
| from_, to_ | BETWEEN ? AND ? |
~~~

This technique of using prefixes, will avoid writing custom queries to large extent.

## Selecting Single Column ##

Some times you might want value of a specific column rather than values of all columns.

to find first name of employee of given id:

~~~java
@Select(column="firstName")
public abstract String getFirstName(int id);
~~~

Notice that the value of `column` attribute is `firstName` (not `first_name`).  
i.e value of `column` attribute is name of the column property to be selected.

if there is no employee with the specified `id`, the above method returns `null`.  
if the first name of employee with specified `id` is `NULL` in database, the above method returns `null`

in order to differentiate above to cases:

~~~java
@Select(column="firstName", assertMinimumCount=1)
public abstract String getFirstName(int id);
~~~

the attribute `assertMinimumCount` ensures that there are atleast specified number of records.  
Now the above method throws `jlibs.jdbc.IncorrectResultSizeException`, if there is no employee with given `id`.  
`jlibs.jdbc.IncorrectResultSizeException` is subclass of `jlibs.jdbc.DAOException`

the `assertMinimumCount` can also be used when selecting entire records:

~~~java
@Select(assertMinimumCount=5)
public abstract List<Employee> findByAge(int from_age, int to_age);
~~~

if you want to find first names of employees:

~~~java
@Select(column="firstName")
public abstract List<String> findFirstNames(int from_age, int to_age);
~~~

## Selecting Expression ##

to find number of employees with age greater than specified:

~~~java
@Select(expression="count(*)")
public abstract int countOlderEmployees(int gt_age);
~~~

to find sum of salaries of all employees:

~~~java
@Select(expression="sum(#{salary})")
public abstract int totalSalary();
~~~

the method can also return `List`:

~~~java
@Select(expression="#{age}-#{experience}")
public abstract List<Integer> calculateEquation1();
~~~

## Dynamic SQL ##

~~~java
@Select(ignoreNullConditions=true)
public abstract List<Employee> search(String firstName, String lastName, Integer age, Integer experience);
~~~

`ignoreNullConditions=true` says that don't include the conditions whose values are null.  
i.e

~~~java
EMPLOYEES.search(null, null, 28, null); // search by age
EMPLOYEES.search(null, null, 35, 5); // search by age and experience
~~~

the generated implementation will be as below:

~~~java
@Override
public List<Employee> search(String firstName, String lastName, Integer age, Integer experience){
    java.util.List<String> __conditions = new java.util.ArrayList<String>(4);
    java.util.List<Object> __params = new java.util.ArrayList<Object>(4);
    if(firstName!=null){
        __conditions.add("first_name=?");
        __params.add(firstName);
    }
    if(lastName!=null){
        __conditions.add("last_name=?");
        __params.add(lastName);
    }
    if(age!=null){
        __conditions.add("age=?");
        __params.add(age);
    }
    if(experience!=null){
        __conditions.add("experience=?");
        __params.add(experience);
    }
    String __query = null;
    if(__conditions.size()>0)
        __query = " WHERE " + jlibs.core.lang.StringUtil.join(__conditions.iterator(), " AND ");
    return all(__query, __params.toArray());
}
~~~

`ignoreNullConditions` attribute is supported on `@Select`, `@Delete` and `@Update`

## Custom Java Types ##

let us say `employees` table in database has column named `grade` of type number.  
You don't want to use `int` for `grade` property in `Employee` class, rather you want  
to use following `enum`.

~~~java
public enum Grade{
    JUNIOR, SENIOR, LEAD, MANAGER
}
~~~

because `Grade` is not one of the java types supported by JDBC,  
you need to create a mapping as below:

~~~java
import jlibs.jdbc.JDBCTypeMapper;

public class GradeTypeMapper implements JDBCTypeMapper<Grade, Integer>{
    @Override
    public Grade nativeToUser(Integer nativeValue){
        if(nativeValue==null)
            return null;
        else
            return Grade.values()[nativeValue];
    }

    @Override
    public Integer userToNative(Grade userValue){
        if(userValue==null)
            return null;
        else
            return userValue.ordinal();
    }
}
~~~

The above class maps `Grade` with `Integer`.  
`Grade` is called User Type.  
`Integer` is called Native Type.

Now in `Employee` class you need to specify `grade` property to use above mapping:

~~~java
@Table
public class Employee{
    ...
    
    @Column
    @TypeMapper(mapper=GradeTypeMapper.class, mapsTo=Integer.class)
    public Grade grade;
}
~~~

if you are using custom queries, you must specify column-property for parameter which is non-native type:

~~~java
@Select(expression="count(*)", sql="WHERE #{grade}=${(grade)grade} AND #{age}<${age}")
public abstract int countByGradeAndAge(Grade grade, int age);
~~~

Notice that we used `${(grade)grade}` in query rather than `${grade}`.  
otherwise, you will get following compilation error:

~~~
the column property must be specified for parameter grade in query.
~~~

## Sorting by Column ##

to sort employees by experience:

~~~java
@Select(orderBy=@OrderBy(column="experience", order=Order.DESCENDING))
public abstract List<Employee> youngEmployees(int le_age);
~~~

the `order` attribute is optional and defaults to `Order.ASCENDING`.

to sort by multiple columns:

~~~java
@Select(orderBy={
    @OrderBy(column="experience", order=Order.DESCENDING),
    @OrderBy(column="age", order=Order.DESCENDING)
})
public abstract List<Employee> youngEmployees(int le_age);
~~~

## Paging ##

Suppose you are searching employees in databse, the number of employees maching your criteria might be large.  
You don't want list all employees, but you want to page though the search results:

~~~java
@Select(ignoreNullConditions=true, pageBy=@OrderBy(column="id", order=Order.DESCENDING))
public abstract Paging<Employee> pageById(String firstName, String lastName, Integer age, int experience);
~~~

`pageBy` attribute specifies that you want to page through results.  
when `pageBy` attribute specified the method return type should be `jlibs.jdbc.Paging`.

In order to page through the results, you need to sort the results by set of columns whose combination of  
values is unique. In above method, we are paging by column property `id` and `id` is unique for each employee.

Now you can do paging as follows:

~~~java
Paging<Employee> paging = EMPLOYEES.pageById(null, null, 35, 5);
Page<Employee> page = paging.createPage(10); // page size is 10

int totalRowCount = page.getTotalRowCount();
int totalPageCount = getTotalPageCount();
~~~

to Navigate the page:

~~~java
List<Employee> firstPage = page.navigate(Page.Action.FIRST);
List<Employee> secondPage = page.navigate(Page.Action.NEXT);
List<Employee> lastPage = page.navigate(Page.Action.LAST);
List<Employee> lastButOnePage = page.navigate(Page.Action.PREVIOUS);
~~~

`Page.Action` is an enum.  
to find whether you can navigation in given direction:

~~~java
boolean enableNextButton = page.canNavigate(Page.Action.NEXT);
~~~

Note that, if the total number of rows is zero, you can't navigate in any direction.  
`page.navigate(...)` throws `IllegalArgumentException` if the specified navigation action is not possible.

`page.getIndex()` returns the index of current page. The indexing starts from zero.  
`page.getIndex()` returns `-1` if the page is created but not navigated yet.

Note that you can't jump to a given page.

## Paging in Servlet/JSP ##

You can place the page object in session and use it. But if user is paging simultaniously  
by different crieterias, you will end up multiple page objects in session and becomes impossible  
to know which page object from session to use.

In order to overcome this situation, you can pass the information required to create current page
from request to request using POST.

the information required to construct page at given index are:

~~~java
page.getIndex();
page.getTotalRowCount();
page.getFirstRow(); // returns Employee
page.getLastRow(); // returns Employee
~~~

all above methods have their corresponding set methods.  
For first and last rows, it is enough to fill only properties of the `Employee` object which are used for paging.  
i.e `Employee.id` for above example.

## Transactions ##

To run multiple statements in a transaction:

~~~java
import static sample.DB.*;

Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction<Object>(){
    @Override
    public Object run(Connection con) throws SQLException{
        // invoke some DAO/DB actions
        // return something
    }
});
~~~

If `Transaction.run(...)` throws exception, then the current transaction is rolled back.

What happens when transactions are nested as below:

~~~java
public void method1(){
    Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction<Object>(){
        @Override
        public Object run(Connection con) throws SQLException{
            // invoke some DAO/DB actions
            method2();
            // return something
        }
    });
}

public void method2(){
    Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction<Object>(){
        @Override
        public Object run(Connection con) throws SQLException{
            // invoke some DAO/DB actions
            // return something
        }
    });
}
~~~

here both both `method1()` and `method1()` are using transactions. Notice that `method1()` is calling `method2()`.  
so when `method1` is invoked, `method2()` will run in the same transaction that is started by `method1()`.

## Pros of this library ##

- no reflection is used at runtime
- you need to write sql only in rare cases
- column names are not hardcode in queries
- strict compile-time checks
- supports custom java types
- supports dynamic queries
- paging support
