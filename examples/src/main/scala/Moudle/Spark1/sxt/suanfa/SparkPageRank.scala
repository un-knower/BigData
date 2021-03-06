/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Moudle.Spark1.sxt.suanfa

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Computes the PageRank of URLs from an input file. Input file should
 * be in format of:
 * URL         neighbor URL
 * URL         neighbor URL
 * URL         neighbor URL
 * ...
 * where URL and their neighbors are separated by space(s).
 */
object SparkPageRank {
  def main(args: Array[String]) {
    //    if (args.length < 1) {
    //      System.err.println("Usage: SparkPageRank <file> <iter>")
    //      System.exit(1)
    //    }
    val sparkConf = new SparkConf().setAppName("PageRank").setMaster("local[2]")
    val iters = 20
    //    val iters = if (args.length > 0) args(1).toInt else 10
    val ctx = new SparkContext(sparkConf)
    val lines = ctx.textFile("sort.txt",1)
//    ctx.setCheckpointDir(".")

    //根据边关系数据生成 邻接表 如：(1,(2,3,4,5)) (2,(1,5))..
    val links = lines.map{ s => val parts = s.split("\\s+")
      (parts(0), parts(1))}.distinct().groupByKey().cache()

    //打印邻接表
    val result1 = links.foreach(word => println(word))

    // (1,1.0) (2,1.0)..
    var ranks = links.mapValues(v => 1.0)

    //打印重新赋值value
    val result2 = ranks.foreach(word => println(word))



    for (i <- 1 to iters) {
      // (1,((2,3,4,5), 1.0))
      val contribs = links.join(ranks).values.flatMap{ case (urls, rank) =>
        val size = urls.size
        urls.map(url => (url, rank / size))
      }
      ranks = contribs.reduceByKey(_ + _).mapValues(0.15 + 0.85 * _)
      //ranks.checkpoint()
    }
    //结果打印
    //ranks.foreach(tup => println(tup._1 + " has rank: " + tup._2 + "."))
    
    ctx.stop()
  }
}
