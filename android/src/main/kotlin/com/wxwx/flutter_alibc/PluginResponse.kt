package com.wxwx.flutter_alibc

import java.util.*

class PluginResponse constructor(var errorCode: String?, var errorMessage: String?, var data: Any?){

    companion object{
        fun success(obj: Any?): PluginResponse {
            return PluginResponse("0", "成功", obj)
        }
    }

    fun toMap(): Map<String, Any?>? {
        val map = HashMap<String, Any?>()
        map.put("errorCode", errorCode)
        map.put("errorMessage", errorMessage)
        map.put("data", data)
        return map
    }
}