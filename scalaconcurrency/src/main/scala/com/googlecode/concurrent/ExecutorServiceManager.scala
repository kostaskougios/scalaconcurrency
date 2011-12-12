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
import java.util.concurrent.ScheduledFuture

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
	def newCachedThreadPoolCompletionService[V](
		corePoolSize: Int,
		maximumPoolSize: Int,
		keepAliveTimeInSeconds: Int = 60,
		workQueue: BlockingQueue[Runnable] = new SynchronousQueue) =
		new CompletionExecutor[V](
			new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTimeInSeconds,
				TimeUnit.SECONDS,
				workQueue)
		)

	def newScheduledThreadPool(corePoolSize: Int) =
		new Executor with Shutdown with Scheduling {
			override protected val executorService = new ScheduledThreadPoolExecutor(corePoolSize)
		}

	def newFixedThreadPool(nThreads: Int) =
		new Executor with Shutdown {
			override protected val executorService = Executors.newFixedThreadPool(nThreads)
		}

	def newFixedThreadPoolCompletionService[V](nThreads: Int) =
		new CompletionExecutor[V](Executors.newFixedThreadPool(nThreads))

	/**
	 * creates an executor of nThread, submits f() x times and returns V x times
	 * as returned by f(). It then shutsdown the executor.
	 *
	 * f: Int => V , where Int is the i-th execution, i is between [1..times]
	 * inclusive.
	 *
	 * If any of the invocation of f() fails, the executor will be shut down
	 * and no further threads will be submitted to it. The exception will propagate
	 * to the caller.
	 */
	def lifecycle[V](nThreads: Int, times: Int)(f: Int => V): Seq[V] = {
		val pool = newFixedThreadPool(nThreads)
		try {
			val seq = for (i <- 1 to times) yield pool.submit(f(i))
			seq.map(_.get)
		} finally {
			pool.shutdown
		}
	}

	/**
	 * creates an executor of nThread, submits f() x params.size and returns V x params.size
	 * as returned by f(). It then shutsdown the executor.
	 *
	 * f: T => V , each thread getting a different parameter from the traversable
	 *
	 * If any of the invocation of f() fails, the executor will be shut down
	 * and no further threads will be submitted to it. The exception will propagate
	 * to the caller.
	 */
	def lifecycle[T, V](nThreads: Int, params: Traversable[T])(f: T => V): Traversable[V] = {
		val pool = newFixedThreadPool(nThreads)
		try {
			val results = params.map(param => pool.submit(f(param)))
			results.map(_.get)
		} finally {
			pool.shutdown
		}
	}
}

/**
 * wrapper for the ExecutorService
 *
 * new Executor with Shutdown
 */
abstract class Executor {
	// ideally the underlying executor should not be accessible
	protected val executorService: ExecutorService

	def submit[R](f: => R) = executorService.submit(new Callable[R] {
		def call = f
	})
	def submit[V](task: Callable[V]) = executorService.submit(task)
	def submit(task: Runnable) = executorService.submit(task)
}

/*
 * new Executor with Scheduling with Shutdown
 */
trait Scheduling {
	protected val executorService: ScheduledExecutorService

	/**
	 * val future=schedule(100,TimeUnit.MILLISECONDS) {
	 * 	// to do in 100 millis from now
	 * }
	 */
	def schedule[R](delay: Long, unit: TimeUnit)(f: => R): ScheduledFuture[R] = executorService.schedule(new Callable[R] {
		def call = f
	}, delay, unit)

	import org.scala_tools.time.Imports._

	/**
	 * usage: val future=schedule(DateTime.now + 2.days) {
	 * 	// to do in 2 days from now
	 * }
	 */
	def schedule[R](nextRun: DateTime): (=> R) => ScheduledFuture[R] = {
		val dt = nextRun.millis - System.currentTimeMillis
		if (dt < 0) throw new IllegalArgumentException("next run time is in the past : %s".format(nextRun))
		schedule(dt, TimeUnit.MILLISECONDS) _
	}

	def runPeriodically[R](firstRun: DateTime, process: R => Option[DateTime])(f: => R): Unit =
		{
			schedule(firstRun) {
				val r = f
				process(r) match {
					case Some(nextRun) => runPeriodically(nextRun, process)(f)
					case None =>
				}
			}
		}
}

/**
 * provides shutdown services to Executor
 */
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

/*
 * @see CompletionService
 */
class CompletionExecutor[V](protected val executorService: ExecutorService) extends Shutdown {
	private val completionService = new ExecutorCompletionService[V](executorService)
	def submit(f: => V): Future[V] = completionService.submit(new Callable[V] {
		def call = f
	})
	def submit(task: Callable[V]) = completionService.submit(task)
	def submit(task: Runnable, result: V) = completionService.submit(task, result)

	/**
	 * Retrieves and removes the Future representing the next
	 * completed task, waiting if none are yet present.
	 *
	 * @return the Future representing the next completed task
	 * @throws InterruptedException if interrupted while waiting
	 */
	def take: Future[V] = completionService.take

	/**
	 * Retrieves and removes the Future representing the next
	 * completed task or <tt>None</tt> if none are present.
	 *
	 * @return the Future representing the next completed task, or
	 *         <tt>None</tt> if none are present
	 */
	def poll: Option[Future[V]] = {
		val t = completionService.poll
		if (t == null) None else Some(t)
	}

	/**
	 * Retrieves and removes the Future representing the next
	 * completed task, waiting if necessary up to the specified wait
	 * time if none are yet present.
	 *
	 * @param timeout how long to wait before giving up, in units of
	 *        <tt>unit</tt>
	 * @param unit a <tt>TimeUnit</tt> determining how to interpret the
	 *        <tt>timeout</tt> parameter
	 * @return the Future representing the next completed task or
	 *         <tt>None</tt> if the specified waiting time elapses
	 *         before one is present
	 * @throws InterruptedException if interrupted while waiting
	 */
	def poll(timeout: Long, unit: TimeUnit): Option[Future[V]] = {
		val t = completionService.poll(timeout, unit)
		if (t == null) None else Some(t)
	}

	def pollWaitInMillis(timeoutMs: Long): Option[Future[V]] = poll(timeoutMs, TimeUnit.MILLISECONDS)
}
