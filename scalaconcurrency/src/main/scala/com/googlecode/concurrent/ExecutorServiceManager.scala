package com.googlecode.concurrent

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors

/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2011
 */
object ExecutorServiceManager {

	def wrap(executor: ExecutorService) = new Executor {
		protected val executorService = executor
	}

	def newCachedThreadPool(
		corePoolSize: Int,
		maximumPoolSize: Int,
		keepAliveTimeInSeconds: Int = 60,
		workQueue: BlockingQueue[Runnable] = new SynchronousQueue) =
		new Executor {
			override protected val executorService = new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTimeInSeconds,
				TimeUnit.SECONDS,
				workQueue)
		}
	def newScheduledThreadPool(corePoolSize: Int) =
		new Executor with Scheduling {
			override protected val executorService = new ScheduledThreadPoolExecutor(corePoolSize)
		}

	def newFixedThreadPool(nThreads: Int) =
		new Executor {
			override protected val executorService = Executors.newFixedThreadPool(nThreads)
		}
}

abstract class Executor {
	// ideally the underlying executor should not be accessible
	protected val executorService: ExecutorService

	def submit[R](f: () => R) = executorService.submit(new Callable[R] {
		def call = f()
	})
	def shutdown = executorService.shutdown
	def shutdownNow = executorService.shutdownNow
	def awaitTermination(timeout: Long, unit: TimeUnit) = executorService.awaitTermination(timeout, unit)
	def shutdownAndAwaitTermination(waitTimeInSeconds: Int) {
		shutdown
		awaitTermination(waitTimeInSeconds, TimeUnit.SECONDS)
	}
}

trait Scheduling {
	protected val executorService: ScheduledExecutorService
	def schedule[R](delay: Long, unit: TimeUnit)(f: () => R) = executorService.schedule(new Callable[R] {
		def call = f()
	}, delay, unit)
}