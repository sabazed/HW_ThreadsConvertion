# Kanji Conversion with Threads

<h5 id="summary">Summary</h5>

<p>We want to create a flashcard pack to practice number translation into Japanese. The Japanese number system is different from the western
number system in some respects. That is, not only Arabic numerals, but kanji symbols are also used to represent numbers, furthermore, numbers
are grouped by  10,000. For example, we can express 130,500 as 13 (⼗三) × 10,000 (万) + 5 (五) × 100 (百) = ⼗三万五百.</p>
<p>We need to create a small program that will generate random numbers (in the [0; 99,999,999] interval) and their kanji representations. For example:</p>
<pre><code class="hljs"><span class="hljs-number">68702</span><span class="hljs-string">,</span> <span class="hljs-string">六万⼋千七百⼆</span>
<span class="hljs-number">43959</span><span class="hljs-string">,</span> <span class="hljs-string">四万三千九百五⼗九</span>
<span class="hljs-number">21149</span><span class="hljs-string">,</span> <span class="hljs-string">⼆万千百四⼗九</span>
<span class="hljs-number">71456</span><span class="hljs-string">,</span> <span class="hljs-string">七万千四百五⼗六</span>
<span class="hljs-number">65838</span><span class="hljs-string">,</span> <span class="hljs-string">六万五千⼋百三⼗⼋</span>
<span class="hljs-number">1700</span><span class="hljs-string">,</span> <span class="hljs-string">千七百</span>
<span class="hljs-number">657</span><span class="hljs-string">,</span> <span class="hljs-string">六百五⼗七</span>
</code></pre>
<h5 id="rules">Rules</h5>
<ul>
<li>Your code should compile and run. If the code does not compile, you get 0 points directly, no matter how close you are to the solution. Fix or comment out all compilation errors before submitting the code!</li>
<li>If your solution does not use threads to parallelize number generation as described bellow in each task, you will get 0 points.</li>
<li>If your program does not terminate and runs infinitly, you will get 0 points.</li>
<li>You have to solve this problem using 3 different ways. Each way is slightly better and more complex than previous one, so follow tasks in order.</li>
<li>Each solution should be written in appropriate file. For example:<ul>
<li>If you write 1st task's solution in <code>Task2</code> file and 2nd task's solution in <code>Task1</code>, you will get 0 points for both of them.</li>
<li>If you solve 1st task and copy the solution in other 2 files too, you will get points only for the 1st task.</li></ul></li>
<li>Points will be deducted if your solution does not follow the task requirments or if you have race-conditions/deadlocks in your code.</li>
</ul>
<h5 id="kanjilib">KanjiLib</h5>
<ul>
<li>To convert integers to kanji number's string representation, you can use <code>KanjiLib.convert</code> method.</li>
<li>You should write test code into the <code>TaskN.java</code> files. You should only test your solution for numbers in the [0; 99,999,999] interval, as <code>KanjiLib.convert</code> cannot handle values outside that range.</li>
<li>Each solution shall be in the <code>TaskN.generate</code> method, which generates count pieces of random numbers that are in the from..to interval.</li>
<li>This method returns a list of strings, that has format <code>&lt;number&gt;, &lt;kanji number&gt;</code> .<ul>
<li>The length of this list must be <code>count</code> , except for task 3, which implements an interrupt mechanism.</li></ul></li>
<li>Parameters to this method may not be checked, as they are assumed to be correct.</li>
<li><strong>Method signatures must not be modified.</strong></li>
<li>Your solution must avoid <strong>race-conditions</strong> and <strong>deadlocks</strong>.</li>
<li>You should not inherit from <code>java.lang.Thread</code> in your solution.</li>
</ul>
<h5 id="task1task1java">Task 1 : <code>Task1.java</code></h5>
<p>The solution for this task shall be in the method called <code>Task1.generate</code>. You shall use a shared list for collecting the generated <code>Strings</code> (already
declared in the file). Your task is to start 10 threads, each of which has the following behaviour:</p>
<ul>
<li>In a loop:<ul>
<li>Generate a random number in the [ <code>from</code> , <code>to</code> ] closed interval. <code>Math.random</code> or other <strong>thread-safe</strong> random number generator can be used.</li>
<li>Create the Japanese kanji version by calling <code>KanjiLib.convert</code>.</li>
<li>Create the string <code>&lt;number&gt;, &lt;kanji number&gt;</code> (e.g.: <code>123, 百⼆⼗三</code> ), which will be added to the shared list.</li>
<li>Without interference (as an atomic operation):<ul>
<li>Check whether the list has enough items (<code>count</code>), and if so, immediately stop.</li>
<li>Check whether the generated <code>String</code> is already in the list, and if not, place it into the end of the list.</li>
<li>Make this operation atomic to avoid race conditions. You can use synchronization or locks.</li>
<li>Make sure to avoid deadlocks.</li></ul></li></ul></li>
</ul>
<p>Once <strong>all</strong> 10 threads finished their execution the method shall return the list of generated strings.</p>
<h5 id="task2task2java">Task 2: <code>Task2.java</code></h5>
<p>The solution for this task shall be in the method called <code>Task2.generate</code>. In this task, a random number generation and kanji conversion will be
separated into two distinct steps. One dedicated thread will generate exactly <code>count</code> pieces of unique random number and send these numbers to
the other threads that do only the conversion part. As a result, conversion threads need not check preconditions anymore. They can focus only on their
designated task (kanji conversion).
The <code>generate</code> method shall work as follows:</p>
<ul>
<li>Start a thread (we shall call it A) that will:<ul>
<li>Generate <code>count</code> random numbers in a loop:<ul>
<li>If the number is already present, generate a new one.</li>
<li>If the number is unique, <strong>send it</strong> to the other threads (see below).</li>
<li>Ignore interruptions.</li></ul></li>
<li>When the loop ends, make sure that all the other threads will eventually stop (see below).</li></ul></li>
<li>Start another 10 thread (referred to as <strong>B</strong> threads), each of which will start a loop and:<ul>
<li>Receive a random number from thread <strong>A</strong>.</li>
<li>Convert this number into the usual <code>String</code> and add this string to a shared data structure (shared among <strong>B</strong> threads). Since <strong>A</strong> already ensured uniqueness and will only send up to <code>count</code> numbers, there is <strong>no need for further verifications</strong>.</li>
<li>Ignore interruptions.</li>
<li>However, when <strong>A</strong> signals the end of work, break out of the loop, and stop (there is no other exit criterium).</li></ul></li>
<li>For the communication of threads (<strong>A</strong> and <strong>B</strong>), you shall create a <strong>thread-safe</strong> container, where <strong>A</strong> thread can put generated numbers and <strong>A</strong> thread can read, remove &amp; convert numbers.</li>
<li>The communication container should have maximum size  of 100 (use constant <code>CHANNEL_CAPACITY</code>).
If container has already reached the maximum capacity <strong>A</strong> thread should wait, until an element is
removed from the container to put a new one inside. If container is empty, <strong>B</strong> thread should wait for
an element to be added and try reading a new number only after that.</li>
<li>You should use wait/notify calls to make sure that there are no race-conditions when using communication container.</li>
<li>When all threads have stopped, return the generated list of <code>String</code>.</li>
<li>The end of communication (or ending of the work) is signaled via a designated special value ( <em>poison pill</em> ) that thread <strong>A</strong> sends to each thread <strong>B</strong> (one to each of them, a total of 10). You can use the <code>POISON_PILL</code> constant. In the end, <strong>A</strong> simply should send 10 <code>POISEN_PILL</code>s instead of numbers. If <strong>B</strong> reads a <code>POISEN_PILL</code> instead of a new number it should stop working.</li>
</ul>
<h5 id="task3task3java">Task 3: <code>Task3.java</code></h5>
<p>Your solution is expected to be in class <code>Task3</code>.</p>
<p>This time, instead of using a single static method, you shall create a class that is capable of not only random number generation but also gracefully
stopping in the middle of work. The skeleton of this class is available in <code>Task3.java</code> and your task is to complete it. Additional private helper
methods can be defined, if necessary (although it is not needed), however, removing methods or changing signatures is <strong>not allowed</strong>. For this task,
you will need to declare private data member references to shared data structures (instead of local references).</p>
<h4 id="parta">Part (a)</h4>
<p>The constructor of class <code>Task3</code> works similarly to <code>generate</code> in <strong>Task 2</strong>. That is, it starts 10 + 1 threads which cooperate in generating numbers
(strings) via the same pattern. Therefore, it is advised to start by copy-pasting your previous solution.</p>
<p>Then, we have the following changes:</p>
<ul>
<li>The constructor only starts the threads, it does not wait for them to finish.</li>
<li>Parts of the code shall be placed into private methods. For this, fill the skeleton methods provided in <code>Task3.java</code>.</li>
<li><strong>Waiting</strong> for threads to stop and returning the result list shall be implemented in method <code>Task3.get</code>. In this method, <code>InterruptedException</code> should be propagated instead of caught.</li>
</ul>
<h4 id="partb">Part (b)</h4>
<p>Implement the <code>Task3.interrupt</code> method which can stop the <strong>whole</strong> process of number generation before it finishes.</p>
<ul>
<li>For this, you have to modify the previous solution so when threads are interrupted, they eventually *<em>stop</em>.</li>
<li>For this sub-task, it is enough to make thread <strong>A</strong> interruptible and ensure that <strong>A</strong> signals end of work to all <strong>B</strong> threads as usual.</li>
</ul>
<h4 id="partc">Part (c)</h4>
<p>The <code>Task3.getThreads</code> method shall return the list of <strong>B</strong> threads. As these threads are public now, they can be interrupted by someone else. When any of these threads is interrupted, the whole number generation process should stop (as soon as possible). For this we need to make sure that, if thread <strong>B</strong> is interrupted
it will stop all other <strong>B</strong> and <strong>A</strong> threads as well.</p>
<ul>
<li>You can use <strong>Thead.interrupted()</strong> method to check, if thread has been interrupted. <strong>Thread.interrupt</strong>
method can be called on thread objects to change their status to <em>interrupted</em>. Refer to Java documentation
for more information.</li>
<li>When interrupted thread <strong>B</strong> can stop other <strong>B</strong> threads and <strong>A</strong> thread or it can notify <strong>A</strong> and <strong>A</strong> thread can send <code>POISEN_PILL</code> to others and stop everything. You are free to come up with other ideas as well, as long as everything stops at the end and there are no race-conditions or deadlocks.</li>
</ul></div>
</div><!---->
<!---->
</jhi-programming-exercise-instructions><!--->
