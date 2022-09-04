package dimensional.socks

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

@OptIn(ExperimentalUnsignedTypes::class)
public interface SocksClient {
    public companion object {
        internal suspend fun createConnection(
            selectorManager: SelectorManager,
            socksServer: SocketAddress,
        ): Connection = aSocket(selectorManager)
            .tcpNoDelay()
            .tcp()
            .connect(socksServer)
            .connection()
    }

    /**
     * The socket connection for this SOCKS client.
     */
    public val connection: Connection

    /**
     * The version of this SOCKS client.
     */
    public val version: SocksVersion

    /**
     * Perform SOCKS handshake.
     * Behavior varies on the version being implemented
     */
    public suspend fun handshake()

    /**
     * Sends a SOCKS command to the remote server.
     */
    public suspend fun sendCommand(cmd: SocksCommand, addr: SocksAddress): SocksAddress

    /**
     *
     */
    public suspend fun send(writeVersion: Boolean = true, block: BytePacketBuilder.() -> Unit) {
        val packet = BytePacketBuilder()
        if (writeVersion) {
            packet.writeUByte(version.value) // SOCKS5
        }

        packet.block()

        /* write packet to the output channel and flush */
        connection.output.writePacket(packet.build())
        connection.output.flush()
    }

    /**
     *
     */
    public suspend fun <T> read(readVersion: Boolean = true, block: suspend ByteReadChannel.() -> T): T {
        connection.input.awaitContent()

        if (readVersion) {
            val version = connection.input.readByte().toUByte()
            require(version == this.version.value) {
                "Version was not SOCKS5"
            }
        }

        return connection.input.block()
    }
}
