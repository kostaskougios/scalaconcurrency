package com.googlecode.concurrent.mock
import java.util.concurrent.locks.Lock
import java.util.concurrent.TimeUnit

/**
 * @author kostantinos.kougios
 *
 * 7 Nov 2011
 */
class MockLock extends Lock {
	var lockCount = 0
	var lockInterruptiblyCount = 0
	var tryLockCount = 0
	var tryLockTimedCount = 0
	var unlockCount = 0

	def lock {
		lockCount += 1
	}

	def lockInterruptibly {
		lockInterruptiblyCount += 1
	}

	def tryLock = {
		tryLockCount += 1
		true
	}

	def tryLock(time: Long, unit: TimeUnit) = {
		tryLockTimedCount += 1
		true
	}
	def unlock {
		unlockCount += 1
	}

	def newCondition = null

}
