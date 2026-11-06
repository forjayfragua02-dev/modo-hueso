package com.david.psyche.bone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.david.psyche.bone.databinding.ActivityMainBinding
import java.time.Instant

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var storedHash = "argon2id_placeholder" // En prod: hash real de psyche_2029
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Estado inicial del hueso
        updateBoneUI()
        
        binding.btnLogin.setOnClickListener {
            onLoginClicked(binding.etPassword.text.toString())
        }
        
        binding.btnClearLogs.setOnClickListener {
            // Solo para pruebas - en prod no exponer
            Toast.makeText(this, "Logs limpiados (solo demo)", Toast.LENGTH_SHORT).show()
            updateBoneUI()
        }
    }
    
    fun onLoginClicked(password: String) {
        // FIX 1: No doble conteo - solo un onAttack por intento
        val ip = getSimulatedIP()
        val ua = "Android ${android.os.Build.VERSION.RELEASE} - DavidLab"
        
        // Registrar intento ANTES de verificar
        val log = IntruderTracker.logAttempt(ip, ua)
        
        // Actualiza UI del hueso
        updateBoneUI()
        updateIntruderTable()
        
        // FIX 2: Verificación en background para no congelar UI
        Thread {
            val isValid = LoginShieldFixed.verifyPasswordFixed(password, storedHash, ip)
            runOnUiThread {
                if (isValid) {
                    // ÉXITO - Resetea tarpit para usuario legitimo
                    binding.tvStatus.text = "✓ Acceso concedido. Hueso en reposo."
                    binding.tvStatus.setTextColor(0xFF4CAF50.toInt())
                    BoneCore.attackCount = 0 // Reset tras login bueno
                    updateBoneUI()
                } else {
                    // FALLO - Muestra dureza
                    binding.tvStatus.text = "✗ Intento ${log.attempts} registrado. Dureza: ${"%.2f".format(log.hardnessAtPoint)}\nTiempo para romper: ${log.hardnessAtPoint.toInt()}M años"
                    binding.tvStatus.setTextColor(0xFFF44336.toInt())
                    Toast.makeText(this, "Intento registrado desde ${log.city}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    fun updateBoneUI() {
        val state = BoneState(
            hardness = BoneCore.hardness,
            tarpitDelay = Math.pow(2.0, BoneCore.attackCount.toDouble()).coerceAtMost(86400.0),
            argonMemoryGB = (1.0 + BoneCore.attackCount * 0.5).coerceAtMost(8.0),
            yearsToCrack = Math.pow(2.0, BoneCore.attackCount.toDouble()) / 3600 / 24 / 365 * 1000000,
            attackCount = BoneCore.attackCount
        )
        binding.tvHardness.text = "Dureza: ${"%.2f".format(state.hardness)}"
        binding.tvTarpit.text = "Tarpit: ${state.tarpitDelay.toInt()}s"
        binding.tvArgon.text = "Argon: ${state.argonMemoryGB}GB"
        binding.tvYears.text = "Años para crackear: ${"%.1f".format(state.yearsToCrack/1000)}k años"
        binding.progressBone.progress = (state.hardness * 10).toInt().coerceAtMost(100)
    }
    
    fun updateIntruderTable() {
        val logs = IntruderTracker.getLogs().takeLast(10).reversed()
        val sb = StringBuilder()
        sb.append("IP | Ciudad | Intentos | Dureza\n")
        sb.append("--------------------------------\n")
        logs.forEach {
            sb.append("${it.ip.take(12)} | ${it.city.take(15)} | ${it.attempts} | ${"%.1f".format(it.hardnessAtPoint)}\n")
        }
        binding.tvLogs.text = sb.toString()
    }
    
    fun getSimulatedIP(): String {
        // En producción: tu backend ve la IP real, no el cliente
        val ips = listOf("200.75.12.34", "190.34.56.78", "tor-exit-node-123", "172.58.44.10")
        return ips.random()
    }
}

// FIX: Versión corregida sin doble onAttack y sin bloqueo UI
object LoginShieldFixed {
    fun verifyPasswordFixed(input: String, storedHash: String, ip: String): Boolean {
        // Ya se llamó a onAttack en logAttempt, no llamar de nuevo
        // Solo verificar
        // En prod: Argon2id con memoria adaptativa
        return input == "psyche_2029"
    }
}
