package com.payment.app

import io.reactivex.subjects.BehaviorSubject

class ApiCallManager {
    companion object {
        var log = BehaviorSubject.createDefault("");

        fun appendLog(newLog: String) {
            log.onNext((log.value ?: "") + "\n$newLog\n")
        }
    }
}