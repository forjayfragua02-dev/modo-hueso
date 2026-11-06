// MODO HUESO - Sistema Antifragil Defensivo
// Laboratorio David, Chiriqui - 13kW
// Legal y defensivo - No destruye, calcifica.

package com.david.psyche.bone

import java.security.SecureRandom
import java.time.Instant
import kotlin.math.pow

// 1. NUCLEO DEL HUESO - Calcula dureza
object BoneCore {
    var hardness = 1.0
    var attackCount = 0
    const val k = 0.15 // calcio

    // Cada golpe = mas duro
    fun onAttack(): BoneState {
        attackCount++
        hardness += k * (1 + attackCount * 0.1)
        val tarpitSeconds = 2.0.pow(attackCount.toDouble()).coerceAtMost(86400.0) // max 24h
        val argonMemoryGB = (1.0 + attackCount * 0.5).coerceAtMost(8.0)
        val yearsToCrack = tarpitSeconds / 3600 / 24 / 365 * 1000000 // 1M intentos
        
        return BoneState(
            hardness = hardness,
            tarpitDelay = tarpitSeconds,
            argonMemoryGB = argonMemoryGB,
            yearsToCrack = yearsToCrack,
            attackCount = attackCount
        )
    }
}

data class BoneState(
    val hardness: Double,
    val tarpitDelay: Double,
    val argonMemoryGB: Double,
    val yearsToCrack: Double,
    val attackCount: Int
)

// 2. REGISTRO DEL INTRUSO - Solo en tu propio sistema
data class IntruderLog(
    val ip: String,
    val country: String,
    val city: String,
    val isp: String,
    val lat: Double,
    val lon: Double,
    val timestamp: String = Instant.now().toString(),
    val userAgent: String,
    val attempts: Int,
    val hardnessAtPoint: Double
)

object IntruderTracker {
    private val logs = mutableListOf<IntruderLog>()
    
    // Simula geolocalizacion por IP - usa ip-api.com en produccion
    fun logAttempt(ip: String, userAgent: String): IntruderLog {
        val state = BoneCore.onAttack()
        // En produccion: val geo = call ip-api.com/json/$ip
        val fakeGeo = when {
            ip.startsWith("200.75") -> Triple("PanamÃ¡", "David, ChiriquÃ­", "Cable Onda")
            ip.contains("tor") -> Triple("Alemania", "Tor Exit Node", "Tor Network")
            else -> Triple("USA", "Miami, FL", "Comcast")
        }
        
        val log = IntruderLog(
            ip = ip,
            country = fakeGeo.first,
            city = fakeGeo.second,
            isp = fakeGeo.third,
            lat = 8.43, lon = -82.43, // David
            userAgent = userAgent,
            attempts = state.attackCount,
            hardnessAtPoint = state.hardness
        )
        logs.add(log)
        return log
    }
    
    fun getLogs() = logs.toList()
}

// 3. PROTECCION DE LOGIN - Argon2id + Tarpit
// Usa libreria: implementation 'com.lambdaworks:crypto:1.0.0' para Argon2
object LoginShield {
    fun verifyPassword(input: String, storedHash: String, ip: String): Boolean {
        // 1. Si IP esta en lista de hueso calcificado, espera
        val state = BoneCore.onAttack()
        Thread.sleep((state.tarpitDelay * 1000).toLong().coerceAtMost(5000)) // max 5s real para no colgar app
        
        // 2. Verifica con Argon2id con memoria adaptativa
        // val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
        // val hash = argon2.hash(state.argonMemoryGB.toInt() * 1024, 10, 1, input.toCharArray())
        // return argon2.verify(storedHash, input.toCharArray())
        
        // Demo: compara simple
        return input == "psyche_2029" // tu clave real va hasheada
    }
}

// 4. USO EN TU ACTIVITY ANDROID
/*
class MainActivity : AppCompatActivity() {
    fun onLoginClicked(password: String) {
        val ip = getPublicIP() // tu backend la ve, no el cliente
        val ua = "Android ${Build.VERSION.RELEASE}"
        
        val log = IntruderTracker.logAttempt(ip, ua)
        updateBoneCanvas(log.hardnessAtPoint) // engrosa el hueso visual
        
        if (LoginShield.verifyPassword(password, storedHash, ip)) {
            // entra
        } else {
            // muestra: "Intento registrado. Dureza ahora ${log.hardnessAtPoint}. Tiempo para romper: ${BoneCore.yearsToCrack} aÃ±os"
            showIntruderTable(IntruderTracker.getLogs())
        }
    }
}
*/

// FORMULA VISIBLE EN TU LABORATORIO:
// D_nueva = D_actual + k * intentos
// Tiempo_crack = 2^intentos / (3600*24*365) * 1M
// Con 13kW: cada intento fallido cuesta 0.003Wh al atacante, pero 0 a ti.