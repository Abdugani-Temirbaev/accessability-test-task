package com.example.testtask.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.testtask.database.AccountRepository
import com.example.testtask.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MyAccessibilityService : AccessibilityService() {

    private lateinit var accountRepository: AccountRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    val tag: String = "AccessibilityLog"

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = arrayOf("com.instagram.android")
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
        accountRepository = AccountRepository(AppDatabase.getDatabase(this).accountDao())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName == "com.instagram.android" && event.className == "com.instagram.mainactivity.InstagramMainActivity") {
            event.source?.let { source ->
                var profileNode: AccessibilityNodeInfo? = null
                fun findProfileNode(node: AccessibilityNodeInfo) {
                    node.forEachChild { child ->
                        if (listOf("Профиль", "Profile")
                                .contains(child.contentDescription?.toString())
                        ) {
                            profileNode = child
                        } else {
                            findProfileNode(child)
                        }
                    }
                }
                findProfileNode(source)
                profileNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                getLoggedInAccountName(source)?.let { accountName ->
                    serviceScope.launch {
                        accountRepository.insertAccount(accountName)
                    }
                    val intent = Intent("com.example.testtask.UPDATE_UI")
                    intent.putExtra("accountName", accountName)
                    sendBroadcast(intent)
                }
            }
        }
    }

    override fun onInterrupt() {}

    private fun getLoggedInAccountName(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null

        return if (node.getChild(0).className == "android.view.ViewGroup" && node.getChild(0).childCount >= 2
            && node.getChild(0)
                .getChild(1).className == "android.widget.LinearLayout" && node.getChild(0)
                .getChild(1).childCount > 1
            && node.getChild(0).getChild(1).getChild(0).className == "android.widget.Button"
        ) node.getChild(0).getChild(1).getChild(0).text.toString()
        else null
    }

    private fun AccessibilityNodeInfo.forEachChild(action: (AccessibilityNodeInfo) -> Unit) {
        for (i in 0 until childCount) {
            action(getChild(i))
        }
    }
}