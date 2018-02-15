# Этап 3

Нагрузочное тестирование с помощью wrk.
Профилирование с помощью jvisualvm.

Всё плохо, необходимо оптимизировать

### PUT без перезаписи с replicas=2/3: 2 потока, 4 соединения
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.86ms    0.95ms  67.25ms   79.45%
    Req/Sec    45.69      6.57    60.00     90.54%
  Latency Distribution
     50%   42.87ms
     75%   43.12ms
     90%   43.90ms
     99%   45.90ms
  10962 requests in 2.00m, 0.91MB read
Requests/sec:     91.27
Transfer/sec:      7.76KB
```
### PUT без перезаписи с replicas=2/3: 4 потока, 4 соединения
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    51.06ms   56.65ms   1.04s    97.41%
    Req/Sec    22.46      4.77    30.00     72.58%
  Latency Distribution
     50%   43.06ms
     75%   43.90ms
     90%   44.84ms
     99%  331.27ms
  10540 requests in 2.00m, 0.87MB read
Requests/sec:     87.82
Transfer/sec:      7.46KB
```
### PUT без перезаписи с replicas=3/3: 2 потока, 4 соединения
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    43.21ms    8.59ms 319.48ms   99.53%
    Req/Sec    45.68      6.77    60.00     89.86%
  Latency Distribution
     50%   42.83ms
     75%   43.13ms
     90%   43.95ms
     99%   47.78ms
  10953 requests in 2.00m, 0.91MB read
Requests/sec:     91.20
Transfer/sec:      7.75KB
```
### PUT без перезаписи с replicas=3/3:  4 потока, 4 соединения
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.87ms    1.22ms  55.21ms   86.72%
    Req/Sec    22.84      4.51    30.00     71.60%
  Latency Distribution
     50%   42.88ms
     75%   43.18ms
     90%   43.91ms
     99%   45.73ms
  10963 requests in 2.00m, 0.91MB read
Requests/sec:     91.28
Transfer/sec:      7.76KB
```
### PUT с перезаписью с replicas=2/3  2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-rep-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    43.04ms    1.22ms  57.52ms   89.62%
    Req/Sec    45.40      6.85    60.00     88.95%
  Latency Distribution
     50%   42.96ms
     75%   43.50ms
     90%   43.95ms
     99%   45.69ms
  10894 requests in 2.00m, 0.90MB read
Requests/sec:     90.71
Transfer/sec:      7.71KB
```
### PUT с перезаписью с replicas=2/3  4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-rep-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.97ms    1.18ms  55.97ms   88.82%
    Req/Sec    22.74      4.47    30.00     72.47%
  Latency Distribution
     50%   42.94ms
     75%   43.35ms
     90%   43.94ms
     99%   45.65ms
  10917 requests in 2.00m, 0.91MB read
Requests/sec:     90.90
Transfer/sec:      7.73KB

```
### PUT с перезаписью с replicas=3/3 2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-rep-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    43.07ms    1.18ms  57.46ms   91.55%
    Req/Sec    45.35      6.94    60.00     88.37%
  Latency Distribution
     50%   42.98ms
     75%   43.51ms
     90%   43.97ms
     99%   45.58ms
  10876 requests in 2.00m, 0.90MB read
Requests/sec:     90.63
Transfer/sec:      7.70KB
```
### PUT с перезаписью с replicas=3/3 4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/put-rep-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    43.24ms    1.19ms  56.84ms   89.04%
    Req/Sec    22.56      4.37    30.00     74.36%
  Latency Distribution
     50%   43.02ms
     75%   43.75ms
     90%   44.02ms
     99%   45.78ms
  10826 requests in 2.00m, 0.90MB read
Requests/sec:     90.14
Transfer/sec:      7.66KB

```

### GET без перезаписи с replicas=2/3 2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    55.10ms   20.27ms 173.86ms   77.74%
    Req/Sec    36.60     11.17    60.00     64.87%
  Latency Distribution
     50%   43.98ms
     75%   53.93ms
     90%   85.08ms
     99%  126.99ms
  8781 requests in 2.00m, 9.23MB read
Requests/sec:     73.13
Transfer/sec:     78.70KB
```
### GET без перезаписи с replicas=2/3 4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    53.75ms   19.47ms 169.03ms   79.35%
    Req/Sec    18.84      6.28    30.00     59.53%
  Latency Distribution
     50%   43.94ms
     75%   50.83ms
     90%   84.85ms
     99%  126.95ms
  9002 requests in 2.00m, 9.46MB read
Requests/sec:     74.97
Transfer/sec:     80.68KB
```
### GET без перезаписи с replicas=3/3 2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    46.35ms    9.78ms 135.97ms   93.67%
    Req/Sec    43.21      8.93    60.00     79.38%
  Latency Distribution
     50%   43.94ms
     75%   44.90ms
     90%   47.95ms
     99%   87.95ms
  10372 requests in 2.00m, 10.90MB read
Requests/sec:     86.37
Transfer/sec:     92.95KB
```
### GET без перезаписи с replicas=3/3 4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    55.21ms   19.87ms 170.01ms   77.28%
    Req/Sec    18.30      6.16    30.00     59.41%
  Latency Distribution
     50%   43.98ms
     75%   53.91ms
     90%   85.09ms
     99%  127.00ms
  8746 requests in 2.00m, 9.19MB read
Requests/sec:     72.83
Transfer/sec:     78.38KB
```
### GET с перезаписью с replicas=2/3 2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-rep-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    54.52ms   19.79ms 169.01ms   78.44%
    Req/Sec    36.99     11.17    60.00     64.52%
  Latency Distribution
     50%   43.96ms
     75%   51.89ms
     90%   84.99ms
     99%  127.01ms
  8871 requests in 2.00m, 9.32MB read
Requests/sec:     73.87
Transfer/sec:     79.50KB
```
### GET с перезаписью с replicas=2/3 4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-rep-23.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    54.34ms   19.35ms 169.08ms   78.65%
    Req/Sec    18.59      6.15    30.00     60.36%
  Latency Distribution
     50%   43.97ms
     75%   51.82ms
     90%   85.00ms
     99%  126.97ms
  8889 requests in 2.00m, 9.34MB read
Requests/sec:     74.03
Transfer/sec:     79.67KB
```
### GET с перезаписью с replicas=3/3 2 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t2 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-rep-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    55.41ms   20.18ms 170.96ms   76.87%
    Req/Sec    36.38     10.68    60.00     67.04%
  Latency Distribution
     50%   43.98ms
     75%   63.89ms
     90%   85.86ms
     99%  127.03ms
  8719 requests in 2.00m, 9.16MB read
Requests/sec:     72.62
Transfer/sec:     78.15KB
```
### GET с перезаписью с replicas=3/3 4 потока, 4 соединения:
```
[mikhail@localhost wrk]$ ./wrk --latency -c4 -t4 -d2m -s /home/mikhail/Documents/Soft/wrk/scripts/get-rep-33.lua http://localhost:8080
Running 2m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    54.51ms   19.61ms 169.15ms   77.92%
    Req/Sec    18.53      6.26    30.00     58.90%
  Latency Distribution
     50%   43.96ms
     75%   51.96ms
     90%   85.00ms
     99%  126.04ms
  8859 requests in 2.00m, 9.31MB read
Requests/sec:     73.78
Transfer/sec:     79.40KB
```
