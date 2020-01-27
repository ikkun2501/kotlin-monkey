Go言語で作るインタプリタのKotlin実装

# example
```
let fibonacci = fn(x) {
  if (x == 0) {
    return 0;
  } else if(x == 1) {
    return 1;
  } else {
    return fibonacci(x - 1) + fibonacci(x - 2);
  }
};
fibonacci(10);
```

```
let newAdder = fn(a, b) {
    fn(c) { a + b + c };
};
let adder = newAdder(1, 2);

adder(8); 
```