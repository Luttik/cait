"""Google Drive tool — creates Markdown documents in the user's Drive."""

from __future__ import annotations

import os
from typing import Any

from langchain_core.tools import tool

_SCOPES = ["https://www.googleapis.com/auth/drive.file"]


def _build_drive_service() -> Any:
    """Build an authenticated Google Drive API v3 service client.

    Expects a ``GOOGLE_APPLICATION_CREDENTIALS`` env-var pointing at a
    service-account JSON key, **or** a ``credentials.json`` OAuth
    client-secrets file in the working directory with a cached
    ``token.json``.
    """
    from google.oauth2 import service_account
    from googleapiclient.discovery import build

    creds_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if creds_path:
        creds = service_account.Credentials.from_service_account_file(
            creds_path, scopes=_SCOPES
        )
    else:
        from google.auth.transport.requests import Request
        from google.oauth2.credentials import Credentials
        from google_auth_oauthlib.flow import InstalledAppFlow

        creds = None
        if os.path.exists("token.json"):
            creds = Credentials.from_authorized_user_file("token.json", _SCOPES)
        if not creds or not creds.valid:
            if creds and creds.expired and creds.refresh_token:
                creds.refresh(Request())
            else:
                flow = InstalledAppFlow.from_client_secrets_file(
                    "credentials.json", _SCOPES
                )
                creds = flow.run_local_server(port=0)
            with open("token.json", "w") as tok:
                tok.write(creds.to_json())

    return build("drive", "v3", credentials=creds)


@tool
def write_document(title: str, markdown_body: str) -> str:
    """Create a Markdown document on Google Drive.

    Args:
        title: The document title (used as the file name).
        markdown_body: The full Markdown content of the document.

    Returns:
        A confirmation message with the Google Drive file URL.
    """
    service = _build_drive_service()

    file_metadata: dict[str, str] = {
        "name": f"{title}.md",
        "mimeType": "text/markdown",
    }

    from googleapiclient.http import MediaInMemoryUpload

    media = MediaInMemoryUpload(
        markdown_body.encode("utf-8"),
        mimetype="text/markdown",
        resumable=False,
    )

    created = (
        service.files()
        .create(body=file_metadata, media_body=media, fields="id,webViewLink")
        .execute()
    )

    link = created.get(
        "webViewLink", f"https://drive.google.com/file/d/{created['id']}"
    )
    return f"Document '{title}' created successfully: {link}"
