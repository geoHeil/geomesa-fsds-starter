import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.fs.storage.common.PartitionScheme
import org.locationtech.geomesa.spark.GeoMesaSparkKryoRegistrator
import org.locationtech.geomesa.spark.jts._
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes

import scala.collection.JavaConversions._

object JobRead extends App {
  val spark = SparkSession
    .builder()
    .config(new SparkConf().setAppName("dummy")
      .setIfMissing("spark.master", "local[*]")
      .setIfMissing("spark.sql.orc.impl", "native")
      .setIfMissing("spark.sql.orc.enabled", "true")
      .setIfMissing("park.sql.orc.enableVectorizedReader", "true")
      .setIfMissing("spark.sql.hive.convertMetastoreOrc", "true")
      .setIfMissing("spark.sql.orc.filterPushdown", "true")
      .setIfMissing("spark.sql.orc.char.enabled", "true")
      .setIfMissing("spark.kryo.registrator", classOf[GeoMesaSparkKryoRegistrator].getName)
      .setIfMissing("spark.serializer", classOf[KryoSerializer].getCanonicalName)
      .setIfMissing("spark.kryo.unsafe", "true")
      .setIfMissing("spark.kryo.referenceTracking", "false")
    )
    .getOrCreate()

  spark.withJTS

  val sfName = "myDummyData"
  val fsdsPathDummy = "fooGeomesaFsds"

  val dataFrame = spark.read
    .format("geomesa")
    .option("fs.path",fsdsPathDummy)
    .option("geomesa.feature", sfName)
    .load()
  dataFrame.printSchema
  dataFrame.show


}
