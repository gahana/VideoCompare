object Metrics {

  def main(args: Array[String]): Unit = {
    results
  }
  
  def checkResults: List[(Int, Int, Int, Int, Int, Int)] = {
    val refResults = DataBase.getResults(false)
    val ourResults = DataBase.getResults(true)
    ( for {
      (key, r1) <- refResults
    } yield metrics(key._1, key._2, r1, ourResults.getOrElse(key, 0)) ).toList
  }
  
  def metrics(from: Int, to: Int, exp: Int, obt: Int): (Int, Int, Int, Int, Int, Int) = 
    (from, to , exp, obt, diff(exp, obt), error(exp, obt))
  
  def diff(expected: Int, obtained: Int): Int = 
    abs(expected - obtained)
  
  def error(expected: Int, obtained: Int): Int = 
    if (0 == expected) 100 * obtained
    else 100 * abs(expected - obtained) / expected
  
  def abs(a: Int): Int = if (a < 0) -a else a
  
  def printResults(tup: (Int, Int, Int, Int, Int, Int)): Unit = {
    printf("[%2d to %2d] => [%3d - %3d] = [%3d, %3d%%]\n", tup._1, tup._2, tup._3, tup._4, tup._5, tup._6)
  }
  
  def results(): Unit = {
    val result = checkResults
    result.foreach(printResults)
    printf("Average Error: %3d%%\n", (result.map(_._5).sum / result.length))
  }

}