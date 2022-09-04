package dimensional.socks.v5

import dimensional.socks.SocksAddress
import dimensional.socks.SocksClient
import dimensional.socks.SocksCommand
import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public class Socks5Builder(public val selectorManager: SelectorManager, public val remote: SocketAddress) {
    private var authMethods: MutableList<Socks5AuthMethod> = mutableListOf(Socks5AuthMethod.None)

    /**
     * Add a new authentication method to use.
     */
    public fun auth(method: Socks5AuthMethod): Socks5Builder {
        authMethods += method
        return this
    }

    public suspend fun connect(address: InetSocketAddress): Connection {
        val inet = address as? InetSocketAddress
            ?: error("unix sockets are not supported.")

        return connect(SocksAddress(inet.hostname, inet.port.toShort()))
    }

    /**
     * Connect to the specified [address][SocketAddress]
     *
     * @param address The [SocketAddress] to connect to
     */
    public suspend fun connect(address: SocksAddress): Connection {
        val client = Socks5Client.createClient(
            SocksClient.createConnection(selectorManager, remote),
            authMethods
        )

        client.sendCommand(SocksCommand.Connect, address)
        return client.connection
    }
}
