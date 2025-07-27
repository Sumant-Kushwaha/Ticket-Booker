const selector =
  "body > app-root > app-home > div.header-fix > app-header > p-dialog.ng-tns-c19-2 > div > div > div.ng-tns-c19-2.ui-dialog-content.ui-widget-content > div > form > div.text-center.col-xs-12 > button";
function waitForElement(s, t = 10000) {
  return new Promise((r, j) => {
    let e = document.querySelector(s);
    if (e) return r(e);
    const o = new MutationObserver(() => {
      let e = document.querySelector(s);
      if (e) {
        o.disconnect();
        r(e);
      }
    });
    o.observe(document.body, { childList: true, subtree: true });
    setTimeout(() => {
      o.disconnect();
      j();
    }, t);
  });
}
async function autoClick() {
  try {
    let e = await waitForElement(selector, 15000);
    e.scrollIntoView({ behavior: "smooth", block: "center" });
    setTimeout(() => e.click(), 500);
    browser.runtime.sendNativeMessage("browser", {
      type: "automation_result",
      success: true,
      selector,
    });
  } catch {
    browser.runtime.sendNativeMessage("browser", {
      type: "automation_result",
      success: false,
      selector,
    });
  }
}
if (window.location.href.includes("irctc.co.in")) {
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () =>
      setTimeout(autoClick, 2000)
    );
  } else {
    setTimeout(autoClick, 2000);
  }
}
