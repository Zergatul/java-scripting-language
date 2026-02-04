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
    - [string](#string)
- [Variables](#variables)
- [Expressions](#expressions)
    - [`in` operator](#in-operator)
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
- [`is` operator and pattern matching](#is-operator-and-pattern-matching)
- [null](#null)
- [Boxing](#boxing)
- [Reflection](#reflection)
- [Classes](#classes)
- [Extensions](#extensions)
- [Java Interop](#java-interop)
    - [Accessing Private Members](#accessing-private-members)
- [Type Aliases](#type-aliases)
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
Language is not well adapted for using `int8`/`int16`/`float32`. If you can, you should better use `int` and `float` instead.

#### string
The built-in `string` type is directly backed by `java.lang.String` on the JVM.
- At runtime, a value of type `string` is a normal Java `java.lang.String` instance.
- The language adds its own conveniences on top of it:
    - Indexer: `s[i]` instead of `s.charAt(i)`
    - Read-only length property: `s.length`
- Because of this, you should treat `string` as the canonical text type in this language.

For low-level interop you can still expose the raw Java type, e.g.:
```c#
typealias JString = Java<java.lang.String>;
```

Both `string` and `Java<java.lang.String>` share the same underlying Java class. All type checks and casts (`is` / `as`) are based on that underlying Java class, so they are compatible:
```c#
Java<java.lang.Object> obj = ...;

if (obj is string) {
    // true if obj actually contains a java.lang.String
}

if (obj is JString) {
    // also true in the same case
}

string s1 = obj as string;
JString s2 = obj as JString;
```

Use `string` for normal scripting and only use `Java<java.lang.String>` (or its alias) when you specifically need to call Java APIs that require an explicit `java.lang.String` type, or you are doing low-level reflection.

### Variables
```c#
int x;        // will have value 0 assigned implicitly
float f = 11.25;
let s = "qq"; // variable "s" will have "string" type
```

### Expressions
Supported binary operators: `+`, `-`, `*`, `/`, `%`, `&&`, `||`, `==`, `!=`, `<`, `>`, `<=`, `>=`, `&`, `|`, `is`, `as`, `in`

Supported unary operators: `+`, `-`, `!`

#### `in` operator
`in` operator is just a syntactic sugar for `contains` method that returns `boolean` and accepts single argument. You can also declare extension `contains` method and `in` operator will work for this:
```c#
extension(int) {
    boolean contains(int value) => value.toString() in this.toString();
}

boolean b1 = 12 in 123; // true, equivalent: "123".contains("12")
boolean b2 = 34 in 123; // false, equivalent: "123".contains("34")
```

### Arrays
```c#
int[] array1; // will have zero length array assigned implicitly
int[] array2 = new int[5];
let array3 = new int[] { 10, 20, 30, 40, 50 };
let array4 = [1, 2, 3];
let array5 = [];   // not allowed, cannot infer type of array5
int[] array6 = []; // allowed
int[][] array7 = new int[][10]; // array of 10 arrays, by default with null values

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

Function overloading is supported:
```c#
int max(int i1, int i2) => i1 > i2 ? i1 : i2;
int max(int i1, int i2, int i3) => max(i1, max(i2, i3));
int max(int i1, int i2, int i3, int i4) => max(max(i1, i2), max(i3, i4));
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
if (x is ItemStack stack) {
    debug.write(stack.item.name);
}
```

This works for basic types like `int`, `string`, for defined classes, and for Java interop types, like `Java<java.lang.Object>`.

`as`-expression features:
- if expression result can't be cast to target type, it evaluates as default value:
  ```c#
  let x = 1 as float; // 1 is int, it can't be cast to float, x is set 0.0
  let y = new Java<java.lang.Object>() as string; // y is set to null
  ```
- transparently handles value types and their boxed variants:
  ```c#
  typealias Object = Java<java.lang.Object>;
  typealias Integer = Java<java.lang.Integer>;

  Object getInt() => 10;

  let a = getInt() as int; // a is 10, unboxed
  let b = 20 as Integer;   // b is 20, boxed
  ```

Use `#cast(<expr>, <type>)` expression for strong check cast. If expression can't be cast to type, `ClassCastException` is thrown.
```c#
typealias Object = Java<java.lang.Object>;
Object getInt() => 10;
let x = #cast(getInt(), int); // x is 10
```

### `is` operator and pattern matching
`is` operator supports basic pattern matching (similar to C#):
```c#
typealias Object = Java<java.lang.Object>;
// ...
Object o = func();

// constant patterns
if (o is null) { /* ... */ }
if (o is not null) { /* ... */ }
if (o is 100) { /* ... */ }
if (o is not 200) { /* ... */ }
if (o is "hello") { /* ... */ }
if (o is not "world") { /* ... */ }

// type patterns
if (o is int) { /* ... */ }
if (o is string) { /* ... */ }

// declaration pattern
if (o is string str) {
    // str is defined here
} else {
    // but not here
}
if (o is int i) {
    // i is not defined here
} else {
    // i can be used here
}
if (o is not float f) {
    return;
}
// f can be used here
```

Multiple declaration patterns are support in single condition:
```c#
Object o1 = func1();
Object o2 = func1();

if (o1 is string str && o2 is int x) {
    // str and x can be used here
}
```

However, pattern variables can't be used in the same expression:
```c#
Object o1 = func1();
// will not work, str is not defined here
if (o1 is string str && str.length > 5) {}
```

### null
It is advised that APIs to be used from scripting language should not return or expect `null`.
This way it should be more beginner-friendly to not get NullReferenceException.
But for Java interop you most likely have to work with `null` a lot.

```c#
string s = null;
if (s == null) {
    debug.write("s is null");
}
```

### Boxing
Wrapper classes (Java terminology) or boxed classes (C# terminology) are described like this: `Boxed<int>` (however you can't use such syntax in the code, you have to use `Java<java.lang.Integer>` instead).
Normally you don't need to use them explicitly, but you may often see them as parameters or return types when working with Java interop. Language supports automatic boxing/unboxing.

For example, API method may return `Future<Boxed<boolean>>`, because `Future` type corresponds to `CompletableFuture` under the hood, and it actually has `java.lang.Boolean` as type parameter. But you can write code like this:
```c#
boolean result = await game.connect("my.server.example.com");
```

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
Class can have fields, constructors, methods, operator overloads.
Class without constructors receives implicit parameterless constructor:
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

For constructor/method bodies you can use square brackets, or arrow if method is short:
```c#
class MyClass {
    int x;

    constructor(int x) {
        this.x = x;
    }
    // or
    // constructor(int x) => this.x = x;

    int getX() {
        return x;
    }
    // or
    // int getX() => x;
    
    void inc() {
        x++;
    }
    // or
    // void inc() => x++;
}
```

Async methods supported.

Inheritance supported:
```c#
class BaseClass {
    virtual int calc() => 12;
}
class ChildClass : BaseClass {
    override int calc() => base.calc() + 3;
}
```
Using `virtual`/`override` is required for polymorphism. Methods/fields shadowing is not allowed.

You can also extend Java classes:
```c#
class MyList : Java<java.util.Vector> {
    void add(int value) => base.add(value);
    int get2(int index) => base.get(index) as int;
}
class FakeList : Java<java.util.Vector> {
    override int size() => 0;
}
```

Constructor initializers:
```c#
class BaseClass {
    constructor(int value) {}
}
class ChildClass : BaseClass {
    constructor(int a, int b) : base(a + b) {}
    constructor(int a, int b, int c) : this(a, b + c) {}
}
```

Operator overloads:
```c#
class Vec2 {
    float x;
    float y;

    constructor(float x, float y) {
        this.x = x;
        this.y = y;
    }

    operator [+] Vec2(Vec2 vec) => vec;

    operator [+] Vec2(Vec2 left, Vec2 right) {
        return new Vec2(left.x + right.x, left.y + right.y);
    }

    operator [-] Vec2(Vec2 vec) => new Vec2(-vec.x, -vec.y, -vec.z);

    operator [-] Vec2(Vec2 left, Vec2 right) {
        return new Vec2(left.x - right.x, left.y - right.y);
    }

    operator [==] boolean(Vec2 left, Vec2 right) => left.x == right.x && left.y == right.y;
    operator [!=] boolean(Vec2 left, Vec2 right) => left.x != right.x || left.y != right.y;
}

let v1 = new Vec2(1, 2);
let v2 = new Vec2(3, 4);
let v3 = v1 + v2;
if (v2 == new Vec2(0, 0)) { /* ... */ }
```

Limitations:
- access modifiers (private/public/etc.) are not supported
- abstract classes not supported
- static members are not supported
- generics not supported

### Extensions
Extensions should be defined in the beginning of the script, before all script statements.

Extension blocks can have methods inside:

```c#
extension(int) {
    int next() => this + 1;
    int more(int x) => this + x;
}

int a = (10).next(); // 11
int b = a.more(5);   // 16
```

```c#
extension(int[]) {
    boolean contains(int value) {
        for (int i = 0; i < this.length; i++) {
            if (this[i] == value) {
                return true;
            }
        }
        return false;
    }
}

boolean b1 = 1 in [1, 2, 3]; // true
boolean b2 = 4 in [1, 2, 3]; // false
```

Extension blocks can have operator overloads inside:

```c#
extension(string) {
    operator [+] int(string str) {
        int value;
        if (int.tryParse(str, ref value)) {
            return value;
        } else {
            return int.MIN_VALUE;
        }
    }
}

string s = "100"
int x = +s;      // 100
```

```c#
extension(string) {
    operator [/] string[](string str, char ch) => str.split(ch);
}

let str = "hello world";
let parts = str / ' ';   // ["hello", "world"]
```

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

#### Accessing Private Members
To write complex scripts you often have to access private members.
Language supports unique syntax to simplify this process:
```ts
typealias Minecraft = Java<net.minecraft.client.Minecraft>;

let mc = Minecraft.#instance; // accessing package-private static field
mc.#rightClickDelay = 0; // modifying private instance field
mc.#handleKeybinds(); // calling private method
```

Any "private" modifier is considered private by this syntax: private, package-private, protected.
This syntax doesn't switch the language into dynamic type mode (like `dynamic` keyword in C#),
every private access/call is validated during compilation.

Auto-completion is supported: `obj.#<cursor>` should pull private-only members.

Accessing private members is only allowed on `Java<...>` types.

Under the hood such calls are highly optimized by using `MethodHandle` objects.
Above example compiles to something like this in Java code:

```java
class MethodHandleCache {
  public static final VarHandle instance_var_handle;
  public static final VarHandle rightClickDelay_var_handle;
  public static final MethodHandle handleKeybinding_method_handle;

  static MethodHandleCache() {
    var caller = MethodHandles.lookup();
    var mcPrivateLookup = MethodHandles.privateLookupIn(Minecraft.class, caller);
    instance_var_handle = mcPrivateLookup.findStaticVarHandle(Minecraft.class, "instance", Minecraft.class);
    rightClickDelay_var_handle = mcPrivateLookup.findVarHandle(Minecraft.class, "rightClickDelay", int.class);
    handleKeybinding_method_handle = mcPrivateLookup.findVirtual(Minecraft.class, "handleKeybinds", MethodType.methodType(void.class));
  }
}

class Script {
    public void run() {
        var mc = (Minecraft) MethodHandleCache.instance_var_handle.get();
        MethodHandleCache.rightClickDelay_var_handle.set(mc, 0);
        MethodHandleCache.handleKeybinding_method_handle.invokeExact(mc);
    }
}
```

### Type Aliases
Type aliases should be defined in the beginning of the script, before all script statements.
Example:
```c#
typealias str = string;
void log(str s) => debug.write(s);

str x1 = "s1";
log(x1);
string x2 = "s2";
log(x2);
```

The most useful case for it is Java interop:
```c#
typealias Minecraft = Java<net.minecraft.client.Minecraft>;
typealias LocalPlayer = Java<net.minecraft.client.player.LocalPlayer>;
typealias ClientLevel = Java<net.minecraft.client.multiplayer.ClientLevel>;

LocalPlayer player = Minecraft.instance.player;
ClientLevel level = Minecraft.instance.level;
```

### Limitations
- `try/catch` not supported
- Java interop with parameterized types (generics) is not supported

### Comparison Table
| C#            | Scripting Language |
|---------------|--------------------|
| `var`         | `let`              |
| `(int)x`      | `#cast(x, int)`    |
| `x as int`    | `x as int`         |
| `x is ClassA` | `x is ClassA`      |