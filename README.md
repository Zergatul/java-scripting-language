To see full list of supported feature you can check test cases here:
`src/test/java/com/zergatul/scripting/tests/compiler`

# Short list of supported features

### Basic Types
- `boolean`
- `char`
- `int` (synonym: `int32`)
- `long` (synonym: `int64`)
- `float32` (correspongs to `float` in Java, only use it for Java interop)
- `float` (correspongs to `double` in Java, synonym: `float64`)
- `string`

User-defined types are not supported

### Variables Declaration
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
int[][] a = new int[][10]; // array of arrays

// array concatenation
int[] a1 = [1, 2, 3];
int[] a2 = [7, 8, 9];
int[] a3 = a1 + a2 + 10; // a3=[1,2,3,7,8,9,10]
```

### if/else statements
```c#
if (api.getCount() > 10) {
    debug.write("OK");
} else {
    debug.write("NOT OK");
}
```

### Loops
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
    debug.write(i.toString());
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
Functions should be defined in the beginning of the script, before all script statements:
```c#
int factorial(int value) {
    return value * factorial(value - 1);
}

debug.write(factorial(10).toString());
```

### Static variables
Static variables should be defined in the beginning of the script, along with functions.
These variables preserve values across multiple script invocations.
```c#
static int x1;

int x2;
x1++;
x2++;
debug.write(x1.toString()); // increases each time
debug.write(x2.toString()); // always logs 1
```

### async/await
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
// 2 loop's will run at the same time
```

### Lambda Functions
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

And thus lambda functions can exists only as parameters to functions/methods.

### Function as Values
```c#
boolean filterSword(ItemStack stack) {
    return stack.item.id == "minecraft:wooden_sword";
}

inventory.findAndMoveToHotbar(1, filterSword);
```

### Type Check/Cast
```c#
let x = api.getSomething();
if (x is ItemStack) {
    let stack = x as ItemStack;
    debug.write(stack.item.name);
}
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
```