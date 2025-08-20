package com.squirtle.callgraph;

import java.io.Serializable;

class F{
    public void foo(){

    }
}

class A extends F  {
    public Object b;

    @Override
    public void foo(){
        b.toString();
    }

}

class B extends F{
    @Override
    public void foo(){

    }
    @Override
    public String toString(){
        System.out.println("B");
        return "";
    }
}


public class CallgraphDemo2 {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        a.b = b;
        a.foo();
    }
}
