import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.fs.storage.common.PartitionScheme
import org.locationtech.geomesa.spark.GeoMesaSparkKryoRegistrator
import org.locationtech.geomesa.spark.jts._
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes

import collection.JavaConversions._

object Job1 extends App {
  val format = "orc"
//  val format = "parquet"
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
  val dummyGeo = spark.sql("SELECT '2018-01-01' dtg, st_makePoint(1,2) geom")
  dummyGeo.show

  val attributes = new StringBuilder
  attributes.append("dtg:Date,")
  attributes.append("*geom:Point:srid=4326")
  val sfName = "myDummyData"
  val sft = SimpleFeatureTypes.createType(sfName, attributes.toString)
  sft.getUserData.put(SimpleFeatureTypes.DEFAULT_DATE_KEY, "dtg")
  val fsdsPathDummy = "fooGeomesaFsds"


  val scheme = PartitionScheme(sft, "daily,z2-2bit", Map[String, String]())
  val schemeNew = PartitionScheme.addToSft(sft, scheme)

  val datastore = DataStoreFinder.getDataStore(Map("fs.path" -> fsdsPathDummy, "fs.encoding" -> format))
  datastore.createSchema(sft)

  dummyGeo.write
    .format("geomesa")
    .option("fs.path", fsdsPathDummy)
    .option("fs.encoding", format)
    .option("geomesa.feature", sfName)
    .save


  val dataFrame = spark.read
    .format("geomesa")
    .option("fs.path",fsdsPathDummy)
    .option("geomesa.feature", sfName)
    .load()
  dataFrame.printSchema
  dataFrame.show


}
