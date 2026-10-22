# Parkour Helper

## English

Parkour Helper is a client-side Fabric mod for Minecraft Java 1.21.11. Practice a jump with a local ghost on blocks already loaded from the server, then replay the recorded movement with your real player.

| Key | Command | Action |
| --- | --- | --- |
| F4 | `/parkourhelper start` | Save the current position and start practicing; press again to cancel. |
| F6 | `/parkourhelper reset` | Return the ghost to the saved position and clear the recording. |
| F7 | `/parkourhelper apply` | Replay the recording on the server. |
| F8 | `/parkourhelper cancel` | Stop practice or playback. |

Your real player remains on the server during practice. Before playback, the mod checks that its position and velocity still match the checkpoint; playback stops if the path diverges significantly. Results depend on network latency, server physics, and anti-cheat. Check the server's rules before using automated playback. Each practice attempt is limited to 10 minutes.

Download the jar from [Releases](../../releases) and place it and Fabric API in your `mods` folder. To build from source, use JDK 21 and run `gradlew.bat build` on Windows or `./gradlew build` elsewhere.

This project is based on [Parkour Practice by TheVoidBlock](https://github.com/TheVoidBlock/ParkourPractice). The modified source is distributed under GPL-3.0-or-later; see [LICENSE.txt](LICENSE.txt).

## Русский

Клиентский Fabric-мод для Minecraft Java 1.21.11. Позволяет тренировать прыжок локальным «призраком» на блоках, которые уже загружены с сервера, а затем воспроизвести записанные движения своим персонажем.

| Клавиша | Команда | Действие |
| --- | --- | --- |
| F4 | `/parkourhelper start` | Сохранить точку и начать тренировку; повторное нажатие отменяет её. |
| F6 | `/parkourhelper reset` | Вернуть призрака в точку и очистить запись. |
| F7 | `/parkourhelper apply` | Воспроизвести запись на сервере. |
| F8 | `/parkourhelper cancel` | Прервать тренировку или воспроизведение. |

Настоящий игрок во время тренировки остаётся на сервере. Перед воспроизведением мод проверяет, что его положение и скорость не изменились; при заметном расхождении траектории останавливается. Точность зависит от задержки сети, серверной физики и античита. Проверьте правила сервера перед использованием автоматического воспроизведения. Попытка ограничена 10 минутами.

Для установки скачайте jar из [Releases](../../releases) и положите его вместе с Fabric API в папку `mods`. Для сборки исходников нужен JDK 21: `gradlew.bat build` (Windows) или `./gradlew build`.

Основа проекта — [Parkour Practice от TheVoidBlock](https://github.com/TheVoidBlock/ParkourPractice). Изменённый исходный код распространяется по GPL-3.0-or-later; текст лицензии — в [LICENSE.txt](LICENSE.txt).
