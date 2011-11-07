package com.googlecode.concurrent
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

/**
 * manages locks
 *
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
object LockManager {
	def reentrantLock = new LockEx(new ReentrantLock)
}

class LockEx(lock: Lock) {

	def lockAndDo[T](f: => T): T = {
		lock.lock
		try {
			f
		} finally {
			lock.unlock
		}
	}

	def lockInterruptiblyAndDo[T](f: => T): T = {
		lock.lockInterruptibly
		try {
			f
		} finally {
			lock.unlock
		}
	}

	def tryLockAndDo[T](f: => T): Option[T] =
		if (lock.tryLock)
			try {
				Some(f)
			} finally {
				lock.unlock
			}
		else None

	def tryLockAndDo[T](time: Long, unit: TimeUnit)(f: => T): Option[T] =
		if (lock.tryLock(time, unit))
			try {
				Some(f)
			} finally {
				lock.unlock
			}
		else None

}