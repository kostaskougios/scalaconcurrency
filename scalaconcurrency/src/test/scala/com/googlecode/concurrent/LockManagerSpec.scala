package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import com.googlecode.concurrent.mock.MockLock
import java.util.concurrent.TimeUnit
import com.googlecode.concurrent.mock.MockReadWriteLock

/**
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
class LockManagerSpec extends SpecificationWithJUnit {

	"tryLockAndDo, datetime, executes function and returns value" in {
		var c = 0
		val lock = LockManager.reentrantLock

		import org.scala_tools.time.Imports._

		lock.tryLockAndDo(DateTime.now.plus(10)) {
			c += 1
			"ok"
		} must_== Some("ok")
		c must_== 1
	}

	"lockAndDo executes function and returns value" in {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.lockAndDo {
			c += 1
			"ok"
		} must_== "ok"
		c must_== 1
	}

	"lockAndDo aquires and releases lock" in {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.lockAndDo {
			// nop
		}
		mockLock.lockCount must_== 1
		mockLock.unlockCount must_== 1
	}

	"lockInterruptiblyAndDo executes function and returns value" in {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.lockInterruptiblyAndDo {
			c += 1
			"ok"
		} must_== "ok"
		c must_== 1
	}

	"lockInterruptiblyAndDo aquires and releases the lock" in {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.lockInterruptiblyAndDo {
		}
		mockLock.lockInterruptiblyCount must_== 1
		mockLock.unlockCount must_== 1
	}

	"tryLockAndDo executes function and returns value" in {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.tryLockAndDo {
			c += 1
			"ok"
		} must_== Some("ok")
		c must_== 1
	}

	"tryLockAndDo executes tryLock and unlock" in {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.tryLockAndDo {
		}
		mockLock.tryLockCount must_== 1
		mockLock.unlockCount must_== 1
	}

	"tryLockTimedAndDo executes function and returns value" in {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.tryLockAndDo(100, TimeUnit.MINUTES) {
			c += 1
			"ok"
		} must_== Some("ok")
		c must_== 1
	}

	"tryLockTimedAndDo executes tryLock and unlock" in {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.tryLockAndDo(100, TimeUnit.MINUTES) {
		}
		mockLock.tryLockTimedCount must_== 1
		mockLock.unlockCount must_== 1
	}

	"readWriteLock readLock executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.readLockAndDo { c += 1; "ok" } must_== "ok"
		c must_== 1
	}

	"readWriteLock readLock acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.readLockAndDo {}
		mock.readLock.lockCount must_== 1
		mock.readLock.unlockCount must_== 1
	}

	"readWriteLock writeLock executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.writeLockAndDo { c += 1; "ok" } must_== "ok"
		c must_== 1
	}

	"readWriteLock writeLock acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.writeLockAndDo {}
		mock.writeLock.lockCount must_== 1
		mock.writeLock.unlockCount must_== 1
	}

	"readWriteLock readInterrubtiblyAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.readLockInterruptiblyAndDo { c += 1; "ok" } must_== "ok"
		c must_== 1
	}

	"readWriteLock readLockInterruptiblyAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.readLockInterruptiblyAndDo {}
		mock.readLock.lockInterruptiblyCount must_== 1
		mock.readLock.unlockCount must_== 1
	}

	"readWriteLock writeInterrubtiblyAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.writeLockInterruptiblyAndDo { c += 1; "ok" } must_== "ok"
		c must_== 1
	}

	"readWriteLock writeLockInterruptiblyAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.writeLockInterruptiblyAndDo {}
		mock.writeLock.lockInterruptiblyCount must_== 1
		mock.writeLock.unlockCount must_== 1
	}

	"readWriteLock tryReadLockAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryReadLockAndDo { c += 1; "ok" } must_== Some("ok")
		c must_== 1
	}

	"readWriteLock tryReadLockAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryReadLockAndDo {}
		mock.readLock.tryLockCount must_== 1
		mock.readLock.unlockCount must_== 1
	}

	"readWriteLock tryWriteLockAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryWriteLockAndDo { c += 1; "ok" } must_== Some("ok")
		c must_== 1
	}

	"readWriteLock tryWriteLockAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryWriteLockAndDo {}
		mock.writeLock.tryLockCount must_== 1
		mock.writeLock.unlockCount must_== 1
	}

	"readWriteLock tryReadLockAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryReadLockAndDo(10, TimeUnit.MINUTES) { c += 1; "ok" } must_== Some("ok")
		c must_== 1
	}

	"readWriteLock tryReadLockAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryReadLockAndDo(10, TimeUnit.MINUTES) {}
		mock.readLock.tryLockTimedCount must_== 1
		mock.readLock.unlockCount must_== 1
	}

	"readWriteLock tryWriteLockAndDo executes function" in {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryWriteLockAndDo(10, TimeUnit.MINUTES) { c += 1; "ok" } must_== Some("ok")
		c must_== 1
	}

	"readWriteLock tryWriteLockAndDo acquires/releases lock" in {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryWriteLockAndDo(10, TimeUnit.MINUTES) {}
		mock.writeLock.tryLockTimedCount must_== 1
		mock.writeLock.unlockCount must_== 1
	}

}