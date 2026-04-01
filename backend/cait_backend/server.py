"""FastAPI server exposing the Cait agent over the AG-UI protocol."""

from __future__ import annotations

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

load_dotenv()

app = FastAPI(
    title="Cait Backend",
    description="AI car assistant — AG-UI endpoint powered by LangGraph",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def _mount_agent_endpoint() -> None:
    """Register the AG-UI LangGraph endpoint on the FastAPI app."""
    from ag_ui_langgraph import add_langgraph_fastapi_endpoint

    from cait_backend.agent import graph

    add_langgraph_fastapi_endpoint(app, graph, "/agent")


_mount_agent_endpoint()


@app.get("/health")
async def health() -> dict[str, str]:
    """Simple liveness probe."""
    return {"status": "ok"}
