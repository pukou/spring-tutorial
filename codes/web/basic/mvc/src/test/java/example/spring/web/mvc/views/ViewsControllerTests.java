package example.spring.web.mvc.views;

import example.spring.web.mvc.AbstractContextControllerTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class ViewsControllerTests extends AbstractContextControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac).alwaysExpect(status().isOk()).build();
    }

    @Test
    public void htmlView() throws Exception {
        this.mockMvc.perform(get("/views/html")).andExpect(view().name(containsString("views/html")))
                    .andExpect(model().attribute("foo", "bar")).andExpect(model().attribute("fruit", "apple"))
                    .andExpect(model().size(2));
    }

    @Test
    public void viewName() throws Exception {
        this.mockMvc.perform(get("/views/viewName")).andExpect(view().name(containsString("views/viewName")))
                    .andExpect(model().attribute("foo", "bar")).andExpect(model().attribute("fruit", "apple"))
                    .andExpect(model().size(2));
    }

    @Test
    public void uriTemplate() throws Exception {
        this.mockMvc.perform(get("/views/pathVariables/bar/apple"))
                    .andExpect(view().name(containsString("views/html")));
    }

}
