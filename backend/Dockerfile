FROM python:3.12-slim AS builder

RUN pip install --no-cache-dir poetry==2.1.3

WORKDIR /app

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.in-project true && \
    poetry install --only main --no-root --no-interaction

COPY cait_backend/ cait_backend/

RUN poetry install --only main --no-interaction

# ---------------------------------------------------------------------------

FROM python:3.12-slim

WORKDIR /app

COPY --from=builder /app/.venv .venv
COPY --from=builder /app/cait_backend cait_backend

ENV PATH="/app/.venv/bin:$PATH"

EXPOSE 8000

CMD ["uvicorn", "cait_backend.server:app", "--host", "0.0.0.0", "--port", "8000"]
