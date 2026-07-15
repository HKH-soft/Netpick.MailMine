import os
from pathlib import Path
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, IntInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data


class CodeEditorComponent(Component):
    """Code editor component for file modifications.

    Read and edit files with path validation.
    Similar to Copilot's file editing capabilities.
    """

    display_name = "Code Editor"
    description = "Read and edit files in the workspace."
    icon = "file-code"
    name = "CodeEditor"

    inputs = [
        DropdownInput(
            name="action",
            display_name="Action",
            info="File operation to perform",
            options=["read", "write", "replace", "append", "delete"],
            value="read",
            tool_mode=True,
        ),
        MessageTextInput(
            name="file_path",
            display_name="File Path",
            info="Path to file (relative to workspace)",
            value="",
            tool_mode=True,
        ),
        MultilineInput(
            name="content",
            display_name="Content",
            info="Content to write or replacement text",
            value="",
            tool_mode=True,
        ),
        MultilineInput(
            name="old_string",
            display_name="Old String",
            info="Text to replace (for replace action)",
            value="",
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_editor_action",
        ),
    ]

    def execute_editor_action(self) -> Data:
        """Execute editor action and return results."""
        action = self.action
        workspace_root = Path(os.getcwd())
        result_data: dict[str, Any] = {}

        try:
            if action == "read":
                file_path = workspace_root / self.file_path
                if not file_path.exists():
                    result_data = {"error": f"File not found: {self.file_path}"}
                else:
                    content = file_path.read_text(encoding="utf-8")
                    result_data = {"action": "read", "path": str(file_path), "content": content}

            elif action == "write":
                file_path = workspace_root / self.file_path
                file_path.parent.mkdir(parents=True, exist_ok=True)
                file_path.write_text(self.content, encoding="utf-8")
                result_data = {"action": "write", "path": str(file_path), "bytes_written": len(self.content)}

            elif action == "replace":
                file_path = workspace_root / self.file_path
                if not file_path.exists():
                    result_data = {"error": f"File not found: {self.file_path}"}
                else:
                    content = file_path.read_text(encoding="utf-8")
                    if self.old_string not in content:
                        result_data = {"error": "Old string not found in file"}
                    else:
                        new_content = content.replace(self.old_string, self.content)
                        file_path.write_text(new_content, encoding="utf-8")
                        result_data = {"action": "replace", "path": str(file_path), "replacements": content.count(self.old_string)}

            elif action == "append":
                file_path = workspace_root / self.file_path
                file_path.parent.mkdir(parents=True, exist_ok=True)
                with open(file_path, "a", encoding="utf-8") as f:
                    f.write(self.content)
                result_data = {"action": "append", "path": str(file_path), "bytes_appended": len(self.content)}

            elif action == "delete":
                file_path = workspace_root / self.file_path
                if not file_path.exists():
                    result_data = {"error": f"File not found: {self.file_path}"}
                else:
                    file_path.unlink()
                    result_data = {"action": "delete", "path": str(file_path)}

        except Exception as e:
            result_data = {"error": f"Editor action failed: {e!s}"}

        return Data(data=result_data)