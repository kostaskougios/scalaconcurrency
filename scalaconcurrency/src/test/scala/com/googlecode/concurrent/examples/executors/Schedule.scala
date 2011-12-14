package com.googlecode.concurrent.examples.executors

import com.googlecode.concurrent.ExecutorServiceManager

import org.scala_tools.time.Imports._

/**
 * runs a closure periodically, once per second.
 *
 * @author kostantinos.kougios
 *
 * 12 Dec 2011
 */
object Schedule extends App {

	val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

	val start = System.currentTimeMillis
	// please note that the 2nd parameter is by-value. This means that it is re-calculated
	// every time the closure completes it's execution. So it always points to 1 second
	// in the future. In fact it is 1 second AFTER the task finishes it's execution.
	executorService.runPeriodically(DateTime.now + 50.millis, Some(DateTime.now + 1.second)) {
		// should print dt 6 times, once per second
		println("dt:%d".format(System.currentTimeMillis - start))
	}

	Thread.sleep(5500)
	// shuting down the executor means that no further tasks will be scheduled,
	// but running tasks will complete. The executor will wait 100 milliseconds
	// from now for currently running tasks to finish. If not, those tasks will
	// keep on running but the executor will be shut.
	executorService.shutdownAndAwaitTermination(DateTime.now + 100.millis)
}