# Запуск в Eclipse

Проект Maven, поэтому в Eclipse он импортируется как есть. Нужны Eclipse IDE for Java EE Developers
(или любой Eclipse со встроенным m2e) и JDK 17 или новее. Версия ниже не подойдёт: в `pom.xml` указана Java 17.

## 1. Импорт

1. Скачайте репозиторий (`git clone` или кнопка Code, Download ZIP на GitHub и распаковать).
2. File, Import, Maven, **Existing Maven Projects**, Next.
3. В поле Root Directory укажите папку проекта (где лежит `pom.xml`), Finish.
4. Подождите, пока внизу справа закончится «Building workspace» и загрузка зависимостей.
5. Если у проекта красный крестик из-за версии Java: правый щелчок по проекту, Properties, Java Build Path,
   Libraries, JRE System Library, Edit, выберите JDK 17 или новее.

## 2. Запуск всего приложения (окно Swing и сайт)

1. Откройте в Package Explorer файл `src/main/java/edu/java/issues/Launcher.java`.
2. Правый щелчок, Run As, **Java Application**.
3. Появится окно «Картотека выдач», в консоли Eclipse напишет «Сайт запущен: http://localhost:8080/».
4. Откройте в браузере http://localhost:8080/ (HTTPS: https://localhost:8443/).

Рабочий каталог запуска должен быть корнем проекта (так по умолчанию): оттуда читаются `data`, `img`, `conf`
и `src/main/webapp`. Если окно открылось без иконок или сайт отдаёт 404, проверьте: Run, Run Configurations,
Java Application, Launcher, вкладка Arguments, Working directory: Default (корень проекта).

Только сайт без окна: в той же конфигурации на вкладке Arguments в Program arguments напишите `--no-gui`.
Другой порт: в VM arguments напишите `-Dhttp.port=9090`.

Остановить: красный квадрат в панели Console.

## 3. Запуск отдельных лабораторных

| Что запустить | Как |
|---|---|
| ООП 1, консоль | Открыть `lab1/ArraySort.java`, Run As, Java Application. Результат в Console |
| ООП 2-8, 10 | Окно Swing запускается через `Launcher` (пункт 2). Меню и кнопки описаны в [LABS.md](LABS.md) |
| ООП 9 и Web 9, тесты | Правый щелчок по папке `src/test/java`, Run As, **JUnit Test**. Во вкладке JUnit должно быть 28 зелёных тестов |
| Один тестовый класс | Открыть, например, `IssueServletTest.java`, Run As, JUnit Test |
| Web 3-8, сайт | Запустить `Launcher` (пункт 2) и открыть адрес в браузере |
| Web 2, сборка war | Правый щелчок по проекту, Run As, Maven build, в Goals написать `package`, Run. Файл `target/issues.war` |
| Javadoc | Run As, Maven build, Goals: `javadoc:javadoc`. Результат в `target/reports/apidocs/index.html` |
| Пересборка GWT | Maven build, Goals: `-Pgwt net.ltgt.gwt.maven:gwt-maven-plugin:1.1.0:compile`, добавьте параметр `skipEcj` со значением `true` |

## 4. Деплой в отдельный Tomcat (как в методичке Web 2)

Приложение запускает свой встроенный Tomcat, поэтому отдельный Tomcat для проверки не нужен. Но если по методичке надо
разместить war в Tomcat 10.1 (Jakarta EE 10):

1. Собрать `package` (см. таблицу), получится `target/issues.war`.
2. Скопировать его в каталог `webapps` вашего Tomcat 10.1 и запустить Tomcat.
3. Открыть http://localhost:8080/issues/ (адрес совпадает с именем war).

В этом режиме окно Swing не открывается, а данные берутся из `data/issues.xml` не автоматически: список будет
пустым, пока вы не добавите записи на сайте. Полная версия работает через `Launcher`.
Tomcat 9 и старше не подойдёт: приложение использует пакеты `jakarta.servlet`.

## 5. Если что-то не работает

| Симптом | Причина и решение |
|---|---|
| Port 8080 was already in use | Порт занят другой программой. Закройте её или запустите с `-Dhttp.port=9090` |
| Русские буквы квадратиками в консоли Eclipse | Window, Preferences, General, Workspace, Text file encoding: UTF-8 |
| Ошибка `release version 17 not supported` | Eclipse использует старую Java. Выберите JDK 17+ (пункт 1.5) |
| Maven не скачивает библиотеки | Нужен интернет при первом импорте. Проверьте прокси в Window, Preferences, Maven, User Settings |
| Тесты красные только в Eclipse | Project, Clean, затем Maven, Update Project (Alt+F5) |
| Страница сайта показывает ошибку 500 про компиляцию JSP | Выполните Maven, Update Project и перезапустите `Launcher`; в зависимостях должен быть `ecj-3.43.0.jar` |
