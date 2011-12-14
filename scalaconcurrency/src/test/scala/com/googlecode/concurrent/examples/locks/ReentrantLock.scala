package com.googlecode.concurrent.examples.locks
import com.googlecode.concurrent.ExecutorServiceManager
import com.googlecode.concurrent.LockManager

/**
 * a reentrant lock is tried to be locked by 10 threads. The thread that succeeds
 * keeps the lock for 1 ms before releasing it. The other threads resume execution
 * without waiting for the lock to be free.
 *
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object ReentrantLock extends App {
	val lock = LockManager.reentrantLock

	// 10 threads execute the closure. 10000 times the closure is submited for execution
	ExecutorServiceManager.lifecycle(10, 10000) { counter =>
		// tries to lock, upon success executes the function and return Some("ok").
		// if lock is already aquired by an other thread, returns None without executing
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