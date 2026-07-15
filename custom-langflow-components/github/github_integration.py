import os
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import DropdownInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data


class GitHubIntegrationComponent(Component):
    """GitHub integration component for PR/issue management.

    Provides GitHub API operations for repository management.
    """

    display_name = "GitHub Integration"
    description = "GitHub API operations for PRs, issues, and repository management."
    icon = "github"
    name = "GitHubIntegration"

    inputs = [
        DropdownInput(
            name="operation",
            display_name="Operation",
            info="GitHub operation to perform",
            options=["create_pr", "create_issue", "list_prs", "list_issues", "get_repo_info"],
            value="list_prs",
            tool_mode=True,
        ),
        MessageTextInput(
            name="repo",
            display_name="Repository",
            info="Repository in format owner/repo",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="title",
            display_name="Title",
            info="PR or issue title",
            value="",
            tool_mode=True,
        ),
        MultilineInput(
            name="body",
            display_name="Body",
            info="PR or issue body/description",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="branch",
            display_name="Branch",
            info="Source branch for PR",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="base",
            display_name="Base Branch",
            info="Target branch for PR",
            value="main",
            tool_mode=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_github_operation",
        ),
    ]

    def execute_github_operation(self) -> Data:
        """Execute GitHub operation and return results."""
        operation = self.operation
        result_data: dict[str, Any] = {}

        token = os.getenv("GITHUB_TOKEN")
        if not token:
            result_data = {"error": "GITHUB_TOKEN environment variable not set"}
            return Data(data=result_data)

        try:
            import requests

            headers = {"Authorization": f"token {token}", "Accept": "application/vnd.github.v3+json"}

            if operation == "create_pr":
                if not self.repo or not self.title:
                    result_data = {"error": "repo and title required for create_pr"}
                else:
                    url = f"https://api.github.com/repos/{self.repo}/pulls"
                    payload = {"title": self.title, "head": self.branch, "base": self.base, "body": self.body}
                    response = requests.post(url, headers=headers, json=payload)
                    result_data = {"action": "create_pr", "status": response.status_code, "response": response.json()}

            elif operation == "create_issue":
                if not self.repo or not self.title:
                    result_data = {"error": "repo and title required for create_issue"}
                else:
                    url = f"https://api.github.com/repos/{self.repo}/issues"
                    payload = {"title": self.title, "body": self.body}
                    response = requests.post(url, headers=headers, json=payload)
                    result_data = {"action": "create_issue", "status": response.status_code, "response": response.json()}

            elif operation == "list_prs":
                if not self.repo:
                    result_data = {"error": "repo required for list_prs"}
                else:
                    url = f"https://api.github.com/repos/{self.repo}/pulls"
                    response = requests.get(url, headers=headers)
                    prs = response.json()
                    result_data = {
                        "action": "list_prs",
                        "count": len(prs),
                        "prs": [{"number": pr["number"], "title": pr["title"], "state": pr["state"]} for pr in prs],
                    }

            elif operation == "list_issues":
                if not self.repo:
                    result_data = {"error": "repo required for list_issues"}
                else:
                    url = f"https://api.github.com/repos/{self.repo}/issues"
                    response = requests.get(url, headers=headers)
                    issues = response.json()
                    result_data = {
                        "action": "list_issues",
                        "count": len(issues),
                        "issues": [{"number": i["number"], "title": i["title"], "state": i["state"]} for i in issues],
                    }

            elif operation == "get_repo_info":
                if not self.repo:
                    result_data = {"error": "repo required for get_repo_info"}
                else:
                    url = f"https://api.github.com/repos/{self.repo}"
                    response = requests.get(url, headers=headers)
                    repo = response.json()
                    result_data = {
                        "action": "get_repo_info",
                        "name": repo.get("full_name"),
                        "stars": repo.get("stargazers_count"),
                        "forks": repo.get("forks_count"),
                        "language": repo.get("language"),
                    }

        except Exception as e:
            result_data = {"error": f"GitHub operation failed: {e!s}"}

        return Data(data=result_data)