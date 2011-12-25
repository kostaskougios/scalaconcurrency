package com.googlecode.concurrent.examples.locks

import com.googlecode.concurrent.LockManager
import com.googlecode.concurrent.ExecutorServiceManager
import org.scala_tools.time.Imports._

/**
 * one thread will aquire a read lock and keep it for 1 sec.
 * The main thread will then try to acquire a write lock and
 * wait for 100ms and then fail to acquire the lock. It will
 * then try to acquire the lock waiting for 1100ms and
 * succeed.
 *
 * @author kostantinos.kougios
 *
 * 22 Dec 2011
 */
object TryLockAndDo extends App {

	val lock = LockManager.readWriteLock

	val e = ExecutorServiceManager.newSingleThreadExecutor

	e.submit {
		lock.readLockAndDo {
			println("aquired a read lock")
			Thread.sleep(1000)
			println("releasing read lock")
		}
	}
	Thread.sleep(100)
	lock.tryWriteLockAndDo {
		println("(A) shouldn't execute this line because a read lock is already aquired")
	}

	lock.tryWriteLockAndDo(DateTime.now + 100.millis) {
		println("(B) shouldn't execute this line because a read lock is already aquired")
	}

	val startTime = System.currentTimeMillis
	val result = lock.tryWriteLockAndDo(DateTime.now + 1100.millis) {
		val dt = System.currentTimeMillis - startTime
		println("(C) aquired the lock after waiting for %d ms".format(dt))
		25
	}

	println("Result of calculation : %s".format(result))

	e.shutdownAndAwaitTermination(1)
	println("done")
}
