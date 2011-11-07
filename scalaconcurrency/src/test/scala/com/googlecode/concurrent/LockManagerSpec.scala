package com.googlecode.concurrent
import org.specs2.mutable.SpecificationWithJUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import com.googlecode.concurrent.mock.MockLock
import java.util.concurrent.TimeUnit

/**
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
class LockManagerSpec extends SpecificationWithJUnit {

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
		val lock = new MockLock with LockEx
		lock.lockAndDo {
			// nop
		}
		lock.lockCount must_== 1
		lock.unlockCount must_== 1
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
		val lock = new MockLock with LockEx
		lock.lockInterruptiblyAndDo {
		}
		lock.lockInterruptiblyCount must_== 1
		lock.unlockCount must_== 1
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
		val lock = new MockLock with LockEx
		lock.tryLockAndDo {
		}
		lock.tryLockCount must_== 1
		lock.unlockCount must_== 1
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
		val lock = new MockLock with LockEx
		lock.tryLockAndDo(100, TimeUnit.MINUTES) {
		}
		lock.tryLockTimedCount must_== 1
		lock.unlockCount must_== 1
	}

}