package dimensional.socks.v4

import dimensional.socks.SocksAddress
import dimensional.socks.SocksClient
import dimensional.socks.SocksCommand
import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public class Socks4Builder(
    public val selectorManager: SelectorManager,
    public val remote: SocketAddress
) {
    private lateinit var userId: String

    /**
     *
     */
    public fun user(id: String): Socks4Builder {
        userId = id
        return this
    }

    /**
     * Connect to the specified [address][SocketAddress]
     *
     * @param address The [SocketAddress] to connect to
     */
    public suspend fun connect(address: SocksAddress): Connection {
        val client = Socks4Client.createClient(
            SocksClient.createConnection(selectorManager, remote),
            userId,
        )

        client.sendCommand(
            SocksCommand.Connect,
            address,
        )

        return client.connection
    }
}
