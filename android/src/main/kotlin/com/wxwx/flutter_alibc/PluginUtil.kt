package com.wxwx.flutter_alibc

import com.alibaba.alibcprotocol.param.AlibcDegradeType
import com.alibaba.alibcprotocol.param.AlibcTaokeParams
import com.alibaba.alibcprotocol.param.OpenType
import com.wxwx.flutter_alibc.PluginConstants.*
import com.wxwx.flutter_alibc.PluginConstants.Companion as Constants

class PluginUtil {
    companion object{
        fun getOpenType(open: String?): OpenType? {
            return if (Constants.Auto_OpenType == open) {
                OpenType.Auto
            } else {
                OpenType.Native
            }
        }

        fun getClientType(client: String): String? {
            return if (client == Constants.Tmall_ClientType) {
                "tmall"
            } else {
                "taobao"
            }
        }

        fun getFailModeType(mode: String?): AlibcDegradeType? {
            return if (Constants.JumpH5_FailMode == mode) {
                AlibcDegradeType.H5
            } else if (Constants.JumpDownloadPage_FailMode == mode) {
                AlibcDegradeType.Download
            } else {
                AlibcDegradeType.NONE
            }
        }

        fun getTaokeParams(taokePar: Map<String?, Any?>): AlibcTaokeParams? {
            val taokeParams = AlibcTaokeParams("", "", "")
            val pid = taokePar["pid"] as String?
            if (pid != null) {
                taokeParams.pid = pid
            }
            val unionId = taokePar["unionId"] as String?
            if (unionId != null) {
                taokeParams.unionId = unionId
            }
            val subPid = taokePar["subPid"] as String?
            if (subPid != null) {
                taokeParams.subPid = subPid
            }
            val extParams = taokePar["extParams"] as Map<String, String>?
            if (extParams != null) {
                taokeParams.extParams = extParams;
            }
            return taokeParams
        }
    }

}