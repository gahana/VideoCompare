import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.io.File

import FileUtil._
import Metrics.abs
import javax.imageio.ImageIO

object MeanPSNR {

  type Pixel = (Int, Int, Int)

  def similarity(stack1: List[File], stack2: List[File]): (Int, Int) = {
    val psnrs: List[List[Double]] = stack1.map(compareWith(_, stack2))
    psnrs.foreach(_.foreach(printf("%.1f ", _)))
    val matches: Int = if (multiPSNR) applyMultiVarThreshold(psnrs) else applyThreshold(psnrs)
    (percent(matches, stack1.length), percent(matches, stack2.length))
  }

  def compareWith(file: File, stack: List[File]): List[Double] = {
    val fromImage = getImage(file)
    if (!equalSize(fromImage, getImage(stack.head))) List(0)
    else stack.map(f => compare(fromImage, getImage(f)))
  }
  
  def compare(orig: BufferedImage, edited: BufferedImage): Double = {
    val pixelPair = toPixels(orig.getData).zip(toPixels(edited.getData))
    val diff = for ((orig, edit) <- pixelPair) yield pixelDiff(orig, edit)
    psnr(diff)
  }
  
  def getImage(file: File): BufferedImage = ImageIO.read(file)

  def percent(part: Int, whole: Int): Int = (100 * part) / whole

  def equalSize(orig: BufferedImage, edit: BufferedImage): Boolean =
    (orig.getWidth == edit.getWidth) && (orig.getHeight == edit.getHeight)

  def toPixels(data: Raster): List[(Int, Int, Int)] =
    (for {
      y <- data.getMinY until data.getHeight
      x <- data.getMinX until data.getWidth
    } yield pixelTuple(data.getPixel(x, y, new Array[Int](3)))).toList

  def pixelTuple(arr: Array[Int]): Pixel = arr.toList match {
    case r :: g :: b :: _ => (r, g, b)
    case _ => (-1, -1, -1) // error case, not sure what to return
  }

  def pixelDiff(a: Pixel, b: Pixel): Int = {
    abs(a._1 - b._1) + abs(a._2 - b._2) + abs(a._3 - b._3)
    //abs(a._1 - b._1) * 3
  }

  def psnr(diff: List[Int]): Double = {
    val mse: Double = diff.map(square(_)).sum / diff.length // mean squared error
    if (mse == 0) 50
    else 10 * (java.lang.Math.log10(65025 / mse)) // peak signal to noise ratio (65025 = 255 * 255)
  }
  
  def square(x: Double): Double = x * x
  
  def applyThreshold(psnrs: List[List[Double]]): Int = {
    val flat = psnrs.flatten
    val mean = if (flat.length == 0) 0 else flat.sum / flat.length
    val threshold = theta0 + (theta1 * mean)
    val matches = psnrs.filter(_.exists(_ >= threshold)).length
    printf("[PSNR > %.2f = %d @ %.2f fps]", threshold, matches, (1.0 / frameInterval))
    matches
  }
  
  def applyMultiVarThreshold(psnrs: List[List[Double]]): Int = {
    val flat = psnrs.flatten
    val frameMean = if (psnrs.length == 0) 0 else (psnrs.map(_.max).sum / psnrs.length)
    val mean = if (flat.length == 0) 0 else flat.sum / flat.length
    val variance = flat.map(r => square(r - mean)).sum / (flat.length - 1)		// Square of variance
    
    val threshold = calcThreshold(frameMean, variance)
    
    val matches = psnrs.filter(_.exists(_ >= threshold)).length
    printf("[PSNR > %.2f = %d]", threshold, matches)
    matches
  }
  
  def calcThreshold(x: Double, y: Double): Double = 
    p00 + p10*x + p01*y + p20*square(x) + p11*x*y + p02*square(y) 
  
}