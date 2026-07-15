import subprocess
import shlex
from pathlib import Path

from lfx.custom.custom_component.component import Component
from lfx.io import BoolInput, MultilineInput, Output
from lfx.schema.data import Data


class ShellCommandComponent(Component):
    """Shell command execution component for agents.

    Executes whitelisted shell commands with safety restrictions.
    Designed for Copilot-like code editing workflows.
    """

    display_name = "Shell Command"
    description = "Execute whitelisted shell commands safely."
    icon = "square-terminal"
    name = "ShellCommand"

    # Whitelisted commands for safety
    ALLOWED_COMMANDS = {
        "ls", "cat", "grep", "find", "pwd", "echo",
        "npm", "yarn", "pnpm", "node",
        "mvn", "gradle", "./mvnw", "./gradlew",
        "git", "python", "python3", "pip",
        "curl", "wget", "head", "tail",
        "mkdir", "touch", "cp", "mv"
    }

    inputs = [
        MultilineInput(
            name="command",
            display_name="Command",
            info="Shell command to execute (whitelist enforced)",
            tool_mode=True,
            required=True,
        ),
        BoolInput(
            name="safe_mode",
            display_name="Safe Mode",
            info="Enforce command whitelist",
            value=True,
            advanced=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_command",
        ),
    ]

    def execute_command(self) -> Data:
        """Execute the shell command and return results."""
        cmd_text = self.command.strip()
        if not cmd_text:
            return Data(data={"error": "No command provided"})

        # Parse command
        try:
            parts = shlex.split(cmd_text)
        except ValueError as e:
            return Data(data={"error": f"Invalid command syntax: {e}"})

        if not parts:
            return Data(data={"error": "Empty command"})

        base_cmd = parts[0]

        # Check whitelist if safe mode enabled
        if self.safe_mode:
            cmd_name = Path(base_cmd).name
            if cmd_name not in self.ALLOWED_COMMANDS:
                return Data(
                    data={
                        "error": f"Command '{cmd_name}' not in whitelist. Allowed: {', '.join(sorted(self.ALLOWED_COMMANDS))}",
                        "attempted": cmd_text,
                    }
                )

        try:
            result = subprocess.run(
                parts,
                capture_output=True,
                text=True,
                timeout=30,
                cwd=Path.cwd(),
            )
            return Data(
                data={
                    "stdout": result.stdout,
                    "stderr": result.stderr,
                    "returncode": result.returncode,
                    "success": result.returncode == 0,
                }
            )
        except subprocess.TimeoutExpired:
            return Data(data={"error": "Command timed out after 30 seconds"})
        except FileNotFoundError:
            return Data(data={"error": f"Command not found: {base_cmd}"})
        except Exception as e:
            return Data(data={"error": f"Execution error: {e!s}"})