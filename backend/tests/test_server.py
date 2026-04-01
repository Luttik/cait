"""Tests for the FastAPI server."""

from __future__ import annotations

import pytest
from httpx import ASGITransport, AsyncClient

from cait_backend.server import app


@pytest.fixture
def anyio_backend() -> str:
    return "asyncio"


@pytest.mark.anyio
async def test_health_endpoint() -> None:
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "ok"}
