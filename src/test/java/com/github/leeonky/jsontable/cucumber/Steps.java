package com.github.leeonky.jsontable.cucumber;

import com.github.leeonky.util.NumberContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;

public class Steps {
    private String inputCode;
    private final JsonTableParser jsonTableParser = new JsonTableParser();

    @Given("the following code:")
    public void the_following_code(String json) {
        inputCode = json;
    }

    @Then("got the following data:")
    public void got_the_following_data(String assertion) {
        expect(jsonTableParser.parse(inputCode)).should(assertion);
    }

    @Then("got the following number:")
    public void got_the_following_number(String assertion) {
        expect(NumberContext.parseNumber(inputCode)).should(assertion);
    }

    public static class JsonTableParser {
        public Object parse(String content) {
            if (content.equals("null"))
                return null;
            if (content.equals("true"))
                return true;
            if (content.equals("false"))
                return false;
            return NumberContext.parseNumber(content);
        }
    }
}
