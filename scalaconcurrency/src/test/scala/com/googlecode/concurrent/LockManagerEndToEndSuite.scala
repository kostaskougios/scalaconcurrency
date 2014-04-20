package com.googlecode.concurrent

import scala.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         9 Nov 2011
 */
class LockManagerEndToEndSuite extends FunSuite with ShouldMatchers
{
	test("tryLockAndDo") {
		val ec = ExecutionContext.Implicits.global
		val lock = LockManager.readWriteLock
		val readers = new AtomicInteger
		val writers = new AtomicInteger
		val errors = new AtomicInteger

		for (i <- 0 to 4) yield {
			ec.execute(new Runnable
			{
				override def run() = {
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
			})
		}
		readers.get should be === 0
		writers.get should be === 0
		errors.get should be === 0
	}
}