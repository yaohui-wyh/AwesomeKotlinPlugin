IntelliJ Plugin for [Awesome Kotlin](https://kotlin.link/)

https://plugins.jetbrains.com/plugin/11357-awesome-kotlin

![](/docs/screenshots.gif)

- Show content from <a href="https://github.com/KotlinBy/awesome-kotlin">Awesome Kotlin</a> inside IDE ToolWindow
- Enable search / checkout Kotlin projects
- Custom content sources & dynamic KotlinScript loading is supported

![](/docs/ide.png)

![](/docs/settings.png)

![](/docs/settings2.png)

## Installation

#### Install from repositories

1. Settings >> Plugins >> Browse repositories...
2. Search for "Awesome Kotlin", install & restart

#### Install from zip file
1. Download zip from JetBrains Plugins Repository or from Github [Releases](https://github.com/alex-yh99/AwesomeKotlinPlugin/releases)
2. Settings >> Plugins >> Install plugin from disk...,select the downloaded zip file in previous step then restart your idea 

## Plugin Development

#### Prerequisite

- JDK 8u112+
- Intellij IDEA (2016.3+)
- Plugins Enabled: Kotlin

Tech Stack

- Build system: Gradle
- Language: Kotlin
- Frameworks: IntelliJ Platform SDK / Java GUI (Swing)

#### Build / Run

```
# run in sandbox
gradle clean runIde

# build artifact
gradle clean buildPlugin
```

## TODO

- [x] Fix GitHub star generation and enable cache
- [ ] ~~Show GitHub repo activities (Issues, etc.)~~
- [x] Update Links from Awesome-Kotlin GitHub repo (KtsCompiler, [KEEP/scripting-support](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md))

## Thanks

- [Awesome Kotlin](https://kotlin.link/)
- [JetBrains](http://plugins.jetbrains.com/)
