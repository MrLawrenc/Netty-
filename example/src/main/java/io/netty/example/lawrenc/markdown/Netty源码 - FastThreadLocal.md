Netty1-ThreadLocal和FastThreadLocal源码分析

## 概述

Netty的FastThreadLocal、FastThreadLocalThread和它自身封装的一些并发类、非阻塞队列一起奠定了高并发的基础。

## 总结

博主再次学完之后的收获

- 常用的lock 是一般都是通过时间换空间的做法。

- JDK的ThreadLocal 是典型的通过空间换时间的做法
- 解决hash冲突常见方法
  - 开放地址法（ThreadLocalMap ）（线性探测再散列、二次探测再散列、伪随机探测再散列）（当当前位置出现hash冲突就寻找下一个空的散列地址）
    1. 容易产生堆积问题，不适于大规模的数据存储。
    2. 散列函数的设计对冲突会有很大的影响，插入时可能会出现多次冲突的现象。
    3. 删除的元素是多个冲突元素中的一个，需要对后面的元素作处理，实现较复杂。
  - 链地址法（hashmap）
    1. 处理冲突简单，且无堆积现象，平均查找长度短。
    2. 链表中的结点是动态申请的，适合构造表不能确定长度的情况。
    3. 删除结点的操作易于实现。只要简单地删去链表上相应的结点即可。
    4. 指针需要额外的空间，故当结点规模较小时，开放定址法较为节省空间。
  - rehash
  - 建立公共溢出区
- ThreadLocalMap 采用开放地址法原因
  1. ThreadLocal 中看到一个属性 HASH_INCREMENT = 0x61c88647 ，0x61c88647 是一个神奇的数字，让哈希码能均匀的分布在2的N次方的数组里, 即 Entry[] table，关于这个神奇的数字google 有很多解析，这里就不重复说了
  2. ThreadLocal 往往存放的数据量不会特别大（而且key 是弱引用又会被垃圾回收，及时让数据量更小），这个时候开放地址法简单的结构会显得更省空间，同时数组的查询效率也是非常高，加上第一点的保障，冲突概率也低
- 缓存行
  - CPU缓存，L1,L2,L3
  - 伪共享产生和解决方案
  - JDK的@Contended
  - 线程的栈空间是否是挂在CPU的一个核心（多核）上
- Netty中对于FTL的大量使用

## JDK的ThreadLocal源码

### 核心方法源码

为了查看源码方便，创建一个简单的ThreadLocal使用demo

```java
@Slf4j
public class ThreadLocalTest {
    ThreadLocal<Integer> local1 = ThreadLocal.withInitial(() -> 1024);
    ThreadLocal<Integer> local2 = new ThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 28;
        }
    };

    volatile boolean stop = false;

    @Test
    public void threadLocal() throws Exception {

        log.info("########################################");
        log.info("local1 init:{}", local1.get());
        log.info("local2 init:{}", local2.get());
        Thread thread = new Thread(() -> {
            int count = 0;
            while (true) {

                LockSupport.park();
                log.info("local1 value({}):{}", ++count, local1.get());
                log.info("local2 value({}):{}", count, local2.get());
                if (stop) {
                    return;
                }
            }
        });
        thread.start();

        LockSupport.unpark(thread);

        TimeUnit.SECONDS.sleep(2);

        log.info("change local value");
        local1.set(9527);
        local2.set(2145);
        stop = true;
        LockSupport.unpark(thread);

        thread.join();
        log.info("########################################");
        local1.remove();
        local2.remove();
    }
}
```

输出

```java
22:15:11.059 [main] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - ########################################
22:15:11.062 [main] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local1 init:1024
22:15:11.063 [main] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local2 init:28
22:15:11.064 [Thread-0] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local1 value(1):1024
22:15:11.064 [Thread-0] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local2 value(1):28
22:15:13.065 [main] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - change local value
22:15:13.065 [Thread-0] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local1 value(2):1024
22:15:13.065 [Thread-0] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - local2 value(2):28
22:15:13.065 [main] INFO  i.n.e.lawrenc.netty.ThreadLocalTest - ######################################## 
```

##### ThreadLocal#set

- ThreadLocal#set

  ```java
  public void set(T value) {
      Thread t = Thread.currentThread();
      ThreadLocalMap map = getMap(t);
      if (map != null) {
          map.set(this, value);
      } else {
          createMap(t, value);
      }
  }
  
  ThreadLocalMap getMap(Thread t) {
      return t.threadLocals;
  }
  ```

  首先获取当前线程对象，再根据线程对象t获取ThreadLocalMap，该值是线程的一个成员变量

  ```java
  public class Thread implements Runnable {
      /* ThreadLocal values pertaining to this thread. This map is maintained
       * by the ThreadLocal class. */
      ThreadLocal.ThreadLocalMap threadLocals = null;
  
      /*
       * InheritableThreadLocal values pertaining to this thread. This map is
       * maintained by the InheritableThreadLocal class.
       */
      ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
  }
  ```

  inheritableThreadLocals这个变量也是ThreadLocalMap，配合InheritableThreadLocal使用，稍后会说到。

  首次调用set时，获取到的map是null，因此会进入 createMap(t, value);方法

  ```java
  void createMap(Thread t, T firstValue) {
      t.threadLocals = new ThreadLocalMap(this, firstValue);
  }
  ```

  这里会创建一个ThreadLocalMap给线程变量threadLocals赋值

  ```java
  ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
      table = new Entry[INITIAL_CAPACITY];
      int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
      table[i] = new Entry(firstKey, firstValue);
      size = 1;
      setThreshold(INITIAL_CAPACITY);
  }
  
  //阈值设定
  private void setThreshold(int len) {
      threshold = len * 2 / 3;
  }
  ```

  ThreadLocalMap里面维护了一个Entry数组table，即是给table这个数组初始化，并设值。同时根据threadLocal对象的threadLocalHashCode和初始容量-1进行hash，得到firstValue的落地位置。接着看看Entry的结构

  ```java
  static class ThreadLocalMap {
  
      static class Entry extends WeakReference<ThreadLocal<?>> {
          /** The value associated with this ThreadLocal. */
          Object value;
  
          Entry(ThreadLocal<?> k, Object v) {
              super(k);
              value = v;
          }
      }
      private static final int INITIAL_CAPACITY = 16;
  
      private Entry[] table;
  }
  ```

  Entry是由key，value构成的，且key是一个ThreadLocal的弱引用（当没有强引用指向弱引用时，发生gc会立即回收弱引用对象）。

  其实到此为止第一次赋值就结束了，当我们向ThreadLocal对象里面存入了一个值时，该值和线程Thread绑定，而Thread又和内部成员变量ThreadLocalMap绑定，ThreadLocalMap内部存储了一个Entry数组，Entry是由弱引用（即当前ThreadLocal对象）作为key，我们存入的值作为value。各个依赖关系如下:

  ![image](https://lmy25.wang/upload/2020/08/image-fad4a861e373441d8755fcbbb80625f0.png)

  当在同一个线程第二次设值时，此时线程的map不为空，则会直接进入set方法

  ```java
  map.set(this, value);
  ```

  ThreadLocalMap#set具体如下

  ```java
   private void set(ThreadLocal<?> key, Object value) {
       Entry[] tab = table;
       int len = tab.length;
       //hash计算当前value应该存放的位置
       int i = key.threadLocalHashCode & (len-1);
  
       //若是当前位置已经存在元素，则逐步搜索（链寻址），直到元素为空就结束循环
       for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
           ThreadLocal<?> k = e.get();
  		  //当有相同key，则直接更新为新的value值
           if (k == key) {
               e.value = value;
               return;
           }
  		//当key为null时代表该threadLocal被gc回收了，此时“顺便”做清理相关操作
           if (k == null) {
               //探测式清理
               replaceStaleEntry(key, value, i);
               return;
           }
       }
  	//在for循环中没返回，证明在当前i处entry是为null的，因此直接进行赋值
       tab[i] = new Entry(key, value);
       int sz = ++size;
       //启发式清理之后判断是否需要扩容 sz >= threshold为 第一次（真正扩容阈值小于threshold） 阈值判断
       if (!cleanSomeSlots(i, sz) && sz >= threshold)
           rehash();
   }
  ```

  这里继续跟set方法之前看几个小细节

  1. 这里 int i = key.threadLocalHashCode & (len-1);计算索引用的是魔数和len-1做与，使 `hashcode` 均匀的分布在大小为 2 的 N 次方的数组里

  2. 注意下nextIndex和prevIndex两个方法，它们是环形的，因此个人觉得entry数组更像是一个环形链表。

     ```java
             /**
              * Increment i modulo len.
              */
             private static int nextIndex(int i, int len) {
                 return ((i + 1 < len) ? i + 1 : 0);
             }
     
             /**
              * Decrement i modulo len.
              */
             private static int prevIndex(int i, int len) {
                 return ((i - 1 >= 0) ? i - 1 : len - 1);
             }
     ```

  3. for循环即是一个链寻址寻址的过程，如果在寻址过程中发现被回收的key，则顺便清理这个“脏槽”

  4. if (k == null)成立的条件是弱引用的threalLocal对象被回收，如要被回收，则必须我们手动将threadlocal对象设为null（threadlocal对象创建并set值之后至少存在两个引用，一个是new时候的强引用，一个是threadLocalMap中的弱引用），或者让其存在局部变量中，而通常我们是给的static的静态变量，此时threadlocal在正常情况下是永远无法被回收的，因为这个强引用一直存在整个类的生命周期，则就会导致ThreadLocalMap对象在线程池的情况下会一直存在，set进的不需要使用的数据也得不到清理，因此提倡不需要使用的时候remove显示清除。

  接着看set方法，for循环实际就是一个链寻址的过程，遇见hash冲突的就寻找向下一个槽，直到为空或者key相等，就插入/覆盖新值。当然如果链寻址过程中遇见脏槽，会进入清理方法，顺便清除脏数据。

  进入方法replaceStaleEntry，这个方法相对来说有点绕0.0

  ```java
  /**
   * 该方法的javadoc说到是清除两个空槽之间的所有数据(被回收key的槽)。注意该方法可能不会清理任何数据
   */
  private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                 int staleSlot) {
      Entry[] tab = table;
      int len = tab.length;
      Entry e;
  
      // Back up to check for prior stale entry in current run.
      // We clean out whole runs at a time to avoid continual
      // incremental rehashing due to garbage collector freeing
      // up refs in bunches (i.e., whenever the collector runs).
      //staleSlot位置是链寻址发现的第一个脏槽
      int slotToExpunge = staleSlot;
      //向前查找，最前面（非空槽）的一个脏槽。可以思考下为什么这里向前搜索，向后可以吗？
      for (int i = prevIndex(staleSlot, len);
           (e = tab[i]) != null;
           i = prevIndex(i, len))
          if (e.get() == null)
              slotToExpunge = i;
  
      // Find either the key or trailing null slot of run, whichever
      // occurs first
      //向下查找，遇空槽结束循环
      for (int i = nextIndex(staleSlot, len);
           (e = tab[i]) != null;
           i = nextIndex(i, len)) {
          ThreadLocal<?> k = e.get();
  
          // If we find key, then we need to swap it
          // with the stale entry to maintain hash table order.
          // The newly stale slot, or any other stale slot
          // encountered above it, can then be sent to expungeStaleEntry
          // to remove or rehash all of the other entries in run.
          if (k == key) {
              e.value = value;
  			//很重要的一个交换步骤，将staleSlot处脏槽的数据移到此处，将staleSlot处脏槽的数据赋值为新值（staleSlot和i闭区间内的数据都是hash冲突的数据）。目的是更少的rehash（在staleSlot到i之间没有空槽了，就不需要rehash了）
              tab[i] = tab[staleSlot];
              tab[staleSlot] = e;
  
              // Start expunge at preceding stale entry if it exists
              //判断之前的向前查找是否找到可脏槽，如果没有，则将最前面的脏槽索引标记为i，即i前面（到非空槽）的所有连续槽里面是没有脏槽存在的
              if (slotToExpunge == staleSlot)
                  slotToExpunge = i;
              //先探测式清理，再启发式清理，部分清理出入的为第一个脏槽索引slotToExpunge，len为entry数组长度
              cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
              return;
          }
  
          // If we didn't find stale entry on backward scan, the
          // first stale entry seen while scanning for key is the
          // first still present in the run.
          //在向后查找过程中，如果发现脏槽，且之前的向前查找没有找到连续空间内包含的其他槽，则将第一个脏槽其实点标记为当前i，该if至多只会执行一次
          if (k == null && slotToExpunge == staleSlot)
              slotToExpunge = i;
      }
  
      // If key not found, put new entry in stale slot
      //能到这儿，证明该key在连续区间内没有插入过（可能插入被删除了）
      tab[staleSlot].value = null;
      tab[staleSlot] = new Entry(key, value);
  
      // If there are any other stale entries in run, expunge them
      //之后，如果是向前找到了其余脏槽，或者在向后链寻址的过程中发现了脏槽，则代表存在脏数据，也会执行探测式清理个启发式清理
      if (slotToExpunge != staleSlot)
          cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
  }
  ```

  这个方法算是ThreadLocal里面比较难理解的地方了，注释里面都详细解释了，下面再详细说说几个关键的地方

  1. 第一个迷惑的地方就是向前查找空槽的那个for循环了。这里主要是确定一个连续有值的区间。在staleSlot处是链寻址过程中发现的第一个脏槽，在staleSlot之前的连续非空槽数据可能也是发生过hash冲突，通过链寻址插入的，如果这时候顺便找到之前的空槽（开销小），在之后若是要清理，则会进入清理步骤，将从当前连续区间的第一个脏槽开始清理，连续区间所有脏槽都会被清理rehash。

  2. 第二个for循环箱后查找就是继续链寻址查找插入位置的逻辑。从第一个空槽staleSlot处开始，如果发现key已存在，则证明已经插入过值了，首先更新值，之后再交换，这个交换是很重要的，可以提高性能。将当前i，即key原来的插入位置的entry交换到staleSlot的空槽位置，空槽移到当前i的位置。此时在slotToExpunge到i处的区间均是”干净的槽“，即没有脏槽。

     之后紧跟的if判断向前是否找到了脏槽，如果没有找到，则说明该连续空间的第一个脏槽就是在i处（已经和staleSlot交换过了），之后就是启发式清理了，入参是第一个脏槽位置和数组len，这个我们放到稍后再看。

  3. 在这里的链寻址过程中发现有脏槽，则也会判断之前向前查找是否找到脏槽，如果没有找到也会将第一个脏槽的位置设为当前i。**其实这里和前面的判断给slotToExpunge赋值都是一个目的，使slotToExpunge的值是当前连续区间的第一个脏槽位置，之后在探测式清理的时候就从该处开始清理。**

  4. 如果for循环没有结束方法，则证明在连续区间内该key不存在，则将staleSlot处的脏槽的值清理之后再赋值当前需要插入的新值，最后再判断一下是否存在其余脏数据，即判断slotToExpunge和 staleSlot是否相等。

  最后看一下探测式清理，注意这个方法里的探测式清理入参都是该连续区间的第一个脏槽索引

  ```java
  private int expungeStaleEntry(int staleSlot) {
      Entry[] tab = table;
      int len = tab.length;
  
      // expunge entry at staleSlot
      tab[staleSlot].value = null;
      tab[staleSlot] = null;
      size--;
  
      // Rehash until we encounter null
      Entry e;
      int i;
      for (i = nextIndex(staleSlot, len);
           (e = tab[i]) != null;
           i = nextIndex(i, len)) {
          ThreadLocal<?> k = e.get();
          if (k == null) {
              e.value = null;
              tab[i] = null;
              size--;
          } else {
              int h = k.threadLocalHashCode & (len - 1);
              if (h != i) {
                  tab[i] = null;
  
                  // Unlike Knuth 6.4 Algorithm R, we must scan until
                  // null because multiple entries could have been stale.
                  while (tab[h] != null)
                      h = nextIndex(h, len);
                  tab[h] = e;
              }
          }
      }
      return i;
  }
  ```

  这个探测式清理比较简单，先清理当前连续区间的第一个脏槽，之后从此处开始，向后逐步搜索，如果发现k==null，则清理掉，如果不为null则rehash，重新计算槽位，如果计算出来的槽位不在当前位置，则将当前位置设为null清理，然后从新的地址h处开始链寻址，找到新的槽位插入。如此循环，直到找到第一个空槽null结束，再返回此处索引。

  之后看启发式清理，入参为探测式清理发现的第一个null位置和entry长度（扫描控制循环次数）。

  ```java
  private boolean cleanSomeSlots(int i, int n) {
      boolean removed = false;
      Entry[] tab = table;
      int len = tab.length;
      do {
          i = nextIndex(i, len);
          Entry e = tab[i];
          if (e != null && e.get() == null) {
              n = len;
              removed = true;
              i = expungeStaleEntry(i);
          }
      } while ( (n >>>= 1) != 0);
      return removed;
  }
  ```

  从探测式清理返回的第一个空槽位置开始查找，如果遇见脏槽就清理，之后会将n重置为entry长度len，继续循环，直到n递减为0结束。

  我看到这里有个疑惑，为什么while结束的条件是n为0，n从len开始每次减半，即log2n递减的。

  看了下javadoc的说明：

  **使用对数log2n简单、快速、运行效果较好，如果增加查找次数，或者减少都不太好**

  也引用下别的博主的解释，大致和官方说的差不多：

  主要用于扫描控制（scan control），从while中是通过n来进行条件判断的说明n就是用来控制扫描趟数（循环次数）的。在扫描过程中，如果没有遇到脏entry就整个扫描过程持续log2(n)次，log2(n)的得来是因为n >>>= 1，每次n右移一位相当于n除以2。
  如果在扫描过程中遇到脏entry的话就会令n为当前hash表的长度（n=len），再扫描log2(n)趟，注意此时n增加无非就是多增加了循环次数从而通过nextIndex往后搜索的范围扩大，示意图如下
  ![upload successful](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/%E5%90%AF%E5%8F%91%E5%BC%8F%E6%B8%85%E7%90%86.png)
  按照n的初始值，搜索范围为黑线，当遇到了脏entry，此时n变成了哈希数组的长度（n取值增大），搜索范围log2(n)增大，红线表示。如果在整个搜索过程没遇到脏entry的话，搜索结束，采用这种方式的主要是用于时间效率上的平衡。

  最后还剩一点就是在set的第一个for循环链寻址过程中遇见null的槽之后的处理逻辑

  ```java
  tab[i] = new Entry(key, value);
  int sz = ++size;
  if (!cleanSomeSlots(i, sz) && sz >= threshold)
      rehash();
  
  ---
  //第一次初始的时候会赋值阈值
  private void setThreshold(int len) {
      threshold = len * 2 / 3;
  }
  ```

  在空槽设值之后如果启发式清理成功，且当前容量大于阈值2/3*len，则会rehash

  这里需要注意下rehash实际的条件如下

  ```java
  private void rehash() {
      expungeStaleEntries();
  
      // Use lower threshold for doubling to avoid hysteresis
      if (size >= threshold - threshold / 4)
          resize();
  }
  ```

  在执行探测式清理之后数据量大于等于容量的一半之后就会真正的resize扩容，扩容比较简单，可以简要看看。

  注意阈值和容量都是int，在除以分数时会丢弃小数，假设len=16，则threshold=10，进而实际扩容阈值是10-10/4=8，为len的一半。

  ---

  至此，set方法就已经全部走完了，关键的就是链寻址和清理逻辑，仔细想想还是很好懂的。

##### ThreadLocal#get

get方法比较简单，需要注意的是在getEntry拿不到数据的时候，会链寻址查找，过程中发现脏槽也会清理

```java
private Entry getEntry(ThreadLocal<?> key) {
    int i = key.threadLocalHashCode & (table.length - 1);
    Entry e = table[i];
    if (e != null && e.get() == key)
        return e;
    else
        return getEntryAfterMiss(key, i, e);
}

//链寻址，过程中发现脏槽也清理
private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;

    while (e != null) {
        ThreadLocal<?> k = e.get();
        if (k == key)
            return e;
        if (k == null)
            expungeStaleEntry(i);
        else
            i = nextIndex(i, len);
        e = tab[i];
    }
    return null;
}
```

##### ThreadLocal#remove

remove方法同样也比较简单，调用的是ThreadLocalMap的remove方法

```java
private void remove(ThreadLocal<?> key) {
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {
        if (e.get() == key) {
            e.clear();
            expungeStaleEntry(i);
            return;
        }
    }
}
```

如果清理的是hash冲突链上的某个槽，则会在探测式清理之后rehash

### 附

- 魔数

  ```java
  //魔数
  private static final int HASH_INCREMENT = 0x61c88647;
  ```

- 









---

以下废弃

set方法主要分为三种情况

- 进入for循环没有返回，或者是没有进入for循环

  1. 若是进入for循环没有返回说明在整个entry数组中没有空位了（**这种情况实际是不存在的**）

  2. 没有进入for循环则说明当前索引i处的entry就为null，则直接进行赋值，且size递增

     ```java
     tab[i] = new Entry(key, value);
     int sz = ++size;
     if (!cleanSomeSlots(i, sz) && sz >= threshold)
         rehash();
     ```

     在完成元素的添加之后会进行一次启发式清理，即调用cleanSomeSlots(i, sz)，当启发式清理了元素（至少清理一个元素）则会返回false，进而就不会进行扩容，若是启发式清理没有清理元素，则会根据当前size和threshold判断是否需要扩容，而threshold的初始化是在创建ThreadLocalMap时设定的，可以看见扩容阈值为len的2/3，即为10。

     ```java
     ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
         table = new Entry[INITIAL_CAPACITY];
         int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
         table[i] = new Entry(firstKey, firstValue);
         size = 1;
         setThreshold(INITIAL_CAPACITY);
     }
     
     private void setThreshold(int len) {
         threshold = len * 2 / 3;
     }
     
     /**
       * The initial capacity -- MUST be a power of two.
       */
     private static final int INITIAL_CAPACITY = 16;
     ```

     进入扩容方法rehash

     ```java
     private void rehash() {
         //会进行一次清理之后再判断是否需要扩容
         expungeStaleEntries();
     
         // Use lower threshold for doubling to avoid hysteresis
         if (size >= threshold - threshold / 4)
             resize();
     }
     ```

     进入expungeStaleEntries方法

     ```java
     private void expungeStaleEntries() {
         Entry[] tab = table;
         int len = tab.length;
         for (int j = 0; j < len; j++) {
             Entry e = tab[j];
             if (e != null && e.get() == null)
                 //探测式清理
                 expungeStaleEntry(j);
         }
     }
     ```

     从Entry数组的起始位置开始查找，当发现有Entry的key==null时（e是继承了WeakReference<ThreadLocal<?>>的，因此get（）返回的则是对应得弱引用key，若不存在则代表gc回收了），就触发探测式清理。

     ```java
     private int expungeStaleEntry(int staleSlot) {
         Entry[] tab = table;
         int len = tab.length;
     
         // expunge entry at staleSlot
         //先将当前位置设为null，便于回收
         tab[staleSlot].value = null;
         tab[staleSlot] = null;
         size--;
     
         // Rehash until we encounter null
         //接下来从此处开始寻找是否有发生过hash冲突的，需要rehash的，并顺便清理其余被gc回收的数据
         Entry e;
         int i;
         for (i = nextIndex(staleSlot, len);
              (e = tab[i]) != null;
              i = nextIndex(i, len)) {
             ThreadLocal<?> k = e.get();
             if (k == null) {
                 e.value = null;
                 tab[i] = null;
                 size--;
             } else {
                
                 int h = k.threadLocalHashCode & (len - 1);
                  //表明该值是出现过hash冲突的
                 if (h != i) {
                     //当前位置设为null空槽，便于gc
                     tab[i] = null;
     
                     // Unlike Knuth 6.4 Algorithm R, we must scan until
                     // null because multiple entries could have been stale.
                     //继续向后开放寻址，直到找到为null的entry节点
                     while (tab[h] != null)
                         h = nextIndex(h, len);
                     //将之前i处的元素赋值给距离正确索引h最近的一个空槽上（目的也是为了防止内存泄漏）
                     tab[h] = e;
                 }
             }
         }
         return i;
     }
     ```

     探测式清理的入参为key==null的索引。一次探测式清理的示意图如下（**key相同仅代表hash冲突了，值并不同**）

     ![image](https://lmy25.wang/upload/2020/08/image-41f8bf55de564383b974687595312501.png)

     可以试想下，假设不重新计算hash值，判断是否发生过冲突，并且重新寻址会发生什么？

     进行一次探测式清理之后槽位分布如下

     ![image](https://lmy25.wang/upload/2020/08/image-94f83235b1224c7c9dcbfc304ccf0109.png)

     假设再次插入一个值，该数据和index=6的key相同，则计算出来的索引位置也在k2，由于k2的索引位置在index=3处，则开放寻址会定位到index=5的地方，在index=5插入新数据，此时会出现index=5和index=6处的key相同，但是占用了两个槽位，且在index=6处的槽位永远不会回收，从而造成内存泄漏。

- 进入for循环，满足 if (k == key)

  这种情况比较简单，当key相同，说明是在同一个线程中重复赋值，则直接覆盖value即可。

- 进入for循环，满足k == null

  若key==null，则证明该key被gc回收了。当我们使用完threadlocal变量之后，将其废弃设为null，则new出来的强引用就没了，剩下的只有Entry中的弱引用key，只要发生gc，该key所对应的的threadlocal就会被回收。

  当key==null会先处理失效的key此处的entry。便于gc回收

  ```java
  //staleSlot为失效key的槽位位置
  private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                 int staleSlot) {
      Entry[] tab = table;
      int len = tab.length;
      Entry e;
      int slotToExpunge = staleSlot;
      //1.向前搜索
      for (int i = prevIndex(staleSlot, len); (e = tab[i]) != null;  i = prevIndex(i, len))
          if (e.get() == null)
              slotToExpunge = i;
  
      //2.向后搜索
      for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null;  i = nextIndex(i, len)) {
          ThreadLocal<?> k = e.get();
  
          // 找到相同的key，则覆盖value，之后再清理
          if (k == key) {
              e.value = value;
  
              tab[i] = tab[staleSlot];
              tab[staleSlot] = e;
  
              // 判断是否存在其余被回收的key
              if (slotToExpunge == staleSlot)
                  slotToExpunge = i;
              //先完成全量的探测式清理，再完成启发式清理
              cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
              return;
          }
  
          if (k == null && slotToExpunge == staleSlot)
              slotToExpunge = i;
      }
  
      //3.如果以上没有赋值成功，则将当前被回收key处的槽位重新赋新值
      tab[staleSlot].value = null;
      tab[staleSlot] = new Entry(key, value);
  
      //先完成全量的探测式清理，再完成启发式清理
      // If there are any other stale entries in run, expunge them
      if (slotToExpunge != staleSlot)
          cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
  }
  ```

  以上大致分为三步

  1. 首先向前环形搜索，寻找entry的key为null的位置，即被回收的位置，找到之后将索引赋值给slotToExpunge。直到找到entry==null的位置才结束。

  2. 之后向后环形搜索，也是直到找到entry==null的位置才结束。在寻找过程中，若发现key有相同的（即第一次插入k，v的时候索引是staleSlot，但是staleSlot位置处原来有数据，因此插入的数据会开放寻址到当前位置，即i处插入），则**交换位置**。将当前位置i处的entry更改为已经被回收的staleSlot处的entry，将staleSlot处的entry更改为i处entry（这个很重要）。

     交换位置之后判断slotToExpunge == staleSlot，只有在前面寻找其余被回收entry的key的时候才会更改slotToExpunge 的值，因此当没有其余被回收的key时，当前if才满足，随后将slotToExpunge 的值更改为当前索引i（当前索引i已经和staleSlot交换了数据，i此处的数据时无效的，被回收了key的）

     最后进行一次全量的探测式清理，逻辑和之前的探测式清理同理，会进行rehash判断数据是否需要重新移动槽位。完成之后再进行一次启发式清理，逻辑如下

     ```java
     /**
      * 启发式地清理slot,
      * i对应entry是非无效（指向的ThreadLocal没被回收，或者entry本身为空）
      * n是用于控制控制扫描次数的
      * 正常情况下如果log n次扫描没有发现无效slot，函数就结束了
      * 但是如果发现了无效的slot，将n置为table的长度len，做一次连续段的清理
      * 再从下一个空的slot开始继续扫描
      * 
      * 这个函数有两处地方会被调用，一处是插入的时候可能会被调用，另外个是在替换无效slot的时候可能会被调用，
      * 区别是前者传入的n为元素个数，后者为table的容量
      */
     private boolean cleanSomeSlots(int i, int n) {
         boolean removed = false;
         Entry[] tab = table;
         int len = tab.length;
         do {
             // i在任何情况下自己都不会是一个无效slot(指向的ThreadLocal没被回收，或者entry本身为空)，所以从下一个开始判断
             i = nextIndex(i, len);
             Entry e = tab[i];
             if (e != null && e.get() == null) {
                 // 扩大扫描控制因子
                 n = len;
                 removed = true;
                  // 清理一个连续段
                 i = expungeStaleEntry(i);
             }
         } while ( (n >>>= 1) != 0);
         return removed;
     }
     ```

  3. 如果for循环没有结束返回，则证明新值没有插入成功，则将此staleSlot处赋值为新值，最后再判断下是否有其余被回收的槽存在，若有则执行清理。

  replaceStaleEntry()方法整体执行流程如下示意图:

  ![image](https://lmy25.wang/upload/2020/08/image-7f667dc51a854213b60fb7f565e98287.png)
  
  5. 可以看出来，threadlocal在每次set时都会检测是否需要清理，不过还是建议手动remove，以防内存泄漏。
  6. 
  
- ThreadLocal#get

  ```java
  public T get() {
      Thread t = Thread.currentThread();
      ThreadLocalMap map = getMap(t);
      if (map != null) {
          ThreadLocalMap.Entry e = map.getEntry(this);
          if (e != null) {
              @SuppressWarnings("unchecked")
              T result = (T)e.value;
              return result;
          }
      }
      return setInitialValue();
  }
  ```

  get方法就简单很多了，先拿到线程对于的map，如没有设过值，则设置初始值并返回

  ```java
  private T setInitialValue() {
      T value = initialValue();
      Thread t = Thread.currentThread();
      ThreadLocalMap map = getMap(t);
      if (map != null) {
          map.set(this, value);
      } else {
          createMap(t, value);
      }
      if (this instanceof TerminatingThreadLocal) {
          TerminatingThreadLocal.register((TerminatingThreadLocal<?>) this);
      }
      return value;
  }
  ```

- ThreadLocal#remove

  ```java
  public void remove() {
      ThreadLocalMap m = getMap(Thread.currentThread());
      if (m != null) {
          m.remove(this);
      }
  }
  ```

---

以上废弃

### 总结

## FastThreadLocal源码

FastThreadLocal是netty自己实现的threadLocal，摒弃了jdk的存储结构，不会出现hash冲突，在高并发情况下，基本性能可以高出jdk的3倍之多

以下所有的FTL为FastThreadLocal的简称，而FTLT为FastThreadLocalThread的简称

### new FastThreadLocal()

```java
public FastThreadLocal() {
    index = InternalThreadLocalMap.nextVariableIndex();
}
```

初始化index的值，注意这里是的index是从开始递增的，nextIndex变量是在父类声明的，而在FastThreadLocal里面有一个静态变量variablesToRemoveIndex，该变量在FTL加载时已经被赋值为0，且nextIndex已经递增为1了

```java
class UnpaddedInternalThreadLocalMap {
    static final AtomicInteger nextIndex = new AtomicInteger();
}
//FastThreadLocal
private static final int variablesToRemoveIndex = InternalThreadLocalMap.nextVariableIndex();
```

这里提一句，index是每个FTL都有一个，且不重复的，因此也就不会造成hash冲突；variablesToRemoveIndex永远为0，在0处存储所有的FTL信息

### set

```java
public final void set(V value) {
   // InternalThreadLocalMap初始化时全部填充的UNSET，后面跟代码会发现
    if (value != InternalThreadLocalMap.UNSET) {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        setKnownNotUnset(threadLocalMap, value);
    } else {//这个else分支有必要吗？
        remove();
    }
}
// 常量
public static final Object UNSET = new Object();
```

可以发现，在第一次设值时，通常是满足if条件的

```java
public static InternalThreadLocalMap get() {
    Thread thread = Thread.currentThread();
    if (thread instanceof FastThreadLocalThread) {
        return fastGet((FastThreadLocalThread) thread);
    } else {
        //曲线救国  兼兼容非FastThreadLocalThread线程使用的情况
        return slowGet();
    }
}
```

get方法主要是拿到一个InternalThreadLocalMap对象，这里有个区分是否是FTLT的判断，如果是就使用fastGet实例化，否则slowGet。slowGet也是创建InternalThreadLocalMap对象，只不过将该对象存放到ThreadLocal里面了。下面主要看下fastGet过程

```jav
    private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread) {
        InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
        }
        return threadLocalMap;
    }
    
public class FastThreadLocalThread extends Thread {
 
    private InternalThreadLocalMap threadLocalMap;
}
```

拿到FTLF里面的threadLocalMap对象，如果不存在，则创建，第一次来肯定是创建

```java
 private InternalThreadLocalMap() {
        super(newIndexedVariableTable());
    }

    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[32];
        Arrays.fill(array, UNSET);
        return array;
    }
	//super的构造方法
    UnpaddedInternalThreadLocalMap(Object[] indexedVariables) {
        this.indexedVariables = indexedVariables;
    }

   public static final Object UNSET = new Object();
```

主要做了一件事，初始化FTL中的InternalThreadLocalMap存储结构，即indexedVariables数组，该数组在第一次初始化时全部填充了UNSET。

回到set方法，进入setKnownNotUnset(threadLocalMap, value);方法设值

```java
 private void setKnownNotUnset(InternalThreadLocalMap threadLocalMap, V value) {
        if (threadLocalMap.setIndexedVariable(index, value)) {
            //初始化索引0处的一个待移除集合  Set<FastThreadLocal<?>>
            addToVariablesToRemove(threadLocalMap, this);
        }
 }
```

设值成功之后，如果是第一次插入就会初始化idx=0的数据

```java
    public boolean setIndexedVariable(int index, Object value) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object oldValue = lookup[index];
            lookup[index] = value;
            //第一次插入则是true
            return oldValue == UNSET;
        } else {
            expandIndexedVariableTableAndSet(index, value);
            return true;
        }
    }
```

设值比较简单，如果indexedVariables数组还有容量则直接赋值，这里就体现了FTL比传统的ThreadLocal快的原因了，没有hash操作，也不会出现hash冲突，每个FTL初始化成功之后就已经确定好了落在数组的哪个位置，插入和读取速度都快很多。如果第一次插入，该槽的旧值就是初始化填充的UNSET，该方法就会返回true。

如果容量满了就扩容

```java
    private void expandIndexedVariableTableAndSet(int index, Object value) {
        Object[] oldArray = indexedVariables;
        final int oldCapacity = oldArray.length;
        int newCapacity = index;
        newCapacity |= newCapacity >>>  1;
        newCapacity |= newCapacity >>>  2;
        newCapacity |= newCapacity >>>  4;
        newCapacity |= newCapacity >>>  8;
        newCapacity |= newCapacity >>> 16;
        newCapacity ++;

        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        newArray[index] = value;
        indexedVariables = newArray;
    }
```

和hashmap的扩容差不多，都是找到离源oldArray长度最近的一个2的幂次方的数，即在第一次调整容量之后每次扩容2倍，之后数据再赋值过去，由此可以可见，如果FTL的存储数量能预估，最好设定初始容量，以防扩容。

当setIndexedVariable返回true时，代表第一次插入，就会初始化0处的待移除集合，继续跟 addToVariablesToRemove(threadLocalMap, this);

```java
    private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
        Set<FastThreadLocal<?>> variablesToRemove;
        if (v == InternalThreadLocalMap.UNSET || v == null) {
            //使用IdentityHashMap确保key FTL不会出现hash冲突
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<FastThreadLocal<?>, Boolean>());
            threadLocalMap.setIndexedVariable(variablesToRemoveIndex, variablesToRemove);
        } else {
            variablesToRemove = (Set<FastThreadLocal<?>>) v;
        }

        variablesToRemove.add(variable);
    }
```

variablesToRemoveIndex值是静态变量，前面已经分析过了，class被加载的时候，该变量随后会被赋值为0，第一次插入时得到的v=UNSET，则会进入初始化步骤

variablesToRemove是SetFromMap类型，简单理解为一个set，之后将该set存到threadLocalMap的variablesToRemoveIndex=0处，再将本次新增的FastThreadLocal对象添加进set。

至此，FTL第一次插入已经完成，之后的插入基本差不多，先往threadLocalMap的indexedVariables数组中添加值，之后再判断该ftl对象是否是第一次插入，如果是就再添加到0处的set集合中

### get

```java
    public final V get() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }

        return initialize(threadLocalMap);
    }
```

get方法比较简单，拿到threadLocalMap之后，再根据当前FTL的索引index拿到目标值，如果没有，则返回init的默认值

### remove

```java
    public final void remove() {
        remove(InternalThreadLocalMap.getIfSet());
    }
	
    public static InternalThreadLocalMap getIfSet() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return ((FastThreadLocalThread) thread).threadLocalMap();
        }
        return slowThreadLocalMap.get();
    }
```

拿到InternalThreadLocalMap对象，调用重载的remove方法

```java
    public final void remove(InternalThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return;
        }
		//1.
        Object v = threadLocalMap.removeIndexedVariable(index);
        //2.
        removeFromVariablesToRemove(threadLocalMap, this);
		//3.
        if (v != InternalThreadLocalMap.UNSET) {
            try {
                onRemoval((V) v);
            } catch (Exception e) {
                PlatformDependent.throwException(e);
            }
        }
    }
```

分三步完成

1. 根据索引index移除threadLocalMap的数组中的值
2. 从待移除set中，即index=0处移除该FTL对象
3. 如果移除成功，则回调onRemoval方法

### removeAll

```java
    public static void removeAll() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return;
        }

        try {
            Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
            if (v != null && v != InternalThreadLocalMap.UNSET) {
                @SuppressWarnings("unchecked")
                Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
                //????????idx=0处的set是否有存在的必要(removeall直接删除InternalThreadLocalMap(map里面有空槽))；若存在为什么要用set（）；
                // 为什么要转为数组（toarry复制,java.util.IdentityHashMap.KeySet.toArray(T[])复写了复制逻辑）再循环（如果是set则迭代器，数组则for）
                // https://github.com/netty/netty/issues/10599#issuecomment-730283389
                FastThreadLocal<?>[] variablesToRemoveArray =
                        variablesToRemove.toArray(new FastThreadLocal[0]);
                for (FastThreadLocal<?> tlv : variablesToRemoveArray) {
                    tlv.remove(threadLocalMap);
                }
            }
        } finally {
            InternalThreadLocalMap.remove();
        }
    }
```

和remove方法大致差不多，区别就在于是拿出index=0处的set集合，转为variablesToRemoveArray数组，一次调用remove方法全部移除，最后finally中再清除InternalThreadLocalMap

## 参考文章

[Netty-性能优化工具类之FastThreadLocal分析](https://xuanjian1992.top/2019/09/06/Netty-%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96%E5%B7%A5%E5%85%B7%E7%B1%BB%E4%B9%8BFastThreadLocal%E5%88%86%E6%9E%90/)

[从CPU Cache出发彻底弄懂volatile/synchronized/cas机制](https://juejin.im/post/6844903779276439560#comment)

[FastThreadLocal吞吐量居然是ThreadLocal的3倍](https://juejin.im/post/6844903878870171662)（包含大量测试以及参考美团的cpu缓存文章）

[Netty In Action](https://search.jd.com/Search?keyword=netty%20in%20action&enc=utf-8&suggest=1.def.0.base&wq=Netty%20in&pvid=fca1efd55e0848c9a7e9fe29aaf4057e)

 [计算对象大小](https://www.jianshu.com/p/9d729c9c94c4)

[美团agent相关的动态追踪](https://tech.meituan.com/2019/02/28/java-dynamic-trace.html)

[缓存行]( https://my.oschina.net/manmao/blog/804161?nocache=1534146640808)

## 附

### 使用ftl地方

io.netty.buffer.PooledByteBufAllocator.PoolThreadLocalCache

![image-20201116225931847](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/netty%E4%BD%BF%E7%94%A8ftl1.png)

```
private final PoolThreadLocalCache threadCache;
```

---

Recycler对象池中

![Recycler对象池中使用FTL](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/%20netty%E4%BD%BF%E7%94%A8ftl2.png)






