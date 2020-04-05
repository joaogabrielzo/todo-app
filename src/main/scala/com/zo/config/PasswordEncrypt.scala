package com.zo.config

import de.mkammerer.argon2.{Argon2, Argon2Factory}

class PasswordEncrypt {
    
    private val argon2: Argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
    
    private def passwordToByte(password: String): Array[Byte] =
        password.toCharArray.map(_.toByte)
    
    def encrypt(password: String): String = {
        
        val bytePassword: Array[Byte] = passwordToByte(password)
        
        val hash: String = argon2.hash(4, 1024 * 1024, 4, bytePassword)
        hash
    }
    
    def check(hash: String, password: String): Boolean = {
        
        val bytePassword: Array[Byte] = passwordToByte(password)
        
        val isEqual: Boolean = argon2.verify(hash, bytePassword)
        isEqual
    }
}
