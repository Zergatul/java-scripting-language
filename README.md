To see full list of supported features you can check test cases here:
`src/test/java/com/zergatul/scripting/tests/compiler`

# General Notes
Although this scripting language was designed for Minecraft mod, it doesn't have any dependencies on Minecraft.
The only dependency it has is ASM library (for emitting Java bytecode).
The language itself can be used for something else.

This scripting language allows mod users to create custom scripts, bind them to keys, automate tasks, react to in-game events by using C#/Java-like syntax.
It is lightweight, async-friendly, and designed to interop with Java APIs where needed.

# Short list of supported features
- [Hello World](#hello-world)
- [Basic Types](#basic-types)
- [Variables](#variables)
- [Arrays](#arrays)
- [Static Variables](#static-variables)
- [Control Flow](#control-flow)
    - [if/else Statements](#ifelse-statements)
    - [Conditional Expressions](#conditional-expressions)
    - [Loops](#loops)
- [Parsing Strings](#parsing-strings)
- [Functions](#functions)
    - [Simple Functions](#simple-functions)
    - [Lambda Functions](#lambda-functions)
    - [Function as Values](#function-as-values)
    - [Functional Types](#functional-types)
    - [async/await](#asyncawait)
- [Type Check/Cast](#type-checkcast)
- [null](#null)
- [Reflection](#reflection)
- [Classes](#classes)
- [Java Interop](#java-interop)
- [Limitations](#limitations)
- [Comparison Table](#comparison-table)

### Hello World
```c#
debug.write("Hello World!");
```

### Basic Types
- `boolean`
- `char`
- `int8` (only use it for Java interop)
- `int16` (only use it for Java interop)
- `int` (synonym: `int32`)
- `long` (synonym: `int64`)
- `float32` (corresponds to `float` in Java, only use it for Java interop)
- `float` (corresponds to `double` in Java, synonym: `float64`)
- `string`

Unsigned integer types are not supported.
Language is not well adapted for using `int8`/`int16`/`float32`. If you can you should better use `int` and `float` instead.

### Variables
```c#
int x;        // will have value 0 assigned implicitly
float f = 11.25;
let s = "qq"; // variable "s" will have "string" type
```

### Arrays
```c#
int[] array1; // will have zero length array assigned implicitly
int[] array2 = new int[5];
let array3 = new int[] { 10, 20, 30, 40, 50 };
let array4 = [1, 2, 3]; // empty array syntax "[]" is not supported
int[][] a = new int[][10]; // array of 10 arrays, by default with null values

// array concatenation
int[] a1 = [1, 2, 3];
int[] a2 = [7, 8, 9];
int[] a3 = a1 + a2 + 10; // a3=[1,2,3,7,8,9,10]
```

Array length is **static**, it cannot be resized. "+" operation always creates new array.

### Static variables
Static variables should be defined in the beginning of the script, along with functions/classes.
These variables preserve values across multiple script invocations (meaning when mod engine executes script in response to some event).
Imagine below script bound to some key:
```c#
static int x1;

int x2;
x1++;
x2++;
debug.write(x1.toString()); // increases each time you press a key
debug.write(x2.toString()); // always logs 1
```

### Control Flow
#### if/else Statements
```c#
if (api.getCount() > 10) {
    debug.write("OK");
} else {
    debug.write("NOT OK");
}
```

#### Conditional Expressions
```c#
int x = 123;
int y = x > 100 ? x - 100 : x + 100;
```

#### Loops
```c#
let array = [1, 2, 3];
for (let i = 0; i < array.length; i++) {
    debug.write(i.toString());
}
```

`foreach` loop works only with arrays.
```c#
let array = [1, 2, 3];
foreach (let x in array) {
    debug.write(x.toString());
}
```

`continue`, `break` statements are supported. `do/while` loops are not supported.

### Parsing Strings
```c#
int x;
if (int.tryParse("123", ref x)) {
    // success
    // x is 123 here
} else {
    // fail
}
```

### Functions
#### Simple Functions
Functions should be defined in the beginning of the script, before all script statements:
```c#
int factorial(int value) {
    if (value <= 1) {
        return 1;
    } else {
        return value * factorial(value - 1);
    }
}

debug.write(factorial(10).toString());
```

You can also use arrow syntax if function is short:
```c#
int sum(int a, int b) => a + b;
```

Function overloading is not supported:
```c#
void func() {}
void func(int x) {} // will not compile
```

#### Lambda Functions
Subscribing to event:
```c#
events.onTickEnd(() => {
    debug.write("Tick: #" + game.getTick());
});
```

Using lambda for filtering:
```c#
inventory.findAndMoveToHotbar(1, (stack) => {
    return stack.item.id == "minecraft:wooden_sword";
});
// or
inventory.findAndMoveToHotbar(1, stack => stack.item.id == "minecraft:wooden_sword");
```

Lambda functions with explicit types are not supported:
```c#
// not supported
inventory.findAndMoveToHotbar(1, (ItemStack stack) => stack.item.id == "minecraft:wooden_sword");
```

#### Function as Values
```c#
boolean filterSword(ItemStack stack) {
    return stack.item.id == "minecraft:wooden_sword";
}

inventory.findAndMoveToHotbar(1, filterSword);
```

Class methods can be used as functions:
```c#
class MyClass {
    void method(int x) {
        debug.write((x * x).toString());
    }
}

void test(fn<int => void> func) {
    func(10);
}

let my = new MyClass();
test(my.method); // prints "100", captures "my" variable into closure
```

#### Functional Types
You can describe functional types like this: `fn<() => void>` / `fn<int => string>` / `fn<(int, int, int) => fn<int => int>>`

You can use them as function parameters:
```c#
void run(fn<() => void> func, int times) {
    for (let i = 0; i < times; i++) {
        func();
    }
}

run(() => debug.write("a"), 3); // writes "a" to debug 3 times
```

As local variables:
```c#
int x = 3;
fn<int => int> add3 = a => a + x;

debug.write(add3(5).toString()); // writes 8
```

Functions can be cast to functional type:
```c#
void write(string value) {
    debug.write(value);
}

fn<string => void> f = write;
f();
```

#### async/await
In async context you can use await statements:
```c#
for (let i = 0; i < 10; i++) {
    await delay.ticks(1);
    ui.systemMessage("Iteration " + i);
}
```

You can declare your own async functions:
```c#
async void loop() {
    for (let i = 0; i < 5; i++) {
        ui.systemMessage(game.getTick().toString());
        await delay.ticks(10);
    }
}

if (api.getSomething()) {
    await loop();
} else {
    ui.systemMessage("no");
}
```

You can call async function without await. In this case function will run in "background":
```c#
async void loop() {
    for (let i = 0; i < 5; i++) {
        ui.systemMessage(game.getTick().toString());
        await delay.ticks(10);
    }
}

loop();
loop();
// 2 loops will run at the same time
```

Async functions can also return any type:
```c#
async int waitForChestAndCountItems(string itemId) {
    while (containers.getMenuClass() != "net.minecraft.world.inventory.ChestMenu") {
        await delay.ticks(1);
    }

    int count = 0;
    int slots = containers.getSlotsSize();
    for (let i = 0; i < slots; i++) {
        let stack = containers.getItemAtSlot(i);
        if (stack.item.id == itemId) {
            count += stack.count;
        }
    }
    return count;
}
```

### Type Check/Cast
```c#
let x = api.getSomething();
if (x is ItemStack) {
    let stack = x as ItemStack;
    debug.write(stack.item.name);
}
```

This works for basic types like `int`, `string`, for defined classes, and for Java interop types, like `Java<java.lang.Object>`.

### null
There is no `null` keyword in the language. If you have to check if some value is null, you can use `is` expression:
```c#
let possiblyNull = getSomething();
if (possiblyNull is ExpectedType) {
    // possiblyNull != null
} else {
    // possiblyNull == null
}
```

Normally APIs to be used from scripting language should not return or expect `null`. There is no simple way to assign/pass `null` value.

### Reflection
```c#
let x = "123";
let type = #typeof(x);  // type is instance of Type
debug.write(type.name); // logs "string"
if (type == #type(string)) {
    // compare 2 types
}
```

### Classes
Classes should be defined in the beginning of the script, before all script statements.
Class can have fields, constructors, methods. Class without constructors receive implicit parameterless constructor:
```c#
class MyClass {
    int x;
    int y;
}

let c = new MyClass();
c.x = 1;
c.y = 2;
debug.write((c.x + c.y).toString());
```

You can only access class members by using `this` keyword. This was done to simplify compiler code:
```c#
class MyClass {
    int x;

    constructor(int value) {
        this.x = value;
        // "x = value" will not work
    }

    int getX() {
        return this.x;
    }
}
```

Async methods supported.

Limitations:
- access modifiers (private/public/etc) are not supported
- static members are not supported
- inheritance is not supported
- generics not supported

### Java Interop
Generic type syntax is not supported.
```c#
let table = new Java<java.util.Hashtable>();
table.put(false, 100);
table.put(200, true);
table.put("qq", "ww");

debug.write(table.get("qq").toString());  // ww
debug.write(table.get(200).toString());   // true
debug.write(table.get(false).toString()); // 100

// if you need to cast Object to specific type
let obj = table.get("qq"); // type: Java<java.lang.Object>
let str = obj as string;   // cast to string
debug.write(str);
```

### Limitations
- `try/catch` not supported
- operator overloading not supported
- Java interop with parameterized types (generics) is not supported

### Comparison Table
| C# | Scripting Language |
|---|---|
| `var` | `let` |
| `(int)x` | `x as int` |
| `x as int` | Not supported (only cast syntax exists) |
| `x is ClassA` | `x is ClassA` |