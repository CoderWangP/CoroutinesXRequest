package com.wp.coroutinesxrequest.components

import androidx.lifecycle.ViewModel
import com.wp.coroutinesxrequest.http.Status
import com.wp.xrequest.logD
import kotlinx.coroutines.flow.*



open class BaseRequestViewModel : ViewModel() {
    private val _requestStatus = MutableStateFlow(Status.NONE)
    val requestStatus: StateFlow<Status> = _requestStatus

    companion object {
        const val TAG = "BaseRequestViewModel"
    }

    inline fun <T> T?.checkAndUpdateStatus(responseBackStatus: Status = Status.SUCCESS, block: T.() -> Unit) {
        if (this == null || this is Unit) {
            logD(TAG,"checkAndUpdateStatus-> T is null = ${this == null} or T is Unit = ${this is Unit}")
            //handle exception but not need exception detail
            onRequestStatusUpdate(Status.ERROR)
        } else {
            block()
            onRequestStatusUpdate(responseBackStatus)
        }
    }


    /**
     * @param requestStatus request status
     */
    fun onRequestStatusUpdate(requestStatus: Status) {
        _requestStatus.value = requestStatus
    }


    fun nextRequestStatus(requestStatus: Status = Status.SUCCESS): Status {
        return when (_requestStatus.value) {
            Status.NONE -> Status.NONE
            Status.PROGRESS_DIALOG, Status.DISMISS_PROGRESS_DIALOG -> Status.DISMISS_PROGRESS_DIALOG
            else -> requestStatus
        }
    }
}