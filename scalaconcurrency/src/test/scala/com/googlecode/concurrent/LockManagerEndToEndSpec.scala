package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import java.util.concurrent.Executors
import scala.concurrent.ops._
import java.util.concurrent.atomic.AtomicInteger
/**
 * @author kostantinos.kougios
 *
 * 9 Nov 2011
 */
class LockManagerEndToEndSpec extends SpecificationWithJUnit {
	"tryLockAndDo" in {
		val lock = LockManager.readWriteLock
		val readers = new AtomicInteger
		val writers = new AtomicInteger
		val rfs = for (i <- 0 to 4)
			yield future {
			for (i <- 0 to 1000) {
				Thread.sleep(1)
			}
		}
		true must beTrue
	}
}