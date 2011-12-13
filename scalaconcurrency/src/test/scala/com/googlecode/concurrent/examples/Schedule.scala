package com.googlecode.concurrent.examples
import com.googlecode.concurrent.ExecutorServiceManager

import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 12 Dec 2011
 */
object Schedule extends App {

	val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

	val start = System.currentTimeMillis
	executorService.runPeriodically(DateTime.now + 50.millis, Some(DateTime.now + 1.second)) {
		// should print dt 6 times, once per second
		println("dt:%d".format(System.currentTimeMillis - start))
	}

	Thread.sleep(5500)
	executorService.shutdownAndAwaitTermination(DateTime.now + 100.millis)
}