IntelliJ Plugin for [Awesome Kotlin](https://kotlin.link/)

https://plugins.jetbrains.com/plugin/11357-awesome-kotlin

![](/docs/screenshots.gif)

- Show content from <a href="https://github.com/KotlinBy/awesome-kotlin">Awesome Kotlin</a> inside IDE ToolWindow
- Enable search / checkout Kotlin projects
- <s>Custom content sources & dynamic KotlinScript loading is supported</s>

![](/docs/ide.png)

![](/docs/settings.png)

![](/docs/settings2.png)

## Installation

#### Install from repositories

1. Settings >> Plugins >> Browse repositories...
2. Search for "Awesome Kotlin", install & restart

#### Install from zip file
1. Download zip from JetBrains Plugins Repository or from Github [Releases](https://github.com/yaohui-wyh/AwesomeKotlinPlugin/releases)
2. Settings >> Plugins >> Install plugin from disk...,select the downloaded zip file in previous step then restart your idea 

## Plugin Development

#### Prerequisite

- JDK 8u112+
- Intellij IDEA (2016.3+)
- Plugins Enabled: Kotlin

#### Build / Run

```
# run in sandbox
./gradlew clean runIde

# build artifact
./gradlew clean buildPlugin
```

## Thanks

- [Awesome Kotlin](https://kotlin.link/)
- [JetBrains](http://plugins.jetbrains.com/)
