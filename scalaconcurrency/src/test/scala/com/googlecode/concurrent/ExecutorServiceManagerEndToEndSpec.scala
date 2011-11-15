package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

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
}