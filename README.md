## Fingerprinting duration for cached tasks/goals
Fingerprinting tool uses Gradle Enterprise API generating a report of the builds with task/goals `FROM-CACHE` with
fingerprinting duration higher than the value configured:

```
./fingerprinting --api-key=$KEY --url=$URL --duration=10000 --max-builds=20000 --project=nowinandroid
```

#### Usage

##### Install the binary:
```
./gradlew install
```

##### Execute fingerprinting
```
cd build/install/fingerprinting/bin
./fingerprinting --api-key=$KEY --url=$URK --duration=10000 --max-builds=20000 --project=nowinandroid

```

##### Output
###### Report
Available at the end of the execution
```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                                                                  │
│ 9 Builds found with tasks/goals cached with FingerPrinting Time  > 10s                                                                           │
│                                                                                                                                                  │
├──────────────────────────────────────────────────────┬──────────────┬─────────────────────┬──────────────────────────────────────────────────────┤
│                                                      │              │                     │                                                      │
│ Build Scan                                           │ Project      │ Date                │ Total Fingerprinting duration FROM-CACHE tasks/goals │
│                                                      │              │                     │                                                      │
├──────────────────────────────────────────────────────┼──────────────┼─────────────────────┼──────────────────────────────────────────────────────┤
│                                                      │              │                     │                                                      │
│ https://gradle-enterprise-url.com/s/3asym2ar5gg5a    │ nowinandroid │ 22/02/2023 19:45:44 │                                           1m 55.386s │
│                                                      │              │                     │                                                      │
├──────────────────────────────────────────────────────┼──────────────┼─────────────────────┼──────────────────────────────────────────────────────┤
│                                                      │              │                     │                                                      │
│ https://gradle-enterprise-url.com/s/3asym2ar5gg5a    │ nowinandroid │ 22/02/2023 19:45:45 │                                            2m 5.159s │
│                                                      │              │                     │                                                      │
├──────────────────────────────────────────────────────┼──────────────┼─────────────────────┼──────────────────────────────────────────────────────┤

```

###### Extended Report
Each execution with results generates a detailed report `extended_report.txt`:
```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                                                                              │
│ 9 builds found with tasks/goals cached with FingerPrinting Time  > 10s                                                                                       │
│                                                                                                                                                              │
├─────────────────────────────────────────────────────────┬────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                         │                                                                                                    │
│ Build                                                   │ https://gradle-enterprise-url.com/s/3asym2ar5gg5a                                                  │
│                                                         │                                                                                                    │
├─────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                         │                                                                                                    │
│ Project                                                 │ nowinandroid                                                                                       │
│                                                         │                                                                                                    │
├─────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                         │                                                                                                    │
│ Date                                                    │ 22/02/2023 19:45:44                                                                                │
│                                                         │                                                                                                    │
├─────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                         │                                                                                                    │
│ Task/Goal                                               │ test                                                                                               │
│                                                         │                                                                                                    │
├─────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                         │                                                                                                    │
│ Tags                                                    │ CI,HEAD,Linux,WORKERS8-temurin                                                                     │
│                                                         │                                                                                                    │
├─────────────────────────────────────────────────────────┴────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                                                                              │
│ Tasks types                                                                                                                                                  │
│                                                                                                                                                              │
├────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┬─────────┤
│                                                                                                                                                    │         │
│ :app:hiltAggregateDepsDemoDebug                                                                                                                    │ 17.513s │
│ https://gradle-enterprise-url.com/s/3asym2ar5gg5a/timeline?outcome=from-cache&task-path=:app:hiltAggregateDepsDemoDebug                            │         │
│                                                                                                                                                    │         │
├────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┼─────────┤

```

#### Parameters

| Name                   | Description                                  | Default | Required | Example                     |
|------------------------|----------------------------------------------|---------|----------|-----------------------------|
| api-key                | String token                                 |         | Yes      | --api-key=$TOKEN_FILE       |
| url                    | Gradle Enterprise instance                   |         | Yes      | --url=https://ge.acme.dev   |
| max-builds             | Max builds to be processed                   | 1000    | No       | --max-builds=2000           |
| project                | Root project in Gradle Enterprise            |         | No       | --project=acme              |
| duration               | Min Task/goal duration. (>= 100000 - 10secs) |         | Yes      | --duration=20000            |
| concurrent-calls       | Experiment identifier to process Build scans | 150     | No       | --concurrent-calls=200      |
| concurrent-calls-cache | Max allowed concurrent request               | 10      | No       | --concurrent-calls-cache=20 |


#### Notes
It's recommended to specify the project name to limit the cache performance requests. The number of requests per execution is:
```
Requests = ((Max builds / 1000) + Build attributes requests + Cache performance requests)
```
Filtering by project we reduce the required requests for cache performance.
Stress tests showed acceptable time results with `max-builds` < 30000 when filtering by project. For executions without
project parameter specified we recommend `max-builds` < 10000.

#### Compatibility
We have tested the tool with Java 8, 11 and 17.

