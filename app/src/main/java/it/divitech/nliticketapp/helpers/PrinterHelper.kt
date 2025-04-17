package it.divitech.nliticketapp.helpers

import android.content.Context
import android.os.RemoteException
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService

class PrinterHelper(ctx: Context) {
    private val context: Context = ctx.applicationContext
    private var printerService: SunmiPrinterService? = null

    private val callback: InnerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            printerService = service
        }

        override fun onDisconnected() {
            printerService = null
        }
    }

    fun bind() {
        InnerPrinterManager.getInstance().bindService(context, callback)
    }

    fun unbind() {
        InnerPrinterManager.getInstance().unBindService(context, callback)
    }

    fun isConnected(): Boolean {
        return printerService != null
    }

    @Throws(RemoteException::class)
    fun printText(text: String?) {
        printerService?.printText(text, object : InnerResultCallback() {
            override fun onRunResult(isSuccess: Boolean) {}
            override fun onReturnString(result: String) {}
            override fun onRaiseException(code: Int, msg: String) {}
            override fun onPrintResult(code: Int, msg: String) {}
        })
    }

    @Throws(RemoteException::class)
    fun lineWrap(lines: Int) {
        printerService?.lineWrap(lines, null)
    }

    @Throws(RemoteException::class)
    fun cutPaper() {
        printerService?.cutPaper(null)
    }
    @Throws(RemoteException::class)
    fun printQRCode(data: String?, modulesize: Int) {
        if (printerService != null) {
            printerService!!.printQRCode(data, modulesize, 1, object : InnerResultCallback() {
                override fun onRunResult(isSuccess: Boolean) {}
                override fun onReturnString(result: String) {}
                override fun onRaiseException(code: Int, msg: String) {}
                override fun onPrintResult(code: Int, msg: String) {}
            })
        }
    }


    @Throws(RemoteException::class)
    fun printBitmap(bitmap: android.graphics.Bitmap?) {
        printerService?.printBitmap(bitmap, object : InnerResultCallback() {
            override fun onRunResult(isSuccess: Boolean) {}
            override fun onReturnString(result: String) {}
            override fun onRaiseException(code: Int, msg: String) {}
            override fun onPrintResult(code: Int, msg: String) {}
        })
    }
}
