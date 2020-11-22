package io.netty.example.lawrenc.netty.memoryleak;

import java.text.SimpleDateFormat;

public class Foo2 {

    private static final ThreadLocal<SimpleDateFormat> local = new ThreadLocal<>();

    public Foo2() {
        local.set(new SimpleDateFormat("YYYY"));
        //System.out.println(local.getClass().getClassLoader());
        System.out.println("ClassLoader: " + this.getClass().getClassLoader());
    }
}