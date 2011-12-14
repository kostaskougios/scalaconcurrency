package com.googlecode.concurrent.examples.locks
import com.googlecode.concurrent.LockManager
import com.googlecode.concurrent.ExecutorServiceManager

/**
 * a read/write lock is created with 4 threads acquiring a read lock
 * and 1 thread a write lock. While the write lock is acquired, the 4
 * threads have to wait till it is released
 *
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object ReadWriteLock extends App {
	val rwLock = LockManager.readWriteLock

	ExecutorServiceManager.lifecycle(5, 100) { counter =>
		counter % 10 match {
			case 0 =>
				rwLock.writeLockAndDo {
					println("%d: I've got a write lock, no one can get a lock now!".format(counter))
					Thread.sleep(1000)
					println("%d: releasing write lock".format(counter))
				}
			case _ =>
				rwLock.readLockAndDo {
					println("%d: I've got a read lock".format(counter))
					Thread.sleep(100)
					println("%d: releasing read lock".format(counter))
				}
		}
	}
}