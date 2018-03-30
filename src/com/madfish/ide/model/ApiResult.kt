package com.madfish.ide.model

import com.madfish.ide.util.IdeUtil

/**
 * Created by Roger™
 */
class ApiResult<T>(
        var success: Boolean = false,
        var errcode: ErrMessage? = null,
        var errorMessage: String = "",
        var result: T? = null
) {
    constructor(errResult: ApiResult<*>) : this(false, errResult.errcode, errResult.errorMessage)
}

enum class ErrMessage(var text: String) {
    API_NETWORK_ERROR(IdeUtil.message("ErrMessage.API_NETWORK_ERROR"))
}