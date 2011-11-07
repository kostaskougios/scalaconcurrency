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
	def reentrantLock = new ReentrantLock with LockEx
}

trait LockEx { this: Lock =>

	def lockAndDo[T](f: => T): T = {
		lock
		try {
			f
		} finally {
			unlock
		}
	}

	def lockInterruptiblyAndDo[T](f: => T): T = {
		lockInterruptibly
		try {
			f
		} finally {
			unlock
		}
	}

	def tryLockAndDo[T](f: => T): Option[T] =
		if (tryLock)
			try {
				Some(f)
			} finally {
				unlock
			}
		else None

	def tryLockAndDo[T](time: Long, unit: TimeUnit)(f: => T): Option[T] =
		if (tryLock(time, unit))
			try {
				Some(f)
			} finally {
				unlock
			}
		else None

}