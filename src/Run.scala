import java.io.File
import DataBase.update
import Metrics.results
import scala.compat.Platform

object Run {
  
  case class StackPair(stack1: (Int, List[File]), stack2: (Int, List[File]))

  def init(): Unit = {
    FileUtil.clearTempDir
    FileUtil.createTempDir
    DataBase.truncate
    DataBase.saveVideos
  }

  def cleanup(): Unit = {
    FileUtil.clearTempDir
  }

  def main(args: Array[String]): Unit = {
    val start = Platform.currentTime
    init
    dumpFrames
    loop
    val end = Platform.currentTime
    printf("Total time taken: %f secs", (end - start)/1000.0)
  }

  def dumpFrames(): Unit = {
    FileUtil.videoInfo.foreach(p => new Video(p._1).createFrameStack(p._2))
  }

  def loop(): Unit = {
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 0) || (p._1 == 1) || (p._1 == 2) || (p._1 == 3))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 4) || (p._1 == 5) || (p._1 == 6))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 7) || (p._1 == 8) || (p._1 == 9) || (p._1 == 10) || (p._1 == 11))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 12) || (p._1 == 13) || (p._1 == 14) || (p._1 == 15))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 16) || (p._1 == 17) || (p._1 == 18))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 19) || (p._1 == 20) || (p._1 == 21))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 0) || (p._1 == 4) || (p._1 == 7) || (p._1 == 12) || (p._1 == 16) || (p._1 == 19))

    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 19) || (p._1 == 20))
    //val stacks = FileUtil.getFrameStack.filter(p => (p._1 == 12) || (p._1 == 13))

    val stacks = FileUtil.getFrameStack
    for {
      i <- 0 until stacks.length
      j <- i + 1 until stacks.length
    } process(stacks(i), stacks(j)) // runner ! StackPair(stacks(i), stacks(j)) 
  }
  
  def process(t1: (Int, List[File]), t2: (Int, List[File])): Unit = {
      printf("Comparing %2d with %2d ... ", t1._1, t2._1)
      val percents = Image.similarity(t1._2, t2._2)
      printf("\n\t[%2d -> %2d = %3d%%][%2d -> %2d = %3d%%]\n", t1._1, t2._1, percents._1, t2._1, t1._1, percents._2)
      update(t1._1, t2._1, percents._1)
      update(t2._1, t1._1, percents._2)
  }

}
