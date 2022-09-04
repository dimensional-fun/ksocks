package dimensional.socks.v5

import dimensional.socks.SocksAddress
import dimensional.socks.SocksClient
import dimensional.socks.SocksCommand
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import dimensional.socks.SocksVersion
import io.ktor.utils.io.*
import mu.KotlinLogging

@OptIn(ExperimentalUnsignedTypes::class)
public data class Socks5Client(
    override val connection: Connection,
    val authMethods: List<Socks5AuthMethod>
) : SocksClient {
    public companion object {
        private val log = KotlinLogging.logger {  }

        /**
         * Creates a new SOCKS5 client.
         *
         * @param connection  The socket connection to use.
         * @param authMethods The authentication methods to use when handshaking
         */
        public suspend fun createClient(
            connection: Connection,
            authMethods: List<Socks5AuthMethod>
        ): Socks5Client {
            val client = Socks5Client(connection, authMethods)
            client.handshake()

            return client
        }
    }

    override val version: SocksVersion
        get() = SocksVersion.SOCKS5

    /**
     * Performs the SOCKS5 handshake. Performs authentication.
     */
    override suspend fun handshake() {
        try {
            log.info { "Performing SOCKS handshake, auth methods=${authMethods}" }

            /* send greeting. */
            send {
                writeUByte(authMethods.size.toUByte())

                for (auth in authMethods) {
                    writeUByte(auth.type.value)
                }
            }

            /* read response */
            read {
                val authType = Socks5AuthType.valueOf(readByte())
                if (authType is Socks5AuthType.NoMethods) {
                    error("Couldn't authenticate with SOCKS server")
                }

                val auth = authMethods.find { it.type == authType }
                    ?: error("Unknown authentication method was chosen")

                require (auth.handle(this@Socks5Client)) {
                    "Couldn't authenticate"
                }

                log.info { "Successfully performed SOCKS5 handshake" }
            }
        } catch (ex: Throwable) {
            log.info(ex) { "Failed to perform SOCKS5 handshake, closing connection..." }
            connection.socket.close()
            throw ex
        }
    }

    /**
     * Sends a SOCKS command to the remote.
     * **All [SocksCommands][SocksCommand] all supported.**
     *
     * @param cmd  The command to send
     * @param addr The address to use.
     */
    override suspend fun sendCommand(
        cmd: SocksCommand,
        addr: SocksAddress,
    ): SocksAddress {
        log.info { "Sending SOCKS command: $cmd w/ addr: $addr" }

        /* send connect request. */
        send {
            writeUByte(cmd.value) // cmd
            writeUByte(0x00u)  // rsv
            writePacket(addr.asPacket(version)) // dstaddr & dstport
        }

        /* response packet */
        return read {
            readStatusCode()
            readByte()
            SocksAddress.read(version, this)
        }
    }

    private suspend fun ByteReadChannel.readStatusCode() {
        val statusCode = readByte().toUByte()

        /* find the status code */
        val status = Socks5ResponseCode
            .values()
            .find { it.value == statusCode }
            ?: error("Unknown response code")

        if (status.failure) {
            log.info { "SOCKS command failed: ${status.message}" }
            connection.socket.close()
            error(status.message)
        }
    }
}
