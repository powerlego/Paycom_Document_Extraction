package org.extract;

import com.opencsv.CSVWriter;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * @author Nicholas Curl
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please list the directory you want to save the checklist and employee photos: ");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy'\n'hh:mm:ss a");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Path downloadDirPath = null;
        try {
            downloadDirPath = Paths.get(scanner.nextLine()).toFile().getCanonicalFile().toPath();
        }
        catch (IOException canonical) {
            logger.fatal("Unable to get canonical path", canonical);
            System.exit(1);
        }
        if (!downloadDirPath.toFile().exists()) {
            try {
                Files.createDirectories(downloadDirPath);
            }
            catch (IOException download) {
                logger.fatal("Unable to create download directory", download);
                System.exit(1);
            }
        }
        String download_dir = downloadDirPath.toString();
        ChromeOptions chromeOptions = new ChromeOptions();
        JSONObject settings = new JSONObject(
                "{\n" +
                "   \"recentDestinations\": [\n" +
                "       {\n" +
                "           \"id\": \"Save as PDF\",\n" +
                "           \"origin\": \"local\",\n" +
                "           \"account\": \"\",\n" +
                "       }\n" +
                "   ],\n" +
                "   \"selectedDestinationId\": \"Save as PDF\",\n" +
                "   \"version\": 2\n" +
                "}");
        JSONObject prefs = new JSONObject(
                "{\n" +
                "   \"plugins.plugins_list\":\n" +
                "       [\n" +
                "           {\n" +
                "               \"enabled\": False,\n" +
                "               \"name\": \"Chrome PDF Viewer\"\n" +
                "          }\n" +
                "       ],\n" +
                "   \"download.extensions_to_open\": \"applications/pdf\"\n" +
                "}")
                .put("printing.print_preview_sticky_settings.appState", settings)
                .put("download.default_directory", download_dir);
        chromeOptions.setExperimentalOption("prefs", prefs);
        String url = "https://www.paycomonline.net/v4/cl/cl-login.php";
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(url);
        driver.manage().window().maximize();
        System.out.println("Please enter your client code: ");
        String clientCode = scanner.nextLine();
        System.out.println("Please enter your username: ");
        String userName = scanner.nextLine();
        System.out.println("Please enter your password: ");
        String pwd = scanner.nextLine();
        driver.findElement(By.id("clientcode")).sendKeys(clientCode);
        driver.findElement(By.id("txtlogin")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(pwd);
        driver.findElement(By.id("btnSubmit")).click();
        waitForLoad(driver);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/label")).getText());
        String firstQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/div/div/input")).sendKeys(firstQ);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/label")).getText());
        String secQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/div/div/input")).sendKeys(secQ);
        driver.findElement(By.xpath("//button[@name='continue']")).click();
        waitForLoad(driver);
        try {
            driver.findElement(By.id("HumanResources"));
        }
        catch (NoSuchElementException e) {
            logger.fatal("Wrong answers to your questions", e);
            driver.close();
            System.exit(1);
        }
        /*System.out.println("Are you extracting this checklist for the first time?");
        String ans = scanner.nextLine();
        String lastExec = "";
        if (ans.equalsIgnoreCase("no")) {
            System.out.println("Enter the last date you performed this Extraction in the format mmddyyyy: ");
            lastExec = scanner.nextLine();
        }*/
        WebElement elementToHoverOver = driver.findElement(By.id("HumanResources"));
        Actions hover = new Actions(driver).moveToElement(elementToHoverOver);
        hover.perform();
        waitUntilClickable(driver, By.id("DocumentsandChecklists"));
        waitUntilClickable(driver,
                           By.xpath("/html/body/div[3]/div/div[2]/div[1]/div[2]/div/div/div[2]/ul/li[2]/a/div[1]")
        );
        /*if (ans.equalsIgnoreCase("no")) {
            waitUntilClickable(driver,
                               By.xpath("/html/body/div[4]/div/form/div/div[6]/div[14]/div/div/div[1]/div/div[1]/input")
            );
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[6]/div[14]/div/div/div[1]/div/div[1]/input"))
                  .sendKeys("Hire date");
            waitForLoad(driver);
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[6]/div[14]/div/div/div[2]/select")).click();
            waitForLoad(driver);
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[6]/div[14]/div/div/div[2]/select"))
                  .sendKeys("is greater than or equal to");
            waitForLoad(driver);
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[6]/div[15]/div/div/div[1]/input[1]"))
                  .click();
            waitForLoad(driver);
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[6]/div[15]/div/div/div[1]/input[1]"))
                  .sendKeys(lastExec);
            waitForLoad(driver);
            driver.findElement(By.xpath("/html/body/div[4]/div/form/div/div[7]/button")).click();
            waitForLoad(driver);
        }*/
        List<WebElement> options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        waitForLoad(driver);
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitUntilClickable(driver,
                           By.xpath(
                                   "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[3]/table/tbody/tr[1]/td[2]/a")
        );
        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[2]/div[1]/div/div[2]/div[3]/a"));
        List<WebElement> employees;

        options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitForLoad(driver);
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[3]/table/tbody")));
        try {
            employees = driver.findElements(By.xpath(
                    "/html/body/div[4]/div/form/div/div[3]/div[3]/table/tbody/tr/td[2]/a"));
            waitUntilClickable(driver, employees.get(0));
        }
        catch (StaleElementReferenceException e) {
            employees = driver.findElements(By.xpath(
                    "/html/body/div[4]/div/form/div/div[3]/div[3]/table/tbody/tr/td[2]/a"));
            waitUntilClickable(driver, employees.get(5));
        }
        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[4]/div/div[2]/div[4]/div[2]/a"));
        List<List<String>> lists = new ArrayList<>();
        List<String> listRow = new ArrayList<>();
        listRow.add("Employee Name");
        listRow.add("Document Name");
        listRow.add("Employee Acknowledgement");
        listRow.add("Supervisor Acknowledgement");
        lists.add(listRow);
        ProgressBar progressBar = new ProgressBarBuilder().setUpdateIntervalMillis(1000)
                                                          .setTaskName("Downloading Documents")
                                                          .setInitialMax(employees.size())
                                                          .setMaxRenderedLength(120)
                                                          .setStyle(ProgressBarStyle.ASCII)
                                                          .build();
        for (int e_ = 0; e_ < employees.size(); e_++) {
            waitUntilVisible(driver,
                             By.xpath(
                                     "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[1]/div[3]/div/div[2]/div/label/select")
            );
            options = driver.findElements(By.xpath(
                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[1]/div[3]/div/div[2]/div/label/select/option"));
            for (WebElement option : options) {
                if (option.getText().equalsIgnoreCase("500")) {
                    waitUntilClickable(driver, option);
                    break;
                }
            }
            waitUntilVisible(driver,
                             By.xpath(
                                     "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[1]")
            );
            List<WebElement> rows = driver.findElements(By.xpath(
                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr"));
            String eeCode = driver.getCurrentUrl()
                                  .replace("https://www.paycomonline.net/v4/cl/web.php/Doc/Form15/", "")
                                  .substring(0, 4);
            Path employeeDir = downloadDirPath.resolve(eeCode);
            try {
                Files.createDirectories(employeeDir);
            }
            catch (IOException ioException) {
                logger.fatal("Unable to create directory", ioException);
                System.exit(1);
            }
            String name = driver.findElement(By.xpath(
                    "/html/body/div[5]/div/div[1]/div[1]/div/div[1]/div[2]/div/div[1]/a")).getText();
            saveImage(driver.findElement(By.xpath(
                    "/html/body/div[5]/div/div[1]/div[1]/div/div[1]/div[1]/a/img"))
                            .getAttribute("src"),
                      employeeDir.resolve(eeCode + "_employee_photo_.jpg")
            );
            if (rows.size() > 1) {
                for (int i = 0, rowsSize = rows.size(); i < rowsSize; i++) {
                    String title1 = "";
                    String superAck = "";
                    String employeeAck = "";
                    for (int j = 0, colSize = driver.findElements(By.xpath(
                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                            (i + 1) +
                            "]/td")).size();
                         j < colSize;
                         j++
                    ) {
                        boolean contains;
                        try {
                            contains = driver.findElement(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/td[" +
                                    (j + 1) +
                                    "]"))
                                             .getText()
                                             .contains("...");
                        }
                        catch (StaleElementReferenceException e) {
                            sleep(2);
                            contains = driver.findElement(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/td[" +
                                    (j + 1) +
                                    "]"))
                                             .getText()
                                             .contains("...");
                        }
                        if (contains) {
                            List<WebElement> children = driver.findElements(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/td[" +
                                    (j + 1) +
                                    "]//*"));
                            WebElement child = children.get(children.size() - 1);
                            if (j == 1) {
                                try {
                                    title1 = child.getAttribute("title");
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    title1 = driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).get(driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).size() - 1).getAttribute("title");
                                }
                            }
                            else if (j == 4) {
                                try {
                                    employeeAck = child.getAttribute("title");
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    employeeAck = driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).get(driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).size() - 1).getAttribute("title");
                                }
                            }
                            else if (j == 5) {
                                try {
                                    superAck = child.getAttribute("title");
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    superAck = driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).get(driver.findElements(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/td[" +
                                            (j + 1) +
                                            "]//*")).size() - 1).getAttribute("title");
                                }
                            }
                        }
                        else {
                            if (j == 1) {
                                try {
                                    title1 = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                   .getText();
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    title1 = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                   .getText();
                                }
                            }
                            else if (j == 4) {
                                try {
                                    employeeAck = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                        .getText();
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    employeeAck = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                        .getText();
                                }
                            }
                            else if (j == 5) {
                                try {
                                    superAck = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                     .getText();
                                }
                                catch (StaleElementReferenceException e) {
                                    sleep(2000);
                                    superAck = driver.findElement(By.xpath(
                                            "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                            (i + 1) +
                                            "]/*[" +
                                            (j + 1) +
                                            "]"))
                                                     .getText();
                                }
                            }
                        }
                        boolean linkSize;
                        try {
                            linkSize = driver.findElements(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/*[" +
                                    (j + 1) +
                                    "]//*/a[@href]")).size() > 0;
                        }
                        catch (StaleElementReferenceException e) {
                            sleep(2000);
                            linkSize = driver.findElements(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/*[" +
                                    (j + 1) +
                                    "]//*/a[@href]")).size() > 0;
                        }
                        if (linkSize) {
                            List<WebElement> links = driver.findElements(By.xpath(
                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                    (i + 1) +
                                    "]/*[" +
                                    (j + 1) +
                                    "]//*/a[@href]"));
                            for (int k = 0, linksSize = links.size(); k < linksSize; k++) {
                                WebElement link = links.get(k);
                                if (!link.getAttribute("class").isBlank()) {
                                    if (link.getAttribute("class").contains("popoverTrigger")) {
                                        try {
                                            new Actions(driver).moveToElement(link).click(link).perform();
                                        }
                                        catch (StaleElementReferenceException e) {
                                            link = driver.findElements(By.xpath(
                                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                                    (i + 1) +
                                                    "]/td[" +
                                                    (j + 1) +
                                                    "]//*/a[@href]")).get(k);
                                            new Actions(driver).moveToElement(link).click(link).perform();
                                        }
                                        waitForLoad(driver);
                                        String img = "";
                                        String sig = "";
                                        try {
                                            img = driver.findElement(By.xpath(
                                                    "/html/body/div[7]/div[2]/div/div/div/div[2]/div[2]/div/img"))
                                                        .getAttribute("src");
                                        }
                                        catch (NoSuchElementException e) {
                                            try {
                                                img = driver.findElement(By.xpath(
                                                        "/html/body/div[7]/div[2]/div/div/div/div/div/div[2]/div/img"))
                                                            .getAttribute("src");
                                            }
                                            catch (NoSuchElementException exception) {
                                                try {
                                                    sig = driver.findElement(By.xpath(
                                                            "/html/body/div[7]/div[2]/div/div/div/div[2]/div[2]"))
                                                                .getText();
                                                }
                                                catch (NoSuchElementException exception1) {
                                                    try {
                                                        sig = driver.findElement(By.xpath(
                                                                "/html/body/div[7]/div[2]/div/div/div/div/div/div[2]"))
                                                                    .getText();
                                                    }
                                                    catch (NoSuchElementException exception2) {
                                                        sig = "";
                                                    }
                                                }
                                            }
                                        }
                                        if (!img.isBlank()) {
                                            if (j == 4) {
                                                saveImage(img,
                                                          employeeDir.resolve(eeCode +
                                                                              "_employee_acknowledgement_" +
                                                                              title1.toLowerCase(Locale.ROOT)
                                                                                    .replace(" ", "_") +
                                                                              ".jpg")
                                                );
                                            }
                                            else if (j == 5) {
                                                saveImage(img,
                                                          employeeDir.resolve(eeCode +
                                                                              "_supervisor_acknowledgement_" +

                                                                              title1.toLowerCase(Locale.ROOT)
                                                                                    .replace(" ", "_") +
                                                                              ".jpg")
                                                );
                                            }

                                        }
                                        else {
                                            if (j == 4) {
                                                saveSig(sig,
                                                        employeeDir.resolve(eeCode + "_employee_acknowledgement_" +
                                                                            title1.toLowerCase(Locale.ROOT)
                                                                                  .replace(" ", "_") +
                                                                            ".txt")
                                                );
                                            }
                                            else if (j == 5) {
                                                saveSig(sig,
                                                        employeeDir.resolve(eeCode +
                                                                            "_supervisor_acknowledgement_" +
                                                                            title1.toLowerCase(Locale.ROOT)
                                                                                  .replace(" ", "_") +
                                                                            ".txt")
                                                );
                                            }
                                        }
                                        try {
                                            new Actions(driver).moveToElement(link).click(link).perform();
                                        }
                                        catch (StaleElementReferenceException e) {
                                            sleep(2000);
                                            link = driver.findElements(By.xpath(
                                                    "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[3]/table/tbody/tr[" +
                                                    (i + 1) +
                                                    "]/td[" +
                                                    (j + 1) +
                                                    "]//*/a[@href]")).get(k);
                                            new Actions(driver).moveToElement(link).click(link).perform();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    List<String> list = new ArrayList<>();
                    list.add(name);
                    list.add(title1);
                    try {
                        list.add(outputFormat.format(simpleDateFormat.parse(employeeAck)));
                    }
                    catch (ParseException e) {
                        list.add(employeeAck);
                    }
                    try {
                        list.add(outputFormat.format(simpleDateFormat.parse(superAck)));
                    }
                    catch (ParseException e) {
                        list.add(superAck);
                    }
                    lists.add(list);
                }
                new Actions(driver).keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
                sleep(1000);
                waitUntilClickable(driver, By.xpath(
                        "//*[@id=\"ee-doc-table-select-all\"]"));
                waitUntilClickable(driver, By.xpath(
                        "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[1]/div[3]/span/div/div/a/span[1]"));
                waitUntilVisible(driver,
                                 By.xpath(
                                         "/html/body/div[5]/div/div[3]/div/div[1]/div[2]/div[2]/div[2]/div/div[1]/div[1]/span")
                );
                driver.findElement(By.xpath("//*[@id='documentDownload']/a")).click();
                final Instant instant = Instant.now();
                boolean downloaded = false;
                try {
                    new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(
                            "/html/body/div[5]/div/div[3]/div/div[2]/div[1]/div/div/div[3]/div[2]/input"))).click();
                    downloaded = true;
                }
                catch (Exception e) {
                    try {
                        new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(
                                "/html/body/div[5]/div/div[3]/div/div[2]/div/div/div/ul/li[1]/div/div/div[3]/div[2]/input")))
                                                     .click();
                        downloaded = true;
                    }
                    catch (Exception exception) {
                        logger.debug("Timeout", e);
                    }
                }
                waitForLoad(driver);
                if (downloaded) {
                    waitUntilFileDownloaded(driver, downloadDirPath, 60000, "\\S{5}_.*?_eeDocuments_.*?\\.zip");
                    sleep(1000);
                    File file = Objects.requireNonNull(downloadDirPath.toFile()
                                                                      .listFiles(pathname -> pathname.lastModified() >
                                                                                             instant.toEpochMilli()))[0];
                    String downloadName = eeCode + "_eeDocuments.zip";
                    file.renameTo(employeeDir.resolve(downloadName).toFile());
                }
                driver.findElement(By.xpath(
                        "/html/body/div[5]/div/div[3]/div/div[2]/div[1]/div/div/div[3]/div[2]/div[1]/div/input"))
                      .click();
                waitForLoad(driver);
            }
            progressBar.step();
            waitUntilClickable(driver, By.className("cdNextLink"));
        }
        writeCSV(downloadDirPath.resolve("extraction.csv"), lists);
        driver.close();
        progressBar.close();
    }

    public static void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1).executeScript(
                "return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public static void waitUntilClickable(WebDriver driver, By by) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
        waitForLoad(driver);
    }

    public static void waitUntilClickable(WebDriver driver, WebElement element) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(element)).click();
        waitForLoad(driver);
    }

    public static void waitUntilVisible(WebDriver driver, By by) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        try {
            driverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
        }
        catch (TimeoutException e) {
            logger.debug("Timeout", e);
        }
    }

    public static void saveImage(String src, Path file) {
        saveImage(src, file.toFile());
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void saveSig(String sig, Path file) {
        saveSig(sig, file.toFile());
    }

    public static void waitUntilFileDownloaded(WebDriver driver, Path file, long timeout, String pattern) {
        waitUntilFileDownloaded(driver, file.toFile(), timeout, pattern);
    }

    /**
     * Writes the CSV of the data tables
     *
     * @param filename The filename of the CSV
     * @param table    The table to write
     */
    public static void writeCSV(Path filename, List<List<String>> table) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename.toFile()));
            List<String[]> convertedTable = convertTableToTableArrayString(table);
            writer.writeAll(convertedTable);
            writer.close();
        }
        catch (IOException e) {
            logger.fatal("Unable to write CSV", e);
            System.exit(1);
        }
    }

    public static void saveImage(String src, File file) {
        if (src.contains("https://")) {
            try {
                URL url = new URL(src);
                FileUtils.copyURLToFile(url, file);
            }
            catch (MalformedURLException e) {
                logger.fatal("Invalid URL", e);
                System.exit(1);
            }
            catch (IOException e) {
                logger.fatal("Unable to save image", e);
                System.exit(1);
            }
        }
        else {
            byte[] imgBytes = Base64.getDecoder().decode(src.replaceAll("^\\S+base64,", ""));
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(imgBytes);
                fileOutputStream.close();
            }
            catch (IOException e) {
                logger.fatal("Unable to save image", e);
                System.exit(1);
            }
        }
    }

    public static void saveSig(String sig, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(sig);
            writer.close();
        }
        catch (IOException e) {
            logger.fatal("Unable to save signature", e);
            System.exit(1);
        }
    }

    public static void waitUntilFileDownloaded(WebDriver driver, File downloadDir, long timeout, String pattern) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofMillis(timeout))
                                                             .pollingEvery(Duration.ofMillis(200L));
        RegexFileFilter fileFilter = new RegexFileFilter(pattern);
        wait.until(driver1 -> {
            File[] files = downloadDir.listFiles((FileFilter) fileFilter);
            return (files != null && files.length > 0);
        });
    }

    /**
     * Converts List&lt;List&lt;String&gt;&gt; Table into List&lt;String[]&gt; Table
     *
     * @param table The table to convert
     *
     * @return The converted table
     */
    public static List<String[]> convertTableToTableArrayString(List<List<String>> table) {
        List<String[]> tableNew = new ArrayList<>();
        for (List<String> row : table) {
            String[] rowArray = row.toArray(new String[0]);
            tableNew.add(rowArray);
        }
        return tableNew;
    }

    public static void waitUntilVisible(WebDriver driver, WebElement element) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, 30);
        try {
            webDriverWait.until(ExpectedConditions.visibilityOf(element));
        }
        catch (TimeoutException e) {
            logger.debug("Timeout", e);
        }
    }
}
