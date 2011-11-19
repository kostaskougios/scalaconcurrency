package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.util.concurrent.TimeUnit
import java.util.concurrent.RejectedExecutionException

/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2011
 */
@RunWith(classOf[JUnitRunner])
class ExecutorServiceManagerEndToEndSpec extends SpecificationWithJUnit {

	"cached pool, f is executed" in {
		val executorService = ExecutorServiceManager.newCachedThreadPool(5, 5)

		try {
			val future = executorService.submit {
				25
			}
			future.get must_== 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"cached pool, limits are valid" in {
		val executorService = ExecutorServiceManager.newCachedThreadPool(5, 10)

		val start = System.currentTimeMillis
		try {
			(for (i <- 0 to 15) yield executorService.submit {
				Thread.sleep(100)
			}).map(_.get) must throwA[RejectedExecutionException]
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"fixed pool, f is executed" in {
		val executorService = ExecutorServiceManager.newFixedThreadPool(5)

		try {
			val future = executorService.submit {
				25
			}
			future.get must_== 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"scheduled pool, f is executed" in {
		val executorService = ExecutorServiceManager.newScheduledThreadPool(5)

		try {
			val start = System.currentTimeMillis
			val future = executorService.schedule(500, TimeUnit.MILLISECONDS) {
				25
			}
			future.get must_== 25
			(System.currentTimeMillis - start) must be_>(300.toLong)
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"cached completion service, f is executed" in {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				25
			}
			executorService.take.get must_== 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"cached completion service, timed poll positive" in {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.pollWaitInMillis(800).get.get must_== 25
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}

	"cached completion service, timed poll negative" in {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit {
				Thread.sleep(500)
				25
			}
			executorService.pollWaitInMillis(200) must beNone
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}
	"cached completion service, 2 tasks positive" in {
		val executorService = ExecutorServiceManager.newCachedThreadPoolCompletionService[Int](5, 10)

		try {
			executorService.submit { 25 }
			executorService.submit { Thread.sleep(100); 26 }

			executorService.take.get must_== 25
			executorService.take.get must_== 26
			executorService.poll must beNone
		} finally {
			executorService.shutdownAndAwaitTermination(1)
		}
	}
}