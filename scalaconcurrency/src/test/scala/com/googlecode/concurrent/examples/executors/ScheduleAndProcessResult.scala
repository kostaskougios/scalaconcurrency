package com.googlecode.concurrent.examples.executors

import com.googlecode.concurrent.ExecutorServiceManager

import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object ScheduleAndProcessResult extends App {

	val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

	val start = System.currentTimeMillis
	executorService.runPeriodically(DateTime.now + 50.millis, { l: Long =>
		println("processing the result, i.e. storing %d into db ...".format(l))
		Some(DateTime.now + 1.second)
	}) {
		// should print dt 6 times, once per second
		val dt = System.currentTimeMillis - start
		println("dt:%d".format(dt))
		dt
	}

	Thread.sleep(5500)
	executorService.shutdownAndAwaitTermination(DateTime.now + 100.millis)

}