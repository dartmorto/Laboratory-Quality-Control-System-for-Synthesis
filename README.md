# Sample Detection Results

Единый учебный проект, который связывает backend управления экспериментами и модель детекции образца по изображению.

В этот репозиторий перенесено только нужное для задачи:

- Java backend: доменные классы, менеджер коллекций, CLI-команды, пользователи и валидация;
- Python inference: архитектура модели, препроцессинг, CLI-предиктор и один лучший checkpoint;
- не перенесены JavaFX UI, PostgreSQL-черновики, IDE-файлы, обучающие датасеты, изображения, отчеты и скрипты обучения.

## Структура

```text
src/main/java/       Java backend и CLI
ml/sample_detection/ код модели и препроцессинга
ml/checkpoints/      сохраненная модель
scripts/             удобные команды запуска
```

## Python окружение

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r ml\requirements.txt
```

Если Python лежит не в PATH, укажите его для Java-команды:

```powershell
$env:SAMPLE_DETECTION_PYTHON = "C:\path\to\python.exe"
```

## Проверка модели напрямую

```powershell
.\.venv\Scripts\python.exe ml\predict.py --image C:\path\to\sample.jpg
```

## Запуск backend

```powershell
mvn -q compile
java -cp target\classes Main
```

Основной сценарий CLI:

```text
register
login
create_experiment
create_run
detect_sample <run_id> <path_to_image>
res_show <result_id>
```

Команда `detect_sample` вызывает Python-модель, печатает результат детекции образца и сохраняет уверенность модели как результат запуска с параметром `SAMPLE_DETECTION_CONFIDENCE`.
