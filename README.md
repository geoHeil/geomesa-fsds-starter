# geomesa FSDS spark

write a geomesa FSDS from spark

to execute run:
```
make run
```

it will fail with:
```
018-06-01 15:10:11 INFO  ENGINE:? - dataFileCache open start
Exception in thread "main" java.lang.NullPointerException
        at Job1$.delayedEndpoint$Job1$1(Job1.scala:34)
        at Job1$delayedInit$body.apply(Job1.scala:9)
        at scala.Function0$class.apply$mcV$sp(Function0.scala:34)
        at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
        at scala.App$$anonfun$main$1.apply(App.scala:76)
        at scala.App$$anonfun$main$1.apply(App.scala:76)
        at scala.collection.immutable.List.foreach(List.scala:381)
        at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:35)
        at scala.App$class.main(App.scala:76)
        at Job1$.main(Job1.scala:9)
        at Job1.main(Job1.scala)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
        at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:879)
        at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:197)
        at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:227)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:136)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)

```
NPE might be created at https://github.com/locationtech/geomesa/blob/master/geomesa-spark/geomesa-spark-sql/src/main/scala/org/locationtech/geomesa/spark/GeoMesaSparkSQL.scala#L177

As expected the fsds jars are contained within the jar:

```
jar -tf build/libs/geomesa-fsds-starter-all.jar| grep geomesa/fs
org/locationtech/geomesa/fs/
org/locationtech/geomesa/fs/FileSystemFeatureStore$$anonfun$1.class
org/locationtech/geomesa/fs/FileSystemDataStore.class
...
```

However,
```
ar -tf build/libs/geomesa-fsds-starter-all.jar| grep FileSystemRDDProvider
```
turns out to be empty
```
gradle dependencyInsight --dependency geomesa-fs-spark
```
after adding
```
geomesa-fs-spark
```
still an error
```
2018-06-01 15:36:07 INFO  ENGINE:? - dataFileCache open start
Exception in thread "main" java.lang.NullPointerException
        at Job1$.delayedEndpoint$Job1$1(Job1.scala:34)
        at Job1$delayedInit$body.apply(Job1.scala:9)
        at scala.Function0$class.apply$mcV$sp(Function0.scala:34)
        at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
        at scala.App$$anonfun$main$1.apply(App.scala:76)
        at scala.App$$anonfun$main$1.apply(App.scala:76)
        at scala.collection.immutable.List.foreach(List.scala:381)
        at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:35)
        at scala.App$class.main(App.scala:76)
        at Job1$.main(Job1.scala:9)
        at Job1.main(Job1.scala)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
        at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:879)
        at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:197)
        at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:227)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:136)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)

```