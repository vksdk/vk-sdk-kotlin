# VK Bot Java SDK
![Cover](https://petersamokhin.com/files/vk-java-bot-sdk/cover.png)
###### Удобная и простая библиотека, помогающая легко и быстро создать бота для ВКонтакте https://vk.com/vkbotsdk

---

С помощью данной библиотеки можно довольно просто взаимодействовать с [VK API](https://vk.com/dev/manuals) для создания ботов и не только. 
Функционал прекрасно подходит как для сообществ, так и для личных страниц.

## Пример

![Example](https://petersamokhin.com/files/vk-java-bot-sdk/git_screen.png)

Реализуем возможность получать уведомления только о сообщениях нужного типа:
```java
Group group = new Group(151083290, "access_token");

group.longPoll().listen(new Callback() {
    
    @Override
    public void onSimpleTextMessage(Message msg) {

        new Message()
            .from(group)
            .to(msg.authorId())
            .text("Что-то скучновато буковки читать. Картинку кинь лучше.")
            .send();
    }

    @Override
    public void onPhotoMessage(Message msg) {

        new Message()
            .from(group)
            .to(msg.authorId())
            .text("Уже лучше. Но я тоже так могу. Что дальше?")
            .photo("/Users/PeterSamokhin/Desktop/topoviy_mem.png");
            .send();
    }

    @Override
    public void onVoiceMessage(Message msg) {

        new Message()
            .from(group)
            .to(msg.authorId())
            .text("Не охота мне голосовые твои слушать.")
            .doc("https://vk.com/doc62802565_447117479")
            .send();
    }
});
```

## Функционал: версия 0.0.1 (30.07.2017) 

* Работа с личными сообщениями сообществ и личных страниц — необходим только [access_token](https://vk.com/dev/access_token).
* Возможность обработки сообщений только нужного типа (голосовые, простые текстовые, со стикером, и так далее)
* Возможность реагировать на то, что пользователь начал печатать.
* Упрощенное взаимодействие с VK API:
```java
// Так
group.api().call("users.get", "user_ids", 62802565, "fields", "photo_max_orig");
        
// Или так
group.api().call("users.get", "user_ids=62802565&fields=photo_max_orig");
        
// Или вот так
Map<String, Object> params = new HashMap<>();
params.put("user_ids", 62802565);
params.put("fields", "photo_max_orig");
        
group.api().call("users.get", params);
```
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
group.uploadCover("https://www.petersamokhin.com/files/vk-java-bot-sdk/cover.png");

// В эту же группу, если ID не был указан при инициализации (используем access_token)
group.uploadCover(151083290, "https://www.petersamokhin.com/files/vk-java-bot-sdk/cover.png");

// В какую-то другую группу, если знаем её ID и access_token
group.uploadCover(151083290, "access_token", "https://www.petersamokhin.com/files/vk-java-bot-sdk/cover.png");
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
* Скачиваем: [библиотека (28.8 kB)](https://www.petersamokhin.com/files/vk-java-bot-sdk/vk-java-bot-sdk-0.0.1.jar) | [md5](https://www.petersamokhin.com/files/vk-java-bot-sdk/vk-java-bot-sdk-0.0.1.jar.md5)
* Файл назван `vk-java-bot-sdk-0.0.1.jar`. Сохраняем его в любую папку, нам нужен лишь путь до файла. Например: `/root/vk-java-bot-sdk-0.0.1.jar`
* Пишем в консоль (поменять здесь нужно только путь до файла): 
```bash
mvn install:install-file -Dfile=/root/vk-java-bot-sdk-0.0.1.jar -DgroupId=com.petersamokhin -DartifactId=vk-java-bot-sdk -Dversion=0.0.1 -Dpackaging=jar
```
Готово. Библиотека добавлена в локальный репозиторий. Теперь подключим её к проекту.
* Для **maven** — пишем в **pom.xml**:
```xml
<depedencies>
...
<dependency>
    <groupId>com.petersamokhin</groupId>
    <artifactId>vk-java-bot-sdk</artifactId>
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
* Затем чуть ниже в `depedencies` добавьте `compile 'com.petersamokhin:vk-java-bot-sdk:0.0.1'`:
```
depedencies {
    compile 'com.petersamokhin:vk-java-bot-sdk:0.0.1'
}
```

Все остальные нужные зависимости сами будут подгружены автоматически ([com.squareup.okhttp3](https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp/3.8.1), [org.json](https://mvnrepository.com/artifact/org.json/json/20170516), [commons-io](https://mvnrepository.com/artifact/commons-io/commons-io/2.5), [org.apache.commons](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.6)). 

---
#### Без систем сборок (добавляем библиотеку в classpath)
Здесь немного проще, но это не значит, что лучше. Вопрос удобства.

* Скачиваем (все зависимости включены в сборку): [библиотека (1.3 MB)](https://petersamokhin.com/files/vk-java-bot-sdk-0.0.1-jar-with-dependencies.jar) | [md5](https://petersamokhin.com/files/vk-java-bot-sdk-0.0.1-jar-with-dependencies.jar.md5)
* Теперь для использования библиотеки в проекте, нужно всего лишь добавить её в `classpath`:
  * Если компилируете через терминал, то команда будет выглядеть следующим образом: 
  ```bash
  javac -cp "/root/vk-java-bot-sdk-0.0.1-jar-with-dependencies.jar" MyMainClass.jar 
  ```
  * Если используете **IntelliJ IDEA**, то нужно зайти в **Project Structure...** | **Libraries**, нажать `+` и добавить скачанный файл в список библиотек:
  ![Cover](https://petersamokhin.com/files/vk-java-bot-sdk/git_screen_2.png)
  
---
Готово. Библиотека подключена к вашему проекту и готова для использования.

## Подробное описание

### [Callbacks](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java)
* [onMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L19)
* [onPhotoMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L29)
* [onSimpleTextMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L121)
* [onVideoMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L39)
* [onAudioMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L49)
* [onDocMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L60)
* [onWallMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L70)
* [onStickerMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L80)
* [onLinkMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L90)
* [onVoiceMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L100)
* [onGifMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L111)
* [onTyping(Integer user_id)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L132)

По названию каждого метода, я думаю, более чем понятно, в каких условиях он будет вызван. К тому же, каждый метод подробно прокомментирован.<br><br>
**Доступны следующие типы сообщений** (_в порядке приоритета вызова_): `голосовое`, `стикер-сообщение`, `с гифкой`, `с аудио`, `с видео`, `с документом` (но для гифки свой коллбэк), `сообщение с прикрепленной записью со стены`, `сообщение с прикрепленной ссылкой`, `простое текстовое сообщение` (без прикреплений).<br><br>
Если нужно отловить несколько видов сообщений сразу, можно либо переопределить метод [onMessage(Message message)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/callbacks/Callback.java#L19), который будет вызван при каждом сообщении, либо вызвать метод [message.getCountOfAttachmentsByType()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L502), который вернёт `Map<String, Integer>`, содержащую количество прикреплений каждого типа.<br>
По названию каждого метода, я думаю, более чем понятно, в каких условиях он будет вызван. К тому же, каждый метод подробно прокомментирован. <br>
Параметр `message` — входящее сообщение, из которого можно вытащить все нужные параметры.<br>

Коллбэк `onTyping` будет вызван ~ каждые 5 секунд после того, как пользователь начал печатать и до того, как он закончит.

### Clients

* [public abstract class Client](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#L10) Обобщённый клиент для взаимодействия с ВК
  * [Client(String access_token)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#L21) Базовый конструктор, позволяющий производить почти все действия.
  * [Client(Integer id, String access_token)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#35) Расширенный конструктор. Если при инициализации клиента группы не указать ID, то его необходимо будет указать при вызове метода для загрузки обложки.
  * [public void setLongPoll(LongPoll LP)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#L47) Метод позволяет установить свои параметры для LongPoll сервера, не обязателен
  * [public LongPoll longPoll()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#L55) Метод возвращает экземпляр класса для взаимодействия с LongPoll сервером, прослушивания событий и изменения параметров
  * [public API api()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Client.java#L62) Метод возвращает экземпляр класса для взаимодействия с VK API, access_token берётся у текущего клиента.
  * геттеры и сеттеры для `access_token` и `id` клиента.
* [public class Group extends Client](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Group.java#L22) Клиент для взаимодействия с ВК от имени сообщества
  * [public boolean isMember(Object id)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Group.java#L48) Метод возвращает true, если пользователь с переданным id состоит в группе, от имени которой вызвается метод.
  * [public JSONObject uploadCover(Object... params)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/Group.java#L57) Метод позволяет загрузить обложку в сообщество по ссылке или из файла с диска. Возвращает ответ от сервера ВК.
* [public class User extends Client](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/clients/User.java#L8) Клиент для взаимодействия с ВК от имени пользователя

### LongPoll

* [public class LongPoll](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L14) Класс для взаимодействия с longpoll сервером, [больше тут](https://vk.com/dev/using_longpoll)
  * [public LongPoll(String access_token)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L42) Базовый конструктор для работы с longpoll сервером, использующим дефолтные параметры
  * [public LongPoll(String access_token, Integer need_pts, Integer version, Double API, Integer wait, Integer mode)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L58) Расширенный конструктор, позволяющий установить все необходимые параметры
  * [public void off()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L68) Метод позволяет отключить прослушку событий longpoll сервера, и затем установить новый longpoll сервер клиенту, если необходимо поменять параметры, или для чего-то ещё.
  * [private void setData(String access_token, Integer need_pts, Integer version, Double API, Integer wait, Integer mode)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L82) Метод устаналивает все параметры перед запросом к API VK для получения списка событий
  * [private GetLongPollServerResponse getLongPollServer(String access_token)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L105) Метод возвращает список событий longpoll сервера
  * [public void listen(Callback callback)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/LongPoll.java#L126) Метод вызывает у передаваемого коллбэка методы в зависимости от полученного списка событий
* [public class GetLongPollServerResponse](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/longpoll/responses/GetLongPollServerResponse.java#L6) Десериализованный ответ от longpoll сервера

### Message

* [public class Message](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L30) Класс для работы с сообщениями, входящими и исходящими
  * [public Message()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L49) Пустой конструктор, не устанавливающий по дефолту никаких параметров. Используется для сообщений, которые будут отправлены. 
  * [public Message(String accessToken, Integer messageId, Integer flags, Integer peerId, Integer timestamp, String text, JSONObject attachments, Integer randomId)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L55) Конструктор для создания объекта входящего сообщения. Не стоит использовать его для создания сообщения, которое будет отправлено, также не стоит использовать входящий параметр `message` у коллбэков.
  * [public Message from(Client client)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L71) Создаём объект сообщения, которое будет отправлено. Устанавливаем клиента, от имени которого будет отправлено сообщение, у него будет взят `access_token`. Обязателен для вызова.
  * [public Message to(Integer peerId)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L81) Создаём объект сообщения, которое будет отправлено. Устанавливаем id диалога, в который будет отправлено сообщение: положительный для пользователей и отрицательный для сообществ. Обязателен для вызова.
  * [public Message sticker(Integer id)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L89) Указываем id стикера, который нужно отправить в сообщении. Если есть этот параметр, все остальные параметры (текст, прикепления) будут проигнорированы.
  * [public Message forwardedMessages(Object... ids)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L97) Создаём объект сообщения, которое будет отправлено. Указываем (через запятую) id сообщений, которые нужно переслать.
  * [public Message text(Object text)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L113) Создаём объект сообщения, которое будет отправлено. Указываем текст сообщения.
  * [public Message title(Object title)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L121) Создаём объект сообщения, которое будет отправлено. Указываем заголовок сообщения (полужирный текст).
  * [public Message attachments(String... attachments)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L129) Создаём объект сообщения, которое будет отправлено. Указываем прикрепления к сообщению: `photo1111_1111`, и т.д. через запятую, или в виде `photo1111_1111,doc1111_1112`.
  * [public Message randomId(Integer randomId)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L144) Указываем `random_id` для сообщения. Если не знаете, что это и для чего, нет необходимости использовать.
  * [public Message photo(String photo)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L154) Создаём объект сообщения, которое будет отправлено. Прикрепляем фото к сообщению по URL, с диска или уже загруженное, используя ссылку вида `photo{owner_id}_{id}`.
  * [public Message doc(String doc, String... type)](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L261) Создаём объект сообщения, которое будет отправлено. Прикрепляем документ к сообщению, параметры аналогично сообщению. Если грузить `.mp3` или `.ogg` файл и указать type `audio_message`, к сообщению будет прикреплено голосовое сообщение, а если type будет graffiti, то прикрепится граффити.
  * [public Integer send()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L378) Отправляем созданное сообщение. Обязательно перед этим должны быть вызваны методы `from()`, `to()` и хотя бы один из: `text()`, `photo()`, `doc()` и так далее, иначе сообщение не будет отправлено. Метод возвращает `id` отправленного сообщения.
  * [public String messageType()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L411) Метод возвращает тип сообщения, нужен для вызова коллбэков.
  * [public JSONArray getAttachments()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L440) Метод возвращает массив прикреплений полученного сообщения в виде `[photo62802565_456241137, photo111_111, doc100_500]`.
  * [public Map<String, Integer> getCountOfAttachmentsByType()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L502) Метод возвращает количество прикреплений (к полученному сообщению) каждого типа.
  * [public JSONObject getVoiceMessage()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L624) Метод возвращает объект голосового сообщения, если оно есть в сообщении.
  * [private String](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L658)[][getForwardedMessagesIds()](https://github.com/petersamokhin/vk-bot-java-sdk/blob/master/src/main/java/com/petersamokhin/bots/sdk/objects/Message.java#L658) Метод вовзращает массив id пересланных сообщений в виде `[214421,1244242,3124125]`.
  * А также куча геттеров, сеттеров, полей, проверок на тип сообщения, которые не нуждаются в особом представлении. Любой необходимый параметр сообщения можно получить, вызвав соответствующий метод.
