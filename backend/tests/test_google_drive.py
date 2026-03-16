"""Tests for the write_document tool."""

from __future__ import annotations

from unittest.mock import MagicMock, patch

from cait_backend.tools.google_drive import write_document


@patch("cait_backend.tools.google_drive._build_drive_service")
def test_write_document_creates_file(mock_build: MagicMock) -> None:
    mock_service = MagicMock()
    mock_build.return_value = mock_service

    mock_create = mock_service.files.return_value.create.return_value
    mock_create.execute.return_value = {
        "id": "abc123",
        "webViewLink": "https://drive.google.com/file/d/abc123/view",
    }

    result = write_document.invoke(
        {"title": "Test Doc", "markdown_body": "# Hello\nWorld"}
    )

    assert "Test Doc" in result
    assert "https://drive.google.com/file/d/abc123/view" in result

    mock_service.files.return_value.create.assert_called_once()
    call_kwargs = mock_service.files.return_value.create.call_args
    assert call_kwargs[1]["body"]["name"] == "Test Doc.md"
    assert call_kwargs[1]["body"]["mimeType"] == "text/markdown"


@patch("cait_backend.tools.google_drive._build_drive_service")
def test_write_document_fallback_link(mock_build: MagicMock) -> None:
    mock_service = MagicMock()
    mock_build.return_value = mock_service

    mock_create = mock_service.files.return_value.create.return_value
    mock_create.execute.return_value = {"id": "xyz789"}

    result = write_document.invoke({"title": "Another", "markdown_body": "Content"})

    assert "https://drive.google.com/file/d/xyz789" in result
