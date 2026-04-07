package com.aidiettracker.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.BuildConfig
import com.aidiettracker.R
import com.aidiettracker.data.ai.nim.NimApiClient
import com.aidiettracker.data.ai.nim.NimChatRequest
import com.aidiettracker.data.ai.nim.NimChatResponse
import com.aidiettracker.data.ai.nim.NimMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ChatbotActivity : AppCompatActivity() {

    companion object {
        private const val MAX_CONTEXT_MESSAGES = 8
        private const val MAX_RESPONSE_TOKENS = 180
    }

    private lateinit var textStatus: TextView
    private lateinit var messagesContainer: LinearLayout
    private lateinit var messagesScroll: ScrollView
    private lateinit var editMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var loadingBar: ProgressBar
    private lateinit var promptOne: TextView
    private lateinit var promptTwo: TextView
    private lateinit var promptThree: TextView

    private val messageHistory = mutableListOf(
        NimMessage(
            role = "system",
            content = "You are NutriFlow Coach. Give practical, safe, concise nutrition advice in under 120 words. Keep tone supportive and avoid medical diagnosis."
        )
    )

    private var activeCall: Call<NimChatResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        bindViews()
        bindActions()
        addAssistantBubble(getString(R.string.chatbot_welcome_message))
    }

    override fun onDestroy() {
        activeCall?.cancel()
        super.onDestroy()
    }

    private fun bindViews() {
        textStatus = findViewById(R.id.text_chat_status)
        messagesContainer = findViewById(R.id.layout_messages)
        messagesScroll = findViewById(R.id.scroll_messages)
        editMessage = findViewById(R.id.edit_chat_message)
        buttonSend = findViewById(R.id.button_send_chat)
        loadingBar = findViewById(R.id.progress_chat_loading)
        promptOne = findViewById(R.id.text_prompt_one)
        promptTwo = findViewById(R.id.text_prompt_two)
        promptThree = findViewById(R.id.text_prompt_three)
    }

    private fun bindActions() {
        findViewById<ImageButton>(R.id.button_back_chat).setOnClickListener {
            finishWithSmoothTransition()
        }

        buttonSend.setOnClickListener {
            submitMessage()
        }

        editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitMessage()
                true
            } else {
                false
            }
        }

        promptOne.setOnClickListener {
            submitPresetPrompt(getString(R.string.chatbot_prompt_breakfast))
        }
        promptTwo.setOnClickListener {
            submitPresetPrompt(getString(R.string.chatbot_prompt_snack))
        }
        promptThree.setOnClickListener {
            submitPresetPrompt(getString(R.string.chatbot_prompt_protein))
        }
    }

    private fun submitMessage() {
        if (BuildConfig.NIM_API_KEY.isBlank()) {
            Toast.makeText(this, getString(R.string.chatbot_missing_key), Toast.LENGTH_LONG).show()
            return
        }

        if (!hasNetworkConnection()) {
            addAssistantBubble(getString(R.string.chatbot_error_offline))
            textStatus.text = getString(R.string.chatbot_status_offline)
            return
        }

        val userText = editMessage.text?.toString()?.trim().orEmpty()
        if (userText.isBlank()) {
            return
        }

        addUserBubble(userText)
        messageHistory.add(NimMessage(role = "user", content = userText))
        editMessage.text?.clear()

        setLoading(true)

        val request = NimChatRequest(
            model = BuildConfig.NIM_MODEL,
            messages = buildRequestMessages(),
            maxTokens = MAX_RESPONSE_TOKENS,
            temperature = 0.35f
        )

        val call = NimApiClient.service.createChatCompletion(
            authorization = "Bearer ${BuildConfig.NIM_API_KEY}",
            request = request
        )
        activeCall = call

        call.enqueue(object : Callback<NimChatResponse> {
            override fun onResponse(call: Call<NimChatResponse>, response: Response<NimChatResponse>) {
                setLoading(false)
                if (!response.isSuccessful) {
                    val status = response.code()
                    val serverHint = response.errorBody()?.string()?.take(180).orEmpty()
                    when (status) {
                        401, 403 -> {
                            addAssistantBubble(getString(R.string.chatbot_error_auth))
                            textStatus.text = getString(R.string.chatbot_status_http_error, status)
                        }
                        429 -> {
                            addAssistantBubble(getString(R.string.chatbot_error_rate_limit))
                            textStatus.text = getString(R.string.chatbot_status_http_error, status)
                        }
                        else -> {
                            addAssistantBubble(getString(R.string.chatbot_error_generic))
                            textStatus.text = getString(R.string.chatbot_status_http_error, status)
                        }
                    }

                    if (serverHint.isNotBlank()) {
                        textStatus.text = getString(R.string.chatbot_status_server_hint, status, serverHint)
                    }
                    return
                }

                val answer = response.body()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    ?.trim()
                    .orEmpty()

                if (answer.isBlank()) {
                    addAssistantBubble(getString(R.string.chatbot_error_empty))
                    textStatus.text = getString(R.string.chatbot_status_empty)
                    return
                }

                messageHistory.add(NimMessage(role = "assistant", content = answer))
                addAssistantBubble(answer)
                textStatus.text = getString(R.string.chatbot_status_ready)
            }

            override fun onFailure(call: Call<NimChatResponse>, t: Throwable) {
                if (call.isCanceled) {
                    return
                }

                setLoading(false)
                addAssistantBubble(mapThrowableToChatMessage(t))
                textStatus.text = t.localizedMessage ?: getString(R.string.chatbot_status_failed)
            }
        })
    }

    private fun buildRequestMessages(): List<NimMessage> {
        if (messageHistory.size <= MAX_CONTEXT_MESSAGES + 1) {
            return messageHistory
        }

        val system = messageHistory.first()
        val tail = messageHistory.takeLast(MAX_CONTEXT_MESSAGES)
        return listOf(system) + tail
    }

    private fun submitPresetPrompt(prompt: String) {
        editMessage.setText(prompt)
        editMessage.setSelection(editMessage.text?.length ?: 0)
        submitMessage()
    }

    private fun hasNetworkConnection(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun mapThrowableToChatMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> getString(R.string.chatbot_error_dns)
            is SocketTimeoutException -> getString(R.string.chatbot_status_timeout)
            is SSLException -> getString(R.string.chatbot_error_ssl)
            else -> getString(R.string.chatbot_error_network)
        }
    }

    private fun setLoading(loading: Boolean) {
        loadingBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonSend.isEnabled = !loading
        editMessage.isEnabled = !loading
        textStatus.text = if (loading) {
            getString(R.string.chatbot_status_thinking)
        } else {
            getString(R.string.chatbot_status_ready)
        }
    }

    private fun addUserBubble(message: String) {
        addBubble(message, isUser = true)
    }

    private fun addAssistantBubble(message: String) {
        addBubble(message, isUser = false)
    }

    private fun addBubble(message: String, isUser: Boolean) {
        val bubble = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(resources.getColor(android.R.color.black, theme))
            setPadding(28, 20, 28, 20)
            maxWidth = (resources.displayMetrics.widthPixels * 0.8f).toInt()
            background = resources.getDrawable(
                if (isUser) R.drawable.bg_chat_user_bubble else R.drawable.bg_chat_bot_bubble,
                theme
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                gravity = if (isUser) Gravity.END else Gravity.START
            }
        }

        messagesContainer.addView(bubble)
        messagesScroll.post {
            messagesScroll.fullScroll(View.FOCUS_DOWN)
        }
    }
}

