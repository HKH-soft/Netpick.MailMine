# Custom Langflow Components - Copilot Replica

Custom components replicating GitHub Copilot Chat capabilities in Langflow.

## Components Created

### 1. Browser Automation (`browser/browser_automation.py`)
- **Actions**: navigate, click, type, screenshot, extract_text, extract_links, wait, evaluate
- **Features**: Playwright-based browser control, headless mode, element interaction
- **Copilot Equivalent**: Browser integration with Playwright

### 2. Terminal Command (`terminal/terminal_command.py`)
- **Actions**: Execute shell commands with whitelist
- **Safe Commands**: ls, cat, grep, find, pwd, echo, mkdir, touch, rm, cp, mv, git, npm, yarn, pip, python, node, java, mvn, gradle, docker, kubectl, curl, wget
- **Features**: Safe mode enforcement, timeout control, stdout/stderr capture
- **Copilot Equivalent**: Terminal integration

### 3. Git Operations (`git/git_operations.py`)
- **Operations**: status, diff, log, branch, checkout, add, commit, pull, push
- **Features**: Git command execution, argument support
- **Copilot Equivalent**: Git integration

### 4. Code Editor (`editor/code_editor.py`)
- **Actions**: read, write, replace, append, delete
- **Features**: Path traversal protection, workspace boundary enforcement
- **Copilot Equivalent**: File editing capabilities

### 5. Workspace Context (`workspace/workspace_context.py`)
- **Actions**: read_file, write_file, list_dir, search_files, get_structure
- **Features**: Directory tree, file search, workspace navigation
- **Copilot Equivalent**: Workspace awareness

### 6. Shell Command (`tools/shell_command.py`)
- **Purpose**: Execute whitelisted shell commands
- **Whitelist**: npm, yarn, pnpm, node, mvn, gradle, git, python, curl, wget, ls, cat, grep, find, etc.
- **Safety**: Safe mode enabled by default, prevents dangerous commands

### 7. Git Component (`tools/git_operations.py`)
- **Purpose**: Git operations (status, diff, log, branch, checkout, add, commit, pull, push)
- **Use**: Version control integration for agents

### 8. Code Editor (`tools/code_editor.py`)
- **Purpose**: Read and write code files
- **Safety**: Path traversal protection, workspace boundary enforcement
- **Modes**: Replace entire file or append

### 9. GitHub Integration (`github/github_integration.py`)
- **Operations**: create_pr, create_issue, list_prs, list_issues, get_repo_info
- **Features**: GitHub API via requests, GITHUB_TOKEN auth
- **Copilot Equivalent**: GitHub PR/issue creation

### 10. Workspace Memory (`memory/workspace_memory.py`)
- **Actions**: store, retrieve, list_keys, delete, clear
- **Features**: Persistent JSON storage in `.copilot_memory/workspace_memory.json`
- **Copilot Equivalent**: Workspace context memory

## Installation

```bash
# Install Playwright for browser automation
pip install playwright
playwright install chromium

# Install Langflow with custom components
# Copy components to Langflow's custom component directory
```

## Usage in Flow

1. Add custom components to Langflow via **Custom Component** node
2. Set `tool_mode=True` on components to use as agent tools
3. Connect to Agent components using `component_as_tool` output
4. Use CUGA component with `browser_enabled=True` for native browser support

## Flow Architecture

```
Chat Input → Orchestrator Agent → Chat Output
                    ├── Code Agent (tools: shell, git, editor)
                    ├── Research Agent (tools: web search, MCP)
                    └── Data Agent (tools: Python REPL)
```

## CUGA Integration

The CUGA component provides native browser automation:
- Set `browser_enabled=True` in CUGA parameters
- Configure `web_apps` with target URLs
- Requires: `uv run -m playwright install chromium`

## Security Notes

- Shell commands are whitelisted by default
- File operations restricted to workspace directory
- Git operations limited to safe subset