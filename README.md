# ShopChest

ShopChest - Spigot/Bukkit Plugin

## General information

This fork of [ShopChest](https://github.com/EpicEricEE/ShopChest) intend to be updatable easily.
It also adds some common features like barrel or shulker shops.
It's still in progress and therefore, this repository has many branches.
Most of them have very explicit name, but there is two things to know:

- The 'master' branch is the original plugin, with no major changes.
  It supports the most recent version of Minecraft.
- The 'feature/clean-project' branch is a 'work-in-progress' branch.
  It contains bug fixes and adds new features.
  However, as the structure change, some plugin that were previously using ShopChest can break.
  If it's the case, just use the original version (in 'master' branch).

## API

To use the API, you need to add the following repository and dependency in your maven project:

```xml
<repositories>
  <repository>
    <id>shopchest-repo</id>
    <url>https://epicericee.github.io/ShopChest/maven/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>de.epiceric</groupId>
    <artifactId>ShopChest</artifactId>
    <version>1.11.1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

You can find the javadoc here: https://epicericee.github.io/ShopChest/javadoc/

## Build

You need Maven with Github access and a jdk.
If you don't know how to give Github access to maven,
check [this](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token)
.
If you don't know how to create a personal token,
check [this](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
.

- Use ``sh lib/install_local_depedencies.sh`` to import local dependencies.
- Use ``mvn clean install`` at the root of the project to build ShopChest artifact.

After the build succeeded, the ShopChest.jar is found in the ``/plugin/target/`` folder.

## Issues

If you find any issues, please provide them in the [Issues Section](https://github.com/Flowsqy/ShopChest/issues) with a
good description of how to reproduce it. If you get any error messages in the console, please also provide them.

## Download

You can download the recent builds in the [release section](https://github.com/Flowsqy/ShopChest/releases).
This resource/plugin can also be found on the official spigot
page [here](https://www.spigotmc.org/resources/shopchest.11431/), but as I don't own it, the most recent build there
only supports Minecraft up to 1.16.

