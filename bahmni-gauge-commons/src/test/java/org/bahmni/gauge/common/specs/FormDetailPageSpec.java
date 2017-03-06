package org.bahmni.gauge.common.specs;

import com.thoughtworks.gauge.BeforeClassSteps;
import com.thoughtworks.gauge.Step;
import org.bahmni.gauge.common.BahmniPage;
import org.bahmni.gauge.common.DriverFactory;
import org.bahmni.gauge.common.PageFactory;
import org.bahmni.gauge.common.formBuilder.FormDetailPage;
import org.bahmni.gauge.common.formBuilder.domain.Form;
import org.bahmni.gauge.data.StoreHelper;
import org.bahmni.gauge.rest.BahmniRestClient;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FormDetailPageSpec {
    private final WebDriver driver;
    private FormDetailPage formDetailPage;

    public FormDetailPageSpec() {
        this.driver = DriverFactory.getDriver();
    }

    @BeforeClassSteps
    public void waitForAppReady() {
        BahmniPage.waitForSpinner(driver);
    }

    @Step("Click on Edit")
    public void showConfirmEdit() {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        formDetailPage.clickOnEdit();
    }

    @Step("Confirm edit")
    public void goToEditModal() {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        formDetailPage.clickOnOK();
    }

    @Step("Navigate to form list")
    public void goToFormList() {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        formDetailPage.clickOnFormBuilder();
    }

    @Step("Click on publish")
    public void publishForm() {
        String uuid = getUuid();
        Form form = new Form();
        form.setUuid(uuid);
        StoreHelper.store(Form.class, form);
        formDetailPage = PageFactory.get(FormDetailPage.class);
        formDetailPage.clickOnPublish();
    }

    @Step("Click on save")
    public void saveForm() {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        formDetailPage.clickOnSave();
    }

    @Step("Select <propertyType> property for <controlName>")
    public void setPropertyForControl(String propertyType, String controlName) {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        List<WebElement> labelList = formDetailPage.getCanvasBodyLabelList();
        WebElement control = getControl(labelList, controlName);

        Assert.assertTrue("No " + controlName + "in canvas", control != null);

        formDetailPage.clickOnControl(control);
        formDetailPage.clickOnProperty(propertyType);
    }

    private WebElement getControl(List<WebElement> labelList, String controlName) {
        for (int i = 0; i < labelList.size(); i++) {
            if(labelList.get(i).getText().equals(controlName)) {
                return labelList.get(i);
            }
        }
        return null;
    }

    @Step("Save <formName> form using <formModleName> by API")
    public void saveFormByAPI(String formName, String formModelName) {
        String uuid = getUuid();
        Map<String, Object> formAttributes = new HashMap<>();
        formAttributes.put("name", formName);
        formAttributes.put("uuid", uuid);
        BahmniRestClient.get().saveFormUsingAPI(("form_" + formModelName + "_save.ftl"), formAttributes);
    }

    @Step("Verify form is <version> version and <statusValue> status")
    public void verifyFormVersionAndStatus(String version, String statusValue) {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        String[] parsedFormInfo = parseVersionAndStatus(formDetailPage.getFormInfo());
        Assert.assertTrue("Version or status is not correct",
                (parsedFormInfo[0].equals(version) && parsedFormInfo[1].equals(statusValue)));
    }

    @Step("Verify canvas has <labelName> label")
    public void verifyFormHasLabel(String labelName) {
        formDetailPage = PageFactory.get(FormDetailPage.class);
        List<WebElement> labelList = formDetailPage.getCanvasBodyLabelList();
        Assert.assertTrue("Canvas don't have " + labelName,
                hasLabel(labelList, labelName));
    }

    private boolean hasLabel(List<WebElement> labelList, String labelName) {
        for(int i = 0; i < labelList.size(); i++) {
            if(labelList.get(i).getText().equals(labelName)) {
                return true;
            }
        }
        return false;
    }

    private String[] parseVersionAndStatus(String formInfo) {
        String[] splitInfo = formInfo.split(" - ");
        String[] splitFirstHarf = splitInfo[0].split(" ");
        splitInfo[0] = splitFirstHarf[splitFirstHarf.length - 1];
        return splitInfo;
    }

    private String getUuid() {
        String[] splitedString = driver.getCurrentUrl().split("/");
        return splitedString[splitedString.length - 1];
    }
}