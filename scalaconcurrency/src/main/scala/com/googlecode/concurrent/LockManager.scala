package com.googlecode.concurrent
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.ReadWriteLock

/**
 * manages locks.
 *
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
object LockManager {
	def reentrantLock = new LockEx(new ReentrantLock)
	def readWriteLock = new ReadWriteLockLockEx(new ReentrantReadWriteLock)
}

protected class ReadWriteLockLockEx(val lock: ReadWriteLock) {
	private val readLock = new LockEx(lock.readLock())
	private val writeLock = new LockEx(lock.writeLock())

	def readLockAndDo[T](f: => T): T = readLock.lockAndDo(f)
}

protected class LockEx(val lock: Lock) {

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