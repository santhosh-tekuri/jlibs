## NOTE: following API is work in progress. You can try it by source checkout ##

`Count` class is used for counting by Units.

Let us see how to use it:
```
import jlibs.core.lang;

// we want to count the time
Count<DurationUnit> duration = Count.newInstance(DurationUnit.class);

// additions
duration.add(500, DurationUnit.SECONDS);
System.out.println(duration); // prints "8 MINUTES 20 SECONDS"
duration.add(100, DurationUnit.MINUTES);
System.out.println(duration); // prints "1 HOURS 48 MINUTES 20 SECONDS"

// get specific unit value
System.out.println(duration.get(DurationUnit.MINUTES); // prints "48"
System.out.println(duration.get(DurationUnit.SECONDS); // prints "20"

// conversions
System.out.println(duration.to(DurationUnit.SECONDS)); // prints "6500.0"
System.out.println(duration.to(DurationUnit.MINUTES)); // prints "108.33333333333333"

// reset to given value
duration.set(500, DurationUnit.SECONDS);
System.out.println(duration); // prints "8 MINUTES 20 SECONDS"

// reset to 0
duration.clear();
System.out.println(duration); // prints "0 NANO_SECONDS"

// adding two counts
Count<DurationUnit> duration1 = Count.newInstance(DurationUnit.class);
duration1.add(500, DurationUnit.SECONDS);
Count<DurationUnit> duration2 = Count.newInstance(DurationUnit.class);
duration2.add(100, DurationUnit.MINUTES);
duration1.add(duration2);
System.out.println(duration1); // prints "1 HOURS 48 MINUTES 20 SECONDS"

// you can count by any unit, for example
Count<SizeUnit> size = Count.newInstance(SizeUnit.class);
size.add(5000, SizeUnit.BYTES);
System.out.println(size); // prints "4 KB 904 BYTES"
```

You can count by any unit, by providing its definition.<br>
<code>JLibs</code> comes with two units <code>DurationUnit</code> and <code>SizeUnit</code>.<br>

Let us see how to define <code>LengthUnit</code>:<br>
<pre><code>public enum LengthUnit implements Count.Unit{<br>
    MILLI_METERS(10), CENTI_METERS(100), METERS(1000), KILO_METERS(0);<br>
<br>
    private int count;<br>
<br>
    private DurationUnit(int count){<br>
        this.count = count;<br>
    }<br>
<br>
    @Override<br>
    public int count(){<br>
        return count;<br>
    }<br>
}<br>
</code></pre>

we define an <code>enum</code> implementing <code>Count.Unit</code> interface.<br>
The <code>enum</code> constants are ordered from lowest to highest.<br>
for each <code>enum</code> provide the count to promote to next enum.<br>
for example: <code>MILLI_METERS(10)</code> means, 10 millimeters should be promoted to 1 centimeter.<br>
<br>
now we are ready to count length:<br>
<pre><code>Count&lt;LengthUnit&gt; size = Count.newInstance(LengthUnit.class);<br>
</code></pre>