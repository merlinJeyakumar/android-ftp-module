package com.support.utills

import com.support.encryption.AesCbcWithIntegrity
import com.support.encryption.AesCbcWithIntegrity.CipherTextIvMac
import io.reactivex.rxjava3.core.Single

object EncryptionUtils {
    fun String.encryptAES(hash: String): Single<String> {
        return Single.create { singleEmitter ->
            val cipherTextIvMac: CipherTextIvMac =
                AesCbcWithIntegrity.encrypt(
                    this,
                    AesCbcWithIntegrity.generateKeyFromPassword(
                        hash,
                        hash.toByteArray()
                    )
                )
            singleEmitter.onSuccess(cipherTextIvMac.toString())
        }
    }

    fun String.decryptAES(hash: String): Single<String> {
        return Single.create { singleEmitter ->
            val cipherTextIvMac = CipherTextIvMac(this)
            singleEmitter.onSuccess(
                AesCbcWithIntegrity.decryptString(
                    cipherTextIvMac,
                    AesCbcWithIntegrity.generateKeyFromPassword(
                        hash,
                        hash.toByteArray()
                    )
                )
            )
        }
    }
}