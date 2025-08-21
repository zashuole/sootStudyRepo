package com.squirtle.callgraph;

class Animal {
    public void speak() {
        System.out.println("Animal speaks");
    }
}

class Dog extends Animal {
    @Override
    public void speak() {
        System.out.println("Dog barks");
    }
}

class Cat extends Animal {
    @Override
    public void speak() {
        System.out.println("Cat meows");
    }
}

public class CallgraphDemo {
    public static void main(String[] args) {
        Animal a1 = new Dog();
        a1.speak();
    }
}