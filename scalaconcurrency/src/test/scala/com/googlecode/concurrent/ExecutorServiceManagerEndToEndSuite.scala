package com.googlecode.concurrent
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ConcurrentHashMap
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scala_tools.time.Imports._
/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2011
 */
@RunWith(classOf[JUnitRunner])
class ExecutorServiceManagerEndToEndSuite extends FunSuite with ShouldMatchers {
	test("lifecycle with params") {
		val threads = new ConcurrentHashMap[Thread, Thread]
		val results = ExecutorServiceManager.lifecycle(5, List(5, 10, 15, 20, 25, 30, 35, 40)) { param =>
			val ct = Thread.currentThread
			threads.put(ct, ct)
			100 + param
		}
		results.toList should be === List(105, 110, 115, 120, 125, 130, 135, 140)
		threads.size should be === 5
	}

	test("lifecycle") {
		val threads = new ConcurrentHashMap[Thread, Thread]
		val results = ExecutorServiceManager.lifecycle(5, 20) { i =>
			val ct = Thread.currentThread
			threads.put(ct, ct)
			100 + i
		}
		results.toSet should be === (101 to 120).toSet
		threads.size should be === 5
	}

	test("cached pool, f is executed") {
		val executorService = ExecutorServiceManager.newCachedThreadPool(5, 5)

		try {
			val future = executorService.submit {
				25
			}
			future.get should be === 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached pool, limits are valid") {
		val executorService = ExecutorServiceManager.newCachedThreadPool(5, 10)

		val start = System.currentTimeMillis
		try {
			evaluating {
				(for (i <- 0 to 15) yield executorService.submit {
					Thread.sleep(100)
				}).map(_.get)
			} should produce[RejectedExecutionException]
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("fixed pool, f is executed") {
		val executorService = ExecutorServiceManager.newFixedThreadPool(5)

		try {
			val future = executorService.submit {
				25
			}
			future.get should be === 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("scheduled pool, run periodically(simple)") {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			val start = System.currentTimeMillis
			@volatile var counter = 0
			@volatile var fcounter = 0

			def processor = {
				counter += 1
				if (counter < 3) Some(aHundredMs) else None
			}

			executorService.runPeriodically(aHundredMs, processor) {
				fcounter += 1
				25
			}

			Thread.sleep(500)
			counter should be === 3
			fcounter should be === 3
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("scheduled pool, run periodically") {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			val start = System.currentTimeMillis
			@volatile var counter = 0
			val processor = { result: Option[Int] =>
				assert(result.get == 25 + counter) // on a different thread, matchers don't work here
				counter += 1
				if (counter == 1) Some(aSec) else None
			}
			executorService.runPeriodically(halfSec, processor) {
				25 + counter
			}
			Thread.sleep(1700)
			counter should be === 2
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("scheduled pool, f is executed") {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			val start = System.currentTimeMillis
			val future = executorService.schedule(500, TimeUnit.MILLISECONDS) {
				25
			}
			future.get should be === 25
			(System.currentTimeMillis - start) should be > (300.toLong)
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("scheduled pool, with datetime") {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			val start = System.currentTimeMillis
			val future = executorService.schedule(aSec) {
				25
			}
			future.get should be === 25
			(System.currentTimeMillis - start) should be > (900.toLong)

			val start2 = System.currentTimeMillis
			val future2 = executorService.schedule(halfSec) {
				26
			}
			future2.get should be === 26
			(System.currentTimeMillis - start2) should be > (450.toLong)

		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("scheduled pool, with datetime in the past") {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			evaluating {
				executorService.schedule(pastTime) {
					25
				}
			} should produce[IllegalArgumentException]

		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached completion service, f is executed") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				25
			}
			executorService.take.get should be === 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached completion service, timed poll positive") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.pollWaitInMillis(800).get.get should be === 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached completion service, timed poll negative") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.pollWaitInMillis(200) should be(None)
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached completion service, timed poll with DateTime positive") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.poll(DateTime.now + 800.millis).get.get should be === 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	test("cached completion service, timed poll with dateTime negative") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.poll(DateTime.now + 200.millis) should be(None)
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}
	test("cached completion service, 2 tasks positive") {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit { 25 }
			executorService.submit { Thread.sleep(100); 26 }

			executorService.take.get should be === 25
			executorService.take.get should be === 26
			executorService.poll should be(None)
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	def aSec = DateTime.now + 1.second
	def halfSec = DateTime.now + 500.millis
	def pastTime = DateTime.now - 1.second
	def aHundredMs = DateTime.now + 100.millis
}
