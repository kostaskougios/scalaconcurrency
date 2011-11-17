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
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.CompletionService
import java.util.concurrent.Future

/**
 * manages executor instantiation, provides factory methods
 * for various executors
 *
 * @author kostantinos.kougios
 *
 * 15 Nov 2011
 */
object ExecutorServiceManager {

	def wrap(executor: ExecutorService) = new Executor with Shutdown {
		protected val executorService = executor
	}

	def newCachedThreadPool(
		corePoolSize: Int,
		maximumPoolSize: Int,
		keepAliveTimeInSeconds: Int = 60,
		workQueue: BlockingQueue[Runnable] = new SynchronousQueue) =
		new Executor with Shutdown {
			override protected val executorService = new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTimeInSeconds,
				TimeUnit.SECONDS,
				workQueue)
		}
	def newScheduledThreadPool(corePoolSize: Int) =
		new Executor with Shutdown with Scheduling {
			override protected val executorService = new ScheduledThreadPoolExecutor(corePoolSize)
		}

	def newFixedThreadPool(nThreads: Int) =
		new Executor with Shutdown {
			override protected val executorService = Executors.newFixedThreadPool(nThreads)
		}
}

abstract class Executor {
	// ideally the underlying executor should not be accessible
	protected val executorService: ExecutorService

	def submit[R](f: () => R) = executorService.submit(new Callable[R] {
		def call = f()
	})
	def submit[V](task: Callable[V]) = executorService.submit(task)
	def submit(task: Runnable) = executorService.submit(task)
}

trait Scheduling {
	protected val executorService: ScheduledExecutorService
	def schedule[R](delay: Long, unit: TimeUnit)(f: () => R) = executorService.schedule(new Callable[R] {
		def call = f()
	}, delay, unit)
}

trait Shutdown {
	protected val executorService: ExecutorService
	def shutdown = executorService.shutdown
	def shutdownNow = executorService.shutdownNow
	def awaitTermination(timeout: Long, unit: TimeUnit) = executorService.awaitTermination(timeout, unit)
	def shutdownAndAwaitTermination(waitTimeInSeconds: Int) {
		shutdown
		awaitTermination(waitTimeInSeconds, TimeUnit.SECONDS)
	}
}
abstract class CompletionExecutor[V] {
	protected val executorService: ExecutorService
	private val completionService = new ExecutorCompletionService[V](executorService)
	def submit(f: () => V) = completionService.submit(new Callable[V] {
		def call = f()
	})
	def submit(task: Callable[V]) = completionService.submit(task)
	def submit(task: Runnable, result: V) = completionService.submit(task, result)

	def take: Option[Future[V]] = {
		val t = completionService.take
		if (t == null) None else Some(t)
	}

	def poll: Option[Future[V]] = {
		val t = completionService.poll
		if (t == null) None else Some(t)
	}

	def poll(timeout: Long, unit: TimeUnit): Option[Future[V]] = {
		val t = completionService.poll(timeout, unit)
		if (t == null) None else Some(t)
	}
}
