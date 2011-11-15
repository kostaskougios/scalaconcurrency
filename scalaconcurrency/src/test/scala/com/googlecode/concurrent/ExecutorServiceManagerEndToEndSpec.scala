package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.util.concurrent.TimeUnit

/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2011
 */
@RunWith(classOf[JUnitRunner])
class ExecutorServiceManagerEndToEndSpec extends SpecificationWithJUnit {

	"callable is executed" in {
		val executorService = ExecutorServiceManager.cached(5, 5)

		val future = executorService.submit { () =>
			25
		}
		future.get must_== 25
	}

	"scheduled callable is executed" in {
		val executorService = ExecutorServiceManager.scheduled(5)

		val start = System.currentTimeMillis
		val future = executorService.schedule(500, TimeUnit.MILLISECONDS) { () =>
			25
		}
		future.get must_== 25
		(System.currentTimeMillis - start) must be_>(300.toLong)
	}
}