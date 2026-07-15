import json
import os
from datetime import datetime
from pathlib import Path
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, IntInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data


class WorkspaceMemoryComponent(Component):
    """Workspace memory component for persistent context.

    Stores and retrieves project context, preferences, and history.
    """

    display_name = "Workspace Memory"
    description = "Persistent workspace context and memory management."
    icon = "database"
    name = "WorkspaceMemory"

    inputs = [
        DropdownInput(
            name="action",
            display_name="Action",
            info="Memory operation to perform",
            options=["store", "retrieve", "list_keys", "delete", "clear"],
            value="retrieve",
            tool_mode=True,
        ),
        MessageTextInput(
            name="key",
            display_name="Key",
            info="Memory key for store/retrieve/delete",
            value="",
            tool_mode=True,
        ),
        MultilineInput(
            name="value",
            display_name="Value",
            info="Value to store (JSON or text)",
            value="",
            tool_mode=True,
        ),
        IntInput(
            name="max_items",
            display_name="Max Items",
            info="Maximum items to return for list_keys",
            value=50,
            advanced=True,
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_memory_action",
        ),
    ]

    def _get_memory_file(self) -> Path:
        """Get memory storage file path."""
        workspace_root = Path.cwd()
        memory_dir = workspace_root / ".copilot_memory"
        memory_dir.mkdir(exist_ok=True)
        return memory_dir / "workspace_memory.json"

    def execute_memory_action(self) -> Data:
        """Execute memory action and return results."""
        action = self.action
        result_data: dict[str, Any] = {}

        try:
            memory_file = self._get_memory_file()

            # Load existing memory
            if memory_file.exists():
                memory = json.loads(memory_file.read_text(encoding="utf-8"))
            else:
                memory = {}

            if action == "store":
                if not self.key:
                    result_data = {"error": "key required for store"}
                else:
                    try:
                        # Try to parse as JSON
                        parsed_value = json.loads(self.value)
                    except json.JSONDecodeError:
                        parsed_value = self.value

                    memory[self.key] = {
                        "value": parsed_value,
                        "timestamp": datetime.now().isoformat(),
                        "type": "json" if isinstance(parsed_value, (dict, list)) else "text",
                    }
                    memory_file.write_text(json.dumps(memory, indent=2), encoding="utf-8")
                    result_data = {"action": "store", "key": self.key, "success": True}

            elif action == "retrieve":
                if not self.key:
                    result_data = {"error": "key required for retrieve"}
                else:
                    if self.key in memory:
                        result_data = {"action": "retrieve", "key": self.key, "value": memory[self.key]["value"]}
                    else:
                        result_data = {"action": "retrieve", "key": self.key, "value": None, "found": False}

            elif action == "list_keys":
                keys = list(memory.keys())[: self.max_items]
                result_data = {"action": "list_keys", "keys": keys, "count": len(keys)}

            elif action == "delete":
                if not self.key:
                    result_data = {"error": "key required for delete"}
                elif self.key in memory:
                    del memory[self.key]
                    memory_file.write_text(json.dumps(memory, indent=2), encoding="utf-8")
                    result_data = {"action": "delete", "key": self.key, "success": True}
                else:
                    result_data = {"action": "delete", "key": self.key, "found": False}

            elif action == "clear":
                memory_file.unlink(missing_ok=True)
                result_data = {"action": "clear", "success": True}

        except Exception as e:
            result_data = {"error": f"Memory operation failed: {e!s}"}

        return Data(data=result_data)