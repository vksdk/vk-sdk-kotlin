# VK Bot Java SDK
![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/cover.png)
###### Удобная и простая библиотека, помогающая легко и быстро создать бота для ВКонтакте https://vk.com/vkbotsdk

---

С помощью данной библиотеки можно довольно просто взаимодействовать с [VK API](https://vk.com/dev/manuals) для создания ботов и не только. 
Функционал прекрасно подходит как для сообществ, так и для личных страниц.

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
На данный момент библиотека находится на стадии альфа-тестирования, и, пока она не будет достаточно хорошо отлажена и протестирована, загружать её в центральный репозиторий `maven` я не стану. Но и без этого подключение библиотеки к проекту не займёт и полминуты.

---

#### С помощью maven и gradle
Поскольку в `maven central` библиотека ещё не загружена, нужно собственноручно добавить её в локальный репозиторий. Для этого необходимо всего лишь скачать jar-файл и прописать одну команду в терминале (для `macOS` и `linux`):
* Скачиваем: [библиотека (28.8 kB)](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.0.1.jar) | [md5](https://www.petersamokhin.com/files/vk-bot-java-sdk/vk-bot-java-sdk-0.0.1.jar.md5)
* Файл назван `vk-bot-java-sdk-0.0.1.jar`. Сохраняем его в любую папку, нам нужен лишь путь до файла. Например: `/root/vk-bot-java-sdk-0.0.1.jar`
* Пишем в консоль (поменять здесь нужно только путь до файла): 
```bash
mvn install:install-file -Dfile=/root/vk-bot-java-sdk-0.0.1.jar -DgroupId=com.petersamokhin -DartifactId=vk-bot-java-sdk -Dversion=0.0.1 -Dpackaging=jar
```
Готово. Библиотека добавлена в локальный репозиторий. Теперь подключим её к проекту.
* Для **maven** — пишем в **pom.xml**:
```xml
<depedencies>
...
<dependency>
    <groupId>com.petersamokhin</groupId>
    <artifactId>vk-bot-java-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
...
</depedencies>
```

* Для **gradle** — в **build.gradle** на уровне проекта добавьте `mavenLocal()` в `repositories`:
```
repositories {
    mavenCentral()
    mavenLocal()
}
```
* Затем чуть ниже в `depedencies` добавьте `compile 'com.petersamokhin:vk-bot-java-sdk:0.0.1'`:
```
depedencies {
    compile 'com.petersamokhin:vk-bot-java-sdk:0.0.1'
}
```

Все остальные нужные зависимости сами будут подгружены автоматически ([com.squareup.okhttp3](https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp/3.8.1), [org.json](https://mvnrepository.com/artifact/org.json/json/20170516), [commons-io](https://mvnrepository.com/artifact/commons-io/commons-io/2.5), [org.apache.commons](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.6)). 

---
#### Без систем сборок (добавляем библиотеку в classpath)
Здесь немного проще, но это не значит, что лучше. Вопрос удобства.

* Скачиваем (все зависимости включены в сборку): [библиотека (1.3 MB)](https://petersamokhin.com/files/vk-bot-java-sdk-0.0.1-jar-with-dependencies.jar) | [md5](https://petersamokhin.com/files/vk-bot-java-sdk-0.0.1-jar-with-dependencies.jar.md5)
* Теперь для использования библиотеки в проекте, нужно всего лишь добавить её в `classpath`:
  * Если компилируете через терминал, то команда будет выглядеть следующим образом: 
  ```bash
  javac -cp "/root/vk-bot-java-sdk-0.0.1-jar-with-dependencies.jar" MyMainClass.jar 
  ```
  * Если используете **IntelliJ IDEA**, то нужно зайти в **Project Structure...** | **Libraries**, нажать `+` и добавить скачанный файл в список библиотек:
  ![Cover](https://petersamokhin.com/files/vk-bot-java-sdk/git_screen_2.png)
  
---
Готово. Библиотека подключена к вашему проекту и готова для использования.

## Подробное описание
[Подробное описание всех методов находится в документации](https://github.com/petersamokhin/vk-bot-java-sdk/wiki/%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F)
