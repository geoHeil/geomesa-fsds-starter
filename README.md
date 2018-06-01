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
jar -tf build/libs/geomesa-fsds-starter-all.jar| grep FileSystemRDDProvider
```
turns out to be empty
```
gradle dependencyInsight --dependency geomesa-fs-spark
```
after adding
```
geomesa-fs-spark
```
still the same error

after switching to geomesa-fs-spark-runtime this problem is gone.


However, there are problems with ORC:
```
2018-06-01 16:06:58 WARN  ConverterStorageFactory:37 - Couldn't create converter storage: java.lang.IllegalArgumentException: Must provide either simple feature type config or name
java.lang.IllegalArgumentException: Must provide either simple feature type config or name
        at org.locationtech.geomesa.fs.storage.converter.ConverterStorageFactory$$anonfun
java.lang.NoSuchMethodError: org.apache.orc.TypeDescription.createRowBatch()Lorg/apache/hadoop/hive/ql/exec/vector/VectorizedRowBatch
```

```
gradle dependencyInsight --dependency org.apache.orc                                                                                                                                                                                                [±master ●●]
 
 > Task :dependencyInsight
 org.apache.orc:orc-core:1.4.1
    variant "runtime" [
       Requested attributes not found in the selected variant:
          org.gradle.usage = java-api
    ]
 +--- org.apache.orc:orc-mapreduce:1.4.1
 |    +--- org.apache.spark:spark-sql_2.11:2.2.0.2.6.4.9-3
 |    |    \--- compileClasspath
 |    \--- org.locationtech.geomesa:geomesa-fs-storage-orc_2.11:2.0.1
 |         \--- org.locationtech.geomesa:geomesa-fs-spark_2.11:2.0.1
 |              \--- org.locationtech.geomesa:geomesa-fs-spark-runtime_2.11:2.0.1
 |                   \--- compileClasspath
 +--- org.apache.spark:spark-sql_2.11:2.2.0.2.6.4.9-3 (*)
 \--- org.locationtech.geomesa:geomesa-fs-storage-orc_2.11:2.0.1 (*)
 
 org.apache.orc:orc-mapreduce:1.4.1
    variant "runtime" [
       Requested attributes not found in the selected variant:
          org.gradle.usage = java-api
    ]
 +--- org.apache.spark:spark-sql_2.11:2.2.0.2.6.4.9-3
 |    \--- compileClasspath
 \--- org.locationtech.geomesa:geomesa-fs-storage-orc_2.11:2.0.1
      \--- org.locationtech.geomesa:geomesa-fs-spark_2.11:2.0.1
           \--- org.locationtech.geomesa:geomesa-fs-spark-runtime_2.11:2.0.1
                \--- compileClasspath
 
 (*) - dependencies omitted (listed previously)
 
 A web-based, searchable dependency report is available by adding the --scan option.
 
 BUILD SUCCESSFUL in 0s
 1 actionable task: 1 executed
                                                                                                                                                                                                                                                                      
 geoheil@geoheils-MacBook ~/Downloads/geomesa-fsds-starter                                                                                                                                                                                                 [16:00:32] 
 > $                                                                                        
```


> you can ignore those warnings, they're coming from the converter storage class
  which you're not using
  
Unfortunately, something seems to be wrong:
```
ResultStage 1 (foreachPartition at FileSystemRDDProvider.scala:84) failed in 1.859 s due to Job aborted due to stage failure:
```
the spark job writing the data is cancelled
```
+-------+---+----+
|__fid__|dtg|geom|
+-------+---+----+
+-------+---+----+

```
and also reading the data being written (some files are actually being created) does not show any records