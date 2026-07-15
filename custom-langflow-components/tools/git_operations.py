import subprocess
from pathlib import Path

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, MessageTextInput, Output
from lfx.schema.data import Data


class GitComponent(Component):
    """Git operations component for agents.

    Provides safe git operations for code workflows.
    """

    display_name = "Git Operations"
    description = "Execute git commands for version control."
    icon = "git-branch"
    name = "Git"

    ALLOWED_OPERATIONS = ["status", "diff", "log", "branch", "checkout", "add", "commit", "pull", "push"]

    inputs = [
        DropdownInput(
            name="operation",
            display_name="Operation",
            info="Git operation to execute",
            options=ALLOWED_OPERATIONS,
            value="status",
            tool_mode=True,
        ),
        MessageTextInput(
            name="args",
            display_name="Arguments",
            info="Additional arguments for the git command",
            value="",
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_git",
        ),
    ]

    def execute_git(self) -> Data:
        """Execute git operation and return results."""
        op = self.operation
        if op not in self.ALLOWED_OPERATIONS:
            return Data(data={"error": f"Operation '{op}' not allowed"})

        cmd = ["git", op]
        if self.args:
            cmd.extend(self.args.split())

        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=30,
                cwd=Path.cwd(),
            )
            return Data(
                data={
                    "output": result.stdout,
                    "error": result.stderr,
                    "returncode": result.returncode,
                    "success": result.returncode == 0,
                }
            )
        except subprocess.TimeoutExpired:
            return Data(data={"error": "Git command timed out"})
        except FileNotFoundError:
            return Data(data={"error": "Git not found. Ensure git is installed."})
        except Exception as e:
            return Data(data={"error": f"Git error: {e!s}"})