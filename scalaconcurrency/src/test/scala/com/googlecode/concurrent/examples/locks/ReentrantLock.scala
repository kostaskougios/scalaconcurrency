package com.googlecode.concurrent.examples.locks
import com.googlecode.concurrent.ExecutorServiceManager
import com.googlecode.concurrent.LockManager

/**
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object ReentrantLock extends App {
	val lock = LockManager.reentrantLock

	ExecutorServiceManager.lifecycle(10, 10000) { counter =>
		// tries to lock, upon success executes the function and return Some("ok").
		// if lock is aquired by an other thread, returns None without executing
		// the function
		val result = lock.tryLockAndDo {
			println("yeah, got the lock, will keep it for a while!")
			Thread.sleep(1)
			println("now releasing the lock and returning 'ok'")
			"ok"
		}
		println(result) // prints either Some("ok") or None
	}
}