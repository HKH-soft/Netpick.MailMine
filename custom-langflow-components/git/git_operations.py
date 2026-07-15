import subprocess
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, MessageTextInput, Output
from lfx.schema.data import Data


class GitOperationsComponent(Component):
    """Git operations component.

    Execute git commands for version control.
    Similar to Copilot's git integration.
    """

    display_name = "Git Operations"
    description = "Execute git commands for version control."
    icon = "git-branch"
    name = "GitOperations"

    inputs = [
        DropdownInput(
            name="operation",
            display_name="Operation",
            info="Git operation to perform",
            options=["status", "diff", "log", "branch", "checkout", "add", "commit", "pull", "push"],
            value="status",
            tool_mode=True,
        ),
        MessageTextInput(
            name="branch_name",
            display_name="Branch Name",
            info="Branch name for checkout/create operations",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="commit_message",
            display_name="Commit Message",
            info="Message for commit operation",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="file_path",
            display_name="File Path",
            info="File path for add operation",
            value=".",
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_git_operation",
        ),
    ]

    def execute_git_operation(self) -> Data:
        """Execute git operation and return results."""
        operation = self.operation
        result_data: dict[str, Any] = {}

        git_commands = {
            "status": ["git", "status", "--short"],
            "diff": ["git", "diff"],
            "log": ["git", "log", "--oneline", "-10"],
            "branch": ["git", "branch", "-a"],
            "checkout": ["git", "checkout", self.branch_name] if self.branch_name else ["git", "checkout"],
            "add": ["git", "add", self.file_path],
            "commit": ["git", "commit", "-m", self.commit_message] if self.commit_message else ["git", "commit"],
            "pull": ["git", "pull"],
            "push": ["git", "push"],
        }

        cmd = git_commands.get(operation)
        if not cmd:
            result_data = {"error": f"Unknown operation: {operation}"}
            return Data(data=result_data)

        try:
            result = subprocess.run(cmd, capture_output=True, text=True, check=False)
            result_data = {
                "action": "git_operation",
                "operation": operation,
                "returncode": result.returncode,
                "stdout": result.stdout,
                "stderr": result.stderr,
            }
        except Exception as e:
            result_data = {"error": f"Git operation failed: {e!s}"}

        return Data(data=result_data)