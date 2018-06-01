import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.fs.storage.common.PartitionScheme
import org.locationtech.geomesa.spark.jts._
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes
import collection.JavaConversions._

object Job1 extends App {
  val spark = SparkSession
    .builder()
    .config(new SparkConf().setAppName("dummy")
      .setIfMissing("spark.master", "local[*]"))
    .enableHiveSupport()
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

  val datastore = DataStoreFinder.getDataStore(Map("fs.path" -> fsdsPathDummy, "fs.encoding" -> "orc"))
  datastore.createSchema(sft)

  dummyGeo.write
    .format("geomesa")
    .option("fs.path", fsdsPathDummy)
    .option("fs.encoding", "orc")
    .option("geomesa.feature", sfName)
    .save

}
