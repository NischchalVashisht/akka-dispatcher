package com.knoldus

import java.io.File

trait FileBasicOperation {
    def getListOfFile(dir: String): List[File] = {
      val file = new File(dir)
      if (file.exists && file.isDirectory) {
        file.listFiles.toList
      } else {
        List[File]()
      }
    }
  def traverseFile(listOfFile: List[File],resultMap:Map[String,Int]): Map[String,Int]
  }


