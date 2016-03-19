package cpl.gangnam

import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.io.File

import cpl.gangnam.FileUtil.meanPSNR
import cpl.gangnam.FileUtil.multiPSNR
import cpl.gangnam.FileUtil.psnr
import cpl.gangnam.FileUtil.psnrThreashold
import cpl.gangnam.FileUtil.ssimThreashold
import cpl.gangnam.Metrics.abs
import ij.ImagePlus
import javax.imageio.ImageIO

object Image {

  type Pixel = (Int, Int, Int)
  

  def similarity(stack1: List[File], stack2: List[File]): (Int, Int) = {
    if (meanPSNR || multiPSNR) 
      MeanPSNR.similarity(stack1, stack2)
    else {
      val matches: Int = stack1.map(compareWith(_, stack2)).filter(b => b).length
      (percent(matches, stack1.length), percent(matches, stack2.length))
    }
  }

  def compareWith(file: File, stack: List[File]): Boolean = {
    print("\n\t")
    val fromImage = getImage(file)
    if (!equalSize(fromImage, getImage(stack.head))) {
      print("[x size]")
      false
    } else {
      val matchFound = stack.exists(f => compare(fromImage, getImage(f)))
      print("\t" + matchFound)
      matchFound
    }
  }
  
  def compare(orig: BufferedImage, edit: BufferedImage): Boolean =
    if (psnr) psnrCompare(orig, edit)
    else ssimCompare(getIJImage(orig), getIJImage(edit))

  def psnrCompare(orig: BufferedImage, edited: BufferedImage): Boolean = {
    val pixelPair = toPixels(orig.getData).zip(toPixels(edited.getData))
    val diff = for ((orig, edit) <- pixelPair) yield pixelDiff(orig, edit)
    psnrThreshold(diff)
  }
  
  def ssimCompare(orig: ImagePlus, edit: ImagePlus): Boolean = {
      val ssim = new MS_SSIM_Index(orig, edit).run
      printf("[%1.2f] ", ssim)
      ssim >= ssimThreashold
  }
  
  def getImage(file: File): BufferedImage = ImageIO.read(file)

  def getIJImage(bi: BufferedImage): ImagePlus = new ImagePlus("Image Name", bi)
    
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

  def psnrThreshold(diff: List[Int]): Boolean = {
    val mse: Double = diff.map(square(_)).sum / diff.length // mean squared error
    if (mse == 0) {
      printf("mse 0 ")
      true
    } else {
      val psnr: Double = 10 * (java.lang.Math.log10(65025 / mse)) // peak signal to noise ratio (65025 = 255 * 255)
      //val psnr: Double = 48.13 - (10 * Math.log(mse)) // peak signal to noise ratio (48.13 = 20 * log 255)
      printf("%.1f ", psnr)
      psnr >= psnrThreashold
    }
  }
  
  def square(x: Double): Double = x * x
}