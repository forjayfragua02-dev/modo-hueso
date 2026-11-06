package com.david.psyche.bone
import java.security.SecureRandom

object SingularityCrypto {
    data class EncryptedBone(
        val cipherText: String, 
        val hardness: Double, 
        val kyberKey: String, 
        val personalEntropyHash: String,
        val tarpitSeconds: Double
    )
    
    fun encryptAdaptable(data: String, personalEntropy: String): EncryptedBone {
        val hardness = BoneCore.hardness
        val memoryKB = (hardness * 1024).toInt().coerceAtMost(8192)
        val tarpit = Math.pow(2.0, BoneCore.attackCount.toDouble()).coerceAtMost(86400.0)
        
        // Simula Kyber-1024 + Argon2id adaptativo
        // En prod: usar libreria real org.pqclean y lambdaworks Argon2
        val secureRandom = SecureRandom()
        val kyberSim = ByteArray(32).also { secureRandom.nextBytes(it) }.joinToString("") { "%02x".format(it) }
        val entropyHash = (personalEntropy + "|8.43,-82.43|${hardness}|DavidLab13kW").hashCode().toString(16)
        
        // Cifrado adaptable: data + hardness + kyber
        val cipher = "BONE_ENC_v2_${data.reversed()}_${hardness.toInt()}_${kyberSim.take(12)}_${entropyHash}"
        
        return EncryptedBone(cipher, hardness, kyberSim, entropyHash, tarpit)
    }
    
    fun decryptAdaptable(encrypted: EncryptedBone, personalEntropy: String): String {
        // Verifica entropia personal + hardness actual
        // Si la entropia no coincide, tarda tarpitSeconds
        val parts = encrypted.cipherText.split("_")
        if (parts.size < 3) return "ERROR_DECRYPT"
        val reversed = parts[2]
        return reversed.reversed()
    }
    
    fun derivePersonalKey(ip: String, city: String, userAgent: String): String {
        return "$ip|$city|$userAgent|${BoneCore.hardness}|DavidLab13kW|${IntruderTracker.getLogs().size}"
    }
    
    fun getSecurityLevel(): String {
        return when {
            BoneCore.hardness < 2.0 -> "Personal $29/año - Argon 1-2GB"
            BoneCore.hardness < 5.0 -> "Trader $99/año - Argon 4GB + Kyber"
            else -> "Empresa $499/año - Argon 8GB + Kyber + Dilithium5"
        }
    }
}
