package com.squirtle.veryBusyExprExample;

public class VeryBusyExample {

    public int compute(int a, int b) {
        int x = a + b;       // expr1: a + b
        int y = a - b;       // expr2: a - b

        if (x > 10) {        // expr3: x > 10
            x = x + 1;       // expr4: x + 1
        } else {
            y = y + 2;       // expr5: y + 2
        }

        int z = x + y;       // expr6: x + y
        return z;            // 使用 expr6
    }
}
