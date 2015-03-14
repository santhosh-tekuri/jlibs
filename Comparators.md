**DefaultComparator:**

`jlibs.core.util.DefaultComparator` is default implementation of `java.util.Comparator`;

Its implementation is null friendly where null is treated less than non-null:
```
import jlibs.core.util.DefaultComparator;

Comparator<String> comp = new DefaultComparator<String>();
int result1 = comp.compareTo(null, "helloworld"); // returns -1
int result2 = comp.compare("helloworld", null); // returns 1
int result3 = comp.compare(null, null); // returns 0
```

The implemenation of `DefaultComparator` works for any objects implementing `java.util.Comparable`:
```
Comparator<Integer> comp = new DefaultComparator<Integer>(); // Integer implements Comparable
int result = comp.compare(10, 7); // returns 3
```

You can extend `DefaultComparable` for any objects that doesn't implement `Comparable`:

```
class Employee{
    int marks;
    int age;

    public Employee(int marks, int age){
        this.marks = marks;
        this.age = age;
    }
}

class EmployeeAgeComparator extends DefaultComparator<Employee>{
    @Override
    protected int _compare(@NotNull Employee emp1, @NotNull Employee emp2){
        return emp1.age - emp2.age;
    }
}
```

the `compare(...)` method in `DefaultComparator` is final. the `compare(...)` takes care of <br>
comparing values involving nulls. If both arguments are non-null, <br>
then it delegates the comparison to <code>_compare(...)</code>;<br>
<br>
So it is guaranteed that, both arguments of <code>_compare(...)</code> are non-null;<br>
<br>
<b>ReverseComparator:</b>

<code>jlibs.core.util.ReverseComparator</code> can be used to sort objects in reverse order;<br>
<pre><code>import jlibs.core.util.ReverseComparator;<br>
<br>
String arr[] = { "c", "java", "c++", "jsp" };<br>
Arrays.sort(arr, new ReverseComparator&lt;String&gt;());<br>
System.out.println(Arrays.asList(arr)); // prints [jsp, java, c++, c]<br>
</code></pre>

to sort employees, in descending order of their ages:<br>
<pre><code>List&lt;Employee&gt; employees = ...<br>
Collections.sort(employees, new ReverseComparator&lt;Employee&gt;(new EmployeeAgeComparator()));<br>
</code></pre>

i.e <code>ReverseComparator</code> has constructor which takes comparator implementation to be used;<br>
The default constructor uses <code>DefaultComparator</code> implicitly;<br>
<br>
<b>ChainedComparator:</b>

Let us say, you want to sort list of employees by their marks. and If there are more than one employee with same marks sort them by their age.<br>
<br>
To do this kind of chained comparison, <code>ChainedComparator</code> will become handy;<br>
<pre><code>import jlibs.core.util.ChainedComparator;<br>
<br>
class EmployeeMarksComparator extends DefaultComparator&lt;Employee&gt;{<br>
    @Override<br>
    protected int _compare(@NotNull Employee emp1, @NotNull Employee emp2){<br>
        return emp1.marks - emp2.marks;<br>
    }<br>
}<br>
<br>
List&lt;Employee&gt; employees = null;<br>
Collections.sort(employees, new ChainedComparator&lt;Employee&gt;(new EmployeeMarksComparator(), new EmployeeAgeComparator()));<br>
</code></pre>

Your comments are welcomed;