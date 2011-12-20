package com.googlecode.concurrent
import java.util.concurrent.Executors
import scala.concurrent.ops._
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ThreadPoolRunner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 9 Nov 2011
 */
class LockManagerEndToEndSuite extends FunSuite with ShouldMatchers {
	test("tryLockAndDo") {
		val pool = new ThreadPoolRunner {
			val executor = Executors.newFixedThreadPool(10)
			override def shutdown = {
				executor.shutdown
				executor.awaitTermination(1000, TimeUnit.SECONDS)
			}
		}
		val lock = LockManager.readWriteLock
		val readers = new AtomicInteger
		val writers = new AtomicInteger
		val errors = new AtomicInteger
		val rfs = for (i <- 0 to 4) yield {
			import pool._
			pool.execute { () =>
				for (i <- 0 to 1000) {
					lock.readLockAndDo {
						if (writers.get > 0) errors.incrementAndGet
						readers.incrementAndGet
						Thread.sleep(1)
						if (writers.get > 0) errors.incrementAndGet
						readers.decrementAndGet
					}
					Thread.sleep(1)
				}
				for (i <- 0 to 1000) {
					lock.writeLockAndDo {
						if (readers.get > 0) errors.incrementAndGet
						writers.incrementAndGet
						Thread.sleep(1)
						if (readers.get > 0) errors.incrementAndGet
						writers.decrementAndGet
					}
					Thread.sleep(1)
				}
			}
		}
		pool.shutdown()
		readers.get should be === 0
		writers.get should be === 0
		errors.get should be === 0
	}
}