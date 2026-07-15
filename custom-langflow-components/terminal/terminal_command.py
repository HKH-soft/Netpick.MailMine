import subprocess
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import BoolInput, IntInput, MessageTextInput, Output
from lfx.schema.data import Data


class TerminalCommandComponent(Component):
    """Terminal command execution component.

    Executes shell commands with security controls.
    Similar to Copilot's terminal integration.
    """

    display_name = "Terminal Command"
    description = "Execute shell commands in the workspace."
    icon = "terminal"
    name = "TerminalCommand"

    inputs = [
        MessageTextInput(
            name="command",
            display_name="Command",
            info="Shell command to execute",
            value="",
            tool_mode=True,
        ),
        BoolInput(
            name="safe_mode",
            display_name="Safe Mode",
            info="Restrict to safe commands only",
            value=True,
            advanced=True,
        ),
        IntInput(
            name="timeout",
            display_name="Timeout (seconds)",
            info="Command timeout in seconds",
            value=30,
            advanced=True,
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_command",
        ),
    ]

    # Whitelist of safe commands
    SAFE_COMMANDS = {
        "ls", "dir", "pwd", "cd", "cat", "echo", "mkdir", "touch",
        "rm", "cp", "mv", "grep", "find", "head", "tail", "wc",
        "git", "npm", "yarn", "pip", "python", "node", "java",
        "mvn", "gradle", "docker", "kubectl", "curl", "wget",
    }

    def execute_command(self) -> Data:
        """Execute shell command and return results."""
        command = self.command.strip()
        result_data: dict[str, Any] = {}

        if not command:
            result_data = {"error": "No command provided"}
            return Data(data=result_data)

        # Security check
        if self.safe_mode:
            cmd_parts = command.split()
            base_cmd = cmd_parts[0] if cmd_parts else ""
            if base_cmd not in self.SAFE_COMMANDS:
                result_data = {"error": f"Command '{base_cmd}' not allowed in safe mode"}
                return Data(data=result_data)

        try:
            result = subprocess.run(
                command,
                shell=True,
                capture_output=True,
                text=True,
                timeout=self.timeout,
                cwd=None,
            )
            result_data = {
                "action": "execute_command",
                "command": command,
                "returncode": result.returncode,
                "stdout": result.stdout,
                "stderr": result.stderr,
            }
        except subprocess.TimeoutExpired:
            result_data = {"error": f"Command timed out after {self.timeout}s"}
        except Exception as e:
            result_data = {"error": f"Command execution failed: {e!s}"}

        return Data(data=result_data)