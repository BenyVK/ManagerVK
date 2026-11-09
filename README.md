# ManagerVK

A lightweight **Paper** plugin that gives your server a clean **green Creeper theme**, handy **game mode shortcuts**, and a configurable **/lobby** command, all controlled from a single file: `configvk.yml`.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21-2ea44f)
![Platform](https://img.shields.io/badge/Platform-Paper-2ea44f)
![Java](https://img.shields.io/badge/Java-21%2B-2ea44f)
![License](https://img.shields.io/badge/License-MIT-2ea44f)

---

## Features

- **One config file:** every message, command name, alias, permission and option lives in `configvk.yml`. Nothing is hardcoded.
- **Green Creeper theme:** themed replacements for `/gamemode`, `/tps` and `/effect`. Change the colors once in the `theme:` section and every message follows.
- **Game mode shortcuts:** `/gm c`, `/gm s`, `/gm sp`, `/gm a` (plus `0`-`3` and full names). Shortcuts are configurable.
- **Lobby system:** `/lobby` teleports players to a configured location, with a delay, cancel-on-move and a bypass permission.
- **World restrictions:** choose exactly which worlds allow `/lobby`.
- **Live reload:** apply most changes instantly with `/vkmanager reload`.

---

## Requirements

- Paper (or a Paper fork) **1.21+**
- Java **21+**

---

## Installation

1. Download the latest `ManagerVK-x.x.x.jar` (or build it yourself, see below).
2. Drop it into your server's `plugins/` folder.
3. Start the server. `plugins/ManagerVK/configvk.yml` is generated automatically.
4. Edit the config to your liking and run `/vkmanager reload`.

---

## Commands

| Command | Aliases | Description | Permission | Default |
|---|---|---|---|---|
| `/gamemode <mode> [player]` | `/gm` | Change game mode | `vkmanager.gamemode` | OP |
| | | Change another player's game mode | `vkmanager.gamemode.others` | OP |
| `/tps` | | Show TPS and MSPT | `vkmanager.tps` | Everyone |
| `/effect give <player> <effect> [seconds] [level]` | | Give a potion effect | `vkmanager.effect` | OP |
| `/effect clear [player] [effect]` | | Clear potion effects | `vkmanager.effect` | OP |
| `/lobby` | | Teleport to the lobby | `vkmanager.lobby` | Everyone |
| | | Skip the lobby teleport delay | `vkmanager.lobby.bypass` | OP |
| `/setlobby` | | Save your position as the lobby | `vkmanager.lobby.set` | OP |
| `/vkmanager reload` | `/vkm` | Reload `configvk.yml` | `vkmanager.admin` | OP |

### Game mode shortcuts

| Input | Game mode |
|---|---|
| `c` or `1` | Creative |
| `s` or `0` | Survival |
| `sp` or `3` | Spectator |
| `a` or `2` | Adventure |

Example: `/gm c`, `/gm sp Steve`

---

## Configuration

Everything is controlled from `plugins/ManagerVK/configvk.yml`.

### Theme

```yaml
theme:
  prefix: "&2&lVK &8» "
  primary: "&a"
  secondary: "&2"
  muted: "&7"
  highlight: "&f"
  error: "&c"
```

Messages use tokens like `{prefix}`, `{primary}` and `{highlight}`. Legacy color codes (`&a`) and hex colors (`&#55FF55`) are supported.

### Lobby

```yaml
lobby:
  enabled: true
  command: "lobby"
  permission: "vkmanager.lobby"

  # Worlds where /lobby may be used. Leave empty ([]) to allow every world.
  allowed-worlds:
    - "survival"
    - "world"
    - "spawn"

  destination:
    world: "lobby"
    x: 0.5
    y: 100.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0

  teleport:
    delay: 3
    cancel-on-move: true
```

You can also stand where you want the lobby and run `/setlobby` to save the position automatically.

### What needs a restart?

| Change | Needs restart? |
|---|---|
| Messages, colors, theme | No, use `/vkmanager reload` |
| Allowed worlds, delay, destination | No, use `/vkmanager reload` |
| TPS thresholds and colors | No, use `/vkmanager reload` |
| Command names, aliases, `enabled` flags | Yes |
| Permission nodes and their defaults | Yes |

---

## How the themed commands work

Vanilla command output can't be recolored directly, so ManagerVK registers its own versions of `/gamemode`, `/tps` and `/effect` that behave the same way but use your theme. If another plugin (for example Essentials) also overrides one of these commands, the plugin that loads last wins. You can disable any of them with `enabled: false` in the config.

All messages are sent only to the player who ran the command. When a command targets another player (for example `/gm c Steve`), that player also gets a separate notification. Nothing is broadcast to the whole server.

---

## Building from source

```bash
git clone https://github.com/BenyVK/ManagerVK.git
cd ManagerVK
./gradlew build
```

The jar will be in `build/libs/`.

---

## Support

Found a bug or have an idea? Open an [issue](https://github.com/BenyVK/ManagerVK/issues).

---

## License

Released under the [MIT License](LICENSE).
