# VK Bot Java SDK
<a href="https://vk.com/vkbotsdk"><img src="https://i.imgur.com/XvMSJa2.png"></img></a>
###### Convenient and simple library that helps quickly and easily create a bot for VKontakte https://vk.com/vkbotsdk

---

Using this library one can interact with [VK API](https://vk.com/dev/manuals) for making bots - and there is more.
Functionality is nicely suitable either for communities and personal profiles.

Latest version: [![vk-bot-java-sdk](https://img.shields.io/badge/maven--central-v0.1.3-blue.svg?style=flat)](https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk)

**Russian** | English

## Example
=======

![Example](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen.png)

Implementation of accepting messages of only specified types:
```java
Group group = new Group(151083290, "access_token");
    
group.onSimpleTextMessage(message ->
     new Message()
         .from(group)
         .to(message.authorId())
         .text("×òî-òî ñêó÷íîâàòî áóêîâêè ÷èòàòü. Êàðòèíêó êèíü ëó÷øå.")
         .send()
);

group.onPhotoMessage(message ->
    new Message()
         .from(group)
         .to(message.authorId())
         .text("Óæå ëó÷øå. Íî ÿ òîæå òàê ìîãó. ×òî äàëüøå?")
         .photo("/Users/PeterSamokhin/Desktop/topoviy_mem.png")
         .send()
);

group.onVoiceMessage(message ->
    new Message()
         .from(group)
         .to(message.authorId())
         .text("Íå îõîòà ìíå ãîëîñîâûå òâîè ñëóøàòü.")
         .doc("https://vk.com/doc62802565_447117479")
         .send()
);
```

## Ver. 0.1.3 (25.08.2017) functionality
* Processing direct messages of communities and personal profiles - only [access_token](https://vk.com/dev/access_token) is necessary.
* Ability to specify needed type of messages, e.g. voice messages, plain-text messages, messages with stickers etc.
* Also command processing added:
```java
// Simple example
group.onCommand("/start", message -> 
    // do something with message
);

// Defining aliases
group.onCommand(new String[]{"/start", "/bot", "hello"}, message ->
    // do something with message
);
```
* If you attach image, document, cover etc. via a link, the file is not downloaded but transferred directly into VK as a byte array. Due to it, message processing is stably fast.
* Reacting to a user typing. The status ___User is typing...___ will be shown for 10 seconds unless you send a message:
```java
// React to user typing
group.onTyping(userId -> {
    System.out.println("Ïîëüçîâàòåëü https://vk.com/id" + userId + " íà÷àë ïå÷àòàòü");
});
    
// Let's type too
group.enableTyping(true);
```
* Attaching photo/document etc. via link/from local storage/from VK storage:
```java
// Like this
message.doc("doc62802565_447117479").send();

// Or this
message.doc("/Users/PeterSamokhin/Desktop/cp.zip").send();

// Or even this
message.doc("https://www.petersamokhin.com/files/test.txt").send();
```
* Uploading community cover with one line:
```java
// Into the same community, if you specified group_id and access_token on initialization
group.uploadCover("https://www.petersamokhin.com/files/vk-bot-java-sdk/cover.png");
```

## Preparing
* Firstly, you need to create a community if you are intending to use bot on behalf of it
  * You can do it [there](https://vk.com/groups)
* Then you need to retrieve **access_token**
  * It is very well explained [there](https://vk.com/dev/access_token)]

## Installation
The library is in a central `maven` repo. All you need to do is add a few lines into your build file.

#### If you are using Maven
Add following lines to **pom.xml**:
```xml
<dependency>
    <groupId>com.petersamokhin</groupId>
    <artifactId>vk-bot-java-sdk</artifactId>
    <version>0.1.3</version>
</dependency>
```
#### If you are using Gradle
Add following lines to `dependencies` section in your **build.gradle**:
```gradle
compile group: 'com.petersamokhin', name: 'vk-bot-java-sdk', version: '0.1.3'
```
#### If you are using something else
Please, refer to guidelines for other build systems by Maven: https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk/

---
#### If you ARE NOT using any build system 
* Download distributive (all dependencies are included): [library (3.2 MB)](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar) | [md5](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar.md5)
* Now, all you need to do is add it to `classpath`:
  * Using shell:
  ```bash
  javac -cp "/root/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar" Bot.jar 
  ```
  * If you use **IntelliJ IDEA** go to **Project Structure...** | **Libraries**, click `+` and add downloaded .jar to lib list: 
  ![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen_2.png)

---
Done. The library is usable in your project now.

## Extended description
[Please, refer to the documentation](https://github.com/petersamokhin/vk-bot-java-sdk/wiki/%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F)
