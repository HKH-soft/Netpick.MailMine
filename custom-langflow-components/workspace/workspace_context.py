import os
from pathlib import Path
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, IntInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data


class WorkspaceContextComponent(Component):
    """Workspace context component for file operations.

    Provides file read/write, directory listing, and workspace navigation.
    Similar to Copilot's file system access.
    """

    display_name = "Workspace Context"
    description = "Read and write files in the workspace."
    icon = "folder"
    name = "WorkspaceContext"

    inputs = [
        DropdownInput(
            name="action",
            display_name="Action",
            info="File operation to perform",
            options=["read_file", "write_file", "list_dir", "search_files", "get_structure"],
            value="read_file",
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
            info="Content to write to file",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="search_pattern",
            display_name="Search Pattern",
            info="Pattern to search for in files",
            value="",
            tool_mode=True,
        ),
        IntInput(
            name="max_depth",
            display_name="Max Depth",
            info="Maximum directory depth for listing",
            value=3,
            advanced=True,
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_workspace_action",
        ),
    ]

    def execute_workspace_action(self) -> Data:
        """Execute workspace action and return results."""
        action = self.action
        workspace_root = Path(os.getcwd())
        result_data = {}

        try:
            if action == "read_file":
                file_path = workspace_root / self.file_path
                if not file_path.exists():
                    result_data = {"error": f"File not found: {self.file_path}"}
                elif file_path.is_relative_to(workspace_root) or file_path == workspace_root:
                    content = file_path.read_text(encoding="utf-8")
                    result_data = {"action": "read_file", "path": str(file_path), "content": content}
                else:
                    result_data = {"error": "Access denied: path outside workspace"}

            elif action == "write_file":
                file_path = workspace_root / self.file_path
                if not str(file_path).startswith(str(workspace_root)):
                    result_data = {"error": "Access denied: path outside workspace"}
                else:
                    file_path.parent.mkdir(parents=True, exist_ok=True)
                    file_path.write_text(self.content, encoding="utf-8")
                    result_data = {"action": "write_file", "path": str(file_path), "bytes_written": len(self.content)}

            elif action == "list_dir":
                dir_path = workspace_root / self.file_path if self.file_path else workspace_root
                if not dir_path.exists():
                    result_data = {"error": f"Directory not found: {self.file_path}"}
                else:
                    items = []
                    for item in dir_path.iterdir():
                        items.append({"name": item.name, "type": "dir" if item.is_dir() else "file"})
                    result_data = {"action": "list_dir", "path": str(dir_path), "items": items}

            elif action == "search_files":
                matches = []
                for root, _, files in os.walk(workspace_root):
                    for file in files:
                        if self.search_pattern.lower() in file.lower():
                            matches.append(os.path.join(root, file))
                result_data = {"action": "search_files", "pattern": self.search_pattern, "matches": matches[:50]}

            elif action == "get_structure":
                structure = self._get_tree(workspace_root, self.max_depth)
                result_data = {"action": "get_structure", "structure": structure}

        except Exception as e:
            result_data = {"error": f"Workspace action failed: {e!s}"}

        return Data(data=result_data)

    def _get_tree(self, path: Path, max_depth: int) -> dict[str, Any]:
        """Get directory tree structure."""
        if max_depth < 0:
            return {}

        result = {"name": path.name, "type": "dir", "children": []}

        try:
            for item in sorted(path.iterdir()):
                if item.is_dir() and not item.name.startswith(".") and item.name not in ["node_modules", "__pycache__", ".git"]:
                    result["children"].append(self._get_tree(item, max_depth - 1))
                elif item.is_file():
                    result["children"].append({"name": item.name, "type": "file"})
        except PermissionError:
            pass

        return result