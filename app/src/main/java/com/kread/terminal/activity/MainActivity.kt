package com.kread.terminal.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kread.terminal.R
import com.kread.terminal.session.TerminalService
import com.kread.terminal.session.TerminalSession
import com.kread.terminal.view.TerminalView

class MainActivity : AppCompatActivity(), TerminalSession.SessionCallback {

    private lateinit var terminalView: TerminalView
    private var terminalService: TerminalService? = null
    private var currentSession: TerminalSession? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TerminalService.TerminalBinder
            terminalService = binder.getService()
            serviceBound = true
            createNewSession()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            terminalService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        supportActionBar?.title = "kread"
        
        terminalView = findViewById(R.id.terminal_view)
        
        val serviceIntent = Intent(this, TerminalService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun createNewSession() {
        currentSession = TerminalSession(this)
        terminalService?.addSession(currentSession!!)
        terminalView.attachSession(currentSession!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_session -> {
                createNewSession()
                true
            }
            R.id.action_keyboard -> {
                terminalView.requestFocus()
                true
            }
            R.id.action_about -> {
                showAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAbout() {
        Toast.makeText(this, "Kread Terminal v1.0.0", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    override fun onSessionFinished(session: TerminalSession) {
        runOnUiThread {
            terminalService?.removeSession(session)
            Toast.makeText(this, "Session finished", Toast.LENGTH_SHORT).show()
            createNewSession()
        }
    }

    override fun onTextChanged(session: TerminalSession) {
        runOnUiThread {
            terminalView.invalidate()
        }
    }

    override fun onTitleChanged(session: TerminalSession, title: String) {
        runOnUiThread {
            supportActionBar?.title = title
        }
    }

    override fun onBell(session: TerminalSession) {
    }
}
