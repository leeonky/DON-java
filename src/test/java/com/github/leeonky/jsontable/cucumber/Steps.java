package com.github.leeonky.jsontable.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;

public class Steps {
    private String json;
    private JsonTableParser jsonTableParser = new JsonTableParser();

    @Given("the following code:")
    public void the_following_code(String json) {
        this.json = json;
    }

    @Then("got the following data:")
    public void got_the_following_data(String assertion) {
        expect(jsonTableParser.parse(json)).should(assertion);
    }

    public static class JsonTableParser {
        public Object parse(CharSequence content) {
            if (content.equals("null"))
                return null;
            if (content.equals("true"))
                return true;
            return false;
        }
    }
}
