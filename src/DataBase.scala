import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.sql.Connection
import java.sql.DriverManager

import FileUtil._

object DataBase {
  Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()
  
  def getConnection: Connection = {
    val conn = DriverManager.getConnection("jdbc:oracle:thin:@10.140.0.106:1521:CPL2011", "team27", "teamkci27")
    conn.setAutoCommit(true)
    conn
  }
  
  def getVideos: List[Int] = {
    var ids = List[Int]()
    val conn = getConnection
    val statement = conn.createStatement
    try {
      val rs = statement.executeQuery("select item_id from cpldata.item_info")
      while (rs.next()) {
        ids = rs.getInt("item_id") :: ids
      }
      ids
    } finally {
      statement.close
      conn.close
    }
  }
  
  def saveVideos(): Unit = {
    if (videoDownload) getVideos.foreach(saveVideo)
  }
  
  def saveVideo(id: Int): Unit = {
    val conn = getConnection
    val statement = conn.createStatement
    try {
      val rs = statement.executeQuery("select item_info from cpldata.item_info where item_id=" + id)
      rs.next
      val is = rs.getBlob("item_info").getBinaryStream()
      val bos = new BufferedOutputStream(new FileOutputStream(videoDir + id +".mp4"))
      var hasMore: Boolean = true
      while(hasMore) {
        val byte: Int = is.read
        if (byte != -1) bos.write(byte)
        else hasMore = false
      }
      bos.flush
      bos.close
      is.close
    } finally {
      statement.close
      conn.close
    }
  }
  
  def update(id: Int, compid: Int, pc: Int): Unit = {
    if (pc == 0) return
    val conn = getConnection
    val statement = conn.createStatement
    try {
      statement.executeUpdate("insert into item_compare_result values(" + id + ", " + compid + ", " + pc + ")")
    } finally {
      statement.close
      conn.close
    }
  }

  def truncate: Unit = {
    val conn = getConnection
    val statement = conn.createStatement
    try {
      statement.executeUpdate("truncate table item_compare_result")
    } finally {
      statement.close
      conn.close
    }
  }
  
  def getResults(team: Boolean): Map[(Int, Int), Int] = {
    var result = Map[(Int, Int), Int]()
    val conn = getConnection
    val statement = conn.createStatement
    try {
      val table = if (team) "item_compare_result" else "cpldata.item_compare_lookup"
      val rs = statement.executeQuery("select item_id, item_compare_id, percentage from " + table)
      while (rs.next()) {
        result += (rs.getInt("item_id"), rs.getInt("item_compare_id")) -> rs.getInt("percentage")
      }
      result
    } finally {
      statement.close
      conn.close
    }
  }
  
  def main(args: Array[String]): Unit = {
    //printlnResults()
    //getVideo(1)
    //saveVideos
  }

}