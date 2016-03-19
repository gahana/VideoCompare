package cpl.gangnam

import java.awt.image.BufferedImage
import java.io.File

import com.xuggle.mediatool.MediaListenerAdapter
import com.xuggle.mediatool.ToolFactory
import com.xuggle.mediatool.event.IVideoPictureEvent
import com.xuggle.xuggler.Global
import cpl.gangnam.FileUtil.frameInterval

import javax.imageio.ImageIO

class Video(val id: String) {
  
  def createFrameStack(fileName: String): Unit = {
    println("Creating frame stack for " + fileName)
    val mediaReader = ToolFactory.makeReader(fileName)
    mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
    mediaReader.addListener(ImageSnapListener)
    while (mediaReader.readPacket() == null) {}
  }

  object ImageSnapListener extends MediaListenerAdapter {

    val SECONDS_BETWEEN_FRAMES: Double = frameInterval
    val MICRO_SECONDS_BETWEEN_FRAMES: Long = (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES).toLong

    var mVideoStreamIndex: Int = -1
    var mLastPtsWrite = Global.NO_PTS

    override def onVideoPicture(event: IVideoPictureEvent): Unit = {
      if (event.getStreamIndex() != mVideoStreamIndex) {
        if (mVideoStreamIndex == -1) mVideoStreamIndex = event.getStreamIndex()
        else return
      }
      if (mLastPtsWrite == Global.NO_PTS) {
        mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES
      }
      if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {
        dumpImageToFile(event.getImage())
        mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES
      }
    }

    def dumpImageToFile(image: BufferedImage): Unit = {
      val outputFilename = FileUtil.makeTempVideoDir(id) + "img" + System.currentTimeMillis() + ".jpeg"
      ImageIO.write(toGrayscale(image), "jpeg", new File(outputFilename))
      //ImageIO.write(image, "jpeg", new File(outputFilename))
    }
    
    def toGrayscale(colorImage: BufferedImage): BufferedImage = {
      val grayscaleImage = new BufferedImage(colorImage.getWidth, colorImage.getHeight, BufferedImage.TYPE_BYTE_GRAY)
      val graphics = grayscaleImage.getGraphics
      graphics.drawImage(colorImage, 0, 0, null)
      graphics.dispose
      grayscaleImage
    }

  }

}

