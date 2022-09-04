package dimensional.socks.v4

import dimensional.socks.SocksAddress
import dimensional.socks.SocksClient
import dimensional.socks.SocksCommand
import dimensional.socks.SocksVersion
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

@OptIn(ExperimentalUnsignedTypes::class)
public class Socks4Client(
    override val connection: Connection,
    public val userId: String
) : SocksClient {
    public companion object {
        /**
         * Creates a new SOCKS4 client.
         *
         * @param connection The socket connection to use.
         */
        public fun createClient(connection: Connection, userId: String): SocksClient = Socks4Client(connection, userId)
    }

    override val version: SocksVersion
        get() = SocksVersion.SOCKS4

    override suspend fun handshake() {
        // first packet to SOCKS server will be done through Socks4Client.sendCommand
    }

    override suspend fun sendCommand(cmd: SocksCommand, addr: SocksAddress): SocksAddress {
        require(cmd != SocksCommand.Associate) {
            "UDP is not supported by SOCKS4"
        }

        send {
            writeUByte(cmd.value)               // cmd
            writePacket(addr.asPacket(version)) // dstport & dstip

            // id
            writeText(userId)
            writeByte(0x00)
        }

        return read(false) { // version byte is null
            readReplyCode()
            SocksAddress.read(version, this)
        }
    }

    private fun close() {
        connection.socket.close()
    }

    private suspend fun ByteReadChannel.readReplyCode() {
        val replyCode = readByte().toUByte()

        /* find the reply code */
        val reply = Socks4ReplyCode
            .values()
            .find { it.value == replyCode }
            ?: error("Unknown response code")

        if (reply.failure) {
            /* yup */
            close()
            error(reply.message)
        }
    }
}
