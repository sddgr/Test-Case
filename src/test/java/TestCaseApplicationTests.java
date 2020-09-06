import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.get;
import static org.hamcrest.core.IsIterableContaining.hasItem;

public class TestCaseApplicationTests {

    static Properties properties;
    public String apiKey;
    public String apiUrl;
    public String imdbId;
    public String searchText;
    public String movieTitle;

    @BeforeTest
    public void setUp() {
        properties = new Properties();
        InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        apiKey = properties.getProperty("apiKey");
        apiUrl = properties.getProperty("apiUrl");
        searchText = properties.getProperty("searchText");
        movieTitle = properties.getProperty("movieTitle");

        System.out.println("Test is started");
    }

    @Test
    public void givenMovieName_checkExists_thenGetImbdID_thenCorrect() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(apiUrl + "?s=" + searchText + "&apikey=" + apiKey);

        Response response = get(stringBuilder.toString())
                .then().log().ifError().assertThat().statusCode(200)
                .body("Search.Title", hasItem(movieTitle))
                .extract().response();

        List<HashMap<String, Object>> getResponseList = response.jsonPath().getList("Search");
        for (int i = 0; i < getResponseList.size(); i++) {
            if (getResponseList.get(i).get("Title").toString().equals(movieTitle)) {
                imdbId = getResponseList.get(i).get("imdbID").toString();
            }
        }
    }

    @Test(dependsOnMethods = {"givenMovieName_checkExists_thenGetImbdID_thenCorrect"})
    public void givenImdbId_thenCheckResponse_thenCorrect() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(apiUrl + "?i=" + imdbId + "&apikey=" + apiKey);
        Response response = get(stringBuilder.toString())
                .then().log().ifError().assertThat().statusCode(200)
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.path("Released"));
        Assert.assertNotNull(response.path("Year"));
        Assert.assertNotNull(response.path("Title"));
    }

    @AfterTest
    public void close() {
        System.out.println("Test is finished");
    }
}
