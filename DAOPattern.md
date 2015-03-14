**Table of Contents**



---

## Annotating POJO ##

Create a POJO and map it to database using annotations
```
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
```


---

## @Table Annotation ##

```
@Table
public class Employee{
```

`@Table` annotation says that, the class `Employee` is mapped to `employees` table in database.

The name of Table is derived from class name by joining words with <br>
underscore and converting to lowercase and plural.<br>
<br>
You can also explicitly specify the table name as below:<br>
<pre><code>@Table(name="employees")<br>
public class Employee{<br>
</code></pre>

<hr />
<h2>@Column Annotation</h2>
<pre><code>@Column public String firstName;<br>
</code></pre>

here we are saying that the property <code>firstName</code> is mapped to <code>first_name</code> in <code>employees</code> table.<br>
We call <code>firstName</code> as column-property (i.e, property that is mapped to table column).<br>
<br>
The name of the column is derived from property name by joining words with underscore and converting to lowercase.<br>
You can also explicitly specify the column name as below:<br>
<pre><code>@Column(name="first_name")<br>
public String firstName;<br>
</code></pre>

The primary key can be specified by specifying <code>primary=true</code> as below.<br>
<pre><code>@Column(primary=true)<br>
public int id;<br>
</code></pre>
If primary key is combination of multiple columns, specify <code>primary=true</code> on all those column properties;<br>
<br>
If the primary key is auto-incremented value in databse, then specify <code>auto=true</code> as below:<br>
<pre><code>@Column(primary=true, auto=true)<br>
public int id;<br>
</code></pre>

<code>@Column</code> annotation can also be applied on <code>getXXX</code> or <code>setXXX</code> method rather than on field as below:<br>
<pre><code>private String firstName;<br>
@Column(name="first_name")<br>
public String getFirstName(){<br>
    return firstName;<br>
}<br>
public void setFirstName(String firstName){<br>
    this.firstName = firstName;<br>
}<br>
</code></pre>

If you are using database specific sql type then set <code>nativeType</code> to <code>true</code>:<br>
<pre><code>import org.postgresql.util.PGmoney;<br>
<br>
@Column(nativeType=true)<br>
public PGmoney salary;<br>
</code></pre>

A class with <code>@Table</code> annotation must have atleast one member with <code>@Column</code> annotation.<br>
Otherwize you will get compilation error.<br>
<br>
<hr />
<h2>Invalid Column-Property Type</h2>

Only the java types which have sql type mapping can be used for column-property. For example:<br>
<pre><code>@Column Properties defautls;<br>
</code></pre>
gives compilation error <code>java.util.Properties has no mapping SQL Type</code>.<br>
<br>
Later will will know, how to use custom java types.<br>
<hr />
<h2>Duplicate Column-Property Type</h2>

If two column-properties are mapped to same column as below:<br>
<pre><code>@Column(name="first_name")<br>
public String firstName;<br>
<br>
@Column(name="first_name")<br>
public String lastName;<br>
</code></pre>
gives compilation error. This can happen when you used copy/paste and forgot to change column name.<br>
<br>
<hr />
<h2>Validating POJOs with Database <a href='r946.md'>:r946</a></h2>

Sometimes you might misspell table or column names in POJO definition.<br>
To avoid such mistakes, during compilation time you can ask to validate POJO definitions<br>
against database.<br>
<br>
First you define database in <code>package-info.java</code> as follows:<br>
<pre><code>@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test")<br>
package foo.model;<br>
<br>
import jlibs.jdbc.annotations.Database;<br>
</code></pre>

the <code>@Database</code> annotation gives all the information required to make database connection.<br>

If your database requires authentication, then use:<br>
<pre><code>@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test", user="santhosh", password="notYetDecided")<br>
</code></pre>

here <code>@Database</code> annotation is defined on package <code>foo.model</code>. So this database is used to<br>
validate all POJOs in package <code>foo.model</code> and its sub-packages.<br>
<br>
the following are validated in POJO definition:<br>
<ul><li>table name<br>
</li><li>column names<br>
</li><li>compatibility of column property tpe with the database column type<br>
</li><li>check for new columns which are not defined in POJO.</li></ul>

when new columns found in database which are not defined in POJO, it simply produces compilation warning.<br>
If you want to fail compilation in such scenarios, specify <code>failOnMissingColumns</code> attribute as follows:<br>
<pre><code>@Database(driver=com.mysql.jdbc.Driver.class , url="jdbc:mysql://localhost/test", failOnMissingColumns=true)<br>
</code></pre>

<hr />
<h2><code>DataAccessObject</code></h2>

Let us create DAO:<br>
<pre><code>package sample;<br>
<br>
import jlibs.jdbc.*;<br>
import org.apache.commons.dbcp.BasicDataSource;<br>
<br>
public class DB{<br>
    public static final BasicDataSource DATA_SOURCE;<br>
    public static final DAO&lt;Employee&gt; EMPLOYEES;<br>
    <br>
    static{<br>
        DATA_SOURCE = new BasicDataSource();<br>
        DATA_SOURCE.setUrl("jdbc:mysql://localhost/test");<br>
        JDBC jdbc = new JDBC(DATA_SOURCE);<br>
        EMPLOYEES = DAO.create(Employee.class, jdbc);<br>
    }<br>
}<br>
</code></pre>
<pre><code>JDBC jdbc = new JDBC(DATA_SOURCE);<br>
</code></pre>
<code>JDBC</code> is the class which executes the sql statements against the specified <code>DATA_SOURCE</code>.<br>
By default <code>JDBC</code> finds the <code>quoteString</code> from <code>DatabaseMetaData</code> and uses it to quote identifiers<br>
like table name, column name etc. If you want to hardcode the <code>quoteString</code>, you can do:<br>
<pre><code>JDBC jdbc = new JDBC(DATA_SOURCE, "\"");<br>
</code></pre>
If the second argument is null, then identifiers are not quoted.<br>
<br>
if system property <code>jlibs.jdbc.debug</code> is set to <code>true</code>, then all the sql queries executed by <code>JDBC</code><br>
will be logged to <code>System.out</code>. This will be useful for debugging.<br>
<br>
We created class <code>DB</code> with constants to all DAO's.<br>
<pre><code>EMPLOYEES = DAO.create(Employee.class, jdbc);<br>
</code></pre>
The above line creates DAO for <code>Employee</code> pojo on given <code>javax.sql.DataSource</code>.<br>
<code>jlibs.jdbc.DAO</code> is an abstract base class for all DAO's.<br>
It has common methods like <code>insert(...)</code>, <code>delete(...)</code> etc that are expected in all DAO's.<br>
<br>
When you compile <code>Employee</code> class, <code>_EmployeeDAO.java</code> is generated and compiled.<br>
This class extends <code>jlibs.jdbc.DAO&lt;Employee&gt;</code> and implements the abstract methods.<br>
<br>
Let us see how to use <code>DAO</code>.<br>
<hr />
<h2>CRUD Operations</h2>

Inserting record:<br>
<pre><code>import static sample.DB.*;<br>
<br>
Employee emp = new Employee();<br>
emp.id = 1;<br>
emp.firstName = "santhosh";<br>
emp.lastName = "kumar";<br>
emp.age = 28;<br>
<br>
EMPLOYEES.insert(emp);<br>
</code></pre>

let us say <code>id</code> of employee is defined as auto-increment as below:<br>
<pre><code>@Column(primary=true, auto=true)<br>
public int id;<br>
</code></pre>
then after inserting employee, its <code>id</code> value will be updated with the actual <code>id</code> assigned by database.<br>
<pre><code>Employee emp = new Employee();<br>
emp.firstName = "santhosh";<br>
emp.lastName = "kumar";<br>
emp.age = 28;<br>
<br>
EMPLOYEES.insert(emp);<br>
System.out.println("ID of new employee: "+emp.id);<br>
</code></pre>

Updating record:<br>
<pre><code>emp.age = 27;<br>
EMPLOYEES.update(emp);<br>
</code></pre>
Listing records:<br>
<pre><code>List&lt;Employee&gt; employees = EMPLOYEES.all();<br>
Employee emp = EMPLOYEES.first(); // get first employee from table<br>
</code></pre>
Deleting records:<br>
<pre><code>if(!EMPLOYEES.delete(emp))<br>
    System.out.println("employee with id '"+emp.id+"' doesn't exist"); <br>
</code></pre>
Deleting all records:<br>
<pre><code>int count = EMPLOYEES.delete();<br>
System.out.println(count+" employees deleted"); <br>
</code></pre>

Update if exists, otherwise insert (i.e, upsert):<br>
<pre><code>import static sample.DB.*;<br>
<br>
Employee emp = new Employee();<br>
emp.id = 1;<br>
emp.firstName = "Santhosh";<br>
emp.lastName = "Kumar";<br>
emp.age = 26;<br>
<br>
EMPLOYEES.upsert(emp);<br>
</code></pre>
The above code updates the values of employee with id <code>1</code>.<br>
If employee with id <code>1</code> doesn't exist, it inserts new record.<br>
<br>
to select only specified number of rows rather than all from database.<br>
for example, to find first 10 employees whose age is less than specified:<br>
<pre><code>int age = ...;<br>
List&lt;Employee&gt; employees = employeeDAO.top(10, "where age&lt;?", age);<br>
</code></pre>
if first argument to <code>top(...)</code> is zero, then it fetches all rows.<br>
<br>
All <code>DAO</code> methods throw <code>jlibs.jdbc.DAOException</code>.<br>
This is runtime exception wrapping SQLException.<br>
<hr />
<h2>Custom Queries</h2>

Except <code>upsert(...)</code> all above methods in <code>DAO</code> support custom queries.<br>
<pre><code>int id = (Integer)EMPLOYEES.insert("(first_name, last_name) values(?, ?)", "james", "gosling");<br>
int numberOfEmployeesUpdated = EMPLOYEES.update("set age=? where last_name=?", 25, "kumar");<br>
int numberOfEmployeesDeleted = EMPLOYEES.delete("where age &lt; ?", 20);<br>
Employee emp = EMPLOYEES.first("where id=?", 5);<br>
List&lt;Employees&gt; youngEmployees = EMPLOYEES.all("where age &lt; ?", 21);<br>
</code></pre>

Rather than doing custom queries like this, you can use the following technique, <br>
which provides more compile time validations.<br>
<hr />
<h2>@Select Annotation</h2>

Create a separator DAO for Employee, and add abstract methods for each custom query you want:<br>
<pre><code>package sample;<br>
<br>
import jlibs.jdbc.*;<br>
import javax.sql.*;<br>
<br>
public abstract class EmployeeDAO extends DAO&lt;Employee&gt;{<br>
    public EmployeeDAO(DataSource dataSource, TableMetaData table){<br>
        super(dataSource, table);<br>
    }<br>
    <br>
    @Select<br>
    public abstract Employee findByID(int id);<br>
}<br>
</code></pre>

Here we created <code>abstract EmployeeDAO</code> class which extends <code>DAO&lt;Employee&gt;</code>.<br>
<pre><code>public abstract class EmployeeDAO extends DAO&lt;Employee&gt;{<br>
</code></pre>
The constructor simply delegates to the constructor in superclass.<br>
<pre><code>public EmployeeDAO(DataSource dataSource, TableMetaData table){<br>
    super(dataSource, table);<br>
}<br>
</code></pre>
Next, we added an abstract query method:<br>
<pre><code>@Select<br>
public abstract Employee findByID(int id);<br>
</code></pre>
<code>@Select</code> annotation on this method says that you want to use <code>SELECT</code> query.<br>
<br>
The method signature should follow some rules.<br>
When these rules are violated, you will get compilation error.<br>
<br>
<b>Return Type:</b>

If you want to select single employee use return type as <code>Employee</code>.<br>
If you want to select list of employees use return type as <code>List&lt;Employee&gt;</code>.<br>
<br>
<b>Parameters:</b>

The parameter names should match the column-proprty name in your POJO,<br>
and their type should match with the type of that column-property in POJO.<br>
<br>
For example, here the method <code>findByID(...)</code> takes one parameter <code>int id</code>.<br>
Its name and type matches with the column-property <code>Employee.id</code> in POJO.<br>
<br>
<b>Method Name:</b>

There is no restriction on method name.<br>
i.e you can name the method of your choice.<br>
<br>
<b>Exceptions:</b>

The abstract query methods can only throw <code>jlibs.jdbc.DAOException</code> which is runtime exception.<br>
<br>
We have seen that, when you compile <code>Employee.java</code>, the <code>_EmployeeDAO.java</code> generated extends <code>DAO&lt;Employee&gt;</code>.<br>
Now we have to say that <code>_EmployeeDAO.java</code> generated should extend <code>EmployeeDAO</code> that we have written.<br>
This is done by specifing <code>extend</code> attribute of <code>@Table</code> annotation<br>
<pre><code>@Table(name="employees", extend=EmployeeDAO.class)<br>
public class Employee{<br>
</code></pre>
Now the generated <code>_EmployeeDAO</code> class extends our <code>EmployeeDAO</code> class and implements the abstract query method <code>findByID(...)</code>

If you open <code>_EmployeeDAO.java</code>, you can see the following implementation generated:<br>
<pre><code>@Override<br>
public jlibs.jdbc.Employee findByID(long id){<br>
    return first("where id=?", id);<br>
}<br>
</code></pre>

To find employees by last name and age, you define abstract query method as below:<br>
<pre><code>@Select<br>
public abstract List&lt;Employee&gt; findByLastNameAndAge(String lastName, int age);<br>
</code></pre>
and the generated implementation will be:<br>
<pre><code>@Override<br>
public java.util.List&lt;jlibs.jdbc.Employee&gt; findByLastNameAndAge(java.lang.String lastName, int age){<br>
    return all("where last_name=? and age=?", lastName, age);<br>
}<br>
</code></pre>

If you want to find all employees with complex conditions:<br>
<pre><code>@Select(sql="where #{age} between ${fromAge} and ${toAge} or #{lastName}=${lastName}")<br>
public List&lt;Employee&gt; findByAgeOrLastName(int fromAge, int toAge, String lastName);<br>
</code></pre>
here we are specifying the sql query in @Select annotation.<br>

<code>#{column-property}</code> in query will be replaced by its columnName<br>
i,e #{age} is replaced by <code>age</code> and #{lastName} is replaced by <code>last_name</code>

<code>${parameter-name}</code> in query will be replaced by its parameter value<br>
i,e ${fromAge} is replaced by the value of fromAge passed to the method.<br>
<br>
the generated implementation for the above method will be:<br>
<pre><code>@Override<br>
public List&lt;Employee&gt; findByAgeOrLastName(int fromAge, int toAge, String lastName){<br>
    return all("where age between ? and ? or last_name=?", fromAge, toAge, lastName);<br>
}<br>
</code></pre>

Let us say we did a mistake as below:<br>
<pre><code>@Select(sql="where #{age} between ${fromAge} and ${toAge} or #{lastName}=${lastName}")<br>
public List&lt;Employee&gt; findByAgeOrLastName(int fromAge, String toAge, String lastName);<br>
</code></pre>
here the second parameter <code>toAge</code> is specified as <code>String</code>. But it is supposed to be <code>int</code>.<br>
But when you compile above method, it generates the following implementation without any compile time error:<br>
<pre><code>@Override<br>
public List&lt;Employee&gt; findByAgeOrLastName(int fromAge, String toAge, String lastName){<br>
    return all("where age between ? and ? or last_name=?", fromAge, toAge, lastName);<br>
}<br>
</code></pre>
to add compile time validations to verify that the types are matching with their column types:<br>
<pre><code>@Select(sql="where #{age} between ${fromAge} and ${(age)toAge} or #{lastName}=${lastName}")<br>
public List&lt;Employee&gt; findByAgeOrLastName(int fromAge, String toAge, int lastName);<br>
</code></pre>
Now on compiling the above method you get following compile time error:<br>
<pre><code>toAge must be of type int/java.lang.Integer<br>
</code></pre>
Notice that, here rather than simply using <code>${toAge}</code> in query, we specified <code>${(age)toAge}</code><br>
i.e, <code>${(column-property)parameter-name}</code> will verify that the type of parameter matches with<br>
the type of column property specified, during compile time.<br>
<br>
<hr />
<h2>@Insert Annotation</h2>

<pre><code>@Insert<br>
public abstract void insert(int id, String firstName);<br>
</code></pre>

The parameter names should match the column-proprty name in your POJO,<br>
and their type should match with the type of that column-property in POJO.<br>
<br>
the generated implementation will be:<br>
<pre><code>@Override<br>
public void insert(int id, java.lang.String firstName){<br>
    insert("(id, first_name) values(?, ?)", id, firstName);<br>
}<br>
</code></pre>

The method can return either void or the POJO. for example:<br>
<pre><code>@Insert<br>
public Employee insert(long id, String firstName, String lastName);<br>
</code></pre>
the generated implemenation will be:<br>
<pre><code>@Override<br>
public jlibs.jdbc.Employee insert(long id, java.lang.String firstName, java.lang.String lastName){<br>
    insert("(id, first_name, last_name) values(?, ?, ?)", id, firstName, lastName);<br>
    return first("where id=?", id);<br>
}<br>
</code></pre>

<hr />
<h2>@Delete Annotation</h2>

<pre><code>@Delete<br>
public abstract int delete(String firstName, String lastName);<br>
</code></pre>

The parameter names should match the column-proprty name in your POJO,<br>
and their type should match with the type of that column-property in POJO.<br>
<br>
The return value will be the number of employees deleted;<br>
The method can return void, if you are not interested in that number.<br>
<br>
the generated implementation will be:<br>
<pre><code>@Override<br>
public int delete(java.lang.String firstName, java.lang.String lastName){<br>
    return delete("where first_name=? and last_name=?", firstName, lastName);<br>
}<br>
</code></pre>

If you want to use complex conditions:<br>
<pre><code>@Delete(sql="where #{age} between ${fromAge} and ${toAge} or #{lastName}=${lastN}")<br>
public abstract int delete(int fromAge, int toAge, String lastN);<br>
</code></pre>

the generated implementation will be:<br>
<pre><code>@Override<br>
public int delete(int fromAge, int toAge, java.lang.String lastN){<br>
    return delete("where age between ? and ? or last_name=?", fromAge, toAge, lastN);<br>
}<br>
</code></pre>

<hr />
<h2>@Update Annotation</h2>

<pre><code>@Update<br>
public abstract int update(int age, String where_firstName, String where_lastName);<br>
</code></pre>

The parameter names should match the column-proprty name in your POJO,<br>
and their type should match with the type of that column-property in POJO.<br>
the parameters to be used in <code>WHERE</code> condition should be prefixed with <code>where_</code>.<br>
<br>
The return value will be the number of employees updated;<br>
The method can return void, if you are not interested in that number.<br>
<br>
the generated implementation will be:<br>
<pre><code>@Override<br>
public int update(int age, java.lang.String where_firstName, java.lang.String where_lastName){<br>
    return update("set age=? where first_name=? and last_name=?", id, age, where_firstName, where_lastName);<br>
}<br>
</code></pre>

If you want to use complex conditions:<br>
<pre><code>@Update(sql="set #{lastName}=${lastN} where #{age} between ${fromAge} and ${toAge}")<br>
public abstract int updateLastName(int fromAge, int toAge, String lastN);<br>
</code></pre>

the generated implementation will be:<br>
<pre><code>@Override<br>
public int updateLastName(int fromAge, int toAge, java.lang.String lastN){<br>
    return update("set last_name=? where age between ? and ?", lastN, fromAge, toAge);<br>
}<br>
</code></pre>

<hr />
<h2>@Upsert Annotation</h2>

This is similar to <code>@Update</code> except that the method should return <code>void</code>.<br>
<br>
If the record is found in database it is updated, otherwise new record is inserted.<br>
<br>
<hr />
<h2>WHERE Condition</h2>

various sql operators can be specified by using prefixes for method parameters.<br>

for example to find all employees whose age is less than specified age:<br>
<pre><code>@Select<br>
public abstract List&lt;Employee&gt; findYoungEmployees(int lt_age);<br>
</code></pre>
here the prefix is <code>lt_</code> and column property is <code>age</code>.<br>
the prefix <code>lt_</code> says to use the <code>&lt;</code> sql operator on <code>age</code> column<br>
<br>
to delete all employees between specified ages:<br>
<pre><code>@Delete<br>
public abstract int deleteEmployeesByAge(int from_age, int to_age);<br>
</code></pre>

to update experience of employees between specified ages:<br>
<pre><code>@Update<br>
public abstract int updateEmployeesByAge(int from_age, int to_age, int experience);<br>
</code></pre>

The supported operators and the prefixes to use are:<br>
<table><thead><th> <b>Prefix</b>     </th><th> <b>Operator</b>        </th></thead><tbody>
<tr><td> <code>where_</code>     </td><td> <code>= ?</code>             </td></tr>
<tr><td> <code>eq_</code>        </td><td> <code>= ?</code>             </td></tr>
<tr><td> <code>ne_</code>        </td><td> <code>&lt;&gt; ?</code>            </td></tr>
<tr><td> <code>lt_</code>        </td><td> <code>&lt; ?</code>             </td></tr>
<tr><td> <code>le_</code>        </td><td> <code>&lt;= ?</code>            </td></tr>
<tr><td> <code>gt_</code>        </td><td> <code>&gt; ?</code>             </td></tr>
<tr><td> <code>ge_</code>        </td><td> <code>&gt;= ?</code>            </td></tr>
<tr><td> <code>like_</code>      </td><td> <code>LIKE ?</code>          </td></tr>
<tr><td> <code>nlike_</code>     </td><td> <code>NOT LIKE ?</code>      </td></tr>
<tr><td> <code>from_, to_</code> </td><td> <code>BETWEEN ? AND ?</code> </td></tr></tbody></table>

This technique of using prefixes, will avoid writing custom queries to large extent.<br>
<br>
<hr />
<h2>Selecting Single Column</h2>

Some times you might want value of a specific column rather than values of all columns.<br>

to find first name of employee of given id:<br>
<pre><code>@Select(column="firstName")<br>
public abstract String getFirstName(int id);<br>
</code></pre>
Notice that the value of <code>column</code> attribute is <code>firstName</code> (not <code>first_name</code>).<br>
i.e value of <code>column</code> attribute is name of the column property to be selected.<br>
<br>
if there is no employee with the specified <code>id</code>, the above method returns <code>null</code>.<br>
if the first name of employee with specified <code>id</code> is <code>NULL</code> in database, the above method returns <code>null</code><br>

in order to differentiate above to cases:<br>
<pre><code>@Select(column="firstName", assertMinimumCount=1)<br>
public abstract String getFirstName(int id);<br>
</code></pre>

the attribute <code>assertMinimumCount</code> ensures that there are atleast specified number of records.<br>
Now the above method throws <code>jlibs.jdbc.IncorrectResultSizeException</code>, if there is no employee with given <code>id</code>.<br>
<code>jlibs.jdbc.IncorrectResultSizeException</code> is subclass of <code>jlibs.jdbc.DAOException</code>

the <code>assertMinimumCount</code> can also be used when selecting entire records:<br>
<pre><code>@Select(assertMinimumCount=5)<br>
public abstract List&lt;Employee&gt; findByAge(int from_age, int to_age);<br>
</code></pre>

if you want to find first names of employees:<br>
<pre><code>@Select(column="firstName")<br>
public abstract List&lt;String&gt; findFirstNames(int from_age, int to_age);<br>
</code></pre>

<hr />
<h2>Selecting Expression</h2>

to find number of employees with age greater than specified<br>
<pre><code>@Select(expression="count(*)")<br>
public abstract int countOlderEmployees(int gt_age);<br>
</code></pre>

to find sum of salaries of all employees:<br>
<pre><code>@Select(expression="sum(#{salary})")<br>
public abstract int totalSalary();<br>
</code></pre>

the method can also return <code>List</code>:<br>
<pre><code>@Select(expression="#{age}-#{experience}")<br>
public abstract List&lt;Integer&gt; calculateEquation1();<br>
</code></pre>

<hr />
<h2>Dynamic SQL</h2>

<pre><code>@Select(ignoreNullConditions=true)<br>
public abstract List&lt;Employee&gt; search(String firstName, String lastName, Integer age, Integer experience);<br>
</code></pre>

<code>ignoreNullConditions=true</code> says that don't include the conditions whose values are null.<br>
i.e<br>
<pre><code>EMPLOYEES.search(null, null, 28, null); // search by age<br>
EMPLOYEES.search(null, null, 35, 5); // search by age and experience<br>
</code></pre>

the generated implementation will be as below:<br>
<pre><code>@Override<br>
public List&lt;Employee&gt; search(String firstName, String lastName, Integer age, Integer experience){<br>
    java.util.List&lt;String&gt; __conditions = new java.util.ArrayList&lt;String&gt;(4);<br>
    java.util.List&lt;Object&gt; __params = new java.util.ArrayList&lt;Object&gt;(4);<br>
    if(firstName!=null){<br>
        __conditions.add("first_name=?");<br>
        __params.add(firstName);<br>
    }<br>
    if(lastName!=null){<br>
        __conditions.add("last_name=?");<br>
        __params.add(lastName);<br>
    }<br>
    if(age!=null){<br>
        __conditions.add("age=?");<br>
        __params.add(age);<br>
    }<br>
    if(experience!=null){<br>
        __conditions.add("experience=?");<br>
        __params.add(experience);<br>
    }<br>
    String __query = null;<br>
    if(__conditions.size()&gt;0)<br>
        __query = " WHERE " + jlibs.core.lang.StringUtil.join(__conditions.iterator(), " AND ");<br>
    return all(__query, __params.toArray());<br>
}<br>
</code></pre>

<code>ignoreNullConditions</code> attribute is supported on <code>@Select</code>, <code>@Delete</code> and <code>@Update</code>

<hr />
<h2>Custom Java Types</h2>

let us say <code>employees</code> table in database has column named <code>grade</code> of type number.<br>
You don't want to use <code>int</code> for <code>grade</code> property in <code>Employee</code> class, rather you want<br>
to use following <code>enum</code>.<br>
<br>
<pre><code>public enum Grade{<br>
    JUNIOR, SENIOR, LEAD, MANAGER<br>
}<br>
</code></pre>

because <code>Grade</code> is not one of the java types supported by JDBC,<br>
you need to create a mapping as below:<br>
<pre><code>import jlibs.jdbc.JDBCTypeMapper;<br>
<br>
public class GradeTypeMapper implements JDBCTypeMapper&lt;Grade, Integer&gt;{<br>
    @Override<br>
    public Grade nativeToUser(Integer nativeValue){<br>
        if(nativeValue==null)<br>
            return null;<br>
        else<br>
            return Grade.values()[nativeValue];<br>
    }<br>
<br>
    @Override<br>
    public Integer userToNative(Grade userValue){<br>
        if(userValue==null)<br>
            return null;<br>
        else<br>
            return userValue.ordinal();<br>
    }<br>
}<br>
</code></pre>
Here we are mapping <code>Grade</code> with <code>Integer</code>.<br>
<code>Grade</code> is called User Type.<br>
<code>Integer</code> is called Native Type.<br>

Now in <code>Employee</code> class you need to specify <code>grade</code> property to use above mapping:<br>
<pre><code>@Table<br>
public class Employee{<br>
    ...<br>
    <br>
    @Column<br>
    @TypeMapper(mapper=GradeTypeMapper.class, mapsTo=Integer.class)<br>
    public Grade grade;<br>
}<br>
</code></pre>

if you are using custom queries, you must specify column-property for parameter which is non-native type:<br>
<pre><code>@Select(expression="count(*)", sql="WHERE #{grade}=${(grade)grade} AND #{age}&lt;${age}")<br>
public abstract int countByGradeAndAge(Grade grade, int age);<br>
</code></pre>
Notice that we used <code>${(grade)grade}</code> in query rather than "${grade}`.<br>
otherwise, you will get following compilation error:<br>
<pre><code>the column property must be specified for parameter grade in query.<br>
</code></pre>

<hr />
<h2>Sorting by Column</h2>

to sort employees by experience:<br>
<pre><code>@Select(orderBy=@OrderBy(column="experience", order=Order.DESCENDING))<br>
public abstract List&lt;Employee&gt; youngEmployees(int le_age);<br>
</code></pre>
the <code>order</code> attribute is optional and defaults to <code>Order.ASCENDING</code>.<br>

to sort by multiple columns:<br>
<pre><code>@Select(orderBy={<br>
    @OrderBy(column="experience", order=Order.DESCENDING),<br>
    @OrderBy(column="age", order=Order.DESCENDING)<br>
})<br>
public abstract List&lt;Employee&gt; youngEmployees(int le_age);<br>
</code></pre>

<hr />
<h2>Paging</h2>

Suppose you are searching employees in databse, the number of employees maching your criteria might be large.<br>
You don't want list all employees, but you want to page though the search results:<br>
<pre><code>@Select(ignoreNullConditions=true, pageBy=@OrderBy(column="id", order=Order.DESCENDING))<br>
public abstract Paging&lt;Employee&gt; pageById(String firstName, String lastName, Integer age, int experience);<br>
</code></pre>

<code>pageBy</code> attribute specifies that you want to page through results.<br>
when <code>pageBy</code> attribute specified the method return type should be <code>jlibs.jdbc.Paging</code>.<br>

In order to page through the results, you need to sort the results by set of columns whose combination of<br>
values is unique. In above method, we are paging by column property <code>id</code> and <code>id</code> is unique for each employee.<br>
<br>
Now you can do paging as follows:<br>
<pre><code>Paging&lt;Employee&gt; paging = EMPLOYEES.pageById(null, null, 35, 5);<br>
Page&lt;Employee&gt; page = paging.createPage(10); // page size is 10<br>
<br>
int totalRowCount = page.getTotalRowCount();<br>
int totalPageCount = getTotalPageCount();<br>
</code></pre>

to Navigate the page:<br>
<pre><code>List&lt;Employee&gt; firstPage = page.navigate(Page.Action.FIRST);<br>
List&lt;Employee&gt; secondPage = page.navigate(Page.Action.NEXT);<br>
List&lt;Employee&gt; lastPage = page.navigate(Page.Action.LAST);<br>
List&lt;Employee&gt; lastButOnePage = page.navigate(Page.Action.PREVIOUS);<br>
</code></pre>
<code>Page.Action</code> is an enum.<br>
to find whether you can navigation in given direction:<br>
<pre><code>boolean enableNextButton = page.canNavigate(Page.Action.NEXT);<br>
</code></pre>
Note that, if the total number of rows is zero, you can't navigate in any direction.<br>
<code>page.navigate(...)</code> throws <code>IllegalArgumentException</code> if the specified navigation action is not possible.<br>
<br>
<code>page.getIndex()</code> returns the index of current page. The indexing starts from zero.<br>
<code>page.getIndex()</code> returns <code>-1</code> if the page is created but not navigated yet.<br>
<br>
Note that you can't jump to a given page.<br>
<br>
<hr />
<h2>Paging in Servlet/JSP</h2>

You can place the page object in session and use it. But if user is paging simultaniously<br>
by different crieterias, you will end up multiple page objects in session and becomes impossible<br>
to know which page object from session to use.<br>
<br>
In order to overcome this situation, you can pass the information required to create current page<br>
from request to request using POST.<br>
<br>
the information required to construct page at given index are:<br>
<pre><code>page.getIndex();<br>
page.getTotalRowCount();<br>
page.getFirstRow(); // returns Employee<br>
page.getLastRow(); // returns Employee<br>
</code></pre>
all above methods have their corresponding set methods.<br>
For first and last rows, it is enough to fill only properties of the <code>Employee</code> object which are used for paging.<br>
i.e <code>Employee.id</code> for above example.<br>
<br>
<hr />
<h2>Transactions</h2>

To run multiple statements in a transaction:<br>
<br>
<pre><code>import static sample.DB.*;<br>
<br>
Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction&lt;Object&gt;(){<br>
    @Override<br>
    public Object run(Connection con) throws SQLException{<br>
        // invoke some DAO/DB actions<br>
        // return something<br>
    }<br>
});<br>
</code></pre>

If <code>Transaction.run(...)</code> throws exception, then the current transaction is rolled back.<br>
<br>
What happens when transactions are nested as below:<br>
<br>
<pre><code>    public void method1(){<br>
        Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction&lt;Object&gt;(){<br>
            @Override<br>
            public Object run(Connection con) throws SQLException{<br>
                // invoke some DAO/DB actions<br>
                method2();<br>
                // return something<br>
            }<br>
        });<br>
    }<br>
<br>
    public void method2(){<br>
        Object returnValue = TransactionManager.run(DATA_SOURCE, new Transaction&lt;Object&gt;(){<br>
            @Override<br>
            public Object run(Connection con) throws SQLException{<br>
                // invoke some DAO/DB actions<br>
                // return something<br>
            }<br>
        });<br>
    }<br>
</code></pre>

here both both <code>method1()</code> and <code>method1()</code> use transactions. Notice that <code>method1()</code> is calling <code>method2()</code>.<br>
Here <code>method2()</code> will run in the same transaction that is started by <code>method1()</code>.<br>
<br>
<hr />
<h2>What Next</h2>

This is not finished yet. I am still developing new features in this.<br>
If you have any suggestions or comments, they will be great help to me.<br>
<br>
just now found <a href='https://eodsql.dev.java.net/'>eodsql</a> project, which uses somewhat similar approach.<br>
some advantages over eodsql are:<br>
<ul><li>no reflection is used at runtime<br>
</li><li>you need to write sql only in rare cases<br>
</li><li>column names are not hardcode in queries<br>
</li><li>strict compile-time checks<br>
</li><li>supports custom java types<br>
</li><li>supports dynamic queries<br>
</li><li>paging support