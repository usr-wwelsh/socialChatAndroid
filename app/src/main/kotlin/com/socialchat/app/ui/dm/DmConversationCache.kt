package com.socialchat.app.ui.dm

import com.socialchat.app.data.model.DmConversation

/** Temporary in-memory holder used to pass DmConversation across navigation destinations. */
object DmConversationCache {
    var current: DmConversation? = null
}
