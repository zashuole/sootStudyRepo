package com.squirtle.intraprocedural;

public class ConstantDemo {

    void assign() {
        int x = 1, y;
        x = 2;
        x = 3;
        x = 4;
        y = x;
    }

    void constant1(boolean b) {
        int x = 2;
        int y = 3;
        int z;
        if (b) {
            z = x + y;
        } else {
            z = x * y;
        }
        int n = z;
    }

    void constant2(boolean b) {
        int x = 20;
        if (b) {
            x = 10;
        }
        int y = x;
    }
}
