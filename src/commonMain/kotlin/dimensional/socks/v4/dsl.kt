package dimensional.socks.v4

import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public fun socksV4(selectorManager: SelectorManager, remoteAddress: SocketAddress): Socks4Builder =
    Socks4Builder(selectorManager, remoteAddress)
