import asyncio
from typing import Any

from lfx.custom.custom_component.component import Component
from lfx.io import BoolInput, DropdownInput, IntInput, MessageTextInput, MultilineInput, Output
from lfx.schema.data import Data
from lfx.schema.message import Message


class BrowserAutomationComponent(Component):
    """Browser automation component using Playwright.

    Provides web scraping, navigation, and browser interaction capabilities.
    Similar to Copilot's browser integration.
    """

    display_name = "Browser Automation"
    description = "Control a web browser for scraping and automation."
    icon = "globe"
    name = "BrowserAutomation"

    inputs = [
        DropdownInput(
            name="action",
            display_name="Action",
            info="Browser action to perform",
            options=["navigate", "click", "type", "screenshot", "extract_text", "extract_links", "wait", "evaluate"],
            value="navigate",
            tool_mode=True,
        ),
        MessageTextInput(
            name="url",
            display_name="URL",
            info="URL to navigate to",
            value="",
            tool_mode=True,
        ),
        MessageTextInput(
            name="selector",
            display_name="CSS Selector",
            info="Element selector for click/type actions",
            value="",
            tool_mode=True,
        ),
        MultilineInput(
            name="text",
            display_name="Text",
            info="Text to type or JavaScript to evaluate",
            value="",
            tool_mode=True,
        ),
        IntInput(
            name="wait_time",
            display_name="Wait Time (ms)",
            info="Time to wait for element or page load",
            value=5000,
            advanced=True,
            tool_mode=True,
        ),
        BoolInput(
            name="headless",
            display_name="Headless Mode",
            info="Run browser without UI",
            value=True,
            advanced=True,
        ),
    ]

    outputs = [
        Output(
            display_name="Result",
            name="result",
            method="execute_browser_action",
        ),
    ]

    def execute_browser_action(self) -> Data:
        """Execute browser action and return results."""
        try:
            from playwright.sync_api import sync_playwright
        except ImportError:
            return Data(data={"error": "Playwright not installed. Run: pip install playwright && playwright install chromium"})

        action = self.action
        result_data = {}

        with sync_playwright() as p:
            browser = p.chromium.launch(headless=self.headless)
            page = browser.new_page()

            try:
                if action == "navigate":
                    page.goto(self.url, timeout=self.wait_time)
                    result_data = {"action": "navigate", "url": self.url, "status": page.status}

                elif action == "click":
                    page.goto(self.url, timeout=self.wait_time)
                    page.click(self.selector, timeout=self.wait_time)
                    result_data = {"action": "click", "selector": self.selector}

                elif action == "type":
                    page.goto(self.url, timeout=self.wait_time)
                    page.fill(self.selector, self.text)
                    result_data = {"action": "type", "selector": self.selector, "text_length": len(self.text)}

                elif action == "screenshot":
                    page.goto(self.url, timeout=self.wait_time)
                    screenshot = page.screenshot()
                    result_data = {"action": "screenshot", "url": self.url, "bytes": len(screenshot)}

                elif action == "extract_text":
                    page.goto(self.url, timeout=self.wait_time)
                    if self.selector:
                        text = page.text_content(self.selector)
                    else:
                        text = page.text_content("body")
                    result_data = {"action": "extract_text", "text": text}

                elif action == "extract_links":
                    page.goto(self.url, timeout=self.wait_time)
                    links = page.eval_on_selector_all("a", "elements => elements.map(e => e.href)")
                    result_data = {"action": "extract_links", "links": links}

                elif action == "wait":
                    page.wait_for_timeout(self.wait_time)
                    result_data = {"action": "wait", "time_ms": self.wait_time}

                elif action == "evaluate":
                    page.goto(self.url, timeout=self.wait_time)
                    result = page.eval_on_selector("body", self.text)
                    result_data = {"action": "evaluate", "result": str(result)}

            except Exception as e:
                result_data = {"error": f"Browser action failed: {e!s}"}
            finally:
                browser.close()

        return Data(data=result_data)