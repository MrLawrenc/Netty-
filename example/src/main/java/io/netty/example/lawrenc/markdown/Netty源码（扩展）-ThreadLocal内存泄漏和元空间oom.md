                                                                                                                                                                                                                                                                          # ThreadLocal内存泄漏与ClassLoader内存泄漏

## ThreadLocal内存泄漏的本质

ThreadLocal本身不存储数据，由ThreadLocalMap存储，而ThreadLocalMap是绑定在线程变量之中的，因此发生内存泄漏就有如下两种情况（线程长时间存在，或者一直存在）：

1. ThreadLocalMap存储的数据越来越多，废弃的数据得不到释放回收
2. ThreadLocal持有对Class的引用，当该class本应当卸载但是未卸载的情况，则该

