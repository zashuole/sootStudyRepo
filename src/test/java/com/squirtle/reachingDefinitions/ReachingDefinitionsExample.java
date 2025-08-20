package com.squirtle.reachingDefinitions;

public class ReachingDefinitionsExample {

    public void exampleMethod() {
        int a = 1;           // d1
        int b = 2;           // d2
        int c = 0;           // d3

        a = a + b;           // d4

        if (a > 2) {
            b = a - 1;       // d5
        } else {
            c = b + 1;       // d6
        }

        int d = a + b + c;   // d7
        b = d - a;           // d8
    }

}
