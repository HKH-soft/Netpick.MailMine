from pathlib import Path

from lfx.custom.custom_component.component import Component
from lfx.io import BoolInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data


class CodeEditorComponent(Component):
    """Code file editing component for agents.

    Provides file read/write operations for code editing workflows.
    """

    display_name = "Code Editor"
    description = "Read and edit code files."
    icon = "file-code"
    name = "CodeEditor"

    inputs = [
        MessageTextInput(
            name="file_path",
            display_name="File Path",
            info="Path to the file to edit (relative to workspace)",
            tool_mode=True,
            required=True,
        ),
        MultilineInput(
            name="content",
            display_name="Content",
            info="New content to write (leave empty to read)",
            value="",
            tool_mode=True,
        ),
        BoolInput(
            name="replace",
            display_name="Replace",
            info="Replace entire file (false = append)",
            value=True,
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="edit_file",
        ),
    ]

    def edit_file(self) -> Data:
        """Read or edit file and return results."""
        path = Path(self.file_path)

        # Security: prevent path traversal
        try:
            path = path.resolve()
            workspace = Path.cwd().resolve()
            if not str(path).startswith(str(workspace)):
                return Data(data={"error": "File path must be within workspace"})
        except Exception as e:
            return Data(data={"error": f"Invalid path: {e!s}"})

        # Read mode
        if not self.content:
            if not path.exists():
                return Data(data={"error": f"File not found: {path}"})
            return Data(
                data={
                    "action": "read",
                    "path": str(path),
                    "content": path.read_text(),
                }
            )

        # Write mode
        try:
            if self.replace:
                path.write_text(self.content)
                action = "replaced"
            else:
                path.write_text(path.read_text() + "\n" + self.content)
                action = "appended"

            return Data(
                data={
                    "action": action,
                    "path": str(path),
                    "bytes_written": len(self.content),
                }
            )
        except Exception as e:
            return Data(data={"error": f"Write error: {e!s}"})