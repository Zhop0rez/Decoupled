# Decoupled (Perfect LAN)

**Decoupled** is an advanced Fabric optimization mod that completely separates the integrated server from the client's Java process in singleplayer. 

By spinning up the singleplayer world as a true Dedicated Server running in the background, Decoupled completely isolates the client from server-side lag spikes, chunk generation stutter, and Garbage Collection (GC) pauses. It delivers flawlessly smooth 1% low FPS, making singleplayer feel as smooth as playing on a premium multiplayer server.

## Features
- **True Multi-Processing:** Splits Minecraft into two separate Java Virtual Machines (JVMs).
- **Flawless 1% Lows:** World generation, mob AI, and redstone calculations no longer block the client's Render thread.
- **Perfect LAN:** Opens your world to the local network automatically without Windows Firewall issues. The server port is printed directly to the host's chat.
- **In-Game Configuration:** Configure background server RAM and CPU cores allocation directly from the client's mod menu.
- **Smart Pause (TickRateManager):** Vanilla's `ESC` pause still works flawlessly! It seamlessly freezes the background server entities and world time without interrupting the network KeepAlive packets.
- **Host Persistence:** Uses a custom login handshake interceptor (`HostIdentityMixin`) to ensure the host keeps their original Singleplayer inventory and stats, regardless of nickname or offline mode status.

## Requirements
- **Memory:** Minimum of 12-16 GB of system RAM. Running two JVMs simultaneously increases the base memory footprint.
- **CPU:** 6-core processor or better recommended for optimal OS-level context switching between processes.
- **Fabric Loader:** `0.19.3` or higher.
- **Minecraft:** `1.21.x`

## Known Limitations
- Mod configs altered via Mod Menu on the client do not sync to the background server in real-time. You must restart the world to apply server-side config changes.
- Requires raw system resources. Not recommended for old dual-core laptops with 8 GB of RAM.

## How to Play with Friends
Simply join your singleplayer world. The mod will automatically print the assigned background server port in your chat. Share your IP address (Local/Hamachi/Radmin) and that port with your friends!

## License
Licensed under CC0-1.0. Feel free to use, modify, and distribute.
