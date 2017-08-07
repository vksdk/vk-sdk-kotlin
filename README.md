# VK Bot Java SDK
![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/cover.png)
###### Удобная и простая библиотека, помогающая легко и быстро создать бота для ВКонтакте https://vk.com/vkbotsdk

---

С помощью данной библиотеки можно довольно просто взаимодействовать с [VK API](https://vk.com/dev/manuals) для создания ботов и не только. 
Функционал прекрасно подходит как для сообществ, так и для личных страниц.

Последняя версия: [![vk-bot-java-sdk](https://img.shields.io/badge/maven--central-v0.1.0-blue.svg?style=flat)](https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk)

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
## Функционал: версия 0.1.0 (6.08.2017)
* [Старый функционал](https://github.com/petersamokhin/vk-bot-java-sdk#Функционал-версия-001-30072017) по возможности оптимизирован и протестирован
* Улучшено взаимодействие с VK API: теперь все запросы становятся в очередь и выполняются с помощью метода [execute](https://vk.com/dev/execute), позволяющего за одно выполнение метода делать до 25 запросов к API. Без него можно делать всего до трёх запросов в секунду. Ответ от VK вернётся через callback:
```java
// Постарался предусмотреть все возможные варианты вызова этого метода
// Данный вариант мне кажется самым удобным
// В виде списка параметров можно передавать Map, JSONObject, строку и так далее
group.api().call("users.get", "{user_ids:[1,2,3]}", response ->
    System.out.println("Response: " + response.toString())
);
```
* Добавлена возможность работать с [Callback API](https://vk.com/dev/callback_api) ВКонтакте:
```java
// Самый простой способ - все настройки по дефолту
// Указываем только путь для прослушки запросов
group.callbackApi("/callback").onGroupJoin(newSubscriber ->
    System.out.println("Новый подписчик: https://vk.com/id" + newSubscriber.getInt("user_id"))
);

// Или же настраиваем всё по-своему
// В данном случае настройка Callback API у сообщества будет проведена автоматически
// Параметры: 
// 1 - адрес сервера; 
// 2 - порт (если у вас установлен другой сервер, настройте переадресацию на нужный порт);
// 3 - путь, который будем слушать; отвечать ли "ok" серверу ВК сразу, или после выполнения всех действий по обработке запроса;
// 4 - настраивать ли автоматически получение уведомлений для вызываемых коллбэков
// Пример: вы вызвали onGroupJoin, и сразу же, если вы забыли в админке группы сами установить получение событий такого типа,
// Настройка проведётся автоматически
CallbackApiSettings settings = new CallbackApiSettings("https://petersamokhin.com", 80, "/callback", false, true);
group.setCallbackApiSettings(settings);
 
// Возвращён будет только object из ответа 
// (помимо него в ответе от ВК присутствует тип запроса и id группы)
group.onGroupJoin(newSubscriber ->
    System.out.println("Новый подписчик: https://vk.com/id" + newSubscriber.getInt("user_id"))
);
```
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

## Функционал: версия 0.0.1 (30.07.2017) 

* Работа с личными сообщениями сообществ и личных страниц — необходим только [access_token](https://vk.com/dev/access_token).
* Возможность обработки сообщений только нужного типа (голосовые, простые текстовые, со стикером, и так далее)
* Возможность реагировать на то, что пользователь начал печатать.
* Возможность прикрепить картинку/документ/etc по ссылке:
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

// В эту же группу, если ID не был указан при инициализации (используем access_token)
group.uploadCover(151083290, "https://www.petersamokhin.com/files/vk-bot-java-sdk/cover.png");

// В какую-то другую группу, если знаем её ID и access_token
group.uploadCover(151083290, "access_token", "https://www.petersamokhin.com/files/vk-bot-java-sdk/cover.png");
```
* Возможность как использовать настройки по умолчанию и написать бота в две строчки кода, так и возможность провести тонкую настройку, указать любой параметр, полностью управлять всем процессом и получать лог событий в консоль.
* Библиотека полностью и довольно подробно продукоментирована. В этом репозитории можно увидеть комментарии почти к каждому методу и каждому параметру, а также скомпилированы **javadoc**.

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
    <version>0.1.0</version>
</dependency>
```
#### Для gradle 
Добавить строки, что ниже, в **build.gradle** в dependencies:
```gradle
compile group: 'com.petersamokhin', name: 'vk-bot-java-sdk', version: '0.1.0'
```
### Любые другие системы сборок
Поскольку библиотека загружена в центральный репозиторий, на сайте поиска по репозиторию описаны способы подключения библиотеки с помощью любой из систем сборок: https://mvnrepository.com/artifact/com.petersamokhin/vk-bot-java-sdk/0.1.0

---
#### Без систем сборок (добавляем библиотеку в classpath)
Здесь немного проще, но это не значит, что лучше. Вопрос удобства.

* Скачиваем (все зависимости включены в сборку): [библиотека (3.8 MB)](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.0-jar-with-dependencies.jar) | [md5](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.1.0-jar-with-dependencies.jar.md5)
* Теперь для использования библиотеки в проекте, нужно всего лишь добавить её в `classpath`:
  * Если компилируете через терминал, то команда будет выглядеть следующим образом: 
  ```bash
  javac -cp "/root/vk-bot-java-sdk-0.1.0-jar-with-dependencies.jar" Bot.jar 
  ```
  * Если используете **IntelliJ IDEA**, то нужно зайти в **Project Structure...** | **Libraries**, нажать `+` и добавить скачанный файл в список библиотек:
  ![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen_2.png)
  
---
Готово. Библиотека подключена к вашему проекту и готова для использования.

## Подробное описание
[Подробное описание всех методов находится в документации](https://github.com/petersamokhin/vk-bot-java-sdk/wiki/%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F)
