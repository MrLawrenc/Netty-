package io.netty.example.lawrenc.netty.memoryleak;

public class Foo {

    public static final ThreadLocal<Foo> local = new ThreadLocal<>();

    public Foo() {
        System.out.println(Thread.currentThread().getName());
        //null-->BootStrap ClassLoader(c编写)
        System.out.println(local.getClass().getClassLoader());
        local.set(this);
        System.out.println("ClassLoader: " + this.getClass().getClassLoader());
    }
}