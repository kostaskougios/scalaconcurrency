package com.googlecode.concurrent

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import com.googlecode.concurrent.mock.MockLock
import java.util.concurrent.TimeUnit
import com.googlecode.concurrent.mock.MockReadWriteLock
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
@RunWith(classOf[JUnitRunner])
class LockManagerSuite extends FunSuite with ShouldMatchers {
	test("tryLockAndDo, datetime, fails within time") {
		val lock = LockManager.reentrantLock
		// acquire lock on a different thread
		val t = new Thread {
			override def run {
				lock.tryLockAndDo {
					Thread.sleep(1000)
				}
			}
		}

		t.start
		Thread.sleep(200)
		import org.scala_tools.time.Imports._

		val start = System.currentTimeMillis
		lock.tryLockAndDo(DateTime.now + 100.millis) {
			"ok"
		} should be(None)

		// did it wait?
		(System.currentTimeMillis - start) should be > 50.toLong
	}

	test("tryLockAndDo, datetime, executes function and returns value") {
		var c = 0
		val lock = LockManager.reentrantLock

		import org.scala_tools.time.Imports._

		lock.tryLockAndDo(DateTime.now + 10.millis) {
			c += 1
			"ok"
		} should be === Some("ok")
		c should be === 1
	}

	test("lockAndDo executes function and returns value") {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.lockAndDo {
			c += 1
			"ok"
		} should be === "ok"
		c should be === 1
	}

	test("lockAndDo aquires and releases lock") {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.lockAndDo {
			// nop
		}
		mockLock.lockCount should be === 1
		mockLock.unlockCount should be === 1
	}

	test("lockInterruptiblyAndDo executes function and returns value") {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.lockInterruptiblyAndDo {
			c += 1
			"ok"
		} should be === "ok"
		c should be === 1
	}

	test("lockInterruptiblyAndDo aquires and releases the lock") {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.lockInterruptiblyAndDo {
		}
		mockLock.lockInterruptiblyCount should be === 1
		mockLock.unlockCount should be === 1
	}

	test("tryLockAndDo executes function and returns value") {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.tryLockAndDo {
			c += 1
			"ok"
		} should be === Some("ok")
		c should be === 1
	}

	test("tryLockAndDo executes tryLock and unlock") {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.tryLockAndDo {
		}
		mockLock.tryLockCount should be === 1
		mockLock.unlockCount should be === 1
	}

	test("tryLockTimedAndDo executes function and returns value") {
		var c = 0
		val lock = LockManager.reentrantLock
		lock.tryLockAndDo(100, TimeUnit.MINUTES) {
			c += 1
			"ok"
		} should be === Some("ok")
		c should be === 1
	}

	test("tryLockTimedAndDo executes tryLock and unlock") {
		val mockLock = new MockLock
		val lock = new LockEx(mockLock)
		lock.tryLockAndDo(100, TimeUnit.MINUTES) {
		}
		mockLock.tryLockTimedCount should be === 1
		mockLock.unlockCount should be === 1
	}

	test("readWriteLock readLock executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.readLockAndDo { c += 1; "ok" } should be === "ok"
		c should be === 1
	}

	test("readWriteLock readLock acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.readLockAndDo {}
		mock.readLock.lockCount should be === 1
		mock.readLock.unlockCount should be === 1
	}

	test("readWriteLock writeLock executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.writeLockAndDo { c += 1; "ok" } should be === "ok"
		c should be === 1
	}

	test("readWriteLock writeLock acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.writeLockAndDo {}
		mock.writeLock.lockCount should be === 1
		mock.writeLock.unlockCount should be === 1
	}

	test("readWriteLock readInterrubtiblyAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.readLockInterruptiblyAndDo { c += 1; "ok" } should be === "ok"
		c should be === 1
	}

	test("readWriteLock readLockInterruptiblyAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.readLockInterruptiblyAndDo {}
		mock.readLock.lockInterruptiblyCount should be === 1
		mock.readLock.unlockCount should be === 1
	}

	test("readWriteLock writeInterrubtiblyAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.writeLockInterruptiblyAndDo { c += 1; "ok" } should be === "ok"
		c should be === 1
	}

	test("readWriteLock writeLockInterruptiblyAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.writeLockInterruptiblyAndDo {}
		mock.writeLock.lockInterruptiblyCount should be === 1
		mock.writeLock.unlockCount should be === 1
	}

	test("readWriteLock tryReadLockAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryReadLockAndDo { c += 1; "ok" } should be === Some("ok")
		c should be === 1
	}

	test("readWriteLock tryReadLockAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryReadLockAndDo {}
		mock.readLock.tryLockCount should be === 1
		mock.readLock.unlockCount should be === 1
	}

	test("readWriteLock tryWriteLockAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryWriteLockAndDo { c += 1; "ok" } should be === Some("ok")
		c should be === 1
	}

	test("readWriteLock tryWriteLockAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryWriteLockAndDo {}
		mock.writeLock.tryLockCount should be === 1
		mock.writeLock.unlockCount should be === 1
	}

	test("readWriteLock timed tryReadLockAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryReadLockAndDo(10, TimeUnit.MINUTES) { c += 1; "ok" } should be === Some("ok")
		c should be === 1
	}

	test("readWriteLock timed tryReadLockAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryReadLockAndDo(10, TimeUnit.MINUTES) {}
		mock.readLock.tryLockTimedCount should be === 1
		mock.readLock.unlockCount should be === 1
	}

	test("readWriteLock timed tryWriteLockAndDo executes function") {
		val lock = LockManager.readWriteLock
		var c = 0
		lock.tryWriteLockAndDo(10, TimeUnit.MINUTES) { c += 1; "ok" } should be === Some("ok")
		c should be === 1
	}

	test("readWriteLock timed tryWriteLockAndDo acquires/releases lock") {
		val mock = new MockReadWriteLock
		val lock = new ReadWriteLockEx(mock)
		lock.tryWriteLockAndDo(10, TimeUnit.MINUTES) {}
		mock.writeLock.tryLockTimedCount should be === 1
		mock.writeLock.unlockCount should be === 1
	}

}