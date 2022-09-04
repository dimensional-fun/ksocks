package dimensional.socks.v5

import io.ktor.utils.io.core.*

/** Class dealing with authentication stuff */
public sealed class Socks5AuthMethod(public val type: Socks5AuthType) {
    public abstract suspend fun handle(client: Socks5Client): Boolean

    /** No authentication */
    public object None : Socks5AuthMethod(Socks5AuthType.NoAuth) {
        override suspend fun handle(client: Socks5Client): Boolean = true

        override fun toString(): String = "Socks5AuthMethod::None"
    }

    /** Username/password authentication */
    @OptIn(ExperimentalUnsignedTypes::class)
    public data class UserPass(val username: String, val password: String) : Socks5AuthMethod(Socks5AuthType.UserPass) {
        init {
            require(username.length in 1..255) { "Username length must be within 1..255 in length" }

            require(password.length in 1..255) { "Password length must be within 1..255 in length" }
        }

        override suspend fun handle(client: Socks5Client): Boolean {
            client.send(false) {
                writeUByte(0x01u)

                /* IDLEN, ID */
                writeUByte(username.length.toUByte())
                writeText(username)

                /* PWLEN, PW */
                writeUByte(password.length.toUByte())
                writeText(password)
            }

            return client.read(false) {
                /* make sure the auth version is the same */
                val version = readByte().toUByte()
                require(version == 0x01u.toUByte()) {
                    "Mismatching user/pass versions"
                }

                /* make sure status is 0x00 */
                readByte() == 0x00.toByte()
            }
        }

        override fun toString(): String =
            "Socks5AuthMethod::UserPass(username=$username, password=${"*".repeat(password.length)})"
    }
}
