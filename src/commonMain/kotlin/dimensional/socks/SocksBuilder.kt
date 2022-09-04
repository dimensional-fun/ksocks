package dimensional.socks

import dimensional.socks.v4.Socks4Builder
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import dimensional.socks.v5.Socks5Builder

public class SocksBuilder(public val selectorManager: SelectorManager, public val remote: SocketAddress) {
    /** SOCKS v4 */
    public fun v4(): Socks4Builder = Socks4Builder(selectorManager, remote)

    /** SOCKS v5 */
    public fun v5(): Socks5Builder = Socks5Builder(selectorManager, remote)
}
