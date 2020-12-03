<article itemscope="itemscope" itemtype="http://schema.org/Article" data-entry-id="6844903656299593742" data-draft-id="6845075593764012040" class="article" data-v-761b54e8="" data-v-6a3e47cf=""><meta itemprop="url" content="https://juejin.im/post/6844903656299593742"> <meta itemprop="headline" content="Java并发（5）- ReentrantLock与AQS"> <meta itemprop="keywords" content="Node.js,后端,Java,安全"> <meta itemprop="datePublished" content="2018-08-14T01:53:56.000Z"> <meta itemprop="image" content="https://b-gold-cdn.xitu.io/icon/icon-128.png"> <div itemprop="author" itemscope="itemscope" itemtype="http://schema.org/Person"><meta itemprop="name" content="knock_小新"> <meta itemprop="url" content="https://juejin.cn/user/1838039171884413"></div> <div itemprop="publisher" itemscope="itemscope" itemtype="http://schema.org/Organization"><meta itemprop="name" content="掘金"> <div itemprop="logo" itemscope="itemscope" itemtype="https://schema.org/ImageObject"><meta itemprop="url" content="https://b-gold-cdn.xitu.io/icon/icon-white-180.png"> <meta itemprop="width" content="180"> <meta itemprop="height" content="180"></div></div> <div class="author-info-block" data-v-761b54e8=""><a href="/user/1838039171884413" target="_blank" rel="" class="avatar-link" data-v-761b54e8=""><div data-src="https://user-gold-cdn.xitu.io/2017/12/18/160690085f941d90?imageView2/1/w/100/h/100/q/85/format/webp/interlace/1" class="lazy avatar avatar loaded" style="background-image: url(&quot;https://user-gold-cdn.xitu.io/2017/12/18/160690085f941d90?imageView2/1/w/100/h/100/q/85/format/webp/interlace/1&quot;);" data-v-47cc2604="" data-v-1c418ef0="" data-v-761b54e8=""></div></a> <div class="author-info-box" data-v-761b54e8=""><a data-v-1b6b7cba="" data-v-761b54e8="" href="/user/1838039171884413" target="_blank" rel="" class="username username ellipsis"><span data-v-1b6b7cba="" class="name" style="max-width: calc(100% - 50px);">
      knock_小新
    </span> <a data-v-3a1b18e3="" data-v-1b6b7cba="" href="/book/5c90640c5188252d7941f5bb/section/5c9065385188252da6320022" target="_blank" rel="" class="rank"><img data-v-3a1b18e3="" src="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMyIgaGVpZ2h0PSIxNCIgdmlld0JveD0iMCAwIDIzIDE0Ij4KICAgIDxnIGZpbGw9Im5vbmUiIGZpbGwtcnVsZT0iZXZlbm9kZCI+CiAgICAgICAgPHBhdGggZmlsbD0iIzZFQ0VGRiIgZD0iTTMgMWgxN2EyIDIgMCAwIDEgMiAydjhhMiAyIDAgMCAxLTIgMkgzYTIgMiAwIDAgMS0yLTJWM2EyIDIgMCAwIDEgMi0yeiIvPgogICAgICAgIDxwYXRoIGZpbGw9IiNGRkYiIGQ9Ik0zIDRoMnY3SDN6TTggNmgybDIgNWgtMnoiLz4KICAgICAgICA8cGF0aCBmaWxsPSIjRkZGIiBkPSJNMTQgNmgtMmwtMiA1aDJ6TTMgOWg1djJIM3pNMTUgM2g1djJoLTV6TTE4IDVoMnYyaC0yek0xNSA5VjdoMnYyeiIvPgogICAgICAgIDxwYXRoIGZpbGw9IiNGRkYiIGQ9Ik0xNSA4VjZoNXYyek0xNSA5aDV2MmgtNXoiLz4KICAgIDwvZz4KPC9zdmc+Cg==" alt="lv-2"></a> </a> <div class="meta-box" data-v-761b54e8=""><time datetime="2018-08-14T01:53:56.000Z" title="Tue Aug 14 2018 09:53:56 GMT+0800 (China Standard Time)" class="time" data-v-761b54e8="">
                  2018年08月14日
                </time> <span class="views-count" data-v-761b54e8="">阅读 2211</span> <!----></div></div> <button class="follow-button follow" data-v-06c7d5b3="" data-v-761b54e8=""><span data-v-06c7d5b3="">关注</span></button></div> <!----> <h1 class="article-title" data-v-761b54e8=""><!---->
            Java并发（5）- ReentrantLock与AQS
          </h1> <!----> <div itemprop="articleBody" class="article-content" data-v-761b54e8=""><div class="markdown-body"><style>.markdown-body{word-break:break-word;line-height:1.75;font-weight:400;font-size:15px;overflow-x:hidden;color:#333}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6{line-height:1.5;margin-top:35px;margin-bottom:10px;padding-bottom:5px}.markdown-body h1{font-size:30px;margin-bottom:5px}.markdown-body h2{padding-bottom:12px;font-size:24px;border-bottom:1px solid #ececec}.markdown-body h3{font-size:18px;padding-bottom:0}.markdown-body h4{font-size:16px}.markdown-body h5{font-size:15px}.markdown-body h6{margin-top:5px}.markdown-body p{line-height:inherit;margin-top:22px;margin-bottom:22px}.markdown-body img{max-width:100%}.markdown-body hr{border:none;border-top:1px solid #ddd;margin-top:32px;margin-bottom:32px}.markdown-body code{word-break:break-word;border-radius:2px;overflow-x:auto;background-color:#fff5f5;color:#ff502c;font-size:.87em;padding:.065em .4em}.markdown-body code,.markdown-body pre{font-family:Menlo,Monaco,Consolas,Courier New,monospace}.markdown-body pre{overflow:auto;position:relative;line-height:1.75}.markdown-body pre>code{font-size:12px;padding:15px 12px;margin:0;word-break:normal;display:block;overflow-x:auto;color:#333;background:#f8f8f8}.markdown-body a{text-decoration:none;color:#0269c8;border-bottom:1px solid #d1e9ff}.markdown-body a:active,.markdown-body a:hover{color:#275b8c}.markdown-body table{display:inline-block!important;font-size:12px;width:auto;max-width:100%;overflow:auto;border:1px solid #f6f6f6}.markdown-body thead{background:#f6f6f6;color:#000;text-align:left}.markdown-body tr:nth-child(2n){background-color:#fcfcfc}.markdown-body td,.markdown-body th{padding:12px 7px;line-height:24px}.markdown-body td{min-width:120px}.markdown-body blockquote{color:#666;padding:1px 23px;margin:22px 0;border-left:4px solid #cbcbcb;background-color:#f8f8f8}.markdown-body blockquote:after{display:block;content:""}.markdown-body blockquote>p{margin:10px 0}.markdown-body ol,.markdown-body ul{padding-left:28px}.markdown-body ol li,.markdown-body ul li{margin-bottom:0;list-style:inherit}.markdown-body ol li .task-list-item,.markdown-body ul li .task-list-item{list-style:none}.markdown-body ol li .task-list-item ol,.markdown-body ol li .task-list-item ul,.markdown-body ul li .task-list-item ol,.markdown-body ul li .task-list-item ul{margin-top:0}.markdown-body ol ol,.markdown-body ol ul,.markdown-body ul ol,.markdown-body ul ul{margin-top:3px}.markdown-body ol li{padding-left:6px}@media (max-width:720px){.markdown-body h1{font-size:24px}.markdown-body h2{font-size:20px}.markdown-body h3{font-size:18px}}</style><h2 class="heading" data-id="heading-0">引言</h2>
<p>在<code>synchronized</code>未优化之前，我们在编码中使用最多的同步工具类应该是<code>ReentrantLock</code>类，<code>ReentrantLock</code>拥有优化后<code>synchronized</code>关键字的性能，又提供了更多的灵活性。相比<code>synchronized</code>，他在功能上更加强大，具有等待可中断，公平锁以及绑定多个条件等<code>synchronized</code>不具备的功能，是我们开发过程中必须要重点掌握的一个关键并发类。</p>
<p><code>ReentrantLock</code>在JDK并发包中举足轻重，不仅是因为他本身的使用频度，同时他也为大量JDK并发包中的并发类提供底层支持，包括<code>CopyOnWriteArrayLit</code>、<code>CyclicBarrier</code>和<code>LinkedBlockingDeque</code>等等。既然<code>ReentrantLock</code>如此重要，那么了解他的底层实现原理对我们在不同场景下灵活使用<code>ReentrantLock</code>以及查找各种并发问题就很关键。这篇文章就带领大家一步步剖析<code>ReentrantLock</code>底层的实现逻辑，了解实现逻辑之后又应该怎么更好的使用<code>ReentrantLock</code>。</p>
<h2 class="heading" data-id="heading-1">ReentrantLock与AbstractQueuedSynchronizer的关系</h2>
<p>在使用<code>ReentrantLock</code>类时，第一步就是对他进行实例化，也就是使用<code>new ReentrantLock()</code>，来看看他的实例化的源码：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">ReentrantLock</span><span class="hljs-params">()</span> </span>{
    sync = <span class="hljs-keyword">new</span> NonfairSync();
}

<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">ReentrantLock</span><span class="hljs-params">(<span class="hljs-keyword">boolean</span> fair)</span> </span>{
    sync = fair ? <span class="hljs-keyword">new</span> FairSync() : <span class="hljs-keyword">new</span> NonfairSync();
}
<span class="copy-code-btn">复制代码</span></code></pre><p>在代码中可以看到，<code>ReentrantLock</code>提供了2个实例化方法，未带参数的实例化方法默认用<code>NonfairSync()</code>初始化了<code>sync</code>字段，带参数的实例化方法通过参数区用<code>NonfairSync()</code>或<code>FairSync()</code>初始化<code>sync</code>字段。</p>
<p>通过名字看出也就是我们常用的非公平锁与公平锁的实现，公平锁需要通过排队FIFO的方式来获取锁，非公平锁也就是说可以插队，默认情况下<code>ReentrantLock</code>会使用非公平锁的实现。那么是<code>sync</code>字段的实现逻辑是什么呢？看下<code>sync</code>的代码：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> Sync sync;

<span class="hljs-keyword">abstract</span> <span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Sync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractQueuedSynchronizer</span> </span>{......}

<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">NonfairSync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Sync</span> </span>{......}

<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">FairSync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Sync</span> </span>{......}
<span class="copy-code-btn">复制代码</span></code></pre><p>到这里就发现了<code>AbstractQueuedSynchronizer</code>类，公平锁和非公平锁其实都是在<code>AbstractQueuedSynchronizer</code>的基础上实现的，也就是AQS。AQS提供了<code>ReentrantLock</code>实现的基础。</p>
<h2 class="heading" data-id="heading-2">ReentrantLock的lock()方法</h2>
<p>分析了<code>ReentrantLock</code>的实例化之后，来看看他是怎么实现锁这个功能的：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-comment">//ReentrantLock的lock方法</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">lock</span><span class="hljs-params">()</span> </span>{
    sync.lock();
}

<span class="hljs-comment">//调用了Sync中的lock抽象方法</span>
<span class="hljs-keyword">abstract</span> <span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Sync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractQueuedSynchronizer</span> </span>{
    ......
    <span class="hljs-comment">/**
        * Performs {<span class="hljs-doctag">@link</span> Lock#lock}. The main reason for subclassing
        * is to allow fast path for nonfair version.
        */</span>
    <span class="hljs-function"><span class="hljs-keyword">abstract</span> <span class="hljs-keyword">void</span> <span class="hljs-title">lock</span><span class="hljs-params">()</span></span>;
    ......
}
<span class="copy-code-btn">复制代码</span></code></pre><p>调用了<code>sync</code>的<code>lock()</code>方法，<code>Sync</code>类的<code>lock()</code>方法是一个抽象方法，<code>NonfairSync()</code>和<code>FairSync()</code>分别对<code>lock()</code>方法进行了实现。</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-comment">//非公平锁的lock实现</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">NonfairSync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Sync</span> </span>{
    ......
    <span class="hljs-comment">/**
        * Performs lock.  Try immediate barge, backing up to normal
        * acquire on failure.
        */</span>
    <span class="hljs-function"><span class="hljs-keyword">final</span> <span class="hljs-keyword">void</span> <span class="hljs-title">lock</span><span class="hljs-params">()</span> </span>{
        <span class="hljs-keyword">if</span> (compareAndSetState(<span class="hljs-number">0</span>, <span class="hljs-number">1</span>)) <span class="hljs-comment">//插队操作，首先尝试CAS获取锁，0为锁空闲</span>
            setExclusiveOwnerThread(Thread.currentThread()); <span class="hljs-comment">//获取锁成功后设置当前线程为占有锁线程</span>
        <span class="hljs-keyword">else</span>
            acquire(<span class="hljs-number">1</span>);
    }
    ......
}

<span class="hljs-comment">//公平锁的lock实现</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">FairSync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Sync</span> </span>{
    ......
    <span class="hljs-function"><span class="hljs-keyword">final</span> <span class="hljs-keyword">void</span> <span class="hljs-title">lock</span><span class="hljs-params">()</span> </span>{
        acquire(<span class="hljs-number">1</span>);
    }
    ......
}
<span class="copy-code-btn">复制代码</span></code></pre><p>注意看他们的区别，<code>NonfairSync()</code>会先进行一个CAS操作，将一个state状态从0设置到1，这个也就是上面所说的非公平锁的“插队”操作，前面讲过CAS操作默认是原子性的，这样就保证了设置的线程安全性。这是非公平锁和公平锁的第一点区别。</p>
<p>那么这个state状态是做什么用的呢？从0设置到1又代表了什么呢？再来看看跟state有关的源码：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">compareAndSetState</span><span class="hljs-params">(<span class="hljs-keyword">int</span> expect, <span class="hljs-keyword">int</span> update)</span> </span>{
    <span class="hljs-comment">// See below for intrinsics setup to support this</span>
    <span class="hljs-keyword">return</span> unsafe.compareAndSwapInt(<span class="hljs-keyword">this</span>, stateOffset, expect, update);
}

<span class="hljs-comment">/**
    * The synchronization state.
    */</span>
<span class="hljs-keyword">private</span> <span class="hljs-keyword">volatile</span> <span class="hljs-keyword">int</span> state;

<span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> <span class="hljs-title">getState</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> state;
}

<span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">void</span> <span class="hljs-title">setState</span><span class="hljs-params">(<span class="hljs-keyword">int</span> newState)</span> </span>{
    state = newState;
}
<span class="copy-code-btn">复制代码</span></code></pre><p>首先state变量是一个<code>volatile</code>修饰的<code>int</code>类型变量，这样就保证了这个变量在多线程环境下的可见性。从变量的注释“The synchronization state”可以看出state代表了一个同步状态。再回到上面的<code>lock()</code>方法，在设置成功之后，调用了<code>setExclusiveOwnerThread</code>方法将当前线程设置给了一个私有的变量，这个变量代表了当前获取锁的线程，放到了AQS的父类<code>AbstractOwnableSynchronizer</code>类中实现。</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-keyword">public</span> <span class="hljs-keyword">abstract</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">AbstractOwnableSynchronizer</span>
    <span class="hljs-keyword">implements</span> <span class="hljs-title">java</span>.<span class="hljs-title">io</span>.<span class="hljs-title">Serializable</span> </span>{
    ......

    <span class="hljs-comment">/**
     * The current owner of exclusive mode synchronization.
     */</span>
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">transient</span> Thread exclusiveOwnerThread;

    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">void</span> <span class="hljs-title">setExclusiveOwnerThread</span><span class="hljs-params">(Thread thread)</span> </span>{
        exclusiveOwnerThread = thread;
    }

    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> Thread <span class="hljs-title">getExclusiveOwnerThread</span><span class="hljs-params">()</span> </span>{
        <span class="hljs-keyword">return</span> exclusiveOwnerThread;
    }
}
<span class="copy-code-btn">复制代码</span></code></pre><p>如果设置state成功，<code>lock()</code>方法执行完毕，代表获取了锁。可以看出state状态就是用来管理是否获取到锁的一个同步状态，0代表锁空闲，1代表获取到了锁。那么如果设置state状态不成功呢？接下来会调用<code>acquire(1)</code>方法，公平锁则直接调用<code>acquire(1)</code>方法，不会用CAS操作进行插队。<code>acquire(1)</code>方法是实现在AQS中的一个方法，看下他的源码：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">void</span> <span class="hljs-title">acquire</span><span class="hljs-params">(<span class="hljs-keyword">int</span> arg)</span> </span>{
    <span class="hljs-keyword">if</span> (!tryAcquire(arg) &amp;&amp;
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
<span class="copy-code-btn">复制代码</span></code></pre><p>这个方法很重要也很简单理解，有几步操作，首先调用<code>tryAcquire</code>尝试获取锁，如果成功，则执行完毕，如果获取失败，则调用<code>addWaiter</code>方法添加当前线程到等待队列，同时添加后执行<code>acquireQueued</code>方法挂起线程。如果挂起等待中需要中断则执行<code>selfInterrupt</code>将线程中断。下面来具体看看这个流程执行的细节，首先看看<code>tryAcquire</code>方法：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">tryAcquire</span><span class="hljs-params">(<span class="hljs-keyword">int</span> arg)</span> </span>{
    <span class="hljs-keyword">throw</span> <span class="hljs-keyword">new</span> UnsupportedOperationException();
}

<span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">tryAcquire</span><span class="hljs-params">(<span class="hljs-keyword">int</span> acquires)</span> </span>{
    <span class="hljs-keyword">return</span> nonfairTryAcquire(acquires);
}

<span class="hljs-comment">//NonfairSync</span>
<span class="hljs-function"><span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">nonfairTryAcquire</span><span class="hljs-params">(<span class="hljs-keyword">int</span> acquires)</span> </span>{
    <span class="hljs-keyword">final</span> Thread current = Thread.currentThread();
    <span class="hljs-keyword">int</span> c = getState();
    <span class="hljs-keyword">if</span> (c == <span class="hljs-number">0</span>) { <span class="hljs-comment">//锁空闲</span>
        <span class="hljs-keyword">if</span> (compareAndSetState(<span class="hljs-number">0</span>, acquires)) { <span class="hljs-comment">//再次cas操作获取锁</span>
            setExclusiveOwnerThread(current);
            <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
        }
    }
    <span class="hljs-keyword">else</span> <span class="hljs-keyword">if</span> (current == getExclusiveOwnerThread()) { <span class="hljs-comment">//当前线程重复获取锁，也就是锁重入</span>
        <span class="hljs-keyword">int</span> nextc = c + acquires;
        <span class="hljs-keyword">if</span> (nextc &lt; <span class="hljs-number">0</span>) <span class="hljs-comment">// overflow</span>
            <span class="hljs-keyword">throw</span> <span class="hljs-keyword">new</span> Error(<span class="hljs-string">"Maximum lock count exceeded"</span>);
        setState(nextc);
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
    }
    <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
}

<span class="hljs-comment">//FairSync</span>
<span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">tryAcquire</span><span class="hljs-params">(<span class="hljs-keyword">int</span> acquires)</span> </span>{
    <span class="hljs-keyword">final</span> Thread current = Thread.currentThread();
    <span class="hljs-keyword">int</span> c = getState();
    <span class="hljs-keyword">if</span> (c == <span class="hljs-number">0</span>) {
        <span class="hljs-keyword">if</span> (!hasQueuedPredecessors() &amp;&amp; <span class="hljs-comment">//判断队列中是否已经存在等待线程，如果存在则获取锁失败，需要排队</span>
            compareAndSetState(<span class="hljs-number">0</span>, acquires)) { <span class="hljs-comment">//不存在等待线程，再次cas操作获取锁</span>
            setExclusiveOwnerThread(current);
            <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
        }
    }
    <span class="hljs-keyword">else</span> <span class="hljs-keyword">if</span> (current == getExclusiveOwnerThread()) { <span class="hljs-comment">//当前线程重复获取锁，也就是锁重入</span>
        <span class="hljs-keyword">int</span> nextc = c + acquires;
        <span class="hljs-keyword">if</span> (nextc &lt; <span class="hljs-number">0</span>)
            <span class="hljs-keyword">throw</span> <span class="hljs-keyword">new</span> Error(<span class="hljs-string">"Maximum lock count exceeded"</span>);
        setState(nextc);
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
    }
    <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
}

<span class="hljs-comment">//AQS中实现，判断队列中是否已经存在等待线程</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">hasQueuedPredecessors</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-comment">// The correctness of this depends on head being initialized</span>
    <span class="hljs-comment">// before tail and on head.next being accurate if the current</span>
    <span class="hljs-comment">// thread is first in queue.</span>
    Node t = tail; <span class="hljs-comment">// Read fields in reverse initialization order</span>
    Node h = head;
    Node s;
    <span class="hljs-keyword">return</span> h != t &amp;&amp;
        ((s = h.next) == <span class="hljs-keyword">null</span> || s.thread != Thread.currentThread());
}
<span class="copy-code-btn">复制代码</span></code></pre><p>AQS没有提供具体的实现，<code>ReentrantLock</code>中公平锁和非公平锁分别有自己的实现。非公平锁在锁空闲的状态下再次CAS操作尝试获取锁，保证线程安全。如果当前锁非空闲，也就是<code>state状态不为0，则判断是否是重入锁，也就是同一个线程多次获取锁，是重入锁则将state状态+1，这也是</code>ReentrantLock`支持锁重入的逻辑。</p>
<p>公平锁和非公平锁在这上面有第二点区别，公平锁在锁空闲时首先会调用<code>hasQueuedPredecessors</code>方法判断锁等待队列中是否存在等待线程，如果存在，则不会去尝试获取锁，而是走接下来的排队流程。至此非公平锁和公平锁的区别大家应该清楚了。如果面试时问道公平锁和非公平锁的区别，相信大家可以很容易答出来了。</p>
<p>通过<code>tryAcquire</code>获取锁失败之后，会调用<code>acquireQueued(addWaiter)</code>，先来看看<code>addWaiter</code>方法：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">private</span> Node <span class="hljs-title">addWaiter</span><span class="hljs-params">(Node mode)</span> </span>{
    Node node = <span class="hljs-keyword">new</span> Node(Thread.currentThread(), mode);   <span class="hljs-comment">//用EXCLUSIVE模式初始化一个Node节点，代表是一个独占锁节点</span>
    <span class="hljs-comment">// Try the fast path of enq; backup to full enq on failure</span>
    Node pred = tail;
    <span class="hljs-keyword">if</span> (pred != <span class="hljs-keyword">null</span>) { <span class="hljs-comment">//如果尾节点不为空，代表等待队列中已经有线程节点在等待</span>
        node.prev = pred; <span class="hljs-comment">//将当前节点的前置节点指向尾节点</span>
        <span class="hljs-keyword">if</span> (compareAndSetTail(pred, node)) { <span class="hljs-comment">//cas设置尾节点为当前节点，将当前线程加入到队列末尾，避免多线程设置导致数据丢失</span>
            pred.next = node;
            <span class="hljs-keyword">return</span> node;
        }
    }
    enq(node); <span class="hljs-comment">//如果队列中无等待线程，或者设置尾节点不成功，则循环设置尾节点</span>
    <span class="hljs-keyword">return</span> node;
}

<span class="hljs-function"><span class="hljs-keyword">private</span> Node <span class="hljs-title">enq</span><span class="hljs-params">(<span class="hljs-keyword">final</span> Node node)</span> </span>{
    <span class="hljs-keyword">for</span> (;;) {
        Node t = tail;
        <span class="hljs-keyword">if</span> (t == <span class="hljs-keyword">null</span>) { <span class="hljs-comment">// Must initialize</span>
            <span class="hljs-keyword">if</span> (compareAndSetHead(<span class="hljs-keyword">new</span> Node())) <span class="hljs-comment">//空队列，初始化头尾节点都为一个空节点</span>
                tail = head;
        } <span class="hljs-keyword">else</span> {
            node.prev = t;
            <span class="hljs-keyword">if</span> (compareAndSetTail(t, node)) { <span class="hljs-comment">//重复addWaiter中的设置尾节点，也是cas的经典操作--自旋，避免使用Synchronized关键字导致的线程挂起</span>
                t.next = node;
                <span class="hljs-keyword">return</span> t;
            }
        }
    }
}

<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Node</span> </span>{
    <span class="hljs-comment">/** Marker to indicate a node is waiting in shared mode */</span>
    <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> Node SHARED = <span class="hljs-keyword">new</span> Node(); <span class="hljs-comment">//共享模式</span>
    <span class="hljs-comment">/** Marker to indicate a node is waiting in exclusive mode */</span>
    <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> Node EXCLUSIVE = <span class="hljs-keyword">null</span>;  <span class="hljs-comment">//独占模式</span>

    ......
}
<span class="copy-code-btn">复制代码</span></code></pre><p><code>addWaiter</code>方法首先初始化了一个EXCLUSIVE模式的Node节点。Node节点大家应该很熟悉，我写的集合系列文章里面介绍了很多链式结构都是通过这种方式来实现的。AQS中的Node也不例外，他的队列结构也是通过实现一个Node内部类来实现的，这里实现的是一个双向队列。Node节点分两种模式，一种SHARED共享锁模式，一种EXCLUSIVE独占锁模式，<code>ReentrantLock</code>使用的是EXCLUSIVE独占锁模式，所用用EXCLUSIVE来初始化。共享锁模式后面的文章我们再详细讲解。</p>
<p>初始化Node节点之后就是将节点加入到队列之中，这里有一点要注意的是多线程环境下，如果CAS设置尾节点不成功，需要自旋进行CAS操作来设置尾节点，这样即保证了线程安全，又保证了设置成功，这是一种乐观的锁模式，当然你可以通过synchronized关键字锁住这个方法，但这样效率就会下降，是一种悲观锁模式。</p>
<p>设置节点的过程我通过下面几张图来描述下，让大家有更形象的理解：
</p><figure><img class="lazyload inited" data-src="https://user-gold-cdn.xitu.io/2018/8/12/1652ca0437847fb0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="367" data-height="204" src="data:image/svg+xml;utf8,<?xml version=&quot;1.0&quot;?><svg xmlns=&quot;http://www.w3.org/2000/svg&quot; version=&quot;1.1&quot; width=&quot;367&quot; height=&quot;204&quot;></svg>"><figcaption></figcaption></figure><p></p>
<p></p><figure><img class="lazyload inited" data-src="https://user-gold-cdn.xitu.io/2018/8/12/1652ca04477b4258?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="586" data-height="214" src="data:image/svg+xml;utf8,<?xml version=&quot;1.0&quot;?><svg xmlns=&quot;http://www.w3.org/2000/svg&quot; version=&quot;1.1&quot; width=&quot;586&quot; height=&quot;214&quot;></svg>"><figcaption></figcaption></figure><p></p>
<p></p><figure><img class="lazyload inited" data-src="https://user-gold-cdn.xitu.io/2018/8/12/1652ca0438075c05?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="800" data-height="600" src="data:image/svg+xml;utf8,<?xml version=&quot;1.0&quot;?><svg xmlns=&quot;http://www.w3.org/2000/svg&quot; version=&quot;1.1&quot; width=&quot;800&quot; height=&quot;600&quot;></svg>"><figcaption></figcaption></figure><p></p>
<p>将当前线程加入等待队列之后，需要调用<code>acquireQueued</code>挂起当前线程：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">acquireQueued</span><span class="hljs-params">(<span class="hljs-keyword">final</span> Node node, <span class="hljs-keyword">int</span> arg)</span> </span>{
    <span class="hljs-keyword">boolean</span> failed = <span class="hljs-keyword">true</span>;
    <span class="hljs-keyword">try</span> {
        <span class="hljs-keyword">boolean</span> interrupted = <span class="hljs-keyword">false</span>;
        <span class="hljs-keyword">for</span> (;;) {
            <span class="hljs-keyword">final</span> Node p = node.predecessor(); <span class="hljs-comment">//获取当前节点的前置节点</span>
            <span class="hljs-keyword">if</span> (p == head &amp;&amp; tryAcquire(arg)) { <span class="hljs-comment">//如果前置节点是头节点，说明当前节点是第一个挂起的线程节点，再次cas尝试获取锁</span>
                setHead(node); <span class="hljs-comment">//获取锁成功设置当前节点为头节点，当前节点占有锁</span>
                p.next = <span class="hljs-keyword">null</span>; <span class="hljs-comment">// help GC</span>
                failed = <span class="hljs-keyword">false</span>;
                <span class="hljs-keyword">return</span> interrupted;
            }
            <span class="hljs-keyword">if</span> (shouldParkAfterFailedAcquire(p, node) &amp;&amp; <span class="hljs-comment">//非头节点或者获取锁失败，检查节点状态，查看是否需要挂起线程</span>
                parkAndCheckInterrupt())  <span class="hljs-comment">//挂起线程，当前线程阻塞在这里！</span>
                interrupted = <span class="hljs-keyword">true</span>;
        }
    } <span class="hljs-keyword">finally</span> {
        <span class="hljs-keyword">if</span> (failed)
            cancelAcquire(node);
    }
}
<span class="copy-code-btn">复制代码</span></code></pre><p>可以看到这个方法是一个自旋的过程，首先获取当前节点的前置节点，如果前置节点为头结点则再次尝试获取锁，失败则挂起阻塞，阻塞被取消后自旋这一过程。是否可以阻塞通过<code>shouldParkAfterFailedAcquire</code>方法来判断，阻塞通过<code>parkAndCheckInterrupt</code>方法来执行。</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-function"><span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">shouldParkAfterFailedAcquire</span><span class="hljs-params">(Node pred, Node node)</span> </span>{
    <span class="hljs-keyword">int</span> ws = pred.waitStatus;
    <span class="hljs-keyword">if</span> (ws == Node.SIGNAL) <span class="hljs-comment">//代表继任节点需要挂起</span>
        <span class="hljs-comment">/*
            * This node has already set status asking a release
            * to signal it, so it can safely park.
            */</span>
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
    <span class="hljs-keyword">if</span> (ws &gt; <span class="hljs-number">0</span>) { <span class="hljs-comment">//代表前置节点已经退出（超时或中断等情况） </span>
        <span class="hljs-comment">/*
            * Predecessor was cancelled. Skip over predecessors and
            * indicate retry.
            */</span>
        <span class="hljs-keyword">do</span> {
            node.prev = pred = pred.prev;
        } <span class="hljs-keyword">while</span> (pred.waitStatus &gt; <span class="hljs-number">0</span>); <span class="hljs-comment">//前置节点退出，循环设置到最近的一个未退出节点</span>
        pred.next = node;
    } <span class="hljs-keyword">else</span> { <span class="hljs-comment">//非可挂起状态或退出状态则尝试设置为Node.SIGNAL状态</span>
        <span class="hljs-comment">/*
            * waitStatus must be 0 or PROPAGATE.  Indicate that we
            * need a signal, but don't park yet.  Caller will need to
            * retry to make sure it cannot acquire before parking.
            */</span>
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
}

<span class="hljs-function"><span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">parkAndCheckInterrupt</span><span class="hljs-params">()</span> </span>{
    LockSupport.park(<span class="hljs-keyword">this</span>);<span class="hljs-comment">//挂起当前线程</span>
    <span class="hljs-keyword">return</span> Thread.interrupted();
}
<span class="copy-code-btn">复制代码</span></code></pre><p>只有当节点处于SIGNAL状态时才可以挂起线程，Node的waitStatus有4个状态分别是：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-comment">/** waitStatus value to indicate thread has cancelled */</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> CANCELLED =  <span class="hljs-number">1</span>;
<span class="hljs-comment">/** waitStatus value to indicate successor's thread needs unparking */</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> SIGNAL    = -<span class="hljs-number">1</span>;
<span class="hljs-comment">/** waitStatus value to indicate thread is waiting on condition */</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> CONDITION = -<span class="hljs-number">2</span>;
<span class="hljs-comment">/**
    * waitStatus value to indicate the next acquireShared should
    * unconditionally propagate
    */</span>
<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> PROPAGATE = -<span class="hljs-number">3</span>;
<span class="copy-code-btn">复制代码</span></code></pre><p>注释写的很清楚，这里就不详细解释着四种状态了。到这里整个Lock的过程我们就全部说完了，公平锁和非公平锁的区别从Lock的过程中我们也很容易发现，非公平锁一样要进行排队，只不过在排队之前会CAS尝试直接获取锁。说完了获取锁，下面来看下释放锁的过程。</p>
<h2 class="heading" data-id="heading-3">ReentrantLock的unLock()方法</h2>
<p><code>unLock()</code>方法比较好理解，因为他不需要考虑多线程的问题，如果<code>unLock()</code>的不是之前<code>lock</code>的线程，直接退出就可以了。看看<code>unLock()</code>的源码：</p>
<pre><code class="hljs java copyable" lang="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">ReentrantLock</span> <span class="hljs-keyword">implements</span> <span class="hljs-title">Lock</span>, <span class="hljs-title">java</span>.<span class="hljs-title">io</span>.<span class="hljs-title">Serializable</span> </span>{
    ......
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">unlock</span><span class="hljs-params">()</span> </span>{
        sync.release(<span class="hljs-number">1</span>);
    }
    ......
}

<span class="hljs-keyword">public</span> <span class="hljs-keyword">abstract</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">AbstractQueuedSynchronizer</span> </span>{
    ......
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">release</span><span class="hljs-params">(<span class="hljs-keyword">int</span> arg)</span> </span>{
        <span class="hljs-keyword">if</span> (tryRelease(arg)) { <span class="hljs-comment">//尝试释放锁</span>
            Node h = head;
            <span class="hljs-keyword">if</span> (h != <span class="hljs-keyword">null</span> &amp;&amp; h.waitStatus != <span class="hljs-number">0</span>)
                unparkSuccessor(h); <span class="hljs-comment">//释放锁成功后启动后继线程</span>
            <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
        }
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
    }
    ......
}

<span class="hljs-keyword">abstract</span> <span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Sync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractQueuedSynchronizer</span> </span>{
    ......
    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">tryRelease</span><span class="hljs-params">(<span class="hljs-keyword">int</span> releases)</span> </span>{
        <span class="hljs-keyword">int</span> c = getState() - releases;
        <span class="hljs-keyword">if</span> (Thread.currentThread() != getExclusiveOwnerThread()) <span class="hljs-comment">//释放锁必须要是获取锁的线程，否则退出，保证了这个方法只能单线程访问</span>
            <span class="hljs-keyword">throw</span> <span class="hljs-keyword">new</span> IllegalMonitorStateException();
        <span class="hljs-keyword">boolean</span> free = <span class="hljs-keyword">false</span>;
        <span class="hljs-keyword">if</span> (c == <span class="hljs-number">0</span>) { <span class="hljs-comment">//独占锁为0后代表锁释放，否则为重入锁，不释放</span>
            free = <span class="hljs-keyword">true</span>;
            setExclusiveOwnerThread(<span class="hljs-keyword">null</span>);
        }
        setState(c);
        <span class="hljs-keyword">return</span> free;
    }
    ......
}

<span class="hljs-keyword">abstract</span> <span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Sync</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">AbstractQueuedSynchronizer</span> </span>{
    ......
    <span class="hljs-function"><span class="hljs-keyword">private</span> <span class="hljs-keyword">void</span> <span class="hljs-title">unparkSuccessor</span><span class="hljs-params">(Node node)</span> </span>{
        <span class="hljs-comment">/*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */</span>
        <span class="hljs-keyword">int</span> ws = node.waitStatus;
        <span class="hljs-keyword">if</span> (ws &lt; <span class="hljs-number">0</span>)
            compareAndSetWaitStatus(node, ws, <span class="hljs-number">0</span>);

        <span class="hljs-comment">/*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */</span>
        Node s = node.next;
        <span class="hljs-keyword">if</span> (s == <span class="hljs-keyword">null</span> || s.waitStatus &gt; <span class="hljs-number">0</span>) {
            s = <span class="hljs-keyword">null</span>;
            <span class="hljs-keyword">for</span> (Node t = tail; t != <span class="hljs-keyword">null</span> &amp;&amp; t != node; t = t.prev)
                <span class="hljs-keyword">if</span> (t.waitStatus &lt;= <span class="hljs-number">0</span>)
                    s = t;
        }
        <span class="hljs-keyword">if</span> (s != <span class="hljs-keyword">null</span>)
            LockSupport.unpark(s.thread); <span class="hljs-comment">//挂起当前线程</span>
    }
    ......
}
<span class="copy-code-btn">复制代码</span></code></pre><p>同<code>lock()</code>方法一样，会调用AQS的<code>release</code>方法，首先调用<code>tryRelease</code>尝试释放，首先必须要是当前获取锁的线程，之后判断是否为重入锁，非重入锁则释放当前线程的锁。锁释放之后调用<code>unparkSuccessor</code>方法启动后继线程。</p>
<h2 class="heading" data-id="heading-4">总结</h2>
<p><code>ReentrantLock</code>的获取锁和释放锁到这里就讲完了，总的来说还是比较清晰的一个流程，通过AQS的state状态来控制锁获取和释放状态，AQS内部用一个双向链表来维护挂起的线程。在AQS和ReentrantLock之间通过状态和行为来分离，AQS用管理各种状态，并内部通过链表管理线程队列，ReentrantLock则对外提供锁获取和释放的功能，具体实现则在AQS中。下面我通过两张流程图总结了公平锁和非公平锁的流程。</p>
<p>非公平锁：
</p><figure><img class="lazyload inited" data-src="https://user-gold-cdn.xitu.io/2018/8/12/1652ca04475e83d2?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="991" data-height="1280" src="data:image/svg+xml;utf8,<?xml version=&quot;1.0&quot;?><svg xmlns=&quot;http://www.w3.org/2000/svg&quot; version=&quot;1.1&quot; width=&quot;991&quot; height=&quot;1280&quot;></svg>"><figcaption></figcaption></figure>
公平锁：
<figure><img class="lazyload inited" data-src="https://user-gold-cdn.xitu.io/2018/8/12/1652ca0416b6035e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="655" data-height="1280" src="data:image/svg+xml;utf8,<?xml version=&quot;1.0&quot;?><svg xmlns=&quot;http://www.w3.org/2000/svg&quot; version=&quot;1.1&quot; width=&quot;655&quot; height=&quot;1280&quot;></svg>"><figcaption></figcaption></figure><p></p>
<style>.markdown-body pre,.markdown-body pre>code.hljs{color:#333;background:#f8f8f8}.hljs-comment,.hljs-quote{color:#998;font-style:italic}.hljs-keyword,.hljs-selector-tag,.hljs-subst{color:#333;font-weight:700}.hljs-literal,.hljs-number,.hljs-tag .hljs-attr,.hljs-template-variable,.hljs-variable{color:teal}.hljs-doctag,.hljs-string{color:#d14}.hljs-section,.hljs-selector-id,.hljs-title{color:#900;font-weight:700}.hljs-subst{font-weight:400}.hljs-class .hljs-title,.hljs-type{color:#458;font-weight:700}.hljs-attribute,.hljs-name,.hljs-tag{color:navy;font-weight:400}.hljs-link,.hljs-regexp{color:#009926}.hljs-bullet,.hljs-symbol{color:#990073}.hljs-built_in,.hljs-builtin-name{color:#0086b3}.hljs-meta{color:#999;font-weight:700}.hljs-deletion{background:#fdd}.hljs-addition{background:#dfd}.hljs-emphasis{font-style:italic}.hljs-strong{font-weight:700}</style></div> <div class="image-viewer-box" data-v-78c9b824=""><!----></div></div></article>