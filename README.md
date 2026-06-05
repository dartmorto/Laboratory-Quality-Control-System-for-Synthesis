# Sample Detection Results

Единый учебный проект, который связывает backend управления экспериментами и модель детекции образца по изображению.

В репозитории оставлено только нужное для задачи:

- Java backend: доменные классы, менеджер коллекций, CLI-команды, пользователи и валидация;
- JavaFX UI: журнал экспериментов, запусков, результатов и экран детекции образца;
- Python inference: архитектура модели, препроцессинг, CLI-предиктор и один checkpoint;
- не перенесены PostgreSQL-черновики, IDE-файлы, обучающие датасеты, изображения, отчеты и скрипты обучения.

## Структура

```text
src/main/java/             Java backend, CLI и JavaFX UI
src/main/resources/ui/     CSS тема JavaFX
ml/sample_detection/       код модели и препроцессинга
ml/checkpoints/            сохраненная модель
scripts/                   удобные команды запуска
```

## Python окружение

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r ml\requirements.txt
```

Если Python лежит не в PATH, его можно указать в поле `Python` в JavaFX окне или через переменную:

```powershell
$env:SAMPLE_DETECTION_PYTHON = "C:\path\to\python.exe"
```

## Запуск JavaFX UI

```powershell
.\scripts\run-ui.ps1
```

Или напрямую через Maven:

```powershell
mvn -q javafx:run
```

## Проверка модели напрямую

```powershell
.\.venv\Scripts\python.exe ml\predict.py --image C:\path\to\sample.jpg
```

## Запуск CLI backend

```powershell
mvn -q compile
java -cp target\classes Main
```

Основной CLI-сценарий:

```text
register
login
create_experiment
create_run
detect_sample <run_id> <path_to_image>
res_show <result_id>
```

Команда `detect_sample` и JavaFX экран детекции вызывают Python-модель, печатают/показывают результат детекции образца и сохраняют уверенность модели как результат запуска с параметром `SAMPLE_DETECTION_CONFIDENCE`.
