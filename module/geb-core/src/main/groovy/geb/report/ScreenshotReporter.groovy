/* Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb.report

import geb.Browser
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriverException

/**
 * Writes the source of the browser's current page as html and takes a PNG screenshot
 * if the underlying driver implementation supports it.
 */
class ScreenshotReporter extends ReporterSupport {

    void writeReport(ReportState reportState) {
        // note - this is not covered by tests unless using a driver that can take screenshots
        def screenshotDriver = determineScreenshotDriver(reportState.browser)
        if (screenshotDriver) {
            def decoded
            try {
                decoded = screenshotDriver.getScreenshotAs(OutputType.BYTES)

                // WebDriver has a bug where sometimes the screenshot has been encoded twice
                if (!PngUtils.isPng(decoded)) {
                    decoded = Base64.mimeDecoder.decode(decoded)
                }
            } catch (WebDriverException e) {
                decoded = new ExceptionToPngConverter(e).convert('An exception has been thrown while getting the screenshot:')
            }

            def file = saveScreenshotPngBytes(reportState.outputDir, reportState.label, decoded)
            notifyListeners(reportState, [file])
        }
    }

    protected static TakesScreenshot determineScreenshotDriver(Browser browser) {
        if (browser.driver instanceof TakesScreenshot) {
            browser.driver as TakesScreenshot
        } else if (browser.augmentedDriver instanceof TakesScreenshot) {
            browser.augmentedDriver as TakesScreenshot
        } else {
            null
        }
    }

    protected File saveScreenshotPngBytes(File outputDir, String label, byte[] bytes) {
        def file = getFile(outputDir, label, 'png')
        file.withOutputStream { it << bytes }
        file
    }

}