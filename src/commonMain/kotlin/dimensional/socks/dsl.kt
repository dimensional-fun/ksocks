package dimensional.socks

import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public fun socks(selectorManager: SelectorManager, remoteAddress: SocketAddress): SocksBuilder =
    SocksBuilder(selectorManager, remoteAddress)
