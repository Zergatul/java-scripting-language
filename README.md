## Features

Supports 5 types:
- boolean
- int (corresponds to int32)
- float (corresponds to double)
- string (corresponds to String)
- arrays
- Action (corresponds to Runnable)

### boolean

Supports usual java operators.

### int

Supports most usual java operators, expect bitwise shifting.

Supports floor div operator: `!/`.

Supports floor mod operator: `!%`.

### float

Supports most usual java operators.

### string

Supports `+`, `==`, `!=` operators. You can get length with `str.length` expression.

### array

Access individual items in the same way as in Java, get length with `arr.length` expression.

Example:
```
int[] data = new int[5];
data[0] = 1;
// data[0] == 1
// data.length == 5 here
```

### Action

You can pass Action into methods that accepts Runnable like this:
```
events.onSomething(() => {
    // code
});
```

Closures are not supported.

### Implicit type casting

If you do mathematical operator between **int** and **float**, **int** is converted to ***float***. Same happens when you pass **int** to method that can accept **float**.

**int** can be converted to **string** when initializing **string** variable, or when passing as argument into method.

### Local variables

If you don't initialize local variable it implicitly receives default value: `0` for **int**/**float**, `false` for **boolean**, empty string for **string**, empty array for **array**.

Example:
```
int x;
x = x + 1;
// x == 1 here
```

### Overloaded methods call

You can have overloaded methods (methods with the same name but with different arguments), and compiler looks for best match with minimum implicit type casting required.

### For loops

Example:
```
int[] a = new int[10];
for (int i = 0; i < a.length; i++) {
    a[i] = i + 1;
}
```

### For Each loop

Example:
```
int[] a = new int[10];
// ...

int sum = 0;
foreach (int x in a) {
    sum = sum + x;
}
```

### Static variables

Static variables are initialized once when you compile the script, and don't lose value when you run compiled script multiple times. Static variables should be declared in the beginning of the script.

Example:
```
static int ticks = 0;

events.onTick(() => {
    ticks++;
});

events.onSomething(() => {
    console.log(ticks);
})
```

### Functions

Functions must be declared after static variables, and before actual script code. Order of functions don't matter, you can do recursion, or call any other function. If function returns a value, last statement must be return statement.

Example:
```
static int a = 100;

// void function
function func1() {
    do.something();
}

// return int function
function int func2(int b, boolean c) {
    a++;
    return c ? a + b : a - b;
}

// script code begins here
for (int i = 0; i < 3; i++) {
    func1();
}
```

## How to use

Create class with static fields to group methods allowed to use by users. Example:
```java
public class ApiRoot {
    public static Methods methods = new Methods();
    public static Events events = new Events();
}

public static class Methods {
    public void doSomething(int value) {
        // ...
    }
}

public static class Events {
    public void onTick(Runnable action) {
        action.run();
    }
}
```

Now you can create compiler:
```java
ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
```

And compile code:
```java
String code = "events.onTick(() => { methods.doSomething(1); methods.doSomething(2); });"
Runnable program = compiler.compile(code);
```
