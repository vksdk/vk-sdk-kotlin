# VK Bot Java SDK
<a href="https://vk.com/vkbotsdk"><img src="https://petersamokhin.com/files/vk-bot-java-sdk/cover.png"></img></a>
###### Удобная и простая библиотека, помогающая легко и быстро создать бота для ВКонтакте https://vk.com/vkbotsdk

---

С помощью данной библиотеки можно довольно просто взаимодействовать с [VK API](https://vk.com/dev/manuals) для создания ботов и не только. 
Функционал прекрасно подходит как для сообществ, так и для личных страниц.

Последняя версия: [![vk-bot-java-sdk](https://img.shields.io/badge/maven--central-v0.1.3-blue.svg?style=flat)](https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk)

## Пример

![Example](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen.png)

Реализуем возможность получать уведомления только о сообщениях нужного типа:
```java
Group group = new Group(151083290, "access_token");
    
group.onSimpleTextMessage(message ->
     new Message()
         .from(group)
         .to(message.authorId())
         .text("Что-то скучновато буковки читать. Картинку кинь лучше.")
         .send()
);

group.onPhotoMessage(message ->
    new Message()
         .from(group)
         .to(message.authorId())
         .text("Уже лучше. Но я тоже так могу. Что дальше?")
         .photo("/Users/PeterSamokhin/Desktop/topoviy_mem.png")
         .send()
);

group.onVoiceMessage(message ->
    new Message()
         .from(group)
         .to(message.authorId())
         .text("Не охота мне голосовые твои слушать.")
         .doc("https://vk.com/doc62802565_447117479")
         .send()
);
```
## Функционал: 0.1.4-alpha
Эта версия доступна только при прямом скачивании библиотеки отсюда — в центральный репозиторий она будет добавлена чуть позже, в дополненном виде.

* Пофикшено много мелочей, не влияющих на качество работы и производительность, но делающих работу с библиотекой удобнее, и код читаемее.
* Добавлена работа с чатами — теперь на основе библиотеки легко и быстро можно делать ботов, работающих с чатами:
  * добавлена возможность обработки сообщений конкретно из чатов и проверка, из чата ли сообщение:
  ```java
  user.onMessage(message -> {
        if (message.isMessageFromChat()) {

            // Get chat id
            int chatIdLong = message.getChatIdLong(); // 2000000011
            int chatId = message.chatId();            // 11
            int sender = message.authorId();          // 62802565

            // Handle message, it's from chat
            new Message()
                        .from(user)
                        .to(chatIdLong)
                        .text("Hello, chat!")
                        .send();

        } else {

           // Handle message that not from chat
           new Message()
                        .from(user)
                        .to(message.authorId())
                        .text("Sorry, I will work only in chats.")
                        .send();
        }
  });
  
  user.onChatMessage(message -> {
      // Handle message, it's from chat
  });
  ```
  * добавлена возможность обрабатывать события в чате:
  ```java
  // Handle title changing
  user.onChatTitleChanged((oldTitle, newTitle, who, chat) -> {
  
      String s = "User with id " + who + " changed title in chat " + chat + " from «" + oldTitle + "» to «" + newTitle + "»";

      new Message()
          .from(user)
          .to(chat)
          .text(s)
          .send();

      // User with id 62802565 changed title in chat 2000000011 from «Test 0» to «Test 1»
  });  
  ```
  И так далее. Во всех методах возвращается полный ID чата, при получении сообщения можно получить оба варианта. Для удобства, это значение (`2000000000`) добавлено в качестве константы, и его можно получить из `com.petersamokhin.bots.sdk.objects.Chat.CHAT_PREFIX`.

## Функционал: версия 0.1.3 (25.08.2017)
* Работа с личными сообщениями сообществ и личных страниц — необходим только [access_token](https://vk.com/dev/access_token).
* Возможность обработки сообщений только нужного типа (голосовые, простые текстовые, со стикером, и так далее)
* Можно, также, реагировать не только на сообщения с определенными вложениями, но и на сообщения, содержащие только определенные команды:
```java
// Самый простой вариант для одной команды
group.onCommand("/start", message -> 
    // do something with message
);

// Если команд много
group.onCommand(new String[]{"/start", "/bot", "hello"}, message ->
    // do something with message
);
```
* При прикреплении фотографий и документов к сообщению используется новый метод API, позволяющий загружать вложения напрямую в диалог. Таким образом все вложения отправляются как бы от имени пользователя, и никаких лимитов нет.
* В случае загрузки фото, документа, обложки и прочего по ссылке, файл не будет скачан, а напрямую, как массив байт, будет передан и загружен в VK. Благодаря этому достигнута высокая скорость обработки сообщений.
* Возможность реагировать на то, что пользователь начал печатать и автоматически начинать печатать всем пользователям, от которых пришло сообщение (статус ___...набирает сообщение...___ будет показан в течение 10 секунд, либо пока вы не отправите сообщение):
```java
// Реагируем на печать
group.onTyping(userId -> {
    System.out.println("Пользователь https://vk.com/id" + userId + " начал печатать");
});
    
// Печатаем сами
group.enableTyping(true);
```
* Возможность прикрепить картинку/документ/etc по ссылке/с диска/из VK:
```java
// Можно так
message.doc("doc62802565_447117479").send();

// Или так
message.doc("/Users/PeterSamokhin/Desktop/cp.zip").send();

// Или даже так
message.doc("https://www.petersamokhin.com/files/test.txt").send();
```
* Возможность загрузить обложку в сообщество одной строчкой:
```java
// В эту же группу, если при инициализации были указаны и access_token, и ID группы
group.uploadCover("https://www.petersamokhin.com/files/vk-bot-java-sdk/cover.png");
```
* Улучшено и упрощено взаимодействие с VK API: все запросы, делаемые с помощью библиотеки, напрямую или косвенно (отправкой сообщений и т.д.), становятся в очередь и выполняются с помощью метода `execute`, но можно и напрямую использовать этот метод и отдавать несколько запросов для одновременного их выполнения, синхронно или асинхронно:
```java
// Обращаемся к VK API
// Запрос будет поставлен в очередь и ответ вернётся в коллбэк
// Таким образом можно выполнять до 75 обращений к VK API в секунду
group.api().call("users.get", "{user_ids:[1,2,3]}", response -> {
     System.out.println(response);
});

// Асинхронно ставим запросы к API в очередь
JSONObject params_0 = new JSONObject();
params_0.put("user_ids", new JSONArray("[1,2,3]"));
params_0.put("fields", "photo_max_orig");
        
CallAsync call = new CallAsync("users.get", params_0, response -> {
    System.out.println(response);
});

JSONObject params_1 = new JSONObject();
params_1.put("offset", 100);
params_1.put("count", 50);

CallAsync call_1 = new CallAsync("messages.get", params_1, response -> {
    System.out.println(response);
});

// Выполняем столько запросов, сколько нам нужно
// Перечислив их через запятую в качестве параметров
group.api().execute(call_0, call_1);

// Или же синхронно
// Тогда ответы от ВК будут в массиве
// Под теми же индексами, в каком порядке были переданы запросы
JSONObject params_0 = new JSONObject();
params_0.put("user_ids", new JSONArray("[1,2,3]"));
params_0.put("fields", "photo_max_orig");

CallSync call_0 = new CallSync("users.get", params_0);

JSONObject params_1 = new JSONObject();
params_1.put("offset", 100);
params_1.put("count", 50);

CallSync call_1 = new CallSync("messages.get", params_1);

// Выводим на экран ответ на call_1
System.out.println(responses.get(1));
```
* Работаем с [Callback API](https://vk.com/dev/callback_api) ВКонтакте:
```java
// Самый простой способ - все настройки по дефолту
// Указываем только путь для прослушки запросов
// Полную и подробную настройку провести тоже можно при необходимости
group.callbackApi("/callback").onGroupJoin(newSubscriber ->
    System.out.println("Новый подписчик: https://vk.com/id" + newSubscriber.getInt("user_id"))
);
 
// Возвращён будет только object из ответа 
// (помимо него в ответе от ВК присутствует тип запроса и id группы)
group.onGroupJoin(newSubscriber ->
    System.out.println("Новый подписчик: https://vk.com/id" + newSubscriber.getInt("user_id"))
);
```
* Возможность как использовать настройки по умолчанию и написать бота в две строчки кода, так и возможность провести тонкую настройку, указать любой параметр, полностью управлять всем процессом и получать лог событий в консоль.
* В последнем обновлении библиотека стала полностью потокобезопасна благодаря внедрению `java.util.concurrent` пакета: производительность увеличена в разы, задержек при обработке сообщений нет, старые баги исправлены.
* Библиотека полностью и довольно подробно продукоментирована. В этом репозитории можно увидеть комментарии почти к каждому методу и каждому параметру, а также скомпилированы <a href="https://www.petersamokhin.com/files/vk-bot-java-sdk/javadoc/index.html">javadoc</a>.
* Убраны лишние зависимости, библиотека является самодостаточной настолько, насколько это было возможно (используется только **slf4j** и **log4j** для логгирования и **sparkjava** для обработки запросов).
## Подготовка
* Для начала необходимо создать сообщество, если бот будет работать от его имени
  * Сделать это можно [здесь](https://vk.com/groups)
* Затем необходимо получить **access_token** (_ключ доступа_)
  * Максимально подробно всё изложено [здесь](https://vk.com/dev/access_token)

## Установка
Библиотека добавлена в центральный репозиторий `maven`. Для её использования достаточно (при условии успользования любых систем сборок) добавить всего пару строк в конфигурационный файл.

#### Для maven
Добавить строки, что ниже, в **pom.xml**:
```xml
<dependency>
    <groupId>com.petersamokhin</groupId>
    <artifactId>vk-bot-java-sdk</artifactId>
    <version>0.1.3</version>
</dependency>
```
#### Для gradle 
Добавить строки, что ниже, в **build.gradle** в dependencies:
```gradle
compile group: 'com.petersamokhin', name: 'vk-bot-java-sdk', version: '0.1.3'
```
### Любые другие системы сборок
Поскольку библиотека загружена в центральный репозиторий, на сайте поиска по репозиторию описаны способы подключения библиотеки с помощью любой из систем сборок: https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk/

---
#### Без систем сборок (добавляем библиотеку в classpath)
Здесь немного проще, но это не значит, что лучше. Вопрос удобства.

* Скачиваем (все зависимости включены в сборку): [библиотека (3.2 MB)](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar) | [md5](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar.md5)
* Теперь для использования библиотеки в проекте, нужно всего лишь добавить её в `classpath`:
  * Если компилируете через терминал, то команда будет выглядеть следующим образом: 
  ```bash
  javac -cp "/root/vk-bot-java-sdk-0.1.3-jar-with-dependencies.jar" Bot.jar 
  ```
  * Если используете **IntelliJ IDEA**, то нужно зайти в **Project Structure...** | **Libraries**, нажать `+` и добавить скачанный файл в список библиотек:
  ![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen_2.png)
  
---
Готово. Библиотека подключена к вашему проекту и готова для использования.

## Подробное описание
[Подробное описание всех методов находится в документации](https://github.com/petersamokhin/vk-bot-java-sdk/wiki/%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F)
