package io.netty.example.lawrenc.netty.juc.clh;

public class CLHLockTest {

    public static void main(String[] args) {
        final Kfc kfc = new Kfc();
        for (int i = 0; i < 10; i++) {
            new Thread("eat" + i) {
                public void run() {
                    kfc.eat();
                }
            }.start();
        }

    }
}

class Kfc {
    private final Lock lock = new CLHLock();
    private int i = 0;

    public void eat() {
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + ": " + --i);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void cook() {
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + ": " + ++i);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}