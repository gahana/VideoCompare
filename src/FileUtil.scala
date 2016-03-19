import java.io.File
import java.io.FileInputStream
import java.util.PropertyResourceBundle

import scala.Array.canBuildFrom

object FileUtil {

  val bundle = new PropertyResourceBundle(new FileInputStream(new File("./compare.config")))

  def getProperty(key: String): String = bundle.getString(key)
  
  def frameInterval(): Int = getProperty("frame.interval").toInt

  def psnrThreashold(): Double = getProperty("threshold.psnr").toDouble
  
  def ssimThreashold(): Double = getProperty("threshold.ssim").toDouble
  
  def meanPSNRThreashold(): Double = getProperty("threashold.mean-psnr").toDouble
  
  def theta0(): Double = getProperty("threshold.psnr.theta0").toDouble
  
  def theta1(): Double = getProperty("threshold.psnr.theta1").toDouble

  def tempDir(): String = getProperty("temp.dir")
  
  def videoDir(): String = getProperty("video.dir")
  
  def videoDownload(): Boolean = getProperty("video.download") == "yes"
  
  def psnr: Boolean = getProperty("compare.algo") == "psnr"
    
  def ssim: Boolean = getProperty("compare.algo") == "ssim"
    
  def meanPSNR: Boolean = getProperty("compare.algo") == "mean-psnr"
    
  def multiPSNR: Boolean = getProperty("compare.algo") == "multi-psnr"
    
  def p00: Double = getProperty("p00").toDouble
  def p10: Double = getProperty("p10").toDouble
  def p01: Double = getProperty("p01").toDouble
  def p20: Double = getProperty("p20").toDouble
  def p11: Double = getProperty("p11").toDouble
  def p02: Double = getProperty("p02").toDouble

  def createTempDir(): Unit =
    new File(tempDir).mkdir

  def clearTempDir: Unit = {
    def deleteDir(file: File): Unit = {
      if (file.isDirectory()) {
        file.listFiles.foreach(deleteDir)
        file.delete
      } else {
        file.delete
      }
    }
    deleteDir(new File(tempDir))
  }
  
  def makeTempVideoDir(vid: String): String = {
    val dirName = tempDir+ vid + "/"
    val dir = new File(dirName)
    if (! dir.exists()) dir.mkdir
    dirName
  }
  
  def tempVideoDir(vid: String): File = new File(makeTempVideoDir(vid))
  
  def videoInfo: List[(String, String)] = {
    val dir = new File(FileUtil.videoDir)
    dir.listFiles.toList.map(videoInfoPair(_))
  }
  
  def videoInfoPair(file: File): (String, String) = {
    val name: String = file.getName()
    (name.split("\\.").head, FileUtil.videoDir + name)
  }
  
  def getFrameStack: List[(Int, List[File])] = {
    val ids: List[String] = new File(tempDir).listFiles.filter(_.isDirectory).map(_.getName).toList
    val stacks: List[List[File]] = ids.map(id => tempVideoDir(id)).map(_.listFiles.toList)
    ids.zip(stacks).map(p => (p._1.toInt, p._2))
  }
  
  def main(args: Array[String]): Unit = {
    println("Coeff:" + p00 + ":" +  p10 + ":" +  p01 + ":" +  p20 + ":" +  p11 + ":" +  p02)
  }
  
}