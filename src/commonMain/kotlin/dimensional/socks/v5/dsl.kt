package dimensional.socks.v5

import io.ktor.network.selector.*
import io.ktor.network.sockets.*

public fun socksV5(selectorManager: SelectorManager, remoteAddress: SocketAddress): Socks5Builder =
    Socks5Builder(selectorManager, remoteAddress)

public fun Socks5Builder.userPass(username: String, password: String): Socks5Builder =
    auth(Socks5AuthMethod.UserPass(username, password))
